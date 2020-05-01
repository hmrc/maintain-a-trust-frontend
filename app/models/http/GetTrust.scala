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

import mapping.{AgentDetails, AssetMonetaryAmount, PassportType, PropertyLandType, TrustDetailsType}
import models.Constant._
import models.pages.{KindOfBusiness, RoleInCompany, ShareClass, ShareType}
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
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

case class Declaration(name: NameType)

object Declaration {
  implicit val declarationFormat: Format[Declaration] = Json.format[Declaration]
}

case class DeclarationForApi(declaration: Declaration,
                             agentDetails: Option[AgentDetails])

object DeclarationForApi {
  implicit val declarationForApiFormat: Format[DeclarationForApi] = Json.format[DeclarationForApi]
}

case class AddressType(line1: String,
                       line2: String,
                       line3: Option[String],
                       line4: Option[String],
                       postCode: Option[String],
                       country: String)

object AddressType {
  implicit val addressTypeFormat: Format[AddressType] = Json.format[AddressType]
}

case class NameType(firstName: String,
                    middleName: Option[String],
                    lastName: String)

object NameType {
  implicit val nameTypeFormat: Format[NameType] = Json.format[NameType]
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

case class DisplayTrust(
                         details: TrustDetailsType,
                         entities: DisplayTrustEntitiesType,
                         assets: DisplayTrustAssets)

object DisplayTrust {
  implicit val trustFormat: Format[DisplayTrust] = Json.format[DisplayTrust]
}



case class DisplayTrustEntitiesType(naturalPerson: Option[List[DisplayTrustNaturalPersonType]],
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

case class DisplayTrustNaturalPersonType(lineNo: String,
                                         bpMatchStatus: Option[String],
                                         name: NameType,
                                         dateOfBirth: Option[DateTime],
                                         identification: Option[DisplayTrustIdentificationType],
                                         entityStart: String)

object DisplayTrustNaturalPersonType {
  implicit val dateFormat: Format[DateTime] = Format[DateTime](JodaReads.jodaDateReads(dateTimePattern), JodaWrites.jodaDateWrites(dateTimePattern))
  implicit val naturalPersonTypeFormat: Format[DisplayTrustNaturalPersonType] = Json.format[DisplayTrustNaturalPersonType]
}

case class DisplayTrustLeadTrusteeIndType(
                                           lineNo: Option[String],
                                           bpMatchStatus: Option[String],
                                           name: NameType,
                                           dateOfBirth: DateTime,
                                           phoneNumber: String,
                                           email: Option[String] = None,
                                           identification: DisplayTrustIdentificationType,
                                           entityStart: String
                                         ) extends Trustees

object DisplayTrustLeadTrusteeIndType {

  implicit val dateFormat: Format[DateTime] = Format[DateTime](JodaReads.jodaDateReads(dateTimePattern), JodaWrites.jodaDateWrites(dateTimePattern))
  implicit val leadTrusteeIndTypeFormat: Format[DisplayTrustLeadTrusteeIndType] = Json.format[DisplayTrustLeadTrusteeIndType]

}

case class DisplayTrustLeadTrusteeOrgType(
                                           lineNo: Option[String],
                                           bpMatchStatus: Option[String],
                                           name: String,
                                           phoneNumber: String,
                                           email: Option[String] = None,
                                           identification: DisplayTrustIdentificationOrgType,
                                           entityStart: String
                                         ) extends Trustees

object DisplayTrustLeadTrusteeOrgType {
  implicit val leadTrusteeOrgTypeFormat: Format[DisplayTrustLeadTrusteeOrgType] = Json.format[DisplayTrustLeadTrusteeOrgType]
}

case class DisplayTrustLeadTrusteeType(
                                        leadTrusteeInd: Option[DisplayTrustLeadTrusteeIndType] = None,
                                        leadTrusteeOrg: Option[DisplayTrustLeadTrusteeOrgType] = None
                                      )

object DisplayTrustLeadTrusteeType {

  implicit val dateFormat: Format[DateTime] = Format[DateTime](JodaReads.jodaDateReads(dateTimePattern), JodaWrites.jodaDateWrites(dateTimePattern))

  implicit val writes: Writes[DisplayTrustLeadTrusteeType] = Json.writes[DisplayTrustLeadTrusteeType]

  implicit val reads : Reads[DisplayTrustLeadTrusteeType] = Json.reads[DisplayTrustLeadTrusteeType]
}

case class DisplayTrustBeneficiaryType(individualDetails: Option[List[DisplayTrustIndividualDetailsType]],
                                       company: Option[List[DisplayTrustCompanyType]],
                                       trust: Option[List[DisplayTrustBeneficiaryTrustType]],
                                       charity: Option[List[DisplayTrustCharityType]],
                                       unidentified: Option[List[DisplayTrustUnidentifiedType]],
                                       large: Option[List[DisplayTrustLargeType]],
                                       other: Option[List[DisplayTrustOtherType]])

object DisplayTrustBeneficiaryType {
  implicit val beneficiaryTypeFormat: Format[DisplayTrustBeneficiaryType] = Json.format[DisplayTrustBeneficiaryType]
}


case class DisplayTrustIndividualDetailsType(lineNo: Option[String],
                                             bpMatchStatus: Option[String],
                                             name: NameType,
                                             dateOfBirth: Option[DateTime],
                                             vulnerableBeneficiary: Boolean,
                                             beneficiaryType: Option[RoleInCompany],
                                             beneficiaryDiscretion: Option[Boolean],
                                             beneficiaryShareOfIncome: Option[String],
                                             identification: Option[DisplayTrustIdentificationType],
                                             entityStart: String)

object DisplayTrustIndividualDetailsType {
  implicit val dateFormat: Format[DateTime] = Format[DateTime](JodaReads.jodaDateReads(dateTimePattern), JodaWrites.jodaDateWrites(dateTimePattern))
  implicit val individualDetailsTypeFormat: Format[DisplayTrustIndividualDetailsType] = Json.format[DisplayTrustIndividualDetailsType]
}

case class DisplayTrustCompanyType(lineNo: Option[String],
                                   bpMatchStatus: Option[String], organisationName: String,
                                   beneficiaryDiscretion: Option[Boolean],
                                   beneficiaryShareOfIncome: Option[String],
                                   identification: Option[DisplayTrustIdentificationOrgType],
                                   entityStart: String)

object DisplayTrustCompanyType {
  implicit val companyTypeFormat: Format[DisplayTrustCompanyType] = Json.format[DisplayTrustCompanyType]
}

case class DisplayTrustWillType(lineNo: String,
                                bpMatchStatus: Option[String],
                                name: NameType,
                                dateOfBirth: Option[DateTime],
                                dateOfDeath: Option[DateTime],
                                identification: Option[DisplayTrustIdentificationType],
                                entityStart: String)

object DisplayTrustWillType {
  implicit val dateFormat: Format[DateTime] = Format[DateTime](JodaReads.jodaDateReads(dateTimePattern), JodaWrites.jodaDateWrites(dateTimePattern))
  implicit val willTypeFormat: Format[DisplayTrustWillType] = Json.format[DisplayTrustWillType]
}

case class DisplayTrustBeneficiaryTrustType(lineNo: Option[String],
                                            bpMatchStatus: Option[String],
                                            organisationName: String,
                                            beneficiaryDiscretion: Option[Boolean],
                                            beneficiaryShareOfIncome: Option[String],
                                            identification: Option[DisplayTrustIdentificationOrgType],
                                            entityStart: String)

object DisplayTrustBeneficiaryTrustType {
  implicit val beneficiaryTrustTypeFormat: Format[DisplayTrustBeneficiaryTrustType] = Json.format[DisplayTrustBeneficiaryTrustType]
}

case class DisplayTrustCharityType(lineNo: Option[String],
                                   bpMatchStatus: Option[String],
                                   organisationName: String,
                                   beneficiaryDiscretion: Option[Boolean],
                                   beneficiaryShareOfIncome: Option[String],
                                   identification: Option[DisplayTrustIdentificationOrgType],
                                   entityStart: String)

object DisplayTrustCharityType {
  implicit val charityTypeFormat: Format[DisplayTrustCharityType] = Json.format[DisplayTrustCharityType]
}


case class DisplayTrustUnidentifiedType(lineNo: Option[String],
                                        bpMatchStatus: Option[String],
                                        description: String,
                                        beneficiaryDiscretion: Option[Boolean],
                                        beneficiaryShareOfIncome: Option[String],
                                        entityStart: String)

object DisplayTrustUnidentifiedType {
  implicit val unidentifiedTypeFormat: Format[DisplayTrustUnidentifiedType] = Json.format[DisplayTrustUnidentifiedType]
}


case class DisplayTrustLargeType(lineNo: Option[String],
                                 bpMatchStatus: Option[String],
                                 organisationName: String,
                                 description: String,
                                 description1: Option[String],
                                 description2: Option[String],
                                 description3: Option[String],
                                 description4: Option[String],
                                 numberOfBeneficiary: String,
                                 identification: Option[DisplayTrustIdentificationOrgType],
                                 beneficiaryDiscretion: Option[Boolean],
                                 beneficiaryShareOfIncome: Option[String],
                                 entityStart: String)

object DisplayTrustLargeType {
  implicit val largeTypeFormat: Format[DisplayTrustLargeType] = Json.format[DisplayTrustLargeType]
}

case class DisplayTrustOtherType(lineNo: Option[String],
                                 bpMatchStatus: Option[String],
                                 description: String,
                                 address: Option[AddressType],
                                 beneficiaryDiscretion: Option[Boolean],
                                 beneficiaryShareOfIncome: Option[String],
                                 entityStart: String)

object DisplayTrustOtherType {
  implicit val otherTypeFormat: Format[DisplayTrustOtherType] = Json.format[DisplayTrustOtherType]
}

case class DisplayTrustTrusteeType(trusteeInd: Option[DisplayTrustTrusteeIndividualType],
                                   trusteeOrg: Option[DisplayTrustTrusteeOrgType])

object DisplayTrustTrusteeType {
  implicit val trusteeTypeFormat: Format[DisplayTrustTrusteeType] = Json.format[DisplayTrustTrusteeType]
}

sealed trait Trustees

case class DisplayTrustTrusteeOrgType(lineNo: Option[String],
                                      bpMatchStatus: Option[String],
                                      name: String,
                                      phoneNumber: Option[String] = None,
                                      email: Option[String] = None,
                                      identification: Option[DisplayTrustIdentificationOrgType],
                                      entityStart: String) extends Trustees

object DisplayTrustTrusteeOrgType {
  implicit val trusteeOrgTypeFormat: Format[DisplayTrustTrusteeOrgType] = Json.format[DisplayTrustTrusteeOrgType]
}

case class DisplayTrustTrusteeIndividualType(lineNo: Option[String],
                                             bpMatchStatus: Option[String],
                                             name: NameType,
                                             dateOfBirth: Option[DateTime],
                                             phoneNumber: Option[String],
                                             identification: Option[DisplayTrustIdentificationType],
                                             entityStart: String) extends Trustees

object DisplayTrustTrusteeIndividualType {

  implicit val dateFormat: Format[DateTime] = Format[DateTime](JodaReads.jodaDateReads(dateTimePattern), JodaWrites.jodaDateWrites(dateTimePattern))
  implicit val trusteeIndividualTypeFormat: Format[DisplayTrustTrusteeIndividualType] = Json.format[DisplayTrustTrusteeIndividualType]
}


case class DisplayTrustProtectorsType(protector: List[DisplayTrustProtector],
                                      protectorCompany: List[DisplayTrustProtectorBusiness])

sealed trait Protector

object DisplayTrustProtectorsType {

  implicit val protectorReads : Reads[DisplayTrustProtectorsType] = (
    (__ \ "protector").read[List[DisplayTrustProtector]].orElse(Reads.pure(Nil)) and
      (__ \ "protectorCompany").read[List[DisplayTrustProtectorBusiness]].orElse(Reads.pure(Nil))
    ) (DisplayTrustProtectorsType.apply _)

  implicit val protectorWrites : Writes[DisplayTrustProtectorsType] = Json.writes[DisplayTrustProtectorsType]

}

case class DisplayTrustProtector(lineNo: String,
                                 bpMatchStatus: Option[String],
                                 name: NameType,
                                 dateOfBirth: Option[DateTime],
                                 identification: Option[DisplayTrustIdentificationType],
                                 entityStart: String) extends Protector

object DisplayTrustProtector {
  implicit val dateFormat: Format[DateTime] = Format[DateTime](JodaReads.jodaDateReads(dateTimePattern), JodaWrites.jodaDateWrites(dateTimePattern))
  implicit val protectorFormat: Format[DisplayTrustProtector] = Json.format[DisplayTrustProtector]
}

case class DisplayTrustProtectorBusiness(lineNo: String,
                                         bpMatchStatus: Option[String],
                                         name: String,
                                         identification: Option[DisplayTrustIdentificationOrgType],
                                         entityStart: String) extends Protector

object DisplayTrustProtectorBusiness {
  implicit val protectorCompanyFormat: Format[DisplayTrustProtectorBusiness] = Json.format[DisplayTrustProtectorBusiness]
}


case class DisplayTrustSettlors(settlor: Option[List[DisplayTrustSettlor]],
                                settlorCompany: Option[List[DisplayTrustSettlorCompany]])

sealed trait LivingSettlor

object DisplayTrustSettlors {
  implicit val settlorsFormat: Format[DisplayTrustSettlors] = Json.format[DisplayTrustSettlors]
}

case class DisplayTrustSettlor(lineNo: Option[String],
                               bpMatchStatus: Option[String],
                               name: NameType,
                               dateOfBirth: Option[DateTime],
                               identification: Option[DisplayTrustIdentificationType],
                               entityStart: String) extends LivingSettlor

object DisplayTrustSettlor {
  implicit val dateFormat: Format[DateTime] = Format[DateTime](JodaReads.jodaDateReads(dateTimePattern), JodaWrites.jodaDateWrites(dateTimePattern))
  implicit val settlorFormat: Format[DisplayTrustSettlor] = Json.format[DisplayTrustSettlor]
}

case class DisplayTrustSettlorCompany(lineNo: Option[String],
                                      bpMatchStatus: Option[String],
                                      name: String,
                                      companyType: Option[KindOfBusiness],
                                      companyTime: Option[Boolean],
                                      identification: Option[DisplayTrustIdentificationOrgType],
                                      entityStart: String) extends LivingSettlor

object DisplayTrustSettlorCompany {
  implicit val settlorCompanyFormat: Format[DisplayTrustSettlorCompany] = Json.format[DisplayTrustSettlorCompany]
}

case class DisplayTrustIdentificationType(safeId: Option[String],
                                          nino: Option[String],
                                          passport: Option[PassportType],
                                          address: Option[AddressType])

object DisplayTrustIdentificationType {
  implicit val identificationTypeFormat: Format[DisplayTrustIdentificationType] = Json.format[DisplayTrustIdentificationType]
}

case class DisplayTrustIdentificationOrgType(safeId: Option[String],
                                             utr: Option[String],
                                             address: Option[AddressType])

object DisplayTrustIdentificationOrgType {
  implicit val trustBeneficiaryIdentificationFormat: Format[DisplayTrustIdentificationOrgType] = Json.format[DisplayTrustIdentificationOrgType]
}

case class DisplayTrustPartnershipType(utr: Option[String],
                                       description: String,
                                       partnershipStart: Option[DateTime]) extends Asset

object DisplayTrustPartnershipType {

  implicit val dateFormat: Format[DateTime] = Format[DateTime](JodaReads.jodaDateReads(dateTimePattern), JodaWrites.jodaDateWrites(dateTimePattern))
  implicit val partnershipTypeFormat: Format[DisplayTrustPartnershipType] = Json.format[DisplayTrustPartnershipType]
}

case class DisplayTrustAssets(monetary: List[AssetMonetaryAmount],
                              propertyOrLand: List[PropertyLandType],
                              shares: List[DisplaySharesType],
                              business: List[DisplayBusinessAssetType],
                              partnerShip: List[DisplayTrustPartnershipType],
                              other: List[DisplayOtherAssetType])

trait Asset

object DisplayTrustAssets {

  implicit val assetReads : Reads[DisplayTrustAssets] = (
    (__ \ "monetary").read[List[AssetMonetaryAmount]].orElse(Reads.pure(Nil)) and
      (__ \ "propertyOrLand").read[List[PropertyLandType]].orElse(Reads.pure(Nil)) and
      (__ \ "shares").read[List[DisplaySharesType]].orElse(Reads.pure(Nil)) and
      (__ \ "business").read[List[DisplayBusinessAssetType]].orElse(Reads.pure(Nil)) and
      (__ \ "partnerShip").read[List[DisplayTrustPartnershipType]].orElse(Reads.pure(Nil)) and
      (__ \ "other").read[List[DisplayOtherAssetType]].orElse(Reads.pure(Nil))

    ) (DisplayTrustAssets.apply _)

  implicit val assetWrites : Writes[DisplayTrustAssets] = Json.writes[DisplayTrustAssets]

}

case class DisplaySharesType(numberOfShares: Option[String],
                             orgName: String,
                             utr: Option[String],
                             shareClass: Option[ShareClass],
                             typeOfShare: Option[ShareType],
                             value: Option[Long]) extends Asset

object DisplaySharesType {
  implicit val sharesTypeFormat: Format[DisplaySharesType] = Json.format[DisplaySharesType]
}

case class DisplayBusinessAssetType(orgName: String,
                                    utr: Option[String],
                                    businessDescription: String,
                                    address: Option[AddressType],
                                    businessValue: Option[Long]) extends Asset

object DisplayBusinessAssetType {
  implicit val businessAssetTypeFormat: Format[DisplayBusinessAssetType] = Json.format[DisplayBusinessAssetType]
}

case class DisplayOtherAssetType(description: String,
                                 value: Option[Long]) extends Asset

object DisplayOtherAssetType {
  implicit val otherAssetTypeFormat: Format[DisplayOtherAssetType] = Json.format[DisplayOtherAssetType]
}