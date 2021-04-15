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

import base.SpecBase
import models.InternationalAddress
import pages.assets.nonEeaBusiness.{NonEeaBusinessAddressPage, NonEeaBusinessGoverningCountryPage, NonEeaBusinessNamePage}
import play.twirl.api.Html
import utils.print.sections.assets.AllAssetsPrinter
import viewmodels.{AnswerRow, AnswerSection}

class AllAssetsPrinterSpec extends SpecBase {

  private val helper: AllAssetsPrinter = injector.instanceOf[AllAssetsPrinter]
  private val name = "NonEeaBusiness"
  private val address = InternationalAddress("Line 1", "Line 2", None, "DE")

  "AllAssetsPrinter" must {

    "generate Non-Eea Company section" when {

      "taxable" in {

        val answers = emptyUserAnswersForUtr
          .set(NonEeaBusinessNamePage(0), name).success.value
          .set(NonEeaBusinessAddressPage(0), address).success.value
          .set(NonEeaBusinessGoverningCountryPage(0), "FR").success.value
          .set(NonEeaBusinessNamePage(1), name).success.value
          .set(NonEeaBusinessAddressPage(1), address).success.value
          .set(NonEeaBusinessGoverningCountryPage(1), "FR").success.value

        val result = helper.entities(answers)

        result mustBe Seq(
          AnswerSection(None, Nil, Some(messages("answerPage.section.assets.heading"))),
          AnswerSection(
            headingKey = Some("Non-EEA Company 1"),
            rows = Seq(
              AnswerRow(label = messages("nonEeaBusiness.name.checkYourAnswersLabel"), answer = Html(name), changeUrl = None),
              AnswerRow(label = messages("nonEeaBusiness.internationalAddress.checkYourAnswersLabel", name), answer = Html("Line 1<br />Line 2<br />Germany"), changeUrl = None),
              AnswerRow(label = messages("nonEeaBusiness.governingCountry.checkYourAnswersLabel", name), answer = Html("France"), changeUrl = None)
            ),
            sectionKey = None
          ),
          AnswerSection(
            headingKey = Some("Non-EEA Company 2"),
            rows = Seq(
              AnswerRow(label = messages("nonEeaBusiness.name.checkYourAnswersLabel"), answer = Html(name), changeUrl = None),
              AnswerRow(label = messages("nonEeaBusiness.internationalAddress.checkYourAnswersLabel", name), answer = Html("Line 1<br />Line 2<br />Germany"), changeUrl = None),
              AnswerRow(label = messages("nonEeaBusiness.governingCountry.checkYourAnswersLabel", name), answer = Html("France"), changeUrl = None)
            ),
            sectionKey = None
          )
        )
      }

      "non-taxable" in {

        val answers = emptyUserAnswersForUrn
          .set(NonEeaBusinessNamePage(0), name).success.value
          .set(NonEeaBusinessAddressPage(0), address).success.value
          .set(NonEeaBusinessGoverningCountryPage(0), "FR").success.value
          .set(NonEeaBusinessNamePage(1), name).success.value
          .set(NonEeaBusinessAddressPage(1), address).success.value
          .set(NonEeaBusinessGoverningCountryPage(1), "FR").success.value

        val result = helper.entities(answers)

        result mustBe Seq(
          AnswerSection(None, Nil, Some(messages("answerPage.section.nonEeaBusinesses.heading"))),
          AnswerSection(
            headingKey = Some("Non-EEA Company 1"),
            rows = Seq(
              AnswerRow(label = messages("nonEeaBusiness.name.checkYourAnswersLabel"), answer = Html(name), changeUrl = None),
              AnswerRow(label = messages("nonEeaBusiness.internationalAddress.checkYourAnswersLabel", name), answer = Html("Line 1<br />Line 2<br />Germany"), changeUrl = None),
              AnswerRow(label = messages("nonEeaBusiness.governingCountry.checkYourAnswersLabel", name), answer = Html("France"), changeUrl = None)
            ),
            sectionKey = None
          ),
          AnswerSection(
            headingKey = Some("Non-EEA Company 2"),
            rows = Seq(
              AnswerRow(label = messages("nonEeaBusiness.name.checkYourAnswersLabel"), answer = Html(name), changeUrl = None),
              AnswerRow(label = messages("nonEeaBusiness.internationalAddress.checkYourAnswersLabel", name), answer = Html("Line 1<br />Line 2<br />Germany"), changeUrl = None),
              AnswerRow(label = messages("nonEeaBusiness.governingCountry.checkYourAnswersLabel", name), answer = Html("France"), changeUrl = None)
            ),
            sectionKey = None
          )
        )
      }
    }
  }
}
