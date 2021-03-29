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

package utils.print.sections.trustees.leadtrustee

import models.{FullName, UserAnswers}
import pages.trustees._
import play.api.i18n.Messages
import play.api.libs.json._
import utils.print.sections.{AnswerRowConverter, Printer}
import viewmodels.AnswerRow

import javax.inject.Inject

class LeadTrusteeIndividualPrinter @Inject()(converter: AnswerRowConverter) extends LeadTrustee(converter) with Printer[FullName] {

  override def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                         (implicit messages: Messages): Seq[Option[AnswerRow]] = {
    Seq(
      converter.fullNameQuestion(TrusteeNamePage(index), userAnswers, "leadTrusteeName"),
      converter.dateQuestion(TrusteeDateOfBirthPage(index), userAnswers, "trusteeDateOfBirth", name),
      converter.yesNoQuestion(TrusteeAUKCitizenPage(index), userAnswers, "trusteeAUKCitizen", name),
      converter.ninoQuestion(TrusteeNinoPage(index), userAnswers, "trusteeNino", name),
      converter.yesNoQuestion(TrusteePassportIDCardYesNoPage(index), userAnswers, "trusteePassportOrIdCardYesNo", name),
      converter.passportOrIdCardQuestion(TrusteePassportIDCardPage(index), userAnswers, "trusteePassportOrIdCard", name)
    ) ++
      addressAnswers(index, userAnswers, name) ++
      Seq(
        converter.yesNoQuestion(TrusteeEmailYesNoPage(index), userAnswers, "trusteeEmailAddressYesNo", name),
        converter.stringQuestion(TrusteeEmailPage(index), userAnswers, "trusteeEmailAddress", name),
        converter.stringQuestion(TrusteeTelephoneNumberPage(index), userAnswers, "trusteeTelephoneNumber", name)
      )
  }

  override def namePath(index: Int): JsPath = TrusteeNamePage(index).path

  override val subHeadingKey: Option[String] = Some("leadTrustee")

}
