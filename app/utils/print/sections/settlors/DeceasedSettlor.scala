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
import pages.settlors.deceased_settlor._
import play.api.i18n.Messages
import utils.print.sections.AnswerRowConverter._
import utils.CheckAnswersFormatters
import utils.countryoptions.CountryOptions
import viewmodels.AnswerSection

object DeceasedSettlor {

  def apply(userAnswers: UserAnswers, countryOptions: CountryOptions)(implicit messages: Messages): Seq[AnswerSection] = {
    userAnswers.get(SettlorNamePage).map(CheckAnswersFormatters.fullName).map { name =>
      Seq(
        AnswerSection(
          headingKey = None,
          rows = Seq(
            fullNameQuestion(SettlorNamePage, userAnswers, "settlorName"),
            yesNoQuestion(SettlorDateOfDeathYesNoPage, userAnswers, "settlorDateOfDeathYesNo", name),
            dateQuestion(SettlorDateOfDeathPage, userAnswers, "settlorDateOfDeath", name),
            yesNoQuestion(SettlorDateOfBirthYesNoPage, userAnswers, "settlorDateOfBirthYesNo", name),
            dateQuestion(SettlorDateOfBirthPage, userAnswers, "settlorDateOfBirth", name),
            yesNoQuestion(SettlorNationalInsuranceYesNoPage, userAnswers, "settlorNationalInsuranceYesNo", name),
            ninoQuestion(SettlorNationalInsuranceNumberPage, userAnswers, "settlorNationalInsuranceNumber", name),
            yesNoQuestion(SettlorLastKnownAddressYesNoPage, userAnswers, "settlorLastKnownAddressYesNo", name),
            yesNoQuestion(SettlorLastKnownAddressUKYesNoPage, userAnswers, "settlorLastKnownAddressUKYesNo", name),
            ukAddressQuestion(SettlorUKAddressPage, userAnswers, "settlorUKAddress", name, countryOptions),
            internationalAddressQuestion(SettlorInternationalAddressPage, userAnswers, "settlorInternationalAddress", name, countryOptions),
            passportOrIdCardQuestion(SettlorPassportIDCardPage, userAnswers, "settlorPassportOrIdCard", name, countryOptions)
          ).flatten,
          sectionKey = None
        )
      )
    }.getOrElse(Nil)
  }

}
