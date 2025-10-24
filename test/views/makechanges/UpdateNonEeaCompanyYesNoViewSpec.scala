/*
 * Copyright 2024 HM Revenue & Customs
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

package views.makechanges

import forms.YesNoFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.makechanges.AddNonEeaCompanyYesNoView

class UpdateNonEeaCompanyYesNoViewSpec extends YesNoViewBehaviours {

  def displayExpectedContent(messageKeyPrefix: String): Unit = {

    val form = new YesNoFormProvider().withPrefix(messageKeyPrefix)

    val view = viewFor[AddNonEeaCompanyYesNoView](Some(emptyUserAnswersForUtr))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, messageKeyPrefix)(fakeRequest, messages)

    behave like normalPage(
      applyView(form),
      "nonEeaCompany",
      expectedGuidanceKeys =
        "paragraph1",
        "lead_in",
        "bullet1",
        "bullet2",
        "bullet3",
        "bullet4",
        "bullet5",
        "paragraph2",
        "bullet6",
        "bullet7",
        "bullet8"
    )

    behave like pageWithBackLink(applyView(form))

    behave like yesNoPage(form, applyView, messageKeyPrefix, Some("nonEeaCompany"))

    behave like pageWithASubmitButton(applyView(form))

  }

  "UpdateNonEeaCompanyYesNoView" when {

    "making changes" must {
      displayExpectedContent(messageKeyPrefix = "updateNonEeaCompany")
    }

    "closing" must {
      displayExpectedContent(messageKeyPrefix = "addNonEeaCompanyClosing")
    }
  }
}
