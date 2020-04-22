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

package services

import base.SpecBase
import connectors.{TrustAuthAllowed, TrustAuthConnector, TrustAuthDenied}
import models.requests.{AgentUser, DataRequest}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{EitherValues, RecoverMethods}
import play.api.inject.bind
import play.api.mvc.AnyContent
import play.api.mvc.Results._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class AuthenticationServiceSpec extends SpecBase with ScalaFutures with EitherValues with RecoverMethods {

  private val utr = "0987654321"

  private val agentEnrolment = Enrolment("HMRC-AS-AGENT", List(EnrolmentIdentifier("AgentReferenceNumber", "SomeVal")), "Activated", None)
  private val trustsEnrolment = Enrolment("HMRC-TERS-ORG", List(EnrolmentIdentifier("SAUTR", utr)), "Activated", None)

  val enrolments = Enrolments(Set(
    agentEnrolment,
    trustsEnrolment
  ))

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private implicit val dataRequest: DataRequest[AnyContent] = DataRequest[AnyContent](fakeRequest, emptyUserAnswers, AgentUser("internalId", enrolments, "arn"))

  private val mockTrustAuthConnector = mock[TrustAuthConnector]

  "invoking the AuthenticationService" when {

    "the trust auth connector returns OK" must {

      "continue processing the request" in {

        when(mockTrustAuthConnector.authorised(any())(any(), any()))
          .thenReturn(Future.successful(TrustAuthAllowed))

        val app = applicationBuilder()
          .overrides(bind[TrustAuthConnector].toInstance(mockTrustAuthConnector))
          .build()

        val service = app.injector.instanceOf[AuthenticationService]

        whenReady(service.authenticate[AnyContent](utr)) {
          result =>
            result mustBe Right(dataRequest)
        }
      }
    }
    "the trust auth connector returns a non-OK result" must {

      "return the non-OK result" in {

        val connectorResult = TrustAuthDenied("redirect-url")

        when(mockTrustAuthConnector.authorised(any())(any(), any()))
          .thenReturn(Future.successful(connectorResult))

        val app = applicationBuilder()
          .overrides(bind[TrustAuthConnector].toInstance(mockTrustAuthConnector))
          .build()

        val service = app.injector.instanceOf[AuthenticationService]

        whenReady(service.authenticate[AnyContent](utr)) {
          result =>
            result mustBe Left(Redirect("redirect-url"))
        }
      }
    }
  }
}

