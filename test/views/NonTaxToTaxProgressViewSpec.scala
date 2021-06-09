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

import models.URN
import sections.assets.Assets
import sections.{TaxLiability, TrustDetails}
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import viewmodels.{Link, Task}
import views.behaviours.{TransitionsProgressViewBehaviours, ViewBehaviours}
import views.html.NonTaxToTaxProgressView

class NonTaxToTaxProgressViewSpec extends ViewBehaviours with TransitionsProgressViewBehaviours {

  private val urn = "urn"

  private val mandatorySections = List(
    Task(Link(TrustDetails, ""), None),
    Task(Link(Assets, ""), None),
    Task(Link(TaxLiability, ""), None)
  )

  private val expectedContinueUrl: String = controllers.declaration.routes.IndividualDeclarationController.onPageLoad().url

  "TransitionProgressView" when {

    "not all sections completed" when {

      "agent user" must {

        val group = Agent

        val userAnswers = emptyUserAnswersForUtr

        val view = viewFor[NonTaxToTaxProgressView](Some(userAnswers))

        val applyView = view.apply(
          identifier = urn,
          identifierType = URN,
          mandatory = mandatorySections,
          additional = Nil,
          affinityGroup = group,
          nextUrl = expectedContinueUrl,
          isAbleToDeclare = false
        )(fakeRequest, messages)

        behave like normalPageTitleWithCaption(
          view = applyView,
          messageKeyPrefix = "transitionProgress",
          captionKey = "urn",
          captionParam = urn,
          expectedGuidanceKeys = "p1", "subHeading.1", "p2", "p2.bullet1", "p2.bullet2", "p3", "p4"
        )

        behave like pageWithBackLink(applyView)

        behave like taskListHeading(applyView)

        behave like taskList(applyView, mandatorySections)

        behave like pageWithWarning(applyView)

        val doc = asDocument(applyView)

        "render agent overview link" in {
          assertContainsText(doc, messages("transitionsProgress.return.link"))
        }
      }

      "non-agent user" must {

        val group = Organisation

        val userAnswers = emptyUserAnswersForUtr

        val view = viewFor[NonTaxToTaxProgressView](Some(userAnswers))

        val applyView = view.apply(
          identifier = urn,
          identifierType = URN,
          mandatory = mandatorySections,
          additional = Nil,
          affinityGroup = group,
          nextUrl = expectedContinueUrl,
          isAbleToDeclare = false
        )(fakeRequest, messages)

        behave like normalPageTitleWithCaption(
          view = applyView,
          messageKeyPrefix = "transitionProgress",
          captionKey = "urn",
          captionParam = urn,
          expectedGuidanceKeys = "p1", "subHeading.1", "p2", "p2.bullet1", "p2.bullet2", "p3", "p4"
        )

        behave like pageWithBackLink(applyView)

        behave like taskListHeading(applyView)

        behave like taskList(applyView, mandatorySections)

        behave like pageWithWarning(applyView)
      }
    }

    "all sections completed" when {

      "agent user" must {

        val group = Agent

        val userAnswers = emptyUserAnswersForUtr

        val view = viewFor[NonTaxToTaxProgressView](Some(userAnswers))

        val applyView = view.apply(
          identifier = urn,
          identifierType = URN,
          mandatory = mandatorySections,
          additional = Nil,
          affinityGroup = group,
          nextUrl = expectedContinueUrl,
          isAbleToDeclare = true
        )(fakeRequest, messages)

        behave like normalPageTitleWithCaption(
          view = applyView,
          messageKeyPrefix = "transitionProgress",
          captionKey = "urn",
          captionParam = urn,
          expectedGuidanceKeys = "p1", "subHeading.1", "p2", "p2.bullet1", "p2.bullet2", "p3", "p4", "p5",
          "p5.bullet1", "p5.bullet2", "p6", "p7", "p7.bullet1", "p7.bullet2", "p8"
        )

        behave like pageWithBackLink(applyView)

        behave like taskListHeading(applyView)

        behave like taskList(applyView, mandatorySections)

        behave like pageWithWarning(applyView)

        behave like pageWithContinueButton(applyView, expectedContinueUrl, Some("taskList.summary.continue"))

        val doc = asDocument(applyView)

        "render agent overview link" in {
          assertContainsText(doc, messages("transitionsProgress.return.link"))
        }
      }

      "non-agent user" must {

        val group = Organisation

        val userAnswers = emptyUserAnswersForUtr

        val view = viewFor[NonTaxToTaxProgressView](Some(userAnswers))

        val applyView = view.apply(
          identifier = urn,
          identifierType = URN,
          mandatory = mandatorySections,
          additional = Nil,
          affinityGroup = group,
          nextUrl = expectedContinueUrl,
          isAbleToDeclare = true
        )(fakeRequest, messages)

        behave like normalPageTitleWithCaption(
          view = applyView,
          messageKeyPrefix = "transitionProgress",
          captionKey = "urn",
          captionParam = urn,
          expectedGuidanceKeys = "p1", "subHeading.1", "p2", "p2.bullet1", "p2.bullet2", "p3", "p4", "p5",
          "p5.bullet1", "p5.bullet2", "p6", "p7", "p7.bullet1", "p7.bullet2", "p8"
        )

        behave like pageWithBackLink(applyView)

        behave like taskListHeading(applyView)

        behave like taskList(applyView, mandatorySections)

        behave like pageWithWarning(applyView)

        behave like pageWithContinueButton(applyView, expectedContinueUrl, Some("taskList.summary.continue"))
      }
    }
  }
}
