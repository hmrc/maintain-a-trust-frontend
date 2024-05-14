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

import base.SpecBase
import cats.data.EitherT
import models.errors.{MongoError, TrustErrors}
import models.requests.{IdentifierRequest, OptionalDataRequest, OrganisationUser}
import models.{IdentifierSession, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.mvc.Results.InternalServerError
import play.twirl.api.Html
import repositories.ActiveSessionRepository
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUserAnswers

import scala.concurrent.Future

class DataRetrievalRefinerActionSpec extends SpecBase with MockitoSugar with ScalaFutures {

  private val mockSessionRepository = mock[ActiveSessionRepository]

  class Harness() extends DataRetrievalRefinerAction(mockSessionRepository, mockPlaybackRepository, mockErrorHandler)(executionContext) {
      def callRefine[A](request: IdentifierRequest[A]): Future[Either[Result, OptionalDataRequest[A]]] = refine(request)
  }

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val sessionId: String = utils.Session.id(hc)

  "Data Retrieval Action" when {

    "there is no active session" must {

      "not continue" in {
        when(mockSessionRepository.get("id"))
          .thenReturn(EitherT[Future, TrustErrors, Option[IdentifierSession]](Future.successful(Right(None))))

        when(mockErrorHandler.internalServerErrorTemplate(any()))
          .thenReturn(Html(""))

        val action = new Harness

        val futureResult = action.callRefine(IdentifierRequest(fakeRequest, OrganisationUser("id", Enrolments(Set()))))

        futureResult.futureValue mustBe Left(InternalServerError(mockErrorHandler.internalServerErrorTemplate(fakeRequest)))
      }

      "return an Internal Server Error when there is a problem when calling ActiveSessionRepository" in {

        when(mockSessionRepository.get("id"))
          .thenReturn(EitherT[Future, TrustErrors, Option[IdentifierSession]](Future.successful(Left(MongoError))))

        when(mockErrorHandler.internalServerErrorTemplate(any()))
          .thenReturn(Html(""))

        val action = new Harness

        val futureResult = action.callRefine(IdentifierRequest(fakeRequest, OrganisationUser("id", Enrolments(Set()))))

        futureResult.futureValue mustBe Left(InternalServerError(mockErrorHandler.internalServerErrorTemplate(fakeRequest)))
      }
    }

    "there is no data in the cache" must {

      "set userAnswers to 'None' in the request" in {

        when(mockSessionRepository.get("id"))
          .thenReturn(EitherT[Future, TrustErrors, Option[IdentifierSession]](Future.successful(Right(Some(IdentifierSession("id", "utr"))))))

        when(mockPlaybackRepository.get("id", "utr", sessionId))
          .thenReturn(EitherT[Future, TrustErrors, Option[UserAnswers]](Future.successful(Right(None))))

        val action = new Harness

        val futureResult = action.callRefine(IdentifierRequest(fakeRequest, OrganisationUser("id", Enrolments(Set()))))

        whenReady(futureResult) { result =>
          result.value.userAnswers mustBe None
        }
      }

      "return an Internal Server Error when there is a problem when calling PlaybackRepository" in {

        when(mockSessionRepository.get("id"))
          .thenReturn(EitherT[Future, TrustErrors, Option[IdentifierSession]](Future.successful(Right(Some(IdentifierSession("id", "utr"))))))

        when(mockPlaybackRepository.get("id", "utr", sessionId))
          .thenReturn(EitherT[Future, TrustErrors, Option[UserAnswers]](Future.successful(Left(MongoError))))

        when(mockErrorHandler.internalServerErrorTemplate(any()))
          .thenReturn(Html(""))

        val action = new Harness

        val futureResult = action.callRefine(IdentifierRequest(fakeRequest, OrganisationUser("id", Enrolments(Set()))))

        futureResult.futureValue mustBe Left(InternalServerError(mockErrorHandler.internalServerErrorTemplate(fakeRequest)))
      }
    }

    "there is data in the cache" must {

      "build a userAnswers object and add it to the request" in {

        when(mockSessionRepository.get("id"))
          .thenReturn(EitherT[Future, TrustErrors, Option[IdentifierSession]](Future.successful(Right(Some(IdentifierSession("id", "utr"))))))

        when(mockPlaybackRepository.get("id", "utr", sessionId))
          .thenReturn(EitherT[Future, TrustErrors, Option[UserAnswers]](Future.successful(Right(Some(TestUserAnswers.emptyUserAnswersForUtr)))))

        val action = new Harness

        val futureResult = action.callRefine(IdentifierRequest(fakeRequest, OrganisationUser("id", Enrolments(Set()))))

        whenReady(futureResult) { result =>
          result.value.userAnswers.isDefined mustBe true
        }
      }
    }
  }
}
