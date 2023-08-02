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

package controllers.declaration

import base.SpecBase
import forms.InternationalAddressFormProvider
import models.errors.ServerError
import models.pages.WhatIsNext.{MakeChanges, NeedsToPayTax}
import models.{InternationalAddress, UserAnswers}
import pages.WhatIsNextPage
import pages.declaration.AgencyRegisteredAddressInternationalPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.countryoptions.CountryOptionsNonUK
import views.html.declaration.AgencyRegisteredAddressInternationalView

class AgencyRegisteredAddressInternationalControllerSpec extends SpecBase {

  private val formProvider = new InternationalAddressFormProvider()
  private val form = formProvider()
  private lazy val agencyRegisteredAddressInternationalRoute = routes.AgencyRegisteredAddressInternationalController.onPageLoad().url

  private val baseAnswers: UserAnswers = emptyUserAnswersForUtr
    .set(WhatIsNextPage, MakeChanges).value

  "AgencyRegisteredAddressInternational Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request = FakeRequest(GET, agencyRegisteredAddressInternationalRoute)

      val view = application.injector.instanceOf[AgencyRegisteredAddressInternationalView]
      val countryOptions = application.injector.instanceOf[CountryOptionsNonUK].options()

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, countryOptions)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers
        .set(AgencyRegisteredAddressInternationalPage, InternationalAddress("line 1", "line 2", Some("line 3"), "country")).value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, agencyRegisteredAddressInternationalRoute)

      val view = application.injector.instanceOf[AgencyRegisteredAddressInternationalView]
      val countryOptions = application.injector.instanceOf[CountryOptionsNonUK].options()

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(InternationalAddress("line 1","line 2", Some("line 3"), "country")), countryOptions)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" when {

      "migrating from non-taxable to taxable" in {

        val answers = baseAnswers.set(WhatIsNextPage, NeedsToPayTax).value

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        mockPlaybackRepositoryBuilder(mockPlaybackRepository)

        val request = FakeRequest(POST, agencyRegisteredAddressInternationalRoute)
          .withFormUrlEncodedBody(("line1", "value 1"), ("line2", "value 2"), ("country", "DE"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.transition.declaration.routes.AgentDeclarationController.onPageLoad().url

        application.stop()
      }

      "not migrating from non-taxable to taxable" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(POST, agencyRegisteredAddressInternationalRoute)
          .withFormUrlEncodedBody(("line1", "value 1"), ("line2", "value 2"), ("country", "DE"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.AgentDeclarationController.onPageLoad().url

        application.stop()
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, agencyRegisteredAddressInternationalRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[AgencyRegisteredAddressInternationalView]
      val countryOptions = application.injector.instanceOf[CountryOptionsNonUK].options()

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, countryOptions)(request, messages).toString

      application.stop()
    }

    "return an Internal Server Error when setting the user answers goes wrong" in {

      mockPlaybackRepositoryBuilder(mockPlaybackRepository, setResult = Left(ServerError()))

      val answers = baseAnswers.set(WhatIsNextPage, NeedsToPayTax).value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(POST, agencyRegisteredAddressInternationalRoute)
        .withFormUrlEncodedBody(("line1", "value 1"), ("line2", "value 2"), ("country", "DE"))

      val result = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("text/html")

      application.stop()
    }
  }
}
