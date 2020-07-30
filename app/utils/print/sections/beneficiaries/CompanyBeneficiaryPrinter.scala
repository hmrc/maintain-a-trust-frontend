/*
 * Copyright 2020 HM Revenue & Customs
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
import pages.beneficiaries.company._
import play.api.i18n.Messages
import utils.countryoptions.CountryOptions
import viewmodels.AnswerSection
import utils.print.sections.AnswerRowConverter._

object CompanyBeneficiaryPrinter {

  def print(index: Int, userAnswers: UserAnswers, countryOptions: CountryOptions)
           (implicit messages: Messages): Seq[AnswerSection] =

    userAnswers.get(CompanyBeneficiaryNamePage(index)).map { name =>
      Seq(
        AnswerSection(
          headingKey = Some(messages("answerPage.section.companyBeneficiary.subheading", index + 1)),
          Seq(
            stringQuestion(CompanyBeneficiaryNamePage(index), userAnswers, "companyBeneficiaryName"),
            yesNoQuestion(CompanyBeneficiaryDiscretionYesNoPage(index), userAnswers, "companyBeneficiaryShareOfIncomeYesNo", name),
            percentageQuestion(CompanyBeneficiaryShareOfIncomePage(index), userAnswers, "companyBeneficiaryShareOfIncome", name),
            yesNoQuestion(CompanyBeneficiaryAddressYesNoPage(index), userAnswers, "companyBeneficiaryAddressYesNo", name),
            yesNoQuestion(CompanyBeneficiaryAddressUKYesNoPage(index), userAnswers, "companyBeneficiaryAddressUKYesNo", name),
            addressQuestion(CompanyBeneficiaryAddressPage(index), userAnswers, "companyBeneficiaryAddress", countryOptions = countryOptions, messageArg = name),
            utrQuestion(CompanyBeneficiaryUtrPage(index), userAnswers, "companyBeneficiaryUtr", name)
          ).flatten
        )
      )
    }.getOrElse(Nil)

}
