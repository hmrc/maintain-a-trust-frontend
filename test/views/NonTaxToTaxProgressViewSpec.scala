/*
 * Copyright 2022 HM Revenue & Customs
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
import models.pages.Tag.{Completed, NoActionNeeded}
import sections.assets.Assets
import sections.beneficiaries.Beneficiaries
import sections.{TaxLiability, TrustDetails}
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import viewmodels.{Link, Task}
import views.behaviours.{ProgressViewBehaviours, ViewBehaviours}
import views.html.NonTaxToTaxProgressView

class NonTaxToTaxProgressViewSpec extends ViewBehaviours with ProgressViewBehaviours {

  private val urn = "urn"

  private val mandatorySections = List(
    Task(Link(TrustDetails, "#"), Completed),
    Task(Link(Assets, "#"), Completed),
    Task(Link(TaxLiability, "#"), Completed)
  )

  private val additionalSections = List(
    Task(Link(Beneficiaries, "#"), NoActionNeeded)
  )

  private val additionalSectionsCompleted = List(
    Task(Link(Beneficiaries, "#"), Completed)
  )

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
          additional = additionalSections,
          affinityGroup = group,
          isAbleToDeclare = false
        )(fakeRequest, messages)

        behave like normalPageTitleWithCaption(
          view = applyView,
          messageKeyPrefix = "transitionProgress",
          captionKey = "urn",
          captionParam = urn,
          expectedGuidanceKeys = "p1", "p2", "p3"
        )

        behave like pageWithBackLink(applyView)

        behave like taskListHeading(applyView)

        behave like taskList(applyView, mandatorySections)

        behave like taskListWithNotActiveLink(applyView, additionalSections)

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
          additional = additionalSections,
          affinityGroup = group,
          isAbleToDeclare = false
        )(fakeRequest, messages)

        behave like normalPageTitleWithCaption(
          view = applyView,
          messageKeyPrefix = "transitionProgress",
          captionKey = "urn",
          captionParam = urn,
          expectedGuidanceKeys = "p1", "p2", "p3"
        )

        behave like pageWithBackLink(applyView)

        behave like taskListHeading(applyView)

        behave like taskList(applyView, mandatorySections)

        behave like taskListWithNotActiveLink(applyView, additionalSections)

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
          additional = additionalSectionsCompleted,
          affinityGroup = group,
          isAbleToDeclare = true
        )(fakeRequest, messages)

        behave like normalPageTitleWithCaption(
          view = applyView,
          messageKeyPrefix = "transitionProgress",
          captionKey = "urn",
          captionParam = urn,
          expectedGuidanceKeys = "p1", "p4", "p4.bullet1", "p4.bullet2", "p5",
          "printsave.link", "p5b", "p6", "p6.link"
        )

        behave like pageWithBackLink(applyView)

        behave like taskListHeading(applyView)

        behave like taskList(applyView, mandatorySections)

        behave like taskList(applyView, additionalSectionsCompleted)

        behave like pageWithWarning(applyView)

        behave like pageWithContinueButton(applyView, Some("taskList.summary.continue"))

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
          additional = additionalSectionsCompleted,
          affinityGroup = group,
          isAbleToDeclare = true
        )(fakeRequest, messages)

        behave like normalPageTitleWithCaption(
          view = applyView,
          messageKeyPrefix = "transitionProgress",
          captionKey = "urn",
          captionParam = urn,
          expectedGuidanceKeys = "p1", "p4", "p4.bullet1", "p4.bullet2", "p5",
          "printsave.link", "p5b", "p6", "p6.link"
        )

        behave like pageWithBackLink(applyView)

        behave like taskListHeading(applyView)

        behave like taskList(applyView, mandatorySections)

        behave like taskList(applyView, additionalSectionsCompleted)

        behave like pageWithWarning(applyView)

        behave like pageWithContinueButton(applyView, Some("taskList.summary.continue"))
      }
    }
  }
}
