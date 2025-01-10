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

package services

import base.SpecBase
import cats.data.EitherT
import connectors.TrustConnector
import models.errors.{ServerError, TrustErrors}
import models.http.{DeclarationForApi, TVNResponse}
import models.{AgentDeclaration, FullName, IndividualDeclaration, UKAddress}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{EitherValues, RecoverMethods}
import play.api.inject.bind
import play.api.mvc.{Request, RequestHeader}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.Future

class DeclarationServiceSpec extends SpecBase with ScalaFutures with EitherValues with RecoverMethods {

  private val utr = "0987654321"
  private val address: UKAddress = UKAddress("Line 1", "Line 2", None, None, "NE11NE")
  private val mockTrustConnector: TrustConnector = mock[TrustConnector]
  private val date: LocalDate = LocalDate.parse("2019-02-03")

  private val agentDeclaration: AgentDeclaration = AgentDeclaration(
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

  private val individualDeclaration: IndividualDeclaration = IndividualDeclaration(
    name = FullName(
      "First",
      None,
      "Last"
    ),
    email = None
  )

  private implicit val request: RequestHeader = fakeRequest
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  "Declaration service" when {

    "agent declaration" must {

      "return TVN response when no errors" in {

        when(mockTrustConnector.declare(any[String], any[DeclarationForApi])(any(), any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, TVNResponse](Future.successful(Right(TVNResponse("123456")))))

        val app = applicationBuilder()
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .build()

        val service = app.injector.instanceOf[DeclarationService]

        whenReady(service.agentDeclaration(utr, agentDeclaration, "SARN1234567", address, "agentFriendlyName", Some(date)).value) {
          result =>
            result mustBe Right(TVNResponse("123456"))
        }
      }

      "return InternalServerError when errors" in {

        when(mockTrustConnector.declare(any[String], any[DeclarationForApi])(any(), any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, TVNResponse](Future.successful(Left(ServerError()))))

        val app = applicationBuilder()
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .build()

        val service = app.injector.instanceOf[DeclarationService]

        whenReady(service.agentDeclaration(utr, agentDeclaration, "SARN1234567", address, "agentFriendlyName", Some(date)).value) {
          result =>
            result mustBe Left(ServerError())
        }
      }

    }

    "individual declaration" must {

      "return TVN response when no errors" in {

        when(mockTrustConnector.declare(any[String], any[DeclarationForApi])(any(), any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, TVNResponse](Future.successful(Right(TVNResponse("123456")))))

        val app = applicationBuilder()
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .build()

        val service = app.injector.instanceOf[DeclarationService]

        whenReady(service.individualDeclaration(utr, individualDeclaration, Some(date)).value) {
          result =>
            result mustBe Right(TVNResponse("123456"))
        }
      }

      "return InternalServerError when errors" in {

        when(mockTrustConnector.declare(any[String], any[DeclarationForApi])(any(), any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, TVNResponse](Future.successful(Left(ServerError()))))

        val app = applicationBuilder()
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .build()

        val service = app.injector.instanceOf[DeclarationService]

        whenReady(service.individualDeclaration(utr, individualDeclaration, Some(date)).value) {
          result =>
            result mustBe Left(ServerError())
        }
      }

    }
  }
}
