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
import models.UTR
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup
import views.html.InformationSchedule3aExemptionView

class InformationSchedule3aExemptionControllerSpec extends SpecBase {

  "InformationMaintainingThisTrustPage Controller" must {

    "return OK and the correct view for a GET" when {

      "user is an organisation" in {

        val userAnswers = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, routes.InformationSchedule3aExemptionController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[InformationSchedule3aExemptionView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(userAnswers.identifier, UTR, isAgent = false)(request, messages).toString

        application.stop()
      }

      "user is an agent" in {

        val userAnswers = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

        val application = applicationBuilder(userAnswers = Some(userAnswers), affinityGroup = AffinityGroup.Agent).build()

        val request = FakeRequest(GET, routes.InformationSchedule3aExemptionController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[InformationSchedule3aExemptionView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(userAnswers.identifier, UTR, isAgent = true)(request, messages).toString

        application.stop()

      }
    }

    ".onSubmit" must {
      "redirect to Schedule3aExemptYesNoController " in {

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
