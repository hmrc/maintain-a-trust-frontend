/*
 * Copyright 2025 HM Revenue & Customs
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

import models.FullName
import play.api.libs.json._

import java.time.LocalDate

case class GetTrust(matchData: MatchData,
                    correspondence: Correspondence,
                    declaration: Declaration,
                    trust: DisplayTrust)

object GetTrust {
  implicit val format: Format[GetTrust] = Json.format[GetTrust]
}

case class MatchData(utr: Option[String], urn: Option[String])

object MatchData {
  implicit val format: Format[MatchData] = Json.format[MatchData]
}

case class Correspondence(abroadIndicator: Boolean,
                          name: String,
                          address: AddressType,
                          bpMatchStatus: Option[String],
                          phoneNumber: String)

object Correspondence {
  implicit val format: Format[Correspondence] = Json.format[Correspondence]
}

case class Declaration(name: FullName)

object Declaration {
  implicit val format: Format[Declaration] = Json.format[Declaration]
}

case class DeclarationForApi(declaration: Declaration,
                             agentDetails: Option[AgentDetails],
                             endDate: Option[LocalDate])

object DeclarationForApi {
  implicit val format: Format[DeclarationForApi] = Json.format[DeclarationForApi]
}

case class GetTrustDesResponse(getTrust: Option[GetTrust],
                               responseHeader: ResponseHeader)

object GetTrustDesResponse {
  implicit val format: Format[GetTrustDesResponse] = Json.format[GetTrustDesResponse]
}

case class ResponseHeader(status: String,
                          formBundleNo: String)

object ResponseHeader {
  implicit val format: Format[ResponseHeader] = Json.format[ResponseHeader]
}

case class DisplayTrust(details: TrustDetailsType,
                        entities: DisplayTrustEntitiesType,
                        assets: Option[DisplayTrustAssets])

object DisplayTrust {
  implicit val format: Format[DisplayTrust] = Json.format[DisplayTrust]
}

case class DisplayTrustEntitiesType(naturalPerson: Option[List[NaturalPersonType]],
                                    beneficiary: DisplayTrustBeneficiaryType,
                                    deceased: Option[DisplayTrustWillType],
                                    leadTrustee: DisplayTrustLeadTrusteeType,
                                    trustees: Option[List[DisplayTrustTrusteeType]],
                                    protectors: Option[DisplayTrustProtectorsType],
                                    settlors: Option[DisplayTrustSettlors])

object DisplayTrustEntitiesType {
  implicit val format: Format[DisplayTrustEntitiesType] = Json.format[DisplayTrustEntitiesType]
}
