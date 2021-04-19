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

import com.google.inject.Inject
import models.UserAnswers
import models.requests.{IdentifierRequest, OptionalDataRequest}
import play.api.Logging
import play.api.mvc.ActionTransformer
import repositories.{ActiveSessionRepository, PlaybackRepository}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.Session

import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionImpl @Inject()(activeSessionRepository: ActiveSessionRepository,
                                         val playbackRepository: PlaybackRepository
                                       )(implicit val executionContext: ExecutionContext) extends DataRetrievalAction with Logging {

  private def createdOptionalDataRequest[A](request: IdentifierRequest[A],
                                            userAnswers: Option[UserAnswers],
                                            identifier: Option[String]): OptionalDataRequest[A] =
    OptionalDataRequest(request.request, userAnswers, request.user, identifier)

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = {

    val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    activeSessionRepository.get(request.user.internalId) flatMap {
      case Some(session) =>
        playbackRepository.get(request.user.internalId, session.identifier) map {
          case None =>
            logger.info(s"[Session ID: ${Session.id(hc)}] no user answers in mongo for UTR/URN ${session.identifier}")
            createdOptionalDataRequest(request, None, Some(session.identifier))
          case Some(userAnswers) =>
            logger.info(s"[Session ID: ${Session.id(hc)}] user answers found in mongo for UTR/URN ${session.identifier}")
            createdOptionalDataRequest(request, Some(userAnswers), Some(session.identifier))
        }
      case None =>
        logger.info(s"[Session ID: ${Session.id(hc)}] no active UTR/URN found in mongo for session")
        Future.successful(createdOptionalDataRequest(request, None, None))
    }

  }
}

trait DataRetrievalAction extends ActionTransformer[IdentifierRequest, OptionalDataRequest]
