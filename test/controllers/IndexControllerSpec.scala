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
import connectors.TrustConnector
import controllers.Assets.Redirect
import models.TrustDetails
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, route, status, _}
import services.{FeatureFlagService, UserAnswersSetupService}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolment, EnrolmentIdentifier, Enrolments}

import java.time.LocalDate
import scala.concurrent.Future

class IndexControllerSpec extends SpecBase with BeforeAndAfterAll with BeforeAndAfterEach {

  lazy val onPageLoad: String = routes.IndexController.onPageLoad().url
  lazy val startUtr: String = routes.IndexController.startUtr().url
  lazy val startUrn: String = routes.IndexController.startUrn().url

  val utr: String = "1234567892"
  val urn: String = "ABTRUST12345678"

  val startDate: LocalDate = LocalDate.parse("2000-01-01")

  val taxableEnrolment: Enrolments = Enrolments(Set(Enrolment(
    key = "HMRC-TERS-ORG",
    identifiers = Seq(EnrolmentIdentifier(key = "SAUTR", value = utr)),
    state = "Activated"
  )))

  val nonTaxableEnrolment: Enrolments = Enrolments(Set(
    Enrolment(
      key = "HMRC-TERSNT-ORG",
      identifiers = Seq(EnrolmentIdentifier(key = "URN", value = urn)),
      state = "Activated"
    )
  ))

  val redirectUrl = "redirectUrl"

  val mockFeatureFlagService: FeatureFlagService = mock[FeatureFlagService]
  val mockTrustsConnector: TrustConnector = mock[TrustConnector]
  val mockUserAnswersSetupService: UserAnswersSetupService = mock[UserAnswersSetupService]

  override def beforeEach(): Unit = {
    reset(mockFeatureFlagService)
    reset(mockUserAnswersSetupService)

    when(mockUserAnswersSetupService.setupAndRedirectToStatus(any(), any(), any(), any(), any())(any(), any()))
      .thenReturn(Future.successful(Redirect(redirectUrl)))
  }

  "Index Controller" when {

    "onPageLoad in 4mld mode" must {
      behave like indexController(
        is5mldEnabled = false,
        isExpress = None,
        isTaxable = None,
        onPageLoadRoute = onPageLoad,
        redirectRoute = controllers.routes.UTRController.onPageLoad().url
      )
    }

    "onPageLoad in 5mld mode" must {
      behave like indexController(
        is5mldEnabled = true,
        isExpress = Some(true),
        isTaxable = Some(true),
        onPageLoadRoute = onPageLoad,
        redirectRoute = controllers.routes.UTRController.onPageLoad().url
      )
    }

    "startUtr in 5mld mode" must {
      behave like indexController(
        is5mldEnabled = true,
        isExpress = Some(true),
        isTaxable = Some(true),
        onPageLoadRoute = startUtr,
        redirectRoute = controllers.routes.UTRController.onPageLoad().url
      )
    }

    "startUrn in 5mld mode" must {
      behave like indexController(
        is5mldEnabled = true,
        isExpress = Some(true),
        isTaxable = Some(false),
        onPageLoadRoute = startUrn,
        redirectRoute = controllers.routes.URNController.onPageLoad().url
      )
    }
  }

  def indexController(is5mldEnabled: Boolean,
                      isExpress: Option[Boolean],
                      isTaxable: Option[Boolean],
                      onPageLoadRoute: String,
                      redirectRoute: String): Unit = {

    val trustDetails = TrustDetails(startDate, isExpress, isTaxable)

    s"redirect to $redirectRoute when user is not enrolled (agent)" in {

      when(mockFeatureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(is5mldEnabled))

      when(mockTrustsConnector.getUntransformedTrustDetails(any())(any(), any()))
        .thenReturn(Future.successful(trustDetails))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).overrides(
        bind[FeatureFlagService].toInstance(mockFeatureFlagService),
        bind[TrustConnector].toInstance(mockTrustsConnector)
      ).build()

      val request = FakeRequest(GET, onPageLoadRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe redirectRoute

      application.stop()
    }

    "redirect to status controller when user is a returning taxable user who is enrolled" in {

      when(mockFeatureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(is5mldEnabled))

      when(mockTrustsConnector.getUntransformedTrustDetails(any())(any(), any()))
        .thenReturn(Future.successful(trustDetails))

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswersForUtr),
        affinityGroup = AffinityGroup.Organisation,
        enrolments = taxableEnrolment
      ).overrides(
        bind[FeatureFlagService].toInstance(mockFeatureFlagService),
        bind[UserAnswersSetupService].toInstance(mockUserAnswersSetupService),
        bind[TrustConnector].toInstance(mockTrustsConnector)
      ).build()

      val request = FakeRequest(GET, onPageLoadRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe redirectUrl

      verify(mockUserAnswersSetupService).setupAndRedirectToStatus(
        eqTo(utr),
        eqTo("id"),
        eqTo(is5mldEnabled),
        eqTo(trustDetails.is5mld),
        eqTo(trustDetails.isTaxable)
      )(any(), any())

      application.stop()
    }

    "redirect to status controller when user is a returning non-taxable user who is enrolled" in {

      when(mockFeatureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(is5mldEnabled))

      when(mockTrustsConnector.getUntransformedTrustDetails(any())(any(), any()))
        .thenReturn(Future.successful(trustDetails))

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswersForUtr),
        affinityGroup = AffinityGroup.Organisation,
        enrolments = nonTaxableEnrolment
      ).overrides(
        bind[FeatureFlagService].toInstance(mockFeatureFlagService),
        bind[UserAnswersSetupService].toInstance(mockUserAnswersSetupService),
        bind[TrustConnector].toInstance(mockTrustsConnector)
      ).build()

      val request = FakeRequest(GET, onPageLoadRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe redirectUrl

      verify(mockUserAnswersSetupService).setupAndRedirectToStatus(
        eqTo(urn),
        eqTo("id"),
        eqTo(is5mldEnabled),
        eqTo(trustDetails.is5mld),
        eqTo(trustDetails.isTaxable)
      )(any(), any())

      application.stop()
    }

  }
}
