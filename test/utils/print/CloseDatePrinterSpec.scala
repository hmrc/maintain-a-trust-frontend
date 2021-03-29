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

package utils.print

import java.time.LocalDate
import base.SpecBase
import pages.close.taxable.DateLastAssetSharedOutPage
import play.twirl.api.Html
import utils.print.sections.CloseDatePrinter
import viewmodels.{AnswerRow, AnswerSection}

class CloseDatePrinterSpec extends SpecBase {

  private val helper: CloseDatePrinter = injector.instanceOf[CloseDatePrinter]

  "CloseDatePrinter" must {

    "generate close date section" in {

      val answers = emptyUserAnswersForUtr
        .set(DateLastAssetSharedOutPage, LocalDate.parse("2019-02-03")).success.value

      val result = helper.print(answers)

      result mustBe
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(
              label = messages("dateLastAssetSharedOut.checkYourAnswersLabel"),
              answer = Html("3 February 2019"),
              changeUrl = None
            )
          ),
          sectionKey = Some(messages("answerPage.section.closeDate.heading"))
        )

    }

  }

}
