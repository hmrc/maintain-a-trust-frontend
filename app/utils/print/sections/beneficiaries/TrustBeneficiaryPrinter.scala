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
import pages.beneficiaries.trust._
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, JsPath}
import sections.beneficiaries.TrustBeneficiaries
import utils.print.sections.{AnswerRowConverter, Printer}
import viewmodels.AnswerRow

import javax.inject.Inject

class TrustBeneficiaryPrinter @Inject()(converter: AnswerRowConverter) extends Printer[String] {

  override def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                         (implicit messages: Messages): Seq[Option[AnswerRow]] = Seq(
    converter.stringQuestion(TrustBeneficiaryNamePage(index), userAnswers, "trustBeneficiaryName"),
    converter.yesNoQuestion(TrustBeneficiaryDiscretionYesNoPage(index), userAnswers, "trustBeneficiaryShareOfIncomeYesNo", name),
    converter.percentageQuestion(TrustBeneficiaryShareOfIncomePage(index), userAnswers, "trustBeneficiaryShareOfIncome", name),
    converter.yesNoQuestion(TrustBeneficiaryAddressYesNoPage(index), userAnswers, "trustBeneficiaryAddressYesNo", name),
    converter.yesNoQuestion(TrustBeneficiaryAddressUKYesNoPage(index), userAnswers, "trustBeneficiaryAddressUKYesNo", name),
    converter.addressQuestion(TrustBeneficiaryAddressPage(index), userAnswers, "trustBeneficiaryAddress", name),
    converter.stringQuestion(TrustBeneficiaryUtrPage(index), userAnswers, "trustBeneficiaryUtr", name)
  )

  override def namePath(index: Int): JsPath = TrustBeneficiaryNamePage(index).path

  override val section: QuestionPage[JsArray] = TrustBeneficiaries

  override val sectionKey: String = "trustBeneficiary"

}
