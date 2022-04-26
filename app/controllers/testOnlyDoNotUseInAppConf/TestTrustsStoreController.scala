/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.TrustsStoreConnector

import javax.inject.Inject
import play.api.Logging
import play.api.libs.json.JsBoolean
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Session

import scala.concurrent.{ExecutionContext, Future}

class TestTrustsStoreController @Inject()(
                                           connector: TrustsStoreConnector,
                                           val controllerComponents: MessagesControllerComponents
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with Logging {

  def set4Mld: Action[AnyContent] = Action.async {
    implicit request =>
      logger.info(s"[TestTrustsStoreController][set4Mld][Session ID: ${Session.id(hc)}] set 4MLD mode")
      connector.setFeature("5mld", state = false).map(_ => Ok)
  }

  def set5Mld: Action[AnyContent] = Action.async {
    implicit request =>
      logger.info(s"[TestTrustsStoreController][set5Mld][Session ID: ${Session.id(hc)}] set 5MLD mode")
      connector.setFeature("5mld", state = true).map(_ => Ok)
  }

  /**
   *
   * @param feature can be either "5mld" or "non-taxable.access-code"
   * @return Ok if successful or BadRequest for invalid request body
   */
  def setFeature(feature: String): Action[AnyContent] = Action.async {
    implicit request =>
      request.body.asJson match {
        case Some(JsBoolean(value)) =>
          logger.info(s"[TestTrustsStoreController][setFeature][Session ID: ${Session.id(hc)}] setting $feature to $value")
          connector.setFeature(feature, value).map(_ => Ok).recover {
            case ex =>
              logger.error(s"[TestTrustsStoreController][setFeature][Session ID: ${Session.id(hc)}] error setting feature: ${ex.getMessage}", ex)
              InternalServerError
          }
        case None =>
          logger.error(s"[TestTrustsStoreController][setFeature][Session ID: ${Session.id(hc)}] invalid request body")
          Future.successful(BadRequest)
      }
  }
}
