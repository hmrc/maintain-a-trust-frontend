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
import models.CompletedMaintenanceTasks
import models.pages.Tag
import models.pages.Tag.InProgress
import sections.assets.NonEeaBusinessAsset
import sections.beneficiaries.Beneficiaries
import sections.settlors.Settlors
import sections.{Natural, Protectors, TrustDetails, Trustees}
import viewmodels.{Link, Task}

trait TaskListSections {

  case class TaskList(mandatory: List[Task], other: List[Task]) {
    val isAbleToDeclare: Boolean = !(mandatory ::: other).exists(_.tag.contains(InProgress))
  }

  private lazy val notYetAvailable: String =
    controllers.routes.FeatureNotAvailableController.onPageLoad().url

  val config: FrontendAppConfig

  private def trustDetailsRouteEnabled(identifier: String): String = {
    redirectToServiceIfEnabled(config.maintainTrustDetailsEnabled) {
      config.maintainTrustDetailsUrl(identifier)
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
      notYetAvailable
    }
  }

  def generateTaskList(tasks: CompletedMaintenanceTasks,
                       identifier: String,
                       is5mldEnabled: Boolean,
                       isTrust5mldTaxable: Boolean): TaskList = {

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
    ).filterNot(_.link.text == TrustDetails.toString && !(is5mldEnabled && isTrust5mldTaxable))

    val optionalSections = List(
      Task(
        Link(NonEeaBusinessAsset, nonEeaCompanyRouteEnabled(identifier)),
        Some(Tag.tagFor(tasks.nonEeaCompany, config.maintainNonEeaCompaniesEnabled))
      ),
      Task(
        Link(Protectors, protectorsRouteEnabled(identifier)),
        Some(Tag.tagFor(tasks.protectors))
      ),
      Task(
        Link(Natural, otherIndividualsRouteEnabled(identifier)),
        Some(Tag.tagFor(tasks.other))
      )
    ).filterNot(_.link.text == NonEeaBusinessAsset.toString && !(is5mldEnabled && isTrust5mldTaxable))

    TaskList(mandatorySections, optionalSections)
  }

}