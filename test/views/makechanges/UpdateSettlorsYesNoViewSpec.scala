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

package views.makechanges

import forms.YesNoFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.makechanges.UpdateSettlorsYesNoView

class UpdateSettlorsYesNoViewSpec extends YesNoViewBehaviours {

  "UpdateSettlorsYesNoView" when {

    "making changes" must {

      val messageKeyPrefix = "updateSettlors"
      val determinePrefix = (_: Boolean) => messageKeyPrefix
      val form = new YesNoFormProvider().withPrefix(messageKeyPrefix)

      val view = viewFor[UpdateSettlorsYesNoView](Some(emptyUserAnswersForUtr))

      def applyView(form: Form[_]): HtmlFormat.Appendable =
        view.apply(form, determinePrefix, closingTrust = false)(fakeRequest, messages)

      behave like normalPage(applyView(form), messageKeyPrefix, "additionalContent1")

      behave like pageWithBackLink(applyView(form))

      behave like yesNoPage(form, applyView, messageKeyPrefix)

      behave like pageWithASubmitButton(applyView(form))
    }

    "closing" must {

      val messageKeyPrefix = "updateSettlorsClosing"
      val determinePrefix = (_: Boolean) => messageKeyPrefix
      val form = new YesNoFormProvider().withPrefix(messageKeyPrefix)

      val view = viewFor[UpdateSettlorsYesNoView](Some(emptyUserAnswersForUtr))

      def applyView(form: Form[_]): HtmlFormat.Appendable =
        view.apply(form, determinePrefix, closingTrust = true)(fakeRequest, messages)

      behave like normalPage(applyView(form), messageKeyPrefix, "additionalContent1", "additionalContent2")

      behave like pageWithBackLink(applyView(form))

      behave like yesNoPage(form, applyView, messageKeyPrefix)

      behave like pageWithASubmitButton(applyView(form))
    }
  }
}
