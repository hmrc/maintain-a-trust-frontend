/*
 * Copyright 2024 HM Revenue & Customs
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

package views.declaration

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.declaration.CloseTrustConfirmationView

class CloseTrustConfirmationViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "closeTrustConfirmation"
  val fakeTvn = "XC TVN 000 000 4912"
  val accessibleRefNumber = fakeTvn

  private def confirmationPage(view: HtmlFormat.Appendable) : Unit = {

    "assert content" in {
      val doc = asDocument(view)

      assertContainsText(doc, s"Declaration received")
      assertContainsText(doc, s"Your reference is:")
      assertContainsText(doc, s"$fakeTvn")
      assertContainsText(doc, "Print or save a declared copy of the trust’s registration")
      assertContainsText(doc, "What happens next")
      assertContainsText(doc, "Keep a note of your reference in case you need to contact HMRC. " +
        "If there is a problem with the declaration, we will contact the lead trustee.")
      assertContainsText(doc, "Your request to close this trust will be processed and access to its online register will now be removed.")
    }

  }

  private def confirmationPageForAgent(view: HtmlFormat.Appendable) : Unit = {
    "display return to agent overview link" in {

      val doc = asDocument(view)
      val agentOverviewLink = doc.getElementById("agent-overview")
      assertAttributeValueForElement(agentOverviewLink, "href", "#")
      assertContainsTextForId(doc, "agent-overview", "return to register or manage a trust for a client.")
    }

  }

  "Confirmation view for an agent" must {
    val view = viewFor[CloseTrustConfirmationView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(
      fakeTvn = fakeTvn,
      isAgent = true,
      agentOverviewUrl = "#"
    )(fakeRequest, messages)

    behave like confirmationPage(applyView)

    behave like confirmationPageForAgent(applyView)
  }

  "Confirmation view for an organisation" must {
    val view = viewFor[CloseTrustConfirmationView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(
      fakeTvn = fakeTvn,
      isAgent = true,
      agentOverviewUrl = "#"
    )(fakeRequest, messages)

    behave like confirmationPage(applyView)
  }

}
