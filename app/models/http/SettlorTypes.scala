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

package models.http

import models.FullName
import models.pages.KindOfBusiness
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

sealed trait SettlorType extends EntityType

sealed trait LivingSettlor extends SettlorType

case class DisplayTrustSettlors(settlor: List[DisplayTrustSettlor],
                                settlorCompany: List[DisplayTrustSettlorCompany])

object DisplayTrustSettlors {

  implicit val reads: Reads[DisplayTrustSettlors] = (
    (__ \ "settlor").readWithDefault[List[DisplayTrustSettlor]](Nil) and
      (__ \ "settlorCompany").readWithDefault[List[DisplayTrustSettlorCompany]](Nil)
    )(DisplayTrustSettlors.apply _)

  implicit val writes: Writes[DisplayTrustSettlors] = Json.writes[DisplayTrustSettlors]

}

case class DisplayTrustSettlor(lineNo: Option[String],
                               bpMatchStatus: Option[String],
                               name: FullName,
                               dateOfBirth: Option[LocalDate],
                               nationality: Option[String],
                               countryOfResidence: Option[String],
                               legallyIncapable: Option[Boolean],
                               identification: Option[DisplayTrustIdentificationType],
                               entityStart: String) extends LivingSettlor

object DisplayTrustSettlor {
  implicit val settlorFormat: Format[DisplayTrustSettlor] = Json.format[DisplayTrustSettlor]
}

case class DisplayTrustSettlorCompany(lineNo: Option[String],
                                      bpMatchStatus: Option[String],
                                      name: String,
                                      countryOfResidence: Option[String],
                                      companyType: Option[KindOfBusiness],
                                      companyTime: Option[Boolean],
                                      identification: Option[DisplayTrustIdentificationOrgType],
                                      entityStart: String) extends LivingSettlor

object DisplayTrustSettlorCompany {
  implicit val settlorCompanyFormat: Format[DisplayTrustSettlorCompany] = Json.format[DisplayTrustSettlorCompany]
}

case class DisplayTrustWillType(lineNo: Option[String],
                                bpMatchStatus: Option[String],
                                name: FullName,
                                dateOfBirth: Option[LocalDate],
                                dateOfDeath: Option[LocalDate],
                                nationality: Option[String],
                                countryOfResidence: Option[String],
                                identification: Option[DisplayTrustIdentificationType],
                                entityStart: String) extends SettlorType

object DisplayTrustWillType {
  implicit val willTypeFormat: Format[DisplayTrustWillType] = Json.format[DisplayTrustWillType]
}
