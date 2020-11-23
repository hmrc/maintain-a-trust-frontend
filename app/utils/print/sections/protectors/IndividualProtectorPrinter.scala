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

import javax.inject.Inject
import models.UserAnswers
import pages.protectors.individual._
import play.api.i18n.Messages
import utils.print.sections.AnswerRowConverter
import viewmodels.AnswerSection

class IndividualProtectorPrinter @Inject()(converter: AnswerRowConverter)
                                          (implicit messages: Messages) {

  def print(index: Int, userAnswers: UserAnswers): Seq[AnswerSection] =
    userAnswers.get(IndividualProtectorNamePage(index)).map(_.toString).map { protectorName =>
      Seq(AnswerSection(
        headingKey = Some(messages("answerPage.section.protector.subheading", index + 1)),
        Seq(
          converter.fullNameQuestion(IndividualProtectorNamePage(index), userAnswers, "individualProtectorName", protectorName),
          converter.yesNoQuestion(IndividualProtectorDateOfBirthYesNoPage(index), userAnswers, "individualProtectorDateOfBirthYesNo", protectorName),
          converter.dateQuestion(IndividualProtectorDateOfBirthPage(index),userAnswers, "individualProtectorDateOfBirth", protectorName),
          converter.yesNoQuestion(IndividualProtectorNINOYesNoPage(index), userAnswers, "individualProtectorNINOYesNo", protectorName),
          converter.ninoQuestion(IndividualProtectorNINOPage(index), userAnswers, "individualProtectorNINO", protectorName),
          converter.yesNoQuestion(IndividualProtectorAddressYesNoPage(index), userAnswers, "individualProtectorAddressYesNo", protectorName),
          converter.yesNoQuestion(IndividualProtectorAddressUKYesNoPage(index), userAnswers, "individualProtectorAddressUkYesNo", protectorName),
          converter.addressQuestion(IndividualProtectorAddressPage(index), userAnswers, "individualProtectorAddress", protectorName),
          converter.yesNoQuestion(IndividualProtectorPassportIDCardYesNoPage(index), userAnswers, "individualProtectorPassportIDCardYesNo", protectorName),
          converter.passportOrIdCardQuestion(IndividualProtectorPassportIDCardPage(index), userAnswers, "individualProtectorPassportIDCard", protectorName)
        ).flatten,
        sectionKey = None
      ))
    }.getOrElse(Nil)

}
