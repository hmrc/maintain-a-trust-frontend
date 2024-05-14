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

package controllers

import connectors.TrustsObligedEntityOutputConnector
import controllers.actions.Actions
import handlers.ErrorHandler

import javax.inject.Inject
import models.headers.PdfHeaders
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.http.HttpEntity
import play.api.libs.ws.WSResponse
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.language.LanguageUtils
import utils.Session

import scala.concurrent.ExecutionContext

class ObligedEntityPdfController @Inject()(
                                            actions: Actions,
                                            connector: TrustsObligedEntityOutputConnector,
                                            val controllerComponents: MessagesControllerComponents,
                                            errorHandler: ErrorHandler
                                          )(
                                            implicit ec: ExecutionContext,
                                            lang: LanguageUtils
                                          ) extends FrontendBaseController with Logging {

  private val className = getClass.getSimpleName

  def getPdf(identifier: String): Action[AnyContent] = actions.auth.async {
    implicit request =>

      connector.getPdf(identifier).map { response =>
        response.status match {
          case OK => handleSuccessfulGetPdfResponse(identifier, response)
          case _ =>
            logger.error(s"[$className][getPdf][Session ID: ${Session.id(hc)}][Identifier: $identifier] Error retrieving pdf: $response.")
            InternalServerError(errorHandler.internalServerErrorTemplate)
        }
      } recover {
        case e =>
          logger.error(s"[$className][getPdf][Session ID: ${Session.id(hc)}][Identifier: $identifier] Exception thrown when retrieving pdf ${e.getMessage}")
          InternalServerError(errorHandler.internalServerErrorTemplate)
      }
  }

  private def handleSuccessfulGetPdfResponse(identifier: String, response: WSResponse)
                                            (implicit request: IdentifierRequest[AnyContent]): Result = {

    val headers: Option[PdfHeaders] = for {
      contentDisposition <- response.header(CONTENT_DISPOSITION)
      contentType <- response.header(CONTENT_TYPE)
      contentLength <- response.header(CONTENT_LENGTH)
    } yield {
      PdfHeaders(contentDisposition, contentType, contentLength.toLong)
    }

    headers match {
      case Some(h) =>
        Ok.sendEntity(HttpEntity.Streamed(data = response.bodyAsSource, contentLength = Some(h.contentLength), contentType = Some(h.contentType)))
          .withHeaders(CONTENT_DISPOSITION -> h.fileNameWithServiceName)
      case _ =>
        logger.error(s"[$className][handleSuccessfulGetPdfResponse][Session ID: ${Session.id(hc)}][Identifier: $identifier] Response has no headers: ${response.headers}.")
        InternalServerError(errorHandler.internalServerErrorTemplate)
    }
  }
}
