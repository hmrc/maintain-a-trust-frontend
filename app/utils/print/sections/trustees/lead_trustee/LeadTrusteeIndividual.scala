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

package utils.print.sections.trustees.lead_trustee

import models.UserAnswers
import pages.trustees._
import play.api.i18n.Messages
import utils.{CheckAnswersFormatters, CountryOptions}
import viewmodels.AnswerSection
import utils.print.sections.AnswerRowConverter._

object LeadTrusteeIndividual extends LeadTrustee {

  def apply(index: Int, userAnswers: UserAnswers, countryOptions: CountryOptions)(implicit messages: Messages): Option[Seq[AnswerSection]] = {

    userAnswers.get(TrusteeNamePage(index)).map(CheckAnswersFormatters.fullName).flatMap { name =>
      Some(
        Seq(
          AnswerSection(
            headingKey = Some(messages("answerPage.section.leadTrustee.subheading")),
            Seq(
              fullNameQuestion(TrusteeNamePage(index), userAnswers, "leadTrusteeName"),
              dateQuestion(TrusteeDateOfBirthPage(index), userAnswers, "trusteeDateOfBirth", name),
              yesNoQuestion(TrusteeAUKCitizenPage(index), userAnswers, "trusteeAUKCitizen", name),
              ninoQuestion(TrusteeNinoPage(index), userAnswers, "trusteeNino", name),
              yesNoQuestion(TrusteePassportIDCardYesNoPage(index), userAnswers, "trusteePassportOrIdCardYesNo", name),
              passportOrIdCardQuestion(TrusteePassportIDCardPage(index), userAnswers, "trusteePassportOrIdCard", name, countryOptions)
            ).flatten ++
            addressAnswers(index, userAnswers, countryOptions, name).flatten ++
            Seq(
              yesNoQuestion(TrusteeEmailYesNoPage(index), userAnswers, "trusteeEmailAddressYesNo", name),
              stringQuestion(TrusteeEmailPage(index), userAnswers, "trusteeEmailAddress", name),
              stringQuestion(TrusteeTelephoneNumberPage(index), userAnswers, "trusteeTelephoneNumber", name)
            ).flatten,
            sectionKey = Some(messages("answerPage.section.trustees.heading"))
          )
        )
      )
    }
  }

}
