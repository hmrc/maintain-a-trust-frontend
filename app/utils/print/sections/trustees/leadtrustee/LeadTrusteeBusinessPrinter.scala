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

import models.UserAnswers
import pages.trustees._
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.print.sections.{AnswerRowConverter, EntityPrinter}
import viewmodels.AnswerRow

import javax.inject.Inject

class LeadTrusteeBusinessPrinter @Inject()(converter: AnswerRowConverter) extends LeadTrustee(converter) with EntityPrinter[String] {

  override def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                         (implicit messages: Messages): Seq[Option[AnswerRow]] = {
    Seq(
      converter.yesNoQuestion(TrusteeUtrYesNoPage(index), userAnswers, "leadTrusteeUtrYesNo", name),
      converter.stringQuestion(TrusteeOrgNamePage(index), userAnswers, "trusteeBusinessName"),
      converter.stringQuestion(TrusteeUtrPage(index), userAnswers, "trusteeUtr", name),
      converter.yesNoQuestion(TrusteeCountryOfResidenceYesNoPage(index), userAnswers, "trusteeCountryOfResidenceYesNo", name),
      converter.yesNoQuestion(TrusteeCountryOfResidenceInTheUkYesNoPage(index), userAnswers, "trusteeCountryOfResidenceUkYesNo", name),
      converter.countryQuestion(TrusteeCountryOfResidenceInTheUkYesNoPage(index), TrusteeCountryOfResidencePage(index), userAnswers, "trusteeCountryOfResidence", name)
    ) ++
      addressAnswers(index, userAnswers, name) ++
      Seq(
        converter.yesNoQuestion(TrusteeEmailYesNoPage(index), userAnswers, "trusteeEmailAddressYesNo", name),
        converter.stringQuestion(TrusteeEmailPage(index), userAnswers, "trusteeEmailAddress", name),
        converter.stringQuestion(TrusteeTelephoneNumberPage(index), userAnswers, "trusteeTelephoneNumber", name)
      )
  }

  override def namePath(index: Int): JsPath = TrusteeOrgNamePage(index).path

  override val subHeadingKey: Option[String] = Some("leadTrustee")

}
