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

package controllers.makechanges

import com.google.inject.{Inject, Singleton}
import connectors.{TrustConnector, TrustsStoreConnector}
import controllers.actions._
import forms.YesNoFormProvider
import handlers.ErrorHandler
import models.errors.{FormValidationError, TrustErrors}
import models.requests.ClosingTrustRequest
import pages.makechanges.AddOrUpdateNonEeaCompanyYesNoPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import utils.TrustEnvelope
import views.html.makechanges.UpdateNonEeaCompanyYesNoView
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateNonEeaCompanyYesNoController @Inject()(
                                                    override val messagesApi: MessagesApi,
                                                    playbackRepository: PlaybackRepository,
                                                    actions: Actions,
                                                    yesNoFormProvider: YesNoFormProvider,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    view: UpdateNonEeaCompanyYesNoView,
                                                    trustConnector: TrustConnector,
                                                    trustStoreConnector: TrustsStoreConnector,
                                                    errorHandler: ErrorHandler
                                                  ) (implicit ec: ExecutionContext)
  extends MakeChangesQuestionRouterController(trustConnector, trustStoreConnector) with I18nSupport with Logging {

  private val className = getClass.getSimpleName

  private def prefix(closingTrust: Boolean): String = {
    if (closingTrust) "updateNonEeaCompanyClosing" else "updateNonEeaCompany"
  }

  def onPageLoad(): Action[AnyContent] = actions.requireIsClosingAnswer {
    implicit request =>

      val form: Form[Boolean] = yesNoFormProvider.withPrefix(prefix(request.closingTrust))

      val preparedForm = request.userAnswers.get(AddOrUpdateNonEeaCompanyYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, prefix(request.closingTrust)))
  }

  def onSubmit(): Action[AnyContent] = actions.requireIsClosingAnswer.async {
    implicit request =>

      val result = for {
        formData <- TrustEnvelope(handleFormValidation)
        updatedAnswers <- TrustEnvelope(request.userAnswers.set(AddOrUpdateNonEeaCompanyYesNoPage, formData))
        _ <- playbackRepository.set(updatedAnswers)
        nextRoute <- routeToDeclareOrTaskList(updatedAnswers, request.closingTrust)(request.request)
      } yield nextRoute

      result.value.flatMap {
        case Right(call) => Future.successful(call)
        case Left(FormValidationError(formBadRequest)) => Future.successful(formBadRequest)
        case Left(_) =>
          logger.warn(s"[$className][onSubmit][Session ID: ${utils.Session.id(hc)}] Error while storing user answers")
//          InternalServerError(errorHandler.internalServerErrorTemplate)
          errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      }
  }

  private def handleFormValidation(implicit request: ClosingTrustRequest[AnyContent]): Either[TrustErrors, Boolean] = {
    val form: Form[Boolean] = yesNoFormProvider.withPrefix(prefix(request.closingTrust))

    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Left(FormValidationError(BadRequest(view(formWithErrors, prefix(request.closingTrust))))),
      value => Right(value)
    )
  }

}
