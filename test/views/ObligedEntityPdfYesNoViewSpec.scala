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

package views

import forms.YesNoFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.ObligedEntityPdfYesNoView

class ObligedEntityPdfYesNoViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "obligedEntityPdfYesNo"

  val form: Form[Boolean] = new YesNoFormProvider().withPrefix(messageKeyPrefix)

  val identifier = "1234567890"

  "ObligedEntityPdfYesNo view" must {

    val view = viewFor[ObligedEntityPdfYesNoView](Some(emptyUserAnswersForUtr))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, identifier)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(
      view = applyView(form),
      messageKeyPrefix = messageKeyPrefix,
      captionKey = "obligedEntityPdfYesNo",
      captionParam = "",
      "p1",
      "bullet1",
      "bullet2",
      "p2",
      "details.relevant.persons",
      "details.p1",
      "details.bullet1",
      "details.bullet2",
      "details.bullet3",
      "details.bullet4",
      "details.bullet5",
      "details.bullet6",
      "details.bullet7",
      "details.bullet8",
      "details.bullet9",
      "details.bullet10",
      "details.bullet11",
      "details.bullet12",
      "details.bullet13",
      "details.bullet14",
      "details.bullet15",
      "subheading1",
      "p3",
      "bullet3",
      "bullet4",
      "bullet5",
      "bullet6",
      "p4",
      "bullet7",
      "bullet8",
      "bullet9",
      "bullet10",
      "p5",
      "bullet11",
      "bullet12",
      "bullet13",
      "subheading2",
      "p6",
      "p7",
      "bullet14",
      "bullet15",
      "p8",
      "question",
      "yes",
      "no"
    )

    behave like pageWithBackLink(applyView(form))

    behave like yesNoPage(form, applyView, messageKeyPrefix)
  }
}
