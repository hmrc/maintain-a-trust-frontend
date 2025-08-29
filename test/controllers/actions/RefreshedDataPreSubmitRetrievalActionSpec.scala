/*
 * Copyright 2025 HM Revenue & Customs
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
import connectors.TrustConnector
import generators.ModelGenerators
import mapping.UserAnswersExtractor
import models.errors.{FailedToExtractData, MongoError, ServerError, TrustErrors}
import models.http.{Processed, TrustsResponse}
import models.pages.WhatIsNext
import models.requests.{DataRequest, OrganisationUser, User}
import models.{AgentDeclaration, FullName, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.WhatIsNextPage
import pages.close.nontaxable.DateClosedPage
import pages.close.taxable.DateLastAssetSharedOutPage
import pages.declaration.AgentDeclarationPage
import play.api.mvc.Result
import play.api.test.Helpers.{defaultAwaitTimeout, redirectLocation}
import repositories.PlaybackRepository
import uk.gov.hmrc.auth.core.Enrolments

import java.time.LocalDate
import scala.concurrent.Future

class RefreshedDataPreSubmitRetrievalActionSpec extends SpecBase with MockitoSugar with ScalaFutures with EitherValues
  with ScalaCheckPropertyChecks with ModelGenerators {

  class Harness(playbackRepository: PlaybackRepository,
                trustConnector: TrustConnector,
                playbackExtractor: UserAnswersExtractor)
    extends RefreshedDataPreSubmitRetrievalActionImpl(bodyParsers, playbackRepository, trustConnector, playbackExtractor) {
    def callRefine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  private val user: User = OrganisationUser("id", Enrolments(Set()))

  "RefreshedDataPreSubmitRetrievalAction" must {

    "redirect to 'Sorry there has been a problem'" when {

      "WhatIsNextPage in user answers is undefined" in {

        val playbackRepository = mock[PlaybackRepository]
        val trustsConnector = mock[TrustConnector]
        val playbackExtractor = mock[UserAnswersExtractor]

        val userAnswers = emptyUserAnswersForUtr

        val action = new Harness(playbackRepository, trustsConnector, playbackExtractor)

        val result = action.callRefine(DataRequest(fakeRequest, userAnswers, user)).futureValue

        redirectLocation(Future.successful(result.left.value)).value mustEqual controllers.routes.TrustStatusController.sorryThereHasBeenAProblem().url
      }

      "trust is not in processed state" in {

        forAll(arbitrary[TrustsResponse].suchThat(!_.isInstanceOf[Processed])) {
          response =>

            val playbackRepository = mock[PlaybackRepository]
            val trustsConnector = mock[TrustConnector]
            val playbackExtractor = mock[UserAnswersExtractor]

            val userAnswers = emptyUserAnswersForUtr
              .set(WhatIsNextPage, WhatIsNext.MakeChanges).value

            when(trustsConnector.playback(any())(any(), any()))
              .thenReturn(EitherT[Future, TrustErrors, TrustsResponse](Future.successful(Right(response))))

            val action = new Harness(playbackRepository, trustsConnector, playbackExtractor)

            val result = action.callRefine(DataRequest(fakeRequest, userAnswers, user)).futureValue

            redirectLocation(Future.successful(result.left.value)).value mustEqual controllers.routes.TrustStatusController.sorryThereHasBeenAProblem().url
        }
      }

      "playback extractor fails" in {

        forAll(arbitrary[Processed]) {
          response =>

            val playbackRepository = mock[PlaybackRepository]
            val trustsConnector = mock[TrustConnector]
            val playbackExtractor = mock[UserAnswersExtractor]

            val userAnswers = emptyUserAnswersForUtr
              .set(WhatIsNextPage, WhatIsNext.MakeChanges).value

            when(trustsConnector.playback(any())(any(), any()))
              .thenReturn(EitherT[Future, TrustErrors, TrustsResponse](Future.successful(Right(response))))
            when(playbackExtractor.extract(any(), any())(any(), any()))
              .thenReturn(EitherT[Future, TrustErrors, UserAnswers](Future.successful(Left(FailedToExtractData("")))))

            val action = new Harness(playbackRepository, trustsConnector, playbackExtractor)

            val result = action.callRefine(DataRequest(fakeRequest, userAnswers, user)).futureValue

            redirectLocation(Future.successful(result.left.value)).value mustEqual controllers.routes.TrustStatusController.sorryThereHasBeenAProblem().url
        }
      }

      "there is an error while setting user answers" in {

        forAll(arbitrary[Processed]) {
          response =>

            val playbackRepository = mock[PlaybackRepository]
            val trustsConnector = mock[TrustConnector]
            val playbackExtractor = mock[UserAnswersExtractor]

            val userAnswers = emptyUserAnswersForUtr
              .set(WhatIsNextPage, WhatIsNext.MakeChanges).value

            when(trustsConnector.playback(any())(any(), any()))
              .thenReturn(EitherT[Future, TrustErrors, TrustsResponse](Future.successful(Right(response))))
            when(playbackExtractor.extract(any(), any())(any(), any()))
              .thenReturn(EitherT[Future, TrustErrors, UserAnswers](Future.successful(Right(userAnswers))))

            mockPlaybackRepositoryBuilder(playbackRepository, setResult = Left(MongoError))

            val action = new Harness(playbackRepository, trustsConnector, playbackExtractor)

            val result = action.callRefine(DataRequest(fakeRequest, userAnswers, user)).futureValue

            redirectLocation(Future.successful(result.left.value)).value mustEqual controllers.routes.TrustStatusController.sorryThereHasBeenAProblem().url
        }
      }

      "trust connector returns an error" in {

        forAll(arbitrary[Processed]) {
          response =>

            val playbackRepository = mock[PlaybackRepository]
            val trustsConnector = mock[TrustConnector]
            val playbackExtractor = mock[UserAnswersExtractor]

            val whatNext = WhatIsNext.MakeChanges
            val agentDeclaration = AgentDeclaration(FullName("Joe", None, "Bloggs"), "Agency", "tel", "crn", None)
            val closeDate = LocalDate.parse("2021-01-01")

            val userAnswers = emptyUserAnswersForUtr
              .set(WhatIsNextPage, whatNext).value
              .set(AgentDeclarationPage, agentDeclaration).value
              .set(DateLastAssetSharedOutPage, closeDate).value

            when(trustsConnector.playback(any())(any(), any()))
              .thenReturn(EitherT[Future, TrustErrors, TrustsResponse](Future.successful(Left(ServerError()))))

            when(playbackExtractor.extract(eqTo(userAnswers), eqTo(response.playback))(any(), any()))
              .thenReturn(EitherT[Future, TrustErrors, UserAnswers](Future.successful(Right(userAnswers))))
            when(playbackRepository.set(any()))
              .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

            val action = new Harness(playbackRepository, trustsConnector, playbackExtractor)

            val result = action.callRefine(DataRequest(fakeRequest, userAnswers, user)).futureValue

            redirectLocation(Future.successful(result.left.value)).value mustEqual controllers.routes.TrustStatusController.sorryThereHasBeenAProblem().url
        }
      }
    }

    "return right" when {
      "action succeeds" in {

        forAll(arbitrary[Processed]) {
          response =>

            val playbackRepository = mock[PlaybackRepository]
            val trustsConnector = mock[TrustConnector]
            val playbackExtractor = mock[UserAnswersExtractor]

            val whatNext = WhatIsNext.MakeChanges
            val agentDeclaration = AgentDeclaration(FullName("Joe", None, "Bloggs"), "Agency", "tel", "crn", None)
            val closeDate = LocalDate.parse("2021-01-01")

            val userAnswers = emptyUserAnswersForUtr
              .set(WhatIsNextPage, whatNext).value
              .set(AgentDeclarationPage, agentDeclaration).value
              .set(DateLastAssetSharedOutPage, closeDate).value

            when(trustsConnector.playback(any())(any(), any()))
              .thenReturn(EitherT[Future, TrustErrors, TrustsResponse](Future.successful(Right(response))))
            when(playbackExtractor.extract(eqTo(userAnswers), eqTo(response.playback))(any(), any()))
              .thenReturn(EitherT[Future, TrustErrors, UserAnswers](Future.successful(Right(userAnswers))))
            when(playbackRepository.set(any()))
              .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

            val action = new Harness(playbackRepository, trustsConnector, playbackExtractor)

            action.callRefine(DataRequest(fakeRequest, userAnswers, user)).futureValue

            val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(playbackRepository).set(uaCaptor.capture)

            uaCaptor.getValue.get(WhatIsNextPage).get mustBe whatNext
            uaCaptor.getValue.get(AgentDeclarationPage).get mustBe agentDeclaration
            uaCaptor.getValue.get(DateLastAssetSharedOutPage).get mustBe closeDate
            uaCaptor.getValue.get(DateClosedPage) mustBe None
        }
      }
    }
  }
}
