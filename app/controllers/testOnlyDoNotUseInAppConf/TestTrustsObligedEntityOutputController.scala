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

import connectors.TrustsObligedEntityOutputConnector
import controllers.actions.Actions
import play.api.Logger
import play.api.http.HttpEntity
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Session

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TestTrustsObligedEntityOutputController @Inject()(actions: Actions,
                                                        connector: TrustsObligedEntityOutputConnector,
                                                        val controllerComponents: MessagesControllerComponents
                                                       )(implicit ec: ExecutionContext) extends FrontendBaseController {

  private val logger: Logger = Logger(getClass)

  def getPdf(identifier: String) = actions.auth.async {
    implicit request =>

      connector.getPdf(identifier).map { response =>

        if (response.status == 200) {
          val contentType = response.header(CONTENT_TYPE).getOrElse("application/pdf")
          val contentDisposition = CONTENT_DISPOSITION -> response.header(CONTENT_DISPOSITION).getOrElse("inline")

          response.headers.get(CONTENT_LENGTH) match {
            case Some(Seq(length)) =>
              Ok.sendEntity(HttpEntity.Streamed(response.bodyAsSource, Some(length.toLong), Some(contentType))).withHeaders(contentDisposition)
            case _ =>
              Ok.chunked(response.bodyAsSource).as(contentType).withHeaders(contentDisposition)
          }
        } else {
          logger.error(s"[Session ID: ${Session.id(hc)}][Identifier: $identifier] Error retrieving pdf: $response.")
          InternalServerError
        }
      }
  }
}
