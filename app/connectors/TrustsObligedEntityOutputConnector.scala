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

import config.FrontendAppConfig
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.http.HttpVerbs.GET
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}
import javax.inject.Inject
import scala.concurrent.Future

class TrustsObligedEntityOutputConnector @Inject()(ws: WSClient, config: FrontendAppConfig) {

  def getPdf(identifier: String)(implicit hc: HeaderCarrier): Future[WSResponse] = {
    val url: String = s"${config.trustsObligedEntityOutputUrl}/trusts-obliged-entity-output/get-pdf/$identifier"
    val headers = hc.headers(HeaderNames.explicitlyIncludedHeaders)

    ws.url(url)
      .withMethod(GET)
      .withHttpHeaders(headers: _*)
      .stream()
  }
}
