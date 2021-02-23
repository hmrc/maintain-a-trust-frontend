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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{get, okJson, urlEqualTo, _}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import models.{CompletedMaintenanceTasks, FeatureResponse}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import pages.makechanges._
import play.api.http.HeaderNames._
import play.api.http.MimeTypes._
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class TrustsStoreConnectorSpec extends SpecBase with BeforeAndAfterAll with BeforeAndAfterEach with ScalaFutures with IntegrationPatience {

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  protected val server: WireMockServer = new WireMockServer(wireMockConfig().dynamicPort())

  override def beforeAll(): Unit = {
    server.start()
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    server.resetAll()
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    server.stop()
  }

  "trusts store connector" must {

    "return OK with the current task status" in {
      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts-store.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustsStoreConnector]

      val json = Json.parse(
        """
          |{
          |  "trustees": true,
          |  "beneficiaries": false,
          |  "settlors": false,
          |  "protectors": false,
          |  "other": false
          |}
          |""".stripMargin)

      server.stubFor(
        get(urlEqualTo("/trusts-store/maintain/tasks/123456789"))
          .willReturn(okJson(json.toString))
      )

      val result = connector.getStatusOfTasks("123456789")

      result.futureValue mustBe
        CompletedMaintenanceTasks(trustees = true, beneficiaries = false, settlors = false, protectors = false, other = false)

      application.stop()
    }

    "return OK response with current tasks when setting tasks" in {
      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts-store.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustsStoreConnector]

      val userAnswers = emptyUserAnswers
        .set(UpdateTrusteesYesNoPage, true).success.value
        .set(UpdateBeneficiariesYesNoPage, false).success.value
        .set(UpdateSettlorsYesNoPage, false).success.value
        .set(AddOrUpdateProtectorYesNoPage, false).success.value
        .set(AddOrUpdateOtherIndividualsYesNoPage, false).success.value

      val json = Json.parse(
        """
          |{
          |  "trustees": false,
          |  "beneficiaries": true,
          |  "settlors": true,
          |  "protectors": true,
          |  "other": true
          |}
          |""".stripMargin)

      server.stubFor(
        post(urlEqualTo("/trusts-store/maintain/tasks/123456789"))
          .withHeader(CONTENT_TYPE, containing(JSON))
          .withRequestBody(equalTo(json.toString))
          .willReturn(okJson(json.toString))
      )

      val result = connector.set("123456789", userAnswers)

      result.futureValue mustBe
        CompletedMaintenanceTasks(trustees = false, beneficiaries = true, settlors = true, protectors = true, other = true)

      application.stop()
    }

    "return default tasks when a failure occurs" in {
      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts-store.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustsStoreConnector]

      server.stubFor(
        get(urlEqualTo("/trusts-store/maintain/tasks/123456789"))
          .willReturn(serverError())
      )

      val result = connector.getStatusOfTasks("123456789")

      result.futureValue mustBe
        CompletedMaintenanceTasks(trustees = false, beneficiaries = false, settlors = false, protectors = false, other = false)

      application.stop()
    }

    "return OK response when setting feature" in {
      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts-store.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustsStoreConnector]

      server.stubFor(
        put(urlEqualTo("/trusts-store/features/TestFeature"))
          .withHeader(CONTENT_TYPE, containing(JSON))
          .withRequestBody(equalTo("true"))
          .willReturn(aResponse()
            .withStatus(Status.OK))
      )

      val result = Await.result(connector.setFeature("TestFeature", state = true), Duration.Inf)

      result.status mustBe Status.OK

      application.stop()
    }

    "return a feature flag of true if 5mld is enabled" in {
      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts-store.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustsStoreConnector]

      server.stubFor(
        get(urlEqualTo(s"/trusts-store/features/5mld"))
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(
                Json.stringify(
                  Json.toJson(FeatureResponse("5mld", isEnabled = true))
                )
              )
          )
      )

      val result = Await.result(connector.getFeature("5mld"), Duration.Inf)
      result mustBe FeatureResponse("5mld", isEnabled = true)
    }

    "return a feature flag of false if 5mld is not enabled" in {
      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts-store.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustsStoreConnector]

      server.stubFor(
        get(urlEqualTo(s"/trusts-store/features/5mld"))
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(
                Json.stringify(
                  Json.toJson(FeatureResponse("5mld", isEnabled = false))
                )
              )
          )
      )

      val result = Await.result(connector.getFeature("5mld"), Duration.Inf)
      result mustBe FeatureResponse("5mld", isEnabled = false)
    }
  }

}
