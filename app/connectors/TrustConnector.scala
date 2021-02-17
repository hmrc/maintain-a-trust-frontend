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

import java.time.LocalDate

import config.FrontendAppConfig
import javax.inject.Inject
import models.TrustDetails
import models.http.{DeclarationResponse, TrustsResponse, TrustsStatusReads}
import play.api.libs.json.{JsBoolean, JsValue, Writes}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.http.HttpReads.Implicits.readFromJson

import scala.concurrent.{ExecutionContext, Future}

class TrustConnector @Inject()(http: HttpClient, config : FrontendAppConfig) {

  private def getTrustDetailsUrl(identifier: String) = s"${config.trustsUrl}/trusts/$identifier/trust-details"

  def getTrustDetails(identifier: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[TrustDetails] = {
    http.GET[TrustDetails](getTrustDetailsUrl(identifier))
  }

  def getStartDate(identifier: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[LocalDate] = {
    for {
      details <- getTrustDetails(identifier)
      date = LocalDate.parse(details.startDate)
    } yield date
  }

  def playbackUrl(identifier: String) = s"${config.trustsUrl}/trusts/$identifier/transformed"

  def playbackFromEtmpUrl(identifier: String) = s"${config.trustsUrl}/trusts/$identifier/refresh"

  private def getDoProtectorsAlreadyExistUrl(identifier: String) =
    s"${config.trustsUrl}/trusts/$identifier/transformed/protectors-already-exist"

  private def getDoOtherIndividualsAlreadyExistUrl(identifier: String) =
    s"${config.trustsUrl}/trusts/$identifier/transformed/other-individuals-already-exist"

  def declareUrl(identifier: String) = s"${config.trustsUrl}/trusts/declare/$identifier"

  def playback(identifier: String)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[TrustsResponse] = {
    http.GET[TrustsResponse](playbackUrl(identifier))(TrustsStatusReads.httpReads, hc, ec)
  }

  def playbackfromEtmp(identifier: String)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[TrustsResponse] = {
    http.GET[TrustsResponse](playbackFromEtmpUrl(identifier))(TrustsStatusReads.httpReads, hc, ec)
  }

  def getDoProtectorsAlreadyExist(identifier: String)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[JsBoolean] = {
    http.GET[JsBoolean](getDoProtectorsAlreadyExistUrl(identifier))
  }

  def getDoOtherIndividualsAlreadyExist(identifier: String)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[JsBoolean] = {
    http.GET[JsBoolean](getDoOtherIndividualsAlreadyExistUrl(identifier))
  }

  def declare(identifier: String, payload: JsValue)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse] = {
    http.POST[JsValue, DeclarationResponse](declareUrl(identifier), payload)(implicitly[Writes[JsValue]], DeclarationResponse.httpReads, hc, ec)
  }
}


