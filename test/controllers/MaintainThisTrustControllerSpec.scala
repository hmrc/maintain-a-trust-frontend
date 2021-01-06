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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.MaintainThisTrustView

class MaintainThisTrustControllerSpec extends SpecBase {

  "MaintainThisTrust Controller" must {

    "return OK and the correct view for a GET when needs IV" in {

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers)
      ).build()

      val request = FakeRequest(GET, routes.MaintainThisTrustController.onPageLoad(needsIv = true).url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[MaintainThisTrustView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view("utr",
          "settlors, trustees, beneficiaries, protectors and other individuals",
          frontendAppConfig.verifyIdentityForATrustUrl("utr")
        )(request, messages).toString

      application.stop()
    }

    "return OK and the correct view for a GET when doesn't need IV" in {

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers)
      ).build()

      val request = FakeRequest(GET, routes.MaintainThisTrustController.onPageLoad(needsIv = false).url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[MaintainThisTrustView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view("utr",
          "settlors, trustees, beneficiaries, protectors and other individuals",
          routes.InformationMaintainingThisTrustController.onPageLoad().url
        )(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired if UTR cannot be found in enrolments" in {

      val application = applicationBuilder().build()

      val request = FakeRequest(GET, routes.MaintainThisTrustController.onPageLoad(needsIv = true).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
