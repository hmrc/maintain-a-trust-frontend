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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{TrustConnector, TrustsStoreConnector}
import controllers.actions.Actions
import models.Enumerable
import navigation.Navigator.declarationUrl
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{NonTaxToTaxProgressView, VariationProgressView}

import scala.concurrent.ExecutionContext

class TaskListController @Inject()(
                                    override val messagesApi: MessagesApi,
                                    actions: Actions,
                                    view: VariationProgressView,
                                    nonTaxToTaxView: NonTaxToTaxProgressView,
                                    val controllerComponents: MessagesControllerComponents,
                                    val config: FrontendAppConfig,
                                    storeConnector: TrustsStoreConnector,
                                    trustsConnector: TrustConnector
                                  )(implicit ec: ExecutionContext) extends FrontendBaseController
  with I18nSupport with Enumerable.Implicits with TaskListSections {

  def onPageLoad(): Action[AnyContent] = actions.refreshAndRequireIsClosingAnswer.async {
    implicit request =>

      val identifier = request.userAnswers.identifier

      for {
        tasks <- storeConnector.getStatusOfTasks(identifier)
        settlorsStatus <- trustsConnector.getSettlorsStatus(identifier)
        beneficiariesStatus <- trustsConnector.getBeneficiariesStatus(identifier)
      } yield {
        if (request.userAnswers.isTrustMigratingFromNonTaxableToTaxable) {
          val sections = generateTransitionTaskList(tasks, identifier, settlorsStatus, beneficiariesStatus)
          Ok(nonTaxToTaxView(
            identifier,
            identifierType = request.userAnswers.identifierType,
            mandatory = sections.mandatory,
            additional = sections.other,
            affinityGroup = request.user.affinityGroup,
            nextUrl = declarationUrl(request.user.affinityGroup, isTrustMigratingFromNonTaxableToTaxable = true),
            isAbleToDeclare = sections.isAbleToDeclare
          ))
        } else {
          val sections = generateTaskList(tasks, identifier, request.userAnswers.trustMldStatus)
          Ok(view(
            identifier,
            identifierType = request.userAnswers.identifierType,
            mandatory = sections.mandatory,
            optional = sections.other,
            affinityGroup = request.user.affinityGroup,
            nextUrl = declarationUrl(request.user.affinityGroup, isTrustMigratingFromNonTaxableToTaxable = false),
            isAbleToDeclare = sections.isAbleToDeclare,
            closingTrust = request.closingTrust
          ))
        }
      }
  }
}
