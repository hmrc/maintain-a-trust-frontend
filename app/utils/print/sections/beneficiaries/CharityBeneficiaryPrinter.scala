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
import pages.beneficiaries.charity._
import play.api.i18n.Messages
import utils.print.sections.AnswerRowConverter
import viewmodels.AnswerSection

class CharityBeneficiaryPrinter @Inject()(converter: AnswerRowConverter) {

  def print(index: Int, userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] = {

    userAnswers.get(CharityBeneficiaryNamePage(index)).map { name =>
      Seq(
        AnswerSection(
          headingKey = Some(messages("answerPage.section.charityBeneficiary.subheading", index + 1)),
          Seq(
            converter.stringQuestion(CharityBeneficiaryNamePage(index), userAnswers, "charityBeneficiaryName"),
            converter.yesNoQuestion(CharityBeneficiaryDiscretionYesNoPage(index), userAnswers, "charityBeneficiaryShareOfIncomeYesNo", name),
            converter.percentageQuestion(CharityBeneficiaryShareOfIncomePage(index), userAnswers, "charityBeneficiaryShareOfIncome", name),
            converter.yesNoQuestion(CharityBeneficiaryAddressYesNoPage(index), userAnswers, "charityBeneficiaryAddressYesNo", name),
            converter.yesNoQuestion(CharityBeneficiaryAddressUKYesNoPage(index), userAnswers, "charityBeneficiaryAddressUKYesNo", name),
            converter.addressQuestion(CharityBeneficiaryAddressPage(index), userAnswers, "charityBeneficiaryAddress", name),
            converter.stringQuestion(CharityBeneficiaryUtrPage(index), userAnswers, "charityBeneficiaryUtr", name)
          ).flatten,
          sectionKey = None
        )
      )
    }.getOrElse(Nil)
  }

}
