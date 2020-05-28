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
import pages.makechanges.UpdateTrusteesYesNoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.makechanges.UpdateTrusteesYesNoView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateTrusteesYesNoController @Inject()(
                                               override val messagesApi: MessagesApi,
                                               playbackRepository: PlaybackRepository,
                                               actions: AuthenticateForPlayback,
                                               yesNoFormProvider: YesNoFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               view: UpdateTrusteesYesNoView
                                             )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = actions.requireAnswer {
    implicit request =>

      val form: Form[Boolean] = yesNoFormProvider.withPrefix(prefix(request.closingTrust))

      val preparedForm = request.userAnswers.get(UpdateTrusteesYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, prefix(request.closingTrust)))
  }

  def onSubmit(): Action[AnyContent] = actions.requireAnswer.async {
    implicit request =>

      val form: Form[Boolean] = yesNoFormProvider.withPrefix(prefix(request.closingTrust))

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, prefix(request.closingTrust)))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(
              request.userAnswers
                .set(UpdateTrusteesYesNoPage, value)
            )
            _ <- playbackRepository.set(updatedAnswers)
          } yield {
            Redirect(controllers.makechanges.routes.UpdateBeneficiariesYesNoController.onPageLoad())
          }
        }
      )

  }

  private def prefix(closingTrust: Boolean): String = {
    if (closingTrust) "updateTrusteesClosing" else "updateTrustees"
  }

}
