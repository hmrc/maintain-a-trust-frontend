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
import play.api.Logging
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Session

import scala.concurrent.ExecutionContext

class EnrolmentStoreStubController @Inject()(
                                              connector: TestUserConnector,
                                              override val controllerComponents: MessagesControllerComponents
                                            )(implicit ec: ExecutionContext) extends FrontendBaseController with Logging {

  def insertTestUserIntoEnrolmentStore = Action.async(parse.json) {
    implicit request =>
      logger.info(s"[EnrolmentStoreStubController][insertTestUserIntoEnrolmentStore][Session ID: ${Session.id(hc)}] inserting test user: ${request.body}")
      connector.insert(request.body).map(_ => Ok)
  }

  def flush = Action.async {
    implicit request =>
    logger.info(s"[EnrolmentStoreStubController][flush][Session ID: ${Session.id(hc)}] flushing test users from enrolment-store")
    connector.delete().map(_ => Ok)
  }

}
