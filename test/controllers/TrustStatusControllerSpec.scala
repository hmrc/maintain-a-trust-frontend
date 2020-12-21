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

package controllers

import base.SpecBase
import connectors.{TrustClaim, TrustConnector, TrustsStoreConnector}
import mapping.{FakeFailingUserAnswerExtractor, FakeUserAnswerExtractor, UserAnswersExtractor}
import models.http._
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json._
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.PlaybackRepository
import services.{AuthenticationService, FakeAuthenticationService, FakeFailingAuthenticationService}
import uk.gov.hmrc.auth.core.AffinityGroup
import views.html.status._

import scala.concurrent.Future
import scala.io.Source

class TrustStatusControllerSpec extends SpecBase with BeforeAndAfterEach {

  trait BaseSetup {

    val builder: GuiceApplicationBuilder

    def utr = "utr"

    def userAnswers = emptyUserAnswers

    val fakeTrustConnector: TrustConnector = mock[TrustConnector]
    val fakeTrustStoreConnector: TrustsStoreConnector = mock[TrustsStoreConnector]
    val fakePlaybackRepository: PlaybackRepository = mock[PlaybackRepository]

    def request: FakeRequest[AnyContentAsEmpty.type]

    def result: Future[Result] = route(application, request).value

    lazy val application: Application = builder.overrides(
      bind[TrustConnector].to(fakeTrustConnector),
      bind[TrustsStoreConnector].to(fakeTrustStoreConnector),
      bind[AuthenticationService].to(new FakeAuthenticationService()),
      bind[UserAnswersExtractor].to[FakeUserAnswerExtractor]
    ).build()
  }

  trait LocalSetup extends BaseSetup {
    override val builder: GuiceApplicationBuilder = applicationBuilder(userAnswers = Some(userAnswers))
  }

  trait LocalSetupForAgent extends BaseSetup {
    override val builder: GuiceApplicationBuilder = applicationBuilder(
      userAnswers = Some(userAnswers),
      affinityGroup = AffinityGroup.Agent
    )
  }

  "TrustStatus Controller" when {

    "must return OK and the correct view for GET ../status/closed" in new LocalSetup {

      override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.closed().url)

      val view: TrustClosedView = application.injector.instanceOf[TrustClosedView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(AffinityGroup.Individual, utr)(request, messages).toString

      application.stop()
    }

    "must return OK and the correct view for GET ../status/processing" in new LocalSetup {

      override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.processing().url)

      val view: TrustStillProcessingView = application.injector.instanceOf[TrustStillProcessingView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(AffinityGroup.Individual, utr)(request, messages).toString

      application.stop()
    }

    "must return OK and the correct view for GET ../status/not-found" in new LocalSetup {

      override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.notFound().url)

      val view: TrustUtrDoesNotMatchView = application.injector.instanceOf[TrustUtrDoesNotMatchView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(AffinityGroup.Individual)(request, messages).toString

      application.stop()
    }

    "must return OK and the correct view for GET ../status/locked" in new LocalSetup {

      override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.locked().url)

      val view: TrustLockedView = application.injector.instanceOf[TrustLockedView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(utr)(request, messages).toString

      application.stop()
    }

    "must return OK and the correct view for GET ../status/already-claimed" in new LocalSetup {

      override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.alreadyClaimed().url)

      val view: TrustAlreadyClaimedView = application.injector.instanceOf[TrustAlreadyClaimedView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(utr)(request, messages).toString

      application.stop()
    }

    "must return OK and the correct view for GET ../status/sorry-there-has-been-a-problem" in new LocalSetup {

      override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.sorryThereHasBeenAProblem().url)

      val view: PlaybackProblemContactHMRCView = application.injector.instanceOf[PlaybackProblemContactHMRCView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(utr)(request, messages).toString

      application.stop()
    }

    "must return SERVICE_UNAVAILABLE and the correct view for GET ../status/down" in new LocalSetup {

      override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.down().url)

      val view: IVDownView = application.injector.instanceOf[IVDownView]

      status(result) mustEqual SERVICE_UNAVAILABLE

      contentAsString(result) mustEqual
        view(AffinityGroup.Individual)(request, messages).toString

      application.stop()
    }

    "must return SERVICE_UNAVAILABLE and the correct view for GET ../status/unavailable" in new LocalSetup {

      override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.unavailable().url)

      val view: IVDownView = application.injector.instanceOf[IVDownView]

      status(result) mustEqual SERVICE_UNAVAILABLE

      contentAsString(result) mustEqual
        view(AffinityGroup.Individual)(request, messages).toString

      application.stop()
    }

    "must redirect to the correct route for GET ../status/start" when {

      "a Closed status is received from the trust connector" in new LocalSetup {

        override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

        when(fakeTrustStoreConnector.get(any[String])(any(), any()))
          .thenReturn(Future.successful(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false))))

        when(fakeTrustConnector.playbackfromEtmp(any[String])(any(), any())).thenReturn(Future.successful(Closed))

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "/maintain-a-trust/status/closed"

        application.stop()
      }

      "a Processing status is received from the trust connector" in new LocalSetup {

        override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

        when(fakeTrustStoreConnector.get(any[String])(any(), any()))
          .thenReturn(Future.successful(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false))))

        when(fakeTrustConnector.playbackfromEtmp(any[String])(any(), any())).thenReturn(Future.successful(Processing))

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "/maintain-a-trust/status/processing"

        application.stop()
      }

      "a NotFound status is received from the trust connector" in new LocalSetup {

        override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

        when(fakeTrustStoreConnector.get(any[String])(any(), any()))
          .thenReturn(Future.successful(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false))))

        when(fakeTrustConnector.playbackfromEtmp(any[String])(any(), any())).thenReturn(Future.successful(UtrNotFound))

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "/maintain-a-trust/status/not-found"

        application.stop()
      }

      "A locked trust claim is returned from the trusts store connector" in new LocalSetup {

        override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

        when(fakeTrustStoreConnector.get(any[String])(any(), any()))
          .thenReturn(Future.successful(Some(TrustClaim("utr", trustLocked = true, managedByAgent = false))))

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "/maintain-a-trust/status/locked"

        application.stop()
      }

      "a ServiceUnavailable status is received from the trust connector" in new LocalSetup {

        override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

        when(fakeTrustStoreConnector.get(any[String])(any(), any()))
          .thenReturn(Future.successful(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false))))

        when(fakeTrustConnector.playbackfromEtmp(any[String])(any(), any())).thenReturn(Future.successful(TrustServiceUnavailable))

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "/maintain-a-trust/status/unavailable"

        application.stop()
      }

      "a ServiceUnavailable status is received from the trust connector when we get a ServerError" in new LocalSetup {

        override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

        when(fakeTrustStoreConnector.get(any[String])(any(), any()))
          .thenReturn(Future.successful(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false))))

        when(fakeTrustConnector.playbackfromEtmp(any[String])(any(), any())).thenReturn(Future.successful(ServerError))

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "/maintain-a-trust/status/down"

        application.stop()
      }

      "a Processed status is received from the trust connector" when {

        "user is authenticated for playback" when {

          "user answers is extracted" must {

            "redirect to maintain this trust for organisation" in new LocalSetup {

              override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

              val payload : String =
                Source.fromFile(getClass.getResource("/display-trust.json").getPath).mkString

              val json : JsValue = Json.parse(payload)

              val getTrust = json.as[GetTrustDesResponse].getTrust.value

              when(fakeTrustStoreConnector.get(any[String])(any(), any()))
                .thenReturn(Future.successful(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false))))

              when(fakeTrustConnector.playbackfromEtmp(any[String])(any(), any()))
                .thenReturn(Future.successful(Processed(getTrust, "9873459837459837")))

              when(fakePlaybackRepository.set(any())).thenReturn(Future.successful(true))

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustEqual routes.MaintainThisTrustController.onPageLoad(needsIv=false).url

              application.stop()
            }

            "redirect to information maintaining this trust for agent" in new LocalSetupForAgent {

              override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

              val payload : String =
                Source.fromFile(getClass.getResource("/display-trust.json").getPath).mkString

              val json : JsValue = Json.parse(payload)

              val getTrust = json.as[GetTrustDesResponse].getTrust.value

              when(fakeTrustStoreConnector.get(any[String])(any(), any()))
                .thenReturn(Future.successful(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false))))

              when(fakeTrustConnector.playbackfromEtmp(any[String])(any(), any()))
                .thenReturn(Future.successful(Processed(getTrust, "9873459837459837")))

              when(fakePlaybackRepository.set(any())).thenReturn(Future.successful(true))

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustEqual routes.InformationMaintainingThisTrustController.onPageLoad().url

              application.stop()
            }
          }

          "user answers is not extracted" must {

            "render sorry there's been a problem" in  {

              lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

              val fakeTrustConnector: TrustConnector = mock[TrustConnector]
              val fakeTrustStoreConnector: TrustsStoreConnector = mock[TrustsStoreConnector]

              val userAnswers = emptyUserAnswers

              def application: Application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
                bind[TrustConnector].to(fakeTrustConnector),
                bind[TrustsStoreConnector].to(fakeTrustStoreConnector),
                bind[UserAnswersExtractor].to[FakeFailingUserAnswerExtractor],
                bind[AuthenticationService].to(new FakeAuthenticationService())
              ).build()

              val payload : String =
                Source.fromFile(getClass.getResource("/display-trust.json").getPath).mkString

              val json : JsValue = Json.parse(payload)

              val getTrust = json.as[GetTrustDesResponse].getTrust.value

              when(fakeTrustStoreConnector.get(any[String])(any(), any()))
                .thenReturn(Future.successful(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false))))

              when(fakeTrustConnector.playbackfromEtmp(any[String])(any(), any()))
                .thenReturn(Future.successful(Processed(getTrust, "9873459837459837")))

              when(playbackRepository.set(any())).thenReturn(Future.successful(true))

              val result: Future[Result] = route(application, request).value

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustEqual controllers.routes.TrustStatusController.sorryThereHasBeenAProblem().url

              application.stop()
            }

          }

        }

        "user is not authenticated for playback" must {

          "render authentication reason page" in {

            lazy val request = FakeRequest(GET, routes.TrustStatusController.status().url)

            def userAnswers = emptyUserAnswers

            val fakeTrustConnector: TrustConnector = mock[TrustConnector]
            val fakeTrustStoreConnector: TrustsStoreConnector = mock[TrustsStoreConnector]

            def application: Application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
              bind[TrustConnector].to(fakeTrustConnector),
              bind[TrustsStoreConnector].to(fakeTrustStoreConnector),
              bind[AuthenticationService].to(new FakeFailingAuthenticationService())
            ).build()

            val payload : String =
              Source.fromFile(getClass.getResource("/display-trust.json").getPath).mkString

            val json : JsValue = Json.parse(payload)

            val getTrust = json.as[GetTrustDesResponse].getTrust.value

            def result: Future[Result] = route(application, request).value

            when(fakeTrustStoreConnector.get(any[String])(any(), any()))
              .thenReturn(Future.successful(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false))))

            when(fakeTrustConnector.playbackfromEtmp(any[String])(any(), any()))
              .thenReturn(Future.successful(Processed(getTrust, "9873459837459837")))

            when(playbackRepository.set(any())).thenReturn(Future.successful(true))

            status(result) mustEqual UNAUTHORIZED

            application.stop()
          }

        }

      }
    }
    "must redirect to the correct route for GET ../status" when {
      "a Processed status is received from the trust connector" when {

        "user is authenticated for playback" when {

          "user answers is extracted" must {

            "redirect to maintain this trust for organisation" in new LocalSetup {

              override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.statusAfterVerify().url)

              val payload: String =
                Source.fromFile(getClass.getResource("/display-trust.json").getPath).mkString

              val json: JsValue = Json.parse(payload)

              val getTrust = json.as[GetTrustDesResponse].getTrust.value

              when(fakeTrustStoreConnector.get(any[String])(any(), any()))
                .thenReturn(Future.successful(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false))))

              when(fakeTrustConnector.playbackfromEtmp(any[String])(any(), any()))
                .thenReturn(Future.successful(Processed(getTrust, "9873459837459837")))

              when(fakePlaybackRepository.set(any())).thenReturn(Future.successful(true))

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustEqual routes.InformationMaintainingThisTrustController.onPageLoad().url

              application.stop()
            }
          }
        }
      }
    }
  }
}
