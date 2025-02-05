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

package base

import cats.data.EitherT
import handlers.ErrorHandler
import models.UserAnswers
import models.errors.TrustErrors
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar
import play.twirl.api.Html
import repositories.{ActiveSessionRepository, PlaybackRepository}
import utils.TrustEnvelope.TrustEnvelope

import scala.concurrent.Future

trait Mocked extends MockitoSugar {

  val mockErrorHandler: ErrorHandler = mock[ErrorHandler]

  when(mockErrorHandler.internalServerErrorTemplate(any()))
    .thenReturn(Future.successful(Html("")))

  val mockPlaybackRepository: PlaybackRepository = mock[PlaybackRepository]

  when(mockPlaybackRepository.set(any()))
    .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

  val mockActiveSessionRepository: ActiveSessionRepository = mock[ActiveSessionRepository]

  def mockPlaybackRepositoryBuilder(playbackRepository: PlaybackRepository,
                                    getResult: Either[TrustErrors, Option[UserAnswers]] = Right(None),
                                    setResult: Either[TrustErrors, Boolean] = Right(true),
                                    resetCacheResult: Either[TrustErrors, Option[Boolean]] = Right(Some(true))
                                   ): OngoingStubbing[TrustEnvelope[Option[Boolean]]] = {

    when(playbackRepository.get(any(), any(), any()))
      .thenReturn(EitherT[Future, TrustErrors, Option[UserAnswers]](Future.successful(getResult)))

    when(playbackRepository.set(any()))
      .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(setResult)))

    when(playbackRepository.resetCache(any(), any(), any()))
      .thenReturn(EitherT[Future, TrustErrors, Option[Boolean]](Future.successful(resetCacheResult)))
  }

}
