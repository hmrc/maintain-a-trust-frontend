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

package controllers.auth

import base.SpecBase
import config.FrontendAppConfig
import connectors.EnrolmentStoreConnector
import controllers.actions.TrustsAuthorisedFunctions
import models.requests.EnrolmentStoreResponse.{AlreadyClaimed, NotClaimed, ServerError}
import org.mockito.Matchers.{any, eq => mEq}
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{EitherValues, RecoverMethods}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{EmptyRetrieval, Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class TrustAuthControllerSpec extends SpecBase with ScalaFutures with EitherValues with RecoverMethods {

  private val utr = "0987654321"

  private val appConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  private val agentEnrolment = Enrolment("HMRC-AS-AGENT", List(EnrolmentIdentifier("AgentReferenceNumber", "SomeVal")), "Activated", None)
  private val trustsEnrolment = Enrolment("HMRC-TERS-ORG", List(EnrolmentIdentifier("SAUTR", utr)), "Activated", None)

  private val enrolments = Enrolments(Set(
    agentEnrolment,
    trustsEnrolment
  ))

  private val mockAuthConnector: AuthConnector = mock[AuthConnector]
  private val mockEnrolmentStoreConnector: EnrolmentStoreConnector = mock[EnrolmentStoreConnector]

  private type RetrievalType = Option[String] ~ Option[AffinityGroup] ~ Enrolments

  private def authRetrievals(affinityGroup: AffinityGroup, enrolment: Enrolments) =
    Future.successful(new ~(new ~(Some("id"), Some(affinityGroup)), enrolment))

  private lazy val trustsAuth = new TrustsAuthorisedFunctions(mockAuthConnector, appConfig)

  private def applicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(bind[TrustsAuthorisedFunctions].toInstance(trustsAuth))
      .overrides(bind[EnrolmentStoreConnector].toInstance(mockEnrolmentStoreConnector))

  "invoking the TrustAuthController" when {

    "authenticating an agent" when {

      "trust is claimed and agent is authorised" must {

        "return OK" in {

          when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
            .thenReturn(authRetrievals(AffinityGroup.Agent, enrolments))

          val predicatedMatcher = mEq(
            Enrolment("HMRC-TERS-ORG")
              .withIdentifier("SAUTR", utr)
              .withDelegatedAuthRule("trust-auth")
          )

          when(mockAuthConnector.authorise(predicatedMatcher, mEq(EmptyRetrieval))(any(), any()))
            .thenReturn(Future.successful(()))

          when(mockEnrolmentStoreConnector.checkIfAlreadyClaimed(mEq(utr))(any(), any()))
            .thenReturn(Future.successful(AlreadyClaimed))

          val app = applicationBuilder().build()

          val request = FakeRequest(GET, controllers.auth.routes.TrustAuthController.authorised(utr).url)

          val result = route(app, request).value

          status(result) mustBe OK
        }
      }

      "trust has not been claimed by a trustee" must {

        "redirect to trust not claimed page" in  {

          when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
            .thenReturn(authRetrievals(AffinityGroup.Agent, enrolments))

          when(mockEnrolmentStoreConnector.checkIfAlreadyClaimed(mEq(utr))(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(NotClaimed))

          val app = applicationBuilder().build()

          val request = FakeRequest(GET, controllers.auth.routes.TrustAuthController.authorised(utr).url)

          val result = route(app, request).value
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.TrustNotClaimedController.onPageLoad().url
        }
      }

      "agent has not been authorised for any trusts" must {

        "redirect to agent not authorised" in {

          val enrolments = Enrolments(Set(agentEnrolment))

          when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
            .thenReturn(authRetrievals(AffinityGroup.Agent, enrolments))

          val predicatedMatcher = mEq(
            Enrolment("HMRC-TERS-ORG")
              .withIdentifier("SAUTR", utr)
              .withDelegatedAuthRule("trust-auth")
          )

          when(mockAuthConnector.authorise(predicatedMatcher, mEq(EmptyRetrieval))(any(), any()))
            .thenReturn(Future.failed(InsufficientEnrolments()))

          when(mockEnrolmentStoreConnector.checkIfAlreadyClaimed(mEq(utr))(any(), any()))
            .thenReturn(Future.successful(AlreadyClaimed))

          val app = applicationBuilder().build()

          val request = FakeRequest(GET, controllers.auth.routes.TrustAuthController.authorised(utr).url)

          val result = route(app, request).value
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.AgentNotAuthorisedController.onPageLoad().url
        }
      }

      "an agent that has a trusts enrolment without matching submitted utr" must {

        "redirect to agent not authorised" in {

          val enrolments = Enrolments(Set(
            agentEnrolment,
            Enrolment("HMRC-TERS-ORG", List(EnrolmentIdentifier("SAUTR", "1234567890")), "Activated", None)
          ))

          when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
            .thenReturn(authRetrievals(AffinityGroup.Agent, enrolments))

          val predicatedMatcher = mEq(
            Enrolment("HMRC-TERS-ORG")
              .withIdentifier("SAUTR", utr)
              .withDelegatedAuthRule("trust-auth")
          )

          when(mockAuthConnector.authorise(predicatedMatcher, mEq(EmptyRetrieval))(any(), any()))
            .thenReturn(Future.failed(InsufficientEnrolments()))


          when(mockEnrolmentStoreConnector.checkIfAlreadyClaimed(mEq(utr))(any(), any()))
            .thenReturn(Future.successful(AlreadyClaimed))

          val app = applicationBuilder().build()

          val request = FakeRequest(GET, controllers.auth.routes.TrustAuthController.authorised(utr).url)

          val result = route(app, request).value
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.AgentNotAuthorisedController.onPageLoad().url
        }
      }

    }

    "authenticating an organisation user" when {

      "organisation user has an enrolment for the trust" when {

        "relationship does not exist in Trust IV" must {

          "redirect to trust IV for a non claiming check" in {

            val enrolments = Enrolments(Set(trustsEnrolment))

            when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
              .thenReturn(authRetrievals(AffinityGroup.Organisation, enrolments))

            when(mockAuthConnector.authorise(any[Relationship], mEq(EmptyRetrieval))(any(), any()))
              .thenReturn(Future.failed(FailedRelationship()))

            val app = applicationBuilder().build()

            val request = FakeRequest(GET, controllers.auth.routes.TrustAuthController.authorised(utr).url)

            val result = route(app, request).value
            status(result) mustBe SEE_OTHER
            redirectLocation(result).value must include("/verify-your-identity-for-a-trust")
          }

        }

        "relationship does exist in Trust IV" must {

          "return OK" in {

            val enrolments = Enrolments(Set(trustsEnrolment))

            when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
              .thenReturn(authRetrievals(AffinityGroup.Organisation, enrolments))

            when(mockAuthConnector.authorise(any[Relationship], mEq(EmptyRetrieval))(any(), any()))
              .thenReturn(Future.successful(()))

            val app = applicationBuilder().build()

            val request = FakeRequest(GET, controllers.auth.routes.TrustAuthController.authorised(utr).url)

            val result = route(app, request).value
            status(result) mustBe OK
          }
        }
      }

      "organisation user has no enrolment for the trust" when {

        "unable to determine if the UTR belongs to a different org account" must {

          "redirect to tech difficulties" in {
            val enrolments = Enrolments(Set())

            when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
              .thenReturn(authRetrievals(AffinityGroup.Organisation, enrolments))

            when(mockEnrolmentStoreConnector.checkIfAlreadyClaimed(mEq(utr))(any(), any()))
              .thenReturn(Future.successful(ServerError))

            val app = applicationBuilder().build()

            val request = FakeRequest(GET, controllers.auth.routes.TrustAuthController.authorised(utr).url)

            val result = route(app, request).value
            status(result) mustBe INTERNAL_SERVER_ERROR
          }
        }

        "utr is already claimed by a different org account" must {

          "redirect to already claimed" in {

            val enrolments = Enrolments(Set())

            when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
              .thenReturn(authRetrievals(AffinityGroup.Organisation, enrolments))

            when(mockEnrolmentStoreConnector.checkIfAlreadyClaimed(mEq(utr))(any(), any()))
              .thenReturn(Future.successful(AlreadyClaimed))

            val app = applicationBuilder().build()

            val request = FakeRequest(GET, controllers.auth.routes.TrustAuthController.authorised(utr).url)

            val result = route(app, request).value
            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe controllers.routes.TrustStatusController.alreadyClaimed().url
          }
        }

        "utr is not already claimed by an org account" must {

          "redirect to claim a trust" in {

            val enrolments = Enrolments(Set())

            when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
              .thenReturn(authRetrievals(AffinityGroup.Organisation, enrolments))

            when(mockEnrolmentStoreConnector.checkIfAlreadyClaimed(mEq(utr))(any(), any()))
              .thenReturn(Future.successful(NotClaimed))

            val app = applicationBuilder().build()

            val request = FakeRequest(GET, controllers.auth.routes.TrustAuthController.authorised(utr).url)

            val result = route(app, request).value
            status(result) mustBe SEE_OTHER
            redirectLocation(result).value must include("/claim-a-trust")
          }
        }
      }
    }
  }

  "passing a non authenticated request" must {

    "redirect to the login page" in {

      val app = applicationBuilder().build()

      when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
        .thenReturn(Future failed BearerTokenExpired())

      val request = FakeRequest(GET, controllers.auth.routes.TrustAuthController.authorised(utr).url)

      val futuristicResult = route(app, request).value
      recoverToSucceededIf[BearerTokenExpired](futuristicResult)
    }
  }

}
