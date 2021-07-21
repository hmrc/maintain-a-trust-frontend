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
import models.{URN, UTR}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.MaintainThisTrustView

class MaintainThisTrustControllerSpec extends SpecBase {

  "MaintainThisTrust Controller" must {

    "return OK and the correct view for a UTR GET when needs IV" in {

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswersForUtr)
      ).build()

      val request = FakeRequest(GET, routes.MaintainThisTrustController.onPageLoad(needsIv = true).url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[MaintainThisTrustView]

      val utr = "1234567890"

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(utr, UTR,
          "settlors, trustees, beneficiaries, protectors and other individuals",
          routes.MaintainThisTrustController.onSubmit()
        )(request, messages).toString

      application.stop()
    }

    "return OK and the correct view for a URN GET when needs IV" in {
      val urn = "NTTRUST00000001"

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswersForUtr.copy(identifier = urn))
      ).build()

      val request = FakeRequest(GET, routes.MaintainThisTrustController.onPageLoad(needsIv = true).url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[MaintainThisTrustView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(urn, URN,
          "settlors, trustees, beneficiaries, protectors and other individuals",
          routes.MaintainThisTrustController.onSubmit()
        )(request, messages).toString

      application.stop()
    }

    "return OK and the correct view for a UTR GET when doesn't need IV" in {

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswersForUtr)
      ).build()

      val request = FakeRequest(GET, routes.MaintainThisTrustController.onPageLoad(needsIv = false).url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[MaintainThisTrustView]

      val utr = "1234567890"

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(utr, UTR,
          "settlors, trustees, beneficiaries, protectors and other individuals",
          routes.InformationMaintainingThisTrustController.onPageLoad()
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
