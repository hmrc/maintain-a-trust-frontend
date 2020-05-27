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
import connectors.TrustsStoreConnector
import controllers.actions._
import forms.YesNoFormProvider
import navigation.MakeChangesQuestionController
import pages.makechanges.AddOrUpdateOtherIndividualsYesNoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import views.html.makechanges.UpdateOtherIndividualsYesNoView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateOtherIndividualsYesNoController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        playbackRepository: PlaybackRepository,
                                        actions: AuthenticateForPlayback,
                                        yesNoFormProvider: YesNoFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: UpdateOtherIndividualsYesNoView,
                                        trustStoreConnector: TrustsStoreConnector
                                     )(implicit ec: ExecutionContext)
  extends MakeChangesQuestionController(trustStoreConnector) with I18nSupport {

  val form: Form[Boolean] = yesNoFormProvider.withPrefix("updateOtherIndividuals")

  def onPageLoad(): Action[AnyContent] = actions.verifiedForUtr {
    implicit request =>

      val preparedForm = request.userAnswers.get(AddOrUpdateOtherIndividualsYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForUtr.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors))),
        value => {
          for {
            updatedAnswers <- Future.fromTry(
              request.userAnswers
                .set(AddOrUpdateOtherIndividualsYesNoPage, value)
            )
            _ <- playbackRepository.set(updatedAnswers)
            nextRoute <- decideNexRouteFromAnswers(updatedAnswers)
          } yield nextRoute
        }
      )
  }

}
