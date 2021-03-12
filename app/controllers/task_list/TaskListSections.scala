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

package controllers.task_list

import config.FrontendAppConfig
import models.CompletedMaintenanceTasks
import models.pages.Tag
import models.pages.Tag.InProgress
import sections.Protectors
import viewmodels.tasks.{Beneficiaries, NaturalPeople, NonEeaCompany, Settlors, Trustees}
import viewmodels.{Link, Task}

trait TaskListSections {

  case class TaskList(mandatory: List[Task], other: List[Task]) {
    val isAbleToDeclare : Boolean = !(mandatory ::: other).exists(_.tag.contains(InProgress))
  }

  private lazy val notYetAvailable : String =
    controllers.routes.FeatureNotAvailableController.onPageLoad().url

  val config: FrontendAppConfig

  private def beneficiariesRouteEnabled(identifier: String): String = {
    if (config.maintainBeneficiariesEnabled) {
      config.maintainBeneficiariesUrl(identifier)
    } else {
      notYetAvailable
    }
  }

  private def settlorsRouteEnabled(identifier: String): String = {
    if (config.maintainSettlorsEnabled) {
      config.maintainSettlorsUrl(identifier)
    } else {
      notYetAvailable
    }
  }

  private def protectorsRouteEnabled(identifier: String): String = {
    if (config.maintainProtectorsEnabled) {
      config.maintainProtectorsUrl(identifier)
    } else {
      notYetAvailable
    }
  }

  private def otherIndividualsRouteEnabled(identifier: String): String = {
    if (config.maintainOtherIndividualsEnabled) {
      config.maintainOtherIndividualsUrl(identifier)
    } else {
      notYetAvailable
    }
  }

  private def nonEeaCompanyRouteEnabled(identifier: String): String = {
    if (config.maintainNonEeaCompanyEnabled) {
      config.maintainONonEeaCompanyUrl(identifier)
    } else {
      notYetAvailable
    }
  }

  def generateTaskList(tasks : CompletedMaintenanceTasks,
                       utr: String,
                       is5mldEnabled: Boolean,
                       isTrust5mldTaxable: Boolean) : TaskList = {

    val mandatorySections = List(
      Task(
        Link(Settlors, settlorsRouteEnabled(utr)),
        Some(Tag.tagFor(tasks.settlors, config.maintainSettlorsEnabled))
      ),
      Task(
        Link(Trustees, config.maintainTrusteesUrl(utr)),
        Some(Tag.tagFor(tasks.trustees, config.maintainTrusteesEnabled))
      ),
      Task(
        Link(Beneficiaries, beneficiariesRouteEnabled(utr)),
        Some(Tag.tagFor(tasks.beneficiaries, config.maintainBeneficiariesEnabled))
      )
    )

    val optionalSections = List(
      Task(
        Link(NonEeaCompany, nonEeaCompanyRouteEnabled(utr)),
        Some(Tag.tagFor(tasks.nonEeaCompany, config.maintainNonEeaCompanyEnabled))
      ),
      Task(
        Link(Protectors, protectorsRouteEnabled(utr)),
        Some(Tag.tagFor(tasks.protectors, config.maintainProtectorsEnabled))
      ),
      Task(
        Link(NaturalPeople, otherIndividualsRouteEnabled(utr)),
        Some(Tag.tagFor(tasks.other, config.maintainOtherIndividualsEnabled))
      )
    ).filterNot(_.link.text == NonEeaCompany.toString && !(is5mldEnabled && isTrust5mldTaxable))

    TaskList(mandatorySections, optionalSections)
  }

}
