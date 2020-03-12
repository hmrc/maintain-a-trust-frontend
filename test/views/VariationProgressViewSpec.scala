/*
 * Copyright 2020 HM Revenue & Customs
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

import Sections.{BeneficiariesVariationDetails, NaturalPeople, SettlorsVariationDetails, TrusteeVariationDetails}
import pages.UTRPage
import views.behaviours.{VariationsProgressViewBehaviours, ViewBehaviours}
import views.html.VariationProgressView
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import viewmodels.{Link, Task}

class VariationProgressViewSpec extends ViewBehaviours with VariationsProgressViewBehaviours {

  "VariationProgress view" must {

    val utr = "1234545678"

    val mandatorySections = List(
      Task(Link(SettlorsVariationDetails, ""), None),
      Task(Link(TrusteeVariationDetails, ""), None),
      Task(Link(BeneficiariesVariationDetails, ""), None)
    )
    val optionalSections = List(
      Task(Link(NaturalPeople, ""),None))

    val group = Organisation

    val userAnswers = emptyUserAnswers
      .set(UTRPage, utr).success.value

    val view = viewFor[VariationProgressView](Some(userAnswers))

    val applyView = view.apply(utr, mandatorySections, optionalSections, group)(fakeRequest, messages)

    "Have a dynamic utr in the subheading" in {
      val doc = asDocument(applyView)
      assertContainsText(doc, s"This trust’s UTR: $utr")
    }

    behave like normalPage(applyView, "variationProgress")

    behave like pageWithBackLink(applyView)

    behave like taskList(applyView)

  }
}
