/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.transition.declaration

import base.SpecBase
import models.pages.WhatIsNext.NeedsToPayTax
import pages.{TVNPage, WhatIsNextPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import utils.TestUserAnswers
import views.html.transition.declaration.{AgentConfirmationView, IndividualConfirmationView}

class ConfirmationControllerSpec extends SpecBase {

  private val fakeTvn = "XCTVN0000004912"

  "Confirmation Controller" must {

    "agent" when {

      "return OK and the correct view for a onPageLoad when TVN is available" in {

        val playbackAnswers = TestUserAnswers.emptyUserAnswersForUtr
          .set(WhatIsNextPage, NeedsToPayTax).value
          .set(TVNPage, fakeTvn).value

        val application = applicationBuilder(userAnswers = Some(playbackAnswers), affinityGroup = Agent).build()

        val request = FakeRequest(GET, routes.ConfirmationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentConfirmationView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(fakeTvn, "the lead trustee")(request, messages).toString

        application.stop()
      }
    }

    "individual" when {

      "return OK and the correct view for a onPageLoad when TVN is available" in {

        val playbackAnswers = TestUserAnswers.emptyUserAnswersForUtr
          .set(WhatIsNextPage, NeedsToPayTax).value
          .set(TVNPage, fakeTvn).value

        val application = applicationBuilder(userAnswers = Some(playbackAnswers)).build()

        val request = FakeRequest(GET, routes.ConfirmationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IndividualConfirmationView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(fakeTvn, "the lead trustee")(request, messages).toString

        application.stop()
      }
    }
  }

}
