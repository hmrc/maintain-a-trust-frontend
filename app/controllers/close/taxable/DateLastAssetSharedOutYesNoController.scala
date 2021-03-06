/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.close.taxable

import com.google.inject.{Inject, Singleton}
import controllers.actions._
import forms.YesNoFormProvider
import pages.close.taxable.DateLastAssetSharedOutYesNoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.close.taxable.DateLastAssetSharedOutYesNoView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DateLastAssetSharedOutYesNoController @Inject()(
                                                       override val messagesApi: MessagesApi,
                                                       playbackRepository: PlaybackRepository,
                                                       actions: Actions,
                                                       yesNoFormProvider: YesNoFormProvider,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       view: DateLastAssetSharedOutYesNoView
                                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = yesNoFormProvider.withPrefix("dateLastAssetSharedOutYesNo")

  def onPageLoad(): Action[AnyContent] = actions.verifiedForIdentifier {
    implicit request =>

      val preparedForm = request.userAnswers.get(DateLastAssetSharedOutYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.userAnswers.identifier))
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForIdentifier.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, request.userAnswers.identifier))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(
              request.userAnswers
                .set(DateLastAssetSharedOutYesNoPage, value)
            )
            _ <- playbackRepository.set(updatedAnswers)
          } yield {
            if (value) {
              Redirect(routes.DateLastAssetSharedOutController.onPageLoad())
            } else {
              Redirect(routes.HowToCloseATrustController.onPageLoad())
            }
          }
        }
      )
  }

}
