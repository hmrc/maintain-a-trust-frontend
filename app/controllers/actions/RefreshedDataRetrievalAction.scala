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

package controllers.actions

import com.google.inject.{ImplementedBy, Inject}
import connectors.TrustConnector
import controllers.routes
import mapping.UserAnswersExtractor
import models.UserAnswers
import models.http.{GetTrust, Processed}
import models.requests.DataRequest
import pages.UTRPage
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, BodyParsers, Result}
import repositories.PlaybackRepository
import services.AuthenticationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class RefreshedDataRetrievalActionImpl @Inject()(val parser: BodyParsers.Default,
                                             playbackRepository: PlaybackRepository,
                                             trustConnector: TrustConnector,
                                             playbackExtractor: UserAnswersExtractor,
                                             authenticationService: AuthenticationService
                                            )(override implicit val executionContext: ExecutionContext) extends RefreshedDataRetrievalAction {

  override def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    val utr = request.userAnswers.get(UTRPage).get

    playbackRepository.resetCache(request.user.internalId).flatMap{ _ =>
      trustConnector.playback(utr) flatMap {
        case Processed(playback, _) =>
          authenticationService.authenticate(utr)(request, hc) flatMap {
            case Left(_) => Future.successful(Left(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem())))
            case Right(_) => extract(utr, playback)(request)
          }
        case _ =>
          Logger.warn(s"[RefreshedDataRetrievalAction] unable to retrieve trust due to an error")
          Future.successful(Left(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem())))
      }
    }
  }

  private def extract[A](utr: String, playback: GetTrust)(implicit request: DataRequest[A]) : Future[Either[Result, DataRequest[A]]] = {

    val newUserAnswers = UserAnswers(request.user.internalId).set(UTRPage, utr).get

    playbackExtractor.extract(newUserAnswers, playback) match {
      case Right(answers) =>
        playbackRepository.set(answers) map { _ =>
          Logger.debug(s"[RefreshedDataRetrievalAction] Set updated user answers in db")
          Right(DataRequest(request.request, answers, request.user))
        }
      case Left(reason) =>
        Logger.warn(s"[RefreshedDataRetrievalAction] unable to extract user answers due to $reason")
        Future.successful(Left(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem())))
    }
  }

}

@ImplementedBy(classOf[RefreshedDataRetrievalActionImpl])
trait RefreshedDataRetrievalAction extends ActionRefiner[DataRequest, DataRequest] {

  def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]]

}