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

package views.transition

import models.URN
import views.behaviours.ViewBehaviours
import views.html.transition.BeforeYouContinueToTaxableView

class BeforeYouContinueToTaxableViewSpec extends ViewBehaviours {

  "BeforeYouContinueToTaxable" when {

    val urn = "XATRUST12345678"
    val view = viewFor[BeforeYouContinueToTaxableView](Some(emptyUserAnswersForUtr))

    "express answered at registration" must {

      val applyView = view.apply(urn, URN, displayExpress = false)(fakeRequest, messages)

      behave like normalPageTitleWithCaption(
        view = applyView,
        messageKeyPrefix = "beforeYouContinueToTaxable",
        captionKey = "urn",
        captionParam = urn,
        expectedGuidanceKeys = "p1", "p2", "bullet2", "bullet3", "bullet4", "p3", "bullet5", "bullet6", "bullet7", "bullet8", "bullet9", "bullet10"
      )

      behave like pageWithBackLink(applyView)

      behave like pageWithASubmitButton(applyView)
    }

    "express not answered at registration" must {

      val applyView = view.apply(urn, URN, displayExpress = true)(fakeRequest, messages)

      behave like normalPageTitleWithCaption(
        view = applyView,
        messageKeyPrefix = "beforeYouContinueToTaxable",
        captionKey = "urn",
        captionParam = urn,
        expectedGuidanceKeys = "p1", "p2", "bullet1", "bullet2", "bullet3", "bullet4", "p3", "bullet5", "bullet6", "bullet7", "bullet8", "bullet9", "bullet10"
      )

      behave like pageWithBackLink(applyView)

      behave like pageWithASubmitButton(applyView)
    }
  }
}
