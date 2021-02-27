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

package mapping.settlors

import mapping.PlaybackExtractor
import mapping.PlaybackImplicits._
import models.http.{DisplayTrustIdentificationType, DisplayTrustWillType}
import models.{Address, InternationalAddress, MetaData, PassportOrIdCardDetails, UKAddress, UserAnswers}
import pages.QuestionPage
import pages.settlors.deceased_settlor._

import scala.util.Try

class DeceasedSettlorExtractor extends PlaybackExtractor[DisplayTrustWillType] {

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = SettlorLastKnownAddressYesNoPage
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = SettlorLastKnownAddressUKYesNoPage

  override def updateUserAnswers(answers: Try[UserAnswers], entity: DisplayTrustWillType, index: Int): Try[UserAnswers] = {
    answers
      .flatMap(_.set(SettlorNamePage, entity.name))
      .flatMap(answers => extractDateOfDeath(entity, answers))
      .flatMap(answers => extractDateOfBirth(entity, answers))
      .flatMap(answers => extractIdentification(entity.identification, answers))
      .flatMap(_.set(DeceasedSettlorSafeIdPage, entity.identification.flatMap(_.safeId)))
      .flatMap {
        _.set(
          DeceasedSettlorMetaData,
          MetaData(
            lineNo = entity.lineNo,
            bpMatchStatus = entity.bpMatchStatus,
            entityStart = entity.entityStart
          )
        )
      }
  }

  private def extractDateOfDeath(deceasedSettlor: DisplayTrustWillType, answers: UserAnswers) = {
    deceasedSettlor.dateOfDeath match {
      case Some(dateOfDeath) =>
        answers.set(SettlorDateOfDeathYesNoPage, true)
          .flatMap(_.set(SettlorDateOfDeathPage, dateOfDeath))
      case None =>
        // Assumption that user answered no as the date of death is not provided
        answers.set(SettlorDateOfDeathYesNoPage, false)
    }
  }

  private def extractDateOfBirth(deceasedSettlor: DisplayTrustWillType, answers: UserAnswers) = {
    deceasedSettlor.dateOfBirth match {
      case Some(dateOfBirth) =>
        answers.set(SettlorDateOfBirthYesNoPage, true)
          .flatMap(_.set(SettlorDateOfBirthPage, dateOfBirth))
      case None =>
        // Assumption that user answered no as the date of birth is not provided
        answers.set(SettlorDateOfBirthYesNoPage, false)
    }
  }

  private def extractIdentification(identification : Option[DisplayTrustIdentificationType], answers: UserAnswers) = {
    identification match {
      case Some(DisplayTrustIdentificationType(_, Some(nino), None, None)) =>
        extractNino(nino, answers)

      case Some(DisplayTrustIdentificationType(_, None, Some(passport), Some(address))) =>
        extractPassportIdCard(passport.convert, answers)
          .flatMap(updated => extractAddress(address.convert, updated))

      case Some(DisplayTrustIdentificationType(_, None, None, Some(address))) =>
        extractAddress(address.convert, answers)

      case Some(DisplayTrustIdentificationType(_, None, Some(passport), None)) =>
        extractPassportIdCard(passport.convert, answers)

      case _ =>
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
