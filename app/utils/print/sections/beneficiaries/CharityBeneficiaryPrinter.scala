/*
 * Copyright 2022 HM Revenue & Customs
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
import pages.beneficiaries.charity._
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, JsPath}
import sections.beneficiaries.CharityBeneficiaries
import utils.print.sections.{EntitiesPrinter, AnswerRowConverter, EntityPrinter}
import viewmodels.{AnswerRow, AnswerSection}

import javax.inject.Inject

class CharityBeneficiaryPrinter @Inject()(converter: AnswerRowConverter) extends EntitiesPrinter[JsArray] with EntityPrinter[String] {

  override def printSection(index: Int, userAnswers: UserAnswers)
                           (implicit messages: Messages): Option[AnswerSection] = {
    printAnswerRows(index, userAnswers)
  }

  override def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                         (implicit messages: Messages): Seq[Option[AnswerRow]] = Seq(
    converter.stringQuestion(CharityBeneficiaryNamePage(index), userAnswers, "charityBeneficiaryName"),
    converter.yesNoQuestion(CharityBeneficiaryDiscretionYesNoPage(index), userAnswers, "charityBeneficiaryShareOfIncomeYesNo", name),
    converter.percentageQuestion(CharityBeneficiaryShareOfIncomePage(index), userAnswers, "charityBeneficiaryShareOfIncome", name),
    converter.yesNoQuestion(CharityBeneficiaryCountryOfResidenceYesNoPage(index), userAnswers, "charityBeneficiaryCountryOfResidenceYesNo", name),
    converter.yesNoQuestion(CharityBeneficiaryCountryOfResidenceInTheUkYesNoPage(index), userAnswers, "charityBeneficiaryCountryOfResidenceUkYesNo", name),
    converter.countryQuestion(
      CharityBeneficiaryCountryOfResidenceInTheUkYesNoPage(index),
      CharityBeneficiaryCountryOfResidencePage(index),
      userAnswers,
      "charityBeneficiaryCountryOfResidence",
      name
    ),
    converter.yesNoQuestion(CharityBeneficiaryAddressYesNoPage(index), userAnswers, "charityBeneficiaryAddressYesNo", name),
    converter.yesNoQuestion(CharityBeneficiaryAddressUKYesNoPage(index), userAnswers, "charityBeneficiaryAddressUKYesNo", name),
    converter.addressQuestion(CharityBeneficiaryAddressPage(index), userAnswers, "charityBeneficiaryAddress", name),
    converter.stringQuestion(CharityBeneficiaryUtrPage(index), userAnswers, "charityBeneficiaryUtr", name)
  )

  override def namePath(index: Int): JsPath = CharityBeneficiaryNamePage(index).path

  override def section: QuestionPage[JsArray] = CharityBeneficiaries

  override def headingKey(migratingFromNonTaxableToTaxable: Boolean): Option[String] = None

  override val subHeadingKey: Option[String] = Some("charityBeneficiary")

}
