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

package controllers.testOnlyDoNotUseInAppConf

import com.google.inject.Inject
import config.FrontendAppConfig
import play.api.Logging
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}

class TestUserConnector @Inject()(http: HttpClientV2, config: FrontendAppConfig) extends Logging {

  private val dataUrl: String = s"${config.enrolmentStoreProxyUrl}/enrolment-store-stub/data"

  def insert(user: JsValue)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    val headers = Seq(
      ("content-type", "application/json")
    )
    http
      .post(url"$dataUrl")
      .withBody(user)
      .setHeader(headers: _*)
      .execute[Unit]
  }

  def delete()(implicit hc: HeaderCarrier, ec: ExecutionContext) : Future[HttpResponse] = {
    http
      .delete(url"$dataUrl")
      .execute[HttpResponse]
  }
}
