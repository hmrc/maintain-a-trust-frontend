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
import pages.beneficiaries.other._
import play.api.i18n.Messages
import utils.print.sections.AnswerRowConverter
import viewmodels.AnswerSection

class OtherBeneficiaryPrinter @Inject()(converter: AnswerRowConverter) {

  def print(index: Int, userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] = {

    userAnswers.get(OtherBeneficiaryDescriptionPage(index)).map { name =>
      Seq(AnswerSection(
        headingKey = Some(messages("answerPage.section.otherBeneficiary.subheading", index + 1)),
        Seq(
          converter.stringQuestion(OtherBeneficiaryDescriptionPage(index), userAnswers, "otherBeneficiaryDescription"),
          converter.yesNoQuestion(OtherBeneficiaryDiscretionYesNoPage(index), userAnswers, "otherBeneficiaryShareOfIncomeYesNo", name),
          converter.percentageQuestion(OtherBeneficiaryShareOfIncomePage(index), userAnswers, "otherBeneficiaryShareOfIncome", name),
          converter.yesNoQuestion(OtherBeneficiaryAddressYesNoPage(index), userAnswers, "otherBeneficiaryAddressYesNo", name),
          converter.yesNoQuestion(OtherBeneficiaryAddressUKYesNoPage(index), userAnswers, "otherBeneficiaryAddressUKYesNo", name),
          converter.addressQuestion(OtherBeneficiaryAddressPage(index), userAnswers, "otherBeneficiaryAddress", name)
        ).flatten,
        sectionKey = None
      ))
    }.getOrElse(Nil)
  }

}
