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
import utils.countryoptions.CountryOptions
import viewmodels.AnswerSection
import utils.print.sections.AnswerRowConverter._

object LeadTrusteeBusiness extends LeadTrustee {

  def apply(index: Int, userAnswers: UserAnswers, countryOptions: CountryOptions)(implicit messages: Messages): Option[Seq[AnswerSection]] = {

    userAnswers.get(TrusteeOrgNamePage(index)).flatMap { name =>
      Some(
        Seq(
          AnswerSection(
            headingKey = Some(messages("answerPage.section.leadTrustee.subheading")),
            Seq(
              yesNoQuestion(TrusteeUtrYesNoPage(index), userAnswers, "leadTrusteeUtrYesNo", name),
              stringQuestion(TrusteeOrgNamePage(index), userAnswers, "trusteeBusinessName"),
              stringQuestion(TrusteeUtrPage(index), userAnswers, "trusteeUtr", name)
            ).flatten ++
            addressAnswers(index, userAnswers, countryOptions, name).flatten ++
            Seq(yesNoQuestion(TrusteeEmailYesNoPage(index), userAnswers, "trusteeEmailAddressYesNo", name),
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
