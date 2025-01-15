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
import forms.InternationalAddressFormProvider
import handlers.ErrorHandler
import models.InternationalAddress
import models.errors.{FormValidationError, TrustErrors}
import models.requests.ClosingTrustRequest
import navigation.Navigator.agentDeclarationUrl
import pages.declaration.AgencyRegisteredAddressInternationalPage
import play.api.Logging
import play.api.data.Form
import play.api.http.Writeable
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.TrustEnvelope
import utils.countryoptions.CountryOptionsNonUK
import views.html.declaration.AgencyRegisteredAddressInternationalView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgencyRegisteredAddressInternationalController @Inject()(
                                                                override val messagesApi: MessagesApi,
                                                                playbackRepository: PlaybackRepository,
                                                                actions: Actions,
                                                                formProvider: InternationalAddressFormProvider,
                                                                countryOptions: CountryOptionsNonUK,
                                                                val controllerComponents: MessagesControllerComponents,
                                                                view: AgencyRegisteredAddressInternationalView,
                                                                errorHandler: ErrorHandler
                                                              ) (implicit ec: ExecutionContext,writeableFutureHtml: Writeable[Future[Html]])
  extends FrontendBaseController with I18nSupport with Logging {

  private val className = getClass.getSimpleName
  private val form: Form[InternationalAddress] = formProvider()

  def onPageLoad(): Action[AnyContent] = actions.requireIsClosingAnswer {
    implicit request =>

      val preparedForm = request.userAnswers.get(AgencyRegisteredAddressInternationalPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, countryOptions.options()))
  }

  def onSubmit(): Action[AnyContent] = actions.requireIsClosingAnswer.async {
    implicit request =>

      val result = for {
        formData <- TrustEnvelope(handleFormValidation)
        updatedAnswers <- TrustEnvelope(request.userAnswers.set(AgencyRegisteredAddressInternationalPage, formData))
        _ <- playbackRepository.set(updatedAnswers)
      } yield Redirect(agentDeclarationUrl(request.userAnswers.isTrustMigratingFromNonTaxableToTaxable))

      result.value.map {
        case Right(call) => call
        case Left(FormValidationError(formBadRequest)) => formBadRequest
        case Left(_) =>
          logger.warn(s"[$className][onSubmit][Session ID: ${utils.Session.id(hc)}] Error while storing user answers")
          InternalServerError(errorHandler.internalServerErrorTemplate)
      }
  }

  private def handleFormValidation(implicit request: ClosingTrustRequest[AnyContent]): Either[TrustErrors, InternationalAddress] = {
    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Left(FormValidationError(BadRequest(view(formWithErrors, countryOptions.options())))),
      value => Right(value)
    )
  }

}
