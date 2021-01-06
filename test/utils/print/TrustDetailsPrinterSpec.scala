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
import pages.trustdetails._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

class TrustDetailsPrinterSpec extends SpecBase {

  ".print" must {

    "generate an answer section with trust name, created date and utr" in {
      val helper = injector.instanceOf[PrintPlaybackHelper]

      val answers = emptyUserAnswers
        .set(TrustNamePage, "Trust Ltd.").success.value
        .set(WhenTrustSetupPage, LocalDate.of(2019,6,1)).success.value

      val actualSection = helper.trustDetails(answers)

      actualSection mustBe Seq(
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow("What is the trust’s name?", Html("Trust Ltd."), None),
            AnswerRow("When was the trust created?", Html("1 June 2019"), None),
            AnswerRow("What is the trust’s Unique Taxpayer Reference (UTR)?", Html("utr"), None)
          ),
          sectionKey = Some("Trust details")
        )
      )
    }
  }

}
