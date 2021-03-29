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
import pages.trustdetails.ExpressTrustYesNoPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.{InformationMaintainingTaxableTrustView, InformationMaintainingThisTrustView}

class InformationMaintainingThisTrustControllerSpec extends SpecBase {

  private val utr = "1234567890"

  "InformationMaintainingThisTrustPage Controller" must {

    "return OK and the correct view for a GET" when {

      "4mld" in {

        val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = false, isTrustTaxable = true)

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, routes.InformationMaintainingThisTrustController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[InformationMaintainingThisTrustView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(utr, UTR)(request, messages).toString

        application.stop()
      }

      "5mld" when {

        "underlying trust data is 4mld" in {

          val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true, isTrustTaxable = true)

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          val request = FakeRequest(GET, routes.InformationMaintainingThisTrustController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[InformationMaintainingThisTrustView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(utr, UTR)(request, messages).toString

          application.stop()
        }

        "taxable" in {

          val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true, isTrustTaxable = true)
            .set(ExpressTrustYesNoPage, true).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          val request = FakeRequest(GET, routes.InformationMaintainingThisTrustController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[InformationMaintainingTaxableTrustView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(utr, UTR)(request, messages).toString

          application.stop()
        }

        "non-taxable" in {

        }
      }
    }
  }
}
