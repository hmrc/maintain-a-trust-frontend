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

import base.SpecBaseHelpers
import com.github.tomakehurst.wiremock.client.WireMock._
import controllers.Assets._
import generators.Generators
import models.http._
import models.pages.ShareClass.Ordinary
import models.{FirstTaxYearAvailable, FullName, MigrationStatus, TrustDetails}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Inside, MustMatchers, OptionValues}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.Status
import play.api.libs.json.{JsBoolean, Json}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.WireMockHelper

import java.time.LocalDate
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.Source

class TrustConnectorSpec extends FreeSpec with MustMatchers with OptionValues with Generators
  with SpecBaseHelpers with WireMockHelper with ScalaFutures with Inside with ScalaCheckPropertyChecks {

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  private def playbackUrl(identifier: String) : String = s"/trusts/$identifier/transformed"
  private def declareUrl(identifier: String) : String = s"/trusts/declare/$identifier"

  private val identifier = "1000000008"

  "TrustConnector" - {

    "get trusts details" in {

      val startDate = "1920-03-28"
      val express: Boolean = false
      val taxable: Boolean = true

      val json = Json.parse(
        s"""
          |{
          |  "startDate": "$startDate",
          |  "lawCountry": "AD",
          |  "administrationCountry": "GB",
          |  "residentialStatus": {
          |    "uk": {
          |      "scottishLaw": false,
          |      "preOffShore": "AD"
          |    }
          |  },
          |  "typeOfTrust": "Will Trust or Intestacy Trust",
          |  "deedOfVariation": "Previously there was only an absolute interest under the will",
          |  "interVivos": false,
          |  "expressTrust": $express,
          |  "trustTaxable": $taxable
          |}
          |""".stripMargin)

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        get(urlEqualTo(s"/trusts/trust-details/$identifier/untransformed"))
          .willReturn(okJson(json.toString))
      )

      val result = Await.result(connector.getUntransformedTrustDetails(identifier), Duration.Inf)
      result mustBe TrustDetails(startDate = LocalDate.parse(startDate), trustTaxable = Some(taxable), expressTrust = Some(express))

      application.stop()
    }

    "playback data must" - {

      "return TrustFound response" in {

        val json = Json.parse(
          """
            |{
            |  "responseHeader": {
            |    "status": "In Processing",
            |    "formBundleNo": "1"
            |  }
            |}
            |""".stripMargin)

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(playbackUrl(identifier)))
            .willReturn(okJson(json.toString))
        )

        val result = Await.result(connector.playback(identifier), Duration.Inf)
        result mustBe Processing

        application.stop()
      }

      "return NoContent response" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(playbackUrl(identifier)))
            .willReturn(
              aResponse()
                .withStatus(Status.NO_CONTENT)))

        val result = Await.result(connector.playback(identifier), Duration.Inf)
        result mustBe SorryThereHasBeenAProblem

        application.stop()
      }

      "return NotFound response" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(playbackUrl(identifier)))
            .willReturn(
              aResponse()
                .withStatus(Status.NOT_FOUND)))

        val result = Await.result(connector.playback(identifier), Duration.Inf)
        result mustBe IdentifierNotFound

        application.stop()
      }

      "return ServiceUnavailable response" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(playbackUrl(identifier)))
            .willReturn(
              aResponse()
                .withStatus(Status.SERVICE_UNAVAILABLE)))

        val result = Await.result(connector.playback(identifier), Duration.Inf)
        result mustBe TrustServiceUnavailable

        application.stop()
      }

      "must return playback data inside a Processed trust" in {

        val utr = "1000000007"

        val source = Source.fromFile(getClass.getResource("/display-trust.json").getPath)
        val payload = source.mkString

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(playbackUrl(utr)))
            .willReturn(okJson(payload))
        )

        val processed = Await.result(connector.playback(utr), Duration.Inf)

        inside(processed) {
          case Processed(data, bundleNumber) =>

            bundleNumber mustBe "000012345678"

            data.matchData.utr.get mustBe utr

            data.correspondence.name mustBe "Trust of Brian Cloud"

            data.declaration.name mustBe FullName("Agent", None, "Agency")

            data.trust.entities.leadTrustee.leadTrusteeInd.value.name mustBe FullName("Lead", None, "Trustee")

            data.trust.details.startDate mustBe LocalDate.parse("2016-04-06")

            data.trust.entities.trustees.value.head.trusteeInd.value.lineNo mustBe Some("1")
            data.trust.entities.trustees.value.head.trusteeInd.value.identification.value.nino.value mustBe "JS123456A"
            data.trust.entities.trustees.value.head.trusteeInd.value.entityStart mustBe "2019-02-28"

            data.trust.entities.settlors.value.settlorCompany.head.name mustBe "Settlor Org 01"

            data.trust.entities.protectors.value.protectorCompany.head.lineNo mustBe Some("1")
            data.trust.entities.protectors.value.protectorCompany.head.name mustBe "Protector Org 01"
            data.trust.entities.protectors.value.protectorCompany.head.entityStart mustBe "2019-03-05"

            data.trust.assets.get.propertyOrLand.head.buildingLandName.value mustBe "Land of Brian Cloud"
        }

        source.close()
        application.stop()
      }

      "must return playback data inside a Processed trust with shares asset" in {

        val utr = "2134514321"

        val source = Source.fromFile(getClass.getResource("/display-trust-shares-asset.json").getPath)
        val payload = source.mkString

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(playbackUrl(utr)))
            .willReturn(okJson(payload))
        )

        val processed = Await.result(connector.playback(utr), Duration.Inf)

        inside(processed) {
          case Processed(data, _) =>

            data.trust.assets.get.shares.head.shareClass.get mustBe Ordinary
        }

        source.close()
        application.stop()
      }

      "must playback data for a trust with property or land, no previous value" in {

        val utr = "1000000007"

        val source = Source.fromFile(getClass.getResource("/display-trust-property-or-land-no-previous.json").getPath)
        val payload = source.mkString

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(playbackUrl(utr)))
            .willReturn(okJson(payload))
        )

        val processed = Await.result(connector.playback(utr), Duration.Inf)

        inside(processed) {
          case Processed(data, bundleNumber) =>

            bundleNumber mustBe "000012345678"

            data.matchData.utr.get mustBe utr

            data.correspondence.name mustBe "Trust of Brian Cloud"

            data.declaration.name mustBe FullName("Agent", None, "Agency")

            data.trust.entities.leadTrustee.leadTrusteeInd.value.name mustBe FullName("Lead", None, "Trustee")

            data.trust.details.startDate mustBe LocalDate.parse("2016-04-06")

            data.trust.entities.trustees.value.head.trusteeInd.value.lineNo mustBe Some("1")
            data.trust.entities.trustees.value.head.trusteeInd.value.identification.value.nino.value mustBe "JS123456A"
            data.trust.entities.trustees.value.head.trusteeInd.value.entityStart mustBe "2019-02-28"

            data.trust.entities.settlors.value.settlorCompany.head.name mustBe "Settlor Org 01"

            data.trust.entities.protectors.value.protectorCompany.head.lineNo mustBe Some("1")
            data.trust.entities.protectors.value.protectorCompany.head.name mustBe "Protector Org 01"
            data.trust.entities.protectors.value.protectorCompany.head.entityStart mustBe "2019-03-05"

            data.trust.assets.get.propertyOrLand.head.buildingLandName.value mustBe "Land of Brian Cloud"
            data.trust.assets.get.propertyOrLand.head.valueFull mustBe 999999999999L
            data.trust.assets.get.propertyOrLand.head.valuePrevious mustBe None
        }

        source.close()
        application.stop()
      }

      "must playback data for a non-taxable trust" in {

        val urn = "NTTRUST00000001"

        val source = Source.fromFile(getClass.getResource("/display-trust-non-taxable.json").getPath)
        val payload = source.mkString

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(playbackUrl(urn)))
            .willReturn(okJson(payload))
        )

        val processed = Await.result(connector.playback(urn), Duration.Inf)

        inside(processed) {
          case Processed(data, bundleNumber) =>

            bundleNumber mustBe "000012345678"

            data.matchData.urn.get mustBe urn
        }

        source.close()
        application.stop()
      }
    }

    "declare no change must" - {

      val payload = DeclarationForApi(
        declaration = Declaration(FullName("John", None, "Smith")),
        agentDetails = None,
        endDate = None
      )

      "return TVN on success" in {

        val tvn = "2345678"

        val response = Json.parse(
          s"""
            |{
            |  "tvn": "$tvn"
            |}
            |""".stripMargin)

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(declareUrl(identifier)))
            .willReturn(okJson(Json.stringify(response)))
        )

        val result = Await.result(connector.declare(identifier, payload), Duration.Inf)

        result mustEqual TVNResponse(tvn)

        application.stop()
      }

      "return an error for non-success response" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(declareUrl(identifier)))
            .willReturn(serviceUnavailable()))

        val result = Await.result(connector.declare(identifier, payload), Duration.Inf)

        result mustEqual DeclarationErrorResponse

        application.stop()
      }
    }

    "get whether protectors already exist must" - {

      "Return true or false when the request is successful" in {

        val json = JsBoolean(true)

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(s"/trusts/$identifier/transformed/protectors-already-exist"))
            .willReturn(okJson(json.toString))
        )

        val processed = Await.result(connector.getDoProtectorsAlreadyExist(identifier), Duration.Inf)

        processed.value mustBe true

        application.stop()
      }

      "throw UpstreamException when returned a 404 NotFound for the cached trust" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(s"/trusts/$identifier/transformed/protectors-already-exist"))
            .willReturn(notFound())
        )

        a[UpstreamErrorResponse] mustBe thrownBy {
          Await.result(connector.getDoProtectorsAlreadyExist(identifier), Duration.Inf)
        }

        application.stop()
      }
    }

    "get whether other individuals already exist must" - {

      "return true or false when the request is successful" in {

        val json = JsBoolean(true)

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(s"/trusts/$identifier/transformed/other-individuals-already-exist"))
            .willReturn(okJson(json.toString))
        )

        val processed = Await.result(connector.getDoOtherIndividualsAlreadyExist(identifier), Duration.Inf)

        processed.value mustBe true

        application.stop()
      }

      "throw UpstreamException when returned a 404 NotFound for the cached trust" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(s"/trusts/$identifier/transformed/other-individuals-already-exist"))
            .willReturn(notFound())
        )

        a[UpstreamErrorResponse] mustBe thrownBy {
          Await.result(connector.getDoOtherIndividualsAlreadyExist(identifier), Duration.Inf)
        }

        application.stop()
      }
    }

    "get whether non-EEA companies already exist must" - {

      "return true or false when the request is successful" in {

        val json = JsBoolean(true)

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(s"/trusts/$identifier/transformed/non-eea-companies-already-exist"))
            .willReturn(okJson(json.toString))
        )

        val processed = Await.result(connector.getDoNonEeaCompaniesAlreadyExist(identifier), Duration.Inf)

        processed.value mustBe true

        application.stop()
      }

      "throw UpstreamException when returned a 404 NotFound for the cached trust" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(s"/trusts/$identifier/transformed/non-eea-companies-already-exist"))
            .willReturn(notFound())
        )

        a[UpstreamErrorResponse] mustBe thrownBy {
          Await.result(connector.getDoNonEeaCompaniesAlreadyExist(identifier), Duration.Inf)
        }

        application.stop()
      }
    }

    ".setTaxableMigrationFlag" - {

      "return OK when the request is successful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(s"/trusts/$identifier/taxable-migration/migrating-to-taxable"))
            .withRequestBody(equalTo("true"))
            .willReturn(ok)
        )

        val processed = Await.result(connector.setTaxableMigrationFlag(identifier, value = true), Duration.Inf)

        processed.status mustBe OK

        application.stop()
      }

      "return BAD_REQUEST for invalid payload" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(s"/trusts/$identifier/taxable-migration/migrating-to-taxable"))
            .willReturn(aResponse().withStatus(BAD_REQUEST))
        )

        val processed = Await.result(connector.setTaxableMigrationFlag(identifier, value = true), Duration.Inf)

        processed.status mustBe BAD_REQUEST

        application.stop()
      }

      "return INTERNAL_SERVER_ERROR when the request is unsuccessful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(s"/trusts/$identifier/taxable-migration/migrating-to-taxable"))
            .withRequestBody(equalTo("true"))
            .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
        )

        val processed = Await.result(connector.setTaxableMigrationFlag(identifier, value = true), Duration.Inf)

        processed.status mustBe INTERNAL_SERVER_ERROR

        application.stop()
      }
    }

    ".removeTransforms" - {

      "return OK when the request is successful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          delete(urlEqualTo(s"/trusts/$identifier/transforms"))
            .willReturn(ok)
        )

        val processed = Await.result(connector.removeTransforms(identifier), Duration.Inf)

        processed.status mustBe OK

        application.stop()
      }

      "return INTERNAL_SERVER_ERROR when the request is unsuccessful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          delete(urlEqualTo(s"/trusts/$identifier/transforms"))
            .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
        )

        val processed = Await.result(connector.removeTransforms(identifier), Duration.Inf)

        processed.status mustBe INTERNAL_SERVER_ERROR

        application.stop()
      }
    }

    ".setExpressTrust" - {
      "return OK when the request is successful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          put(urlEqualTo(s"/trusts/trust-details/$identifier/express"))
            .willReturn(ok)
        )

        val result = Await.result(connector.setExpressTrust(identifier, value = true), Duration.Inf)

        result.status mustBe OK

        application.stop()
      }
    }

    ".setTaxableTrust" - {
      "return OK when the request is successful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          put(urlEqualTo(s"/trusts/trust-details/$identifier/taxable"))
            .willReturn(ok)
        )

        val result = Await.result(connector.setTaxableTrust(identifier, value = true), Duration.Inf)

        result.status mustBe OK

        application.stop()
      }
    }

    ".getSettlorsStatus" - {
      "return entity status response when the request is successful" in {

        forAll(arbitrary[MigrationStatus]) {
          migrationStatus =>

            val json = Json.toJson(migrationStatus)

            val application = applicationBuilder()
              .configure(
                Seq(
                  "microservice.services.trusts.port" -> server.port(),
                  "auditing.enabled" -> false
                ): _*
              ).build()

            val connector = application.injector.instanceOf[TrustConnector]

            server.stubFor(
              get(urlEqualTo(s"/trusts/settlors/$identifier/complete-for-migration"))
                .willReturn(okJson(json.toString))
            )

            val result = Await.result(connector.getSettlorsStatus(identifier), Duration.Inf)

            result mustBe migrationStatus

            application.stop()
        }
      }
    }

    ".getBeneficiariesStatus" - {
      "return entity status response when the request is successful" in {

        forAll(arbitrary[MigrationStatus]) {
          migrationStatus =>

            val json = Json.toJson(migrationStatus)

            val application = applicationBuilder()
              .configure(
                Seq(
                  "microservice.services.trusts.port" -> server.port(),
                  "auditing.enabled" -> false
                ): _*
              ).build()

            val connector = application.injector.instanceOf[TrustConnector]

            server.stubFor(
              get(urlEqualTo(s"/trusts/beneficiaries/$identifier/complete-for-migration"))
                .willReturn(okJson(json.toString))
            )

            val result = Await.result(connector.getBeneficiariesStatus(identifier), Duration.Inf)

            result mustBe migrationStatus

            application.stop()
        }
      }
    }

    "getFirstTaxYearToAskFor" - {
      "return first tax year available when request is successful" in {

        val json = Json.parse(
          """
            |{
            | "yearsAgo": 1,
            | "earlierYearsToDeclare": false
            |}
            |""".stripMargin)

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(s"/trusts/tax-liability/$identifier/first-year-to-ask-for"))
            .willReturn(okJson(json.toString))
        )

        val processed = connector.getFirstTaxYearToAskFor(identifier)

        whenReady(processed) {
          r =>
            r mustBe FirstTaxYearAvailable(
              yearsAgo = 1,
              earlierYearsToDeclare = false
            )
        }
      }
    }
  }
}
