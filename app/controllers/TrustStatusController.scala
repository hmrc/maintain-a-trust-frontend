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

import config.FrontendAppConfig
import connectors.{TrustConnector, TrustsStoreConnector}
import controllers.actions.Actions
import handlers.ErrorHandler
import mapping.UserAnswersExtractor
import models.errors.TrustErrorWithRedirect
import models.http._
import models.requests.{DataRequest, OptionalDataRequest}
import models.{TrustDetails, Underlying4mldTrustIn5mldMode, UserAnswers}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.PlaybackRepository
import services.{AuthenticationService, SessionService}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Session, TrustEnvelope}
import views.html.status._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TrustStatusController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       playbackRepository: PlaybackRepository,
                                       actions: Actions,
                                       closedView: TrustClosedView,
                                       stillProcessingView: TrustStillProcessingView,
                                       identifierDoesNotMatchView: IdentifierDoesNotMatchView,
                                       ivDownView: IVDownView,
                                       trustConnector: TrustConnector,
                                       trustStoreConnector: TrustsStoreConnector,
                                       lockedView: TrustLockedView,
                                       alreadyClaimedView: TrustAlreadyClaimedView,
                                       playbackProblemContactHMRCView: PlaybackProblemContactHMRCView,
                                       playbackExtractor: UserAnswersExtractor,
                                       authenticationService: AuthenticationService,
                                       val controllerComponents: MessagesControllerComponents,
                                       sessionService: SessionService,
                                       frontendAppConfig: FrontendAppConfig,
                                       errorHandler: ErrorHandler
                                     ) (implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  private val className = getClass.getSimpleName

  def closed(): Action[AnyContent] = actions.authWithOptionalData.async {
    implicit request =>
      Future.successful(Ok(closedView(request.user.affinityGroup, request.identifier, request.identifierType)))
  }

  def processing(): Action[AnyContent] = actions.authWithOptionalData.async {
    implicit request =>
      Future.successful(Ok(stillProcessingView(request.user.affinityGroup, request.identifier, request.identifierType)))
  }

  def sorryThereHasBeenAProblem(): Action[AnyContent] = actions.authWithOptionalData.async {
    implicit request =>
      Future.successful(Ok(playbackProblemContactHMRCView(request.identifier, request.identifierType)))
  }

  def notFound(): Action[AnyContent] = actions.authWithOptionalData.async {
    implicit request =>
      Future.successful(Ok(identifierDoesNotMatchView(request.user.affinityGroup, request.identifier, request.identifierType)))
  }

  def locked(): Action[AnyContent] = actions.authWithOptionalData.async {
    implicit request =>
      Future.successful(Ok(lockedView(request.identifier, request.identifierType)))
  }

  def down(): Action[AnyContent] = actions.authWithOptionalData.async {
    implicit request =>
      Future.successful(ServiceUnavailable(ivDownView(request.identifier, request.identifierType)))
  }

  def alreadyClaimed(): Action[AnyContent] = actions.authWithOptionalData.async {
    implicit request =>
      Future.successful(Ok(alreadyClaimedView(request.identifier, request.identifierType)))
  }

  def status(): Action[AnyContent] = actions.authWithOptionalData.async {
    implicit request =>
      checkIfLocked(request.identifier, fromVerify = false)
  }

  def statusAfterVerify(): Action[AnyContent] = actions.authWithOptionalData.async {
    implicit request =>
      checkIfLocked(request.identifier, fromVerify = true)
  }

  private def checkIfLocked(identifier: String, fromVerify: Boolean)(implicit request: OptionalDataRequest[AnyContent]): Future[Result] = {
    trustStoreConnector.get(identifier).value.flatMap {
      case Right(Some(claim)) if claim.trustLocked =>
        logger.warn(s"[$className][checkIfLocked][Session ID: ${Session.id(hc)}] $identifier user has failed IV 3 times, locked out for 30 minutes")
        Future.successful(Redirect(controllers.routes.TrustStatusController.locked()))
      case Right(_) =>
        logger.info(s"[$className][checkIfLocked][Session ID: ${Session.id(hc)}] $identifier user has not been locked out from IV")
        tryToPlayback(identifier, fromVerify)
      case Left(_) => logger.warn(s"[$className][checkIfLocked][Session ID: ${Session.id(hc)}] Errors from connector call.")
//        Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
        errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
    }
  }

  private def tryToPlayback(identifier: String, fromVerify: Boolean)(implicit request: OptionalDataRequest[AnyContent]): Future[Result] = {
    trustConnector.playbackFromEtmp(identifier).value.flatMap {
      case Right(Closed) =>
        logger.info(s"[$className][tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due it being closed")
        Future.successful(Redirect(controllers.routes.TrustStatusController.closed()))
      case Right(Processing) =>
        logger.info(s"[$className][tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due to trust change processing")
        Future.successful(Redirect(controllers.routes.TrustStatusController.processing()))
      case Right(IdentifierNotFound) =>
        logger.info(s"[$className][tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due to UTR/URN not found")
        Future.successful(Redirect(controllers.routes.TrustStatusController.notFound()))
      case Right(Processed(playback, _)) =>
        authenticateForIdentifierAndExtract(identifier, playback, fromVerify)
      case Right(SorryThereHasBeenAProblem) =>
        logger.warn(s"[$className][tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due no content returned")
        Future.successful(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem()))
      case Right(TrustServiceUnavailable) =>
        logger.error(s"[$className][tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due to the service being unavailable")
        Future.successful(Redirect(routes.TrustStatusController.down()))
      case Right(ClosedRequestResponse) =>
        logger.error(s"[$className][tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due to the connection timing out")
        Future.successful(Redirect(routes.TrustStatusController.down()))
      case Right(TrustsErrorResponse(status)) =>
        logger.error(s"[$className][tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due to an unexpected response $status")
        Future.successful(Redirect(routes.TrustStatusController.down()))
      case Left(response) =>
        logger.error(s"[$className][tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due to an error $response")
        Future.successful(Redirect(routes.TrustStatusController.down()))
    }
  }

  private def authenticateForIdentifierAndExtract(identifier: String, playback: GetTrust, fromVerify: Boolean)
                                                 (implicit request: OptionalDataRequest[AnyContent]): Future[Result] = {
    val expectedResult = for {
      trustDetails <- trustConnector.getUntransformedTrustDetails(identifier)
      userAnswers <- sessionService.initialiseUserAnswers(
        identifier = identifier,
        internalId = request.user.internalId,
        isUnderlyingData5mld = trustDetails.is5mld,
        isUnderlyingDataTaxable = trustDetails.isTaxable
      )
      dataRequest = DataRequest(request.request, userAnswers, request.user)
      _ <- TrustEnvelope.fromLeftResult(authenticationService.authenticateForIdentifier(identifier)(dataRequest, hc))
      resultFromExtractedUserAnswer <- TrustEnvelope.fromFuture(extract(userAnswers, identifier, playback, fromVerify, trustDetails))
    } yield resultFromExtractedUserAnswer

    expectedResult.value.map {
      case Right(route) => route
      case Left(TrustErrorWithRedirect(failureResult)) =>

        val location = failureResult.header.headers.getOrElse(LOCATION, "no location header")
        val failureStatus = failureResult.header.status

        logger.warn(s"[$className][authenticateForIdentifierAndExtract][Session ID: ${Session.id(hc)}]" +
          s" unable to authenticate user for $identifier, " + s"due to $failureStatus status, sending user to $location")

        failureResult

      case Left(_) =>
        logger.warn(s"[$className][authenticateForIdentifierAndExtract][Session ID: ${Session.id(hc)}]" + "authentication and extraction failed.")
        Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem())
    }
  }


  private def extract(userAnswers: UserAnswers,
                      identifier: String,
                      playback: GetTrust,
                      fromVerify: Boolean,
                      trustDetails: TrustDetails
                     )
                     (implicit request: OptionalDataRequest[AnyContent]): Future[Result] = {
    logger.info(s"[$className][extract][Session ID: ${Session.id(hc)}] user authenticated for $identifier, attempting to extract to user answers")

    playbackExtractor.extract(userAnswers, playback).value flatMap {
      case Right(answers) =>
        playbackRepository.set(answers).value map { _ =>
          logger.info(s"[$className][extract][Session ID: ${Session.id(hc)}] $identifier successfully extracted, showing information about maintaining")
          routeAfterExtraction(userAnswers, fromVerify, trustDetails)
        }
      case Left(reason) =>
        logger.warn(s"[$className][extract][Session ID: ${Session.id(hc)}] $identifier unable to extract user answers due to $reason")
        Future.successful(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem()))
    }
  }

  private def routeAfterExtraction[A](answers: UserAnswers, fromVerify: Boolean, trustDetails: TrustDetails)
                                     (implicit request: OptionalDataRequest[A]): Result = {

    def askSchedule3aQuestion: Boolean =
      frontendAppConfig.schedule3aExemptEnabled && !trustDetails.hasSchedule3aExemptAnswer && trustDetails.isTaxable && trustDetails.isExpress

    if (answers.trustMldStatus == Underlying4mldTrustIn5mldMode) {
      logger.info(s"[$className][routeAfterExtraction][Session ID: ${Session.id(hc)}] underlying data is 4MLD. " +
        s"Need to answer express-trust question.")
      Redirect(controllers.routes.MigrateTo5mldInformationController.onPageLoad())
    } else if (askSchedule3aQuestion) {
      logger.info(s"[$className][routeAfterExtraction][Session ID: ${Session.id(hc)}] User has not answered Schedule3a question " +
        s"redirecting to question page")
      Redirect(controllers.routes.InformationSchedule3aExemptionController.onPageLoad())
    } else {
      (request.user.affinityGroup, fromVerify) match {
        case (AffinityGroup.Organisation, false) =>
          Redirect(routes.MaintainThisTrustController.onPageLoad(needsIv = false))
        case _ =>
          Redirect(routes.InformationMaintainingThisTrustController.onPageLoad())
      }
    }
  }

}
