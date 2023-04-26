/*
 * Copyright 2023 HM Revenue & Customs
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
import cats.data.EitherT
import connectors.{TrustClaim, TrustConnector, TrustsStoreConnector}
import mapping.{FakeUserAnswerExtractor, UserAnswersExtractor}
import models.errors.{FailedToExtractData, ServerError, TrustErrors}
import models.http._
import models.{TrustDetails, UTR, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json._
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.PlaybackRepository
import services.{AuthenticationService, FakeAuthenticationService, FakeFailingAuthenticationService, SessionService}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier
import views.html.status._

import java.time.LocalDate
import scala.concurrent.Future
import scala.io.Source

class TrustStatusControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val mockSessionService: SessionService = mock[SessionService]

  trait BaseSetup {

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val builder: GuiceApplicationBuilder

    def utr = "1234567890"

    def userAnswers: UserAnswers = emptyUserAnswersForUtr

    val fakeTrustConnector: TrustConnector = mock[TrustConnector]
    val fakeTrustStoreConnector: TrustsStoreConnector = mock[TrustsStoreConnector]
    val fakePlaybackRepository: PlaybackRepository = mock[PlaybackRepository]

    def request: FakeRequest[AnyContentAsEmpty.type]

    def result: Future[Result] = route(application, request).value

    lazy val application: Application = builder.overrides(
      bind[TrustConnector].to(fakeTrustConnector),
      bind[TrustsStoreConnector].to(fakeTrustStoreConnector),
      bind[AuthenticationService].to(new FakeAuthenticationService()),
      bind[UserAnswersExtractor].to(new FakeUserAnswerExtractor(Right(userAnswers))),
      bind[SessionService].toInstance(mockSessionService)
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

      contentAsString(result) mustEqual view(AffinityGroup.Individual, utr, UTR)(request, messages).toString

      application.stop()
    }

    "must return OK and the correct view for GET ../status/processing" in new LocalSetup {

      override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.processing().url)

      val view: TrustStillProcessingView = application.injector.instanceOf[TrustStillProcessingView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(AffinityGroup.Individual, utr, UTR)(request, messages).toString

      application.stop()
    }

    "must return OK and the correct view for GET ../status/not-found" in new LocalSetup {

      override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.notFound().url)

      val view: IdentifierDoesNotMatchView = application.injector.instanceOf[IdentifierDoesNotMatchView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(AffinityGroup.Individual, utr, UTR)(request, messages).toString

      application.stop()
    }

    "must return OK and the correct view for GET ../status/locked" in new LocalSetup {

      override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.locked().url)

      val view: TrustLockedView = application.injector.instanceOf[TrustLockedView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(utr, UTR)(request, messages).toString

      application.stop()
    }

    "must return OK and the correct view for GET ../status/already-claimed" in new LocalSetup {

      override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.alreadyClaimed().url)

      val view: TrustAlreadyClaimedView = application.injector.instanceOf[TrustAlreadyClaimedView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(utr, UTR)(request, messages).toString

      application.stop()
    }

    "must return OK and the correct view for GET ../status/sorry-there-has-been-a-problem" in new LocalSetup {

      override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.sorryThereHasBeenAProblem().url)

      val view: PlaybackProblemContactHMRCView = application.injector.instanceOf[PlaybackProblemContactHMRCView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(utr, UTR)(request, messages).toString

      application.stop()
    }

    "must return SERVICE_UNAVAILABLE and the correct view for GET ../status/down" in new LocalSetup {

      override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.down().url)

      val view: IVDownView = application.injector.instanceOf[IVDownView]

      status(result) mustEqual SERVICE_UNAVAILABLE

      contentAsString(result) mustEqual
        view(utr, UTR)(request, messages).toString

      application.stop()
    }

    "must redirect to the correct route for GET ../status/start" when {

      "a Closed status is received from the trust connector" in new LocalSetup {

        override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

        when(fakeTrustStoreConnector.get(any[String])(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, Option[TrustClaim]]
            (Future.successful(Right(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false)))))
          )

        when(fakeTrustConnector.playbackFromEtmp(any[String])(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, TrustsResponse](Future.successful(Right(Closed))))

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "/maintain-a-trust/status/closed"

        application.stop()
      }

      "a Processing status is received from the trust connector" in new LocalSetup {

        override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

        when(fakeTrustStoreConnector.get(any[String])(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, Option[TrustClaim]]
            (Future.successful(Right(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false)))))
          )

        when(fakeTrustConnector.playbackFromEtmp(any[String])(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, TrustsResponse](Future.successful(Right(Processing))))

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "/maintain-a-trust/status/processing"

        application.stop()
      }

      "a NotFound status is received from the trust connector" in new LocalSetup {

        override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

        when(fakeTrustStoreConnector.get(any[String])(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, Option[TrustClaim]]
            (Future.successful(Right(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false)))))
          )

        when(fakeTrustConnector.playbackFromEtmp(any[String])(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, TrustsResponse](Future.successful(Right(IdentifierNotFound))))

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "/maintain-a-trust/status/not-found"

        application.stop()
      }

      "A locked trust claim is returned from the trusts store connector" in new LocalSetup {

        override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

        when(fakeTrustStoreConnector.get(any[String])(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, Option[TrustClaim]]
            (Future.successful(Right(Some(TrustClaim("utr", trustLocked = true, managedByAgent = false)))))
          )

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "/maintain-a-trust/status/locked"

        application.stop()
      }

      "a ServiceUnavailable status is received from the trust connector" in new LocalSetup {

        override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

        when(fakeTrustStoreConnector.get(any[String])(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, Option[TrustClaim]]
            (Future.successful(Right(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false)))))
          )

        when(fakeTrustConnector.playbackFromEtmp(any[String])(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, TrustsResponse](Future.successful(Right(TrustServiceUnavailable))))

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "/maintain-a-trust/status/down"

        application.stop()
      }

      "a ClosedRequest status is received from the trust connector" in new LocalSetup {

        override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

        when(fakeTrustStoreConnector.get(any[String])(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, Option[TrustClaim]]
            (Future.successful(Right(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false)))))
          )

        when(fakeTrustConnector.playbackFromEtmp(any[String])(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, TrustsResponse](Future.successful(Right(ClosedRequestResponse))))

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "/maintain-a-trust/status/down"

        application.stop()
      }

      "a ServerError status is received from the trust connector" in new LocalSetup {

        override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

        when(fakeTrustStoreConnector.get(any[String])(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, Option[TrustClaim]]
            (Future.successful(Right(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false)))))
          )

        when(fakeTrustConnector.playbackFromEtmp(any[String])(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, TrustsResponse](Future.successful(Right(TrustsErrorResponse(INTERNAL_SERVER_ERROR)))))

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "/maintain-a-trust/status/down"

        application.stop()
      }

      "a Processed status is received from the trust connector" when {

        "user is authenticated for playback" when {

          "user answers is extracted" must {

            "redirect to express trust for underlying 4mld trust data in 5mld mode" in new LocalSetup {

              override def userAnswers: UserAnswers = super.userAnswers

              override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

              val payload: String =
                Source.fromFile(getClass.getResource("/display-trust.json").getPath).mkString

              val json: JsValue = Json.parse(payload)

              val getTrust: GetTrust = json.as[GetTrustDesResponse].getTrust.value

              when(fakeTrustStoreConnector.get(any[String])(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, Option[TrustClaim]]
                  (Future.successful(Right(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false)))))
                )

              when(fakeTrustConnector.playbackFromEtmp(any[String])(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, TrustsResponse](Future.successful(Right(Processed(getTrust, "9873459837459837")))))

              when(fakeTrustConnector.getUntransformedTrustDetails(any())(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, TrustDetails]
                  (Future.successful(Right(TrustDetails(LocalDate.now, trustTaxable = None, expressTrust = None, schedule3aExempt = None))))
                )

              when(fakePlaybackRepository.resetCache(any(), any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, Option[Boolean]](Future.successful(Right(Some(true)))))

              when(fakePlaybackRepository.set(any()))
                .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

              when(mockActiveSessionRepository.set(any()))
                .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

              when(mockSessionService.initialiseUserAnswers(any(), any(), any(), any())(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, UserAnswers](Future.successful(Right(userAnswers.copy(isUnderlyingData5mld = false)))))

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustEqual controllers.routes.MigrateTo5mldInformationController.onPageLoad().url

              application.stop()
            }

            "redirect to maintain this trust for organisation" in new LocalSetup {

              override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

              val payload: String =
                Source.fromFile(getClass.getResource("/display-trust.json").getPath).mkString

              val json: JsValue = Json.parse(payload)

              val getTrust: GetTrust = json.as[GetTrustDesResponse].getTrust.value

              when(fakeTrustStoreConnector.get(any[String])(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, Option[TrustClaim]]
                  (Future.successful(Right(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false)))))
                )

              when(fakeTrustConnector.getUntransformedTrustDetails(any())(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, TrustDetails](Future.successful(Right(
                  TrustDetails(LocalDate.now, trustTaxable = Some(true), expressTrust = Some(true), schedule3aExempt = Some(true))
                ))))

              when(fakeTrustConnector.playbackFromEtmp(any[String])(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, TrustsResponse](Future.successful(Right(Processed(getTrust, "9873459837459837")))))

              when(fakePlaybackRepository.resetCache(any(), any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, Option[Boolean]](Future.successful(Right(Some(true)))))

              when(fakePlaybackRepository.set(any()))
                .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

              when(mockActiveSessionRepository.set(any()))
                .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

              when(mockSessionService.initialiseUserAnswers(any(), any(), any(), any())(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, UserAnswers](Future.successful(Right(
                  userAnswers.copy(isUnderlyingData5mld = true, isUnderlyingDataTaxable = false)
                ))))

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustEqual routes.MaintainThisTrustController.onPageLoad(needsIv = false).url

              application.stop()
            }

            "redirect to information maintaining this trust for agent" in new LocalSetupForAgent {
              override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

              val payload: String =
                Source.fromFile(getClass.getResource("/display-trust.json").getPath).mkString

              val json: JsValue = Json.parse(payload)

              val getTrust: GetTrust = json.as[GetTrustDesResponse].getTrust.value

              when(fakeTrustStoreConnector.get(any[String])(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, Option[TrustClaim]]
                  (Future.successful(Right(Some(TrustClaim("utr", trustLocked = false, managedByAgent = true)))))
                )

              when(fakeTrustConnector.getUntransformedTrustDetails(any())(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, TrustDetails](Future.successful(Right(
                  TrustDetails(LocalDate.now, trustTaxable = Some(true), expressTrust = Some(true), schedule3aExempt = Some(false))
                ))))

              when(fakePlaybackRepository.resetCache(any(), any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, Option[Boolean]](Future.successful(Right(Some(true)))))

              when(fakeTrustConnector.playbackFromEtmp(any[String])(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, TrustsResponse](Future.successful(Right(Processed(getTrust, "9873459837459837")))))

              when(fakePlaybackRepository.set(any()))
                .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

              when(mockActiveSessionRepository.set(any()))
                .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

              when(mockSessionService.initialiseUserAnswers(any(), any(), any(), any())(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, UserAnswers](Future.successful(Right(
                  userAnswers.copy(isUnderlyingData5mld = true, isUnderlyingDataTaxable = false)
                ))))

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustEqual routes.InformationMaintainingThisTrustController.onPageLoad().url

              application.stop()
            }

            "redirect to interrupt page if schedule3a question has not been answered" in new LocalSetup {

              override def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

              val payload: String =
                Source.fromFile(getClass.getResource("/display-trust.json").getPath).mkString

              val json: JsValue = Json.parse(payload)

              val getTrust: GetTrust = json.as[GetTrustDesResponse].getTrust.value

              when(fakeTrustStoreConnector.get(any[String])(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, Option[TrustClaim]]
                  (Future.successful(Right(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false)))))
                )

              when(fakeTrustConnector.getUntransformedTrustDetails(any())(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, TrustDetails]
                  (Future.successful(Right(TrustDetails(LocalDate.now, trustTaxable = Some(true), expressTrust = Some(true), schedule3aExempt = None))))
                )

              when(fakeTrustConnector.playbackFromEtmp(any[String])(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, TrustsResponse](Future.successful(Right(Processed(getTrust, "9873459837459837")))))

              when(fakePlaybackRepository.resetCache(any(), any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, Option[Boolean]](Future.successful(Right(Some(true)))))

              when(fakePlaybackRepository.set(any()))
                .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

              when(mockActiveSessionRepository.set(any()))
                .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

              when(mockSessionService.initialiseUserAnswers(any(), any(), any(), any())(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, UserAnswers](Future.successful(Right(
                  userAnswers.copy(isUnderlyingData5mld = true, isUnderlyingDataTaxable = false)
                ))))

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustEqual routes.InformationSchedule3aExemptionController.onPageLoad().url

              application.stop()
            }
          }

          "user answers is not extracted" must {

            "render sorry there's been a problem" in {

              lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.TrustStatusController.status().url)

              val fakeTrustConnector: TrustConnector = mock[TrustConnector]
              val fakeTrustStoreConnector: TrustsStoreConnector = mock[TrustsStoreConnector]
              val fakeUserAnswerExtractor: UserAnswersExtractor = mock[UserAnswersExtractor]

              when(fakeUserAnswerExtractor.extract(any(), any())(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, UserAnswers](Future.successful(Left(FailedToExtractData("No beneficiaries")))))

              val userAnswers = emptyUserAnswersForUtr

              def application: Application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
                bind[TrustConnector].to(fakeTrustConnector),
                bind[TrustsStoreConnector].to(fakeTrustStoreConnector),
                bind[UserAnswersExtractor].to(fakeUserAnswerExtractor),
                bind[AuthenticationService].to(new FakeAuthenticationService()),
                bind[SessionService].toInstance(mockSessionService)
              ).build()

              val payload: String =
                Source.fromFile(getClass.getResource("/display-trust.json").getPath).mkString

              val json: JsValue = Json.parse(payload)

              val getTrust = json.as[GetTrustDesResponse].getTrust.value

              when(fakeTrustStoreConnector.get(any[String])(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, Option[TrustClaim]]
                  (Future.successful(Right(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false)))))
                )

              when(fakeTrustConnector.playbackFromEtmp(any[String])(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, TrustsResponse](Future.successful(Right(Processed(getTrust, "9873459837459837")))))

              when(fakeTrustConnector.getUntransformedTrustDetails(any())(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, TrustDetails]
                  (Future.successful(Right(TrustDetails(LocalDate.now, trustTaxable = None, expressTrust = None, schedule3aExempt = None))))
                )

              when(mockPlaybackRepository.resetCache(any(), any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, Option[Boolean]](Future.successful(Right(Some(true)))))

              when(mockPlaybackRepository.set(any()))
                .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

              when(mockActiveSessionRepository.set(any()))
                .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(false))))

              when(mockSessionService.initialiseUserAnswers(any(), any(), any(), any())(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, UserAnswers](Future.successful(Left(ServerError()))))

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

            def userAnswers = emptyUserAnswersForUtr

            val fakeTrustConnector: TrustConnector = mock[TrustConnector]
            val fakeTrustStoreConnector: TrustsStoreConnector = mock[TrustsStoreConnector]

            def application: Application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
              bind[TrustConnector].to(fakeTrustConnector),
              bind[TrustsStoreConnector].to(fakeTrustStoreConnector),
              bind[AuthenticationService].to(new FakeFailingAuthenticationService()),
              bind[SessionService].toInstance(mockSessionService)
            ).build()

            val payload: String =
              Source.fromFile(getClass.getResource("/display-trust.json").getPath).mkString

            val json: JsValue = Json.parse(payload)

            val getTrust = json.as[GetTrustDesResponse].getTrust.value

            def result: Future[Result] = route(application, request).value

            when(fakeTrustStoreConnector.get(any[String])(any(), any()))
              .thenReturn(EitherT[Future, TrustErrors, Option[TrustClaim]]
                (Future.successful(Right(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false)))))
              )

            when(fakeTrustConnector.getUntransformedTrustDetails(any())(any(), any()))
              .thenReturn(EitherT[Future, TrustErrors, TrustDetails](Future.successful(Right(
                TrustDetails(LocalDate.now, trustTaxable = Some(true), expressTrust = Some(true), schedule3aExempt = None)
              ))))

            when(fakeTrustConnector.playbackFromEtmp(any[String])(any(), any()))
              .thenReturn(EitherT[Future, TrustErrors, TrustsResponse](Future.successful(Right(Processed(getTrust, "9873459837459837")))))

            when(mockPlaybackRepository.resetCache(any(), any(), any()))
              .thenReturn(EitherT[Future, TrustErrors, Option[Boolean]](Future.successful(Right(Some(true)))))

            when(mockPlaybackRepository.set(any()))
              .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

            when(mockActiveSessionRepository.set(any()))
              .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

            when(mockSessionService.initialiseUserAnswers(any(), any(), any(), any())(any(), any()))
              .thenReturn(EitherT[Future, TrustErrors, UserAnswers](Future.successful(Right(userAnswers))))

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

              val getTrust: GetTrust = json.as[GetTrustDesResponse].getTrust.value

              when(fakeTrustStoreConnector.get(any[String])(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, Option[TrustClaim]]
                  (Future.successful(Right(Some(TrustClaim("utr", trustLocked = false, managedByAgent = false)))))
                )

              when(fakeTrustConnector.playbackFromEtmp(any[String])(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, TrustsResponse](Future.successful(Right(Processed(getTrust, "9873459837459837")))))

              when(fakeTrustConnector.getUntransformedTrustDetails(any())(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, TrustDetails](Future.successful(Right(
                  TrustDetails(LocalDate.now, trustTaxable = Some(true), expressTrust = Some(true), schedule3aExempt = Some(true))
                ))))

              when(fakePlaybackRepository.resetCache(any(), any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, Option[Boolean]](Future.successful(Right(Some(true)))))

              when(fakePlaybackRepository.set(any()))
                .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

              when(mockActiveSessionRepository.set(any()))
                .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

              when(mockSessionService.initialiseUserAnswers(any(), any(), any(), any())(any(), any()))
                .thenReturn(EitherT[Future, TrustErrors, UserAnswers](Future.successful(Right(
                  userAnswers.copy(isUnderlyingData5mld = true, isUnderlyingDataTaxable = false)
                ))))

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
