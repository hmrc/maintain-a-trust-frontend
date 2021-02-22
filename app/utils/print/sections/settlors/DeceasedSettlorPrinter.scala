/*
 * Copyright 2021 HM Revenue & Customs
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
import pages.settlors.deceased_settlor._
import play.api.i18n.Messages
import utils.print.sections.AnswerRowConverter
import viewmodels.AnswerSection

class DeceasedSettlorPrinter @Inject()(converter: AnswerRowConverter)
                                      (implicit messages: Messages) {

  def print(userAnswers: UserAnswers): Seq[AnswerSection] = {
    userAnswers.get(SettlorNamePage).map(_.toString).map { name =>
      Seq(
        AnswerSection(
          headingKey = None,
          rows = Seq(
            converter.fullNameQuestion(SettlorNamePage, userAnswers, "settlorName"),
            converter.yesNoQuestion(SettlorDateOfDeathYesNoPage, userAnswers, "settlorDateOfDeathYesNo", name),
            converter.dateQuestion(SettlorDateOfDeathPage, userAnswers, "settlorDateOfDeath", name),
            converter.yesNoQuestion(SettlorDateOfBirthYesNoPage, userAnswers, "settlorDateOfBirthYesNo", name),
            converter.dateQuestion(SettlorDateOfBirthPage, userAnswers, "settlorDateOfBirth", name),
            converter.yesNoQuestion(SettlorNationalInsuranceYesNoPage, userAnswers, "settlorNationalInsuranceYesNo", name),
            converter.ninoQuestion(SettlorNationalInsuranceNumberPage, userAnswers, "settlorNationalInsuranceNumber", name),
            converter.yesNoQuestion(SettlorLastKnownAddressYesNoPage, userAnswers, "settlorLastKnownAddressYesNo", name),
            converter.yesNoQuestion(SettlorLastKnownAddressUKYesNoPage, userAnswers, "settlorLastKnownAddressUKYesNo", name),
            converter.addressQuestion(SettlorUKAddressPage, userAnswers, "settlorUKAddress", name),
            converter.addressQuestion(SettlorInternationalAddressPage, userAnswers, "settlorInternationalAddress", name),
            converter.passportOrIdCardQuestion(SettlorPassportIDCardPage, userAnswers, "settlorPassportOrIdCard", name)
          ).flatten,
          sectionKey = None
        )
      )
    }.getOrElse(Nil)
  }

}
