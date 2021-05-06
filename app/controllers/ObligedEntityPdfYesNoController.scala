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

package controllers

import com.google.inject.{Inject, Singleton}
import connectors.TrustConnector
import controllers.actions._
import forms.YesNoFormProvider
import models.requests.DataRequest
import pages.{ObligedEntityPdfYesNoPage, ViewLastDeclarationYesNoPage}
import pages.trustdetails.ExpressTrustYesNoPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.MaintainATrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Session
import views.html.ObligedEntityPdfYesNoView
import views.html.transition.ExpressTrustYesNoView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ObligedEntityPdfYesNoController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             playbackRepository: PlaybackRepository,
                                             actions: Actions,
                                             yesNoFormProvider: YesNoFormProvider,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: ObligedEntityPdfYesNoView
                                           )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  private val form: Form[Boolean] = yesNoFormProvider.withPrefix("obligedEntityPdfYesNo")

  def onPageLoad(): Action[AnyContent] = actions.verifiedForIdentifier {
    implicit request =>

      val preparedForm = request.userAnswers.get(ObligedEntityPdfYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForIdentifier.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(
              request.userAnswers
                .set(ObligedEntityPdfYesNoPage, value)
            )
            _ <- playbackRepository.set(updatedAnswers)
          } yield {
            if (value) {
              Redirect(controllers.routes.ObligedEntityPdfController.getPdf(request.userAnswers.identifier))
            } else {
              Redirect(routes.WhatIsNextController.onPageLoad())
            }
          }
        }
      )
  }

}