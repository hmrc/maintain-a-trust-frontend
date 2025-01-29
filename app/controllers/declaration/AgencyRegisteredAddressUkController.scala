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
import forms.UKAddressFormProvider
import handlers.ErrorHandler
import models.UKAddress
import models.errors.{FormValidationError, TrustErrors}
import models.requests.ClosingTrustRequest
import navigation.Navigator.agentDeclarationUrl
import pages.declaration.AgencyRegisteredAddressUkPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.TrustEnvelope
import views.html.declaration.AgencyRegisteredAddressUkView
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgencyRegisteredAddressUkController @Inject()(
                                                     override val messagesApi: MessagesApi,
                                                     playbackRepository: PlaybackRepository,
                                                     actions: Actions,
                                                     formProvider: UKAddressFormProvider,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     view: AgencyRegisteredAddressUkView,
                                                     errorHandler: ErrorHandler
                                                   ) (implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  private val className = getClass.getSimpleName
  private val form: Form[UKAddress] = formProvider()

  def onPageLoad(): Action[AnyContent] = actions.requireIsClosingAnswer {
    implicit request =>

      val preparedForm = request.userAnswers.get(AgencyRegisteredAddressUkPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = actions.requireIsClosingAnswer.async {
    implicit request =>

      val result = for {
        formData <- TrustEnvelope(handleFormValidation)
        updatedAnswers <- TrustEnvelope(request.userAnswers.set(AgencyRegisteredAddressUkPage, formData))
        _ <- playbackRepository.set(updatedAnswers)
      } yield Redirect(agentDeclarationUrl(request.userAnswers.isTrustMigratingFromNonTaxableToTaxable))

      result.value.flatMap {
        case Right(call) => Future.successful(call)
        case Left(FormValidationError(formBadRequest)) => Future.successful(formBadRequest)
        case Left(_) =>
          logger.warn(s"[$className][onSubmit][Session ID: ${utils.Session.id(hc)}] Error while storing user answers")
          errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      }
  }

  private def handleFormValidation(implicit request: ClosingTrustRequest[AnyContent]): Either[TrustErrors, UKAddress] = {
    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Left(FormValidationError(BadRequest(view(formWithErrors)))),
      value => Right(value)
    )
  }

}
