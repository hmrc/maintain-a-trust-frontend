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
import pages.transition.NeedToPayTaxYesNoPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.MaintainATrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.TrustEnvelope.TrustEnvelope
import utils.{Session, TrustEnvelope}
import views.html.transition.NeedToPayTaxYesNoView

import scala.concurrent.ExecutionContext

@Singleton
class NeedToPayTaxYesNoController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             playbackRepository: PlaybackRepository,
                                             actions: Actions,
                                             val controllerComponents: MessagesControllerComponents,
                                             yesNoFormProvider: YesNoFormProvider,
                                             view: NeedToPayTaxYesNoView,
                                             trustConnector: TrustConnector,
                                             maintainATrustService: MaintainATrustService,
                                             errorHandler: ErrorHandler
                                           )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  private val className = getClass.getSimpleName
  private val form: Form[Boolean] = yesNoFormProvider.withPrefix("needToPayTaxYesNo")

  def onPageLoad(): Action[AnyContent] = actions.verifiedForIdentifier {
    implicit request =>

      val identifier = request.userAnswers.identifier
      val identifierType = request.userAnswers.identifierType

      val preparedForm = request.userAnswers.get(NeedToPayTaxYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, identifier, identifierType))
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForIdentifier.async {
    implicit request =>

      val result = for {
        needsToPayTax <- TrustEnvelope(handleFormValidation)
        hasAnswerChanged <- TrustEnvelope(!request.userAnswers.get(NeedToPayTaxYesNoPage).contains(needsToPayTax))
        updatedAnswers <- TrustEnvelope(request.userAnswers.set(NeedToPayTaxYesNoPage, needsToPayTax))
        _ <- playbackRepository.set(updatedAnswers)
        _ <- updateTransforms(hasAnswerChanged, needsToPayTax)
      } yield {
        if (needsToPayTax) {
          Redirect(routes.BeforeYouContinueToTaxableController.onPageLoad())
        } else {
          Redirect(controllers.routes.WhatIsNextController.onPageLoad())
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

  private def updateTransforms(hasAnswerChanged: Boolean, needsToPayTax: Boolean)
                              (implicit request: DataRequest[AnyContent]): TrustEnvelope[Unit] = {
    (hasAnswerChanged, needsToPayTax) match {
      case (false, _) =>
        logger.info(s"[$className][updateTransforms][Session ID: ${Session.id(hc)}] Answer hasn't changed. Nothing to update.")
        TrustEnvelope(())
      case (true, true) =>
        logger.info(s"[$className][updateTransforms][Session ID: ${Session.id(hc)}] Answer has changed to yes. Setting taxable trust.")
        trustConnector.setTaxableTrust(request.userAnswers.identifier, needsToPayTax).map(_ => ())
      case (true, false) =>
        logger.info(s"[$className][updateTransforms][Session ID: ${Session.id(hc)}] Answer has changed to no. Removing transforms and resetting tasks.")
        maintainATrustService.removeTransformsAndResetTaskList(request.userAnswers.identifier)
    }
  }

  private def handleFormValidation(implicit request: DataRequest[AnyContent]): Either[TrustErrors, Boolean] = {
    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) => {
        val identifier = request.userAnswers.identifier
        val identifierType = request.userAnswers.identifierType
        Left(FormValidationError(BadRequest(view(formWithErrors, identifier, identifierType))))
      },
      value => Right(value)
    )
  }

}
