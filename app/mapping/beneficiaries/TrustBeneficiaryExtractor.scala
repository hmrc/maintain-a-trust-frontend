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
import models.http._
import pages.beneficiaries.trust._
import play.api.Logger
import scala.util.{Failure, Success, Try}
import mapping.PlaybackImplicits._

class TrustBeneficiaryExtractor @Inject() extends PlaybackExtractor[Option[List[DisplayTrustBeneficiaryTrustType]]] {

  private val logger: Logger = Logger(getClass)

  override def extract(answers: UserAnswers, data: Option[List[DisplayTrustBeneficiaryTrustType]]): Either[PlaybackExtractionError, UserAnswers] =
    {
      data match {
        case None => Left(FailedToExtractData("No Trust Beneficiary"))
        case Some(trusts) =>

          val updated = trusts.zipWithIndex.foldLeft[Try[UserAnswers]](Success(answers)){
            case (answers, (trustBeneficiary, index)) =>

            answers
              .flatMap(_.set(TrustBeneficiaryNamePage(index), trustBeneficiary.organisationName))
              .flatMap(answers => extractShareOfIncome(trustBeneficiary, index, answers))
              .flatMap(_.set(TrustBeneficiarySafeIdPage(index), trustBeneficiary.identification.flatMap(_.safeId)))
              .flatMap(answers => extractIdentification(trustBeneficiary.identification, index, answers))
              .flatMap {
                _.set(
                  TrustBeneficiaryMetaData(index),
                  MetaData(
                    lineNo = trustBeneficiary.lineNo.getOrElse(""),
                    bpMatchStatus = trustBeneficiary.bpMatchStatus,
                    entityStart = trustBeneficiary.entityStart
                  )
                )
              }
          }

          updated match {
            case Success(a) =>
              Right(a)
            case Failure(exception) =>
              logger.warn(s"[UTR: ${answers.utr}] failed to extract data due to ${exception.getMessage}")
              Left(FailedToExtractData(DisplayTrustBeneficiaryTrustType.toString))
          }
      }
    }

  private def extractShareOfIncome(trustBeneficiary: DisplayTrustBeneficiaryTrustType, index: Int, answers: UserAnswers) = {
    trustBeneficiary.beneficiaryShareOfIncome match {
      case Some(income) =>
        answers.set(TrustBeneficiaryDiscretionYesNoPage(index), false)
          .flatMap(_.set(TrustBeneficiaryShareOfIncomePage(index), income))
      case None =>
        // Assumption that user answered yes as the share of income is not provided
        answers.set(TrustBeneficiaryDiscretionYesNoPage(index), true)
    }
  }

  private def extractIdentification(identification: Option[DisplayTrustIdentificationOrgType], index: Int, answers: UserAnswers) = {
    identification map {
      case DisplayTrustIdentificationOrgType(_, Some(utr), None) =>
        answers.set(TrustBeneficiaryUtrPage(index), utr)
          .flatMap(_.set(TrustBeneficiaryAddressYesNoPage(index), false))

      case DisplayTrustIdentificationOrgType(_, None, Some(address)) =>
        extractAddress(address.convert, index, answers)

      case _ =>
        logger.error(s"[UTR: ${answers.utr}] both utr and address parsed")
        Failure(InvalidExtractorState)

    } getOrElse {
      answers.set(TrustBeneficiaryAddressYesNoPage(index), false)
    }
  }

  private def extractAddress(address: Address, index: Int, answers: UserAnswers) = {
    address match {
      case uk: UKAddress =>
        answers.set(TrustBeneficiaryAddressPage(index), uk)
          .flatMap(_.set(TrustBeneficiaryAddressYesNoPage(index), true))
          .flatMap(_.set(TrustBeneficiaryAddressUKYesNoPage(index), true))
      case nonUk: InternationalAddress =>
        answers.set(TrustBeneficiaryAddressPage(index), nonUk)
          .flatMap(_.set(TrustBeneficiaryAddressYesNoPage(index), true))
          .flatMap(_.set(TrustBeneficiaryAddressUKYesNoPage(index), false))
    }
  }

}