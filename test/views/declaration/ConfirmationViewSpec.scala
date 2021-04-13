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

package views.declaration

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.declaration.ConfirmationView

class ConfirmationViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "confirmationPage"
  val fakeTvn = "XC TVN 000 000 4912"
  val accessibleRefNumber = fakeTvn

  private def confirmationPage(view: HtmlFormat.Appendable, isTrustTaxable: Boolean = true) : Unit = {

    "assert content" in {
      val doc = asDocument(view)

      assertContainsText(doc, s"Declaration received")
      assertContainsText(doc, s"Your reference is:")
      assertContainsText(doc, s"$fakeTvn")
      assertContainsText(doc, "Print or save a copy of your answers")

      assertContainsText(doc, "What happens next")

      assertContainsText(doc, "Keep a note of your reference in case you need to contact HMRC. If there is a problem with the declaration, we will contact the lead trustee.")

      assertContainsText(doc, "If any of the settlor, trustee or beneficiary details change (before you make your next declaration) you will need to update them using the online service.")

      assertContainsText(doc, "Declaring the trust is up to date")

      if (isTrustTaxable) {
        assertContainsText(doc, "You need to declare every year the details we have are up to date. This needs to be done through the Trust Registration Service and Self Assessment online or the Trust and Estate Tax Return form (SA900).")
      } else {
        assertNotRenderedById(doc, "taxable-message")
      }
    }

  }

  private def confirmationPageForAgent(view: HtmlFormat.Appendable) : Unit = {
    "display return to agent overview link" in {

      val doc = asDocument(view)
      val agentOverviewLink = doc.getElementById("agent-overview")
      assertAttributeValueForElement(agentOverviewLink, "href", "#")
      assertContainsTextForId(doc, "agent-overview", "return to register and maintain a trust for a client.")
    }

  }

  "Confirmation view for an agent" must {
    val view = viewFor[ConfirmationView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(
      fakeTvn = fakeTvn,
      isAgent = true,
      isTrustTaxable = true,
      agentOverviewUrl = "#"
    )(fakeRequest, messages)

    behave like confirmationPage(applyView)

    behave like confirmationPageForAgent(applyView)
  }

  "Confirmation view for an organisation" must {
    val view = viewFor[ConfirmationView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(
      fakeTvn = fakeTvn,
      isAgent = true,
      isTrustTaxable = true,
      agentOverviewUrl = "#"
    )(fakeRequest, messages)

    behave like confirmationPage(applyView)
  }

  "Non Taxable confirmation view for an organisation" must {
    val view = viewFor[ConfirmationView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(
      fakeTvn = fakeTvn,
      isAgent = true,
      isTrustTaxable = false,
      agentOverviewUrl = "#"
    )(fakeRequest, messages)

    behave like confirmationPage(applyView, isTrustTaxable = false)
  }

}
