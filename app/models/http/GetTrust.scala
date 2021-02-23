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

import java.time.LocalDate

import models.FullName
import play.api.libs.json._

case class GetTrust(matchData: MatchData,
                    correspondence: Correspondence,
                    declaration: Declaration,
                    trust: DisplayTrust)

object GetTrust {
  implicit val writes: Writes[GetTrust] = Json.writes[GetTrust]
  implicit val reads: Reads[GetTrust] = Json.reads[GetTrust]
}

case class MatchData(utr: String)

object MatchData {
  implicit val matchDataFormat: Format[MatchData] = Json.format[MatchData]
}

case class Correspondence(abroadIndicator: Boolean,
                          name: String,
                          address: AddressType,
                          bpMatchStatus: Option[String],
                          phoneNumber: String)

object Correspondence {
  implicit val correspondenceFormat : Format[Correspondence] = Json.format[Correspondence]
}

case class Declaration(name: FullName)

object Declaration {
  implicit val declarationFormat: Format[Declaration] = Json.format[Declaration]
}

case class DeclarationForApi(declaration: Declaration,
                             agentDetails: Option[AgentDetails],
                             endDate: Option[LocalDate])

object DeclarationForApi {
  implicit val declarationForApiFormat: Format[DeclarationForApi] = Json.format[DeclarationForApi]
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

case class DisplayTrust(details: TrustDetailsType,
                        entities: DisplayTrustEntitiesType,
                        assets: Option[DisplayTrustAssets])

object DisplayTrust {
  implicit val trustFormat: Format[DisplayTrust] = Json.format[DisplayTrust]
}

case class DisplayTrustEntitiesType(naturalPerson: Option[List[NaturalPersonType]],
                                    beneficiary: DisplayTrustBeneficiaryType,
                                    deceased: Option[DisplayTrustWillType],
                                    leadTrustee: DisplayTrustLeadTrusteeType,
                                    trustees: Option[List[DisplayTrustTrusteeType]],
                                    protectors: Option[DisplayTrustProtectorsType],
                                    settlors: Option[DisplayTrustSettlors])

object DisplayTrustEntitiesType {
  implicit val displayTrustEntitiesTypeReads : Reads[DisplayTrustEntitiesType] = Json.reads[DisplayTrustEntitiesType]
  implicit val trustEntitiesTypeWrites: Writes[DisplayTrustEntitiesType] = Json.writes[DisplayTrustEntitiesType]
}
