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
import models.requests.EnrolmentStoreResponse
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class EnrolmentStoreConnector @Inject()(http: HttpClient, config : FrontendAppConfig) {

  private def enrolmentsEndpoint(identifier: String): String = {
    val identifierKey = "SAUTR"
    s"${config.enrolmentStoreProxyUrl}/enrolment-store-proxy/enrolment-store/enrolments/HMRC-TERS-ORG~$identifierKey~$identifier/users"
  }

  def checkIfAlreadyClaimed(utr: String)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[EnrolmentStoreResponse] =
    http.GET[EnrolmentStoreResponse](enrolmentsEndpoint(utr))(EnrolmentStoreResponse.httpReads, hc, ec)

}