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
import models.MigrationTaskStatus._
import models.pages.Tag
import models.pages.Tag._
import models.{CompletedMaintenanceTasks, MigrationTaskStatus, TrustMldStatus}
import pages.Page
import sections._
import sections.assets.{Assets, NonEeaBusinessAsset}
import sections.beneficiaries.Beneficiaries
import sections.settlors.Settlors
import viewmodels.{Link, Task}

trait TaskListSections {

  case class TaskList(mandatory: List[Task] = Nil, other: List[Task] = Nil) {
    val isAbleToDeclare: Boolean = !(mandatory ::: other).exists(_.tag.contains(InProgress))
  }

  val config: FrontendAppConfig

  private def trustDetailsRouteEnabled(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainTrustDetailsEnabled) {
      config.maintainTrustDetailsUrl(identifier)
    }
  }

  private def trustAssetsRouteEnabled(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainTrustAssetsEnabled) {
      config.maintainTrustAssetsUrl(identifier)
    }
  }

  private def taxLiabilityRouteEnabled(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainTaxLiabilityEnabled) {
      config.maintainTaxLiabilityUrl(identifier)
    }
  }

  private def settlorsRouteEnabled(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainSettlorsEnabled) {
      config.maintainSettlorsUrl(identifier)
    }
  }

  private def trusteesRouteEnabled(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainTrusteesEnabled) {
      config.maintainTrusteesUrl(identifier)
    }
  }

  private def beneficiariesRouteEnabled(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainBeneficiariesEnabled) {
      config.maintainBeneficiariesUrl(identifier)
    }
  }

  private def protectorsRouteEnabled(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainProtectorsEnabled) {
      config.maintainProtectorsUrl(identifier)
    }
  }

  private def otherIndividualsRouteEnabled(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainOtherIndividualsEnabled) {
      config.maintainOtherIndividualsUrl(identifier)
    }
  }

  private def nonEeaCompanyRouteEnabled(identifier: String): String = {
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
                       identifier: String,
                       trustMldStatus: TrustMldStatus): TaskList = {

    def filter5mldSections(task: Task, section: Page): Boolean = {
      task.link.text == section.toString && !trustMldStatus.is5mldTrustIn5mldMode
    }

    val mandatoryTasks = List(
      Task(
        Link(TrustDetails, Some(trustDetailsRouteEnabled(identifier))),
        Some(Tag.tagFor(tasks.trustDetails, config.maintainTrustDetailsEnabled))
      ),
      Task(
        Link(Settlors, Some(settlorsRouteEnabled(identifier))),
        Some(Tag.tagFor(tasks.settlors))
      ),
      Task(
        Link(Trustees, Some(trusteesRouteEnabled(identifier))),
        Some(Tag.tagFor(tasks.trustees))
      ),
      Task(
        Link(Beneficiaries, Some(beneficiariesRouteEnabled(identifier))),
        Some(Tag.tagFor(tasks.beneficiaries))
      )
    ).filterNot(filter5mldSections(_, TrustDetails))

    val optionalTasks = List(
      Task(
        Link(NonEeaBusinessAsset, Some(nonEeaCompanyRouteEnabled(identifier))),
        Some(Tag.tagFor(tasks.assets, config.maintainNonEeaCompaniesEnabled))
      ),
      Task(
        Link(Protectors, Some(protectorsRouteEnabled(identifier))),
        Some(Tag.tagFor(tasks.protectors))
      ),
      Task(
        Link(Natural, Some(otherIndividualsRouteEnabled(identifier))),
        Some(Tag.tagFor(tasks.other))
      )
    ).filterNot(filter5mldSections(_, NonEeaBusinessAsset))

    TaskList(mandatoryTasks, optionalTasks)
  }

  def generateTransitionTaskList(tasks: CompletedMaintenanceTasks,
                                 identifier: String,
                                 settlorsStatus: MigrationTaskStatus,
                                 beneficiariesStatus: MigrationTaskStatus,
                                 yearsToAskFor: Int): TaskList = {

    def task(taskStatus: MigrationTaskStatus,
             taskCompleted: Boolean,
             trustDetailsCompleted: Boolean,
             link: Link): List[Task] = taskStatus match {
      case Updated => List(Task(link, Some(Completed)))
      case NeedsUpdating if trustDetailsCompleted => List(Task(link, Some(NotStarted)))
      case NeedsUpdating => List(Task(link, Some(CannotStartYet)))
      case NothingToUpdate if taskCompleted => List(Task(link, Some(Completed)))
      case NothingToUpdate => Nil
    }

    def linkUrl(route: String => String): Option[String] = {
      if (tasks.trustDetails) Some(route(identifier)) else None
    }

    val transitionTasks = List(
      Task(
        Link(TrustDetails, Some(trustDetailsRouteEnabled(identifier))),
        Some(Tag.tagFor(tasks.trustDetails, config.maintainTrustDetailsEnabled))
      ),
      Task(
        Link(Assets, Some(trustAssetsRouteEnabled(identifier))),
        Some(Tag.tagFor(tasks.assets))
      )
    )

    lazy val taxLiabilityTask = Task(
      Link(TaxLiability, Some(taxLiabilityRouteEnabled(identifier))),
      Some(Tag.tagFor(tasks.taxLiability))
    )

    val settlorsTask = task(settlorsStatus, tasks.settlors, tasks.trustDetails, Link(Settlors, linkUrl(settlorsRouteEnabled)))
    val beneficiariesTask = task(beneficiariesStatus, tasks.beneficiaries, tasks.trustDetails, Link(Beneficiaries, linkUrl(beneficiariesRouteEnabled)))

    TaskList(
      if (yearsToAskFor == 0) transitionTasks else transitionTasks :+ taxLiabilityTask,
      settlorsTask ::: beneficiariesTask
    )
  }

}
