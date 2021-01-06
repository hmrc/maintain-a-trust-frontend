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

package views.close

import forms.YesNoFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.close.DateLastAssetSharedOutYesNoView

class DateLastAssetSharedOutYesNoViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "dateLastAssetSharedOutYesNo"
  val utr = "1234567890"
  val form: Form[Boolean] = new YesNoFormProvider().withPrefix(messageKeyPrefix)

  "DateLastAssetSharedOutYesNo view" must {

    val view = viewFor[DateLastAssetSharedOutYesNoView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, utr)(fakeRequest, messages)

    "Have a dynamic utr in the subheading" in {
      val doc = asDocument(applyView(form))
      assertContainsText(doc, s"This trust’s UTR: $utr")
    }

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like yesNoPage(form, applyView, messageKeyPrefix)

    behave like pageWithASubmitButton(applyView(form))
  }
}
