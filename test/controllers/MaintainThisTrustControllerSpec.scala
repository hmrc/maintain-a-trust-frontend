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

package controllers

import base.SpecBase
import pages.UTRPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.{AgentNotAuthorisedView, MaintainThisTrustView}

class MaintainThisTrustControllerSpec extends SpecBase {

  "MaintainThisTrust Controller" must {

    "return OK and the correct view for a GET" in {

      val utr = "0987654321"

      val answers = emptyUserAnswers
        .set(UTRPage, utr)
        .success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, routes.MaintainThisTrustController.onPageLoad(utr).url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[MaintainThisTrustView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(utr, "settlors, trustees and beneficiaries")(fakeRequest, messages).toString

      application.stop()
    }
  }
}
