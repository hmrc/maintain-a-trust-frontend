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

package controllers.close

import com.google.inject.{Inject, Singleton}
import controllers.actions._
import forms.YesNoFormProvider
import models.WhatNextMode
import pages.close.DateLastAssetSharedOutYesNoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.close.DateLastAssetSharedOutYesNoView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DateLastAssetSharedOutYesNoController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        playbackRepository: PlaybackRepository,
                                        actions: AuthenticateForPlayback,
                                        yesNoFormProvider: YesNoFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: DateLastAssetSharedOutYesNoView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = yesNoFormProvider.withPrefix("dateLastAssetSharedOutYesNo")

  def onPageLoad(): Action[AnyContent] = actions.verifiedForUtr {
    implicit request =>

      val preparedForm = request.userAnswers.get(DateLastAssetSharedOutYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      // TODO: don't hard-code utr
      Ok(view(preparedForm, "1234567890"))
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForUtr.async {
    implicit request =>

      // TODO: don't hard-code utr
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, "1234567890"))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(
              request.userAnswers
                .set(DateLastAssetSharedOutYesNoPage, value)
            )
            _ <- playbackRepository.set(updatedAnswers)
          } yield {
            if (value) {
              Redirect(controllers.close.routes.DateLastAssetSharedOutController.onPageLoad())
            } else {
              Redirect(controllers.close.routes.HowToCloseATrustController.onPageLoad())
            }
          }
        }
      )

  }

}
