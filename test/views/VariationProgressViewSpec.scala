/*
 * Copyright 2020 HM Revenue & Customs
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

import views.behaviours.ViewBehaviours
import views.html.VariationProgressView

class VariationProgressViewSpec extends ViewBehaviours {

  "VariationProgress view" must {

    val utr = "1234545678"

    val sections = Nil

    val view = viewFor[VariationProgressView](Some(emptyUserAnswers))

    val applyView = view.apply(utr, sections)(fakeRequest, messages)

    "Have a dynamic utr in the subheading" in {
      val doc = asDocument(applyView)
      assertContainsText(doc, s"This trust’s UTR: $utr")
    }

    behave like normalPage(applyView, "variationProgress",
      "subHeading.1",
      "p1",
      "p2",
      "return.link",
      "warning",
      "subHeading.2",
      "subHeading.3"
    )

    behave like pageWithBackLink(applyView)

  }
}
