/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.close.taxable

import connectors.TrustConnector
import controllers.actions.Actions
import forms.DateFormProvider
import handlers.ErrorHandler
import models.errors.{FormValidationError, TrustErrors}
import models.requests.DataRequest
import pages.close.taxable.DateLastAssetSharedOutPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.TrustEnvelope
import views.html.close.taxable.DateLastAssetSharedOutView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DateLastAssetSharedOutController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  playbackRepository: PlaybackRepository,
                                                  actions: Actions,
                                                  formProvider: DateFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: DateLastAssetSharedOutView,
                                                  trustConnector: TrustConnector,
                                                  errorHandler: ErrorHandler
                                                )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  private val className = getClass.getSimpleName
  private val prefix: String = "dateLastAssetSharedOut"

  def onPageLoad(): Action[AnyContent] = actions.verifiedForIdentifier.async {
    implicit request =>

      val result = for {
        startDate <- trustConnector.getStartDate(request.userAnswers.identifier)
      } yield {
        val form = formProvider.withPrefixAndTrustStartDate(prefix, startDate)

        val preparedForm = request.userAnswers.get(DateLastAssetSharedOutPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm))
      }

      result.value.map {
        case Right(renderPage) => renderPage
        case Left(_) =>
          logger.warn(s"[$className][onPageLoad][Session ID: ${utils.Session.id(hc)}] Error while retrieving start date.")
          InternalServerError(errorHandler.internalServerErrorTemplate)
      }
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForIdentifier.async {
    implicit request =>

      val result = for {
        startDate <- trustConnector.getStartDate(request.userAnswers.identifier)
        formData <- TrustEnvelope(handleFormValidation(startDate))
        updatedAnswers <- TrustEnvelope(request.userAnswers.set(DateLastAssetSharedOutPage, formData))
        _ <- playbackRepository.set(updatedAnswers)
      } yield Redirect(controllers.close.routes.BeforeClosingController.onPageLoad())

      result.value.map {
        case Right(call) => call
        case Left(FormValidationError(formBadRequest)) => formBadRequest
        case Left(_) =>
          logger.warn(s"[$className][onSubmit][Session ID: ${utils.Session.id(hc)}] Error while storing user answers")
          InternalServerError(errorHandler.internalServerErrorTemplate)
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
