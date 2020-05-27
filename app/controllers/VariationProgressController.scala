/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.TrustsStoreConnector
import controllers.actions.AuthenticateForPlayback
import models.pages.{Tag, WhatIsNext}
import models.pages.Tag.InProgress
import models.{CloseMode, CompletedMaintenanceTasks, Enumerable, NormalMode, Mode}
import navigation.DeclareNoChange
import pages.{UTRPage, WhatIsNextPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import sections.Protectors
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import viewmodels.tasks.{Beneficiaries, NaturalPeople, Settlors, Trustees}
import viewmodels.{Link, Task}
import views.html.VariationProgressView

import scala.concurrent.{ExecutionContext, Future}

class VariationProgressController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      actions: AuthenticateForPlayback,
                                      view: VariationProgressView,
                                      val controllerComponents: MessagesControllerComponents,
                                      config: FrontendAppConfig,
                                      storeConnector: TrustsStoreConnector
                                    )(implicit ec: ExecutionContext) extends DeclareNoChange with I18nSupport with Enumerable.Implicits {

  lazy val notYetAvailable : String = controllers.routes.FeatureNotAvailableController.onPageLoad().url

  def beneficiariesRouteEnabled(utr: String): String = {
    if (config.maintainBeneficiariesEnabled) {
      config.maintainBeneficiariesUrl(utr)
    } else {
      notYetAvailable
    }
  }

  def settlorsRouteEnabled(utr: String): String = {
    if (config.maintainSettlorsEnabled) {
      config.maintainSettlorsUrl(utr)
    } else {
      notYetAvailable
    }
  }

  def protectorsRouteEnabled(utr: String): String = {
    if (config.maintainProtectorsEnabled) {
      config.maintainProtectorsUrl(utr)
    } else {
      notYetAvailable
    }
  }

  def otherIndividualsRouteEnabled(utr: String): String = {
    if (config.maintainOtherIndividualsEnabled) {
      config.maintainOtherIndividualsUrl(utr)
    } else {
      notYetAvailable
    }
  }

  case class TaskList(mandatory: List[Task], other: List[Task]) {
    val isAbleToDeclare : Boolean = !(mandatory ::: other).exists(_.tag.contains(InProgress))
  }

  private def taskList(tasks : CompletedMaintenanceTasks, utr: String) : TaskList = {
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
        Link(Protectors, protectorsRouteEnabled(utr)),
        Some(Tag.tagFor(tasks.protectors, config.maintainProtectorsEnabled))
      ),
      Task(
        Link(NaturalPeople, otherIndividualsRouteEnabled(utr)),
        Some(Tag.tagFor(tasks.other, config.maintainOtherIndividualsEnabled))
      )
    )

    TaskList(mandatorySections, optionalSections)
  }

  def onPageLoad(): Action[AnyContent] = actions.verifiedForUtr.async {
    implicit request =>

      request.userAnswers.get(UTRPage) match {
        case Some(utr) =>

          storeConnector.getStatusOfTasks(utr) map {
            tasks =>

              val sections = taskList(tasks, utr)

              request.userAnswers.get(WhatIsNextPage) match {
                case Some(whatNext) =>
                  val next = if (request.user.affinityGroup == Agent) {
                    controllers.declaration.routes.AgencyRegisteredAddressUkYesNoController.onPageLoad(mode(whatNext)).url
                  } else {
                    controllers.declaration.routes.IndividualDeclarationController.onPageLoad(mode(whatNext)).url
                  }

                  Ok(view(utr,
                    sections.mandatory,
                    sections.other,
                    request.user.affinityGroup,
                    next,
                    isAbleToDeclare = sections.isAbleToDeclare,
                    mode(whatNext)
                  ))
                case _ =>
                  Redirect(routes.WhatIsNextController.onPageLoad())
              }
          }
        case _ =>
          Future.successful(Redirect(routes.UTRController.onPageLoad()))
      }
  }

  private def mode(whatNext: WhatIsNext): Mode = {
    whatNext match {
      case WhatIsNext.CloseTrust => CloseMode
      case _ => NormalMode
    }
  }
}
