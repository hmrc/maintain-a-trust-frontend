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

package controllers.close

import base.SpecBase
import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.close.BeforeClosingView

class BeforeClosingControllerSpec extends SpecBase with ScalaCheckPropertyChecks {

  private lazy val beforeClosingRoute: String = routes.BeforeClosingController.onPageLoad().url

  "BeforeClosingController" when {

    ".onPageLoad" must {
      "return OK and the correct view" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

        val request = FakeRequest(GET, beforeClosingRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BeforeClosingView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view()(request, messages).toString

        application.stop()
      }
    }

    ".onSubmit" when {

      "a 5mld trust in 5mld mode" must {
        "redirect to update trust details yes/no" in {

          forAll(arbitrary[Boolean]) { boolean =>

            val baseAnswers: UserAnswers = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true, isUnderlyingDataTaxable = boolean)

            val application = applicationBuilder(userAnswers = Some(baseAnswers))
              .build()

            val request = FakeRequest(POST, beforeClosingRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual controllers.makechanges.routes.UpdateTrustDetailsYesNoController.onPageLoad().url

            application.stop()
          }
        }
      }

      "not a 5mld trust in 5mld mode" must {
        "redirect to update trustees yes/no" in {

          val baseAnswers: UserAnswers = emptyUserAnswersForUtr

          val application = applicationBuilder(userAnswers = Some(baseAnswers))
            .build()

          val request = FakeRequest(POST, beforeClosingRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.makechanges.routes.UpdateTrusteesYesNoController.onPageLoad().url

          application.stop()
        }
      }
    }
  }
}
