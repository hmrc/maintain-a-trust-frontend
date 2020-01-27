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
import pages.beneficiaries.charity._
import play.api.i18n.Messages
import utils.CountryOptions
import utils.print.sections.AnswerRowConverter._
import viewmodels.AnswerSection

object CharityBeneficiary {

  def apply(index: Int,
            userAnswers: UserAnswers,
            countryOptions: CountryOptions)
           (implicit messages: Messages): Seq[AnswerSection] = {

    userAnswers.get(CharityBeneficiaryNamePage(index)).map { name =>
      Seq(
        AnswerSection(
          headingKey = Some(messages("answerPage.section.charityBeneficiary.subheading", index + 1)),
          Seq(
            stringQuestion(CharityBeneficiaryNamePage(index), userAnswers, "charityBeneficiaryName"),
            yesNoQuestion(CharityBeneficiaryDiscretionYesNoPage(index), userAnswers, "charityBeneficiaryShareOfIncomeYesNo", name),
            stringQuestion(CharityBeneficiaryShareOfIncomePage(index), userAnswers, "charityBeneficiaryShareOfIncome", name),
            yesNoQuestion(CharityBeneficiaryAddressYesNoPage(index), userAnswers, "charityBeneficiaryAddressYesNo", name),
            yesNoQuestion(CharityBeneficiaryAddressUKYesNoPage(index), userAnswers, "charityBeneficiaryAddressUKYesNo", name),
            addressQuestion(CharityBeneficiaryAddressPage(index), userAnswers, "charityBeneficiaryAddress", name, countryOptions),
            utrQuestion(CharityBeneficiaryUtrPage(index), userAnswers, "charityBeneficiaryUtr", name)
          ).flatten,
          sectionKey = None
        )
      )
    }.getOrElse(Nil)
  }

}
