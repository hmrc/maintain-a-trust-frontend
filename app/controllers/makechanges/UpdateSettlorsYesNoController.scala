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
import connectors.{TrustConnector, TrustsStoreConnector}
import controllers.actions._
import forms.YesNoFormProvider
import pages.makechanges.UpdateSettlorsYesNoPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import views.html.makechanges.UpdateSettlorsYesNoView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateSettlorsYesNoController @Inject()(
                                               override val messagesApi: MessagesApi,
                                               playbackRepository: PlaybackRepository,
                                               trustConnector: TrustConnector,
                                               actions: Actions,
                                               yesNoFormProvider: YesNoFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               view: UpdateSettlorsYesNoView,
                                               trustStoreConnector: TrustsStoreConnector
                                     )(implicit ec: ExecutionContext)

  extends MakeChangesQuestionRouterController(trustConnector, trustStoreConnector) {

  private def prefix(closingTrust: Boolean): String = {
    if (closingTrust) "updateSettlorsClosing" else "updateSettlors"
  }

  def onPageLoad(): Action[AnyContent] = actions.requireIsClosingAnswer {
    implicit request =>

      val form: Form[Boolean] = yesNoFormProvider.withPrefix(prefix(request.closingTrust))

      val preparedForm = request.userAnswers.get(UpdateSettlorsYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, prefix(request.closingTrust)))
  }

  def onSubmit(): Action[AnyContent] = actions.requireIsClosingAnswer.async {
    implicit request =>

      val form: Form[Boolean] = yesNoFormProvider.withPrefix(prefix(request.closingTrust))

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, prefix(request.closingTrust)))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(
              request.userAnswers
                .set(UpdateSettlorsYesNoPage, value)
            )
            _ <- playbackRepository.set(updatedAnswers)
            nextRoute <- routeToAddOrUpdateProtectors()(request.request)
          } yield nextRoute
        }
      )
  }

}
