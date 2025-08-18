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

package views


import models.UTR
import views.behaviours.ViewBehaviours
import views.html.MigrateTo5mldInformationView

class MigrateTo5mldInformationViewSpec extends ViewBehaviours {

  "InformationMaintainingThisTrust view for UTR for Org User" must {

    val utr = "1234567890"

    val view = viewFor[MigrateTo5mldInformationView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(utr, UTR, isAgent = false)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(applyView,
      "migrateTo5mldInformation",
      "utr",
      utr,
      "p1",
      "p2",
      "bullet1",
      "bullet2",
      "bullet3",
      "p3",
      "details",
      "details.p1",
      "details.p2",
      "details.p3",
      "details.bullet1",
      "details.bullet2",
      "p5",
      "org.p6",
      "heading2",
      "p7")

    behave like pageWithBackLink(applyView)

    behave like pageWithASubmitButton(applyView)

  }

  "InformationMaintainingThisTrust view for UTR for Agent User" must {

    val utr = "1234567890"

    val view = viewFor[MigrateTo5mldInformationView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(utr, UTR, isAgent = true)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(applyView,
      "migrateTo5mldInformation",
      "utr",
      utr,
      "p1",
      "p2",
      "bullet1",
      "bullet2",
      "bullet3",
      "p3",
      "details",
      "details.p1",
      "details.p2",
      "details.p3",
      "details.bullet1",
      "details.bullet2",
      "p5",
      "agent.p6",
      "heading2",
      "p7")

    behave like pageWithBackLink(applyView)

    behave like pageWithASubmitButton(applyView)

  }
}
