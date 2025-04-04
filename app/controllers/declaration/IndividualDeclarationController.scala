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

package controllers.declaration

import com.google.inject.{Inject, Singleton}
import controllers.actions._
import forms.declaration.IndividualDeclarationFormProvider
import handlers.ErrorHandler
import models.IndividualDeclaration
import models.errors.{DeclarationError, FormValidationError, TrustErrors}
import models.requests.ClosingTrustRequest
import pages.declaration.IndividualDeclarationPage
import pages.{SubmissionDatePage, TVNPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.DeclarationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.TrustClosureDate.getClosureDate
import utils.TrustEnvelope
import views.html.declaration.IndividualDeclarationView
import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndividualDeclarationController @Inject()(
                                                 override val messagesApi: MessagesApi,
                                                 playbackRepository: PlaybackRepository,
                                                 actions: Actions,
                                                 formProvider: IndividualDeclarationFormProvider,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 view: IndividualDeclarationView,
                                                 service: DeclarationService,
                                                 errorHandler: ErrorHandler) (implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  private val className = getClass.getSimpleName
  private val form: Form[IndividualDeclaration] = formProvider()

  def onPageLoad(): Action[AnyContent] = actions.requireIsClosingAnswer {
    implicit request =>

      val preparedForm = request.userAnswers.get(IndividualDeclarationPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.closingTrust))
  }

  def onSubmit(): Action[AnyContent] = actions.requireIsClosingAnswer.async {
    implicit request =>

      val result = for {
        declaration <- TrustEnvelope(handleFormValidation)
        tvnResponse <- service.individualDeclaration(request.userAnswers.identifier, declaration, getClosureDate(request.userAnswers))
        updatedAnswers <- TrustEnvelope(
          request.userAnswers
            .set(IndividualDeclarationPage, declaration)
            .flatMap(_.set(SubmissionDatePage, LocalDateTime.now))
            .flatMap(_.set(TVNPage, tvnResponse.tvn))
        )
        _ <- playbackRepository.set(updatedAnswers)
      } yield Redirect(controllers.declaration.routes.ConfirmationController.onPageLoad())

      result.value.flatMap {
        case Right(call) => Future.successful(call)
        case Left(FormValidationError(formBadRequest)) => Future.successful(formBadRequest)
        case Left(DeclarationError()) =>
          logger.warn(s"[$className][onSubmit][Session ID: ${utils.Session.id(hc)}] problem declaring trust")
          Future.successful(Redirect(controllers.declaration.routes.ProblemDeclaringController.onPageLoad()))
        case Left(_) =>
          logger.warn(s"[$className][onSubmit][Session ID: ${utils.Session.id(hc)}] Error while storing user answers")
          errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      }
  }

  private def handleFormValidation(implicit request: ClosingTrustRequest[AnyContent]): Either[TrustErrors, IndividualDeclaration] = {
    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Left(FormValidationError(BadRequest(view(formWithErrors, request.closingTrust)))),
      declaration => Right(declaration)
    )
  }
}
