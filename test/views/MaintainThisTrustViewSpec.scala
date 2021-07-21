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

import controllers.routes
import models.{URN, UTR}
import play.api.mvc.Call
import views.behaviours.ViewBehaviours
import views.html.MaintainThisTrustView

class MaintainThisTrustViewSpec extends ViewBehaviours {

  val continue: Call = routes.MaintainThisTrustController.onSubmit()

    "TrustAlreadyClaimed view for UTR" must {
    val utr = "0987654321"
    val view = viewFor[MaintainThisTrustView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(utr, UTR, "test", continue)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(applyView,
      "maintainThisTrust",
      "utr",
      utr,
      "utr.p1.a", "p1.b", "p3", "p4", "p4.a")

  }

  "TrustAlreadyClaimed view for URN" must {
    val urn = "XATRUST12345678"
    val view = viewFor[MaintainThisTrustView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(urn, URN, "", continue)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(applyView,
      "maintainThisTrust",
      "urn",
      urn,
      "urn.p1.a", "p1.b", "p3", "p4", "p4.a")

  }
}
