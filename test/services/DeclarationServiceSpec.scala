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
import java.time.LocalDate

import base.SpecBase
import connectors.TrustConnector
import models.http.DeclarationResponse.InternalServerError
import models.http.TVNResponse
import models.{AgentDeclaration, FullName, IndividualDeclaration, UKAddress}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{EitherValues, RecoverMethods}
import play.api.inject.bind
import play.api.libs.json.JsValue
import uk.gov.hmrc.auth.core.retrieve.AgentInformation
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class DeclarationServiceSpec extends SpecBase with ScalaFutures with EitherValues with RecoverMethods {

  val utr = "0987654321"
  val address: UKAddress = UKAddress("Line 1", "Line 2", None, None, "NE11NE")
  val mockTrustConnector: TrustConnector = mock[TrustConnector]
  val date: LocalDate = LocalDate.parse("2019-02-03")

  val enrolments: Enrolments = Enrolments(Set(Enrolment(
    "HMRC-AS-AGENT", Seq(EnrolmentIdentifier("AgentReferenceNumber", "SARN1234567")), "Activated"
  )))

  val agentInformation: AgentInformation = AgentInformation(None, None, Some("agentFriendlyName"))

  val agentDeclaration: AgentDeclaration = AgentDeclaration(
    name = FullName(
      "First",
      None,
      "Last"
    ),
    agencyName = "Agency Name",
    telephoneNumber = "01234567890",
    crn = "123456",
    email = None
  )

  val individualDeclaration: IndividualDeclaration = IndividualDeclaration(
    name = FullName(
      "First",
      None,
      "Last"
    ),
    email = None
  )

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "Declaration service" when {

    "agent declaration" must {

      "return TVN response when no errors" in {

        when(mockTrustConnector.declare(any[String], any[JsValue])(any(), any()))
          .thenReturn(Future.successful(TVNResponse("123456")))

        val app = applicationBuilder()
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .build()

        val service = app.injector.instanceOf[DeclarationService]

        whenReady(service.agentDeclaration(utr, agentDeclaration, "SARN1234567", address, "agentFriendlyName", Some(date))) {
          result =>
            result mustBe TVNResponse("123456")
        }
      }

      "return InternalServerError when errors" in {

        when(mockTrustConnector.declare(any[String], any[JsValue])(any(), any()))
          .thenReturn(Future.successful(InternalServerError))

        val app = applicationBuilder()
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .build()

        val service = app.injector.instanceOf[DeclarationService]

        whenReady(service.agentDeclaration(utr, agentDeclaration, "SARN1234567", address, "agentFriendlyName", Some(date))) {
          result =>
            result mustBe InternalServerError
        }
      }

    }

    "individual declaration" must {

      "return TVN response when no errors" in {

        when(mockTrustConnector.declare(any[String], any[JsValue])(any(), any()))
          .thenReturn(Future.successful(TVNResponse("123456")))

        val app = applicationBuilder()
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .build()

        val service = app.injector.instanceOf[DeclarationService]

        whenReady(service.individualDeclaration(utr, individualDeclaration, Some(date))) {
          result =>
            result mustBe TVNResponse("123456")
        }
      }

      "return InternalServerError when errors" in {

        when(mockTrustConnector.declare(any[String], any[JsValue])(any(), any()))
          .thenReturn(Future.successful(InternalServerError))

        val app = applicationBuilder()
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .build()

        val service = app.injector.instanceOf[DeclarationService]

        whenReady(service.individualDeclaration(utr, individualDeclaration, Some(date))) {
          result =>
            result mustBe InternalServerError
        }
      }

    }
  }
}
