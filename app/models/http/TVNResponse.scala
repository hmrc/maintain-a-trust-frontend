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

package models.http

import play.api.Logger
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

sealed trait DeclarationResponse

final case class TVNResponse(tvn : String) extends DeclarationResponse

object TVNResponse {

  implicit val formats : OFormat[TVNResponse] = Json.format[TVNResponse]

}

object DeclarationResponse {

  implicit object RegistrationResponseFormats extends Reads[DeclarationResponse] {

    override def reads(json: JsValue): JsResult[DeclarationResponse] = json.validate[TVNResponse]

  }

  case object InternalServerError extends DeclarationResponse

  case object CannotDeclareError extends DeclarationResponse

  implicit lazy val httpReads: HttpReads[DeclarationResponse] =
    new HttpReads[DeclarationResponse] {
      override def read(method: String, url: String, response: HttpResponse): DeclarationResponse = {
        Logger.info(s"[TrustResponse] response status received from trusts api: ${response.status}")

        response.status match {
          case OK =>
            response.json.as[TVNResponse]
          case _ =>
            InternalServerError
        }
      }
    }


}
