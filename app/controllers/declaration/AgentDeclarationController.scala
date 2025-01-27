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
import forms.declaration.AgentDeclarationFormProvider
import handlers.ErrorHandler
import models.errors._
import models.requests.{AgentUser, ClosingTrustRequest}
import models.{Address, AgentDeclaration, UserAnswers}
import pages._
import pages.declaration.{AgencyRegisteredAddressInternationalPage, AgencyRegisteredAddressUkPage, AgencyRegisteredAddressUkYesNoPage, AgentDeclarationPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.DeclarationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.TrustClosureDate.getClosureDate
import utils.TrustEnvelope
import views.html.declaration.AgentDeclarationView

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentDeclarationController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            playbackRepository: PlaybackRepository,
                                            actions: Actions,
                                            formProvider: AgentDeclarationFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: AgentDeclarationView,
                                            service: DeclarationService,
                                            errorHandler: ErrorHandler
                                          ) (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val className = getClass.getSimpleName
  private val form: Form[AgentDeclaration] = formProvider()

  def onPageLoad(): Action[AnyContent] = actions.requireIsClosingAnswer {
    implicit request =>

      val preparedForm = request.userAnswers.get(AgentDeclarationPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.closingTrust))
  }

  def onSubmit(): Action[AnyContent] = actions.requireIsClosingAnswer.async {
    implicit request =>

      val result = for {
        declaration <- TrustEnvelope(handleFormValidation)
        agentUser <- TrustEnvelope(isUserAgent)
        agencyAddress <- TrustEnvelope.fromOption(getAgencyRegisteredAddress(request.userAnswers))
        tvnResponse <- service.agentDeclaration(
          request.userAnswers.identifier,
          declaration,
          agentUser.agentReferenceNumber,
          agencyAddress,
          declaration.agencyName,
          getClosureDate(request.userAnswers)
        )
        updatedAnswers <- TrustEnvelope(
          request.userAnswers
            .set(AgentDeclarationPage, declaration)
            .flatMap(_.set(SubmissionDatePage, LocalDateTime.now))
            .flatMap(_.set(TVNPage, tvnResponse.tvn))
        )
        _ <- playbackRepository.set(updatedAnswers)
      } yield {
        Redirect(controllers.declaration.routes.ConfirmationController.onPageLoad())
      }
      result.value.flatMap {
        case Right(call) => Future.successful(call)
        case Left(FormValidationError(formBadRequest)) => Future.successful(formBadRequest)
        case Left(WrongUserType()) =>
          logger.warn(s"[$className][onSubmit][Session ID: ${utils.Session.id(hc)}] User was not an agent.")
          Future.successful(Redirect(controllers.declaration.routes.ProblemDeclaringController.onPageLoad()))
        case Left(DeclarationError()) =>
          logger.warn(s"[$className][onSubmit][Session ID: ${utils.Session.id(hc)}] problem declaring trust.")
          Future.successful(Redirect(controllers.declaration.routes.ProblemDeclaringController.onPageLoad()))
        case Left(NoData) =>
          logger.warn(s"[$className][onSubmit][Session ID: ${utils.Session.id(hc)}] Failed to get agency address")
          Future.successful(Redirect(controllers.declaration.routes.ProblemDeclaringController.onPageLoad()))
        case Left(_) =>
          logger.warn(s"[$className][onSubmit][Session ID: ${utils.Session.id(hc)}] Error while storing user answers")
//          InternalServerError(errorHandler.internalServerErrorTemplate)
          errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      }
  }

  private def getAgencyRegisteredAddress(userAnswers: UserAnswers): Option[Address] = {
    userAnswers.get(AgencyRegisteredAddressUkYesNoPage) flatMap {
      case true => userAnswers.get(AgencyRegisteredAddressUkPage)
      case false => userAnswers.get(AgencyRegisteredAddressInternationalPage)
    }
  }

  private def handleFormValidation(implicit request: ClosingTrustRequest[AnyContent]): Either[TrustErrors, AgentDeclaration] = {
    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Left(FormValidationError(BadRequest(view(formWithErrors, request.closingTrust)))),
      declaration => Right(declaration)
    )
  }

  private def isUserAgent(implicit request: ClosingTrustRequest[AnyContent]): Either[TrustErrors, AgentUser] = {
    request.user match {
      case user: AgentUser => Right(user)
      case _ => Left(WrongUserType())
    }
  }

}
