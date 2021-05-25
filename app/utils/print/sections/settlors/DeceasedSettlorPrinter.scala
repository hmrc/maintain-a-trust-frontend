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

package utils.print.sections.settlors

import models.{FullName, UserAnswers}
import pages.QuestionPage
import pages.settlors.deceased_settlor._
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsValue}
import sections.settlors.DeceasedSettlor
import utils.print.sections.{EntitiesPrinter, AnswerRowConverter, EntityPrinter}
import viewmodels.{AnswerRow, AnswerSection}

import javax.inject.Inject

class DeceasedSettlorPrinter @Inject()(converter: AnswerRowConverter) extends EntitiesPrinter[JsValue] with EntityPrinter[FullName] {

  override def printSection(index: Int, userAnswers: UserAnswers)
                           (implicit messages: Messages): Option[AnswerSection] = {
    printAnswerRows(index, userAnswers)
  }

  override def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                         (implicit messages: Messages): Seq[Option[AnswerRow]] = {

    /**
     * This print helper behaves slightly differently since we don't ask the nationality or residency
     * questions when maintaining a deceased settlor.
     *
     * Scenarios:
     * - 4MLD-registered:
     *    - nationality/residency never asked => don't display answer rows
     * - 5MLD-registered:
     *    - at least one of nationality/residency known => display answer rows
     *    - nationality/residency unknown => don't display answer rows ***
     *
     *  *** Ideally we would display the answer rows here (with both "do you know" questions answered with "No"), but
     *      we have no way of differentiating between this and a 4MLD-registered deceased settlor as the datasets look
     *      the same. So in order to avoid displaying answers to questions that a user has never been given the
     *      opportunity to answer, this is the approach that was agreed upon.
     */
    val nationalityAndResidencyUnknown: Boolean = (
      userAnswers.get(DeceasedSettlorCountryOfNationalityYesNoPage).contains(true),
      userAnswers.get(DeceasedSettlorCountryOfResidenceYesNoPage).contains(true)
    ) match {
      case (false, false) => true
      case _ => false
    }

    def displayRow(row: => Option[AnswerRow]): Option[AnswerRow] = {
      if (nationalityAndResidencyUnknown) None else row
    }

    Seq(
      converter.fullNameQuestion(SettlorNamePage, userAnswers, "settlorName"),
      converter.yesNoQuestion(SettlorDateOfDeathYesNoPage, userAnswers, "settlorDateOfDeathYesNo", name),
      converter.dateQuestion(SettlorDateOfDeathPage, userAnswers, "settlorDateOfDeath", name),
      converter.yesNoQuestion(SettlorDateOfBirthYesNoPage, userAnswers, "settlorDateOfBirthYesNo", name),
      converter.dateQuestion(SettlorDateOfBirthPage, userAnswers, "settlorDateOfBirth", name),
      displayRow(converter.yesNoQuestion(DeceasedSettlorCountryOfNationalityYesNoPage, userAnswers, "settlorCountryOfNationalityYesNo", name)),
      displayRow(converter.yesNoQuestion(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage, userAnswers, "settlorCountryOfNationalityUkYesNo", name)),
      displayRow(converter.countryQuestion(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage, DeceasedSettlorCountryOfNationalityPage, userAnswers, "settlorCountryOfNationality", name)),
      converter.yesNoQuestion(SettlorNationalInsuranceYesNoPage, userAnswers, "settlorNationalInsuranceYesNo", name),
      converter.ninoQuestion(SettlorNationalInsuranceNumberPage, userAnswers, "settlorNationalInsuranceNumber", name),
      displayRow(converter.yesNoQuestion(DeceasedSettlorCountryOfResidenceYesNoPage, userAnswers, "settlorCountryOfResidenceYesNo", name)),
      displayRow(converter.yesNoQuestion(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage, userAnswers, "settlorCountryOfResidenceUkYesNo", name)),
      displayRow(converter.countryQuestion(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage, DeceasedSettlorCountryOfResidencePage, userAnswers, "settlorCountryOfResidence", name)),
      converter.yesNoQuestion(SettlorLastKnownAddressYesNoPage, userAnswers, "settlorLastKnownAddressYesNo", name),
      converter.yesNoQuestion(SettlorLastKnownAddressUKYesNoPage, userAnswers, "settlorLastKnownAddressUKYesNo", name),
      converter.addressQuestion(SettlorLastKnownAddressPage, userAnswers, "settlorUKAddress", name),
      converter.passportOrIdCardQuestion(SettlorPassportIDCardPage, userAnswers, "settlorPassportOrIdCard", name)
    )
  }

  override def namePath(index: Int): JsPath = SettlorNamePage.path

  override val section: QuestionPage[JsValue] = DeceasedSettlor

  override def headingKey(migratingFromNonTaxableToTaxable: Boolean): Option[String] = Some("deceasedSettlor")

  override val subHeadingKey: Option[String] = None

}
