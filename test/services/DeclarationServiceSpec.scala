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
import connectors.TrustConnector
import models.http.DeclarationResponse.{CannotDeclareError, InternalServerError}
import models.http.{NameType, TVNResponse}
import models.{AgentDeclaration, IndividualDeclaration, UKAddress}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{EitherValues, RecoverMethods}
import pages.declaration.AgencyRegisteredAddressUkYesNoPage
import pages.trustees.{IsThisLeadTrusteePage, TrusteeAddressPage}
import pages.{AgencyRegisteredAddressUkPage, UTRPage}
import play.api.inject.bind
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class DeclarationServiceSpec extends SpecBase with ScalaFutures with EitherValues with RecoverMethods {

  val utr = "0987654321"
  val address: UKAddress = UKAddress("Line 1", "Line 2", None, None, "NE11NE")
  val mockTrustConnector: TrustConnector = mock[TrustConnector]

  val agentDeclaration: AgentDeclaration = AgentDeclaration(
    name = NameType(
      "First",
      None,
      "Last"
    ),
    crn = "123456",
    email = None
  )

  val individualDeclaration: IndividualDeclaration = IndividualDeclaration(
    name = NameType(
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

        val userAnswers = emptyUserAnswers
          .set(UTRPage, utr).success.value
          .set(AgencyRegisteredAddressUkYesNoPage, true).success.value
          .set(AgencyRegisteredAddressUkPage, address).success.value

        val app = applicationBuilder()
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .build()

        val service = app.injector.instanceOf[DeclarationService]

        whenReady(service.declareNoChange(utr, agentDeclaration, userAnswers)) {
          result =>
            result mustBe TVNResponse("123456")
        }
      }

      "return InternalServerError when errors" in {

        when(mockTrustConnector.declare(any[String], any[JsValue])(any(), any()))
          .thenReturn(Future.successful(InternalServerError))

        val userAnswers = emptyUserAnswers
          .set(UTRPage, utr).success.value
          .set(AgencyRegisteredAddressUkYesNoPage, true).success.value
          .set(AgencyRegisteredAddressUkPage, address).success.value

        val app = applicationBuilder()
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .build()

        val service = app.injector.instanceOf[DeclarationService]

        whenReady(service.declareNoChange(utr, agentDeclaration, userAnswers)) {
          result =>
            result mustBe InternalServerError
        }
      }

      "return cannot declare error when invalid data" in {

        val userAnswers = emptyUserAnswers
          .set(UTRPage, utr).success.value

        val app = applicationBuilder()
          .build()

        val service = app.injector.instanceOf[DeclarationService]

        whenReady(service.declareNoChange(utr, agentDeclaration, userAnswers)) {
          result =>
            result mustBe CannotDeclareError
        }
      }

    }

    "individual declaration" must {

      "return TVN response when no errors" in {

        when(mockTrustConnector.declare(any[String], any[JsValue])(any(), any()))
          .thenReturn(Future.successful(TVNResponse("123456")))

        val userAnswers = emptyUserAnswers
          .set(UTRPage, utr).success.value
          .set(IsThisLeadTrusteePage(0), false).success.value
          .set(TrusteeAddressPage(0), UKAddress("Line 1", "Line 2", None, None, "NE11NE")).success.value
          .set(IsThisLeadTrusteePage(1), true).success.value
          .set(TrusteeAddressPage(1), UKAddress("Line 1", "Line 2", None, None, "NE11NE")).success.value

        val app = applicationBuilder()
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .build()

        val service = app.injector.instanceOf[DeclarationService]

        whenReady(service.declareNoChange(utr, individualDeclaration, userAnswers)) {
          result =>
            result mustBe TVNResponse("123456")
        }
      }

      "return InternalServerError when errors" in {

        when(mockTrustConnector.declare(any[String], any[JsValue])(any(), any()))
          .thenReturn(Future.successful(InternalServerError))

        val userAnswers = emptyUserAnswers
          .set(UTRPage, utr).success.value
          .set(IsThisLeadTrusteePage(0), true).success.value
          .set(TrusteeAddressPage(0), address).success.value

        val app = applicationBuilder()
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .build()

        val service = app.injector.instanceOf[DeclarationService]

        whenReady(service.declareNoChange(utr, individualDeclaration, userAnswers)) {
          result =>
            result mustBe InternalServerError
        }
      }

      "return cannot declare error when invalid data" in {

        val userAnswers = emptyUserAnswers
          .set(UTRPage, utr).success.value
          .set(IsThisLeadTrusteePage(0), true).success.value

        val app = applicationBuilder()
          .build()

        val service = app.injector.instanceOf[DeclarationService]

        whenReady(service.declareNoChange(utr, individualDeclaration, userAnswers)) {
          result =>
            result mustBe CannotDeclareError
        }
      }

    }
  }
}
