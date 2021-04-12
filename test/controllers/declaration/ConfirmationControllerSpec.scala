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

package controllers.declaration

import base.SpecBase
import models.pages.WhatIsNext.MakeChanges
import pages.{TVNPage, WhatIsNextPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.TestUserAnswers
import views.html.declaration.ConfirmationView

class ConfirmationControllerSpec extends SpecBase {

  "Confirmation Controller" must {

    "return OK and the correct view for a onPageLoad when TVN is available" in {

      val fakeTvn = "XCTVN0000004912"

      val playbackAnswers = TestUserAnswers.emptyUserAnswersForUtr
        .set(WhatIsNextPage, MakeChanges).success.value
        .set(TVNPage, fakeTvn).success.value

      val application = applicationBuilder(userAnswers = Some(playbackAnswers)).build()

      val request = FakeRequest(GET, routes.ConfirmationController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ConfirmationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(fakeTvn, isAgent = false, isTrustTaxable = true, "#")(request, messages).toString

      application.stop()
    }
  }

}
