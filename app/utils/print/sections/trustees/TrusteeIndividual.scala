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
import utils.{CheckAnswersFormatters, CountryOptions}
import viewmodels.AnswerSection
import utils.print.sections.AnswerRowConverter._

object TrusteeIndividual {

  def apply(index: Int,
            userAnswers: UserAnswers,
            countryOptions: CountryOptions)
           (implicit messages: Messages): Option[Seq[AnswerSection]] = {

    userAnswers.get(TrusteesNamePage(index)).map(CheckAnswersFormatters.fullName).flatMap { name =>
      Some(Seq(
        AnswerSection(
          headingKey = Some(messages("answerPage.section.trustee.subheading") + s" ${index + 1}"),
          Seq(
            fullNameQuestion(TrusteesNamePage(index), userAnswers, "trusteesName"),
            yesNoQuestion(TrusteeDateOfBirthYesNoPage(index), userAnswers, "trusteeDateOfBirthYesNo", name),
            dateQuestion(TrusteesDateOfBirthPage(index), userAnswers, "trusteesDateOfBirth", name),
            yesNoQuestion(TrusteeNinoYesNoPage(index), userAnswers, "trusteeNinoYesNo", name),
            ninoQuestion(TrusteesNinoPage(index), userAnswers, "trusteesNino", name),
            yesNoQuestion(TrusteeAddressYesNoPage(index), userAnswers, "trusteeUkAddressYesNo", name),
            yesNoQuestion(TrusteeAddressInTheUKPage(index), userAnswers, "trusteeLiveInTheUK", name),
            addressQuestion(TrusteeAddressPage(index), userAnswers, "trusteesUkAddress", name, countryOptions),
            yesNoQuestion(TrusteePassportIDCardYesNoPage(index), userAnswers, "trusteePassportOrIdCardYesNo", name),
            passportOrIdCardQuestion(TrusteePassportIDCardPage(index), userAnswers, "trusteePassportOrIdCard", name, countryOptions)
          ).flatten,
          sectionKey = None
        ))
      )
    }
  }

}
