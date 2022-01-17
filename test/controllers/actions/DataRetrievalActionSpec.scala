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

package controllers.actions

import base.SpecBase
import models.IdentifierSession
import models.requests.{IdentifierRequest, OptionalDataRequest, OrganisationUser}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import repositories.{ActiveSessionRepository, PlaybackRepository}
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUserAnswers

import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with MockitoSugar with ScalaFutures {

  private val mockSessionRepository = mock[ActiveSessionRepository]

  class Harness(playbackRepository: PlaybackRepository)
    extends DataRetrievalActionImpl(mockSessionRepository, playbackRepository) {
      def callTransform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = transform(request)
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val sessionId: String = utils.Session.id(hc)

  "Data Retrieval Action" when {

    "there is no active session" must {

      "not continue" in {

        val playbackRepository = mock[PlaybackRepository]

        when(mockSessionRepository.get("id")).thenReturn(Future.successful(None))

        val action = new Harness(playbackRepository)

        val futureResult = action.callTransform(IdentifierRequest(fakeRequest, OrganisationUser("id", Enrolments(Set()))))

        intercept[RuntimeException] {
          futureResult.futureValue
        }
      }

    }

    "there is no data in the cache" must {

      "set userAnswers to 'None' in the request" in {

        val playbackRepository = mock[PlaybackRepository]

        when(mockSessionRepository.get("id")).thenReturn(Future.successful(Some(IdentifierSession("id", "utr"))))
        when(playbackRepository.get("id", "utr", sessionId)) thenReturn Future(None)

        val action = new Harness(playbackRepository)

        val futureResult = action.callTransform(IdentifierRequest(fakeRequest, OrganisationUser("id", Enrolments(Set()))))

        whenReady(futureResult) { result =>
          result.userAnswers mustBe None
        }
      }
    }

    "there is data in the cache" must {

      "build a userAnswers object and add it to the request" in {

        val playbackRepository = mock[PlaybackRepository]

        when(mockSessionRepository.get("id")).thenReturn(Future.successful(Some(IdentifierSession("id", "utr"))))
        when(playbackRepository.get("id", "utr", sessionId)) thenReturn Future(Some(TestUserAnswers.emptyUserAnswersForUtr))

        val action = new Harness(playbackRepository)

        val futureResult = action.callTransform(IdentifierRequest(fakeRequest, OrganisationUser("id", Enrolments(Set()))))

        whenReady(futureResult) { result =>
          result.userAnswers.isDefined mustBe true
        }
      }
    }
  }
}
