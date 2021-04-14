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

package models.http

import play.api.Logging
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

sealed trait DeclarationResponse

final case class TVNResponse(tvn: String) extends DeclarationResponse

object TVNResponse {
  implicit val format: Format[TVNResponse] = Json.format[TVNResponse]
}

case object DeclarationErrorResponse extends DeclarationResponse

object DeclarationResponse extends Logging {

  implicit lazy val httpReads: HttpReads[DeclarationResponse] = (_: String, _: String, response: HttpResponse) => {
    logger.info(s"[DeclarationResponse] response status received from trusts api: ${response.status}")

    response.status match {
      case OK =>
        response.json.as[TVNResponse]
      case _ =>
        DeclarationErrorResponse
    }
  }

}
