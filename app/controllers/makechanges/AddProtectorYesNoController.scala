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

package controllers.makechanges

import com.google.inject.{Inject, Singleton}
import controllers.actions._
import forms.YesNoFormProvider
import models.{CloseMode, Mode}
import pages.makechanges.AddOrUpdateProtectorYesNoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.makechanges.AddProtectorYesNoView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddProtectorYesNoController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        playbackRepository: PlaybackRepository,
                                        actions: AuthenticateForPlayback,
                                        yesNoFormProvider: YesNoFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: AddProtectorYesNoView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.verifiedForUtr {
    implicit request =>

      val form: Form[Boolean] = yesNoFormProvider.withPrefix(prefix(mode))

      val preparedForm = request.userAnswers.get(AddOrUpdateProtectorYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, prefix(mode)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.verifiedForUtr.async {
    implicit request =>

      val form: Form[Boolean] = yesNoFormProvider.withPrefix(prefix(mode))

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, mode, prefix(mode)))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(
              request.userAnswers
                .set(AddOrUpdateProtectorYesNoPage, value)
            )
            _ <- playbackRepository.set(updatedAnswers)
          } yield {
            Redirect(controllers.makechanges.routes.AddOtherIndividualsYesNoController.onPageLoad(mode))
          }
        }
      )
  }

  private def prefix(mode: Mode): String = {
    mode match {
      case CloseMode => "addProtectorClosing"
      case _ => "addProtector"
    }
  }

}
