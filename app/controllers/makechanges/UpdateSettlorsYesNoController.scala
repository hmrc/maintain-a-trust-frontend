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
import connectors.TrustConnector
import controllers.actions._
import forms.YesNoFormProvider
import models.{CloseMode, UpdateMode, WhatNextMode}
import pages.UTRPage
import pages.makechanges.UpdateSettlorsYesNoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.makechanges.UpdateSettlorsYesNoView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateSettlorsYesNoController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        playbackRepository: PlaybackRepository,
                                        trustConnector: TrustConnector,
                                        actions: AuthenticateForPlayback,
                                        yesNoFormProvider: YesNoFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: UpdateSettlorsYesNoView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: WhatNextMode): Action[AnyContent] = actions.verifiedForUtr {
    implicit request =>

      val form: Form[Boolean] = yesNoFormProvider.withPrefix(prefix(mode))

      val preparedForm = request.userAnswers.get(UpdateSettlorsYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, prefix(mode)))
  }

  def onSubmit(mode: WhatNextMode): Action[AnyContent] = actions.verifiedForUtr.async {
    implicit request =>

      val form: Form[Boolean] = yesNoFormProvider.withPrefix(prefix(mode))

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, mode, prefix(mode)))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(
              request.userAnswers
                .set(UpdateSettlorsYesNoPage, value)
            )
            _ <- playbackRepository.set(updatedAnswers)
            protectorsExist <- trustConnector.getDoProtectorsAlreadyExist(request.userAnswers.get(UTRPage).get)
          } yield {
              if(protectorsExist.value) {
                Redirect(controllers.makechanges.routes.UpdateProtectorYesNoController.onPageLoad(mode))
              } else {
                Redirect(controllers.makechanges.routes.AddProtectorYesNoController.onPageLoad(mode))
              }
          }
        }
      )

  }

  private def prefix(mode: WhatNextMode): String = {
    mode match {
      case UpdateMode => "updateSettlors"
      case CloseMode => "updateSettlorsClosing"
    }
  }

}
