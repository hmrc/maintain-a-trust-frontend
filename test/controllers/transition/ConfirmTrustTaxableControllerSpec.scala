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

package controllers.transition

import base.SpecBase
import connectors.TrustConnector
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import uk.gov.hmrc.http.HttpResponse
import views.html.transition.ConfirmTrustTaxableView

import scala.concurrent.Future

class ConfirmTrustTaxableControllerSpec extends SpecBase with MockitoSugar {

  lazy val confirmTrustTaxableRoute: String = routes.ConfirmTrustTaxableController.onPageLoad().url

  "ConfirmTrustTaxableController" when {

    ".onPageLoad" must {
      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

        val request = FakeRequest(GET, confirmTrustTaxableRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ConfirmTrustTaxableView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view()(request, messages).toString

        application.stop()
      }
    }

    ".onSubmit" must {
      "redirect to the next page" when {

        "agent" in {

          val mockTrustsConnector = mock[TrustConnector]

          when(mockTrustsConnector.setTaxableTrust(any(), any())(any(), any()))
            .thenReturn(Future.successful(HttpResponse(OK, "")))

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr), affinityGroup = Agent)
            .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
            .build()

          val request = FakeRequest(POST, confirmTrustTaxableRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.declaration.routes.AgencyRegisteredAddressUkYesNoController.onPageLoad().url

          application.stop()
        }

        "non-agent" in {

          val mockTrustsConnector = mock[TrustConnector]

          when(mockTrustsConnector.setTaxableTrust(any(), any())(any(), any()))
            .thenReturn(Future.successful(HttpResponse(OK, "")))

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr), affinityGroup = Organisation)
            .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
            .build()

          val request = FakeRequest(POST, confirmTrustTaxableRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.declaration.routes.IndividualDeclarationController.onPageLoad().url

          application.stop()
        }
      }
    }
  }
}
