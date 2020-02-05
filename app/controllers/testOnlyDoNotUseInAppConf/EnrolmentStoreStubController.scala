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

package controllers.testOnlyDoNotUseInAppConf

import com.google.inject.Inject
import config.FrontendAppConfig
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.controller.BackendBaseController
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class TestUserConnector @Inject()(http: HttpClient, config: FrontendAppConfig) {

  private val insertDataUrl: String = s"${config.enrolmentStoreProxyUrl}/enrolment-store-stub/data"

  def insert(user: JsValue)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val headers = Seq(
      ("content-type", "application/json")
    )
    http.POST[JsValue, HttpResponse](insertDataUrl, user, headers)
  }
}

class EnrolmentStoreStubController @Inject()(
                                              connector: TestUserConnector,
                                              val controllerComponents: ControllerComponents
                                            )(implicit ec: ExecutionContext) extends BackendBaseController {

  def insertTestUserIntoEnrolmentStore = Action.async(parse.json) {
    implicit request =>
      Logger.info(s"[EnrolmentStoreStubController] inserting test user: ${request.body}")
      connector.insert(request.body).map(_ => Ok)
  }

}
