/*
 * Copyright 2020 HM Revenue & Customs
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
import config.FrontendAppConfig
import controllers.actions.AuthenticateForPlayback
import forms.UTRFormProvider
import handlers.ErrorHandler
import models.UserAnswers
import pages.UTRPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.UTRView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UTRController @Inject()(
                               override val messagesApi: MessagesApi,
                               actions: AuthenticateForPlayback,
                               playbackRepository: PlaybackRepository,
                               formProvider: UTRFormProvider,
                               val controllerComponents: MessagesControllerComponents,
                               view: UTRView,
                               config: FrontendAppConfig,
                               errorHandler: ErrorHandler
                             )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = actions.authWithOptionalData {
    implicit request =>

      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.user.internalId)).get(UTRPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, routes.UTRController.onSubmit()))
  }

  def onSubmit(): Action[AnyContent] = actions.authWithOptionalData.async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, routes.UTRController.onSubmit()))),
        utr => {

          val newUpdatedAnswerSession = request.userAnswers.getOrElse(UserAnswers(request.user.internalId)).set(UTRPage, utr)

          for {
            updatedAnswers <- Future.fromTry(newUpdatedAnswerSession)
            _ <- playbackRepository.set(updatedAnswers)
          } yield Redirect(controllers.routes.InformationMaintainingThisTrustController.onPageLoad()) //controllers.routes.TrustStatusController.status()
        }
      )
  }

}
