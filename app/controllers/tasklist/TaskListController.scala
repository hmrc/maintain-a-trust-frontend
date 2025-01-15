/*
 * Copyright 2024 HM Revenue & Customs
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
import handlers.ErrorHandler
import models.Enumerable
import models.requests.ClosingTrustRequest
import navigation.Navigator.declarationUrl
import play.api.Logging
import play.api.http.Writeable
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{NonTaxToTaxProgressView, VariationProgressView}

import scala.concurrent.{ExecutionContext, Future}

class TaskListController @Inject()(
                                    override val messagesApi: MessagesApi,
                                    actions: Actions,
                                    view: VariationProgressView,
                                    nonTaxToTaxView: NonTaxToTaxProgressView,
                                    val controllerComponents: MessagesControllerComponents,
                                    val config: FrontendAppConfig,
                                    storeConnector: TrustsStoreConnector,
                                    trustsConnector: TrustConnector,
                                    variationProgress: VariationProgress,
                                    errorHandler: ErrorHandler
                                  ) (implicit ec: ExecutionContext,writeableFutureHtml: Writeable[Future[Html]])
  extends FrontendBaseController with I18nSupport with Enumerable.Implicits with Logging {

  private def identifier(implicit request: ClosingTrustRequest[AnyContent]): String = request.userAnswers.identifier

  def onPageLoad(): Action[AnyContent]= actions.refreshAndRequireIsClosingAnswer.async {
    implicit request =>

      val result = for {
        tasks <- storeConnector.getStatusOfTasks(identifier)
        settlorsStatus <- trustsConnector.getSettlorsStatus(identifier)
        beneficiariesStatus <- trustsConnector.getBeneficiariesStatus(identifier)
        firstYearToAskFor <- trustsConnector.getFirstTaxYearToAskFor(identifier)
      } yield {
        if (request.userAnswers.isTrustMigratingFromNonTaxableToTaxable) {
          val sections = variationProgress.generateTransitionTaskList(
            tasks = tasks,
            settlorsStatus = settlorsStatus,
            beneficiariesStatus = beneficiariesStatus,
            yearsToAskFor = firstYearToAskFor.yearsAgo,
            identifier = request.userAnswers.identifier
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
            trustMldStatus = request.userAnswers.trustMldStatus,
            identifier = request.userAnswers.identifier
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

      result.value.map {
        case Right(call) => call
        case Left(_) =>
          val className = getClass.getSimpleName
          logger.warn(s"[$className][onPageLoad][Session ID: ${utils.Session.id(hc)}] Failed to render view.")
          InternalServerError(errorHandler.internalServerErrorTemplate)
      }
  }

  def onSubmit(): Action[AnyContent] = actions.requireIsClosingAnswer {
    implicit request =>
      Redirect(declarationUrl(request.user.affinityGroup, request.userAnswers.isTrustMigratingFromNonTaxableToTaxable))
  }
}
