/*
 * Copyright 2022 HM Revenue & Customs
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
import models.UserAnswers
import models.pages.WhatIsNext
import models.pages.WhatIsNext._
import models.requests.DataRequest
import pages.WhatIsNextPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.PlaybackRepository
import services.MaintainATrustService
import utils.Session
import views.html.WhatIsNextView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

@Singleton
class WhatIsNextController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      playbackRepository: PlaybackRepository,
                                      actions: Actions,
                                      formProvider: WhatIsNextFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      view: WhatIsNextView,
                                      trustConnector: TrustConnector,
                                      trustsStoreConnector: TrustsStoreConnector,
                                      maintainATrustService: MaintainATrustService,
                                      appConfig: FrontendAppConfig
                                    )(implicit ec: ExecutionContext)
  extends MakeChangesQuestionRouterController(trustConnector, trustsStoreConnector) with Logging {

  val form: Form[WhatIsNext] = formProvider()

  def onPageLoad(): Action[AnyContent] = actions.verifiedForIdentifier {
    implicit request =>

      val preparedForm = request.userAnswers.get(WhatIsNextPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.userAnswers.trustMldStatus))
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForIdentifier.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, request.userAnswers.trustMldStatus))),

        value => {
          for {
            hasAnswerChanged <- Future.fromTry(Success(!request.userAnswers.get(WhatIsNextPage).contains(value)))
            updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatIsNextPage, value))
            _ <- playbackRepository.set(updatedAnswers)
            result <- updateMigrationStatusAndRedirect(updatedAnswers, value, hasAnswerChanged)
          } yield {
            result
          }
        }
      )
  }

  private def updateMigrationStatusAndRedirect(userAnswers: UserAnswers, newAnswer: WhatIsNext, hasAnswerChanged: Boolean)
                                              (implicit request: DataRequest[AnyContent]): Future[Result] = {

    def redirect: Result = Redirect {
      newAnswer match {
        case DeclareTheTrustIsUpToDate =>
          redirectToDeclaration
        case MakeChanges =>
          redirectToFirstUpdateQuestion
        case CloseTrust if userAnswers.isTrustTaxable =>
          controllers.close.taxable.routes.DateLastAssetSharedOutYesNoController.onPageLoad()
        case CloseTrust =>
          controllers.close.nontaxable.routes.DateClosedController.onPageLoad()
        case NoLongerTaxable =>
          controllers.routes.NoTaxLiabilityInfoController.onPageLoad()
        case NeedsToPayTax if appConfig.migrateATrustEnabled =>
          controllers.transition.routes.NeedToPayTaxYesNoController.onPageLoad()
        case GeneratePdf =>
          controllers.routes.ObligedEntityPdfYesNoController.onPageLoad()
        case _ =>
          controllers.routes.FeatureNotAvailableController.onPageLoad()
      }
    }

    if (newAnswer != GeneratePdf) {
      for {
        _ <- removeTransformsIfAnswerHasChanged(hasAnswerChanged)
        _ <- trustConnector.setTaxableMigrationFlag(request.userAnswers.identifier, newAnswer == NeedsToPayTax)
      } yield redirect
    } else {
      Future.successful(redirect)
    }
  }

  private def removeTransformsIfAnswerHasChanged(hasAnswerChanged: Boolean)
                                                (implicit request: DataRequest[AnyContent]): Future[Unit] = {
    if (hasAnswerChanged) {
      logger.info(s"[Session ID: ${Session.id(hc)}] Answer has changed. Removing transforms and resetting tasks.")
      maintainATrustService.removeTransformsAndResetTaskList(request.userAnswers.identifier)
    } else {
      Future.successful(())
    }
  }

}
