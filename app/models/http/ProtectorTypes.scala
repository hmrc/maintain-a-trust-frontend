/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

sealed trait Protector extends EntityType

case class DisplayTrustProtectorsType(protector: List[DisplayTrustProtector],
                                      protectorCompany: List[DisplayTrustProtectorBusiness]) {

  val count: Int = protector.size + protectorCompany.size
}

object DisplayTrustProtectorsType {

  implicit val reads: Reads[DisplayTrustProtectorsType] = (
    (__ \ "protector").readWithDefault[List[DisplayTrustProtector]](Nil) and
      (__ \ "protectorCompany").readWithDefault[List[DisplayTrustProtectorBusiness]](Nil)
    )(DisplayTrustProtectorsType.apply _)

  implicit val writes: Writes[DisplayTrustProtectorsType] = Json.writes[DisplayTrustProtectorsType]

}

case class DisplayTrustProtector(lineNo: Option[String],
                                 bpMatchStatus: Option[String],
                                 name: FullName,
                                 dateOfBirth: Option[LocalDate],
                                 countryOfResidence: Option[String],
                                 nationality: Option[String],
                                 legallyIncapable: Option[Boolean],
                                 identification: Option[DisplayTrustIdentificationType],
                                 entityStart: String) extends Protector

object DisplayTrustProtector {
  implicit val protectorFormat: Format[DisplayTrustProtector] = Json.format[DisplayTrustProtector]
}

case class DisplayTrustProtectorBusiness(lineNo: Option[String],
                                         bpMatchStatus: Option[String],
                                         name: String,
                                         countryOfResidence: Option[String],
                                         identification: Option[DisplayTrustIdentificationOrgType],
                                         entityStart: String) extends Protector

object DisplayTrustProtectorBusiness {
  implicit val protectorCompanyFormat: Format[DisplayTrustProtectorBusiness] = Json.format[DisplayTrustProtectorBusiness]
}
