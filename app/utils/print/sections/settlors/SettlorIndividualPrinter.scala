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
import pages.settlors.living_settlor._
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, JsPath}
import sections.settlors.LivingSettlors
import utils.print.sections.{AnswerRowConverter, Printer}
import viewmodels.AnswerRow

import javax.inject.Inject

class SettlorIndividualPrinter @Inject()(converter: AnswerRowConverter) extends Printer[FullName, JsArray] {

  override def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                         (implicit messages: Messages): Seq[Option[AnswerRow]] = Seq(
    converter.fullNameQuestion(SettlorIndividualNamePage(index), userAnswers, "settlorIndividualName"),
    converter.yesNoQuestion(SettlorIndividualDateOfBirthYesNoPage(index), userAnswers, "settlorIndividualDateOfBirthYesNo", name),
    converter.dateQuestion(SettlorIndividualDateOfBirthPage(index), userAnswers, "settlorIndividualDateOfBirth", name),
    converter.yesNoQuestion(SettlorIndividualNINOYesNoPage(index), userAnswers, "settlorIndividualNINOYesNo", name),
    converter.ninoQuestion(SettlorIndividualNINOPage(index), userAnswers, "settlorIndividualNINO", name),
    converter.yesNoQuestion(SettlorAddressYesNoPage(index), userAnswers, "settlorIndividualAddressYesNo", name),
    converter.yesNoQuestion(SettlorAddressUKYesNoPage(index), userAnswers, "settlorIndividualAddressUKYesNo", name),
    converter.addressQuestion(SettlorAddressPage(index), userAnswers, "settlorIndividualAddressUK", name),
    converter.yesNoQuestion(SettlorIndividualPassportIDCardYesNoPage(index), userAnswers, "settlorPassportOrIdCardYesNo", name),
    converter.passportOrIdCardQuestion(SettlorIndividualPassportIDCardPage(index), userAnswers, "settlorPassportOrIdCard", name)
  )

  override def namePath(index: Int): JsPath = SettlorIndividualNamePage(index).path

  override val section: QuestionPage[JsArray] = LivingSettlors

  override val subHeadingKey: Option[String] = Some("settlor")

}
