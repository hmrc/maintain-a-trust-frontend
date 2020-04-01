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
import models.http.{DisplayTrustCharityType, DisplayTrustIdentificationOrgType}
import pages.beneficiaries.charity._
import play.api.Logger
import scala.util.{Failure, Success, Try}
import mapping.PlaybackImplicits._

class CharityBeneficiaryExtractor @Inject() extends PlaybackExtractor[Option[List[DisplayTrustCharityType]]] {

  override def extract(answers: UserAnswers, data: Option[List[DisplayTrustCharityType]]): Either[PlaybackExtractionError, UserAnswers] = {

    data match {
      case None => Left(FailedToExtractData("No Charity Beneficiary"))
      case Some(charities) =>

        Logger.debug(s"[CharityBeneficiaryExtractor] extracting $charities")

        val updated = charities.zipWithIndex.foldLeft[Try[UserAnswers]](Success(answers)){
          case (answers, (charityBeneficiary, index)) =>

          answers
            .flatMap(_.set(CharityBeneficiaryNamePage(index), charityBeneficiary.organisationName))
            .flatMap(answers => extractShareOfIncome(charityBeneficiary, index, answers))
            .flatMap(_.set(CharityBeneficiarySafeIdPage(index), charityBeneficiary.identification.flatMap(_.safeId)))
            .flatMap(answers => extractIdentification(charityBeneficiary.identification, index, answers))
            .flatMap {
              _.set(
                CharityBeneficiaryMetaData(index),
                MetaData(
                  lineNo = charityBeneficiary.lineNo.getOrElse(""),
                  bpMatchStatus = charityBeneficiary.bpMatchStatus,
                  entityStart = charityBeneficiary.entityStart
                )
              )
            }
        }

        updated match {
          case Success(a) =>
            Right(a)
          case Failure(exception) =>
            Logger.warn(s"[CharityBeneficiaryExtractor] failed to extract data due to ${exception.getMessage}")
            Left(FailedToExtractData(DisplayTrustCharityType.toString))
        }
    }
  }

  private def extractIdentification(identification: Option[DisplayTrustIdentificationOrgType], index: Int, answers: UserAnswers) = {
    identification map {
      case DisplayTrustIdentificationOrgType(_, Some(utr), None) =>
        answers.set(CharityBeneficiaryUtrPage(index), utr)
          .flatMap(_.set(CharityBeneficiaryAddressYesNoPage(index), false))

      case DisplayTrustIdentificationOrgType(_, None, Some(address)) =>
        extractAddress(address.convert, index, answers)

      case _ =>
        Logger.error(s"[CharityBeneficiaryExtractor] both utr and address parsed")
        Failure(InvalidExtractorState)

    } getOrElse {
      answers.set(CharityBeneficiaryAddressYesNoPage(index), false)
    }
  }

  private def extractShareOfIncome(charityBeneficiary: DisplayTrustCharityType, index: Int, answers: UserAnswers) = {
    charityBeneficiary.beneficiaryShareOfIncome match {
      case Some(income) =>
        answers.set(CharityBeneficiaryDiscretionYesNoPage(index), false)
          .flatMap(_.set(CharityBeneficiaryShareOfIncomePage(index), income))
      case None =>
        // Assumption that user answered yes as the share of income is not provided
        answers.set(CharityBeneficiaryDiscretionYesNoPage(index), true)
    }
  }

  private def extractAddress(address: Address, index: Int, answers: UserAnswers) = {
    address match {
      case uk: UKAddress =>
        answers.set(CharityBeneficiaryAddressPage(index), uk)
          .flatMap(_.set(CharityBeneficiaryAddressYesNoPage(index), true))
          .flatMap(_.set(CharityBeneficiaryAddressUKYesNoPage(index), true))
      case nonUk: InternationalAddress =>
        answers.set(CharityBeneficiaryAddressPage(index), nonUk)
          .flatMap(_.set(CharityBeneficiaryAddressYesNoPage(index), true))
          .flatMap(_.set(CharityBeneficiaryAddressUKYesNoPage(index), false))
    }
  }
}