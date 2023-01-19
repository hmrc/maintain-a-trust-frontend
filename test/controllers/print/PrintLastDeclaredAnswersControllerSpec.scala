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

package controllers.print

import base.SpecBase
import models.pages.IndividualOrBusiness.Individual
import models.{FullName, UKAddress}
import pages.beneficiaries.charity._
import pages.trustees._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.TestUserAnswers
import utils.print.PrintPlaybackHelper
import views.html.print.PrintLastDeclaredAnswersView

class PrintLastDeclaredAnswersControllerSpec extends SpecBase {

  val index = 0

  "PrintLastDeclaredController" must {

    "return OK and the correct view for a GET" in {

      val playbackAnswers = TestUserAnswers.emptyUserAnswersForUtr
        .set(CharityBeneficiaryNamePage(0), "Charity Beneficiary 1").success.value
        .set(CharityBeneficiaryDiscretionYesNoPage(0), true).success.value
        .set(CharityBeneficiaryShareOfIncomePage(0), "10").success.value
        .set(CharityBeneficiaryAddressYesNoPage(0), true).success.value
        .set(CharityBeneficiaryAddressUKYesNoPage(0), true).success.value
        .set(CharityBeneficiaryAddressPage(0), UKAddress("line1", "line2", None, None, "NE11NE")).success.value
        .set(CharityBeneficiaryNamePage(1), "Charity Beneficiary 2").success.value
        .set(CharityBeneficiaryDiscretionYesNoPage(1), false).success.value
        .set(CharityBeneficiaryAddressYesNoPage(1), false).success.value

      val entities = injector.instanceOf[PrintPlaybackHelper].entities(playbackAnswers)

      val trustDetails = injector.instanceOf[PrintPlaybackHelper].trustDetails(playbackAnswers)

      val application = applicationBuilder(Some(playbackAnswers)).build()

      val request = FakeRequest(GET, controllers.print.routes.PrintLastDeclaredAnswersController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[PrintLastDeclaredAnswersView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(entities, trustDetails)(request, messages).toString

      application.stop()
    }

    "return OK and the correct view for a GET with Trustee Mental Capacity questions answered when no mental capacity data held" in {

      val playbackAnswers = TestUserAnswers.emptyUserAnswersForUtr
        .set(IsThisLeadTrusteePage(0), false).success.value
        .set(TrusteeIndividualOrBusinessPage(0), Individual).success.value
        .set(TrusteeNamePage(0), FullName("Trustee", None, "1")).success.value
        .set(TrusteeDateOfBirthYesNoPage(0), false).success.value
        .set(TrusteeCountryOfNationalityYesNoPage(0), false).success.value
        .set(TrusteeNinoYesNoPage(0), false).success.value
        .set(TrusteeCountryOfResidenceYesNoPage(0), false).success.value
        .set(TrusteeAddressYesNoPage(0), false).success.value
        .set(TrusteePassportIDCardYesNoPage(0), false).success.value

      val entities = injector.instanceOf[PrintPlaybackHelper].entities(playbackAnswers)

      val trustDetails = injector.instanceOf[PrintPlaybackHelper].trustDetails(playbackAnswers)

      val application = applicationBuilder(Some(playbackAnswers)).build()

      val request = FakeRequest(GET, controllers.print.routes.PrintLastDeclaredAnswersController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[PrintLastDeclaredAnswersView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(entities, trustDetails)(request, messages).toString

      contentAsString(result) must include("Does Trustee 1 have mental capacity at the time of registration?")
      contentAsString(result) must include("I don’t know or not provided")

      application.stop()
    }

  }

  ".onSubmit" must {
    "redirect to WhatIsNextController " in {

      val userAnswers = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(POST, routes.PrintLastDeclaredAnswersController.onSubmit().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.WhatIsNextController.onPageLoad().url

      application.stop()

    }
  }

}
