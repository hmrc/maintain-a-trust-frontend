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

package views.close

import views.behaviours.ViewBehaviours
import views.html.close.HowToCloseATrustView

class HowToCloseATrustViewSpec extends ViewBehaviours {

  val utr = "1234567890"

  "HowToCloseATrust view" must {

    val view = viewFor[HowToCloseATrustView](Some(emptyUserAnswers))

    val applyView = view.apply(utr)(fakeRequest, messages)

    "Have a dynamic utr in the subheading" in {
      val doc = asDocument(applyView)
      assertContainsText(doc, s"This trust’s UTR: $utr")
    }

    behave like normalPage(applyView,
      "howToCloseATrust",
      "p1",
      "bullet1",
      "bullet2",
      "p2"
    )
  }
}
