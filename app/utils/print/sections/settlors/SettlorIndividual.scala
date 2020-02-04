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

package utils.print.sections.settlors

import models.UserAnswers
import pages.settlors.living_settlor._
import play.api.i18n.Messages
import utils.CheckAnswersFormatters
import utils.countryoptions.CountryOptions
import viewmodels.AnswerSection
import utils.print.sections.AnswerRowConverter._

object SettlorIndividual {

  def apply(index: Int, userAnswers: UserAnswers, countryOptions: CountryOptions)(implicit messages: Messages): Option[Seq[AnswerSection]] = {
    userAnswers.get(SettlorIndividualNamePage(index)).map(CheckAnswersFormatters.fullName).flatMap { name =>
      Some(
        Seq(
          AnswerSection(
            headingKey = Some(messages("answerPage.section.settlor.subheading", index + 1)),
            Seq(
              fullNameQuestion(SettlorIndividualNamePage(index), userAnswers, "settlorIndividualName"),
              yesNoQuestion(SettlorIndividualDateOfBirthYesNoPage(index), userAnswers, "settlorIndividualDateOfBirthYesNo", name),
              dateQuestion(SettlorIndividualDateOfBirthPage(index), userAnswers, "settlorIndividualDateOfBirth", name),
              yesNoQuestion(SettlorIndividualNINOYesNoPage(index), userAnswers, "settlorIndividualNINOYesNo", name),
              ninoQuestion(SettlorIndividualNINOPage(index), userAnswers, "settlorIndividualNINO", name),
              yesNoQuestion(SettlorAddressYesNoPage(index), userAnswers, "settlorIndividualAddressYesNo", name),
              yesNoQuestion(SettlorAddressUKYesNoPage(index), userAnswers, "settlorIndividualAddressUKYesNo", name),
              addressQuestion(SettlorAddressUKPage(index), userAnswers, "settlorIndividualAddressUK", name, countryOptions),
              addressQuestion(SettlorAddressInternationalPage(index), userAnswers, "settlorIndividualAddressInternational", name, countryOptions),
              yesNoQuestion(SettlorIndividualPassportIDCardYesNoPage(index), userAnswers, "settlorPassportOrIdCardYesNo", name),
              passportOrIdCardQuestion(SettlorIndividualPassportIDCardPage(index), userAnswers, "settlorPassportOrIdCard", name, countryOptions)
            ).flatten,
            sectionKey = None
          )
        )
      )
    }
  }

}
