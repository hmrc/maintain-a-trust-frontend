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
import utils.print.sections.{AnswerRowConverter, Printer}
import viewmodels.AnswerRow

import javax.inject.Inject

class DeceasedSettlorPrinter @Inject()(converter: AnswerRowConverter) extends Printer[FullName, JsValue] {

  override def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                         (implicit messages: Messages): Seq[Option[AnswerRow]] = Seq(
    converter.fullNameQuestion(SettlorNamePage, userAnswers, "settlorName"),
    converter.yesNoQuestion(SettlorDateOfDeathYesNoPage, userAnswers, "settlorDateOfDeathYesNo", name),
    converter.dateQuestion(SettlorDateOfDeathPage, userAnswers, "settlorDateOfDeath", name),
    converter.yesNoQuestion(SettlorDateOfBirthYesNoPage, userAnswers, "settlorDateOfBirthYesNo", name),
    converter.dateQuestion(SettlorDateOfBirthPage, userAnswers, "settlorDateOfBirth", name),
    converter.yesNoQuestion(SettlorNationalInsuranceYesNoPage, userAnswers, "settlorNationalInsuranceYesNo", name),
    converter.ninoQuestion(SettlorNationalInsuranceNumberPage, userAnswers, "settlorNationalInsuranceNumber", name),
    converter.yesNoQuestion(SettlorLastKnownAddressYesNoPage, userAnswers, "settlorLastKnownAddressYesNo", name),
    converter.yesNoQuestion(SettlorLastKnownAddressUKYesNoPage, userAnswers, "settlorLastKnownAddressUKYesNo", name),
    converter.addressQuestion(SettlorLastKnownAddressPage, userAnswers, "settlorUKAddress", name),
    converter.passportOrIdCardQuestion(SettlorPassportIDCardPage, userAnswers, "settlorPassportOrIdCard", name)
  )

  override def namePath(index: Int): JsPath = SettlorNamePage.path

  override val section: QuestionPage[JsValue] = DeceasedSettlor

  override val subHeadingKey: Option[String] = None

}
