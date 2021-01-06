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
import pages.beneficiaries.trust._
import play.api.i18n.Messages
import utils.print.sections.AnswerRowConverter
import viewmodels.AnswerSection

class TrustBeneficiaryPrinter @Inject()(converter: AnswerRowConverter)
                                       (implicit messages: Messages) {

  def print(index: Int, userAnswers: UserAnswers): Seq[AnswerSection] = {

    userAnswers.get(TrustBeneficiaryNamePage(index)).map { name =>
      Seq(AnswerSection(
        headingKey = Some(messages("answerPage.section.trustBeneficiary.subheading", index + 1)),
        Seq(
          converter.stringQuestion(TrustBeneficiaryNamePage(index), userAnswers, "trustBeneficiaryName"),
          converter.yesNoQuestion(TrustBeneficiaryDiscretionYesNoPage(index), userAnswers, "trustBeneficiaryShareOfIncomeYesNo", name),
          converter.percentageQuestion(TrustBeneficiaryShareOfIncomePage(index), userAnswers, "trustBeneficiaryShareOfIncome", name),
          converter.yesNoQuestion(TrustBeneficiaryAddressYesNoPage(index), userAnswers, "trustBeneficiaryAddressYesNo", name),
          converter.yesNoQuestion(TrustBeneficiaryAddressUKYesNoPage(index), userAnswers, "trustBeneficiaryAddressUKYesNo", name),
          converter.addressQuestion(TrustBeneficiaryAddressPage(index), userAnswers, "trustBeneficiaryAddress", name),
          converter.utrQuestion(TrustBeneficiaryUtrPage(index), userAnswers, "trustBeneficiaryUtr", name)
        ).flatten,
        sectionKey = None
      ))
    }.getOrElse(Nil)
  }

}
