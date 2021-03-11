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
import config.FrontendAppConfig
import connectors.{TrustConnector, TrustsStoreConnector}
import controllers.actions.Actions
import controllers.makechanges.MakeChangesQuestionRouterController
import forms.WhatIsNextFormProvider
import models.Enumerable
import models.pages.WhatIsNext
import models.requests.{DataRequest, IdentifierRequest}
import pages.WhatIsNextPage
import pages.trustdetails.ExpressTrustYesNoPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import views.html.WhatIsNextView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WhatIsNextController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      playbackRepository: PlaybackRepository,
                                      actions: Actions,
                                      formProvider: WhatIsNextFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      view: WhatIsNextView,
                                      config: FrontendAppConfig,
                                      trustConnector: TrustConnector,
                                      trustsStoreConnector: TrustsStoreConnector
                                    )(implicit ec: ExecutionContext)

  extends MakeChangesQuestionRouterController(trustConnector, trustsStoreConnector) with Enumerable.Implicits {

  val form: Form[WhatIsNext] = formProvider()

  def onPageLoad(): Action[AnyContent] = actions.verifiedForIdentifier {
    implicit request =>

      val preparedForm = request.userAnswers.get(WhatIsNextPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.userAnswers.is5mldEnabled, isTrust5mldTaxable))
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForIdentifier.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, request.userAnswers.is5mldEnabled, isTrust5mldTaxable))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatIsNextPage, value))
            _ <- playbackRepository.set(updatedAnswers)
          } yield value match {
            case WhatIsNext.DeclareTheTrustIsUpToDate =>
              redirectToDeclaration()

            case WhatIsNext.MakeChanges =>
              Redirect(controllers.makechanges.routes.UpdateTrusteesYesNoController.onPageLoad())

            case WhatIsNext.CloseTrust if config.closeATrustEnabled =>
              Redirect(controllers.close.routes.DateLastAssetSharedOutYesNoController.onPageLoad())

            case WhatIsNext.NoLongerTaxable =>
              Redirect(controllers.routes.NoTaxLiabilityInfoController.onPageLoad())

            case WhatIsNext.GeneratePdf =>
              Redirect(controllers.routes.ObligedEntityPdfController.getPdf(request.userAnswers.identifier))

            case _ =>
              Redirect(controllers.routes.FeatureNotAvailableController.onPageLoad())
          }
        }
      )
  }

  private def isTrust5mldTaxable(implicit request: DataRequest[_]) = {
    request.userAnswers.get(ExpressTrustYesNoPage).isDefined && request.userAnswers.isTrustTaxable
  }
}
