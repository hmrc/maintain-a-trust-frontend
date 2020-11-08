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

package utils.print.sections.trustees

import models.UserAnswers
import pages.trustees._
import play.api.i18n.Messages
import utils.countryoptions.CountryOptions
import utils.print.sections.AnswerRowConverter._
import viewmodels.AnswerSection

object TrusteeIndividualPrinter {

  def print(index: Int,
            userAnswers: UserAnswers,
            countryOptions: CountryOptions)
           (implicit messages: Messages): Option[Seq[AnswerSection]] = {

    userAnswers
      .get(TrusteeNamePage(index))
      .map(_.toString)
      .flatMap { name =>
        Some(
          Seq(
          AnswerSection(
            headingKey = Some(messages("answerPage.section.trustee.subheading", index + 1)),
            Seq(
              fullNameQuestion(TrusteeNamePage(index), userAnswers, "trusteeName"),
              yesNoQuestion(TrusteeDateOfBirthYesNoPage(index), userAnswers, "trusteeDateOfBirthYesNo", name),
              dateQuestion(TrusteeDateOfBirthPage(index), userAnswers, "trusteeDateOfBirth", name),
              yesNoQuestion(TrusteeNinoYesNoPage(index), userAnswers, "trusteeNinoYesNo", name),
              ninoQuestion(TrusteeNinoPage(index), userAnswers, "trusteeNino", name),
              yesNoQuestion(TrusteeAddressYesNoPage(index), userAnswers, "trusteeUkAddressYesNo", name),
              yesNoQuestion(TrusteeAddressInTheUKPage(index), userAnswers, "trusteeLiveInTheUK", name),
              addressQuestion(TrusteeAddressPage(index), userAnswers, "trusteeUkAddress", name, countryOptions),
              yesNoQuestion(TrusteePassportIDCardYesNoPage(index), userAnswers, "trusteePassportOrIdCardYesNo", name),
              passportOrIdCardQuestion(TrusteePassportIDCardPage(index), userAnswers, "trusteePassportOrIdCard", name, countryOptions)
            ).flatten,
            sectionKey = None
          ))
        )
      }
  }

}
