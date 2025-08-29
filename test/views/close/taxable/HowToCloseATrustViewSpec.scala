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

package views.close.taxable

import models.{URN, UTR}
import views.behaviours.ViewBehaviours
import views.html.close.taxable.HowToCloseATrustView

class HowToCloseATrustViewSpec extends ViewBehaviours {

  "HowToCloseATrust view for utr" must {
    val utr = "1234567890"
    val view = viewFor[HowToCloseATrustView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(utr, UTR)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(applyView,
      "howToCloseATrust",
      "utr",
      utr,
      "p1",
      "bullet1",
      "bullet2",
      "p2"
    )
  }

  "HowToCloseATrust view for urn" must {
    val urn = "XATRUST12345678"
    val view = viewFor[HowToCloseATrustView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(urn, URN)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(applyView,
      "howToCloseATrust",
      "urn",
      urn,
      "p1",
      "bullet1",
      "bullet2",
      "p2"
    )
  }
}
