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

package controllers.close

import base.SpecBase
import forms.YesNoFormProvider
import models.UserAnswers
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.UTRPage
import pages.close.DateLastAssetSharedOutYesNoPage
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.PlaybackRepository
import views.html.close.{DateLastAssetSharedOutYesNoView, HowToCloseATrustView}

import scala.concurrent.Future

class HowToCloseATrustControllerSpec extends SpecBase with MockitoSugar {

  val utr: String = "1234567890"
  lazy val howToCloseATrustRoute: String = routes.HowToCloseATrustController.onPageLoad().url

  override val emptyUserAnswers: UserAnswers = super.emptyUserAnswers
    .set(UTRPage, utr).success.value

  "HowToCloseATrust Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, howToCloseATrustRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[HowToCloseATrustView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(utr)(fakeRequest, messages).toString

      application.stop()
    }
  }
}