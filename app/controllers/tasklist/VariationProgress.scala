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
import models.pages.Tag.{CannotStartYet, Completed}
import models.{CompletedMaintenanceTasks, MigrationTaskStatus, TaskList, TrustMldStatus}
import pages.Page
import sections._
import sections.assets.{Assets, NonEeaBusinessAsset}
import sections.beneficiaries.Beneficiaries
import sections.settlors.Settlors
import viewmodels.{Link, Task}

import javax.inject.Inject

class VariationProgress @Inject()(config: FrontendAppConfig) {

  def trustDetailsRoute(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainTrustDetailsEnabled) {
      config.maintainTrustDetailsUrl(identifier)
    }
  }

  def trustAssetsRoute(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainTrustAssetsEnabled) {
      config.maintainTrustAssetsUrl(identifier)
    }
  }

  def taxLiabilityRoute(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainTaxLiabilityEnabled) {
      config.maintainTaxLiabilityUrl(identifier)
    }
  }

  def settlorsRoute(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainSettlorsEnabled) {
      config.maintainSettlorsUrl(identifier)
    }
  }

  def trusteesRoute(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainTrusteesEnabled) {
      config.maintainTrusteesUrl(identifier)
    }
  }

  def beneficiariesRoute(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainBeneficiariesEnabled) {
      config.maintainBeneficiariesUrl(identifier)
    }
  }

  def protectorsRoute(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainProtectorsEnabled) {
      config.maintainProtectorsUrl(identifier)
    }
  }

  def otherIndividualsRoute(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainOtherIndividualsEnabled) {
      config.maintainOtherIndividualsUrl(identifier)
    }
  }

  def nonEeaCompanyRoute(identifier: String): String = {
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
        Link(TrustDetails, Some(trustDetailsRoute(identifier))),
        Tag.tagFor(tasks.trustDetails, config.maintainTrustDetailsEnabled)
      ),
      Task(
        Link(Settlors, Some(settlorsRoute(identifier))),
        Tag.tagFor(tasks.settlors)
      ),
      Task(
        Link(Trustees, Some(trusteesRoute(identifier))),
        Tag.tagFor(tasks.trustees)
      ),
      Task(
        Link(Beneficiaries, Some(beneficiariesRoute(identifier))),
        Tag.tagFor(tasks.beneficiaries)
      )
    ).filterNot(filter5mldSections(_, TrustDetails))

    val optionalTasks = List(
      Task(
        Link(NonEeaBusinessAsset, Some(trustAssetsRoute(identifier))),
        Tag.tagFor(tasks.assets, config.maintainNonEeaCompaniesEnabled)
      ),
      Task(
        Link(Protectors, Some(protectorsRoute(identifier))),
        Tag.tagFor(tasks.protectors)
      ),
      Task(
        Link(Natural, Some(otherIndividualsRoute(identifier))),
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
             link: Link): List[Task] = migrationTaskStatus match {
      case Updated => List(Task(link, Completed))
      case NeedsUpdating if trustDetailsCompleted => List(Task(link, savedTaskStatus))
      case NothingToUpdate if trustDetailsCompleted => List(Task(link, Completed))
      case _ => List(Task(link, CannotStartYet))
    }

    def linkUrl(route: String): Option[String] = {
      if (tasks.trustDetails.isCompleted) Some(route) else None
    }

    val transitionTasks = List(
      Task(
        Link(TrustDetails, Some(trustDetailsRoute(identifier))),
        Tag.tagFor(tasks.trustDetails, config.maintainTrustDetailsEnabled)
      ),
      Task(
        Link(Assets, Some(trustAssetsRoute(identifier))),
        Tag.tagFor(tasks.assets)
      )
    )

    lazy val taxLiabilityTask = Task(
      Link(TaxLiability, Some(taxLiabilityRoute(identifier))),
      Tag.tagFor(tasks.taxLiability)
    )

    val settlorsTask = task(
      migrationTaskStatus = settlorsStatus,
      savedTaskStatus = tasks.settlors,
      trustDetailsCompleted = tasks.trustDetails.isCompleted,
      link = Link(Settlors, linkUrl(settlorsRoute(identifier)))
    )

    val beneficiariesTask = task(
      migrationTaskStatus = beneficiariesStatus,
      savedTaskStatus = tasks.beneficiaries,
      trustDetailsCompleted = tasks.trustDetails.isCompleted,
      link = Link(Beneficiaries, linkUrl(beneficiariesRoute(identifier)))
    )

    TaskList(
      if (yearsToAskFor == 0) transitionTasks else transitionTasks :+ taxLiabilityTask,
      settlorsTask ::: beneficiariesTask
    )
  }

}
