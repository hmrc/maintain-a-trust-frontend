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
import base.SpecBase
import models.MigrationTaskStatus.{NeedsUpdating, NothingToUpdate, Updated}
import models.pages.Tag._
import models.pages.WhatIsNext
import models.{CompletedMaintenanceTasks, Underlying4mldTrustIn4mldMode, Underlying4mldTrustIn5mldMode, Underlying5mldNonTaxableTrustIn5mldMode, Underlying5mldTaxableTrustIn5mldMode}
import sections._
import sections.assets.Assets
import sections.beneficiaries.Beneficiaries
import sections.settlors.Settlors
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

            val result = variationProgress.generateTaskList(tasks, Underlying4mldTrustIn4mldMode, identifier)

            result.mandatory mustBe List(
              Task(Link(Settlors, s"http://localhost:9795/maintain-a-trust/settlors/$identifier"), NotStarted),
              Task(Link(Trustees, s"http://localhost:9792/maintain-a-trust/trustees/$identifier"), NotStarted),
              Task(Link(Beneficiaries, s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier"), NotStarted)
            )

            result.other mustBe List(
              Task(Link(Protectors, s"http://localhost:9796/maintain-a-trust/protectors/$identifier"), NotStarted),
              Task(Link(OtherIndividuals, s"http://localhost:9799/maintain-a-trust/other-individuals/$identifier"), NotStarted)
            )
          }

          "tasks in progress" in {
            val tasks = CompletedMaintenanceTasks(
              trustDetails = NotStarted,
              assets = NotStarted,
              taxLiability = NotStarted,
              trustees = InProgress,
              beneficiaries = InProgress,
              settlors = InProgress,
              protectors = InProgress,
              other = InProgress
            )

            val userAnswers = emptyUserAnswersForUtr
            val identifier = userAnswers.identifier

            val result = variationProgress.generateTaskList(tasks, Underlying4mldTrustIn4mldMode, identifier)

            result.mandatory mustBe List(
              Task(Link(Settlors, s"http://localhost:9795/maintain-a-trust/settlors/$identifier"), InProgress),
              Task(Link(Trustees, s"http://localhost:9792/maintain-a-trust/trustees/$identifier"), InProgress),
              Task(Link(Beneficiaries, s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier"), InProgress)
            )

            result.other mustBe List(
              Task(Link(Protectors, s"http://localhost:9796/maintain-a-trust/protectors/$identifier"), InProgress),
              Task(Link(OtherIndividuals, s"http://localhost:9799/maintain-a-trust/other-individuals/$identifier"), InProgress)
            )
          }

          "tasks completed" in {
            val tasks = CompletedMaintenanceTasks(
              trustDetails = NotStarted,
              assets = NotStarted,
              taxLiability = NotStarted,
              trustees = Completed,
              beneficiaries = Completed,
              settlors = Completed,
              protectors = Completed,
              other = Completed
            )

            val userAnswers = emptyUserAnswersForUtr
            val identifier = userAnswers.identifier

            val result = variationProgress.generateTaskList(tasks, Underlying4mldTrustIn4mldMode, identifier)

            result.mandatory mustBe List(
              Task(Link(Settlors, s"http://localhost:9795/maintain-a-trust/settlors/$identifier"), Completed),
              Task(Link(Trustees, s"http://localhost:9792/maintain-a-trust/trustees/$identifier"), Completed),
              Task(Link(Beneficiaries, s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier"), Completed)
            )

            result.other mustBe List(
              Task(Link(Protectors, s"http://localhost:9796/maintain-a-trust/protectors/$identifier"), Completed),
              Task(Link(OtherIndividuals, s"http://localhost:9799/maintain-a-trust/other-individuals/$identifier"), Completed)
            )
          }
        }

        "in 5mld mode" when {

          "underlying trust data is 4mld" in {
            val tasks = CompletedMaintenanceTasks()

            val userAnswers = emptyUserAnswersForUtr
            val identifier = userAnswers.identifier

            val result = variationProgress.generateTaskList(tasks, Underlying4mldTrustIn5mldMode, identifier)

            result.mandatory mustBe List(
              Task(Link(Settlors, s"http://localhost:9795/maintain-a-trust/settlors/$identifier"), NotStarted),
              Task(Link(Trustees, s"http://localhost:9792/maintain-a-trust/trustees/$identifier"), NotStarted),
              Task(Link(Beneficiaries, s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier"), NotStarted)
            )

            result.other mustBe List(
              Task(Link(Protectors, s"http://localhost:9796/maintain-a-trust/protectors/$identifier"), NotStarted),
              Task(Link(OtherIndividuals, s"http://localhost:9799/maintain-a-trust/other-individuals/$identifier"), NotStarted)
            )
          }

          "underlying trust data is 5mld" when {

            "trust is taxable" in {
              val tasks = CompletedMaintenanceTasks()

              val userAnswers = emptyUserAnswersForUtr
              val identifier = userAnswers.identifier

              val result = variationProgress.generateTaskList(tasks, Underlying5mldTaxableTrustIn5mldMode, identifier)

              result.mandatory mustBe List(
                Task(Link(TrustDetails, s"http://localhost:9838/maintain-a-trust/trust-details/$identifier"), NotStarted),
                Task(Link(Settlors, s"http://localhost:9795/maintain-a-trust/settlors/$identifier"), NotStarted),
                Task(Link(Trustees, s"http://localhost:9792/maintain-a-trust/trustees/$identifier"), NotStarted),
                Task(Link(Beneficiaries, s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier"), NotStarted)
              )

              result.other mustBe List(
                Task(Link(CompanyOwnershipOrControllingInterest, s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier"), NotStarted),
                Task(Link(Protectors, s"http://localhost:9796/maintain-a-trust/protectors/$identifier"), NotStarted),
                Task(Link(OtherIndividuals, s"http://localhost:9799/maintain-a-trust/other-individuals/$identifier"), NotStarted)
              )
            }

            "trust is non-taxable" in {
              val tasks = CompletedMaintenanceTasks()

              val userAnswers = emptyUserAnswersForUrn
              val identifier = userAnswers.identifier

              val result = variationProgress.generateTaskList(tasks, Underlying5mldNonTaxableTrustIn5mldMode, identifier)

              result.mandatory mustBe List(
                Task(Link(TrustDetails, s"http://localhost:9838/maintain-a-trust/trust-details/$identifier"), NotStarted),
                Task(Link(Settlors, s"http://localhost:9795/maintain-a-trust/settlors/$identifier"), NotStarted),
                Task(Link(Trustees, s"http://localhost:9792/maintain-a-trust/trustees/$identifier"), NotStarted),
                Task(Link(Beneficiaries, s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier"), NotStarted)
              )

              result.other mustBe List(
                Task(Link(CompanyOwnershipOrControllingInterest, s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier"), NotStarted),
                Task(Link(Protectors, s"http://localhost:9796/maintain-a-trust/protectors/$identifier"), NotStarted),
                Task(Link(OtherIndividuals, s"http://localhost:9799/maintain-a-trust/other-individuals/$identifier"), NotStarted)
              )
            }
          }
        }
      }
    }

    "generateTransitionTaskList" must {
      "generate task list" when {

        "trust is migrating from non-taxable to taxable" when {

          "trust details task not complete" when {

            "settlors and beneficiaries need updating" in {
              val tasks = CompletedMaintenanceTasks()

              val userAnswers = emptyUserAnswersForUrn.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value
              val identifier = userAnswers.identifier

              val result = variationProgress.generateTransitionTaskList(tasks, NeedsUpdating, NeedsUpdating, 0, identifier)

              result.mandatory mustBe List(
                Task(Link(TrustDetails, s"http://localhost:9838/maintain-a-trust/trust-details/$identifier"), NotStarted),
                Task(Link(Assets, s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier"), NotStarted),
                Task(Link(TaxLiability, s"http://localhost:9844/maintain-a-trust/tax-liability/$identifier"), NoActionNeeded)
              )

              result.other mustBe List(
                Task(Link(Settlors, s"http://localhost:9795/maintain-a-trust/settlors/$identifier"), CannotStartYet),
                Task(Link(Beneficiaries, s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier"), CannotStartYet)
              )
            }

            // needs to be CannotStartYet as we don't know for certain if it's a NoActionNeeded until we know the trust type (i.e. when trust details is completed)
            "settlors and beneficiaries do not need updating" in {
              val tasks = CompletedMaintenanceTasks()

              val userAnswers = emptyUserAnswersForUrn.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value
              val identifier = userAnswers.identifier

              val result = variationProgress.generateTransitionTaskList(tasks, NothingToUpdate, NothingToUpdate, 0, identifier)

              result.mandatory mustBe List(
                Task(Link(TrustDetails, s"http://localhost:9838/maintain-a-trust/trust-details/$identifier"), NotStarted),
                Task(Link(Assets, s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier"), NotStarted),
                Task(Link(TaxLiability, s"http://localhost:9844/maintain-a-trust/tax-liability/$identifier"), NoActionNeeded)
              )

              result.other mustBe List(
                Task(Link(Settlors, s"http://localhost:9795/maintain-a-trust/settlors/$identifier"), CannotStartYet),
                Task(Link(Beneficiaries, s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier"), CannotStartYet)
              )
            }
          }

          "trust details task complete" when {

            "settlors and beneficiaries do need updating" in {
              val tasks = CompletedMaintenanceTasks(
                trustDetails = Completed,
                assets = NotStarted,
                taxLiability = NotStarted,
                trustees = NotStarted,
                beneficiaries = NotStarted,
                settlors = NotStarted,
                protectors = NotStarted,
                other = NotStarted
              )

              val userAnswers = emptyUserAnswersForUrn.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value
              val identifier = userAnswers.identifier

              val result = variationProgress.generateTransitionTaskList(tasks, NeedsUpdating, NeedsUpdating, 0, identifier)

              result.mandatory mustBe List(
                Task(Link(TrustDetails, s"http://localhost:9838/maintain-a-trust/trust-details/$identifier"), Completed),
                Task(Link(Assets, s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier"), NotStarted),
                Task(Link(TaxLiability, s"http://localhost:9844/maintain-a-trust/tax-liability/$identifier"), NoActionNeeded)
              )

              result.other mustBe List(
                Task(Link(Settlors, s"http://localhost:9795/maintain-a-trust/settlors/$identifier"), NotStarted),
                Task(Link(Beneficiaries, s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier"), NotStarted)
              )
            }

            "settlors and beneficiaries do not need updating" in {
              val tasks = CompletedMaintenanceTasks(
                trustDetails = Completed,
                assets = NotStarted,
                taxLiability = NotStarted,
                trustees = NotStarted,
                beneficiaries = NotStarted,
                settlors = NotStarted,
                protectors = NotStarted,
                other = NotStarted
              )

              val userAnswers = emptyUserAnswersForUrn.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value
              val identifier = userAnswers.identifier

              val result = variationProgress.generateTransitionTaskList(tasks, NothingToUpdate, NothingToUpdate, 0, identifier)

              result.mandatory mustBe List(
                Task(Link(TrustDetails, s"http://localhost:9838/maintain-a-trust/trust-details/$identifier"), Completed),
                Task(Link(Assets, s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier"), NotStarted),
                Task(Link(TaxLiability, s"http://localhost:9844/maintain-a-trust/tax-liability/$identifier"), NoActionNeeded)
              )

              result.other mustBe List(
                Task(Link(Settlors, s"http://localhost:9795/maintain-a-trust/settlors/$identifier"), NoActionNeeded),
                Task(Link(Beneficiaries, s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier"), NoActionNeeded)
              )
            }

            "settlors and beneficiaries do not need updating but tasks have been completed (can happen if all entities removed)" in {
              val tasks = CompletedMaintenanceTasks(
                trustDetails = Completed,
                assets = NotStarted,
                taxLiability = NotStarted,
                trustees = NotStarted,
                beneficiaries = Completed,
                settlors = Completed,
                protectors = NotStarted,
                other = NotStarted
              )

              val userAnswers = emptyUserAnswersForUrn.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value
              val identifier = userAnswers.identifier

              val result = variationProgress.generateTransitionTaskList(tasks, NothingToUpdate, NothingToUpdate, 0, identifier)

              result.mandatory mustBe List(
                Task(Link(TrustDetails, s"http://localhost:9838/maintain-a-trust/trust-details/$identifier"), Completed),
                Task(Link(Assets, s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier"), NotStarted),
                Task(Link(TaxLiability, s"http://localhost:9844/maintain-a-trust/tax-liability/$identifier"), NoActionNeeded)
              )

              result.other mustBe List(
                Task(Link(Settlors, s"http://localhost:9795/maintain-a-trust/settlors/$identifier"), Completed),
                Task(Link(Beneficiaries, s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier"), Completed)
              )
            }

            "settlors and beneficiaries tasks have been completed yet they still need updating " +
              "(i.e. user says they're done without making the required changes first)" in {
              val tasks = CompletedMaintenanceTasks(
                trustDetails = Completed,
                assets = NotStarted,
                taxLiability = NotStarted,
                trustees = NotStarted,
                beneficiaries = Completed,
                settlors = Completed,
                protectors = NotStarted,
                other = NotStarted
              )

              val userAnswers = emptyUserAnswersForUrn.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value
              val identifier = userAnswers.identifier

              val result = variationProgress.generateTransitionTaskList(tasks, NeedsUpdating, NeedsUpdating, 0, identifier)

              result.mandatory mustBe List(
                Task(Link(TrustDetails, s"http://localhost:9838/maintain-a-trust/trust-details/$identifier"), Completed),
                Task(Link(Assets, s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier"), NotStarted),
                Task(Link(TaxLiability, s"http://localhost:9844/maintain-a-trust/tax-liability/$identifier"), NoActionNeeded)
              )

              result.other mustBe List(
                Task(Link(Settlors, s"http://localhost:9795/maintain-a-trust/settlors/$identifier"), InProgress),
                Task(Link(Beneficiaries, s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier"), InProgress)
              )
            }

            "years of tax liability to ask for" in {
              val tasks = CompletedMaintenanceTasks(
                trustDetails = Completed,
                assets = NotStarted,
                taxLiability = NotStarted,
                trustees = NotStarted,
                beneficiaries = NotStarted,
                settlors = NotStarted,
                protectors = NotStarted,
                other = NotStarted
              )

              val userAnswers = emptyUserAnswersForUrn.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value
              val identifier = userAnswers.identifier

              val result = variationProgress.generateTransitionTaskList(tasks, NothingToUpdate, NothingToUpdate, 1, identifier)

              result.mandatory mustBe List(
                Task(Link(TrustDetails, s"http://localhost:9838/maintain-a-trust/trust-details/$identifier"), Completed),
                Task(Link(Assets, s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier"), NotStarted),
                Task(Link(TaxLiability, s"http://localhost:9844/maintain-a-trust/tax-liability/$identifier"), NotStarted)
              )

              result.other mustBe List(
                Task(Link(Settlors, s"http://localhost:9795/maintain-a-trust/settlors/$identifier"), NoActionNeeded),
                Task(Link(Beneficiaries, s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier"), NoActionNeeded)
              )
            }

            "tasks not started" in {
              val tasks = CompletedMaintenanceTasks(
                trustDetails = Completed,
                assets = NotStarted,
                taxLiability = NotStarted,
                trustees = NotStarted,
                beneficiaries = NotStarted,
                settlors = NotStarted,
                protectors = NotStarted,
                other = NotStarted
              )

              val userAnswers = emptyUserAnswersForUrn.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value
              val identifier = userAnswers.identifier

              val result = variationProgress.generateTransitionTaskList(tasks, NeedsUpdating, NeedsUpdating, 1, identifier)

              result.mandatory mustBe List(
                Task(Link(TrustDetails, s"http://localhost:9838/maintain-a-trust/trust-details/$identifier"), Completed),
                Task(Link(Assets, s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier"), NotStarted),
                Task(Link(TaxLiability, s"http://localhost:9844/maintain-a-trust/tax-liability/$identifier"), NotStarted)
              )

              result.other mustBe List(
                Task(Link(Settlors, s"http://localhost:9795/maintain-a-trust/settlors/$identifier"), NotStarted),
                Task(Link(Beneficiaries, s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier"), NotStarted)
              )
            }

            "tasks in progress" in {
              val tasks = CompletedMaintenanceTasks(
                trustDetails = Completed,
                assets = InProgress,
                taxLiability = InProgress,
                trustees = NotStarted,
                beneficiaries = InProgress,
                settlors = InProgress,
                protectors = NotStarted,
                other = NotStarted
              )

              val userAnswers = emptyUserAnswersForUrn.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value
              val identifier = userAnswers.identifier

              val result = variationProgress.generateTransitionTaskList(tasks, NeedsUpdating, NeedsUpdating, 1, identifier)

              result.mandatory mustBe List(
                Task(Link(TrustDetails, s"http://localhost:9838/maintain-a-trust/trust-details/$identifier"), Completed),
                Task(Link(Assets, s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier"), InProgress),
                Task(Link(TaxLiability, s"http://localhost:9844/maintain-a-trust/tax-liability/$identifier"), InProgress)
              )

              result.other mustBe List(
                Task(Link(Settlors, s"http://localhost:9795/maintain-a-trust/settlors/$identifier"), InProgress),
                Task(Link(Beneficiaries, s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier"), InProgress)
              )
            }

            "tasks completed" in {
              val tasks = CompletedMaintenanceTasks(
                trustDetails = Completed,
                assets = Completed,
                taxLiability = Completed,
                trustees = NotStarted,
                beneficiaries = Completed,
                settlors = Completed,
                protectors = NotStarted,
                other = NotStarted
              )

              val userAnswers = emptyUserAnswersForUrn.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value
              val identifier = userAnswers.identifier

              val result = variationProgress.generateTransitionTaskList(tasks, Updated, Updated, 1, identifier)

              result.mandatory mustBe List(
                Task(Link(TrustDetails, s"http://localhost:9838/maintain-a-trust/trust-details/$identifier"), Completed),
                Task(Link(Assets, s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier"), Completed),
                Task(Link(TaxLiability, s"http://localhost:9844/maintain-a-trust/tax-liability/$identifier"), Completed)
              )

              result.other mustBe List(
                Task(Link(Settlors, s"http://localhost:9795/maintain-a-trust/settlors/$identifier"), Completed),
                Task(Link(Beneficiaries, s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier"), Completed)
              )
            }
          }
        }
      }
    }
  }

}
