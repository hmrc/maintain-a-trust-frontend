/*
 * Copyright 2023 HM Revenue & Customs
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

import cats.data.EitherT
import com.google.inject.{ImplementedBy, Inject}
import connectors.TrustConnector
import controllers.routes
import mapping.UserAnswersExtractor
import models.AgentDeclaration
import models.errors.{NoData, PlaybackExtractionErrors, TrustErrorWithRedirect}
import models.http.{GetTrust, Processed, TrustsResponse}
import models.pages.WhatIsNext
import models.requests.DataRequest
import pages.declaration.AgentDeclarationPage
import pages.{SubmissionDatePage, TVNPage, WhatIsNextPage}
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, BodyParsers, Result}
import repositories.PlaybackRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.TrustClosureDate.{getClosureDate, setClosureDate}
import utils.TrustEnvelope
import utils.TrustEnvelope.TrustEnvelope

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.{ExecutionContext, Future}

class RefreshedDataRetrievalActionImpl @Inject()(
                                                  val parser: BodyParsers.Default,
                                                  playbackRepository: PlaybackRepository,
                                                  trustConnector: TrustConnector,
                                                  playbackExtractor: UserAnswersExtractor
                                                )(override implicit val executionContext: ExecutionContext)
  extends RefreshedDataRetrievalAction with Logging {

  private val className = getClass.getSimpleName

  case class SubmissionData(identifier: String,
                            whatIsNext: WhatIsNext,
                            tvn: String,
                            date: LocalDateTime,
                            agent: Option[AgentDeclaration],
                            endDate: Option[LocalDate])

  override def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    val identifier = request.userAnswers.identifier

    val result = for {
      whatIsNext <- TrustEnvelope.fromOption(request.userAnswers.get(WhatIsNextPage))
      tvn <- TrustEnvelope.fromOption(request.userAnswers.get(TVNPage))
      submissionDate <- TrustEnvelope.fromOption(request.userAnswers.get(SubmissionDatePage))
      optionalAgentInformation = request.userAnswers.get(AgentDeclarationPage)
      submissionData = SubmissionData(identifier, whatIsNext, tvn, submissionDate, optionalAgentInformation, getClosureDate(request.userAnswers))
      playbackResponse <- trustConnector.playback(identifier)
      response <- handlePlaybackResponse(playbackResponse, submissionData)(request)
    } yield response

    result.value.map {
      case Right(dataRequest) => Right(dataRequest)
      case Left(NoData) =>
        logger.error(s"[$className][refine][UTR/URN: ${request.userAnswers.identifier}] unable to get data from user answers")
        Left(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem()))
      case Left(TrustErrorWithRedirect(redirect)) => Left(redirect)
      case Left(_) => Left(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem()))
    }
  }

  private def handlePlaybackResponse[A](response: TrustsResponse, submissionData: SubmissionData)
                                       (implicit request: DataRequest[A]): TrustEnvelope[DataRequest[A]] = {
    response match {
      case Processed(playback, _) => extractAndRefreshUserAnswers(submissionData, playback)(request)
      case _ =>
        logger.error(s"[$className][handlePlaybackResponse][UTR/URN: ${request.userAnswers.identifier}] unable to get data from user answers")
        TrustEnvelope(TrustErrorWithRedirect(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem())))
    }
  }


  private def extractAndRefreshUserAnswers[A](data: SubmissionData, playback: GetTrust)
                                             (implicit request: DataRequest[A]): TrustEnvelope[DataRequest[A]] = EitherT {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    val expectedResult = for {
      updatedUserAnswers <- TrustEnvelope(request.userAnswers.clearData
        .set(WhatIsNextPage, data.whatIsNext)
        .flatMap(_.set(TVNPage, data.tvn))
        .flatMap(_.set(SubmissionDatePage, data.date))
        .flatMap(_.set(AgentDeclarationPage, data.agent))
        .flatMap(answers => setClosureDate(answers, data.endDate)))
      extractedAnswers <- playbackExtractor.extract(updatedUserAnswers, playback)(hc, executionContext)
      _ <- TrustEnvelope(playbackRepository.set(extractedAnswers))
    } yield DataRequest(request.request, extractedAnswers, request.user)

    expectedResult.value.map {
      case Right(dataRequest) => Right(dataRequest)
      case Left(reason: PlaybackExtractionErrors) =>
        logger.warn(s"[$className][extractAndRefreshUserAnswers] unable to extract user answers due to $reason")
        Left(TrustErrorWithRedirect(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem())))
      case Left(_) =>
        logger.warn(s"[$className][extractAndRefreshUserAnswers] Error while setting user answers.")
        Left(TrustErrorWithRedirect(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem())))
    }
  }
}

@ImplementedBy(classOf[RefreshedDataRetrievalActionImpl])
trait RefreshedDataRetrievalAction extends ActionRefiner[DataRequest, DataRequest] {

  def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]]
}
