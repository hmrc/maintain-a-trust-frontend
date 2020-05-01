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
import models.pages.KindOfBusiness
import pages.settlors.living_settlor._
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import queries.Gettable
import utils.countryoptions.CountryOptions
import viewmodels.{AnswerRow, AnswerSection}
import utils.print.sections.AnswerRowConverter._

object SettlorCompany {

  def apply(index: Int, userAnswers: UserAnswers, countryOptions: CountryOptions)(implicit messages: Messages): Option[Seq[AnswerSection]] = {

    userAnswers.get(SettlorBusinessNamePage(index)).flatMap { name =>
      Some(Seq(
        AnswerSection(
          headingKey = Some(messages("answerPage.section.settlor.subheading", index + 1)),
          Seq(
            stringQuestion(SettlorBusinessNamePage(index), userAnswers, "settlorBusinessName"),
            yesNoQuestion(SettlorUtrYesNoPage(index), userAnswers, "settlorBusinessUtrYesNo", name),
            stringQuestion(SettlorUtrPage(index), userAnswers, "settlorBusinessUtr", name),
            yesNoQuestion(SettlorAddressYesNoPage(index), userAnswers, "settlorBusinessAddressYesNo", name),
            yesNoQuestion(SettlorAddressUKYesNoPage(index), userAnswers, "settlorBusinessAddressUKYesNo", name),
            addressQuestion(SettlorAddressUKPage(index), userAnswers, "settlorBusinessAddressUK", name, countryOptions),
            addressQuestion(SettlorAddressInternationalPage(index), userAnswers, "settlorBusinessAddressUK", name, countryOptions),
            kindOfBusinessQuestion(SettlorCompanyTypePage(index), userAnswers, "settlorBusinessType", name, messages),
            yesNoQuestion(SettlorCompanyTimePage(index), userAnswers, "settlorBusinessTime", name)
          ).flatten,
          sectionKey = None
        )
      ))
    }
  }

  def kindOfBusinessQuestion(query: Gettable[KindOfBusiness],
                             userAnswers: UserAnswers,
                             labelKey: String,
                             messageArg : String,
                             messages: Messages): Option[AnswerRow] = {
    userAnswers.get(query) map { x =>
      AnswerRow(
        messages(s"$labelKey.checkYourAnswersLabel", messageArg),
        HtmlFormat.escape(x.toString),
        None
      )
    }
  }

}