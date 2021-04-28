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

package controllers.transition

import com.google.inject.{Inject, Singleton}
import connectors.TrustConnector
import controllers.actions._
import forms.YesNoFormProvider
import pages.transition.TaxLiabilityYesNoPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transition.TaxLiabilityYesNoView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxLiabilityYesNoController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       playbackRepository: PlaybackRepository,
                                       actions: Actions,
                                       val controllerComponents: MessagesControllerComponents,
                                       yesNoFormProvider: YesNoFormProvider,
                                       view: TaxLiabilityYesNoView,
                                       trustsConnector: TrustConnector
                                     )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  private val form: Form[Boolean] = yesNoFormProvider.withPrefix("taxLiabilityYesNo")

  def onPageLoad(): Action[AnyContent] = actions.verifiedForIdentifier {
    implicit request =>

      val identifier = request.userAnswers.identifier
      val identifierType = request.userAnswers.identifierType

      val preparedForm = request.userAnswers.get(TaxLiabilityYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, identifier, identifierType))
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForIdentifier.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          val identifier = request.userAnswers.identifier
          val identifierType = request.userAnswers.identifierType
          Future.successful(BadRequest(view(formWithErrors, identifier, identifierType)))
        },
        value => {
          if (value) {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(TaxLiabilityYesNoPage, value))
              _ <- playbackRepository.set(updatedAnswers)
              _ <- trustsConnector.setExpressTrust(request.userAnswers.identifier, true)
            } yield {
              Redirect(routes.BeforeYouContinueToTaxableController.onPageLoad())
            }
          } else {
            Future.successful(Redirect(controllers.routes.WhatIsNextController.onPageLoad()))
          }
        }
      )
  }

}
