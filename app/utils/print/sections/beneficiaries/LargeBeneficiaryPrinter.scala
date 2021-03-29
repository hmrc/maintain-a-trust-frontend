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
import models.{Description, UserAnswers}
import pages.beneficiaries.large._
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import queries.Gettable
import utils.print.sections.AnswerRowConverter
import viewmodels.{AnswerRow, AnswerSection}

class LargeBeneficiaryPrinter @Inject()(converter: AnswerRowConverter) {

  def print(index: Int, userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] =

    userAnswers.get(LargeBeneficiaryNamePage(index)).map { name =>
      Seq(AnswerSection(
        headingKey = Some(messages("answerPage.section.largeBeneficiary.subheading", index + 1)),
        Seq(
          converter.stringQuestion(LargeBeneficiaryNamePage(index), userAnswers, "largeBeneficiaryName"),
          converter.yesNoQuestion(LargeBeneficiaryAddressYesNoPage(index), userAnswers, "largeBeneficiaryAddressYesNo", name),
          converter.yesNoQuestion(LargeBeneficiaryAddressUKYesNoPage(index), userAnswers, "largeBeneficiaryAddressUKYesNo", name),
          converter.addressQuestion(LargeBeneficiaryAddressPage(index), userAnswers, "largeBeneficiaryAddress", name),
          converter.stringQuestion(LargeBeneficiaryUtrPage(index), userAnswers, "largeBeneficiaryUtr", name),
          descriptionQuestion(LargeBeneficiaryDescriptionPage(index), userAnswers, name),
          converter.numberOfBeneficiariesQuestion(LargeBeneficiaryNumberOfBeneficiariesPage(index), userAnswers, "largeBeneficiaryNumberOfBeneficiaries")
        ).flatten
      ))
    }.getOrElse(Nil)

  private def descriptionQuestion(query: Gettable[Description],
                                  userAnswers: UserAnswers,
                                  messageArg: String)(implicit messages: Messages): Option[AnswerRow] = {

    userAnswers.get(query) map {x =>
      AnswerRow(
        messages("largeBeneficiaryDescription.checkYourAnswersLabel", messageArg),
        description(x),
        None
      )
    }
  }

  private def description(description: Description): Html = {
    val lines =
      Seq(
        Some(HtmlFormat.escape(description.description)),
        description.description1.map(HtmlFormat.escape),
        description.description2.map(HtmlFormat.escape),
        description.description3.map(HtmlFormat.escape),
        description.description4.map(HtmlFormat.escape)
      ).flatten

    Html(lines.mkString("<br />"))
  }

}
