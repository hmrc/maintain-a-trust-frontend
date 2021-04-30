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
import models.requests.DataRequest
import pages.transition.NeedToPayTaxYesNoPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transition.NeedToPayTaxYesNoView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

@Singleton
class NeedToPayTaxYesNoController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             playbackRepository: PlaybackRepository,
                                             actions: Actions,
                                             val controllerComponents: MessagesControllerComponents,
                                             yesNoFormProvider: YesNoFormProvider,
                                             view: NeedToPayTaxYesNoView,
                                             trustConnector: TrustConnector
                                     )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  private val form: Form[Boolean] = yesNoFormProvider.withPrefix("needToPayTaxYesNo")

  def onPageLoad(): Action[AnyContent] = actions.verifiedForIdentifier {
    implicit request =>

      val identifier = request.userAnswers.identifier
      val identifierType = request.userAnswers.identifierType

      val preparedForm = request.userAnswers.get(NeedToPayTaxYesNoPage) match {
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
        needsToPayTax =>
          for {
            hasAnswerChanged <- Future.fromTry(Success(!request.userAnswers.get(NeedToPayTaxYesNoPage).contains(needsToPayTax)))
            updatedAnswers <- Future.fromTry(request.userAnswers.set(NeedToPayTaxYesNoPage, needsToPayTax))
            _ <- playbackRepository.set(updatedAnswers)
            - <- updateTransforms(hasAnswerChanged, needsToPayTax)
          } yield {
            if (needsToPayTax) {
              Redirect(routes.BeforeYouContinueToTaxableController.onPageLoad())
            } else {
              Redirect(controllers.routes.WhatIsNextController.onPageLoad())
            }
          }
      )
  }

  private def updateTransforms(hasAnswerChanged: Boolean, needsToPayTax: Boolean)
                              (implicit request: DataRequest[AnyContent]): Future[Unit] = {

    (hasAnswerChanged, needsToPayTax) match {
      case (false, _) => Future.successful(())
      case (true, true) =>
        for {
          _ <- trustConnector.setTaxableTrust(request.userAnswers.identifier, needsToPayTax)
          _ <- trustConnector.setTaxableMigrationFlag(request.userAnswers.identifier, needsToPayTax)
        } yield ()
      case (true, false) => trustConnector.removeTransforms(request.userAnswers.identifier).map(_ => ())
    }

  }
}
