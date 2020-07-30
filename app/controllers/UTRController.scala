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
import controllers.actions.Actions
import forms.UTRFormProvider
import models.{UserAnswers, UtrSession}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{ActiveSessionRepository, PlaybackRepository}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.UTRView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class UTRController @Inject()(
                               override val messagesApi: MessagesApi,
                               actions: Actions,
                               playbackRepository: PlaybackRepository,
                               sessionRepository: ActiveSessionRepository,
                               formProvider: UTRFormProvider,
                               val controllerComponents: MessagesControllerComponents,
                               view: UTRView
                             )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = actions.auth.async {
    implicit request =>
      Future.successful(Ok(view(form, routes.UTRController.onSubmit())))
  }

  def onSubmit(): Action[AnyContent] = actions.auth.async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, routes.UTRController.onSubmit()))),
        utr => {

          val activeSession = UtrSession(request.user.internalId, utr)
          val newEmptyAnswers = UserAnswers.startNewSession(request.user.internalId, utr)

          for {
            _ <- playbackRepository.resetCache(utr, request.user.internalId)
            _ <- playbackRepository.set(newEmptyAnswers)
            _ <- sessionRepository.set(activeSession)
          } yield Redirect(controllers.routes.TrustStatusController.status())
        }
      )
  }

}
