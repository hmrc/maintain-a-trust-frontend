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
import models.pages.KindOfBusiness
import pages.settlors.living_settlor._
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import queries.Gettable
import utils.print.sections.AnswerRowConverter
import viewmodels.{AnswerRow, AnswerSection}

class SettlorCompanyPrinter @Inject()(converter: AnswerRowConverter)
                                     (implicit messages: Messages) {

  def print(index: Int, userAnswers: UserAnswers): Option[Seq[AnswerSection]] = {

    userAnswers.get(SettlorBusinessNamePage(index)).flatMap { name =>
      Some(Seq(
        AnswerSection(
          headingKey = Some(messages("answerPage.section.settlor.subheading", index + 1)),
          Seq(
            converter.stringQuestion(SettlorBusinessNamePage(index), userAnswers, "settlorBusinessName"),
            converter.yesNoQuestion(SettlorUtrYesNoPage(index), userAnswers, "settlorBusinessUtrYesNo", name),
            converter.stringQuestion(SettlorUtrPage(index), userAnswers, "settlorBusinessUtr", name),
            converter.yesNoQuestion(SettlorAddressYesNoPage(index), userAnswers, "settlorBusinessAddressYesNo", name),
            converter.yesNoQuestion(SettlorAddressUKYesNoPage(index), userAnswers, "settlorBusinessAddressUKYesNo", name),
            converter.addressQuestion(SettlorAddressUKPage(index), userAnswers, "settlorBusinessAddressUK", name),
            converter.addressQuestion(SettlorAddressInternationalPage(index), userAnswers, "settlorBusinessAddressUK", name),
            kindOfBusinessQuestion(SettlorCompanyTypePage(index), userAnswers, name, messages),
            converter.yesNoQuestion(SettlorCompanyTimePage(index), userAnswers, "settlorBusinessTime", name)
          ).flatten,
          sectionKey = None
        )
      ))
    }
  }

  private def kindOfBusinessQuestion(query: Gettable[KindOfBusiness],
                                     userAnswers: UserAnswers,
                                     messageArg : String,
                                     messages: Messages): Option[AnswerRow] = {
    userAnswers.get(query) map { x =>
      AnswerRow(
        messages("settlorBusinessType.checkYourAnswersLabel", messageArg),
        HtmlFormat.escape(x.toString),
        None
      )
    }
  }

}