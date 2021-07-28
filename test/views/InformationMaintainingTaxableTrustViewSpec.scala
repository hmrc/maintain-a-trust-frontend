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

import models.UTR
import views.behaviours.ViewBehaviours
import views.html.InformationMaintainingTaxableTrustView

class InformationMaintainingTaxableTrustViewSpec extends ViewBehaviours {

  "InformationMaintainingTaxableTrustView" when {

    val utr = "1234545678"

    val view = viewFor[InformationMaintainingTaxableTrustView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(utr, UTR)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(applyView,
      "informationMaintainingTaxableTrust",
      "utr",
      utr,
      "subheading1",
      "paragraph1",
      "subheading2",
      "paragraph2",
      "bullet1",
      "bullet2",
      "bullet3",
      "paragraph3",
      "bullet4",
      "bullet5",
      "paragraph4",
      "paragraph5",
      "paragraph6"
    )

    behave like pageWithBackLink(applyView)

    behave like pageWithContinueButton(applyView, Some("site.startMaintaining"))
  }
}
