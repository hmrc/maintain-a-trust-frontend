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

package views

import models.{URN, UTR}
import views.behaviours.ViewBehaviours
import views.html.TrustNotClaimedView

class TrustNotClaimedViewSpec extends ViewBehaviours {



  "TrustNotClaimed view for UTR" must {
    val utr = "0987654321"
    val view = viewFor[TrustNotClaimedView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply("0987654321", UTR)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(applyView,
      "trustNotClaimed",
      "utr",
      utr,
    "bullet.1.title", "bullet.1.p1", "bullet.1.p2", "bullet.1.p3", "bullet.1.p4",
    "bullet.2.title", "bullet.2.p1", "bullet.2.p2", "bullet.2.p3", "bullet.2.p4")

    behave like pageWithBackLink(applyView)
  }

  "TrustNotClaimed view for URN" must {
    val urn = "XATRUST12345678"
    val view = viewFor[TrustNotClaimedView](Some(emptyUserAnswersForUrn))

    val applyView = view.apply("XATRUST12345678", URN)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(applyView,
      "trustNotClaimed",
      "urn",
      urn,
      "bullet.1.title", "bullet.1.p1", "bullet.1.p2", "bullet.1.p3", "bullet.1.p4",
      "bullet.2.title", "bullet.2.p1", "bullet.2.p2", "bullet.2.p3", "bullet.2.p4")

    behave like pageWithBackLink(applyView)
  }
}
