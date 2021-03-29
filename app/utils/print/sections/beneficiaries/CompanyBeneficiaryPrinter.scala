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

import javax.inject.Inject
import models.UserAnswers
import pages.beneficiaries.company._
import play.api.i18n.Messages
import utils.print.sections.AnswerRowConverter
import viewmodels.AnswerSection

class CompanyBeneficiaryPrinter @Inject()(converter: AnswerRowConverter) {

  def print(index: Int, userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] =

    userAnswers.get(CompanyBeneficiaryNamePage(index)).map { name =>
      Seq(
        AnswerSection(
          headingKey = Some(messages("answerPage.section.companyBeneficiary.subheading", index + 1)),
          Seq(
            converter.stringQuestion(CompanyBeneficiaryNamePage(index), userAnswers, "companyBeneficiaryName"),
            converter.yesNoQuestion(CompanyBeneficiaryDiscretionYesNoPage(index), userAnswers, "companyBeneficiaryShareOfIncomeYesNo", name),
            converter.percentageQuestion(CompanyBeneficiaryShareOfIncomePage(index), userAnswers, "companyBeneficiaryShareOfIncome", name),
            converter.yesNoQuestion(CompanyBeneficiaryAddressYesNoPage(index), userAnswers, "companyBeneficiaryAddressYesNo", name),
            converter.yesNoQuestion(CompanyBeneficiaryAddressUKYesNoPage(index), userAnswers, "companyBeneficiaryAddressUKYesNo", name),
            converter.addressQuestion(CompanyBeneficiaryAddressPage(index), userAnswers, "companyBeneficiaryAddress", name),
            converter.stringQuestion(CompanyBeneficiaryUtrPage(index), userAnswers, "companyBeneficiaryUtr", name)
          ).flatten
        )
      )
    }.getOrElse(Nil)

}
