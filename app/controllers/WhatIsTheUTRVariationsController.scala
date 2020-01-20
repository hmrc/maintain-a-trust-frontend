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

import config.FrontendAppConfig
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.WhatIsTheUTRFormProvider
import handlers.ErrorHandler
import javax.inject.Inject
import models.UserAnswers
import pages.WhatIsTheUTRVariationPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.WhatIsTheUTRView

import scala.concurrent.{ExecutionContext, Future}

class WhatIsTheUTRVariationsController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  playbackRepository: PlaybackRepository,
                                                  formProvider: WhatIsTheUTRFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: WhatIsTheUTRView,
                                                  config: FrontendAppConfig,
                                                  errorHandler: ErrorHandler
                                                )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData) {
    implicit request =>

      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.internalId)).get(WhatIsTheUTRVariationPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, routes.WhatIsTheUTRVariationsController.onSubmit()))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, routes.WhatIsTheUTRVariationsController.onSubmit()))),
        utr => {

          val newUpdatedAnswerSession = request.userAnswers.getOrElse(UserAnswers(request.internalId)).set(WhatIsTheUTRVariationPage, utr)

          for {
            updatedAnswers <- Future.fromTry(newUpdatedAnswerSession)
            _ <- playbackRepository.set(updatedAnswers)
          } yield Redirect(controllers.routes.IndexController.onPageLoad()) //controllers.routes.TrustStatusController.status()
        }
      )
  }

}
