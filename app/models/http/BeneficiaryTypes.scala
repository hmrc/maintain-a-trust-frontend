/*
 * Copyright 2023 HM Revenue & Customs
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
import models.pages.RoleInCompany
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

sealed trait BeneficiaryType extends EntityType

sealed trait OrgBeneficiaryType extends BeneficiaryType {
  val organisationName: String
  val beneficiaryShareOfIncome: Option[String]
  val identification: Option[DisplayTrustIdentificationOrgType]
  val countryOfResidence: Option[String]
}

case class DisplayTrustBeneficiaryType(individualDetails: List[DisplayTrustIndividualDetailsType],
                                       company: List[DisplayTrustCompanyType],
                                       trust: List[DisplayTrustBeneficiaryTrustType],
                                       charity: List[DisplayTrustCharityType],
                                       unidentified: List[DisplayTrustUnidentifiedType],
                                       large: List[DisplayTrustLargeType],
                                       other: List[DisplayTrustOtherType])

object DisplayTrustBeneficiaryType {

  implicit val reads: Reads[DisplayTrustBeneficiaryType] = (
    (__ \ "individualDetails").readWithDefault[List[DisplayTrustIndividualDetailsType]](Nil) and
      (__ \ "company").readWithDefault[List[DisplayTrustCompanyType]](Nil) and
      (__ \ "trust").readWithDefault[List[DisplayTrustBeneficiaryTrustType]](Nil) and
      (__ \ "charity").readWithDefault[List[DisplayTrustCharityType]](Nil) and
      (__ \ "unidentified").readWithDefault[List[DisplayTrustUnidentifiedType]](Nil) and
      (__ \ "large").readWithDefault[List[DisplayTrustLargeType]](Nil) and
      (__ \ "other").readWithDefault[List[DisplayTrustOtherType]](Nil)
    )(DisplayTrustBeneficiaryType.apply _)

  implicit val writes: Writes[DisplayTrustBeneficiaryType] = Json.writes[DisplayTrustBeneficiaryType]

}

case class DisplayTrustIndividualDetailsType(lineNo: Option[String],
                                             bpMatchStatus: Option[String],
                                             name: FullName,
                                             dateOfBirth: Option[LocalDate],
                                             countryOfResidence: Option[String],
                                             nationality: Option[String],
                                             legallyIncapable: Option[Boolean],
                                             vulnerableBeneficiary: Option[Boolean],
                                             beneficiaryType: Option[RoleInCompany],
                                             beneficiaryDiscretion: Option[Boolean],
                                             beneficiaryShareOfIncome: Option[String],
                                             identification: Option[DisplayTrustIdentificationType],
                                             entityStart: String) extends BeneficiaryType

object DisplayTrustIndividualDetailsType {
  implicit val individualDetailsTypeFormat: Format[DisplayTrustIndividualDetailsType] = Json.format[DisplayTrustIndividualDetailsType]
}

case class DisplayTrustCompanyType(lineNo: Option[String],
                                   bpMatchStatus: Option[String],
                                   organisationName: String,
                                   beneficiaryDiscretion: Option[Boolean],
                                   beneficiaryShareOfIncome: Option[String],
                                   countryOfResidence: Option[String],
                                   identification: Option[DisplayTrustIdentificationOrgType],
                                   entityStart: String) extends OrgBeneficiaryType

object DisplayTrustCompanyType {
  implicit val companyTypeFormat: Format[DisplayTrustCompanyType] = Json.format[DisplayTrustCompanyType]
}

case class DisplayTrustBeneficiaryTrustType(lineNo: Option[String],
                                            bpMatchStatus: Option[String],
                                            organisationName: String,
                                            beneficiaryDiscretion: Option[Boolean],
                                            beneficiaryShareOfIncome: Option[String],
                                            countryOfResidence: Option[String],
                                            identification: Option[DisplayTrustIdentificationOrgType],
                                            entityStart: String) extends OrgBeneficiaryType

object DisplayTrustBeneficiaryTrustType {
  implicit val beneficiaryTrustTypeFormat: Format[DisplayTrustBeneficiaryTrustType] = Json.format[DisplayTrustBeneficiaryTrustType]
}

case class DisplayTrustCharityType(lineNo: Option[String],
                                   bpMatchStatus: Option[String],
                                   organisationName: String,
                                   beneficiaryDiscretion: Option[Boolean],
                                   beneficiaryShareOfIncome: Option[String],
                                   countryOfResidence: Option[String],
                                   identification: Option[DisplayTrustIdentificationOrgType],
                                   entityStart: String) extends OrgBeneficiaryType

object DisplayTrustCharityType {
  implicit val charityTypeFormat: Format[DisplayTrustCharityType] = Json.format[DisplayTrustCharityType]
}

case class DisplayTrustUnidentifiedType(lineNo: Option[String],
                                        bpMatchStatus: Option[String],
                                        description: String,
                                        beneficiaryDiscretion: Option[Boolean],
                                        beneficiaryShareOfIncome: Option[String],
                                        entityStart: String) extends BeneficiaryType

object DisplayTrustUnidentifiedType {
  implicit val unidentifiedTypeFormat: Format[DisplayTrustUnidentifiedType] = Json.format[DisplayTrustUnidentifiedType]
}


case class DisplayTrustLargeType(lineNo: Option[String],
                                 bpMatchStatus: Option[String],
                                 organisationName: String,
                                 countryOfResidence: Option[String],
                                 description: String,
                                 description1: Option[String],
                                 description2: Option[String],
                                 description3: Option[String],
                                 description4: Option[String],
                                 numberOfBeneficiary: String,
                                 identification: Option[DisplayTrustIdentificationOrgType],
                                 beneficiaryDiscretion: Option[Boolean],
                                 beneficiaryShareOfIncome: Option[String],
                                 entityStart: String) extends BeneficiaryType

object DisplayTrustLargeType {
  implicit val largeTypeFormat: Format[DisplayTrustLargeType] = Json.format[DisplayTrustLargeType]
}

case class DisplayTrustOtherType(lineNo: Option[String],
                                 bpMatchStatus: Option[String],
                                 description: String,
                                 countryOfResidence: Option[String],
                                 address: Option[AddressType],
                                 beneficiaryDiscretion: Option[Boolean],
                                 beneficiaryShareOfIncome: Option[String],
                                 entityStart: String) extends BeneficiaryType

object DisplayTrustOtherType {
  implicit val otherTypeFormat: Format[DisplayTrustOtherType] = Json.format[DisplayTrustOtherType]
}
