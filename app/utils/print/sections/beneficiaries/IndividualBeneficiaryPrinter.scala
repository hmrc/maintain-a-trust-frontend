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
import pages.beneficiaries.individual._
import play.api.i18n.Messages
import utils.print.sections.AnswerRowConverter
import viewmodels.AnswerSection

import javax.inject.Inject

class IndividualBeneficiaryPrinter @Inject()(converter: AnswerRowConverter) {

  def print(index: Int, userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] = {

    userAnswers.get(IndividualBeneficiaryNamePage(index)).map(_.toString).map { name =>
      Seq(
        AnswerSection(
          headingKey = Some(messages("answerPage.section.individualBeneficiary.subheading", index + 1)),
          Seq(
            converter.fullNameQuestion(IndividualBeneficiaryNamePage(index), userAnswers, "individualBeneficiaryName"),
            converter.roleInCompanyQuestion(IndividualBeneficiaryRoleInCompanyPage(index), userAnswers, "individualBeneficiaryRoleInCompany", name),
            converter.yesNoQuestion(IndividualBeneficiaryDateOfBirthYesNoPage(index), userAnswers, "individualBeneficiaryDateOfBirthYesNo", name),
            converter.dateQuestion(IndividualBeneficiaryDateOfBirthPage(index), userAnswers, "individualBeneficiaryDateOfBirth", name),
            converter.yesNoQuestion(IndividualBeneficiaryIncomeYesNoPage(index), userAnswers, "individualBeneficiaryIncomeYesNo", name),
            converter.percentageQuestion(IndividualBeneficiaryIncomePage(index), userAnswers, "individualBeneficiaryIncome", name ),
            converter.yesNoQuestion(IndividualBeneficiaryNationalInsuranceYesNoPage(index), userAnswers, "individualBeneficiaryNationalInsuranceYesNo", name),
            converter.ninoQuestion(IndividualBeneficiaryNationalInsuranceNumberPage(index), userAnswers, "individualBeneficiaryNationalInsuranceNumber", name),
            converter.yesNoQuestion(IndividualBeneficiaryAddressYesNoPage(index), userAnswers, "individualBeneficiaryAddressYesNo", name),
            converter.yesNoQuestion(IndividualBeneficiaryAddressUKYesNoPage(index), userAnswers, "individualBeneficiaryAddressUKYesNo", name),
            converter.addressQuestion(IndividualBeneficiaryAddressPage(index), userAnswers, "individualBeneficiaryAddressUK", name),
            converter.yesNoQuestion(IndividualBeneficiaryPassportIDCardYesNoPage(index), userAnswers, "individualBeneficiaryPassportIDCardYesNo", name),
            converter.passportOrIdCardQuestion(IndividualBeneficiaryPassportIDCardPage(index), userAnswers, "individualBeneficiaryPassportIDCard", name),
            converter.yesNoQuestion(IndividualBeneficiaryVulnerableYesNoPage(index), userAnswers, "individualBeneficiaryVulnerableYesNo", name)
          ).flatten,
          sectionKey = None
        )
      )
    }.getOrElse(Nil)
  }
}
