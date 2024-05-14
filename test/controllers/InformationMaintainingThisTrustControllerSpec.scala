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

package controllers

import base.SpecBase
import models.{URN, UTR}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.{InformationMaintainingNonTaxableTrustView, InformationMaintainingTaxableTrustView, InformationMaintainingThisTrustView}

class InformationMaintainingThisTrustControllerSpec extends SpecBase {

  "InformationMaintainingThisTrustPage Controller" must {

    "return OK and the correct view for a GET" when {

        "underlying trust data is 4mld" in {

          val userAnswers = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = false)

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          val request = FakeRequest(GET, routes.InformationMaintainingThisTrustController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[InformationMaintainingThisTrustView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(userAnswers.identifier, UTR)(request, messages).toString

          application.stop()
        }

        "underlying trust data is 5mld" when {

          "taxable" in {

            val userAnswers = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

            val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

            val request = FakeRequest(GET, routes.InformationMaintainingThisTrustController.onPageLoad().url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[InformationMaintainingTaxableTrustView]

            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(userAnswers.identifier, UTR)(request, messages).toString

            application.stop()
          }

          "non-taxable" in {

            val userAnswers = emptyUserAnswersForUrn

            val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

            val request = FakeRequest(GET, routes.InformationMaintainingThisTrustController.onPageLoad().url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[InformationMaintainingNonTaxableTrustView]

            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(userAnswers.identifier, URN)(request, messages).toString

            application.stop()
          }
        }
    }

    ".onSubmit" must {
      "redirect to ViewLastDeclarationYesNoController " in {

        val userAnswers = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(POST, routes.InformationMaintainingThisTrustController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.ViewLastDeclarationYesNoController.onPageLoad().url

        application.stop()

      }
    }
  }
}
