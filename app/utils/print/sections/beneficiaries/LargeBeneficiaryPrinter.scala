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

import models.{Description, UserAnswers}
import pages.beneficiaries.large._
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import queries.Gettable
import utils.countryoptions.CountryOptions
import utils.print.sections.AnswerRowConverter._
import viewmodels.{AnswerRow, AnswerSection}

object LargeBeneficiaryPrinter {

  def print(index: Int, userAnswers: UserAnswers, countryOptions: CountryOptions)
           (implicit messages: Messages): Seq[AnswerSection] =

    userAnswers.get(LargeBeneficiaryNamePage(index)).map { name =>
      Seq(AnswerSection(
        headingKey = Some(messages("answerPage.section.largeBeneficiary.subheading", index + 1)),
        Seq(
          stringQuestion(LargeBeneficiaryNamePage(index), userAnswers, "largeBeneficiaryName"),
          yesNoQuestion(LargeBeneficiaryAddressYesNoPage(index), userAnswers, "largeBeneficiaryAddressYesNo", name),
          yesNoQuestion(LargeBeneficiaryAddressUKYesNoPage(index), userAnswers, "largeBeneficiaryAddressUKYesNo", name),
          addressQuestion(LargeBeneficiaryAddressPage(index), userAnswers, "largeBeneficiaryAddress", countryOptions = countryOptions, messageArg = name),
          utrQuestion(LargeBeneficiaryUtrPage(index), userAnswers, "largeBeneficiaryUtr", name),
          descriptionQuestion(LargeBeneficiaryDescriptionPage(index), userAnswers, "largeBeneficiaryDescription", name),
          stringQuestion(LargeBeneficiaryNumberOfBeneficiariesPage(index), userAnswers, "largeBeneficiaryNumberOfBeneficiaries")
        ).flatten
      ))
    }.getOrElse(Nil)

  private def descriptionQuestion(query: Gettable[Description],
                                  userAnswers: UserAnswers,
                                  labelKey: String,
                                  messageArg: String
                                 )(implicit messages:Messages) = {
    userAnswers.get(query) map {x =>
      AnswerRow(
        messages(s"$labelKey.checkYourAnswersLabel", messageArg),
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
