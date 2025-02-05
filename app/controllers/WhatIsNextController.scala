/*
 * Copyright 2024 HM Revenue & Customs
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
import handlers.ErrorHandler
import models.UserAnswers
import models.errors.{FormValidationError, TrustErrors}
import models.pages.WhatIsNext
import models.pages.WhatIsNext._
import models.requests.DataRequest
import pages.WhatIsNextPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc._
import repositories.PlaybackRepository
import services.MaintainATrustService
import utils.TrustEnvelope.TrustEnvelope
import utils.{Session, TrustEnvelope}
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
                                      trustConnector: TrustConnector,
                                      trustsStoreConnector: TrustsStoreConnector,
                                      maintainATrustService: MaintainATrustService,
                                      appConfig: FrontendAppConfig,
                                      errorHandler: ErrorHandler
                                    ) (implicit ec: ExecutionContext)
  extends MakeChangesQuestionRouterController(trustConnector, trustsStoreConnector) with Logging {

  private val className = getClass.getSimpleName
  private val form: Form[WhatIsNext] = formProvider()

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

      val result = for {
        formData <- TrustEnvelope(handleFormValidation)
        hasAnswerChanged <- TrustEnvelope(!request.userAnswers.get(WhatIsNextPage).contains(formData))
        updatedAnswers <- TrustEnvelope(request.userAnswers.set(WhatIsNextPage, formData))
        _ <- playbackRepository.set(updatedAnswers)
        redirectRoute <- updateMigrationStatusAndRedirect(updatedAnswers, formData, hasAnswerChanged)
      } yield {
        redirectRoute
      }

      result.value.flatMap {
        case Right(call) => Future.successful(call)
        case Left(FormValidationError(formBadRequest)) => Future.successful(formBadRequest)
        case Left(_) =>
          logger.warn(s"[$className][onSubmit][Session ID: ${Session.id(hc)}] Error while storing user answers")
          errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      }
  }

  private def updateMigrationStatusAndRedirect(userAnswers: UserAnswers, newAnswer: WhatIsNext, hasAnswerChanged: Boolean)
                                              (implicit request: DataRequest[AnyContent]): TrustEnvelope[Result] = {

    def redirect: Result = Redirect {
      newAnswer match {
        case DeclareTheTrustIsUpToDate =>
          redirectToDeclaration
        case MakeChanges =>
          redirectToFirstUpdateQuestion
        case CloseTrust =>
          redirectsForClosedTrust(userAnswers)
        case NoLongerTaxable =>
          controllers.routes.NoTaxLiabilityInfoController.onPageLoad()
        case NeedsToPayTax if appConfig.migrateATrustEnabled =>
          controllers.transition.routes.
            NeedToPayTaxYesNoController.onPageLoad()
        case GeneratePdf =>
          controllers.routes.ObligedEntityPdfYesNoController.onPageLoad()
        case _ =>
          controllers.routes.FeatureNotAvailableController.onPageLoad()
      }
    }

    if (newAnswer != GeneratePdf) {
      for {
        _ <- TrustEnvelope(removeTransformsIfAnswerHasChanged(hasAnswerChanged))
        _ <- trustConnector.setTaxableMigrationFlag(request.userAnswers.identifier, newAnswer == NeedsToPayTax)
      } yield redirect
    } else {
      TrustEnvelope(redirect)
    }
  }

  private def removeTransformsIfAnswerHasChanged(hasAnswerChanged: Boolean)
                                                (implicit request: DataRequest[AnyContent]): TrustEnvelope[Unit] = {
    if (hasAnswerChanged) {
      logger.info(s"[$className][removeTransformsIfAnswerHasChanged][Session ID: ${Session.id(hc)}] Answer has changed. Removing transforms and resetting tasks.")
      maintainATrustService.removeTransformsAndResetTaskList(request.userAnswers.identifier)
    } else {
      TrustEnvelope(())
    }
  }

  private def handleFormValidation(implicit request: DataRequest[AnyContent]): Either[TrustErrors, WhatIsNext] = {
    form.bindFromRequest().fold(
      (formWithErrors: Form[_]) =>
        Left(FormValidationError(BadRequest(view(formWithErrors, request.userAnswers.trustMldStatus)))),
      value => Right(value)
    )
  }

  protected def redirectsForClosedTrust(userAnswers: UserAnswers): Call = {
    if(userAnswers.isTrustTaxable) {
      controllers.close.taxable.routes.DateLastAssetSharedOutYesNoController.onPageLoad()
    } else {
      controllers.close.nontaxable.routes.DateClosedController.onPageLoad()
    }
  }

}
