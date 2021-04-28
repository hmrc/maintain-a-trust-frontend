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

package controllers.transition

import base.SpecBase
import models.UTR
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import utils.TestUserAnswers.utr
import views.html.transition.BeforeYouContinueToTaxableView


class BeforeYouContinueToTaxableControllerSpec extends SpecBase with MockitoSugar {

  lazy val beforeYouContineToTaxableRoute: String = routes.BeforeYouContinueToTaxableController.onPageLoad().url

  "BeforeYouContinueToTaxableController" when {

    ".onPageLoad" must {
      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

        val request = FakeRequest(GET, beforeYouContineToTaxableRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BeforeYouContinueToTaxableView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(utr, UTR)(request, messages).toString

        application.stop()
      }
    }

    ".onSubmit" must {
      "redirect to the next page" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr), affinityGroup = Organisation)
          .build()

        val request = FakeRequest(POST, beforeYouContineToTaxableRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.ExpressTrustYesNoController.onPageLoad().url

        application.stop()
      }
    }
  }
}