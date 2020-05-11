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
import play.api.Logger
import play.api.mvc.ActionTransformer
import repositories.PlaybackRepository

import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionImpl @Inject()(
                                         val playbackRepository: PlaybackRepository
                                       )(implicit val executionContext: ExecutionContext) extends DataRetrievalAction {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = {

    def createdOptionalDataRequest(request: IdentifierRequest[A], userAnswers: Option[UserAnswers]) = {

      val utr = request.user.enrolments.enrolments
        .find(_.key equals "HMRC-TERS-ORG")
        .flatMap(_.identifiers.find(_.key equals "SAUTR"))
        .map(_.value)

      OptionalDataRequest(
        request.request,
        userAnswers,
        request.user,
        utr
      )
    }

    playbackRepository.get(request.user.internalId) map {
      case None =>
        Logger.debug(s"[DataRetrievalAction] no user answers returned for internal id")
        createdOptionalDataRequest(request, None)
      case Some(userAnswers) =>
        Logger.debug(s"[DataRetrievalAction] user answers returned for internal id")
        createdOptionalDataRequest(request, Some(userAnswers))
    }
  }
}

trait DataRetrievalAction extends ActionTransformer[IdentifierRequest, OptionalDataRequest]
