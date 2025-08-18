/*
 * Copyright 2025 HM Revenue & Customs
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
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import models.errors.ServerError
import models.pages.Tag._
import models.{CompletedMaintenanceTasks, FeatureResponse}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import pages.makechanges._
import play.api.http.HeaderNames._
import play.api.http.MimeTypes._
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class TrustsStoreConnectorSpec extends SpecBase with BeforeAndAfterAll with BeforeAndAfterEach with ScalaFutures with IntegrationPatience {

  private implicit lazy val hc: HeaderCarrier = HeaderCarrier()

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

  private val identifier: String = "1234567890"

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
          |  "trustDetails": "not-started",
          |  "assets": "not-started",
          |  "taxLiability": "not-started",
          |  "trustees": "completed",
          |  "beneficiaries": "not-started",
          |  "settlors": "not-started",
          |  "protectors": "not-started",
          |  "other": "not-started"
          |}
          |""".stripMargin)

      server.stubFor(
        get(urlEqualTo(s"/trusts-store/maintain/tasks/$identifier"))
          .willReturn(okJson(json.toString))
      )

      val result = connector.getStatusOfTasks(identifier).value

      result.futureValue mustBe Right(
        CompletedMaintenanceTasks(
          trustDetails = NotStarted,
          assets = NotStarted,
          taxLiability = NotStarted,
          trustees = Completed,
          beneficiaries = NotStarted,
          settlors = NotStarted,
          protectors = NotStarted,
          other = NotStarted
        )
      )

      application.stop()
    }

    "return OK response with current tasks when setting tasks" when {

      "trust details and non-EEA company questions unanswered (4mld, 5mld non-taxable)" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts-store.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsStoreConnector]

        val userAnswers = emptyUserAnswersForUtr
          .set(UpdateTrusteesYesNoPage, true).value
          .set(UpdateBeneficiariesYesNoPage, false).value
          .set(UpdateSettlorsYesNoPage, false).value
          .set(AddOrUpdateProtectorYesNoPage, false).value
          .set(AddOrUpdateOtherIndividualsYesNoPage, false).value

        val json = Json.parse(
          """
            |{
            |  "trustDetails": "completed",
            |  "assets": "completed",
            |  "taxLiability": "completed",
            |  "trustees": "not-started",
            |  "beneficiaries": "completed",
            |  "settlors": "completed",
            |  "protectors": "completed",
            |  "other": "completed"
            |}
            |""".stripMargin)

        server.stubFor(
          post(urlEqualTo(s"/trusts-store/maintain/tasks/$identifier"))
            .withHeader(CONTENT_TYPE, containing(JSON))
            .withRequestBody(equalTo(json.toString))
            .willReturn(okJson(json.toString))
        )

        val result = connector.set(identifier, userAnswers).value

        result.futureValue mustBe Right(
          CompletedMaintenanceTasks(
            trustDetails = Completed,
            assets = Completed,
            taxLiability = Completed,
            trustees = NotStarted,
            beneficiaries = Completed,
            settlors = Completed,
            protectors = Completed,
            other = Completed
          )
        )

        application.stop()
      }

      "trust details and non-EEA company questions answered (5mld taxable)" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts-store.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsStoreConnector]

        val userAnswers = emptyUserAnswersForUtr
          .set(UpdateTrustDetailsYesNoPage, false).value
          .set(UpdateTrusteesYesNoPage, true).value
          .set(UpdateBeneficiariesYesNoPage, false).value
          .set(UpdateSettlorsYesNoPage, false).value
          .set(AddOrUpdateProtectorYesNoPage, false).value
          .set(AddOrUpdateOtherIndividualsYesNoPage, false).value
          .set(AddOrUpdateNonEeaCompanyYesNoPage, false).value

        val json = Json.parse(
          """
            |{
            |  "trustDetails": "completed",
            |  "assets": "completed",
            |  "taxLiability": "completed",
            |  "trustees": "not-started",
            |  "beneficiaries": "completed",
            |  "settlors": "completed",
            |  "protectors": "completed",
            |  "other": "completed"
            |}
            |""".stripMargin)

        server.stubFor(
          post(urlEqualTo(s"/trusts-store/maintain/tasks/$identifier"))
            .withHeader(CONTENT_TYPE, containing(JSON))
            .withRequestBody(equalTo(json.toString))
            .willReturn(okJson(json.toString))
        )

        val result = connector.set(identifier, userAnswers).value

        result.futureValue mustBe Right(
          CompletedMaintenanceTasks(
            trustDetails = Completed,
            assets = Completed,
            taxLiability = Completed,
            trustees = NotStarted,
            beneficiaries = Completed,
            settlors = Completed,
            protectors = Completed,
            other = Completed
          )
        )

        application.stop()
      }
    }

    "return Left(ServerError) when no task status is returned for setting tasks" in {

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts-store.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustsStoreConnector]

      val userAnswers = emptyUserAnswersForUtr

      val result = connector.set(identifier, userAnswers).value

      result.futureValue mustBe Left(ServerError())

      application.stop()
    }

    "return None when user is locked out of Trust IV" in {
      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts-store.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustsStoreConnector]

      server.stubFor(
        get(urlEqualTo(s"/trusts-store/claim"))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
          )
      )

      val result = Await.result(connector.get(identifier).value, Duration.Inf)
      result mustBe Right(None)
    }
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
      get(urlEqualTo(s"/trusts-store/maintain/tasks/$identifier"))
        .willReturn(serverError())
    )

    val result = connector.getStatusOfTasks(identifier).value

    result.futureValue mustBe Right(
      CompletedMaintenanceTasks(
        trustDetails = NotStarted,
        assets = NotStarted,
        taxLiability = NotStarted,
        trustees = NotStarted,
        beneficiaries = NotStarted,
        settlors = NotStarted,
        protectors = NotStarted,
        other = NotStarted
      )
    )

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
          .withStatus(OK))
    )

    val result = Await.result(connector.setFeature("TestFeature", state = true).value, Duration.Inf)

    result.value.status mustBe OK

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
            .withStatus(OK)
            .withBody(
              Json.stringify(
                Json.toJson(FeatureResponse("5mld", isEnabled = true))
              )
            )
        )
    )

    val result = Await.result(connector.getFeature("5mld").value, Duration.Inf)
    result mustBe Right(FeatureResponse("5mld", isEnabled = true))
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
            .withStatus(OK)
            .withBody(
              Json.stringify(
                Json.toJson(FeatureResponse("5mld", isEnabled = false))
              )
            )
        )
    )

    val result = Await.result(connector.getFeature("5mld").value, Duration.Inf)
    result mustBe Right(FeatureResponse("5mld", isEnabled = false))
  }

  "return OK when resetting task list" in {
    val application = applicationBuilder()
      .configure(
        Seq(
          "microservice.services.trusts-store.port" -> server.port(),
          "auditing.enabled" -> false
        ): _*
      ).build()

    val connector = application.injector.instanceOf[TrustsStoreConnector]

    server.stubFor(
      delete(urlEqualTo(s"/trusts-store/maintain/tasks/$identifier"))
        .willReturn(
          aResponse()
            .withStatus(OK)
        )
    )

    val result = Await.result(connector.resetTasks(identifier).value, Duration.Inf)
    result.value.status mustBe OK
  }

}
