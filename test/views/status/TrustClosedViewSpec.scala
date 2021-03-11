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

package views.status

import models.{URN, UTR}
import uk.gov.hmrc.auth.core.AffinityGroup
import views.behaviours.ViewBehaviours
import views.html.status.TrustClosedView

class TrustClosedViewSpec extends ViewBehaviours {

  "TrustClosed view for UTR" must {
    val utr = "0987654321"
    val view = viewFor[TrustClosedView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(AffinityGroup.Agent, utr, UTR)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(
      applyView,
      "trustClosed",
      "utr",
      utr,
      "p1",
      "utr.p2",
      "contact.link",
      "p3",
      "return.link"
    )

  }

  "TrustClosed view for URN" must {
    val urn = "XATRUST12345678"
    val view = viewFor[TrustClosedView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(AffinityGroup.Agent, urn, URN)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(
      applyView,
      "trustClosed",
      "urn",
      urn,
      "p1",
      "urn.p2",
      "contact.link",
      "p3",
      "return.link"
    )

  }
}
