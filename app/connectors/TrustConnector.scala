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

package connectors

import config.FrontendAppConfig
import javax.inject.Inject
import models.TrustDetails
import models.http.{DeclarationResponse, TrustsResponse, TrustsStatusReads}
import play.api.libs.json.{JsBoolean, JsValue, Writes}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class TrustConnector @Inject()(http: HttpClient, config : FrontendAppConfig) {

  private def getTrustDetailsUrl(utr: String) = s"${config.trustsUrl}/trusts/$utr/trust-details"

  def getTrustDetails(utr: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[TrustDetails] = {
    http.GET[TrustDetails](getTrustDetailsUrl(utr))
  }

  def playbackUrl(utr: String) = s"${config.trustsUrl}/trusts/$utr/transformed"

  def playbackFromEtmpUrl(utr: String) = s"${config.trustsUrl}/trusts/$utr/refresh"

  private def getDoProtectorsAlreadyExistUrl(utr: String) =
    s"${config.trustsUrl}/trusts/$utr/transformed/protectors-already-exist"

  private def getDoOtherIndividualsAlreadyExistUrl(utr: String) =
    s"${config.trustsUrl}/trusts/$utr/transformed/other-individuals-already-exist"

  def declareUrl(utr: String) = s"${config.trustsUrl}/trusts/declare/$utr"

  def playback(utr: String)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[TrustsResponse] = {
    http.GET[TrustsResponse](playbackUrl(utr))(TrustsStatusReads.httpReads, hc, ec)
  }

  def playbackfromEtmp(utr: String)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[TrustsResponse] = {
    http.GET[TrustsResponse](playbackFromEtmpUrl(utr))(TrustsStatusReads.httpReads, hc, ec)
  }

  def getDoProtectorsAlreadyExist(utr: String)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[JsBoolean] = {
    http.GET[JsBoolean](getDoProtectorsAlreadyExistUrl(utr))
  }

  def getDoOtherIndividualsAlreadyExist(utr: String)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[JsBoolean] = {
    http.GET[JsBoolean](getDoOtherIndividualsAlreadyExistUrl(utr))
  }

  def declare(utr: String, payload: JsValue)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse] = {
    http.POST[JsValue, DeclarationResponse](declareUrl(utr), payload)(implicitly[Writes[JsValue]], DeclarationResponse.httpReads, hc, ec)
  }
}


