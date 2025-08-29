/*
 * Copyright 2025 HM Revenue & Customs
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
import pages.beneficiaries.company._
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, JsPath}
import sections.beneficiaries.CompanyBeneficiaries
import utils.print.sections.{EntitiesPrinter, AnswerRowConverter, EntityPrinter}
import viewmodels.{AnswerRow, AnswerSection}

import javax.inject.Inject

class CompanyBeneficiaryPrinter @Inject()(converter: AnswerRowConverter) extends EntitiesPrinter[JsArray] with EntityPrinter[String] {

  override def printSection(index: Int, userAnswers: UserAnswers)
                           (implicit messages: Messages): Option[AnswerSection] = {
    printAnswerRows(index, userAnswers)
  }

  override def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                         (implicit messages: Messages): Seq[Option[AnswerRow]] = Seq(
    converter.stringQuestion(CompanyBeneficiaryNamePage(index), userAnswers, "companyBeneficiaryName"),
    converter.yesNoQuestion(CompanyBeneficiaryDiscretionYesNoPage(index), userAnswers, "companyBeneficiaryShareOfIncomeYesNo", name),
    converter.percentageQuestion(CompanyBeneficiaryShareOfIncomePage(index), userAnswers, "companyBeneficiaryShareOfIncome", name),
    converter.yesNoQuestion(CompanyBeneficiaryCountryOfResidenceYesNoPage(index), userAnswers, "companyBeneficiaryCountryOfResidenceYesNo", name),
    converter.yesNoQuestion(CompanyBeneficiaryCountryOfResidenceInTheUkYesNoPage(index), userAnswers, "companyBeneficiaryCountryOfResidenceUkYesNo", name),
    converter.countryQuestion(
      CompanyBeneficiaryCountryOfResidenceInTheUkYesNoPage(index),
      CompanyBeneficiaryCountryOfResidencePage(index),
      userAnswers,
      "companyBeneficiaryCountryOfResidence",
      name
    ),
    converter.yesNoQuestion(CompanyBeneficiaryAddressYesNoPage(index), userAnswers, "companyBeneficiaryAddressYesNo", name),
    converter.yesNoQuestion(CompanyBeneficiaryAddressUKYesNoPage(index), userAnswers, "companyBeneficiaryAddressUKYesNo", name),
    converter.addressQuestion(CompanyBeneficiaryAddressPage(index), userAnswers, "companyBeneficiaryAddress", name),
    converter.stringQuestion(CompanyBeneficiaryUtrPage(index), userAnswers, "companyBeneficiaryUtr", name)
  )

  override def namePath(index: Int): JsPath = CompanyBeneficiaryNamePage(index).path

  override def section: QuestionPage[JsArray] = CompanyBeneficiaries

  override def headingKey(migratingFromNonTaxableToTaxable: Boolean): Option[String] = None

  override val subHeadingKey: Option[String] = Some("companyBeneficiary")
}
