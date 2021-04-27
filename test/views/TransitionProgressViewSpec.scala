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

import models.pages.Tag.UpToDate
import models.{URN, UTR}
import sections.assets.Assets
import sections.{TrustDetails, TaxLiability}
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import viewmodels.{Link, Task}
import views.behaviours.{TransitionsProgressViewBehaviours, ViewBehaviours}
import views.html.TransitionProgressView

class TransitionProgressViewSpec extends ViewBehaviours with TransitionsProgressViewBehaviours {

  val expectedContinueUrl: String = controllers.declaration.routes.IndividualDeclarationController.onPageLoad().url

  "TransitionProgress view" must {

    val urn = "urn"

    val mandatorySections = List(
      Task(Link(TrustDetails, ""), None),
      Task(Link(Assets, ""), None),
      Task(Link(TaxLiability, ""), None)
    )

    val group = Organisation

    val userAnswers = emptyUserAnswersForUtr

    val view = viewFor[TransitionProgressView](Some(userAnswers))

    val applyView = view.apply(urn, URN, mandatorySections, group, expectedContinueUrl, isAbleToDeclare = false, closingTrust = false)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(applyView,
      "transitionProgress",
      "urn",
      urn,
      "p1", "p2")

    behave like pageWithBackLink(applyView)

    behave like taskListHeading(applyView)

    behave like taskList(applyView, mandatorySections)

  }

  "render summary" when {

    "all sections are completed" in {

        val urn = "urn"

        val mandatorySections = List(
          Task(Link(TrustDetails, "http://localhost:9838/maintain-a-trust/trust-details/urn"), Some(UpToDate)),
          Task(Link(Assets, "http://localhost:9800/maintain-a-trust/trust-assets/urn"), Some(UpToDate)),
          Task(Link(TaxLiability, "http://localhost:9838/maintain-a-trust/tax-liability/urn"), Some(UpToDate))
        )

        val group = Organisation

        val userAnswers = emptyUserAnswersForUrn

        val view = viewFor[TransitionProgressView](Some(userAnswers))

        val applyView = view.apply(urn, UTR, mandatorySections, group, expectedContinueUrl, isAbleToDeclare = true, closingTrust = false)(fakeRequest, messages)

        val doc = asDocument(applyView)

        assertRenderedById(doc, "summary-heading")
        assertRenderedById(doc, "summary-paragraph")
        assertRenderedById(doc, "summary-heading-2")
        assertRenderedById(doc, "summary-paragraph-2")
        assertRenderedById(doc, "print-and-save")

      }
    }

}
