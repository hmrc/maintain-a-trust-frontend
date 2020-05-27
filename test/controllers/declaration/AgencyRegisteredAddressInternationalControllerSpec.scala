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

package controllers.declaration

import base.SpecBase
import forms.InternationalAddressFormProvider
import models.{InternationalAddress, NormalMode, Mode}
import pages.AgencyRegisteredAddressInternationalPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.countryoptions.CountryOptionsNonUK
import views.html.declaration.AgencyRegisteredAddressInternationalView

class AgencyRegisteredAddressInternationalControllerSpec extends SpecBase {

  val formProvider = new InternationalAddressFormProvider()
  val form = formProvider()
  val mode: Mode = NormalMode
  lazy val agencyRegisteredAddressInternationalRoute = routes.AgencyRegisteredAddressInternationalController.onPageLoad(mode).url

  "AgencyRegisteredAddressInternational Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, agencyRegisteredAddressInternationalRoute)

      val view = application.injector.instanceOf[AgencyRegisteredAddressInternationalView]
      val countryOptions = application.injector.instanceOf[CountryOptionsNonUK].options

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mode, countryOptions)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .set(AgencyRegisteredAddressInternationalPage, InternationalAddress("line 1", "line 2", Some("line 3"), "country")).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, agencyRegisteredAddressInternationalRoute)

      val view = application.injector.instanceOf[AgencyRegisteredAddressInternationalView]
      val countryOptions = application.injector.instanceOf[CountryOptionsNonUK].options

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(InternationalAddress("line 1","line 2", Some("line 3"), "country")), mode, countryOptions)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, agencyRegisteredAddressInternationalRoute)
          .withFormUrlEncodedBody(("line1", "value 1"), ("line2", "value 2"), ("country", "DE"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.AgentDeclarationController.onPageLoad(mode).url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, agencyRegisteredAddressInternationalRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[AgencyRegisteredAddressInternationalView]
      val countryOptions = application.injector.instanceOf[CountryOptionsNonUK].options

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, mode, countryOptions)(fakeRequest, messages).toString

      application.stop()
    }
  }
}
