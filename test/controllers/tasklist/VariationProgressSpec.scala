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

package controllers.tasklist

import _root_.pages.WhatIsNextPage
import _root_.pages.tasks._
import base.SpecBase
import models.{CompletedMaintenanceTasks, Underlying4mldTrustIn4mldMode, Underlying4mldTrustIn5mldMode, Underlying5mldNonTaxableTrustIn5mldMode, Underlying5mldTaxableTrustIn5mldMode}
import models.MigrationTaskStatus.{NeedsUpdating, NothingToUpdate, Updated}
import models.pages.Tag._
import models.pages.WhatIsNext
import sections.assets.{Assets, NonEeaBusinessAsset}
import sections.beneficiaries.Beneficiaries
import sections.settlors.Settlors
import sections.{Natural, Protectors, TaxLiability, TrustDetails, Trustees}
import viewmodels.{Link, Task}

class VariationProgressSpec extends SpecBase {

  private val variationProgress = injector.instanceOf[VariationProgress]

  "VariationProgress" when {

    "generateTaskList" must {
      "generate task list" when {

        "in 4mld mode" when {

          "tasks not started" in {
            val tasks = CompletedMaintenanceTasks()

            val userAnswers = emptyUserAnswersForUtr

            val identifier = userAnswers.identifier
            val result = variationProgress.generateTaskList(tasks, identifier, Underlying4mldTrustIn4mldMode, userAnswers)

            result.mandatory mustBe List(
              Task(Link(Settlors, Some(s"http://localhost:9795/maintain-a-trust/settlors/$identifier")), NotStarted),
              Task(Link(Trustees, Some(s"http://localhost:9792/maintain-a-trust/trustees/$identifier")), NotStarted),
              Task(Link(Beneficiaries, Some(s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier")), NotStarted)
            )

            result.other mustBe List(
              Task(Link(Protectors, Some(s"http://localhost:9796/maintain-a-trust/protectors/$identifier")), NotStarted),
              Task(Link(Natural, Some(s"http://localhost:9799/maintain-a-trust/other-individuals/$identifier")), NotStarted)
            )
          }

          "tasks in progress" in {
            val tasks = CompletedMaintenanceTasks()

            val userAnswers = emptyUserAnswersForUtr
              .set(SettlorsTaskStartedPage, true).success.value
              .set(TrusteesTaskStartedPage, true).success.value
              .set(BeneficiariesTaskStartedPage, true).success.value
              .set(ProtectorsTaskStartedPage, true).success.value
              .set(OtherIndividualsTaskStartedPage, true).success.value

            val identifier = userAnswers.identifier
            val result = variationProgress.generateTaskList(tasks, identifier, Underlying4mldTrustIn4mldMode, userAnswers)

            result.mandatory mustBe List(
              Task(Link(Settlors, Some(s"http://localhost:9795/maintain-a-trust/settlors/$identifier")), InProgress),
              Task(Link(Trustees, Some(s"http://localhost:9792/maintain-a-trust/trustees/$identifier")), InProgress),
              Task(Link(Beneficiaries, Some(s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier")), InProgress)
            )

            result.other mustBe List(
              Task(Link(Protectors, Some(s"http://localhost:9796/maintain-a-trust/protectors/$identifier")), InProgress),
              Task(Link(Natural, Some(s"http://localhost:9799/maintain-a-trust/other-individuals/$identifier")), InProgress)
            )
          }

          "tasks completed" in {
            val tasks = CompletedMaintenanceTasks(
              trustDetails = false,
              assets = false,
              taxLiability = false,
              trustees = true,
              beneficiaries = true,
              settlors = true,
              protectors = true,
              other = true
            )

            val userAnswers = emptyUserAnswersForUtr
              .set(SettlorsTaskStartedPage, true).success.value
              .set(TrusteesTaskStartedPage, true).success.value
              .set(BeneficiariesTaskStartedPage, true).success.value
              .set(ProtectorsTaskStartedPage, true).success.value
              .set(OtherIndividualsTaskStartedPage, true).success.value

            val identifier = userAnswers.identifier
            val result = variationProgress.generateTaskList(tasks, identifier, Underlying4mldTrustIn4mldMode, userAnswers)

            result.mandatory mustBe List(
              Task(Link(Settlors, Some(s"http://localhost:9795/maintain-a-trust/settlors/$identifier")), Completed),
              Task(Link(Trustees, Some(s"http://localhost:9792/maintain-a-trust/trustees/$identifier")), Completed),
              Task(Link(Beneficiaries, Some(s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier")), Completed)
            )

            result.other mustBe List(
              Task(Link(Protectors, Some(s"http://localhost:9796/maintain-a-trust/protectors/$identifier")), Completed),
              Task(Link(Natural, Some(s"http://localhost:9799/maintain-a-trust/other-individuals/$identifier")), Completed)
            )
          }
        }

        "in 5mld mode" when {

          "underlying trust data is 4mld" in {
            val tasks = CompletedMaintenanceTasks()

            val userAnswers = emptyUserAnswersForUtr

            val identifier = userAnswers.identifier
            val result = variationProgress.generateTaskList(tasks, identifier, Underlying4mldTrustIn5mldMode, userAnswers)

            result.mandatory mustBe List(
              Task(Link(Settlors, Some(s"http://localhost:9795/maintain-a-trust/settlors/$identifier")), NotStarted),
              Task(Link(Trustees, Some(s"http://localhost:9792/maintain-a-trust/trustees/$identifier")), NotStarted),
              Task(Link(Beneficiaries, Some(s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier")), NotStarted)
            )

            result.other mustBe List(
              Task(Link(Protectors, Some(s"http://localhost:9796/maintain-a-trust/protectors/$identifier")), NotStarted),
              Task(Link(Natural, Some(s"http://localhost:9799/maintain-a-trust/other-individuals/$identifier")), NotStarted)
            )
          }

          "underlying trust data is 5mld" when {

            "trust is taxable" in {
              val tasks = CompletedMaintenanceTasks()

              val userAnswers = emptyUserAnswersForUtr

              val identifier = userAnswers.identifier
              val result = variationProgress.generateTaskList(tasks, identifier, Underlying5mldTaxableTrustIn5mldMode, userAnswers)

              result.mandatory mustBe List(
                Task(Link(TrustDetails, Some(s"http://localhost:9838/maintain-a-trust/trust-details/$identifier")), NotStarted),
                Task(Link(Settlors, Some(s"http://localhost:9795/maintain-a-trust/settlors/$identifier")), NotStarted),
                Task(Link(Trustees, Some(s"http://localhost:9792/maintain-a-trust/trustees/$identifier")), NotStarted),
                Task(Link(Beneficiaries, Some(s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier")), NotStarted)
              )

              result.other mustBe List(
                Task(Link(NonEeaBusinessAsset, Some(s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier")), NotStarted),
                Task(Link(Protectors, Some(s"http://localhost:9796/maintain-a-trust/protectors/$identifier")), NotStarted),
                Task(Link(Natural, Some(s"http://localhost:9799/maintain-a-trust/other-individuals/$identifier")), NotStarted)
              )
            }

            "trust is non-taxable" in {
              val tasks = CompletedMaintenanceTasks()

              val userAnswers = emptyUserAnswersForUrn

              val identifier = userAnswers.identifier
              val result = variationProgress.generateTaskList(tasks, identifier, Underlying5mldNonTaxableTrustIn5mldMode, userAnswers)

              result.mandatory mustBe List(
                Task(Link(TrustDetails, Some(s"http://localhost:9838/maintain-a-trust/trust-details/$identifier")), NotStarted),
                Task(Link(Settlors, Some(s"http://localhost:9795/maintain-a-trust/settlors/$identifier")), NotStarted),
                Task(Link(Trustees, Some(s"http://localhost:9792/maintain-a-trust/trustees/$identifier")), NotStarted),
                Task(Link(Beneficiaries, Some(s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier")), NotStarted)
              )

              result.other mustBe List(
                Task(Link(NonEeaBusinessAsset, Some(s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier")), NotStarted),
                Task(Link(Protectors, Some(s"http://localhost:9796/maintain-a-trust/protectors/$identifier")), NotStarted),
                Task(Link(Natural, Some(s"http://localhost:9799/maintain-a-trust/other-individuals/$identifier")), NotStarted)
              )
            }
          }
        }
      }
    }

    "generateTransitionTaskList" must {
      "generate task list" when {

        "trust is migrating from non-taxable to taxable" when {

          "trust details task not complete" in {
            val tasks = CompletedMaintenanceTasks()

            val userAnswers = emptyUserAnswersForUrn.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value

            val identifier = userAnswers.identifier
            val result = variationProgress.generateTransitionTaskList(tasks, identifier, NeedsUpdating, NeedsUpdating, 0, userAnswers)

            result.mandatory mustBe List(
              Task(Link(TrustDetails, Some(s"http://localhost:9838/maintain-a-trust/trust-details/$identifier")), NotStarted),
              Task(Link(Assets, Some(s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier")), NotStarted)
            )

            result.other mustBe List(
              Task(Link(Settlors, None), CannotStartYet),
              Task(Link(Beneficiaries, None), CannotStartYet)
            )
          }

          "trust details task complete" when {

            "settlors and beneficiaries do need updating" in {
              val tasks = CompletedMaintenanceTasks(
                trustDetails = true,
                assets = false,
                taxLiability = false,
                trustees = false,
                beneficiaries = false,
                settlors = false,
                protectors = false,
                other = false
              )

              val userAnswers = emptyUserAnswersForUrn.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value

              val identifier = userAnswers.identifier
              val result = variationProgress.generateTransitionTaskList(tasks, identifier, NeedsUpdating, NeedsUpdating, 0, userAnswers)

              result.mandatory mustBe List(
                Task(Link(TrustDetails, Some(s"http://localhost:9838/maintain-a-trust/trust-details/$identifier")), Completed),
                Task(Link(Assets, Some(s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier")), NotStarted)
              )

              result.other mustBe List(
                Task(Link(Settlors, Some(s"http://localhost:9795/maintain-a-trust/settlors/$identifier")), NotStarted),
                Task(Link(Beneficiaries, Some(s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier")), NotStarted)
              )
            }

            "settlors and beneficiaries do not need updating" in {
              val tasks = CompletedMaintenanceTasks(
                trustDetails = true,
                assets = false,
                taxLiability = false,
                trustees = false,
                beneficiaries = false,
                settlors = false,
                protectors = false,
                other = false
              )

              val userAnswers = emptyUserAnswersForUrn.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value

              val identifier = userAnswers.identifier
              val result = variationProgress.generateTransitionTaskList(tasks, identifier, NothingToUpdate, NothingToUpdate, 0, userAnswers)

              result.mandatory mustBe List(
                Task(Link(TrustDetails, Some(s"http://localhost:9838/maintain-a-trust/trust-details/$identifier")), Completed),
                Task(Link(Assets, Some(s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier")), NotStarted)
              )

              result.other mustBe List(
                Task(Link(Settlors, Some(s"http://localhost:9795/maintain-a-trust/settlors/$identifier")), Completed),
                Task(Link(Beneficiaries, Some(s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier")), Completed)
              )
            }

            "settlors and beneficiaries do not need updating but tasks have been completed (can happen if all entities removed)" in {
              val tasks = CompletedMaintenanceTasks(
                trustDetails = true,
                assets = false,
                taxLiability = false,
                trustees = false,
                beneficiaries = true,
                settlors = true,
                protectors = false,
                other = false
              )

              val userAnswers = emptyUserAnswersForUrn.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value

              val identifier = userAnswers.identifier
              val result = variationProgress.generateTransitionTaskList(tasks, identifier, NothingToUpdate, NothingToUpdate, 0, userAnswers)

              result.mandatory mustBe List(
                Task(Link(TrustDetails, Some(s"http://localhost:9838/maintain-a-trust/trust-details/$identifier")), Completed),
                Task(Link(Assets, Some(s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier")), NotStarted)
              )

              result.other mustBe List(
                Task(Link(Settlors, Some(s"http://localhost:9795/maintain-a-trust/settlors/$identifier")), Completed),
                Task(Link(Beneficiaries, Some(s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier")), Completed)
              )
            }

            "years of tax liability to ask for" in {
              val tasks = CompletedMaintenanceTasks(
                trustDetails = true,
                assets = false,
                taxLiability = false,
                trustees = false,
                beneficiaries = false,
                settlors = false,
                protectors = false,
                other = false
              )

              val userAnswers = emptyUserAnswersForUrn.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value

              val identifier = userAnswers.identifier
              val result = variationProgress.generateTransitionTaskList(tasks, identifier, NothingToUpdate, NothingToUpdate, 1, userAnswers)

              result.mandatory mustBe List(
                Task(Link(TrustDetails, Some(s"http://localhost:9838/maintain-a-trust/trust-details/$identifier")), Completed),
                Task(Link(Assets, Some(s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier")), NotStarted),
                Task(Link(TaxLiability, Some(s"http://localhost:9844/maintain-a-trust/tax-liability/$identifier")), NotStarted)
              )

              result.other mustBe List(
                Task(Link(Settlors, Some(s"http://localhost:9795/maintain-a-trust/settlors/$identifier")), Completed),
                Task(Link(Beneficiaries, Some(s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier")), Completed)
              )
            }

            "tasks in progress" in {
              val tasks = CompletedMaintenanceTasks(
                trustDetails = true,
                assets = false,
                taxLiability = false,
                trustees = false,
                beneficiaries = false,
                settlors = false,
                protectors = false,
                other = false
              )

              val userAnswers = emptyUserAnswersForUrn.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value
                .set(TrustDetailsTaskStartedPage, true).success.value
                .set(AssetsTaskStartedPage, true).success.value
                .set(TaxLiabilityTaskStartedPage, true).success.value
                .set(SettlorsTaskStartedPage, true).success.value
                .set(BeneficiariesTaskStartedPage, true).success.value

              val identifier = userAnswers.identifier
              val result = variationProgress.generateTransitionTaskList(tasks, identifier, NeedsUpdating, NeedsUpdating, 1, userAnswers)

              result.mandatory mustBe List(
                Task(Link(TrustDetails, Some(s"http://localhost:9838/maintain-a-trust/trust-details/$identifier")), Completed),
                Task(Link(Assets, Some(s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier")), InProgress),
                Task(Link(TaxLiability, Some(s"http://localhost:9844/maintain-a-trust/tax-liability/$identifier")), InProgress)
              )

              result.other mustBe List(
                Task(Link(Settlors, Some(s"http://localhost:9795/maintain-a-trust/settlors/$identifier")), InProgress),
                Task(Link(Beneficiaries, Some(s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier")), InProgress)
              )
            }

            "tasks completed" in {
              val tasks = CompletedMaintenanceTasks(
                trustDetails = true,
                assets = true,
                taxLiability = true,
                trustees = false,
                beneficiaries = true,
                settlors = true,
                protectors = false,
                other = false
              )

              val userAnswers = emptyUserAnswersForUrn.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value
                .set(TrustDetailsTaskStartedPage, true).success.value
                .set(AssetsTaskStartedPage, true).success.value
                .set(TaxLiabilityTaskStartedPage, true).success.value
                .set(SettlorsTaskStartedPage, true).success.value
                .set(BeneficiariesTaskStartedPage, true).success.value

              val identifier = userAnswers.identifier
              val result = variationProgress.generateTransitionTaskList(tasks, identifier, Updated, Updated, 1, userAnswers)

              result.mandatory mustBe List(
                Task(Link(TrustDetails, Some(s"http://localhost:9838/maintain-a-trust/trust-details/$identifier")), Completed),
                Task(Link(Assets, Some(s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier")), Completed),
                Task(Link(TaxLiability, Some(s"http://localhost:9844/maintain-a-trust/tax-liability/$identifier")), Completed)
              )

              result.other mustBe List(
                Task(Link(Settlors, Some(s"http://localhost:9795/maintain-a-trust/settlors/$identifier")), Completed),
                Task(Link(Beneficiaries, Some(s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier")), Completed)
              )
            }
          }
        }
      }
    }
  }

}
