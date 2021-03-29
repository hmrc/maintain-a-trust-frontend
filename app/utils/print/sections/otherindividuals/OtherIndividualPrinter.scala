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
import utils.print.sections.{AnswerRowConverter, Printer}
import viewmodels.AnswerRow

import javax.inject.Inject

class OtherIndividualPrinter @Inject()(converter: AnswerRowConverter) extends Printer[FullName, JsArray] {

  override def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                         (implicit messages: Messages): Seq[Option[AnswerRow]] = Seq(
    converter.fullNameQuestion(OtherIndividualNamePage(index), userAnswers, "otherIndividualName"),
    converter.yesNoQuestion(OtherIndividualDateOfBirthYesNoPage(index), userAnswers, "otherIndividualDateOfBirthYesNo", name),
    converter.dateQuestion(OtherIndividualDateOfBirthPage(index), userAnswers, "otherIndividualDateOfBirth", name),
    converter.yesNoQuestion(OtherIndividualNationalInsuranceYesNoPage(index), userAnswers, "otherIndividualNINOYesNo", name),
    converter.ninoQuestion(OtherIndividualNationalInsuranceNumberPage(index), userAnswers, "otherIndividualNINO", name),
    converter.yesNoQuestion(OtherIndividualAddressYesNoPage(index), userAnswers, "otherIndividualAddressYesNo", name),
    converter.yesNoQuestion(OtherIndividualAddressUKYesNoPage(index), userAnswers, "otherIndividualAddressUKYesNo", name),
    converter.addressQuestion(OtherIndividualAddressPage(index), userAnswers, "otherIndividualAddress", name),
    converter.yesNoQuestion(OtherIndividualPassportIDCardYesNoPage(index), userAnswers, "otherIndividualPassportIDCardYesNo", name),
    converter.passportOrIdCardQuestion(OtherIndividualPassportIDCardPage(index), userAnswers, "otherIndividualPassportIDCard", name)
  )

  override def namePath(index: Int): JsPath = OtherIndividualNamePage(index).path

  override val section: QuestionPage[JsArray] = Natural

  override val subHeadingKey: Option[String] = Some("otherIndividual")

}
