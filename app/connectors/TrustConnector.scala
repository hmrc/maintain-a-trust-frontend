/*
 * Copyright 2024 HM Revenue & Customs
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

import cats.data.EitherT
import config.FrontendAppConfig
import models.errors.DeclarationError
import models.http.{DeclarationForApi, TVNResponse, TrustsResponse}
import models.{FirstTaxYearAvailable, MigrationTaskStatus, TrustDetails}
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.libs.json.{JsBoolean, Json}
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}
import utils.TrustEnvelope.TrustEnvelope

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TrustConnector @Inject() (http: HttpClientV2, config: FrontendAppConfig) extends ConnectorErrorResponseHandler {

  override val className: String = getClass.getSimpleName

  private lazy val baseUrl: String = s"${config.trustsUrl}/trusts"

  def getUntransformedTrustDetails(identifier: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[TrustDetails] = EitherT {
    val url: String = s"$baseUrl/trust-details/$identifier/untransformed"
    http
      .get(url"$url")
      .execute[TrustDetails]
      .map(Right(_))
      .recover { case ex =>
        Left(handleError(ex, "getUntransformedTrustDetails"))
      }
  }

  def getStartDate(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[LocalDate] =
    getUntransformedTrustDetails(identifier) map { trustDetails =>
      trustDetails.startDate
    }

  def playback(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[TrustsResponse] =
    EitherT {
      val url: String = s"$baseUrl/$identifier/transformed"
      http
        .get(url"$url")
        .execute[TrustsResponse]
        .map(Right(_))
        .recover { case ex =>
          Left(handleError(ex, "playback"))
        }
    }

  def playbackFromEtmp(
    identifier: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[TrustsResponse] = EitherT {
    val url: String = s"$baseUrl/$identifier/refresh"
    http
      .get(url"$url")
      .execute[TrustsResponse]
      .map(Right(_))
      .recover { case ex =>
        Left(handleError(ex, "playbackFromEtmp"))
      }
  }

  def getDoProtectorsAlreadyExist(
    identifier: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[JsBoolean] = EitherT {
    val url: String = s"$baseUrl/$identifier/transformed/protectors-already-exist"
    http
      .get(url"$url")
      .execute[JsBoolean]
      .map(Right(_))
      .recover { case ex =>
        Left(handleError(ex, "getDoProtectorsAlreadyExist"))
      }
  }

  def getDoOtherIndividualsAlreadyExist(
    identifier: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[JsBoolean] = EitherT {
    val url: String = s"$baseUrl/$identifier/transformed/other-individuals-already-exist"
    http
      .get(url"$url")
      .execute[JsBoolean]
      .map(Right(_))
      .recover { case ex =>
        Left(handleError(ex, "getDoProtectorsAlreadyExist"))
      }
  }

  def getDoNonEeaCompaniesAlreadyExist(
    identifier: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[JsBoolean] = EitherT {
    val url: String = s"$baseUrl/$identifier/transformed/non-eea-companies-already-exist"
    http
      .get(url"$url")
      .execute[JsBoolean]
      .map(Right(_))
      .recover { case ex =>
        Left(handleError(ex, "getDoNonEeaCompaniesAlreadyExist"))
      }
  }

  def declare(identifier: String, payload: DeclarationForApi)(implicit
                                                              request: RequestHeader,
                                                              hc: HeaderCarrier,
                                                              ec: ExecutionContext
  ): TrustEnvelope[TVNResponse] = EitherT {

    val httpReads     = HttpReads.Implicits.readRaw
    val trueUserAgent = "True-User-Agent"

    val newHc: HeaderCarrier = hc.withExtraHeaders(
      trueUserAgent -> request.headers.get(HeaderNames.USER_AGENT).getOrElse("No user agent provided")
    )
    val url: String          = s"$baseUrl/declare/$identifier"
    http
      .post(url"$url")(newHc)
      .withBody(Json.toJson(payload))
      .execute[HttpResponse](httpReads,ec)
      .map { response =>
        response.status match {
          case OK     => Right(response.json.as[TVNResponse])
          case status =>
            logger.error(
              s"[$className][declare] problem declaring trust, received a non successful status code: $status"
            )
            Left(DeclarationError())
        }
      }
      .recover { case ex =>
        Left(handleError(ex, "declare"))
      }
  }

  def setTaxableMigrationFlag(identifier: String, value: Boolean)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): TrustEnvelope[HttpResponse] = EitherT {
    val url: String = s"$baseUrl/$identifier/taxable-migration/migrating-to-taxable"
    http
      .post(url"$url")
      .withBody(Json.toJson(value))
      .execute[HttpResponse]
      .map(Right(_))
      .recover { case ex =>
        Left(handleError(ex, "setTaxableMigrationFlag"))
      }
  }

  def removeTransforms(
    identifier: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[HttpResponse] = EitherT {
    val url: String = s"$baseUrl/$identifier/transforms"
    http
      .delete(url"$url")
      .execute[HttpResponse]
      .map(Right(_))
      .recover { case ex =>
        Left(handleError(ex, "removeTransforms"))
      }
  }

  def setExpressTrust(identifier: String, value: Boolean)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): TrustEnvelope[HttpResponse] = EitherT {
    val url: String = s"$baseUrl/trust-details/$identifier/express"
    http
      .put(url"$url")
      .withBody(Json.toJson(value))
      .execute[HttpResponse]
      .map(Right(_))
      .recover { case ex =>
        Left(handleError(ex, "setExpressTrust"))
      }
  }

  def setTaxableTrust(identifier: String, value: Boolean)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): TrustEnvelope[HttpResponse] = EitherT {
    val url: String = s"$baseUrl/trust-details/$identifier/taxable"
    http
      .put(url"$url")
      .withBody(Json.toJson(value))
      .execute[HttpResponse]
      .map(Right(_))
      .recover { case ex =>
        Left(handleError(ex, "setTaxableTrust"))
      }
  }

  def setSchedule3aExempt(identifier: String, value: Boolean)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): TrustEnvelope[HttpResponse] = EitherT {
    val url: String = s"$baseUrl/trust-details/$identifier/schedule-3a-exempt"
    http
      .put(url"$url")
      .withBody(Json.toJson(value))
      .execute[HttpResponse]
      .map(Right(_))
      .recover { case ex =>
        Left(handleError(ex, "setSchedule3aExempt"))
      }
  }

  def getSettlorsStatus(
    identifier: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[MigrationTaskStatus] = EitherT {
    val url: String = s"$baseUrl/settlors/$identifier/complete-for-migration"
    http
      .get(url"$url")
      .execute[MigrationTaskStatus]
      .map(Right(_))
      .recover { case ex =>
        Left(handleError(ex, "getSettlorsStatus"))
      }
  }

  def getBeneficiariesStatus(
    identifier: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[MigrationTaskStatus] = EitherT {
    val url: String = s"$baseUrl/beneficiaries/$identifier/complete-for-migration"
    http
      .get(url"$url")
      .execute[MigrationTaskStatus]
      .map(Right(_))
      .recover { case ex =>
        Left(handleError(ex, "getBeneficiariesStatus"))
      }
  }

  def getFirstTaxYearToAskFor(
    identifier: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[FirstTaxYearAvailable] = EitherT {
    val url = s"$baseUrl/tax-liability/$identifier/first-year-to-ask-for"
    http
      .get(url"$url")
      .execute[FirstTaxYearAvailable]
      .map(Right(_))
      .recover { case ex =>
        Left(handleError(ex, "getFirstTaxYearToAskFor"))
      }
  }
}
