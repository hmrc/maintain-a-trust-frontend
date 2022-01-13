/*
 * Copyright 2022 HM Revenue & Customs
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
import models.http.{DisplayTrustIdentificationType, DisplayTrustWillType, PassportType}
import models.{Address, MetaData, UserAnswers}
import pages.QuestionPage
import pages.settlors.deceased_settlor._

import java.time.LocalDate
import scala.util.Try

class DeceasedSettlorExtractor extends PlaybackExtractor[DisplayTrustWillType] {

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = SettlorLastKnownAddressYesNoPage
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = SettlorLastKnownAddressUKYesNoPage
  override def addressPage(index: Int): QuestionPage[Address] = SettlorLastKnownAddressPage

  override def ninoYesNoPage(index: Int): QuestionPage[Boolean] = SettlorNationalInsuranceYesNoPage
  override def ninoPage(index: Int): QuestionPage[String] = SettlorNationalInsuranceNumberPage

  override def passportOrIdCardPage(index: Int): QuestionPage[PassportType] = SettlorPassportIDCardPage

  override def dateOfBirthYesNoPage(index: Int): QuestionPage[Boolean] = SettlorDateOfBirthYesNoPage
  override def dateOfBirthPage(index: Int): QuestionPage[LocalDate] = SettlorDateOfBirthPage

  override def countryOfNationalityYesNoPage(index: Int): QuestionPage[Boolean] = DeceasedSettlorCountryOfNationalityYesNoPage
  override def ukCountryOfNationalityYesNoPage(index: Int): QuestionPage[Boolean] = DeceasedSettlorCountryOfNationalityInTheUkYesNoPage
  override def countryOfNationalityPage(index: Int): QuestionPage[String] = DeceasedSettlorCountryOfNationalityPage

  override def countryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = DeceasedSettlorCountryOfResidenceYesNoPage
  override def ukCountryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = DeceasedSettlorCountryOfResidenceInTheUkYesNoPage
  override def countryOfResidencePage(index: Int): QuestionPage[String] = DeceasedSettlorCountryOfResidencePage

  override def metaDataPage(index: Int): QuestionPage[MetaData] = DeceasedSettlorMetaData

  override def updateUserAnswers(answers: Try[UserAnswers],
                                 entity: DisplayTrustWillType,
                                 index: Int): Try[UserAnswers] = {
    super.updateUserAnswers(answers, entity, index)
      .flatMap(_.set(SettlorNamePage, entity.name))
      .flatMap(answers => extractDateOfDeath(entity.dateOfDeath, answers))
      .flatMap(answers => extractDateOfBirth(entity.dateOfBirth, index, answers))
      .flatMap(answers => extractCountryOfNationality(entity.nationality, index, answers))
      .flatMap(answers => extractCountryOfResidence(entity.countryOfResidence, index, answers))
      .flatMap(answers => extractIndIdentification(entity.identification, index, answers))
      .flatMap(_.set(DeceasedSettlorSafeIdPage, entity.identification.flatMap(_.safeId)))
  }

  private def extractDateOfDeath(dateOfDeath: Option[LocalDate],
                                 answers: UserAnswers): Try[UserAnswers] = {
    dateOfDeath match {
      case Some(value) =>
        answers.set(SettlorDateOfDeathYesNoPage, true)
          .flatMap(_.set(SettlorDateOfDeathPage, value))
      case None =>
        answers.set(SettlorDateOfDeathYesNoPage, false)
    }
  }

  override def extractIndIdentification(identification: Option[DisplayTrustIdentificationType],
                                        index: Int,
                                        answers: UserAnswers): Try[UserAnswers] = {
    extractIfTaxableOrMigratingToTaxable(answers) {
      identification match {
        case Some(DisplayTrustIdentificationType(_, Some(nino), None, None)) =>
          answers.set(ninoYesNoPage(index), true)
            .flatMap(_.set(ninoPage(index), nino))
        case Some(DisplayTrustIdentificationType(_, None, None, Some(address))) =>
          answers.set(ninoYesNoPage(index), false)
            .flatMap(answers => extractAddress(address, index, answers))
        case Some(DisplayTrustIdentificationType(_, None, Some(passport), Some(address))) =>
          answers.set(ninoYesNoPage(index), false)
            .flatMap(answers => extractAddress(address, index, answers))
            .flatMap(answers => extractPassportIdCard(passport, index, answers))
        case Some(DisplayTrustIdentificationType(_, None, Some(passport), None)) =>
          answers.set(ninoYesNoPage(index), false)
            .flatMap(_.set(addressYesNoPage(index), false))
            .flatMap(answers => extractPassportIdCard(passport, index, answers))
        case _ =>
          answers.set(ninoYesNoPage(index), false)
            .flatMap(_.set(addressYesNoPage(index), false))
      }
    }
  }

  override def extractPassportIdCard(passport: PassportType,
                                     index: Int,
                                     answers: UserAnswers): Try[UserAnswers] = {
    answers.set(SettlorPassportIDCardPage, passport)
  }

}
