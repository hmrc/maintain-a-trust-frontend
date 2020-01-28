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

trait TrustResponse

final case class TVNResponse(tvn : String) extends TrustResponse

object TVNResponse {

  implicit val formats : OFormat[TVNResponse] = Json.format[TVNResponse]

}

object TrustResponse {

  implicit object RegistrationResponseFormats extends Format[TrustResponse] {

    override def reads(json: JsValue): JsResult[TrustResponse] = json.validate[TVNResponse]

    override def writes(o: TrustResponse): JsValue = o match {
      case x : TVNResponse => Json.toJson(x)(TVNResponse.formats)
//      case x : ErrorAuditEvent => Json.toJson(x)(ErrorAuditEvent.formats)
    }

  }

  case object InternalServerError extends TrustResponse

  final case class UnableToMaintain() extends Exception with TrustResponse

  implicit lazy val httpReads: HttpReads[TrustResponse] =
    new HttpReads[TrustResponse] {
      override def read(method: String, url: String, response: HttpResponse): TrustResponse = {
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
