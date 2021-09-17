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
import models.UTR
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.MigrateTo5mldInformationView

class MigrateTo5mldInformationControllerSpec extends SpecBase {

  "MigrateTo5mldInformationController" when {

    ".onPageLoad" must {
      "return OK and the correct view" in {

        val utr = "1234567890"

        val userAnswers = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, routes.MigrateTo5mldInformationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MigrateTo5mldInformationView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(utr, UTR)(request, messages).toString

        application.stop()

      }
    }

    ".onSubmit" must {
      "redirect to ExpressTrustYesNoController " in {


        val userAnswers = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(POST, routes.MigrateTo5mldInformationController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.transition.routes.ExpressTrustYesNoController.onPageLoad().url

        application.stop()

      }
    }


  }
}
