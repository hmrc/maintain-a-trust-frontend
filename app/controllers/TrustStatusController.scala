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
        checkIfLocked(utr)
      }
  }

  private def checkIfLocked(utr: String)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    trustStoreConnector.get(request.userAnswers.internalAuthId, utr).flatMap {
      case Some(claim) if claim.trustLocked =>
        Future.successful(Redirect(controllers.routes.TrustStatusController.locked()))
      case _ =>
        tryToPlayback(utr)
    }
  }

  private def tryToPlayback(utr: String)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    trustConnector.playbackfromEtmp(utr) flatMap {
      case Closed =>
        Logger.info(s"[TrustStatusController][tryToPlayback] unable to retrieve trust due it being closed")
        Future.successful(Redirect(controllers.routes.TrustStatusController.closed()))
      case Processing =>
        Logger.info(s"[TrustStatusController][tryToPlayback] unable to retrieve trust due to trust change processing")
        Future.successful(Redirect(controllers.routes.TrustStatusController.processing()))
      case UtrNotFound =>
        Logger.info(s"[TrustStatusController][tryToPlayback] unable to retrieve trust due to UTR not found")
        Future.successful(Redirect(controllers.routes.TrustStatusController.notFound()))
      case Processed(playback, _) =>
        authenticationService.authenticate(utr) flatMap {
          case Left(failure) => Future.successful(failure)
          case Right(_) => extract(utr, playback)
        }
      case SorryThereHasBeenAProblem =>
        Logger.warn(s"[TrustStatusController][tryToPlayback] unable to retrieve trust due to status")
        Future.successful(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem()))
      case _ =>
        Logger.warn(s"[TrustStatusController][tryToPlayback] unable to retrieve trust due to an error")
        Future.successful(Redirect(routes.TrustStatusController.down()))
    }
  }

  private def extract(utr: String, playback: GetTrust)(implicit request: DataRequest[AnyContent]) : Future[Result] = {

    Logger.debug(s"[TrustStatusController] unpacking the following trust ${Json.stringify(Json.toJson(playback))}")

    playbackExtractor.extract(request.userAnswers, playback) match {
      case Right(answers) =>
        playbackRepository.set(answers) map { _ =>
          Redirect(routes.InformationMaintainingThisTrustController.onPageLoad())
        }
      case Left(reason) =>
        Logger.warn(s"[TrustStatusController] unable to extract user answers due to $reason")
        Future.successful(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem()))
    }
  }

  private def enforceUtr()(block: String => Future[Result])(implicit request: DataRequest[AnyContent]): Future[Result] = {
    request.userAnswers.get(UTRPage) match {
      case None => Future.successful(Redirect(routes.UTRController.onPageLoad()))
      case Some(utr) => block(utr)
    }
  }

}
