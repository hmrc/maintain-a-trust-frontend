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

package utils.print.sections.otherindividuals

import models.{FullName, UserAnswers}
import pages.QuestionPage
import pages.individual._
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, JsPath}
import sections.Natural
import utils.print.sections.{AnswerRowConverter, EntitiesPrinter, EntityPrinter}
import viewmodels.{AnswerRow, AnswerSection}

import javax.inject.Inject

class OtherIndividualsPrinter @Inject()(converter: AnswerRowConverter) extends EntitiesPrinter[JsArray] with EntityPrinter[FullName] {

  override def printSection(index: Int, userAnswers: UserAnswers)
                           (implicit messages: Messages): Option[AnswerSection] = {
    printAnswerRows(index, userAnswers)
  }

  override def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                         (implicit messages: Messages): Seq[Option[AnswerRow]] = Seq(
    converter.fullNameQuestion(OtherIndividualNamePage(index), userAnswers, "otherIndividualName"),
    converter.yesNoQuestion(OtherIndividualDateOfBirthYesNoPage(index), userAnswers, "otherIndividualDateOfBirthYesNo", name),
    converter.dateQuestion(OtherIndividualDateOfBirthPage(index), userAnswers, "otherIndividualDateOfBirth", name),
    converter.yesNoQuestion(OtherIndividualCountryOfNationalityYesNoPage(index), userAnswers, "otherIndividualCountryOfNationalityYesNo", name),
    converter.yesNoQuestion(OtherIndividualCountryOfNationalityInTheUkYesNoPage(index), userAnswers, "otherIndividualCountryOfNationalityUkYesNo", name),
    converter.countryQuestion(OtherIndividualCountryOfNationalityInTheUkYesNoPage(index), OtherIndividualCountryOfNationalityPage(index), userAnswers, "otherIndividualCountryOfNationality", name),
    converter.yesNoQuestion(OtherIndividualNationalInsuranceYesNoPage(index), userAnswers, "otherIndividualNINOYesNo", name),
    converter.ninoQuestion(OtherIndividualNationalInsuranceNumberPage(index), userAnswers, "otherIndividualNINO", name),
    converter.yesNoQuestion(OtherIndividualCountryOfResidenceYesNoPage(index), userAnswers, "otherIndividualCountryOfResidenceYesNo", name),
    converter.yesNoQuestion(OtherIndividualCountryOfResidenceInTheUkYesNoPage(index), userAnswers, "otherIndividualCountryOfResidenceUkYesNo", name),
    converter.countryQuestion(OtherIndividualCountryOfResidenceInTheUkYesNoPage(index), OtherIndividualCountryOfResidencePage(index), userAnswers, "otherIndividualCountryOfResidence", name),
    converter.yesNoQuestion(OtherIndividualAddressYesNoPage(index), userAnswers, "otherIndividualAddressYesNo", name),
    converter.yesNoQuestion(OtherIndividualAddressUKYesNoPage(index), userAnswers, "otherIndividualAddressUKYesNo", name),
    converter.addressQuestion(OtherIndividualAddressPage(index), userAnswers, "otherIndividualAddress", name),
    converter.yesNoQuestion(OtherIndividualPassportIDCardYesNoPage(index), userAnswers, "otherIndividualPassportIDCardYesNo", name),
    converter.passportOrIdCardQuestion(OtherIndividualPassportIDCardPage(index), userAnswers, "otherIndividualPassportIDCard", name),
    converter.yesNoQuestion(OtherIndividualMentalCapacityYesNoPage(index), userAnswers, "otherIndividualMentalCapacityYesNo", name)
  )

  override def namePath(index: Int): JsPath = OtherIndividualNamePage(index).path

  override def section: QuestionPage[JsArray] = Natural

  override def headingKey(isTaxable: Boolean): Option[String] = Some("otherIndividuals")

  override val subHeadingKey: Option[String] = Some("otherIndividual")
}
