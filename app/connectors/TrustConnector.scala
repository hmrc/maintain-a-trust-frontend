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

import config.FrontendAppConfig
import models.http.{DeclarationForApi, DeclarationResponse, TrustsResponse}
import models.{FirstTaxYearAvailable, MigrationTaskStatus, TrustDetails}
import play.api.Logging
import play.api.http.HeaderNames
import play.api.libs.json.{JsBoolean, Writes}
import play.api.mvc.Request
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TrustConnector @Inject()(http: HttpClient, config: FrontendAppConfig) extends Logging {

  private lazy val baseUrl: String = s"${config.trustsUrl}/trusts"

  def getUntransformedTrustDetails(identifier: String)
                                  (implicit hc: HeaderCarrier, ex: ExecutionContext): Future[TrustDetails] = {
    val url: String = s"$baseUrl/trust-details/$identifier/untransformed"
    http.GET[TrustDetails](url)
  }

  def getStartDate(identifier: String)
                  (implicit hc: HeaderCarrier, ex: ExecutionContext): Future[LocalDate] = {
    getUntransformedTrustDetails(identifier) map { trustDetails =>
      trustDetails.startDate
    }
  }

  def playback(identifier: String)
              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[TrustsResponse] = {
    val url: String = s"$baseUrl/$identifier/transformed"
    http.GET[TrustsResponse](url)
  }

  def playbackFromEtmp(identifier: String)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[TrustsResponse] = {
    val url: String = s"$baseUrl/$identifier/refresh"
    http.GET[TrustsResponse](url)
  }

  def getDoProtectorsAlreadyExist(identifier: String)
                                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsBoolean] = {
    val url: String = s"$baseUrl/$identifier/transformed/protectors-already-exist"
    http.GET[JsBoolean](url)
  }

  def getDoOtherIndividualsAlreadyExist(identifier: String)
                                       (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsBoolean] = {
    val url: String = s"$baseUrl/$identifier/transformed/other-individuals-already-exist"
    http.GET[JsBoolean](url)
  }

  def getDoNonEeaCompaniesAlreadyExist(identifier: String)
                                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsBoolean] = {
    val url: String = s"$baseUrl/$identifier/transformed/non-eea-companies-already-exist"
    http.GET[JsBoolean](url)
  }

  def declare(identifier: String, payload: DeclarationForApi)
             (implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Future[DeclarationResponse] = {

    val trueUserAgent = "True-User-Agent"

    val newHc: HeaderCarrier = hc.withExtraHeaders(
      trueUserAgent -> request.headers.get(HeaderNames.USER_AGENT).getOrElse("No user agent provided")
    )

    val url: String = s"$baseUrl/declare/$identifier"
    http.POST[DeclarationForApi, DeclarationResponse](url, payload)(implicitly[Writes[DeclarationForApi]], DeclarationResponse.httpReads, newHc, ec)
  }

  def setTaxableMigrationFlag(identifier: String, value: Boolean)
                             (implicit hc: HeaderCarrier, ex: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$baseUrl/$identifier/taxable-migration/migrating-to-taxable"
    http.POST[Boolean, HttpResponse](url, value)
  }

  def removeTransforms(identifier: String)
                      (implicit hc: HeaderCarrier, ex: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$baseUrl/$identifier/transforms"
    http.DELETE[HttpResponse](url)
  }

  def setExpressTrust(identifier: String, value: Boolean)
                     (implicit hc: HeaderCarrier, ex: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$baseUrl/trust-details/$identifier/express"
    http.PUT[Boolean, HttpResponse](url, value)
  }

  def setTaxableTrust(identifier: String, value: Boolean)
                     (implicit hc: HeaderCarrier, ex: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$baseUrl/trust-details/$identifier/taxable"
    http.PUT[Boolean, HttpResponse](url, value)
  }

  def getSettlorsStatus(identifier: String)
                       (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MigrationTaskStatus] = {
    val url: String = s"$baseUrl/settlors/$identifier/complete-for-migration"
    http.GET[MigrationTaskStatus](url)
  }

  def getBeneficiariesStatus(identifier: String)
                            (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MigrationTaskStatus] = {
    val url: String = s"$baseUrl/beneficiaries/$identifier/complete-for-migration"
    http.GET[MigrationTaskStatus](url)
  }

  def getFirstTaxYearToAskFor(identifier: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[FirstTaxYearAvailable] = {
    val url = s"$baseUrl/tax-liability/$identifier/first-year-to-ask-for"
    http.GET[FirstTaxYearAvailable](url)
  }

}
