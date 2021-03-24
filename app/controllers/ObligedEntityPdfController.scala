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

package controllers

import connectors.TrustsObligedEntityOutputConnector
import controllers.actions.Actions
import handlers.ErrorHandler
import javax.inject.Inject
import play.api.Logging
import play.api.http.HttpEntity
import play.api.i18n.Langs
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.language.LanguageUtils
import utils.Session

import scala.concurrent.ExecutionContext
import scala.util.matching.Regex

class ObligedEntityPdfController @Inject()(
                                            actions: Actions,
                                            connector: TrustsObligedEntityOutputConnector,
                                            val controllerComponents: MessagesControllerComponents,
                                            errorHandler: ErrorHandler,
                                            lang: LanguageUtils
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with Logging {

  case class Headers(contentDisposition: String, contentType: String, contentLength: Long) {

    val dispositionRegex: Regex = "((.+)=(.+).pdf)".r

    /**
     * Content-Disposition: inline; filename=1234567890-2020-10-10.pdf
     * instruction will be "inline; filename="
     * fileName will be "1234567890-2020-10-10"
     */
    def fileNameWithServiceName(implicit request: Request[AnyContent]): String = contentDisposition match {
      case dispositionRegex(instruction, fileName) =>
        s"$instruction=$fileName - ${messagesApi("site.service_name")(lang.getCurrentLang)} - GOV.UK.pdf"
      case _ =>
        contentDisposition
    }

  }

  def getPdf(identifier: String): Action[AnyContent] = actions.auth.async {
    implicit request =>

      connector.getPdf(identifier).map { response =>

        if (response.status == OK) {

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
              )).withHeaders(CONTENT_DISPOSITION -> h.fileNameWithServiceName)
            case _ =>
              logger.error(s"[Session ID: ${Session.id(hc)}][Identifier: $identifier] Response has insufficient headers: ${response.headers}.")
              InternalServerError(errorHandler.internalServerErrorTemplate)
          }
        } else {
          logger.error(s"[Session ID: ${Session.id(hc)}][Identifier: $identifier] Error retrieving pdf: $response.")
          InternalServerError(errorHandler.internalServerErrorTemplate)
        }
      } recover {
        case e =>
          logger.error(s"[Session ID: ${Session.id(hc)}][Identifier: $identifier] Exception thrown when retrieving pdf ${e.getMessage}")
          InternalServerError(errorHandler.internalServerErrorTemplate)
      }
  }
}
