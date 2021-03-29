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

package utils.print.sections.beneficiaries

import models.UserAnswers
import pages.QuestionPage
import pages.beneficiaries.other._
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, JsPath}
import sections.beneficiaries.OtherBeneficiaries
import utils.print.sections.{AnswerRowConverter, Printer}
import viewmodels.AnswerRow

import javax.inject.Inject

class OtherBeneficiaryPrinter @Inject()(converter: AnswerRowConverter) extends Printer[String] {

  override def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                         (implicit messages: Messages): Seq[Option[AnswerRow]] = Seq(
    converter.stringQuestion(OtherBeneficiaryDescriptionPage(index), userAnswers, "otherBeneficiaryDescription"),
    converter.yesNoQuestion(OtherBeneficiaryDiscretionYesNoPage(index), userAnswers, "otherBeneficiaryShareOfIncomeYesNo", name),
    converter.percentageQuestion(OtherBeneficiaryShareOfIncomePage(index), userAnswers, "otherBeneficiaryShareOfIncome", name),
    converter.yesNoQuestion(OtherBeneficiaryAddressYesNoPage(index), userAnswers, "otherBeneficiaryAddressYesNo", name),
    converter.yesNoQuestion(OtherBeneficiaryAddressUKYesNoPage(index), userAnswers, "otherBeneficiaryAddressUKYesNo", name),
    converter.addressQuestion(OtherBeneficiaryAddressPage(index), userAnswers, "otherBeneficiaryAddress", name)
  )

  override def namePath(index: Int): JsPath = OtherBeneficiaryDescriptionPage(index).path

  override val section: QuestionPage[JsArray] = OtherBeneficiaries

  override val sectionKey: String = "otherBeneficiary"

}
