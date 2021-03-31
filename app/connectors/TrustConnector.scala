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
import models.TrustDetails
import models.http.{DeclarationResponse, TrustsResponse, TrustsStatusReads}
import play.api.libs.json.{JsBoolean, JsValue, Writes}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TrustConnector @Inject()(http: HttpClient, config: FrontendAppConfig) {

  def getTrustDetails(identifier: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[TrustDetails] = {
    val url: String = s"${config.trustsUrl}/trusts/$identifier/trust-details"
    http.GET[TrustDetails](url)
  }

  def getStartDate(identifier: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[LocalDate] = {
    for {
      details <- getTrustDetails(identifier)
      date = LocalDate.parse(details.startDate)
    } yield date
  }

  def playback(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[TrustsResponse] = {
    val url: String = s"${config.trustsUrl}/trusts/$identifier/transformed"
    http.GET[TrustsResponse](url)(TrustsStatusReads.httpReads, hc, ec)
  }

  def playbackFromEtmp(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[TrustsResponse] = {
    val url: String = s"${config.trustsUrl}/trusts/$identifier/refresh"
    http.GET[TrustsResponse](url)(TrustsStatusReads.httpReads, hc, ec)
  }

  def getDoProtectorsAlreadyExist(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsBoolean] = {
    val url: String = s"${config.trustsUrl}/trusts/$identifier/transformed/protectors-already-exist"
    http.GET[JsBoolean](url)
  }

  def getDoOtherIndividualsAlreadyExist(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsBoolean] = {
    val url: String = s"${config.trustsUrl}/trusts/$identifier/transformed/other-individuals-already-exist"
    http.GET[JsBoolean](url)
  }

  def getDoNonEeaCompaniesAlreadyExist(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsBoolean] = {
    val url: String = s"${config.trustsUrl}/trusts/$identifier/transformed/non-eea-companies-already-exist"
    http.GET[JsBoolean](url)
  }

  def declare(identifier: String, payload: JsValue)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DeclarationResponse] = {
    val url: String = s"${config.trustsUrl}/trusts/declare/$identifier"
    http.POST[JsValue, DeclarationResponse](url, payload)(implicitly[Writes[JsValue]], DeclarationResponse.httpReads, hc, ec)
  }

  def setTaxableMigrationFlag(identifier: String, value: Boolean)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"${config.trustsUrl}/trusts/$identifier/taxable-migration/migrating-to-taxable"
    http.POST[Boolean, HttpResponse](url, value)
  }

  def removeTransforms(identifier: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"${config.trustsUrl}/trusts/$identifier/transforms"
    http.DELETE[HttpResponse](url)
  }

}
