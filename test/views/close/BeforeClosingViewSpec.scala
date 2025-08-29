/*
 * Copyright 2025 HM Revenue & Customs
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

package views.close

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.close.BeforeClosingView

class BeforeClosingViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "beforeClosing"

  "BeforeClosingView" must {

    val view = viewFor[BeforeClosingView](Some(emptyUserAnswersForUtr))

    def applyView(): HtmlFormat.Appendable = view.apply()(fakeRequest, messages)

    behave like normalPage(
      view = applyView(),
      messageKeyPrefix = messageKeyPrefix,
      expectedGuidanceKeys = "subheading", "paragraph1", "bullet1", "bullet2", "bullet3", "bullet4", "bullet5", "paragraph2", "paragraph3")

    behave like pageWithBackLink(applyView())

    behave like pageWithASubmitButton(applyView())
  }
}
