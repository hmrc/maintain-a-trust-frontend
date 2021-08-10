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
import models.pages.Tag.{CannotStartYet, Completed, InProgress, NotStarted}
import models.{CompletedMaintenanceTasks, MigrationTaskStatus, TaskList, TrustMldStatus, UserAnswers}
import pages.Page
import pages.tasks._
import sections._
import sections.assets.{Assets, NonEeaBusinessAsset}
import sections.beneficiaries.Beneficiaries
import sections.settlors.Settlors
import viewmodels.{Link, Task}

import javax.inject.Inject

class VariationProgress @Inject()(config: FrontendAppConfig) {

  def trustDetailsRouteEnabled(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainTrustDetailsEnabled) {
      config.maintainTrustDetailsUrl(identifier)
    }
  }

  def trustAssetsRouteEnabled(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainTrustAssetsEnabled) {
      config.maintainTrustAssetsUrl(identifier)
    }
  }

  def taxLiabilityRouteEnabled(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainTaxLiabilityEnabled) {
      config.maintainTaxLiabilityUrl(identifier)
    }
  }

  def settlorsRouteEnabled(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainSettlorsEnabled) {
      config.maintainSettlorsUrl(identifier)
    }
  }

  def trusteesRouteEnabled(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainTrusteesEnabled) {
      config.maintainTrusteesUrl(identifier)
    }
  }

  def beneficiariesRouteEnabled(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainBeneficiariesEnabled) {
      config.maintainBeneficiariesUrl(identifier)
    }
  }

  def protectorsRouteEnabled(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainProtectorsEnabled) {
      config.maintainProtectorsUrl(identifier)
    }
  }

  def otherIndividualsRouteEnabled(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainOtherIndividualsEnabled) {
      config.maintainOtherIndividualsUrl(identifier)
    }
  }

  def nonEeaCompanyRouteEnabled(identifier: String): String = {
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

  private def redirectToTask(task: TaskStartedPage): String = controllers.tasklist.routes.TaskListController.redirectToTask(task).url

  def generateTaskList(tasks: CompletedMaintenanceTasks,
                       trustMldStatus: TrustMldStatus,
                       userAnswers: UserAnswers): TaskList = {

    def filter5mldSections(task: Task, section: Page): Boolean = {
      task.link.text == section.toString && !trustMldStatus.is5mldTrustIn5mldMode
    }

    val mandatoryTasks = List(
      Task(
        Link(TrustDetails, Some(redirectToTask(TrustDetailsTaskStartedPage))),
        Tag.tagFor(tasks.trustDetails, userAnswers.get(TrustDetailsTaskStartedPage), config.maintainTrustDetailsEnabled)
      ),
      Task(
        Link(Settlors, Some(redirectToTask(SettlorsTaskStartedPage))),
        Tag.tagFor(tasks.settlors, userAnswers.get(SettlorsTaskStartedPage))
      ),
      Task(
        Link(Trustees, Some(redirectToTask(TrusteesTaskStartedPage))),
        Tag.tagFor(tasks.trustees, userAnswers.get(TrusteesTaskStartedPage))
      ),
      Task(
        Link(Beneficiaries, Some(redirectToTask(BeneficiariesTaskStartedPage))),
        Tag.tagFor(tasks.beneficiaries, userAnswers.get(BeneficiariesTaskStartedPage))
      )
    ).filterNot(filter5mldSections(_, TrustDetails))

    val optionalTasks = List(
      Task(
        Link(NonEeaBusinessAsset, Some(redirectToTask(AssetsTaskStartedPage))),
        Tag.tagFor(tasks.assets, userAnswers.get(AssetsTaskStartedPage), config.maintainNonEeaCompaniesEnabled)
      ),
      Task(
        Link(Protectors, Some(redirectToTask(ProtectorsTaskStartedPage))),
        Tag.tagFor(tasks.protectors, userAnswers.get(ProtectorsTaskStartedPage))
      ),
      Task(
        Link(Natural, Some(redirectToTask(OtherIndividualsTaskStartedPage))),
        Tag.tagFor(tasks.other, userAnswers.get(OtherIndividualsTaskStartedPage))
      )
    ).filterNot(filter5mldSections(_, NonEeaBusinessAsset))

    TaskList(mandatoryTasks, optionalTasks)
  }

  def generateTransitionTaskList(tasks: CompletedMaintenanceTasks,
                                 settlorsStatus: MigrationTaskStatus,
                                 beneficiariesStatus: MigrationTaskStatus,
                                 yearsToAskFor: Int,
                                 userAnswers: UserAnswers): TaskList = {

    def task(taskStatus: MigrationTaskStatus,
             trustDetailsCompleted: Boolean,
             taskStarted: Boolean,
             link: Link): List[Task] = taskStatus match {
      case Updated => List(Task(link, Completed))
      case NeedsUpdating if trustDetailsCompleted => List(Task(link, if (taskStarted) InProgress else NotStarted))
      case NothingToUpdate if trustDetailsCompleted => List(Task(link, Completed))
      case _ => List(Task(link, CannotStartYet))
    }

    def linkUrl(route: String): Option[String] = {
      if (tasks.trustDetails) Some(route) else None
    }

    val transitionTasks = List(
      Task(
        Link(TrustDetails, Some(redirectToTask(TrustDetailsTaskStartedPage))),
        Tag.tagFor(tasks.trustDetails, userAnswers.get(TrustDetailsTaskStartedPage), config.maintainTrustDetailsEnabled)
      ),
      Task(
        Link(Assets, Some(redirectToTask(AssetsTaskStartedPage))),
        Tag.tagFor(tasks.assets, userAnswers.get(AssetsTaskStartedPage))
      )
    )

    lazy val taxLiabilityTask = Task(
      Link(TaxLiability, Some(redirectToTask(TaxLiabilityTaskStartedPage))),
      Tag.tagFor(tasks.taxLiability, userAnswers.get(TaxLiabilityTaskStartedPage))
    )

    val settlorsTask = task(
      taskStatus = settlorsStatus,
      trustDetailsCompleted = tasks.trustDetails,
      taskStarted = userAnswers.get(SettlorsTaskStartedPage).contains(true),
      link = Link(Settlors, linkUrl(redirectToTask(SettlorsTaskStartedPage)))
    )

    val beneficiariesTask = task(
      taskStatus = beneficiariesStatus,
      trustDetailsCompleted = tasks.trustDetails,
      taskStarted = userAnswers.get(BeneficiariesTaskStartedPage).contains(true),
      link = Link(Beneficiaries, linkUrl(redirectToTask(BeneficiariesTaskStartedPage)))
    )

    TaskList(
      if (yearsToAskFor == 0) transitionTasks else transitionTasks :+ taxLiabilityTask,
      settlorsTask ::: beneficiariesTask
    )
  }

}
