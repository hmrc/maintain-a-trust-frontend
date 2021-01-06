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
import mapping.PlaybackExtractionErrors.{FailedToExtractData, PlaybackExtractionError}
import mapping.PlaybackExtractor
import models.{InternationalAddress, MetaData, UKAddress, UserAnswers}
import models.http.DisplayTrustOtherType
import pages.beneficiaries.other._
import play.api.Logging
import scala.util.{Failure, Success, Try}
import mapping.PlaybackImplicits._

class OtherBeneficiaryExtractor @Inject() extends PlaybackExtractor[Option[List[DisplayTrustOtherType]]] with Logging {

  override def extract(answers: UserAnswers, data: Option[List[DisplayTrustOtherType]]): Either[PlaybackExtractionError, UserAnswers] =
    {
      data match {
        case None => Left(FailedToExtractData("No Other Beneficiary"))
        case Some(others) =>

          val updated = others.zipWithIndex.foldLeft[Try[UserAnswers]](Success(answers)){
            case (answers, (otherBeneficiary, index)) =>

            answers
              .flatMap(_.set(OtherBeneficiaryDescriptionPage(index), otherBeneficiary.description))
              .flatMap(answers => extractShareOfIncome(otherBeneficiary, index, answers))
              .flatMap(answers => extractAddress(otherBeneficiary, index, answers))
              .flatMap {
                _.set(
                  OtherBeneficiaryMetaData(index),
                  MetaData(
                    lineNo = otherBeneficiary.lineNo.getOrElse(""),
                    bpMatchStatus = otherBeneficiary.bpMatchStatus.fold(Some("98"))(x => Some(x)),
                    entityStart = otherBeneficiary.entityStart
                  )
                )
              }
          }

          updated match {
            case Success(a) =>
              Right(a)
            case Failure(exception) =>
              logger.warn(s"[UTR: ${answers.utr}] failed to extract data due to ${exception.getMessage}")
              Left(FailedToExtractData(DisplayTrustOtherType.toString))
          }
      }
    }

  private def extractShareOfIncome(otherBeneficiary: DisplayTrustOtherType, index: Int, answers: UserAnswers) = {
    otherBeneficiary.beneficiaryShareOfIncome match {
      case Some(income) =>
        answers.set(OtherBeneficiaryDiscretionYesNoPage(index), false)
          .flatMap(_.set(OtherBeneficiaryShareOfIncomePage(index), income))
      case None =>
        // Assumption that user answered yes as the share of income is not provided
        answers.set(OtherBeneficiaryDiscretionYesNoPage(index), true)
    }
  }

  private def extractAddress(otherBeneficiary: DisplayTrustOtherType, index: Int, answers: UserAnswers) = {
    otherBeneficiary.address.convert match {
      case Some(uk: UKAddress) =>
        answers.set(OtherBeneficiaryAddressPage(index), uk)
          .flatMap(_.set(OtherBeneficiaryAddressYesNoPage(index), true))
          .flatMap(_.set(OtherBeneficiaryAddressUKYesNoPage(index), true))
      case Some(nonUk: InternationalAddress) =>
        answers.set(OtherBeneficiaryAddressPage(index), nonUk)
          .flatMap(_.set(OtherBeneficiaryAddressYesNoPage(index), true))
          .flatMap(_.set(OtherBeneficiaryAddressUKYesNoPage(index), false))
      case None =>
        answers.set(OtherBeneficiaryAddressYesNoPage(index), false)
    }
  }
}