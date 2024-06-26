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

package views.transition.declaration

import views.behaviours.ViewBehaviours
import views.html.transition.declaration.AgentConfirmationView

class AgentConfirmationViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "agentConfirmation"
  val fakeTvn = "XC TVN 000 000 4912"
  val leadTrusteeName = "Name"

  "AgentConfirmationView" must {

    val view = viewFor[AgentConfirmationView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(
      tvn = fakeTvn,
      leadTrustee = leadTrusteeName
    )(fakeRequest, messages)

    behave like normalPage(
      view = applyView,
      messageKeyPrefix = messageKeyPrefix,
      expectedGuidanceKeys = "subheading1", "paragraph1", "paragraph2", "paragraph3", "paragraph4", "subheading2",
      "paragraph6", "subheading3", "paragraph7", "paragraph8", "paragraph9", "paragraph10"
    )

    behave like pageWithWarning(applyView)

    "render content" in {
      val doc = asDocument(applyView)

      assertContainsText(doc, s"Declaration received")
      assertContainsText(doc, s"Your reference is:")
      assertContainsText(doc, s"$fakeTvn")
      assertContainsText(doc, "Print or save a copy of your answers")

      val agentOverviewLink = doc.getElementById("agent-overview")
      assertAttributeValueForElement(agentOverviewLink, "href", frontendAppConfig.agentOverviewUrl)
      assertContainsTextForId(doc, "agent-overview", messages("agentConfirmation.paragraph10.link"))

      assertContainsText(doc, messages(s"$messageKeyPrefix.paragraph5", leadTrusteeName))

    }
  }

}
