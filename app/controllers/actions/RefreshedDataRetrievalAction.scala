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

import java.time.LocalDateTime

import com.google.inject.{ImplementedBy, Inject}
import connectors.TrustConnector
import controllers.routes
import mapping.UserAnswersExtractor
import models.http.{GetTrust, Processed}
import models.requests.DataRequest
import models.{AgentDeclaration, UserAnswers}
import pages.declaration.AgentDeclarationPage
import pages.{SubmissionDatePage, TVNPage, UTRPage}
import play.api.Logger
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

  case class SubmissionData(utr: String, tvn: String, date: LocalDateTime, agent: Option[AgentDeclaration])

  override def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    (for {
      utr <- request.userAnswers.get(UTRPage)
      tvn <- request.userAnswers.get(TVNPage)
      submissionDate <- request.userAnswers.get(SubmissionDatePage)
      optionalAgentInformation = request.userAnswers.get(AgentDeclarationPage)
    } yield {

      val submissionData = SubmissionData(utr, tvn, submissionDate, optionalAgentInformation)

      trustConnector.playback(utr).flatMap {
        case Processed(playback, _) => extract(submissionData, playback)(request)
        case _ => Future.successful(Left(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem())))
      }
    }).getOrElse {
            Logger.error(s"[RefreshedDataRetrievalAction] unable to get data from user answers")
            Future.successful(Left(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem())))
    }
  }

  private def extract[A](data: SubmissionData, playback: GetTrust)
                        (implicit request: DataRequest[A]) : Future[Either[Result, DataRequest[A]]] = {

    playbackExtractor.extract(UserAnswers(request.user.internalId), playback) match {
      case Right(answers) =>
        for {
          updatedAnswers <- Future.fromTry {
            answers.set(UTRPage, data.utr)
              .flatMap(_.set(TVNPage, data.tvn))
              .flatMap(_.set(SubmissionDatePage, data.date))
              .flatMap(_.set(AgentDeclarationPage, data.agent))
          }
          _ <- playbackRepository.set(updatedAnswers)
        } yield {
          Logger.debug(s"[RefreshedDataRetrievalAction] Set updated user answers in db")
          Right(DataRequest(request.request, updatedAnswers, request.user))
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