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

package utils.print.sections.trustees

import javax.inject.Inject
import models.UserAnswers
import pages.trustees._
import play.api.i18n.Messages
import utils.print.sections.AnswerRowConverter
import viewmodels.AnswerSection

class TrusteeOrganisationPrinter @Inject()(converter: AnswerRowConverter) {

  def print(index: Int, userAnswers: UserAnswers)(implicit messages: Messages): Option[Seq[AnswerSection]] = {

    userAnswers.get(TrusteeOrgNamePage(index)).flatMap { name =>
      Some(Seq(
        AnswerSection(
          headingKey = Some(messages("answerPage.section.trustee.subheading", index + 1)),
          Seq(
            converter.stringQuestion(TrusteeOrgNamePage(index), userAnswers, "trusteeBusinessName"),
            converter.yesNoQuestion(TrusteeUtrYesNoPage(index), userAnswers, "trusteeUtrYesNo", name),
            converter.stringQuestion(TrusteeUtrPage(index), userAnswers, "trusteeUtr", name),
            converter.yesNoQuestion(TrusteeAddressYesNoPage(index), userAnswers, "trusteeUkAddressYesNo", name),
            converter.yesNoQuestion(TrusteeAddressInTheUKPage(index), userAnswers, "trusteeLiveInTheUK", name),
            converter.addressQuestion(TrusteeAddressPage(index), userAnswers, "trusteeUkAddress", name)
          ).flatten,
          sectionKey = None
        )
      ))
    }
  }

}
