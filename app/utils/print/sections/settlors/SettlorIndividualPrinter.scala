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

import javax.inject.Inject
import models.UserAnswers
import pages.settlors.living_settlor._
import play.api.i18n.Messages
import utils.print.sections.AnswerRowConverter
import viewmodels.AnswerSection

class SettlorIndividualPrinter @Inject()(converter: AnswerRowConverter)
                                        (implicit messages: Messages) {

  def print(index: Int, userAnswers: UserAnswers): Option[Seq[AnswerSection]] = {
    userAnswers.get(SettlorIndividualNamePage(index)).map(_.toString).flatMap { name =>
      Some(
        Seq(
          AnswerSection(
            headingKey = Some(messages("answerPage.section.settlor.subheading", index + 1)),
            Seq(
              converter.fullNameQuestion(SettlorIndividualNamePage(index), userAnswers, "settlorIndividualName"),
              converter.yesNoQuestion(SettlorIndividualDateOfBirthYesNoPage(index), userAnswers, "settlorIndividualDateOfBirthYesNo", name),
              converter.dateQuestion(SettlorIndividualDateOfBirthPage(index), userAnswers, "settlorIndividualDateOfBirth", name),
              converter.yesNoQuestion(SettlorIndividualNINOYesNoPage(index), userAnswers, "settlorIndividualNINOYesNo", name),
              converter.ninoQuestion(SettlorIndividualNINOPage(index), userAnswers, "settlorIndividualNINO", name),
              converter.yesNoQuestion(SettlorAddressYesNoPage(index), userAnswers, "settlorIndividualAddressYesNo", name),
              converter.yesNoQuestion(SettlorAddressUKYesNoPage(index), userAnswers, "settlorIndividualAddressUKYesNo", name),
              converter.addressQuestion(SettlorAddressUKPage(index), userAnswers, "settlorIndividualAddressUK", name),
              converter.addressQuestion(SettlorAddressInternationalPage(index), userAnswers, "settlorIndividualAddressInternational", name),
              converter.yesNoQuestion(SettlorIndividualPassportIDCardYesNoPage(index), userAnswers, "settlorPassportOrIdCardYesNo", name),
              converter.passportOrIdCardQuestion(SettlorIndividualPassportIDCardPage(index), userAnswers, "settlorPassportOrIdCard", name)
            ).flatten,
            sectionKey = None
          )
        )
      )
    }
  }

}
