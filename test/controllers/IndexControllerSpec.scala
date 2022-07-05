/*
 * Copyright 2022 HM Revenue & Customs
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
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, route, status, _}
import repositories.ActiveSessionRepository
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolment, EnrolmentIdentifier, Enrolments}

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase with BeforeAndAfterAll with BeforeAndAfterEach {

  private lazy val onPageLoad: String = routes.IndexController.onPageLoad.url
  private lazy val startUtr: String = routes.IndexController.startUtr.url
  private lazy val startUrn: String = routes.IndexController.startUrn.url

  private val utr: String = "1234567892"
  private val urn: String = "ABTRUST12345678"

  private val taxableEnrolment: Enrolments = Enrolments(Set(Enrolment(
    key = "HMRC-TERS-ORG",
    identifiers = Seq(EnrolmentIdentifier(key = "SAUTR", value = utr)),
    state = "Activated"
  )))

  private val nonTaxableEnrolment: Enrolments = Enrolments(Set(
    Enrolment(
      key = "HMRC-TERSNT-ORG",
      identifiers = Seq(EnrolmentIdentifier(key = "URN", value = urn)),
      state = "Activated"
    )
  ))

  private val mockSessionRepository = mock[ActiveSessionRepository]

  override def beforeEach(): Unit = {
    reset(mockSessionRepository)
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
  }

  "Index Controller" when {

    "onPageLoad in 5mld mode" must {
      behave like indexController(
        onPageLoadRoute = onPageLoad,
        redirectRoute = controllers.routes.UTRController.onPageLoad().url
      )
    }

    "startUtr in 5mld mode" must {
      behave like indexController(
        onPageLoadRoute = startUtr,
        redirectRoute = controllers.routes.UTRController.onPageLoad().url
      )
    }

    "startUrn in 5mld mode" must {
      behave like indexController(
        onPageLoadRoute = startUrn,
        redirectRoute = controllers.routes.URNController.onPageLoad().url
      )
    }
  }

  def indexController(onPageLoadRoute: String,
                      redirectRoute: String): Unit = {

    s"redirect to $redirectRoute when user is not enrolled (agent)" in {
      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswersForUtr),
        affinityGroup = AffinityGroup.Agent
      ).overrides(
        bind[ActiveSessionRepository].toInstance(mockSessionRepository),
      ).build()

      val request = FakeRequest(GET, onPageLoadRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe redirectRoute

      application.stop()
    }

    "redirect to status controller when user is a returning taxable user who is enrolled" in {

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswersForUtr),
        affinityGroup = AffinityGroup.Organisation,
        enrolments = taxableEnrolment
      ).overrides(
        bind[ActiveSessionRepository].toInstance(mockSessionRepository)
      ).build()

      val request = FakeRequest(GET, onPageLoadRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe "/maintain-a-trust/status/start"

      application.stop()
    }

    "redirect to status controller when user is a returning non-taxable user who is enrolled" in {
      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswersForUtr),
        affinityGroup = AffinityGroup.Organisation,
        enrolments = nonTaxableEnrolment
      ).overrides(
        bind[ActiveSessionRepository].toInstance(mockSessionRepository)
      ).build()

      val request = FakeRequest(GET, onPageLoadRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe "/maintain-a-trust/status/start"

      application.stop()
    }

  }
}
