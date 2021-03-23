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

import views.behaviours.ViewBehaviours
import views.html.makechanges.UnavailableSectionsView

class UnavailableSectionsViewSpec extends ViewBehaviours {

  "UnavailableSections view" must {

    val available = "available section"
    val unavailable = "unavailable section"
    val future = "future section"
    val messageKeyPrefix = "unavailableSections"

    val view = viewFor[UnavailableSectionsView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(available, unavailable, future)(fakeRequest, messages)

    "Have dynamic content" in {
      val doc = asDocument(applyView)
      assertContainsText(doc, messages(s"$messageKeyPrefix.p1", unavailable))
      assertContainsText(doc, messages(s"$messageKeyPrefix.p2", future))
    }

    behave like dynamicTitlePage(applyView, s"$messageKeyPrefix",
      available,
      "p1.a",
      "p3.start",
      "p3.link",
      "p3.end"
    )

    behave like pageWithBackLink(applyView)

  }
}
