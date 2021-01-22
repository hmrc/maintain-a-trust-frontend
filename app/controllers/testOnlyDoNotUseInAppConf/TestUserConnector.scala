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

package controllers.testOnlyDoNotUseInAppConf

import com.google.inject.Inject
import config.FrontendAppConfig
import play.api.libs.json.{JsValue, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class TestUserConnector @Inject()(http: HttpClient, config: FrontendAppConfig) {

  private val dataUrl: String = s"${config.enrolmentStoreProxyUrl}/enrolment-store-stub/data"

  object InsertedReads {
    implicit lazy val httpReads: HttpReads[Unit] = (_: String, _: String, _: HttpResponse) => {
        // Ignore the response from enrolment-store-stub
        ()
      }
  }

  def insert(user: JsValue)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    val headers = Seq(
      ("content-type", "application/json")
    )
    http.POST[JsValue, Unit](dataUrl, user, headers)(implicitly[Writes[JsValue]], InsertedReads.httpReads, hc, ec)
  }

  def delete()(implicit hc: HeaderCarrier, ec: ExecutionContext) : Future[HttpResponse] = {
    http.DELETE[HttpResponse](dataUrl)
  }
}