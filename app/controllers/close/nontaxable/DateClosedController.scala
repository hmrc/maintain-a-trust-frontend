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

package controllers.close.nontaxable

import connectors.TrustConnector
import controllers.actions.Actions
import forms.DateFormProvider
import handlers.ErrorHandler
import models.errors.{FormValidationError, TrustErrors}
import models.requests.DataRequest
import pages.close.nontaxable.DateClosedPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Session, TrustEnvelope}
import views.html.close.nontaxable.DateClosedView
import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DateClosedController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      playbackRepository: PlaybackRepository,
                                      actions: Actions,
                                      formProvider: DateFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      view: DateClosedView,
                                      trustConnector: TrustConnector,
                                      errorHandler: ErrorHandler
                                    ) (implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  private val className = getClass.getSimpleName
  private val prefix: String = "dateClosed"

  def onPageLoad(): Action[AnyContent] = actions.verifiedForIdentifier.async {
    implicit request =>

      val result = for {
        startDate <- trustConnector.getStartDate(request.userAnswers.identifier)
      } yield {
        val form = formProvider.withPrefixAndTrustStartDate(prefix, startDate)

        val preparedForm = request.userAnswers.get(DateClosedPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm))
      }

      result.value.flatMap {
        case Right(renderPage) => Future.successful(renderPage)
        case Left(_) =>
          logger.warn(s"[$className][onPageLoad][Session ID: ${Session.id(hc)}] Error while retrieving start date")
          errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      }
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForIdentifier.async {
    implicit request =>

      val result = for {
        startDate <- trustConnector.getStartDate(request.userAnswers.identifier)
        formData <- TrustEnvelope(handleFormValidation(startDate))
        updatedAnswers <- TrustEnvelope(request.userAnswers.set(DateClosedPage, formData))
        _ <- playbackRepository.set(updatedAnswers)
      } yield {
        Redirect(controllers.close.routes.BeforeClosingController.onPageLoad())
      }

      result.value.flatMap {
        case Right(call) => Future.successful(call)
        case Left(FormValidationError(formBadRequest)) => Future.successful(formBadRequest)
        case Left(_) =>
          logger.warn(s"[$className][onSubmit][Session ID: ${Session.id(hc)}] Error while storing user answers")
          errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      }
  }

  private def handleFormValidation(startDate: LocalDate)(implicit request: DataRequest[AnyContent]): Either[TrustErrors, LocalDate] = {
    val form = formProvider.withPrefixAndTrustStartDate(prefix, startDate)

    form.bindFromRequest().fold(
      formWithErrors =>
        Left(FormValidationError(BadRequest(view(formWithErrors)))),
      value => Right(value)
    )
  }
}
