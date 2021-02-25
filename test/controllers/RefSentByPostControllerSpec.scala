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
import uk.gov.hmrc.auth.core.AffinityGroup
import views.html.{InformationMaintainingThisTrustView, RefSentByPostAgentView, RefSentByPostView}

class RefSentByPostControllerSpec extends SpecBase {

  "RefSentByPost Controller" must {

    "return OK and the correct view for a GET" when {

      "an agent user" in {
        val userAnswers = emptyUserAnswersForUtr

        val application = applicationBuilder(
          affinityGroup = AffinityGroup.Agent,
          userAnswers = Some(userAnswers)
        ).build()

        val request = FakeRequest(GET, routes.RefSentByPostController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RefSentByPostAgentView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view()(request, messages).toString

        application.stop()
      }

      "an individual user" in {
        val userAnswers = emptyUserAnswersForUtr

        val application = applicationBuilder(
          affinityGroup = AffinityGroup.Organisation,
          userAnswers = Some(userAnswers)
        ).build()

        val request = FakeRequest(GET, routes.RefSentByPostController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RefSentByPostView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view()(request, messages).toString

        application.stop()
      }
    }
  }
}
