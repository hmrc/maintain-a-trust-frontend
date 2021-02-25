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

import config.FrontendAppConfig
import connectors.{TrustConnector, TrustsStoreConnector}
import controllers.actions.Actions
import handlers.ErrorHandler
import javax.inject.Inject
import mapping.UserAnswersExtractor
import models.http._
import models.requests.DataRequest
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.PlaybackRepository
import services.AuthenticationService
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Session
import views.html.status._

import scala.concurrent.{ExecutionContext, Future}

class TrustStatusController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       playbackRepository: PlaybackRepository,
                                       actions: Actions,
                                       closedView: TrustClosedView,
                                       stillProcessingView: TrustStillProcessingView,
                                       utrDoesNotMatchView: TrustUtrDoesNotMatchView,
                                       ivDownView: IVDownView,
                                       trustConnector: TrustConnector,
                                       trustStoreConnector: TrustsStoreConnector,
                                       config: FrontendAppConfig,
                                       errorHandler: ErrorHandler,
                                       lockedView: TrustLockedView,
                                       alreadyClaimedView: TrustAlreadyClaimedView,
                                       playbackProblemContactHMRCView: PlaybackProblemContactHMRCView,
                                       playbackExtractor: UserAnswersExtractor,
                                       authenticationService: AuthenticationService,
                                       val controllerComponents: MessagesControllerComponents
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def closed(): Action[AnyContent] = actions.authWithData.async {
    implicit request =>
      Future.successful(Ok(closedView(request.user.affinityGroup, request.userAnswers.identifier)))
  }

  def processing(): Action[AnyContent] = actions.authWithData.async {
    implicit request =>
      Future.successful(Ok(stillProcessingView(request.user.affinityGroup, request.userAnswers.identifier)))
  }

  def sorryThereHasBeenAProblem(): Action[AnyContent] = actions.authWithData.async {
    implicit request =>
      Future.successful(Ok(playbackProblemContactHMRCView(request.userAnswers.identifier)))
  }

  def notFound(): Action[AnyContent] = actions.authWithData.async {
    implicit request =>
      Future.successful(Ok(utrDoesNotMatchView(request.user.affinityGroup)))
  }

  def locked(): Action[AnyContent] = actions.authWithData.async {
    implicit request =>
      Future.successful(Ok(lockedView(request.userAnswers.identifier, request.userAnswers.identifierType)))
  }

  def down(): Action[AnyContent] = actions.authWithData.async {
    implicit request =>
      Future.successful(ServiceUnavailable(ivDownView(request.userAnswers.identifier, request.userAnswers.identifierType)))
  }

  def alreadyClaimed(): Action[AnyContent] = actions.authWithData.async {
    implicit request =>
      Future.successful(Ok(alreadyClaimedView(request.userAnswers.identifier, request.userAnswers.identifierType)))
  }

  def status(): Action[AnyContent] = actions.authWithData.async {
    implicit request =>
      checkIfLocked(request.userAnswers.identifier, fromVerify = false )
  }

  def statusAfterVerify(): Action[AnyContent] = actions.authWithData.async {
    implicit request =>
      checkIfLocked(request.userAnswers.identifier, fromVerify = true)
  }

  private def checkIfLocked(identifier: String, fromVerify: Boolean)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    trustStoreConnector.get(identifier).flatMap {
      case Some(claim) if claim.trustLocked =>
        logger.info(s"[Session ID: ${Session.id(hc)}] $identifier user has failed IV 3 times, locked out for 30 minutes")
        Future.successful(Redirect(controllers.routes.TrustStatusController.locked()))
      case _ =>
        logger.info(s"[Session ID: ${Session.id(hc)}] $identifier user has not been locked out from IV")
        tryToPlayback(identifier, fromVerify)
    }
  }

  private def tryToPlayback(identifier: String, fromVerify: Boolean)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    trustConnector.playbackFromEtmp(identifier) flatMap {
      case Closed =>
        logger.info(s"[tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due it being closed")
        Future.successful(Redirect(controllers.routes.TrustStatusController.closed()))
      case Processing =>
        logger.info(s"[tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due to trust change processing")
        Future.successful(Redirect(controllers.routes.TrustStatusController.processing()))
      case IdentifierNotFound =>
        logger.info(s"[tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due to UTR/URN not found")
        Future.successful(Redirect(controllers.routes.TrustStatusController.notFound()))
      case Processed(playback, _) =>
        authenticateForIdentifierAndExtract(identifier, playback, fromVerify)
      case SorryThereHasBeenAProblem =>
        logger.warn(s"[tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due to status")
        Future.successful(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem()))
      case TrustServiceUnavailable =>
        logger.warn(s"[tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due to the service being unavailable")
        Future.successful(Redirect(routes.TrustStatusController.down()))
      case ClosedRequestResponse =>
        logger.warn(s"[tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due to the service closing the request")
        Future.successful(Redirect(routes.TrustStatusController.down()))
      case response =>
        logger.warn(s"[tryToPlayback][Session ID: ${Session.id(hc)}] $identifier unable to retrieve trust due to an error ${response}")
        Future.successful(Redirect(routes.TrustStatusController.down()))
    }
  }

  private def authenticateForIdentifierAndExtract(identifier: String, playback : GetTrust, fromVerify: Boolean)
                                          (implicit request: DataRequest[AnyContent]) = {
    logger.info(s"[tryToPlayback][Session ID: ${Session.id(hc)}] $identifier trust is in a processed state")
    authenticationService.authenticateForIdentifier(identifier) flatMap {
      case Left(failure) =>
        val location = failure.header.headers.getOrElse(LOCATION, "no location header")
        val failureStatus = failure.header.status
        logger.info(s"[tryToPlayback][Session ID: ${Session.id(hc)}] unable to authenticate user for $identifier, " +
          s"due to $failureStatus status, sending user to $location")

        Future.successful(failure)
      case Right(_) =>
        extract(identifier, playback, fromVerify)
    }
  }

  private def extract(identifier: String, playback: GetTrust, fromVerify: Boolean)
                     (implicit request: DataRequest[AnyContent]) : Future[Result] = {

    logger.info(s"[extract][Session ID: ${Session.id(hc)}] user authenticated for $identifier, attempting to extract to user answers")

    playbackExtractor.extract(request.userAnswers, playback) match {
      case Right(answers) =>
        playbackRepository.set(answers) map { _ =>
          logger.info(s"[extract][Session ID: ${Session.id(hc)}] $identifier successfully extracted, showing information about maintaining")
          (request.user.affinityGroup, fromVerify) match {
            case (AffinityGroup.Organisation, false) => Redirect(routes.MaintainThisTrustController.onPageLoad(needsIv = false))
            case (_,_) => Redirect(routes.InformationMaintainingThisTrustController.onPageLoad())
          }
        }
      case Left(reason) =>
        logger.warn(s"[extract][Session ID: ${Session.id(hc)}] $identifier unable to extract user answers due to $reason")
        Future.successful(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem()))
    }
  }

}
