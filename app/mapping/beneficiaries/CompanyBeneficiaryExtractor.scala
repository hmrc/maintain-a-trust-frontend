/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mapping.beneficiaries

import com.google.inject.Inject
import mapping.PlaybackExtractionErrors.{FailedToExtractData, InvalidExtractorState, PlaybackExtractionError}
import mapping.PlaybackExtractor
import models.{Address, InternationalAddress, MetaData, UKAddress, UserAnswers}
import models.http.{DisplayTrustCompanyType, DisplayTrustIdentificationOrgType}
import pages.beneficiaries.company._
import play.api.Logging
import mapping.PlaybackImplicits._

import scala.util.{Failure, Success, Try}

class CompanyBeneficiaryExtractor @Inject() extends PlaybackExtractor[Option[List[DisplayTrustCompanyType]]] with Logging {

  override def extract(answers: UserAnswers, data: Option[List[DisplayTrustCompanyType]]): Either[PlaybackExtractionError, UserAnswers] =
    {
      data match {
        case None => Left(FailedToExtractData("No Company Beneficiary"))
        case Some(companies) =>

          val updated = companies.zipWithIndex.foldLeft[Try[UserAnswers]](Success(answers)){
            case (answers, (companyBeneficiary, index)) =>

            answers
              .flatMap(_.set(CompanyBeneficiaryNamePage(index), companyBeneficiary.organisationName))
              .flatMap(answers => extractShareOfIncome(companyBeneficiary, index, answers))
              .flatMap(_.set(CompanyBeneficiarySafeIdPage(index), companyBeneficiary.identification.flatMap(_.safeId)))
              .flatMap(answers => extractIdentification(companyBeneficiary.identification, index, answers))
              .flatMap {
                _.set(
                  CompanyBeneficiaryMetaData(index),
                  MetaData(
                    lineNo = companyBeneficiary.lineNo.getOrElse(""),
                    bpMatchStatus = companyBeneficiary.bpMatchStatus,
                    entityStart = companyBeneficiary.entityStart
                  )
                )
              }
          }

          updated match {
            case Success(a) =>
              Right(a)
            case Failure(exception) =>
              logger.warn(s"[UTR: ${answers.utr}] failed to extract data due to ${exception.getMessage}")
              Left(FailedToExtractData(DisplayTrustCompanyType.toString))
          }
      }
    }

  private def extractIdentification(identification: Option[DisplayTrustIdentificationOrgType], index: Int, answers: UserAnswers) = {
    identification map {
      case DisplayTrustIdentificationOrgType(_, Some(utr), None) =>
        answers.set(CompanyBeneficiaryUtrPage(index), utr)
          .flatMap(_.set(CompanyBeneficiaryAddressYesNoPage(index), false))

      case DisplayTrustIdentificationOrgType(_, None, Some(address)) =>
        extractAddress(address.convert, index, answers)

      case _ =>
        logger.error(s"[UTR: ${answers.utr}] only both utr and address parsed")
        Failure(InvalidExtractorState)

    } getOrElse {
      answers.set(CompanyBeneficiaryAddressYesNoPage(index), false)
    }
  }

  private def extractShareOfIncome(companyBeneficiary: DisplayTrustCompanyType, index: Int, answers: UserAnswers) = {
    companyBeneficiary.beneficiaryShareOfIncome match {
      case Some(income) =>
        answers.set(CompanyBeneficiaryDiscretionYesNoPage(index), false)
          .flatMap(_.set(CompanyBeneficiaryShareOfIncomePage(index), income))
      case None =>
        // Assumption that user answered yes as the share of income is not provided
        answers.set(CompanyBeneficiaryDiscretionYesNoPage(index), true)
    }
  }

  private def extractAddress(address: Address, index: Int, answers: UserAnswers) = {
    address match {
      case uk: UKAddress =>
        answers.set(CompanyBeneficiaryAddressPage(index), uk)
          .flatMap(_.set(CompanyBeneficiaryAddressYesNoPage(index), true))
          .flatMap(_.set(CompanyBeneficiaryAddressUKYesNoPage(index), true))
      case nonUk: InternationalAddress =>
        answers.set(CompanyBeneficiaryAddressPage(index), nonUk)
          .flatMap(_.set(CompanyBeneficiaryAddressYesNoPage(index), true))
          .flatMap(_.set(CompanyBeneficiaryAddressUKYesNoPage(index), false))
    }
  }
}