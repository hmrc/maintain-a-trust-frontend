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

package controllers.actions

import com.google.inject.{ImplementedBy, Inject}
import connectors.TrustConnector
import controllers.routes
import mapping.UserAnswersExtractor
import models.http.{GetTrust, Processed}
import models.pages.WhatIsNext
import models.requests.DataRequest
import models.{AgentDeclaration, UserAnswers}
import pages.close.DateLastAssetSharedOutPage
import pages.declaration.AgentDeclarationPage
import pages.{SubmissionDatePage, TVNPage, WhatIsNextPage}
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, BodyParsers, Result}
import repositories.PlaybackRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import utils.Session

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.{ExecutionContext, Future}

class RefreshedDataRetrievalActionImpl @Inject()(
                                                  val parser: BodyParsers.Default,
                                                  playbackRepository: PlaybackRepository,
                                                  trustConnector: TrustConnector,
                                                  playbackExtractor: UserAnswersExtractor
                                                )(override implicit val executionContext: ExecutionContext) extends RefreshedDataRetrievalAction with Logging {

  case class SubmissionData(identifier: String,
                            whatIsNext: WhatIsNext,
                            tvn: String,
                            date: LocalDateTime,
                            agent: Option[AgentDeclaration],
                            endDate: Option[LocalDate])

  override def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    (for {
      whatIsNext <- request.userAnswers.get(WhatIsNextPage)
      tvn <- request.userAnswers.get(TVNPage)
      submissionDate <- request.userAnswers.get(SubmissionDatePage)
      optionalAgentInformation = request.userAnswers.get(AgentDeclarationPage)
      optionalEndDate = request.userAnswers.get(DateLastAssetSharedOutPage)
    } yield {

      val identifier = request.userAnswers.identifier

      val submissionData = SubmissionData(identifier, whatIsNext, tvn, submissionDate, optionalAgentInformation, optionalEndDate)

      trustConnector.playback(identifier).flatMap {
        case Processed(playback, _) => extractAndRefreshUserAnswers(submissionData, identifier, playback)(request)
        case _ => Future.successful(Left(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem())))
      }
    }).getOrElse {
      logger.error(s"[Session ID: ${Session.id(hc)}][UTR/URN: ${request.userAnswers.identifier}] unable to get data from user answers")
      Future.successful(Left(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem())))
    }
  }

  private def extractAndRefreshUserAnswers[A](data: SubmissionData, identifier: String, playback: GetTrust)
                                             (implicit request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSessionAndRequest(request.headers, Some(request.session), Some(request))

    val newSession = UserAnswers.startNewSession(request.user.internalId, identifier)

    playbackExtractor.extract(newSession, playback) flatMap {
      case Right(answers) =>
        for {
          updatedAnswers <- Future.fromTry {
            answers
              .set(WhatIsNextPage, data.whatIsNext)
              .flatMap(_.set(TVNPage, data.tvn))
              .flatMap(_.set(SubmissionDatePage, data.date))
              .flatMap(_.set(AgentDeclarationPage, data.agent))
              .flatMap(_.set(DateLastAssetSharedOutPage, data.endDate))
          }
          _ <- playbackRepository.set(updatedAnswers)
        } yield {
          logger.debug(s"[Session ID: ${Session.id(hc)}][UTR/URN: ${answers.identifier}] Set updated user answers in db")
          Right(DataRequest(request.request, updatedAnswers, request.user))
        }
      case Left(reason) =>
        logger.warn(s"[Session ID: ${Session.id(hc)}] unable to extract user answers due to $reason")
        Future.successful(Left(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem())))
    }
  }

}

@ImplementedBy(classOf[RefreshedDataRetrievalActionImpl])
trait RefreshedDataRetrievalAction extends ActionRefiner[DataRequest, DataRequest] {

  def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]]

}