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
import forms.UKAddressFormProvider
import models.pages.WhatIsNext.MakeChanges
import models.{UKAddress, UserAnswers}
import pages.WhatIsNextPage
import pages.declaration.AgencyRegisteredAddressUkPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.declaration.AgencyRegisteredAddressUkView

class AgencyRegisteredAddressUkControllerSpec extends SpecBase {

  val formProvider = new UKAddressFormProvider()
  val form = formProvider()
  lazy val agencyRegisteredAddressUkRoute = routes.AgencyRegisteredAddressUkController.onPageLoad().url

  val baseAnswers: UserAnswers = emptyUserAnswers
    .set(WhatIsNextPage, MakeChanges).success.value

  "AgencyRegisteredAddressUk Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request = FakeRequest(GET, agencyRegisteredAddressUkRoute)

      val view = application.injector.instanceOf[AgencyRegisteredAddressUkView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers
        .set(AgencyRegisteredAddressUkPage, UKAddress("line 1", "line 2", Some("line 3"), Some("line 4"),"line 5")).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, agencyRegisteredAddressUkRoute)

      val view = application.injector.instanceOf[AgencyRegisteredAddressUkView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(UKAddress("line 1","line 2", Some("line 3"), Some("line 4"),"line 5")))(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, agencyRegisteredAddressUkRoute)
          .withFormUrlEncodedBody(("line1", "value 1"), ("line2", "value 2"),("postcode", "NE1 1ZZ"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.AgentDeclarationController.onPageLoad().url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, agencyRegisteredAddressUkRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[AgencyRegisteredAddressUkView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm)(request, messages).toString

      application.stop()
    }
  }
}
