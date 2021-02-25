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
import views.html.status.IdentifierDoesNotMatchView

class IdentifierDoesNotMatchViewSpec extends ViewBehaviours {

  "IdentifierDoesNotMatch view for utr" must {

    val view = viewFor[IdentifierDoesNotMatchView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(AffinityGroup.Agent, UTR)(fakeRequest, messages)

    behave like normalPage(
      applyView,
      "identifierDoesNotMatch.UTR",
      "p1",
      "p2",
      "p3",
      "contact.link",
      "p4",
      "return.link")
  }

  "IdentifierDoesNotMatch view for urn" must {

    val view = viewFor[IdentifierDoesNotMatchView](Some(emptyUserAnswersForUrn))

    val applyView = view.apply(AffinityGroup.Agent, URN)(fakeRequest, messages)

    behave like normalPage(
      applyView,
      "identifierDoesNotMatch.URN",
      "p1",
      "p2",
      "p3",
      "contact.link",
      "p4",
      "return.link")
  }
}
