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

import models.UTR
import models.pages.Tag.Completed
import sections.beneficiaries.Beneficiaries
import sections.settlors.Settlors
import sections.{Natural, Protectors, TrustDetails, Trustees}
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import viewmodels.{Link, Task}
import views.behaviours.{ProgressViewBehaviours, ViewBehaviours}
import views.html.VariationProgressView

class VariationProgressViewSpec extends ViewBehaviours with ProgressViewBehaviours {

  private val utr = "utr"

  private val mandatorySections = List(
    Task(Link(TrustDetails, "#"), Completed),
    Task(Link(Settlors, "#"), Completed),
    Task(Link(Trustees, "#"), Completed),
    Task(Link(Beneficiaries, "#"), Completed)
  )

  private val optionalSections = List(
    Task(Link(Natural, "#"), Completed),
    Task(Link(Protectors, "#"), Completed)
  )

  "VariationProgressView" when {

    "not all sections completed" when {

      "agent user" must {

        val group = Agent

        val userAnswers = emptyUserAnswersForUtr

        val view = viewFor[VariationProgressView](Some(userAnswers))

        val applyView = view.apply(
          identifier = utr,
          identifierType = UTR,
          mandatory = mandatorySections,
          optional = optionalSections,
          affinityGroup = group,
          isAbleToDeclare = false,
          closingTrust = false
        )(fakeRequest, messages)

        behave like normalPageTitleWithCaption(
          view = applyView,
          messageKeyPrefix = "variationProgress",
          captionKey = "utr",
          captionParam = utr,
          expectedGuidanceKeys = "p1", "p2", "subHeading.2", "subHeading.3"
        )

        behave like pageWithBackLink(applyView)

        behave like taskListHeading(applyView)

        behave like taskList(applyView, mandatorySections)
        behave like taskList(applyView, optionalSections)

        behave like pageWithWarning(applyView)

        val doc = asDocument(applyView)

        "render agent overview link" in {
          assertContainsText(doc, messages("variationsProgress.return.link"))
        }
      }

      "non-agent user" must {

        val group = Organisation

        val userAnswers = emptyUserAnswersForUtr

        val view = viewFor[VariationProgressView](Some(userAnswers))

        val applyView = view.apply(
          identifier = utr,
          identifierType = UTR,
          mandatory = mandatorySections,
          optional = optionalSections,
          affinityGroup = group,
          isAbleToDeclare = false,
          closingTrust = false
        )(fakeRequest, messages)

        behave like normalPageTitleWithCaption(
          view = applyView,
          messageKeyPrefix = "variationProgress",
          captionKey = "utr",
          captionParam = utr,
          expectedGuidanceKeys = "p1", "p2", "subHeading.2", "subHeading.3"
        )

        behave like pageWithBackLink(applyView)

        behave like taskListHeading(applyView)

        behave like taskList(applyView, mandatorySections)
        behave like taskList(applyView, optionalSections)

        behave like pageWithWarning(applyView)
      }
    }

    "all sections completed" when {

      "agent user" must {

        val group = Agent

        val userAnswers = emptyUserAnswersForUtr

        val view = viewFor[VariationProgressView](Some(userAnswers))

        val applyView = view.apply(
          identifier = utr,
          identifierType = UTR,
          mandatory = mandatorySections,
          optional = optionalSections,
          affinityGroup = group,
          isAbleToDeclare = true,
          closingTrust = false
        )(fakeRequest, messages)

        behave like normalPageTitleWithCaption(
          view = applyView,
          messageKeyPrefix = "variationProgress",
          captionKey = "utr",
          captionParam = utr,
          expectedGuidanceKeys = "p1", "p2", "subHeading.2", "subHeading.3", "subHeading.4", "p3", "p4", "subHeading.5", "p5", "sa900.link"
        )

        behave like pageWithBackLink(applyView)

        behave like taskListHeading(applyView)

        behave like taskList(applyView, mandatorySections)
        behave like taskList(applyView, optionalSections)

        behave like pageWithWarning(applyView)

        behave like pageWithContinueButton(applyView, Some("taskList.summary.continue"))

        val doc = asDocument(applyView)

        "render agent overview link" in {
          assertContainsText(doc, messages("variationsProgress.return.link"))
        }

        "render print link" in {
          assertRenderedById(doc, "print-and-save")
        }
      }

      "non-agent user" must {

        val group = Organisation

        val userAnswers = emptyUserAnswersForUtr

        val view = viewFor[VariationProgressView](Some(userAnswers))

        val applyView = view.apply(
          identifier = utr,
          identifierType = UTR,
          mandatory = mandatorySections,
          optional = optionalSections,
          affinityGroup = group,
          isAbleToDeclare = true,
          closingTrust = false
        )(fakeRequest, messages)

        behave like normalPageTitleWithCaption(
          view = applyView,
          messageKeyPrefix = "variationProgress",
          captionKey = "utr",
          captionParam = utr,
          expectedGuidanceKeys = "p1", "p2", "subHeading.2", "subHeading.3", "subHeading.4", "p3", "p4", "subHeading.5", "p5", "sa900.link"
        )

        behave like pageWithBackLink(applyView)

        behave like taskListHeading(applyView)

        behave like taskList(applyView, mandatorySections)
        behave like taskList(applyView, optionalSections)

        behave like pageWithWarning(applyView)

        behave like pageWithContinueButton(applyView, Some("taskList.summary.continue"))

        val doc = asDocument(applyView)

        "render print link" in {
          assertRenderedById(doc, "print-and-save")
        }
      }
    }
  }
}
