/*
 * Copyright 2023 HM Revenue & Customs
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
import pages.settlors.living_settlor._
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.print.sections.{AnswerRowConverter, EntityPrinter}
import viewmodels.AnswerRow

import javax.inject.Inject

class SettlorIndividualPrinter @Inject()(converter: AnswerRowConverter) extends EntityPrinter[FullName] {

  override def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                         (implicit messages: Messages): Seq[Option[AnswerRow]] = Seq(
    converter.fullNameQuestion(SettlorIndividualNamePage(index), userAnswers, "settlorIndividualName"),
    converter.yesNoQuestion(SettlorIndividualDateOfBirthYesNoPage(index), userAnswers, "settlorIndividualDateOfBirthYesNo", name),
    converter.dateQuestion(SettlorIndividualDateOfBirthPage(index), userAnswers, "settlorIndividualDateOfBirth", name),
    converter.yesNoQuestion(SettlorCountryOfNationalityYesNoPage(index), userAnswers, "settlorIndividualCountryOfNationalityYesNo", name),
    converter.yesNoQuestion(SettlorCountryOfNationalityInTheUkYesNoPage(index), userAnswers, "settlorIndividualCountryOfNationalityUkYesNo", name),
    converter.countryQuestion(SettlorCountryOfNationalityInTheUkYesNoPage(index), SettlorCountryOfNationalityPage(index), userAnswers, "settlorIndividualCountryOfNationality", name),
    converter.yesNoQuestion(SettlorIndividualNINOYesNoPage(index), userAnswers, "settlorIndividualNINOYesNo", name),
    converter.ninoQuestion(SettlorIndividualNINOPage(index), userAnswers, "settlorIndividualNINO", name),
    converter.yesNoQuestion(SettlorCountryOfResidenceYesNoPage(index), userAnswers, "settlorIndividualCountryOfResidenceYesNo", name),
    converter.yesNoQuestion(SettlorCountryOfResidenceInTheUkYesNoPage(index), userAnswers, "settlorIndividualCountryOfResidenceUkYesNo", name),
    converter.countryQuestion(SettlorCountryOfResidenceInTheUkYesNoPage(index), SettlorCountryOfResidencePage(index), userAnswers, "settlorIndividualCountryOfResidence", name),
    converter.yesNoQuestion(SettlorAddressYesNoPage(index), userAnswers, "settlorIndividualAddressYesNo", name),
    converter.yesNoQuestion(SettlorAddressUKYesNoPage(index), userAnswers, "settlorIndividualAddressUKYesNo", name),
    converter.addressQuestion(SettlorAddressPage(index), userAnswers, "settlorIndividualAddressUK", name),
    converter.yesNoQuestion(SettlorIndividualPassportIDCardYesNoPage(index), userAnswers, "settlorPassportOrIdCardYesNo", name),
    converter.passportOrIdCardQuestion(SettlorIndividualPassportIDCardPage(index), userAnswers, "settlorPassportOrIdCard", name),
    converter.yesNoDontKnowQuestion(SettlorIndividualMentalCapacityYesNoPage(index), userAnswers, "settlorIndividualMentalCapacityYesNo", name)
  )

  override def namePath(index: Int): JsPath = SettlorIndividualNamePage(index).path

  override val subHeadingKey: Option[String] = Some("settlor")

}
