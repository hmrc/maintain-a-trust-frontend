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
import forms.YesNoFormProvider
import handlers.ErrorHandler
import models.errors.{FormValidationError, TrustErrors}
import models.requests.DataRequest
import pages.trustdetails.ExpressTrustYesNoPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.MaintainATrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.TrustEnvelope.TrustEnvelope
import utils.{Session, TrustEnvelope}
import views.html.transition.ExpressTrustYesNoView

import scala.concurrent.ExecutionContext

@Singleton
class ExpressTrustYesNoController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             playbackRepository: PlaybackRepository,
                                             actions: Actions,
                                             yesNoFormProvider: YesNoFormProvider,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: ExpressTrustYesNoView,
                                             trustsConnector: TrustConnector,
                                             maintainATrustService: MaintainATrustService,
                                             errorHandler: ErrorHandler
                                           )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  private val className = getClass.getSimpleName
  private val form: Form[Boolean] = yesNoFormProvider.withPrefix("expressTrustYesNo")

  def onPageLoad(): Action[AnyContent] = actions.verifiedForIdentifier {
    implicit request =>

      val preparedForm = request.userAnswers.get(ExpressTrustYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForIdentifier.async {
    implicit request =>

      val isTrustMigrating = request.userAnswers.isTrustMigratingFromNonTaxableToTaxable

      val result = for {
        formData <- TrustEnvelope(handleFormValidation)
        updatedAnswers <- TrustEnvelope(request.userAnswers.set(ExpressTrustYesNoPage, formData))
        _ <- playbackRepository.set(updatedAnswers)
        _ <- removeTransformsIfNotMigrating(isTrustMigrating)
        _ <- trustsConnector.setExpressTrust(request.userAnswers.identifier, formData)
      } yield {
        if (isTrustMigrating) {
          Redirect(controllers.tasklist.routes.TaskListController.onPageLoad())
        } else {
          Redirect(routes.ConfirmTrustTaxableController.onPageLoad())
        }
      }

      result.value.map {
        case Right(call) => call
        case Left(FormValidationError(formBadRequest)) => formBadRequest
        case Left(_) =>
          logger.warn(s"[$className][onSubmit][Session ID: ${utils.Session.id(hc)}] Error while storing user answers")
          InternalServerError(errorHandler.internalServerErrorTemplate)
      }
  }

  private def removeTransformsIfNotMigrating(isTrustMigrating: Boolean)
                                            (implicit request: DataRequest[AnyContent]): TrustEnvelope[Unit] = {
    if (isTrustMigrating) {
      logger.info(s"[$className][removeTransformsIfNotMigrating][Session ID: ${Session.id(hc)}] Migrating from non-taxable to taxable. Keeping transforms.")
      TrustEnvelope(())
    } else {
      logger.info(s"[$className][removeTransformsIfNotMigrating][Session ID: ${Session.id(hc)}] Redirected from RefreshedDataPreSubmitRetrievalAction or " +
        s"transitioning from 4MLD to 5MLD. Removing transforms and resetting tasks.")
      maintainATrustService.removeTransformsAndResetTaskList(request.userAnswers.identifier)
    }
  }

  private def handleFormValidation(implicit request: DataRequest[AnyContent]): Either[TrustErrors, Boolean] = {
    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Left(FormValidationError(BadRequest(view(formWithErrors)))),
      value => Right(value)
    )
  }

}
