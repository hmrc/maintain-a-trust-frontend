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
import models.pages.Tag
import models.pages.Tag.InProgress
import models.{CompletedMaintenanceTasks, EntityStatus, TrustMldStatus, TrustTaxability}
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
                       trustMldStatus: TrustMldStatus,
                       trustTaxability: TrustTaxability): TaskList = {

    if (!trustTaxability.isTrustMigratingFromNonTaxableToTaxable) {

      def filter5mldSections(task: Task, section: Page): Boolean = {
        task.link.text == section.toString && !trustMldStatus.is5mldTrustIn5mldMode
      }

      val mandatorySections = List(
        Task(
          Link(TrustDetails, trustDetailsRouteEnabled(identifier)),
          Some(Tag.tagFor(tasks.trustDetails, config.maintainTrustDetailsEnabled))
        ),
        Task(
          Link(Settlors, settlorsRouteEnabled(identifier)),
          Some(Tag.tagFor(tasks.settlors))
        ),
        Task(
          Link(Trustees, trusteesRouteEnabled(identifier)),
          Some(Tag.tagFor(tasks.trustees))
        ),
        Task(
          Link(Beneficiaries, beneficiariesRouteEnabled(identifier)),
          Some(Tag.tagFor(tasks.beneficiaries))
        )
      ).filterNot(filter5mldSections(_, TrustDetails))

      val optionalSections = List(
        Task(
          Link(NonEeaBusinessAsset, nonEeaCompanyRouteEnabled(identifier)),
          Some(Tag.tagFor(tasks.assets, config.maintainNonEeaCompaniesEnabled))
        ),
        Task(
          Link(Protectors, protectorsRouteEnabled(identifier)),
          Some(Tag.tagFor(tasks.protectors))
        ),
        Task(
          Link(Natural, otherIndividualsRouteEnabled(identifier)),
          Some(Tag.tagFor(tasks.other))
        )
      ).filterNot(filter5mldSections(_, NonEeaBusinessAsset))

      TaskList(mandatorySections, optionalSections)
    } else {

      val transitionSections = List(
        Task(
          Link(TrustDetails, trustDetailsRouteEnabled(identifier)),
          Some(Tag.tagFor(tasks.trustDetails, config.maintainTrustDetailsEnabled))
        ),
        Task(
          Link(Assets, trustAssetsRouteEnabled(identifier)),
          Some(Tag.tagFor(tasks.assets))
        ),
        Task(
          Link(TaxLiability, taxLiabilityRouteEnabled(identifier)),
          Some(Tag.tagFor(tasks.taxLiability))
        )
      )

      TaskList(transitionSections)
    }
  }

  def amendTransitionTaskList(taskList: TaskList,
                              identifier: String,
                              settlorsStatus: EntityStatus,
                              beneficiariesStatus: EntityStatus): TaskList = {

    def task(status: EntityStatus, link: Link): Seq[Task] = status.completed match {
      case Some(value) => Seq(Task(link, Some(Tag.tagFor(value))))
      case None => Nil
    }

    val settlorsTask = task(settlorsStatus, Link(Settlors, settlorsRouteEnabled(identifier)))
    val beneficiariesTask = task(beneficiariesStatus, Link(Beneficiaries, beneficiariesRouteEnabled(identifier)))

    TaskList(
      taskList.mandatory ++ settlorsTask ++ beneficiariesTask,
      taskList.other
    )
  }

}
