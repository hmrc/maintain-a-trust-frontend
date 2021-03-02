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
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, route, status, _}
import services.FeatureFlagService
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolment, EnrolmentIdentifier, Enrolments}

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase {

  lazy val onPageLoad: String = routes.IndexController.onPageLoad().url
  lazy val startUtr: String = routes.IndexController.startUtr().url
  lazy val startUrn: String = routes.IndexController.startUrn().url

  val utr: String = "1234567892"
  val urn: String = "ABTRUST12345678"

  val mockFeatureFlagService: FeatureFlagService = mock[FeatureFlagService]

  "Index Controller" when {

    "onPageLoad in 4mld mode" must {
      "redirect to UTR controller when user is not enrolled (agent)" in {

        when(mockFeatureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(false))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).overrides(
          bind[FeatureFlagService].toInstance(mockFeatureFlagService)
        ).build()

        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustBe controllers.routes.UTRController.onPageLoad().url

        application.stop()
      }

      "redirect to status controller when user is a returning user who is enrolled" in {

        when(mockFeatureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(false))

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswersForUtr),
          affinityGroup = AffinityGroup.Organisation,
          enrolments = Enrolments(Set(
            Enrolment(
              key = "HMRC-TERS-ORG",
              identifiers = Seq(EnrolmentIdentifier(key = "SAUTR", value = utr)),
              state = "Activated"
            )
          ))
        ).overrides(
          bind[FeatureFlagService].toInstance(mockFeatureFlagService)
        ).build()

        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustBe controllers.routes.TrustStatusController.status().url

        application.stop()
      }
    }

    "onPageLoad in 5mld mode" must {
      "redirect to UTR controller when user is not enrolled (agent)" in {

        when(mockFeatureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).overrides(
          bind[FeatureFlagService].toInstance(mockFeatureFlagService)
        ).build()

        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustBe controllers.routes.UTRController.onPageLoad().url

        application.stop()
      }

      "redirect to status controller when user is a returning taxable user who is enrolled" in {

        when(mockFeatureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswersForUtr),
          affinityGroup = AffinityGroup.Organisation,
          enrolments = Enrolments(Set(
            Enrolment(
              key = "HMRC-TERS-ORG",
              identifiers = Seq(EnrolmentIdentifier(key = "SAUTR", value = utr)),
              state = "Activated"
            )
          ))
        ).overrides(
          bind[FeatureFlagService].toInstance(mockFeatureFlagService)
        ).build()

        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustBe controllers.routes.TrustStatusController.status().url

        application.stop()
      }

      "redirect to status controller when user is a returning non taxable user who is enrolled" in {

        when(mockFeatureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswersForUtr),
          affinityGroup = AffinityGroup.Organisation,
          enrolments = Enrolments(Set(
            Enrolment(
              key = "HMRC-TERSNT-ORG",
              identifiers = Seq(EnrolmentIdentifier(key = "URN", value = urn)),
              state = "Activated"
            )
          ))
        ).overrides(
          bind[FeatureFlagService].toInstance(mockFeatureFlagService)
        ).build()

        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustBe controllers.routes.TrustStatusController.status().url

        application.stop()
      }
    }

    "startUtr in 5mld mode" must {
      "redirect to UTR controller when user is not enrolled (agent)" in {

        when(mockFeatureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).overrides(
          bind[FeatureFlagService].toInstance(mockFeatureFlagService)
        ).build()

        val request = FakeRequest(GET, startUtr)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustBe controllers.routes.UTRController.onPageLoad().url

        application.stop()
      }

      "redirect to status controller when user is a returning taxable user who is enrolled" in {

        when(mockFeatureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswersForUtr),
          affinityGroup = AffinityGroup.Organisation,
          enrolments = Enrolments(Set(
            Enrolment(
              key = "HMRC-TERS-ORG",
              identifiers = Seq(EnrolmentIdentifier(key = "SAUTR", value = utr)),
              state = "Activated"
            )
          ))
        ).overrides(
          bind[FeatureFlagService].toInstance(mockFeatureFlagService)
        ).build()

        val request = FakeRequest(GET, startUtr)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustBe controllers.routes.TrustStatusController.status().url

        application.stop()
      }

      "redirect to status controller when user is a returning non taxable user who is enrolled" in {

        when(mockFeatureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswersForUtr),
          affinityGroup = AffinityGroup.Organisation,
          enrolments = Enrolments(Set(
            Enrolment(
              key = "HMRC-TERSNT-ORG",
              identifiers = Seq(EnrolmentIdentifier(key = "URN", value = urn)),
              state = "Activated"
            )
          ))
        ).overrides(
          bind[FeatureFlagService].toInstance(mockFeatureFlagService)
        ).build()

        val request = FakeRequest(GET, startUtr)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustBe controllers.routes.TrustStatusController.status().url

        application.stop()
      }
    }

    "startUrn in 5mld mode" must {
      "redirect to URN controller when user is not enrolled (agent)" in {

        when(mockFeatureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).overrides(
          bind[FeatureFlagService].toInstance(mockFeatureFlagService)
        ).build()

        val request = FakeRequest(GET, startUrn)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustBe controllers.routes.URNController.onPageLoad().url

        application.stop()
      }

      "redirect to status controller when user is a returning taxable user who is enrolled" in {

        when(mockFeatureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswersForUtr),
          affinityGroup = AffinityGroup.Organisation,
          enrolments = Enrolments(Set(
            Enrolment(
              key = "HMRC-TERS-ORG",
              identifiers = Seq(EnrolmentIdentifier(key = "SAUTR", value = utr)),
              state = "Activated"
            )
          ))
        ).overrides(
          bind[FeatureFlagService].toInstance(mockFeatureFlagService)
        ).build()

        val request = FakeRequest(GET, startUrn)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustBe controllers.routes.TrustStatusController.status().url

        application.stop()
      }

      "redirect to status controller when user is a returning non taxable user who is enrolled" in {

        when(mockFeatureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswersForUtr),
          affinityGroup = AffinityGroup.Organisation,
          enrolments = Enrolments(Set(
            Enrolment(
              key = "HMRC-TERSNT-ORG",
              identifiers = Seq(EnrolmentIdentifier(key = "URN", value = urn)),
              state = "Activated"
            )
          ))
        ).overrides(
          bind[FeatureFlagService].toInstance(mockFeatureFlagService)
        ).build()

        val request = FakeRequest(GET, startUrn)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustBe controllers.routes.TrustStatusController.status().url

        application.stop()
      }
    }
  }
}