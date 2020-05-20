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
import connectors.{TrustConnector, TrustsStoreConnector}
import controllers.actions.AuthenticateForPlayback
import handlers.ErrorHandler
import javax.inject.Inject
import mapping.UserAnswersExtractor
import models.http._
import models.requests.DataRequest
import pages.UTRPage
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.PlaybackRepository
import services.AuthenticationService
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.status._

import scala.concurrent.{ExecutionContext, Future}

class TrustStatusController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       playbackRepository: PlaybackRepository,
                                       actions: AuthenticateForPlayback,
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
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def closed(): Action[AnyContent] = actions.authWithData.async {
    implicit request =>
      enforceUtr() { utr =>
        Future.successful(Ok(closedView(request.user.affinityGroup, utr)))
      }
  }

  def processing(): Action[AnyContent] = actions.authWithData.async {
    implicit request =>
      enforceUtr() { utr =>
        Future.successful(Ok(stillProcessingView(request.user.affinityGroup, utr)))
      }
  }

  def sorryThereHasBeenAProblem(): Action[AnyContent] = actions.authWithData.async {
    implicit request =>
      enforceUtr() { utr =>
        Future.successful(Ok(playbackProblemContactHMRCView(utr)))
      }
  }

  def notFound(): Action[AnyContent] = actions.authWithData.async {
    implicit request =>
      enforceUtr() { _ =>
        Future.successful(Ok(utrDoesNotMatchView(request.user.affinityGroup)))
      }
  }

  def locked(): Action[AnyContent] = actions.authWithData.async {
    implicit request =>
      enforceUtr() { utr =>
        Future.successful(Ok(lockedView(utr)))
      }
  }

  def down(): Action[AnyContent] = actions.authWithData.async {
    implicit request =>
      enforceUtr() { _ =>
        Future.successful(ServiceUnavailable(ivDownView(request.user.affinityGroup)))
      }
  }

  def alreadyClaimed(): Action[AnyContent] = actions.authWithData.async {
    implicit request =>
      enforceUtr() { utr =>
        Future.successful(Ok(alreadyClaimedView(utr)))
      }
  }

  def status(): Action[AnyContent] = actions.authWithData.async {
    implicit request =>
      enforceUtr() { utr =>
        checkIfLocked(utr, routes.MaintainThisTrustController.onPageLoad(needsIv = false).url)
      }
  }

  def statusAfterVerify(): Action[AnyContent] = actions.authWithData.async {
    implicit request =>
    enforceUtr() { utr =>
        checkIfLocked(utr, routes.InformationMaintainingThisTrustController.onPageLoad().url)
      }
  }

  private def checkIfLocked(utr: String, orgContinueUrl: String)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    trustStoreConnector.get(utr).flatMap {
      case Some(claim) if claim.trustLocked =>
        Logger.info(s"[TrustStatusController] $utr user has failed IV 3 times, locked out for 30 minutes")
        Future.successful(Redirect(controllers.routes.TrustStatusController.locked()))
      case _ =>
        Logger.info(s"[TrustStatusController] $utr user has not been locked out from IV")
        tryToPlayback(utr, orgContinueUrl)
    }
  }

  private def tryToPlayback(utr: String, orgContinueUrl: String)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    trustConnector.playbackfromEtmp(utr) flatMap {
      case Closed =>
        Logger.info(s"[TrustStatusController][tryToPlayback] $utr unable to retrieve trust due it being closed")
        Future.successful(Redirect(controllers.routes.TrustStatusController.closed()))
      case Processing =>
        Logger.info(s"[TrustStatusController][tryToPlayback] $utr unable to retrieve trust due to trust change processing")
        Future.successful(Redirect(controllers.routes.TrustStatusController.processing()))
      case UtrNotFound =>
        Logger.info(s"[TrustStatusController][tryToPlayback] $utr unable to retrieve trust due to UTR not found")
        Future.successful(Redirect(controllers.routes.TrustStatusController.notFound()))
      case Processed(playback, _) =>
        authenticateForUtrAndExtract(utr, playback, orgContinueUrl)
      case SorryThereHasBeenAProblem =>
        Logger.warn(s"[TrustStatusController][tryToPlayback] $utr unable to retrieve trust due to status")
        Future.successful(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem()))
      case _ =>
        Logger.warn(s"[TrustStatusController][tryToPlayback] $utr unable to retrieve trust due to an error")
        Future.successful(Redirect(routes.TrustStatusController.down()))
    }
  }

  private def authenticateForUtrAndExtract(utr: String, playback : GetTrust, orgContinueUrl: String)
                                          (implicit request: DataRequest[AnyContent]) = {
    Logger.info(s"[TrustStatusController][tryToPlayback] $utr trust is in a processed state")
    authenticationService.authenticateForUtr(utr) flatMap {
      case Left(failure) =>
        val location = failure.header.headers.getOrElse(LOCATION, "no location header")
        val failureStatus = failure.header.status
        Logger.info(s"[TrustStatusController][tryToPlayback] unable to authenticate user for $utr, " +
          s"due to $failureStatus status, sending user to $location")

        Future.successful(failure)
      case Right(_) =>
        extract(utr, playback, orgContinueUrl)
    }
  }

  private def extract(utr: String, playback: GetTrust, orgContinueUrl: String)
                     (implicit request: DataRequest[AnyContent]) : Future[Result] = {

    Logger.info(s"[TrustStatusController][extract] user authenticated for $utr, attempting to extract to user answers")

    playbackExtractor.extract(request.userAnswers, playback) match {
      case Right(answers) =>
        playbackRepository.set(answers) map { _ =>
          Logger.info(s"[TrustStatusController][extract] $utr successfully extracted, showing information about maintaining")
          if (request.user.affinityGroup == AffinityGroup.Organisation) {
            Redirect(orgContinueUrl)
          } else {
            Redirect(routes.InformationMaintainingThisTrustController.onPageLoad())
          }
        }
      case Left(reason) =>
        Logger.warn(s"[TrustStatusController] $utr unable to extract user answers due to $reason")
        Future.successful(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem()))
    }
  }

  private def enforceUtr()(block: String => Future[Result])(implicit request: DataRequest[AnyContent]): Future[Result] = {
    request.userAnswers.get(UTRPage) match {
      case None =>
        Logger.info(s"[TrustStatusController] no UTR is user answers, redirecting to ask for it")
        Future.successful(Redirect(routes.UTRController.onPageLoad()))
      case Some(utr) =>
        Logger.info(s"[TrustStatusController] checking status of trust for $utr")
        block(utr)
    }
  }

}
