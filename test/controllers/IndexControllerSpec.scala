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

package controllers

import java.time.LocalDate

import base.SpecBase
import connectors.TrustConnector
import models.{TrustDetails, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, verify, when}
import pages.{StartDatePage, UTRPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, route, status, _}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolment, EnrolmentIdentifier, Enrolments}

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase {

  lazy val onPageLoad: String = routes.IndexController.onPageLoad().url

  private val mockTrustConnector = mock[TrustConnector]

  private val date: String = "2019-06-01"

  when(mockTrustConnector.getTrustDetails(any())(any(), any()))
    .thenReturn(Future.successful(TrustDetails(startDate = date)))

  val utr: String = "1234567892"

  "Index Controller" must {

    "redirect to UTR controller when user is not enrolled" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(bind[TrustConnector].toInstance(mockTrustConnector)).build()

      val request = FakeRequest(GET, onPageLoad)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe controllers.routes.UTRController.onPageLoad().url

      application.stop()
    }

    "redirect to status controller when user is a returning user who is enrolled" in {

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        affinityGroup = AffinityGroup.Organisation,
        enrolments = Enrolments(Set(
          Enrolment(
            key = "HMRC-TERS-ORG",
            identifiers = Seq(EnrolmentIdentifier(key = "SAUTR", value = utr)),
            state = "Activated"
          )
        ))
      ).overrides(bind[TrustConnector].toInstance(mockTrustConnector)).build()

      val request = FakeRequest(GET, onPageLoad)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe controllers.routes.TrustStatusController.status().url

      application.stop()
    }

    "save trust start date and redirect to status controller when user has been redirected from UTR controller" in {

      reset(playbackRepository)

      val userAnswers = emptyUserAnswers.set(UTRPage, utr).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
        .build()

      when(playbackRepository.set(any())).thenReturn(Future.successful(true))
      when(playbackRepository.resetCache(any())).thenReturn(Future.successful(Some(Json.obj())))

      val request = FakeRequest(GET, routes.IndexController.saveStartDate().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe controllers.routes.TrustStatusController.status().url

      val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(playbackRepository).set(uaCaptor.capture)
      uaCaptor.getValue.get(StartDatePage).get mustEqual LocalDate.parse(date)
      uaCaptor.getValue.get(UTRPage).get mustEqual utr

      application.stop()
    }

  }
}