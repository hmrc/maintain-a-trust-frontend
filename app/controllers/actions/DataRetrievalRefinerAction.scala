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

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import handlers.ErrorHandler
import models.requests.{IdentifierRequest, OptionalDataRequest}
import models.{IdentifierSession, UserAnswers}
import play.api.Logging
import play.api.mvc.Results.InternalServerError
import play.api.mvc.{ActionRefiner, Result, Results}
import repositories.{ActiveSessionRepository, PlaybackRepository}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.Session
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataRetrievalRefinerAction @Inject()(activeSessionRepository: ActiveSessionRepository,
                                           val playbackRepository: PlaybackRepository,
                                           errorHandler: ErrorHandler,
                                           appConfig: FrontendAppConfig
                                          )(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[IdentifierRequest, OptionalDataRequest] with Logging {

  private val className = getClass.getSimpleName

  private def createdOptionalDataRequest[A](request: IdentifierRequest[A],
                                            userAnswers: Option[UserAnswers],
                                            identifier: String): OptionalDataRequest[A] =
    OptionalDataRequest(request.request, userAnswers, request.user, identifier)

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, OptionalDataRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    activeSessionRepository.get(request.user.internalId).value.flatMap {
      case Right(Some(session)) => handlePlaybackRepositoryResponse(request, session)
      case Right(None) =>
        logger.warn(s"[$className][refine] no active UTR/URN present in the session data")

        Future.successful(Left(
          Results.Redirect(appConfig.logoutUrl)
        ))
      case Left(_) =>
        logger.warn(s"[$className][refine] Error while retrieving data from active session repository")
        errorHandler.internalServerErrorTemplate(request.request).map {
          html => Left(InternalServerError(html))
        }
    }
  }

  private def handlePlaybackRepositoryResponse[A](request: IdentifierRequest[A],
                                                  session: IdentifierSession
                                                 )(implicit hc: HeaderCarrier): Future[Either[Result, OptionalDataRequest[A]]] = {
    playbackRepository.get(request.user.internalId, session.identifier, Session.id(hc)).value.flatMap {
      case Right(None) =>
        logger.info(s"[$className][handlePlaybackRepositoryResponse] no user answers in session for UTR/URN ${session.identifier}")
        Future.successful(Right(createdOptionalDataRequest(request, None, session.identifier)))
      case Right(Some(userAnswers)) =>
        logger.info(s"[$className][handlePlaybackRepositoryResponse] user answers found in session for UTR/URN ${session.identifier}")
        Future.successful(Right(createdOptionalDataRequest(request, Some(userAnswers), session.identifier)))
      case Left(_) =>
        logger.warn(s"[$className][handlePlaybackRepositoryResponse] Error while retrieving data from playback repository")
        errorHandler.internalServerErrorTemplate(request.request).map {
          html => Left(InternalServerError(html))
        }
    }
  }
}
