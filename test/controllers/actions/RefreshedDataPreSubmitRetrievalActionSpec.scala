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

package controllers.actions

import base.SpecBase
import connectors.TrustConnector
import generators.ModelGenerators
import mapping.PlaybackExtractionErrors.FailedToExtractData
import mapping.UserAnswersExtractor
import models.{AgentDeclaration, FullName, UserAnswers}
import models.http.{Processed, TrustsResponse}
import models.pages.WhatIsNext
import models.requests.{DataRequest, OrganisationUser, User}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{any, eq => eqTo}
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
import services.FeatureFlagService
import uk.gov.hmrc.auth.core.Enrolments

import java.time.LocalDate
import scala.concurrent.Future

class RefreshedDataPreSubmitRetrievalActionSpec extends SpecBase with MockitoSugar with ScalaFutures with EitherValues
  with ScalaCheckPropertyChecks with ModelGenerators {

  class Harness(playbackRepository: PlaybackRepository,
                trustConnector: TrustConnector,
                playbackExtractor: UserAnswersExtractor,
                featureFlagService: FeatureFlagService)
    extends RefreshedDataPreSubmitRetrievalActionImpl(bodyParsers, playbackRepository, trustConnector, playbackExtractor, featureFlagService) {
      def callRefine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  val user: User = OrganisationUser("id", Enrolments(Set()))

  "RefreshedDataPreSubmitRetrievalAction" must {

    "redirect to 'Sorry there has been a problem'" when {

      "WhatIsNextPage in user answers is undefined" in {

        val playbackRepository = mock[PlaybackRepository]
        val trustsConnector = mock[TrustConnector]
        val playbackExtractor = mock[UserAnswersExtractor]
        val featureFlagService = mock[FeatureFlagService]

        val userAnswers = emptyUserAnswersForUtr

        val action = new Harness(playbackRepository, trustsConnector, playbackExtractor, featureFlagService)

        val futureResult = action.callRefine(DataRequest(fakeRequest, userAnswers, user))

        whenReady(futureResult) { result =>
          result mustBe 'left
          redirectLocation(Future.successful(result.left.value)).value mustEqual controllers.routes.TrustStatusController.sorryThereHasBeenAProblem().url
        }
      }

      "trust is not in processed state" in {

        forAll(arbitrary[TrustsResponse].suchThat(!_.isInstanceOf[Processed])) {
          response =>

            val playbackRepository = mock[PlaybackRepository]
            val trustsConnector = mock[TrustConnector]
            val playbackExtractor = mock[UserAnswersExtractor]
            val featureFlagService = mock[FeatureFlagService]

            val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true)
              .set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

            when(trustsConnector.playback(any())(any(), any())).thenReturn(Future.successful(response))

            val action = new Harness(playbackRepository, trustsConnector, playbackExtractor, featureFlagService)

            val futureResult = action.callRefine(DataRequest(fakeRequest, userAnswers, user))

            whenReady(futureResult) { result =>
              result mustBe 'left
              redirectLocation(Future.successful(result.left.value)).value mustEqual controllers.routes.TrustStatusController.sorryThereHasBeenAProblem().url
            }
        }
      }

      "failure fetching 5mld feature flag" in {

        forAll(arbitrary[Processed]) {
          response =>

            val playbackRepository = mock[PlaybackRepository]
            val trustsConnector = mock[TrustConnector]
            val playbackExtractor = mock[UserAnswersExtractor]
            val featureFlagService = mock[FeatureFlagService]

            val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true)
              .set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

            when(trustsConnector.playback(any())(any(), any())).thenReturn(Future.successful(response))
            when(featureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.failed(new Throwable("")))

            val action = new Harness(playbackRepository, trustsConnector, playbackExtractor, featureFlagService)

            val futureResult = action.callRefine(DataRequest(fakeRequest, userAnswers, user))

            whenReady(futureResult) { result =>
              result mustBe 'left
              redirectLocation(Future.successful(result.left.value)).value mustEqual controllers.routes.TrustStatusController.sorryThereHasBeenAProblem().url
            }
        }
      }

      "playback extractor fails" in {

        forAll(arbitrary[Processed]) {
          response =>

            val playbackRepository = mock[PlaybackRepository]
            val trustsConnector = mock[TrustConnector]
            val playbackExtractor = mock[UserAnswersExtractor]
            val featureFlagService = mock[FeatureFlagService]

            val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true)
              .set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

            when(trustsConnector.playback(any())(any(), any())).thenReturn(Future.successful(response))
            when(featureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))
            when(playbackExtractor.extract(any(), any())(any(), any())).thenReturn(Future.successful(Left(FailedToExtractData(""))))

            val action = new Harness(playbackRepository, trustsConnector, playbackExtractor, featureFlagService)

            val futureResult = action.callRefine(DataRequest(fakeRequest, userAnswers, user))

            whenReady(futureResult) { result =>
              result mustBe 'left
              redirectLocation(Future.successful(result.left.value)).value mustEqual controllers.routes.TrustStatusController.sorryThereHasBeenAProblem().url
            }
        }
      }
    }

    "redirect to express trust page" when {
      "MLD mode has changed in session" in {

        forAll(arbitrary[Processed]) {
          response =>

            val playbackRepository = mock[PlaybackRepository]
            val trustsConnector = mock[TrustConnector]
            val playbackExtractor = mock[UserAnswersExtractor]
            val featureFlagService = mock[FeatureFlagService]

            val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = false)
              .set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

            when(trustsConnector.playback(any())(any(), any())).thenReturn(Future.successful(response))
            when(featureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))

            val action = new Harness(playbackRepository, trustsConnector, playbackExtractor, featureFlagService)

            val futureResult = action.callRefine(DataRequest(fakeRequest, userAnswers, user))

            whenReady(futureResult) { result =>
              result mustBe 'left
              redirectLocation(Future.successful(result.left.value)).value mustEqual
                controllers.transition.routes.ExpressTrustYesNoController.onPageLoad().url
            }
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
            val featureFlagService = mock[FeatureFlagService]

            val whatNext = WhatIsNext.MakeChanges
            val agentDeclaration = AgentDeclaration(FullName("Joe", None, "Bloggs"), "Agency", "tel", "crn", None)
            val closeDate = LocalDate.parse("2021-01-01")

            val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true)
              .set(WhatIsNextPage, whatNext).success.value
              .set(AgentDeclarationPage, agentDeclaration).success.value
              .set(DateLastAssetSharedOutPage, closeDate).success.value

            when(trustsConnector.playback(any())(any(), any()))
              .thenReturn(Future.successful(response))
            when(featureFlagService.is5mldEnabled()(any(), any()))
              .thenReturn(Future.successful(true))
            when(playbackExtractor.extract(eqTo(userAnswers.clearData), eqTo(response.playback))(any(), any()))
              .thenReturn(Future.successful(Right(userAnswers)))
            when(playbackRepository.set(any()))
              .thenReturn(Future.successful(true))

            val action = new Harness(playbackRepository, trustsConnector, playbackExtractor, featureFlagService)

            val futureResult = action.callRefine(DataRequest(fakeRequest, userAnswers, user))

            whenReady(futureResult) { result =>
              result mustBe 'right

              val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
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
}
