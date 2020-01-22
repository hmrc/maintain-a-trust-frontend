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

import play.api.libs.json._

case class GetTrust(matchData: MatchData)

object GetTrust {
  implicit val writes: Writes[GetTrust] = Json.writes[GetTrust]
  implicit val reads: Reads[GetTrust] = Json.reads[GetTrust]
}

case class MatchData(utr: String)

object MatchData {
  implicit val matchDataFormat: Format[MatchData] = Json.format[MatchData]
}

case class GetTrustDesResponse(getTrust: Option[GetTrust],
                               responseHeader: ResponseHeader)

object GetTrustDesResponse {
  implicit val writes: Writes[GetTrustDesResponse] = Json.writes[GetTrustDesResponse]
  implicit val reads: Reads[GetTrustDesResponse] = Json.reads[GetTrustDesResponse]
}

case class ResponseHeader(status: String,
                          formBundleNo: String)

object ResponseHeader {
  implicit val writes: Writes[ResponseHeader] = Json.writes[ResponseHeader]
  implicit val reads: Reads[ResponseHeader] = Json.reads[ResponseHeader]
}