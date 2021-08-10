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
                                    trustsConnector: TrustConnector,
                                    variationProgress: VariationProgress
                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Enumerable.Implicits {

  def onPageLoad(): Action[AnyContent] = actions.refreshAndRequireIsClosingAnswer.async {
    implicit request =>

      val identifier = request.userAnswers.identifier

      for {
        tasks <- storeConnector.getStatusOfTasks(identifier)
        settlorsStatus <- trustsConnector.getSettlorsStatus(identifier)
        beneficiariesStatus <- trustsConnector.getBeneficiariesStatus(identifier)
        firstYearToAskFor <- trustsConnector.getFirstTaxYearToAskFor(identifier)
      } yield {
        if (request.userAnswers.isTrustMigratingFromNonTaxableToTaxable) {
          val sections = variationProgress.generateTransitionTaskList(
            tasks = tasks,
            identifier = identifier,
            settlorsStatus = settlorsStatus,
            beneficiariesStatus = beneficiariesStatus,
            yearsToAskFor = firstYearToAskFor.yearsAgo,
            userAnswers = request.userAnswers
          )

          Ok(nonTaxToTaxView(
            identifier = identifier,
            identifierType = request.userAnswers.identifierType,
            mandatory = sections.mandatory,
            additional = sections.other,
            affinityGroup = request.user.affinityGroup,
            isAbleToDeclare = sections.isAbleToDeclare
          ))
        } else {
          val sections = variationProgress.generateTaskList(
            tasks = tasks,
            identifier = identifier,
            trustMldStatus = request.userAnswers.trustMldStatus,
            userAnswers = request.userAnswers
          )

          Ok(view(
            identifier = identifier,
            identifierType = request.userAnswers.identifierType,
            mandatory = sections.mandatory,
            optional = sections.other,
            affinityGroup = request.user.affinityGroup,
            isAbleToDeclare = sections.isAbleToDeclare,
            closingTrust = request.closingTrust
          ))
        }
      }
  }

  def onSubmit(): Action[AnyContent] = actions.requireIsClosingAnswer {
    implicit request =>
      Redirect(declarationUrl(request.user.affinityGroup, request.userAnswers.isTrustMigratingFromNonTaxableToTaxable))
  }
}
