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

import config.FrontendAppConfig
import models.MigrationTaskStatus.{NeedsUpdating, NothingToUpdate, Updated}
import models.pages.Tag
import models.pages.Tag.{CannotStartYet, Completed, InProgress, NoActionNeeded}
import models.{CompletedMaintenanceTasks, MigrationTaskStatus, TaskList, TrustMldStatus}
import pages.Page
import sections._
import sections.assets.{Assets, NonEeaBusinessAsset}
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

  private def nonEeaCompanyRoute(identifier: String): String = {
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
        Tag.tagFor(tasks.trustDetails, config.maintainTrustDetailsEnabled)
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
        Link(NonEeaBusinessAsset, nonEeaCompanyRoute(identifier)),
        Tag.tagFor(tasks.assets, config.maintainNonEeaCompaniesEnabled)
      ),
      Task(
        Link(Protectors, protectorsRoute(identifier)),
        Tag.tagFor(tasks.protectors)
      ),
      Task(
        Link(Natural, otherIndividualsRoute(identifier)),
        Tag.tagFor(tasks.other)
      )
    ).filterNot(filter5mldSections(_, NonEeaBusinessAsset))

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
             link: Link): Task = migrationTaskStatus match {
      case Updated => Task(link, Completed)
      case NeedsUpdating if trustDetailsCompleted => Task(link, if (savedTaskStatus.isCompleted) InProgress else savedTaskStatus)
      case NothingToUpdate if trustDetailsCompleted => Task(link, if (savedTaskStatus.isCompleted) Completed else NoActionNeeded)
      case _ => Task(link, CannotStartYet)
    }

    val transitionTasks = List(
      Task(
        Link(TrustDetails, trustDetailsRoute(identifier)),
        Tag.tagFor(tasks.trustDetails, config.maintainTrustDetailsEnabled)
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
