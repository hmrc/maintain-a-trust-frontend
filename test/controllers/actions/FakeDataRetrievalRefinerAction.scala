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

import handlers.ErrorHandler
import models.UserAnswers
import models.requests.{IdentifierRequest, OptionalDataRequest}
import play.api.http.Writeable
import play.api.mvc.Result
import play.twirl.api.Html
import repositories.{ActiveSessionRepository, PlaybackRepository}

import scala.concurrent.{ExecutionContext, Future}

class FakeDataRetrievalRefinerAction(dataToReturn: Option[UserAnswers],
                                     activeSessionRepository: ActiveSessionRepository,
                                     playbackRepository: PlaybackRepository,
                                     errorHandler: ErrorHandler
                                    )(implicit ec: ExecutionContext, writeableFutureHtml: Writeable[Future[Html]])
  extends DataRetrievalRefinerAction(activeSessionRepository, playbackRepository, errorHandler) {

  private val utr: String = "1234567890"

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, OptionalDataRequest[A]]] = {
    dataToReturn match {
      case None =>
        Future.successful(Right(OptionalDataRequest(request.request, None, request.user, utr)))
      case Some(userAnswers) =>
        Future.successful(Right(OptionalDataRequest(request.request, Some(userAnswers), request.user, utr)))
    }
  }
}
