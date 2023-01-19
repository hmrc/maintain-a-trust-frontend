/*
 * Copyright 2023 HM Revenue & Customs
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

import config.FrontendAppConfig
import models.MigrationTaskStatus.{NeedsUpdating, NothingToUpdate, Updated}
import models.pages.Tag
import models.pages.Tag.{CannotStartYet, Completed, InProgress, NoActionNeeded}
import models.{CompletedMaintenanceTasks, MigrationTaskStatus, TaskList, TrustMldStatus}
import pages.Page
import sections._
import sections.assets.Assets
import sections.beneficiaries.Beneficiaries
import sections.settlors.Settlors
import viewmodels.{Link, Task}

import javax.inject.Inject

class VariationProgress @Inject()(config: FrontendAppConfig) {

  private def trustDetailsRoute(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainTrustDetailsEnabled) {
      config.maintainTrustDetailsUrl(identifier)
    }
  }

  private def trustAssetsRoute(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainTrustAssetsEnabled) {
      config.maintainTrustAssetsUrl(identifier)
    }
  }

  private def taxLiabilityRoute(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainTaxLiabilityEnabled) {
      config.maintainTaxLiabilityUrl(identifier)
    }
  }

  private def settlorsRoute(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainSettlorsEnabled) {
      config.maintainSettlorsUrl(identifier)
    }
  }

  private def trusteesRoute(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainTrusteesEnabled) {
      config.maintainTrusteesUrl(identifier)
    }
  }

  private def beneficiariesRoute(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainBeneficiariesEnabled) {
      config.maintainBeneficiariesUrl(identifier)
    }
  }

  private def protectorsRoute(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainProtectorsEnabled) {
      config.maintainProtectorsUrl(identifier)
    }
  }

  private def otherIndividualsRoute(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainOtherIndividualsEnabled) {
      config.maintainOtherIndividualsUrl(identifier)
    }
  }

  private def companyOwnershipOrControllingInterestRoute(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainNonEeaCompaniesEnabled) {
      config.maintainNonEeaCompanyUrl(identifier)
    }
  }

  private def redirectToServiceIfEnabled(enabled: Boolean)(redirectToService: String): String = {
    if (enabled) {
      redirectToService
    } else {
      controllers.routes.FeatureNotAvailableController.onPageLoad().url
    }
  }

  def generateTaskList(tasks: CompletedMaintenanceTasks,
                       trustMldStatus: TrustMldStatus,
                       identifier: String): TaskList = {

    def filter5mldSections(task: Task, section: Page): Boolean = {
      task.link.text == section.toString && !trustMldStatus.is5mldTrustIn5mldMode
    }

    val mandatoryTasks = List(
      Task(
        Link(TrustDetails, trustDetailsRoute(identifier)),
        Tag.tagFor(tasks.trustDetails)
      ),
      Task(
        Link(Settlors, settlorsRoute(identifier)),
        Tag.tagFor(tasks.settlors)
      ),
      Task(
        Link(Trustees, trusteesRoute(identifier)),
        Tag.tagFor(tasks.trustees)
      ),
      Task(
        Link(Beneficiaries, beneficiariesRoute(identifier)),
        Tag.tagFor(tasks.beneficiaries)
      )
    ).filterNot(filter5mldSections(_, TrustDetails))

    val optionalTasks = List(
      Task(
        Link(CompanyOwnershipOrControllingInterest, companyOwnershipOrControllingInterestRoute(identifier)),
        Tag.tagFor(tasks.assets)
      ),
      Task(
        Link(Protectors, protectorsRoute(identifier)),
        Tag.tagFor(tasks.protectors)
      ),
      Task(
        Link(OtherIndividuals, otherIndividualsRoute(identifier)),
        Tag.tagFor(tasks.other)
      )
    ).filterNot(filter5mldSections(_, CompanyOwnershipOrControllingInterest))

    TaskList(mandatoryTasks, optionalTasks)
  }

  def generateTransitionTaskList(tasks: CompletedMaintenanceTasks,
                                 settlorsStatus: MigrationTaskStatus,
                                 beneficiariesStatus: MigrationTaskStatus,
                                 yearsToAskFor: Int,
                                 identifier: String): TaskList = {

    def task(migrationTaskStatus: MigrationTaskStatus,
             savedTaskStatus: Tag,
             trustDetailsCompleted: Boolean,
             link: Link): Task = {
      val tag: Tag = if (trustDetailsCompleted) {
        migrationTaskStatus match {
          case Updated | NothingToUpdate => if (savedTaskStatus.isCompleted) Completed else NoActionNeeded
          case NeedsUpdating => if (savedTaskStatus.isCompleted) InProgress else savedTaskStatus
          case _ => CannotStartYet
        }
      } else {
        CannotStartYet
      }

      Task(link, tag)
    }

    val transitionTasks = List(
      Task(
        Link(TrustDetails, trustDetailsRoute(identifier)),
        Tag.tagFor(tasks.trustDetails)
      ),
      Task(
        Link(Assets, trustAssetsRoute(identifier)),
        Tag.tagFor(tasks.assets)
      )
    )

    lazy val taxLiabilityTask = Task(
      Link(TaxLiability, taxLiabilityRoute(identifier)),
      Tag.tagFor(if (yearsToAskFor == 0) NoActionNeeded else tasks.taxLiability)
    )

    val settlorsTask = task(
      migrationTaskStatus = settlorsStatus,
      savedTaskStatus = tasks.settlors,
      trustDetailsCompleted = tasks.trustDetails.isCompleted,
      link = Link(Settlors, settlorsRoute(identifier))
    )

    val beneficiariesTask = task(
      migrationTaskStatus = beneficiariesStatus,
      savedTaskStatus = tasks.beneficiaries,
      trustDetailsCompleted = tasks.trustDetails.isCompleted,
      link = Link(Beneficiaries, beneficiariesRoute(identifier))
    )

    TaskList(
      mandatory = transitionTasks :+ taxLiabilityTask,
      other = settlorsTask :: beneficiariesTask :: Nil
    )
  }

}
