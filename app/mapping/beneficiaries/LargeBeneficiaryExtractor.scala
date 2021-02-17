/*
 * Copyright 2021 HM Revenue & Customs
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
import mapping.PlaybackImplicits._
import models.HowManyBeneficiaries.{Over1, Over1001, Over101, Over201, Over501}
import models.http.{DisplayTrustCompanyType, DisplayTrustIdentificationOrgType, DisplayTrustLargeType}
import models.{Address, Description, InternationalAddress, MetaData, UKAddress, UserAnswers}
import pages.beneficiaries.large._
import play.api.Logging

import scala.util.{Failure, Success, Try}

class LargeBeneficiaryExtractor @Inject() extends PlaybackExtractor[Option[List[DisplayTrustLargeType]]] with Logging {

  override def extract(answers: UserAnswers, data: Option[List[DisplayTrustLargeType]]): Either[PlaybackExtractionError, UserAnswers] =
    {
      data match {
        case None => Left(FailedToExtractData("No Large Beneficiary"))
        case Some(largeBeneficiaries) =>

          val updated = largeBeneficiaries.zipWithIndex.foldLeft[Try[UserAnswers]](Success(answers)) {
            case (answers, (largeBeneficiary, index)) =>

              answers
                .flatMap(_.set(LargeBeneficiaryNamePage(index), largeBeneficiary.organisationName))
                .flatMap(answers => extractShareOfIncome(largeBeneficiary, index, answers))
                .flatMap(answers => extractIdentification(largeBeneficiary.identification, index, answers))
                .flatMap(
                  _.set(
                    LargeBeneficiaryDescriptionPage(index),
                    Description(
                      largeBeneficiary.description,
                      largeBeneficiary.description1,
                      largeBeneficiary.description2,
                      largeBeneficiary.description3,
                      largeBeneficiary.description4
                    )
                  )
                )
                .flatMap(answers => extractNumberOfBeneficiaries(largeBeneficiary.numberOfBeneficiary, index, answers))
                .flatMap(_.set(LargeBeneficiarySafeIdPage(index), largeBeneficiary.identification.flatMap(_.safeId)))
                .flatMap {
                  _.set(
                    LargeBeneficiaryMetaData(index),
                    MetaData(
                      lineNo = largeBeneficiary.lineNo.getOrElse(""),
                      bpMatchStatus = largeBeneficiary.bpMatchStatus,
                      entityStart = largeBeneficiary.entityStart
                    )
                  )
                }
          }

          updated match {
            case Success(a) =>
              Right(a)
            case Failure(exception) =>
              logger.warn(s"[UTR/URN: ${answers.identifier}] failed to extract data due to ${exception.getMessage}")
              Left(FailedToExtractData(DisplayTrustCompanyType.toString))
          }
      }
    }

  private def extractIdentification(identification: Option[DisplayTrustIdentificationOrgType], index: Int, answers: UserAnswers) = {
    identification map {
      case DisplayTrustIdentificationOrgType(_, Some(utr), None) =>
        answers.set(LargeBeneficiaryUtrPage(index), utr)
          .flatMap(_.set(LargeBeneficiaryAddressYesNoPage(index), false))

      case DisplayTrustIdentificationOrgType(_, None, Some(address)) =>
        extractAddress(address.convert, index, answers)

      case _ =>
        logger.error(s"[UTR/URN: ${answers.identifier}] only both utr and address parsed")
        Failure(InvalidExtractorState)

    } getOrElse {
      answers.set(LargeBeneficiaryAddressYesNoPage(index), false)
    }
  }

  private def extractShareOfIncome(largeBeneficiary: DisplayTrustLargeType, index: Int, answers: UserAnswers) = {
    largeBeneficiary.beneficiaryShareOfIncome match {
      case Some(income) =>
        answers.set(LargeBeneficiaryDiscretionYesNoPage(index), false)
          .flatMap(_.set(LargeBeneficiaryShareOfIncomePage(index), income))
      case None =>
        // Assumption that user answered yes as the share of income is not provided
        answers.set(LargeBeneficiaryDiscretionYesNoPage(index), true)
    }
  }

  private def extractNumberOfBeneficiaries(numberOfBeneficiary: String, index: Int, answers: UserAnswers): Try[UserAnswers] = {
    numberOfBeneficiary.toInt match {
      case x if 0 to 100 contains x => answers.set(LargeBeneficiaryNumberOfBeneficiariesPage(index), Over1)
      case x if 101 to 200 contains x => answers.set(LargeBeneficiaryNumberOfBeneficiariesPage(index), Over101)
      case x if 201 to 500 contains x => answers.set(LargeBeneficiaryNumberOfBeneficiariesPage(index), Over201)
      case x if 501 to 999 contains x => answers.set(LargeBeneficiaryNumberOfBeneficiariesPage(index), Over501)
      case _ => answers.set(LargeBeneficiaryNumberOfBeneficiariesPage(index), Over1001)
    }
  }

  private def extractAddress(address: Address, index: Int, answers: UserAnswers) = {
    address match {
      case uk: UKAddress =>
        answers.set(LargeBeneficiaryAddressPage(index), uk)
          .flatMap(_.set(LargeBeneficiaryAddressYesNoPage(index), true))
          .flatMap(_.set(LargeBeneficiaryAddressUKYesNoPage(index), true))
      case nonUk: InternationalAddress =>
        answers.set(LargeBeneficiaryAddressPage(index), nonUk)
          .flatMap(_.set(LargeBeneficiaryAddressYesNoPage(index), true))
          .flatMap(_.set(LargeBeneficiaryAddressUKYesNoPage(index), false))
    }
  }
}