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

package views.transition

import forms.YesNoFormProvider
import models.URN
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.transition.NeedToPayTaxYesNoView

class NeedToPayTaxYesNoViewSpec extends YesNoViewBehaviours {

  val prefix = "needToPayTaxYesNo"

  val form: Form[Boolean] = new YesNoFormProvider().withPrefix(prefix)

  "NeedToPayTaxYesNoView" must {
    val urn = "XATRUST12345678"
    val view = viewFor[NeedToPayTaxYesNoView](Some(emptyUserAnswersForUtr))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, urn, URN)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(applyView(form),
      prefix,
      "urn",
      urn,
      expectedGuidanceKeys = "p1", "bullet1", "bullet2", "bullet3")

    behave like pageWithBackLink(applyView(form))

    behave like yesNoPage(form, applyView, prefix)

    behave like pageWithASubmitButton(applyView(form))
  }
}
