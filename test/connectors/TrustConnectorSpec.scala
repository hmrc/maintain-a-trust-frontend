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
import models.http.DeclarationResponse.InternalServerError
import models.http._
import models.pages.ShareClass.Ordinary
import models.{FullName, TrustDetails}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Inside, MustMatchers, OptionValues}
import play.api.http.Status
import play.api.libs.json.{JsBoolean, Json}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.WireMockHelper

import java.time.LocalDate
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.Source

class TrustConnectorSpec extends FreeSpec with MustMatchers
  with OptionValues with Generators with SpecBaseHelpers with WireMockHelper with ScalaFutures with Inside {
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  private def playbackUrl(utr: String) : String = s"/trusts/$utr/transformed"
  private def declareUrl(utr: String) : String = s"/trusts/declare/$utr"

  private val identifier = "1000000008"

  "TrustConnector" - {

    "get trusts details" in {

      val utr = "2134514321"

      val json = Json.parse(
        """
          |{
          | "startDate": "1920-03-28",
          | "lawCountry": "AD",
          | "administrationCountry": "GB",
          | "residentialStatus": {
          |   "uk": {
          |     "scottishLaw": false,
          |     "preOffShore": "AD"
          |   }
          | },
          | "typeOfTrust": "Will Trust or Intestacy Trust",
          | "deedOfVariation": "Previously there was only an absolute interest under the will",
          | "interVivos": false
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
        get(urlEqualTo(s"/trusts/$utr/trust-details"))
          .willReturn(okJson(json.toString))
      )


      val result  = Await.result(connector.getTrustDetails(utr), Duration.Inf)
      result mustBe TrustDetails(startDate = "1920-03-28")
    }

    "playback data must" - {

      "return TrustFound response" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        val utr = "10000000008"

        server.stubFor(
          get(urlEqualTo(playbackUrl(utr)))
            .willReturn(
              aResponse()
                .withStatus(Status.OK)
                .withBody("""{
                            |
                            |  "responseHeader": {
                            |    "status": "In Processing",
                            |    "formBundleNo": "1"
                            |  }
                            |}""".stripMargin)
            )
        )

        val result  = Await.result(connector.playback(utr),Duration.Inf)
        result mustBe Processing

        application.stop()
      }

      "return NoContent response" in {

        val utr = "6666666666"

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
            .willReturn(
              aResponse()
                .withStatus(Status.NO_CONTENT)))

        val result  = Await.result(connector.playback(utr),Duration.Inf)
        result mustBe SorryThereHasBeenAProblem

        application.stop()
      }

      "return NotFound response" in {

        val utr = "10000000008"

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
            .willReturn(
              aResponse()
                .withStatus(Status.NOT_FOUND)))

        val result  = Await.result(connector.playback(utr),Duration.Inf)
        result mustBe IdentifierNotFound

        application.stop()
      }

      "return ServiceUnavailable response" in {

        val utr = "10000000008"

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
            .willReturn(
              aResponse()
                .withStatus(Status.SERVICE_UNAVAILABLE)))

        val result  = Await.result(connector.playback(utr), Duration.Inf)
        result mustBe TrustServiceUnavailable

        application.stop()
      }

      "must return playback data inside a Processed trust" in {
        val utr = "1000000007"
        val payload = Source.fromFile(getClass.getResource("/display-trust.json").getPath).mkString

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

            data.matchData.utr.get mustBe "1000000007"

            data.correspondence.name mustBe "Trust of Brian Cloud"

            data.declaration.name mustBe FullName("Agent", None, "Agency")

            data.trust.entities.leadTrustee.leadTrusteeInd.value.name mustBe FullName("Lead", None, "Trustee")

            data.trust.details.startDate mustBe LocalDate.of(2016, 4, 6)

            data.trust.entities.trustees.value.head.trusteeInd.value.lineNo mustBe Some("1")
            data.trust.entities.trustees.value.head.trusteeInd.value.identification.value.nino.value mustBe "JS123456A"
            data.trust.entities.trustees.value.head.trusteeInd.value.entityStart mustBe "2019-02-28"

            data.trust.entities.settlors.value.settlorCompany.head.name mustBe "Settlor Org 01"

            data.trust.entities.protectors.value.protectorCompany.head.lineNo mustBe Some("1")
            data.trust.entities.protectors.value.protectorCompany.head.name mustBe "Protector Org 01"
            data.trust.entities.protectors.value.protectorCompany.head.entityStart mustBe "2019-03-05"

            data.trust.assets.get.propertyOrLand.head.buildingLandName.value mustBe "Land of Brian Cloud"
        }

        application.stop()
      }

      "must return playback data inside a Processed trust with shares asset" in {
        val utr = "2134514321"
        val payload = Source.fromFile(getClass.getResource("/display-trust-shares-asset.json").getPath).mkString

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

            data.trust.assets.get.shares.head.shareClass.get mustBe Ordinary
        }

        application.stop()
      }

      "must playback data for a trust with property or land, no previous value" in {
        val utr = "1000000007"
        val payload = Source.fromFile(getClass.getResource("/display-trust-property-or-land-no-previous.json").getPath).mkString

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

            data.matchData.utr.get mustBe "1000000007"

            data.correspondence.name mustBe "Trust of Brian Cloud"

            data.declaration.name mustBe FullName("Agent", None, "Agency")

            data.trust.entities.leadTrustee.leadTrusteeInd.value.name mustBe FullName("Lead", None, "Trustee")

            data.trust.details.startDate mustBe LocalDate.of(2016, 4, 6)

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

        application.stop()
      }

      "must playback data for a non-taxable trust" in {

        val urn = "NTTRUST00000001"
        val payload = Source.fromFile(getClass.getResource("/display-trust-non-taxable.json").getPath).mkString

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

        application.stop()
      }
    }

    "declare no change must" - {

      "return TVN on success" in {
        val utr = "1000000007"
        val response = Json.parse(
          """
            |{
            | "tvn": "2345678"
            |}
            |""".stripMargin)

        val payload = Json.parse(
          s"""
             |{
             | "name": {
             |   "firstName": "John",
             |   "lastName": "Smith"
             | },
             | "address": {
             |   "line1": "Line 1",
             |   "line2": "Line 2",
             |   "postCode": "NE981ZZ",
             |   "country": "GB"
             | }
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
          post(urlEqualTo(declareUrl(utr)))
            .willReturn(okJson(Json.stringify(response)).withStatus(Status.OK))
        )

        val result = Await.result(connector.declare(utr, payload), Duration.Inf)

        result mustEqual TVNResponse("2345678")

      }

      "return an error for non-success response" in {
        val utr = "1000000007"
        val payload = Json.parse(
          s"""
             |{
             | "name": {
             |   "firstName": "John",
             |   "lastName": "Smith"
             | },
             | "address": {
             |   "line1": "Line 1",
             |   "line2": "Line 2",
             |   "postCode": "NE981ZZ",
             |   "country": "GB"
             | }
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
          post(urlEqualTo(declareUrl(utr)))
            .willReturn(
              aResponse()
                .withStatus(Status.SERVICE_UNAVAILABLE)))

        val result = Await.result(connector.declare(utr, payload), Duration.Inf)

        result mustEqual InternalServerError
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

    ".isTrust5mld" - {

      "return true" - {
        "untransformed data is 5mld" in {
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
            get(urlEqualTo(s"/trusts/$identifier/is-trust-5mld"))
              .willReturn(okJson(json.toString))
          )

          val processed = connector.isTrust5mld(identifier)

          whenReady(processed) {
            r =>
              r mustBe true
          }
        }
      }

      "return false" - {
        "untransformed data is 4mld" in {
          val json = JsBoolean(false)

          val application = applicationBuilder()
            .configure(
              Seq(
                "microservice.services.trusts.port" -> server.port(),
                "auditing.enabled" -> false
              ): _*
            ).build()

          val connector = application.injector.instanceOf[TrustConnector]

          server.stubFor(
            get(urlEqualTo(s"/trusts/$identifier/is-trust-5mld"))
              .willReturn(okJson(json.toString))
          )

          val processed = connector.isTrust5mld(identifier)

          whenReady(processed) {
            r =>
              r mustBe false
          }
        }
      }
    }
  }
}
