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

package controllers.transition

import com.google.inject.{Inject, Singleton}
import connectors.TrustConnector
import controllers.actions._
import handlers.ErrorHandler
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transition.BeforeYouContinueToTaxableView
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BeforeYouContinueToTaxableController @Inject()(
                                                      override val messagesApi: MessagesApi,
                                                      actions: Actions,
                                                      val controllerComponents: MessagesControllerComponents,
                                                      view: BeforeYouContinueToTaxableView,
                                                      connector: TrustConnector,
                                                      errorHandler: ErrorHandler
                                                    ) (implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  private val className = getClass.getSimpleName

  def onPageLoad(): Action[AnyContent] = actions.verifiedForIdentifier.async {
    implicit request =>

      val identifier = request.userAnswers.identifier
      val identifierType = request.userAnswers.identifierType

      connector.getUntransformedTrustDetails(identifier).value.flatMap {
        case Right(trustDetails) => Future.successful(Ok(view(identifier, identifierType, displayExpress = trustDetails.expressTrust.isEmpty)))
        case Left(_) =>
          logger.warn(s"[$className][onPageLoad][Session ID: ${utils.Session.id(hc)}] Error while retrieving trust details.")
          errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      }
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForIdentifier.async {
    implicit request =>

      connector.getUntransformedTrustDetails(request.userAnswers.identifier).value.flatMap {
        case Right(trustDetails) => if (trustDetails.expressTrust.isEmpty) {
          Future.successful(Redirect(routes.ExpressTrustYesNoController.onPageLoad()))
        } else {
          Future.successful(Redirect(controllers.tasklist.routes.TaskListController.onPageLoad()))
        }
        case Left(_) =>
          logger.warn(s"[$className][onSubmit][Session ID: ${utils.Session.id(hc)}] Error while retrieving trust details.")
          errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      }
  }
}
