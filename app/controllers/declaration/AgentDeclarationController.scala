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

package controllers.declaration

import java.time.LocalDateTime

import com.google.inject.{Inject, Singleton}
import controllers.actions._
import forms.declaration.AgentDeclarationFormProvider
import models.http.TVNResponse
import pages._
import pages.declaration.AgentDeclarationPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.DeclarationService
import views.html.declaration.AgentDeclarationView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentDeclarationController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            playbackRepository: PlaybackRepository,
                                            actions: AuthenticateForPlayback,
                                            formProvider: AgentDeclarationFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: AgentDeclarationView,
                                            service: DeclarationService
                                     )(implicit ec: ExecutionContext) extends DeclarationController with I18nSupport {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = actions.verifiedForUtr {
    implicit request =>

      val preparedForm = request.userAnswers.get(AgentDeclarationPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, controllers.declaration.routes.AgentDeclarationController.onSubmit()))
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForUtr.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, controllers.declaration.routes.AgentDeclarationController.onSubmit()))),

        value => {
          request.userAnswers.get(UTRPage) match {
            case None =>
              Future.successful(Redirect(controllers.routes.UTRController.onPageLoad()))
            case Some(utr) =>
              getAddress(request.userAnswers) match {
                case None =>
                  Future.successful(Redirect(controllers.declaration.routes.ProblemDeclaringController.onPageLoad()))
                case Some(address) =>
                  service.declareNoChange(utr, value, address) flatMap {
                    case TVNResponse(tvn) =>
                      for {
                        updatedAnswers <- Future.fromTry(
                          request.userAnswers
                            .set(AgentDeclarationPage, value)
                            .flatMap(_.set(SubmissionDatePage, LocalDateTime.now))
                            .flatMap(_.set(TVNPage, tvn))
                        )
                        _ <- playbackRepository.set(updatedAnswers)
                      } yield Redirect(controllers.declaration.routes.ConfirmationController.onPageLoad())
                    case _ =>
                      Future.successful(Redirect(controllers.declaration.routes.ProblemDeclaringController.onPageLoad()))
                  }
              }
          }
        }
      )

  }

}