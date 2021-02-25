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
import views.html.AgentNotAuthorisedView

class AgentNotAuthorisedViewSpec extends ViewBehaviours {



  "AgentNotAuthorised view for UTR" must {
    val utr = "0987654321"
    val view = viewFor[AgentNotAuthorisedView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply("0987654321", UTR)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(applyView,
      "agentNotAuthorised",
      "utr",
      utr,
      "p1", "p2", "p3", "p4", "p5")

    behave like pageWithBackLink(applyView)
  }

  "AgentNotAuthorised view for URN" must {
    val urn = "XATRUST12345678"
    val view = viewFor[AgentNotAuthorisedView](Some(emptyUserAnswersForUrn))

    val applyView = view.apply("XATRUST12345678", URN)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(applyView,
      "agentNotAuthorised",
      "urn",
      urn,
      "p1", "p2", "p3", "p4", "p5")

    behave like pageWithBackLink(applyView)
  }
}
