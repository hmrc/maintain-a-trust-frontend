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

import config.FrontendAppConfig
import connectors.{TrustConnector, TrustsStoreConnector}
import controllers.actions.Actions
import mapping.UserAnswersExtractor
import models.{TrustDetails, Underlying4mldTrustIn5mldMode, UserAnswers}
import models.http._
import models.requests.{DataRequest, OptionalDataRequest}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.PlaybackRepository
import services.{AuthenticationService, SessionService}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Session
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
                                       frontendAppConfig: FrontendAppConfig
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

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
    trustStoreConnector.get(identifier).flatMap {
      case Some(claim) if claim.trustLocked =>
        logger.warn(s"[TrustStatusController][checkIfLocked][Session ID: ${Session.id(hc)}] $identifier user has failed IV 3 times, locked out for 30 minutes")
        Future.successful(Redirect(controllers.routes.TrustStatusController.locked()))
      case _ =>
        logger.info(s"[TrustStatusController][checkIfLocked][Session ID: ${Session.id(hc)}] $identifier user has not been locked out from IV")
        tryToPlayback(identifier, fromVerify)
    }
  }

  private def tryToPlayback(identifier: String, fromVerify: Boolean)(implicit request: OptionalDataRequest[AnyContent]): Future[Result] = {
    trustConnector.playbackFromEtmp(identifier) flatMap {
      case Closed =>
        logger.info(s"[TrustStatusController][tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due it being closed")
        Future.successful(Redirect(controllers.routes.TrustStatusController.closed()))
      case Processing =>
        logger.info(s"[TrustStatusController][tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due to trust change processing")
        Future.successful(Redirect(controllers.routes.TrustStatusController.processing()))
      case IdentifierNotFound =>
        logger.info(s"[TrustStatusController][tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due to UTR/URN not found")
        Future.successful(Redirect(controllers.routes.TrustStatusController.notFound()))
      case Processed(playback, _) =>
        authenticateForIdentifierAndExtract(identifier, playback, fromVerify)
      case SorryThereHasBeenAProblem =>
        logger.warn(s"[TrustStatusController][tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due no content returned")
        Future.successful(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem()))
      case TrustServiceUnavailable =>
        logger.error(s"[TrustStatusController][tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due to the service being unavailable")
        Future.successful(Redirect(routes.TrustStatusController.down()))
      case ClosedRequestResponse =>
        logger.error(s"[TrustStatusController][tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due to the connection timing out")
        Future.successful(Redirect(routes.TrustStatusController.down()))
      case TrustsErrorResponse(status) =>
        logger.error(s"[TrustStatusController][tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due to an unexpected response $status")
        Future.successful(Redirect(routes.TrustStatusController.down()))
      case response =>
        logger.error(s"[TrustStatusController][tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due to an error $response")
        Future.successful(Redirect(routes.TrustStatusController.down()))
    }
  }

  private def authenticateForIdentifierAndExtract(identifier: String, playback: GetTrust, fromVerify: Boolean)
                                          (implicit request: OptionalDataRequest[AnyContent]): Future[Result] = {
    val userAnswersF = for {
      trustDetails <- trustConnector.getUntransformedTrustDetails(identifier)
      userAnswers <- sessionService.initialiseUserAnswers(
        identifier = identifier,
        internalId = request.user.internalId,
        isUnderlyingData5mld = trustDetails.is5mld,
        isUnderlyingDataTaxable = trustDetails.isTaxable
      )
    } yield (userAnswers, trustDetails)

    userAnswersF.flatMap { userAnswers =>

      val dataRequest = DataRequest(request.request, userAnswers._1, request.user)
      authenticationService.authenticateForIdentifier(identifier)(dataRequest, hc).flatMap {
        case Left(failure) =>
          val location = failure.header.headers.getOrElse(LOCATION, "no location header")
          val failureStatus = failure.header.status

          logger.warn(s"[TrustStatusController][authenticateForIdentifierAndExtract][Session ID: ${Session.id(hc)}]" +
            s" unable to authenticate user for $identifier, " +
            s"due to $failureStatus status, sending user to $location")

          Future.successful(failure)
        case Right(_) =>
          extract(userAnswers._1, identifier, playback, fromVerify, userAnswers._2)
      }
    } recoverWith {
      case ex: Throwable =>
        logger.error(s"[TrustStatusController][authenticateForIdentifierAndExtract][Session ID: ${Session.id(hc)}]" +
          s"authentication and extraction failed due to ${ex.getMessage}", ex
        )
        Future.successful(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem()))
    }
  }

  private def extract(userAnswers: UserAnswers,
                      identifier: String,
                      playback: GetTrust,
                      fromVerify: Boolean,
                      trustDetails : TrustDetails
                     )
                     (implicit request: OptionalDataRequest[AnyContent]): Future[Result] = {
    logger.info(s"[TrustStatusController][extract][Session ID: ${Session.id(hc)}] user authenticated for $identifier, attempting to extract to user answers")

    playbackExtractor.extract(userAnswers, playback) flatMap {
      case Right(answers) =>
        playbackRepository.set(answers) map { _ =>
          logger.info(s"[TrustStatusController][extract][Session ID: ${Session.id(hc)}] $identifier successfully extracted, showing information about maintaining")
          routeAfterExtraction(userAnswers, fromVerify, trustDetails)
        }
      case Left(reason) =>
        logger.warn(s"[TrustStatusController][extract][Session ID: ${Session.id(hc)}] $identifier unable to extract user answers due to $reason")
        Future.successful(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem()))
    }
  }

  private def routeAfterExtraction[A](answers: UserAnswers, fromVerify: Boolean, trustDetails: TrustDetails)
                                     (implicit request: OptionalDataRequest[A]): Result = {

    def askSchedule3aQuestion: Boolean =
      frontendAppConfig.schedule3aExemptEnabled && !trustDetails.hasSchedule3aExemptAnswer && trustDetails.isTaxable && trustDetails.isExpress

    if (answers.trustMldStatus == Underlying4mldTrustIn5mldMode) {
      logger.info(s"[TrustStatusController][routeAfterExtraction][Session ID: ${Session.id(hc)}] underlying data is 4MLD. " +
        s"Need to answer express-trust question.")
      Redirect(controllers.routes.MigrateTo5mldInformationController.onPageLoad())
    } else if (askSchedule3aQuestion) {
      logger.info(s"[TrustStatusController][routeAfterExtraction][Session ID: ${Session.id(hc)}] User has not answered Schedule3a question " +
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
