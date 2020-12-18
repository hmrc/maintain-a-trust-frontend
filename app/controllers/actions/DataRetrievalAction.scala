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

import com.google.inject.Inject
import models.UserAnswers
import models.requests.{IdentifierRequest, OptionalDataRequest}
import play.api.Logging
import play.api.mvc.ActionTransformer
import repositories.{ActiveSessionRepository, PlaybackRepository}
import uk.gov.hmrc.play.HeaderCarrierConverter
import utils.Session

import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionImpl @Inject()(activeSessionRepository: ActiveSessionRepository,
                                         val playbackRepository: PlaybackRepository
                                       )(implicit val executionContext: ExecutionContext) extends DataRetrievalAction with Logging {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = {

    val hc = HeaderCarrierConverter.fromHeadersAndSessionAndRequest(request.headers, Some(request.session), Some(request))

    def createdOptionalDataRequest(request: IdentifierRequest[A], userAnswers: Option[UserAnswers]) = {
      OptionalDataRequest(
        request.request,
        userAnswers,
        request.user
      )
    }

    activeSessionRepository.get(request.user.internalId) flatMap {
      case Some(session) =>
        playbackRepository.get(request.user.internalId, session.utr) map {
          case None =>
            logger.debug(s"[Session ID: ${Session.id(hc)}] no user answers returned for internal id")
            createdOptionalDataRequest(request, None)
          case Some(userAnswers) =>
            logger.debug(s"[Session ID: ${Session.id(hc)}] user answers returned for internal id")
            createdOptionalDataRequest(request, Some(userAnswers))
        }
      case None =>
        Future.successful(createdOptionalDataRequest(request, None))
    }

  }
}

trait DataRetrievalAction extends ActionTransformer[IdentifierRequest, OptionalDataRequest]
