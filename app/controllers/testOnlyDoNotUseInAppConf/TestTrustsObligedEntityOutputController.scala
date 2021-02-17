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

import connectors.TrustsObligedEntityOutputConnector
import controllers.actions.Actions
import play.api.Logging
import play.api.http.HttpEntity
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Session

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TestTrustsObligedEntityOutputController @Inject()(
                                                         actions: Actions,
                                                         connector: TrustsObligedEntityOutputConnector,
                                                         val controllerComponents: MessagesControllerComponents
                                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with Logging {

  def getPdf(identifier: String): Action[AnyContent] = actions.auth.async {
    implicit request =>

      connector.getPdf(identifier).map { response =>

        if (response.status == OK) {

          case class Headers(contentDisposition: String, contentType: String, contentLength: Long)

          val headers: Option[Headers] = for {
            contentDisposition <- response.header(CONTENT_DISPOSITION)
            contentType <- response.header(CONTENT_TYPE)
            contentLength <- response.header(CONTENT_LENGTH)
          } yield {
            Headers(contentDisposition, contentType, contentLength.toLong)
          }

          headers match {
            case Some(h) =>
              Ok.sendEntity(HttpEntity.Streamed(
                data = response.bodyAsSource,
                contentLength = Some(h.contentLength),
                contentType = Some(h.contentType)
              )).withHeaders(CONTENT_DISPOSITION -> h.contentDisposition)
            case _ =>
              logger.error(s"[Session ID: ${Session.id(hc)}][Identifier: $identifier] Response has insufficient headers.")
              InternalServerError
          }
        } else {
          logger.error(s"[Session ID: ${Session.id(hc)}][Identifier: $identifier] Error retrieving pdf: $response.")
          InternalServerError
        }
      }
  }
}
