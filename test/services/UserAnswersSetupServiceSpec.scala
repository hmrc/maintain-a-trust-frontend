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

import base.SpecBase
import models.{IdentifierSession, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.test.Helpers.{defaultAwaitTimeout, redirectLocation}
import repositories.{ActiveSessionRepository, PlaybackRepository}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class UserAnswersSetupServiceSpec extends SpecBase with ScalaCheckPropertyChecks {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val identifier = "identifier"
  private val internalId = "internalId"

  "UserAnswersSetupService" when {
    "setupAndRedirectToStatus" must {
      "setup user answers and redirect to TrustStatusController" in {

        forAll(arbitrary[Boolean], arbitrary[Boolean]) {
          (is5mldEnabled, isTaxable) =>

            val mockPlaybackRepository = mock[PlaybackRepository]
            when(mockPlaybackRepository.set(any())).thenReturn(Future.successful(true))
            when(mockPlaybackRepository.resetCache(any(), any())).thenReturn(Future.successful(Some(Json.obj())))

            val mockSessionRepository = mock[ActiveSessionRepository]
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val userAnswersSetupService = new UserAnswersSetupService(mockPlaybackRepository, mockSessionRepository)

            val result = userAnswersSetupService.setupAndRedirectToStatus(identifier, internalId, is5mldEnabled, isTaxable)

            redirectLocation(result).value mustBe controllers.routes.TrustStatusController.status().url

            verify(mockPlaybackRepository).resetCache(internalId, identifier)

            val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockPlaybackRepository).set(uaCaptor.capture)

            uaCaptor.getValue.internalId mustBe internalId
            uaCaptor.getValue.identifier mustBe identifier
            uaCaptor.getValue.is5mldEnabled mustBe is5mldEnabled
            uaCaptor.getValue.isTrustTaxable mustBe isTaxable

            val identifierSessionCaptor = ArgumentCaptor.forClass(classOf[IdentifierSession])
            verify(mockSessionRepository).set(identifierSessionCaptor.capture())

            identifierSessionCaptor.getValue.internalId mustBe internalId
            identifierSessionCaptor.getValue.identifier mustBe identifier
        }
      }
    }
  }
}
