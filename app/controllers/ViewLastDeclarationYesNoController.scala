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

package controllers

import com.google.inject.{Inject, Singleton}
import controllers.actions._
import forms.YesNoFormProvider
import handlers.ErrorHandler
import models.errors.{FormValidationError, TrustErrors}
import models.requests.DataRequest
import pages.ViewLastDeclarationYesNoPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.TrustEnvelope
import views.html.ViewLastDeclarationYesNoView

import scala.concurrent.ExecutionContext

@Singleton
class ViewLastDeclarationYesNoController @Inject()(
                                                    override val messagesApi: MessagesApi,
                                                    playbackRepository: PlaybackRepository,
                                                    actions: Actions,
                                                    yesNoFormProvider: YesNoFormProvider,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    view: ViewLastDeclarationYesNoView,
                                                    errorHandler: ErrorHandler
                                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val className = getClass.getSimpleName
  private val form: Form[Boolean] = yesNoFormProvider.withPrefix("viewLastDeclarationYesNo")

  def onPageLoad(): Action[AnyContent] = actions.verifiedForIdentifier {
    implicit request =>

      val preparedForm = request.userAnswers.get(ViewLastDeclarationYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.userAnswers.identifier, request.userAnswers.identifierType))
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForIdentifier.async {
    implicit request =>

      val result = for {
        formData <- TrustEnvelope(handleFormValidation)
        updatedAnswers <- TrustEnvelope(request.userAnswers.set(ViewLastDeclarationYesNoPage, formData))
        _ <- playbackRepository.set(updatedAnswers)
      } yield {
        if (formData) {
          Redirect(controllers.print.routes.PrintLastDeclaredAnswersController.onPageLoad())
        } else {
          Redirect(routes.WhatIsNextController.onPageLoad())
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

  private def handleFormValidation(implicit request: DataRequest[AnyContent]): Either[TrustErrors, Boolean] = {
    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Left(FormValidationError(BadRequest(view(formWithErrors, request.userAnswers.identifier, request.userAnswers.identifierType)))),
      value => Right(value)
    )
  }

}
