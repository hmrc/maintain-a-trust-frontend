/*
 * Copyright 2022 HM Revenue & Customs
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

import base.SpecBase
import models.IdentifierSession
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import repositories.{ActiveSessionRepository, PlaybackRepository}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class SessionServiceSpec extends SpecBase with ScalaCheckPropertyChecks {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val identifier = "identifier"
  private val internalId = "internalId"

  "SessionService" when {

    "initialiseUserAnswers" must {

      "setup user answers" in {

        forAll(arbitrary[Boolean], arbitrary[Boolean]) {
          (isUnderlyingData5mld, isUnderlyingDataTaxable) =>

            val mockPlaybackRepository = mock[PlaybackRepository]
            when(mockPlaybackRepository.set(any())).thenReturn(Future.successful(true))
            when(mockPlaybackRepository.resetCache(any(), any())).thenReturn(Future.successful(Some(Json.obj())))

            val mockSessionRepository = mock[ActiveSessionRepository]
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val userAnswersSetupService = new SessionService(mockPlaybackRepository, mockSessionRepository)

            val resultF = userAnswersSetupService.initialiseUserAnswers(
              identifier = identifier,
              internalId = internalId,
              isUnderlyingData5mld = isUnderlyingData5mld,
              isUnderlyingDataTaxable = isUnderlyingDataTaxable
            )

            val ua = Await.result(resultF, 5.seconds)

            ua.internalId mustBe internalId
            ua.identifier mustBe identifier
            ua.isUnderlyingData5mld mustBe isUnderlyingData5mld
            ua.isUnderlyingDataTaxable mustBe isUnderlyingDataTaxable

            verify(mockPlaybackRepository).resetCache(internalId, identifier)

            val identifierSessionCaptor = ArgumentCaptor.forClass(classOf[IdentifierSession])
            verify(mockSessionRepository).set(identifierSessionCaptor.capture())

            identifierSessionCaptor.getValue.internalId mustBe internalId
            identifierSessionCaptor.getValue.identifier mustBe identifier
        }
      }
    }
  }
}
