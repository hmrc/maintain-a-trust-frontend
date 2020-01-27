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

package utils.print.sections.protectors

import models.UserAnswers
import pages.protectors.individual._
import play.api.i18n.Messages
import utils.{CheckAnswersFormatters, CountryOptions}
import viewmodels.AnswerSection
import utils.print.sections.AnswerRowConverter._

object IndividualProtector {

  def apply(index: Int, userAnswers: UserAnswers, countryOptions: CountryOptions)(implicit messages: Messages): Seq[AnswerSection] =
    userAnswers.get(IndividualProtectorNamePage(index)).map(CheckAnswersFormatters.fullName).map { protectorName =>
      Seq(AnswerSection(
        headingKey = Some(messages("answerPage.section.protector.subheading", index + 1)),
        Seq(
          fullNameQuestion(IndividualProtectorNamePage(index), userAnswers, "individualProtectorName", protectorName),
          yesNoQuestion(IndividualProtectorDateOfBirthYesNoPage(index), userAnswers, "individualProtectorDateOfBirthYesNo", protectorName),
          dateQuestion(IndividualProtectorDateOfBirthPage(index),userAnswers, "individualProtectorDateOfBirth", protectorName),
          yesNoQuestion(IndividualProtectorNINOYesNoPage(index), userAnswers, "individualProtectorNINOYesNo", protectorName),
          ninoQuestion(IndividualProtectorNINOPage(index), userAnswers, "individualProtectorNINO", protectorName),
          yesNoQuestion(IndividualProtectorAddressYesNoPage(index), userAnswers, "individualProtectorAddressYesNo", protectorName),
          yesNoQuestion(IndividualProtectorAddressUKYesNoPage(index), userAnswers, "individualProtectorAddressUkYesNo", protectorName),
          addressQuestion(IndividualProtectorAddressPage(index), userAnswers, "individualProtectorAddress", protectorName, countryOptions),
          yesNoQuestion(IndividualProtectorPassportIDCardYesNoPage(index), userAnswers, "individualProtectorPassportIDCardYesNo", protectorName),
          passportOrIdCardQuestion(IndividualProtectorPassportIDCardPage(index), userAnswers, "individualProtectorPassportIDCard", protectorName, countryOptions)
        ).flatten,
        sectionKey = None
      ))
    }.getOrElse(Nil)

}
