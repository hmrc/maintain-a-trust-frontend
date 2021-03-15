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

package controllers.makechanges

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.makechanges.UnavailableSectionsView

class UnavailableSectionsControllerSpec extends SpecBase {

  "UnavailableSections Controller" must {

    "return OK and the correct view for a GET when maintain trustees is enabled" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
        .configure(
          "microservice.services.features.maintain-trustees.enabled" -> true,
          "microservice.services.features.maintain-beneficiaries.enabled" -> false,
          "microservice.services.features.maintain-settlors.enabled" -> false,
          "microservice.services.features.maintain-protectors.enabled" -> false,
          "microservice.services.features.maintain-other-individuals.enabled" -> false
        )
        .build()

      val request = FakeRequest(GET, routes.UnavailableSectionsController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[UnavailableSectionsView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(
          "trustees",
          "settlors, beneficiaries, protectors or other individuals",
          "settlors, beneficiaries, protectors and other individuals"
        )(request, messages).toString

      application.stop()
    }

    "return OK and the correct view for a GET when maintain trustees and maintain beneficiaries are enabled" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
        .configure(
          "microservice.services.features.maintain-trustees.enabled" -> true,
          "microservice.services.features.maintain-beneficiaries.enabled" -> true,
          "microservice.services.features.maintain-settlors.enabled" -> false,
          "microservice.services.features.maintain-protectors.enabled" -> false,
          "microservice.services.features.maintain-other-individuals.enabled" -> false
        )
        .build()

      val request = FakeRequest(GET, routes.UnavailableSectionsController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[UnavailableSectionsView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(
          "trustees and beneficiaries",
          "settlors, protectors or other individuals",
          "settlors, protectors and other individuals"
        )(request, messages).toString

      application.stop()
    }

    "return OK and the correct view for a GET when maintain trustees, maintain beneficiaries and maintain settlors are enabled" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
        .configure(
          "microservice.services.features.maintain-trustees.enabled" -> true,
          "microservice.services.features.maintain-beneficiaries.enabled" -> true,
          "microservice.services.features.maintain-settlors.enabled" -> true,
          "microservice.services.features.maintain-protectors.enabled" -> false,
          "microservice.services.features.maintain-other-individuals.enabled" -> false
        )
        .build()

      val request = FakeRequest(GET, routes.UnavailableSectionsController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[UnavailableSectionsView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(
          "settlors, trustees and beneficiaries",
          "protectors or other individuals",
          "protectors and other individuals"
        )(request, messages).toString

      application.stop()
    }
  }
}
