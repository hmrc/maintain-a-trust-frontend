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

package utils.print.sections

import models.UserAnswers
import pages.individual._
import play.api.i18n.Messages
import utils.countryoptions.CountryOptions
import utils.print.sections.AnswerRowConverter._
import viewmodels.AnswerSection

object OtherIndividualPrinter {

  def print(index: Int,
            userAnswers: UserAnswers,
            countryOptions: CountryOptions)
           (implicit messages: Messages): Seq[AnswerSection] = {
    userAnswers
      .get(OtherIndividualNamePage(index))
      .map(_.toString)
      .map { name =>
        Seq(
          AnswerSection(
            headingKey = Some(messages("answerPage.section.otherIndividual.subheading", index + 1)),
            Seq(
              fullNameQuestion(OtherIndividualNamePage(index), userAnswers, "otherIndividualName"),
              yesNoQuestion(OtherIndividualDateOfBirthYesNoPage(index), userAnswers, "otherIndividualDateOfBirthYesNo", name),
              dateQuestion(OtherIndividualDateOfBirthPage(index), userAnswers, "otherIndividualDateOfBirth", name),
              yesNoQuestion(OtherIndividualNationalInsuranceYesNoPage(index), userAnswers, "otherIndividualNINOYesNo", name),
              ninoQuestion(OtherIndividualNationalInsuranceNumberPage(index), userAnswers, "otherIndividualNINO", name),
              yesNoQuestion(OtherIndividualAddressYesNoPage(index), userAnswers, "otherIndividualAddressYesNo", name),
              yesNoQuestion(OtherIndividualAddressUKYesNoPage(index), userAnswers, "otherIndividualAddressUKYesNo", name),
              addressQuestion(OtherIndividualAddressPage(index), userAnswers, "otherIndividualAddress", name, countryOptions),
              yesNoQuestion(OtherIndividualPassportIDCardYesNoPage(index), userAnswers, "otherIndividualPassportIDCardYesNo", name),
              passportOrIdCardQuestion(OtherIndividualPassportIDCardPage(index), userAnswers, "otherIndividualPassportIDCard", name, countryOptions)
            ).flatten,
            sectionKey = None
          )
        )
      }.getOrElse(Nil)
  }

}
