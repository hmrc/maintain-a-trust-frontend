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

import models.{FullName, UserAnswers}
import pages.QuestionPage
import pages.beneficiaries.individual._
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, JsPath}
import sections.beneficiaries.IndividualBeneficiaries
import utils.print.sections.{EntitiesPrinter, AnswerRowConverter, EntityPrinter}
import viewmodels.{AnswerRow, AnswerSection}

import javax.inject.Inject

class IndividualBeneficiaryPrinter @Inject()(converter: AnswerRowConverter) extends EntitiesPrinter[JsArray] with EntityPrinter[FullName] {

  override def printSection(index: Int, userAnswers: UserAnswers)
                           (implicit messages: Messages): Option[AnswerSection] = {
    printAnswerRows(index, userAnswers)
  }

  override def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                         (implicit messages: Messages): Seq[Option[AnswerRow]] = Seq(
    converter.fullNameQuestion(IndividualBeneficiaryNamePage(index), userAnswers, "individualBeneficiaryName"),
    converter.roleInCompanyQuestion(IndividualBeneficiaryRoleInCompanyPage(index), userAnswers, "individualBeneficiaryRoleInCompany", name),
    converter.yesNoQuestion(IndividualBeneficiaryDateOfBirthYesNoPage(index), userAnswers, "individualBeneficiaryDateOfBirthYesNo", name),
    converter.dateQuestion(IndividualBeneficiaryDateOfBirthPage(index), userAnswers, "individualBeneficiaryDateOfBirth", name),
    converter.yesNoQuestion(IndividualBeneficiaryIncomeYesNoPage(index), userAnswers, "individualBeneficiaryIncomeYesNo", name),
    converter.percentageQuestion(IndividualBeneficiaryIncomePage(index), userAnswers, "individualBeneficiaryIncome", name),
    converter.yesNoQuestion(IndividualBeneficiaryCountryOfNationalityYesNoPage(index), userAnswers, "individualBeneficiaryCountryOfNationalityYesNo", name),
    converter.yesNoQuestion(IndividualBeneficiaryCountryOfNationalityInTheUkYesNoPage(index), userAnswers, "individualBeneficiaryCountryOfNationalityUkYesNo", name),
    converter.countryQuestion(IndividualBeneficiaryCountryOfNationalityInTheUkYesNoPage(index), IndividualBeneficiaryCountryOfNationalityPage(index), userAnswers, "individualBeneficiaryCountryOfNationality", name),
    converter.yesNoQuestion(IndividualBeneficiaryNationalInsuranceYesNoPage(index), userAnswers, "individualBeneficiaryNationalInsuranceYesNo", name),
    converter.ninoQuestion(IndividualBeneficiaryNationalInsuranceNumberPage(index), userAnswers, "individualBeneficiaryNationalInsuranceNumber", name),
    converter.yesNoQuestion(IndividualBeneficiaryCountryOfResidenceYesNoPage(index), userAnswers, "individualBeneficiaryCountryOfResidenceYesNo", name),
    converter.yesNoQuestion(IndividualBeneficiaryCountryOfResidenceInTheUkYesNoPage(index), userAnswers, "individualBeneficiaryCountryOfResidenceUkYesNo", name),
    converter.countryQuestion(IndividualBeneficiaryCountryOfResidenceInTheUkYesNoPage(index), IndividualBeneficiaryCountryOfResidencePage(index), userAnswers, "individualBeneficiaryCountryOfResidence", name),
    converter.yesNoQuestion(IndividualBeneficiaryAddressYesNoPage(index), userAnswers, "individualBeneficiaryAddressYesNo", name),
    converter.yesNoQuestion(IndividualBeneficiaryAddressUKYesNoPage(index), userAnswers, "individualBeneficiaryAddressUKYesNo", name),
    converter.addressQuestion(IndividualBeneficiaryAddressPage(index), userAnswers, "individualBeneficiaryAddressUK", name),
    converter.yesNoQuestion(IndividualBeneficiaryPassportIDCardYesNoPage(index), userAnswers, "individualBeneficiaryPassportIDCardYesNo", name),
    converter.passportOrIdCardQuestion(IndividualBeneficiaryPassportIDCardPage(index), userAnswers, "individualBeneficiaryPassportIDCard", name),
    converter.yesNoQuestion(IndividualBeneficiaryVulnerableYesNoPage(index), userAnswers, "individualBeneficiaryVulnerableYesNo", name),
    converter.yesNoQuestion(IndividualBeneficiaryMentalCapacityYesNoPage(index), userAnswers, "individualBeneficiaryMentalCapacityYesNo", name)
  )

  override def namePath(index: Int): JsPath = IndividualBeneficiaryNamePage(index).path

  override def section: QuestionPage[JsArray] = IndividualBeneficiaries

  override val headingKey: Option[String] = None

  override val subHeadingKey: Option[String] = Some("individualBeneficiary")
}
