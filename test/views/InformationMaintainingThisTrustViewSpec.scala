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
import views.html.InformationMaintainingThisTrustView

class InformationMaintainingThisTrustViewSpec extends ViewBehaviours {

  "InformationMaintainingThisTrust view for UTR" must {

    val utr = "1234545678"

    val view = viewFor[InformationMaintainingThisTrustView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(utr, UTR)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(applyView,
      "informationMaintainingThisTrust",
      "utr",
      utr,
      "warning",
      "updateDetails",
      "paragraph2",
      "paragraph3",
      "paragraph4"
    )

    behave like pageWithBackLink(applyView)

    behave like pageWithContinueButton(applyView, Some("site.startMaintaining"))

  }

  "InformationMaintainingThisTrust view for URN" must {

    val urn = "XATRUST12345678"

    val view = viewFor[InformationMaintainingThisTrustView](Some(emptyUserAnswersForUrn))

    val applyView = view.apply(urn, URN)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(applyView,
      "informationMaintainingThisTrust",
      "urn",
      urn,
      "warning",
      "updateDetails",
      "paragraph2",
      "paragraph3",
      "paragraph4"
    )

    behave like pageWithBackLink(applyView)

    behave like pageWithContinueButton(applyView, Some("site.startMaintaining"))

  }
}
