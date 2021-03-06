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

package services

import com.google.inject.Inject
import controllers.Assets.Redirect
import models.requests.IdentifierRequest
import models.{IdentifierSession, UserAnswers}
import play.api.Logging
import play.api.mvc.Result
import repositories.{ActiveSessionRepository, PlaybackRepository}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Session

import scala.concurrent.{ExecutionContext, Future}

class SessionService @Inject()(playbackRepository: PlaybackRepository,
                               sessionRepository: ActiveSessionRepository) extends Logging {

  def initialiseUserAnswers(identifier: String,
                            internalId: String,
                            is5mldEnabled: Boolean,
                            isUnderlyingData5mld: Boolean,
                            isUnderlyingDataTaxable: Boolean)
                           (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[UserAnswers] = {

    val activeSession = IdentifierSession(internalId, identifier)
    val newEmptyAnswers = UserAnswers.startNewSession(internalId, identifier, is5mldEnabled, isUnderlyingData5mld, isUnderlyingDataTaxable)

    for {
      _ <- playbackRepository.resetCache(internalId, identifier)
      _ <- playbackRepository.set(newEmptyAnswers)
      _ <- sessionRepository.set(activeSession)
    } yield {
      logger.info(s"[Session ID: ${Session.id(hc)}]" +
        s" $identifier user is enrolled, storing identifier in user answers, checking status of trust")
      newEmptyAnswers
    }
  }

  def initialiseSession[A](identifier: String)
                          (implicit request: IdentifierRequest[A], ec: ExecutionContext): Future[Result] = {
    val session = IdentifierSession(request.user.internalId, identifier)
    for {
      _ <- sessionRepository.set(session)
    } yield {
      Redirect(controllers.routes.TrustStatusController.status())
    }
  }

}
