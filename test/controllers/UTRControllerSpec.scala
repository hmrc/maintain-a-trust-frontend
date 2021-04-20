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

package controllers

import base.SpecBase
import connectors.TrustConnector
import forms.UTRFormProvider
import play.api.data.Form
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.FeatureFlagService
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import views.html.UTRView

import java.time.LocalDate

class UTRControllerSpec extends SpecBase {

  val formProvider = new UTRFormProvider()
  val form: Form[String] = formProvider()

  lazy val trustUTRRoute: String = routes.UTRController.onPageLoad().url

  lazy val onSubmit: Call = routes.UTRController.onSubmit()

  val mockFeatureFlagService: FeatureFlagService = mock[FeatureFlagService]
  val mockTrustsConnector: TrustConnector = mock[TrustConnector]

  val utr = "0987654321"

  val enrolments: Enrolments = Enrolments(Set(Enrolment(
    "HMRC-TERS-ORG", Seq(EnrolmentIdentifier("SAUTR", utr)), "Activated"
  )))

  val startDate: LocalDate = LocalDate.parse("2000-01-01")

  "UTR Controller" must {

    "return OK and the correct view for a GET if no existing data is found (creating a new session)" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, trustUTRRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[UTRView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, onSubmit)(request, messages).toString

      application.stop()
    }

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

      val request = FakeRequest(GET, trustUTRRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[UTRView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, onSubmit)(request, messages).toString

      application.stop()
    }

    "redirect to trust status on a POST when there is no session" in {
      val application =
        applicationBuilder(userAnswers = None, affinityGroup = Organisation, enrolments = enrolments)
          .build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, trustUTRRoute)
        .withFormUrlEncodedBody(("value", utr))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.TrustStatusController.status().url

      application.stop()
    }

    "redirect to trust status on a POST" in {
      val application =
        applicationBuilder(
          userAnswers = Some(emptyUserAnswersForUtr),
          affinityGroup = Organisation,
          enrolments = enrolments
        ).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, trustUTRRoute)
        .withFormUrlEncodedBody(("value", utr))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.TrustStatusController.status().url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
        .build()

      val request = FakeRequest(POST, trustUTRRoute)
        .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[UTRView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, onSubmit)(request, messages).toString

      application.stop()
    }
  }
}
