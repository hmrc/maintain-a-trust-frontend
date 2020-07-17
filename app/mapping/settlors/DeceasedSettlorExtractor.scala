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

package mapping.settlors

import com.google.inject.Inject
import mapping.PlaybackExtractionErrors.{FailedToExtractData, PlaybackExtractionError}
import mapping.PlaybackExtractor
import models.{Address, InternationalAddress, MetaData, PassportOrIdCardDetails, UKAddress, UserAnswers}
import models.http.{DisplayTrustIdentificationType, DisplayTrustWillType}
import pages.settlors.deceased_settlor._
import play.api.Logger

import scala.util.{Failure, Success, Try}
import mapping.PlaybackImplicits._

class DeceasedSettlorExtractor @Inject() extends PlaybackExtractor[Option[DisplayTrustWillType]] {

  override def extract(answers: UserAnswers, data: Option[DisplayTrustWillType]): Either[PlaybackExtractionError, UserAnswers] =
    {
      data match {
        case None => Left(FailedToExtractData("No Deceased Settlor"))
        case deceasedSettlor =>

          val updated = deceasedSettlor.foldLeft[Try[UserAnswers]](Success(answers)){
            case (answers, deceasedSettlor) =>

            answers
              .flatMap(_.set(SettlorNamePage, deceasedSettlor.name.convert))
              .flatMap(answers => extractDateOfDeath(deceasedSettlor, answers))
              .flatMap(answers => extractDateOfBirth(deceasedSettlor, answers))
              .flatMap(answers => extractIdentification(deceasedSettlor.identification, answers))
              .flatMap(_.set(DeceasedSettlorSafeIdPage, deceasedSettlor.identification.flatMap(_.safeId)))
              .flatMap {
                _.set(
                  DeceasedSettlorMetaData,
                  MetaData(
                    lineNo = deceasedSettlor.lineNo,
                    bpMatchStatus = deceasedSettlor.bpMatchStatus,
                    entityStart = deceasedSettlor.entityStart
                  )
                )
              }
          }

          updated match {
            case Success(a) =>
              Right(a)
            case Failure(exception) =>
              Logger.warn(s"[DeceasedSettlorExtractor] failed to extract data due to ${exception.getMessage}")
              Left(FailedToExtractData(DisplayTrustWillType.toString))
          }
      }
    }

  private def extractDateOfDeath(deceasedSettlor: DisplayTrustWillType, answers: UserAnswers) = {
    deceasedSettlor.dateOfDeath match {
      case Some(dateOfDeath) =>
        answers.set(SettlorDateOfDeathYesNoPage, true)
          .flatMap(_.set(SettlorDateOfDeathPage, dateOfDeath.convert))
      case None =>
        // Assumption that user answered no as the date of death is not provided
        answers.set(SettlorDateOfDeathYesNoPage, false)
    }
  }

  private def extractDateOfBirth(deceasedSettlor: DisplayTrustWillType, answers: UserAnswers) = {
    deceasedSettlor.dateOfBirth match {
      case Some(dateOfBirth) =>
        answers.set(SettlorDateOfBirthYesNoPage, true)
          .flatMap(_.set(SettlorDateOfBirthPage, dateOfBirth.convert))
      case None =>
        // Assumption that user answered no as the date of birth is not provided
        answers.set(SettlorDateOfBirthYesNoPage, false)
    }
  }

  private def extractIdentification(identification : Option[DisplayTrustIdentificationType], answers: UserAnswers) = {
    identification map {
      case DisplayTrustIdentificationType(_, Some(nino), None, None) =>
        extractNino(nino, answers)

      case DisplayTrustIdentificationType(_, None, Some(passport), Some(address)) =>
        extractPassportIdCard(passport.convert, answers)
          .flatMap(updated => extractAddress(address.convert, updated))

      case DisplayTrustIdentificationType(_, None, None, Some(address)) =>
        extractAddress(address.convert, answers)

      case DisplayTrustIdentificationType(_, None, Some(passport), None) =>
        extractPassportIdCard(passport.convert, answers)

      case _ =>
        // just a safeId returned
        answers.set(SettlorNationalInsuranceYesNoPage, false)
          .flatMap(_.set(SettlorLastKnownAddressYesNoPage, false))

    } getOrElse {
      answers.set(SettlorNationalInsuranceYesNoPage, false)
        .flatMap(_.set(SettlorLastKnownAddressYesNoPage, false))
    }
  }

  private def extractNino(nino: String, answers: UserAnswers) = {
    answers.set(SettlorNationalInsuranceNumberPage, nino)
      .flatMap(_.set(SettlorNationalInsuranceYesNoPage, true))
  }

  private def extractAddress(address: Address, answers: UserAnswers) = {
    address match {
      case uk: UKAddress =>
        answers
          .set(SettlorNationalInsuranceYesNoPage, false)
          .flatMap(_.set(SettlorUKAddressPage, uk))
          .flatMap(_.set(SettlorLastKnownAddressYesNoPage, true))
          .flatMap(_.set(SettlorLastKnownAddressUKYesNoPage, true))
      case nonUk: InternationalAddress =>
        answers
          .set(SettlorNationalInsuranceYesNoPage, false)
          .flatMap(_.set(SettlorInternationalAddressPage, nonUk))
          .flatMap(_.set(SettlorLastKnownAddressYesNoPage, true))
          .flatMap(_.set(SettlorLastKnownAddressUKYesNoPage, false))
    }
  }

  private def extractPassportIdCard(passport: PassportOrIdCardDetails, answers: UserAnswers) =
    answers
      .set(SettlorPassportIDCardPage, passport)
      .flatMap(_.set(SettlorNationalInsuranceYesNoPage, false))
      .flatMap(_.set(SettlorLastKnownAddressYesNoPage, false))

}