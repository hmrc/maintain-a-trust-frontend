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

import config.FrontendAppConfig
import views.behaviours.ViewBehaviours
import views.html.InformationMaintainingThisTrustView

class InformationMaintainingThisTrustViewSpec extends ViewBehaviours {

  "InformationMaintainingThisTrust view" must {

    val utr = "1234545678"

    val view = viewFor[InformationMaintainingThisTrustView](Some(emptyUserAnswers))

    val applyView = view.apply(utr)(fakeRequest, messages)

    val config = app.injector.instanceOf[FrontendAppConfig]

    "Have a dynamic utr in the subheading" in {
      val doc = asDocument(applyView)
      assertContainsText(doc, s"This trust’s UTR: $utr")
    }

    behave like normalPage(applyView, "informationMaintainingThisTrust",
      "warning",
      "keepTrustUpdated",
      "paragraph1",
      "list1",
      "list2",
      "printsave.link",
      "list3",
      "list4",
      "changesToTrust",
      "paragraph2",
      "paragraph3"
    )

    behave like pageWithBackLink(applyView)

    behave like pageWithContinueButton(applyView, controllers.routes.WhatIsNextController.onPageLoad().url)

  }
}
