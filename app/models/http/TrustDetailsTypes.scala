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

import models.pages.{DeedOfVariation, TypeOfTrust}
import play.api.libs.json._

import java.time.LocalDate

case class TrustDetailsType(startDate: LocalDate,
                            lawCountry: Option[String],
                            administrationCountry: Option[String],
                            residentialStatus: Option[ResidentialStatusType],
                            typeOfTrust: Option[TypeOfTrust],
                            deedOfVariation: Option[DeedOfVariation],
                            interVivos: Option[Boolean],
                            efrbsStartDate: Option[LocalDate],
                            trustTaxable: Option[Boolean],
                            expressTrust: Option[Boolean],
                            trustUKResident: Option[Boolean],
                            trustUKProperty: Option[Boolean],
                            trustRecorded: Option[Boolean],
                            trustUKRelation: Option[Boolean],
                            settlorsUkBased: Option[Boolean] = None,
                            schedule3aExempt: Option[Boolean] = None) {

  def isTaxable: Boolean = !trustTaxable.contains(false)
}

object TrustDetailsType {
  implicit val trustDetailsTypeFormat: Format[TrustDetailsType] = Json.format[TrustDetailsType]
}

case class ResidentialStatusType(uk: Option[UkType],
                                 nonUK: Option[NonUKType])

object ResidentialStatusType {
  implicit val residentialStatusTypeFormat: Format[ResidentialStatusType] = Json.format[ResidentialStatusType]
}

case class UkType(scottishLaw: Boolean,
                  preOffShore: Option[String])

object UkType {
  implicit val ukTypeFormat: Format[UkType] = Json.format[UkType]
}

case class NonUKType(sch5atcgga92: Boolean,
                     s218ihta84: Option[Boolean],
                     agentS218IHTA84: Option[Boolean],
                     trusteeStatus: Option[String])

object NonUKType {
  implicit val nonUKTypeFormat: Format[NonUKType] = Json.format[NonUKType]
}
