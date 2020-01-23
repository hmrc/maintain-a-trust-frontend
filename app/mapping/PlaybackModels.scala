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

package mapping

import java.time.LocalDate

import models.enums.{DeedOfVariation, TypeOfTrust}
import models.http.{AddressType, Asset, Correspondence, Declaration, NameType}
import play.api.libs.json._

case class Playback(matchData: Option[MatchData],
                    correspondence: Correspondence,
                    yearsReturns: Option[YearsReturns],
                    declaration: Declaration,
                    trust: Trust,
                    agentDetails: Option[AgentDetails] = None
                   )

object Playback {
 implicit val registrationReads : Format[Playback] = Json.format[Playback]
}


case class Details(trust: Trust)

object Details {
  implicit val detailsFormat: Format[Details] = Json.format[Details]
}

case class MatchData(utr: String,
                     name: String,
                     postCode: Option[String]
                    )

object MatchData {
  implicit val matchDataFormat: Format[MatchData] = Json.format[MatchData]
}

case class YearsReturns(returns: Option[List[YearReturnType]])

object YearsReturns {
  implicit val yearsReturnsFormat: Format[YearsReturns] = Json.format[YearsReturns]
}

case class Assets(monetary: Option[List[AssetMonetaryAmount]],
                  propertyOrLand: Option[List[PropertyLandType]],
                  shares: Option[List[SharesType]],
                  business: Option[List[BusinessAssetType]],
                  partnerShip: Option[List[PartnershipType]],
                  other: Option[List[OtherAssetType]])

object Assets {
  implicit val assetsFormat: Format[Assets] = Json.format[Assets]
}

case class AssetMonetaryAmount(assetMonetaryAmount: Long) extends Asset

object AssetMonetaryAmount {
  implicit val assetMonetaryAmountFormat: Format[AssetMonetaryAmount] = Json.format[AssetMonetaryAmount]
}

case class Trust(
                  details: TrustDetailsType,
                  entities: TrustEntitiesType,
                  assets: Assets)

object Trust {
  implicit val trustFormat: Format[Trust] = Json.format[Trust]
}

case class TrustEntitiesType(naturalPerson: Option[List[NaturalPersonType]],
                             beneficiary: BeneficiaryType,
                             deceased: Option[WillType],
                             leadTrustees: LeadTrusteeType,
                             trustees: Option[List[TrusteeType]],
                             protectors: Option[ProtectorsType],
                             settlors: Option[Settlors])

object TrustEntitiesType {
  implicit val trustEntitiesTypeFormat: Format[TrustEntitiesType] = Json.format[TrustEntitiesType]
}

case class ProtectorsType(protector: Option[List[Protector]],
                          protectorCompany: Option[List[ProtectorCompany]])

object ProtectorsType {
  implicit val protectorsTypeFormat: Format[ProtectorsType] = Json.format[ProtectorsType]
}

case class Protector(name: NameType,
                     dateOfBirth: Option[LocalDate],
                     identification: Option[IdentificationType])

object Protector {
  implicit val protectorFormat: Format[Protector] = Json.format[Protector]
}

case class ProtectorCompany(name: String,
                            identification: IdentificationOrgType)

object ProtectorCompany {
  implicit val protectorCompanyFormat: Format[ProtectorCompany] = Json.format[ProtectorCompany]
}


case class TrusteeType(trusteeInd : Option[TrusteeIndividualType], trusteeOrg : Option[TrusteeOrgType] )

object TrusteeType {
  implicit val trusteeTypeFormat : Format[TrusteeType] = Json.format[TrusteeType]
}

case class TrusteeOrgType(name: String,
                          phoneNumber: Option[String] = None,
                          email: Option[String] = None,
                          identification: IdentificationOrgType)

object TrusteeOrgType {
  implicit val trusteeOrgTypeFormat: Format[TrusteeOrgType] = Json.format[TrusteeOrgType]
}

case class TrusteeIndividualType(name: NameType,
                       dateOfBirth: Option[LocalDate],
                       phoneNumber: Option[String],
                       identification: Option[IdentificationType])

object TrusteeIndividualType {

  implicit val trusteeIndividualTypeFormat : Format[TrusteeIndividualType] = Json.format[TrusteeIndividualType]
}

case class Settlors(settlor: Option[List[Settlor]],
                    settlorCompany: Option[List[SettlorCompany]])

object Settlors {
  implicit val settlorsFormat: Format[Settlors] = Json.format[Settlors]
}

case class Settlor(name: NameType,
                   dateOfBirth: Option[LocalDate],
                   identification: Option[IdentificationType])

object Settlor {

  implicit val settlorFormat: Format[Settlor] = Json.format[Settlor]
}

case class SettlorCompany(name: String,
                          companyType: Option[String],
                          companyTime: Option[Boolean],
                          identification: Option[IdentificationOrgType])

object SettlorCompany {
  implicit val settlorCompanyFormat: Format[SettlorCompany] = Json.format[SettlorCompany]
}


case class LeadTrusteeIndType (
                            name: NameType,
                            dateOfBirth: LocalDate ,
                            phoneNumber: String,
                            email: Option[String] = None,
                            identification: IdentificationType
                          )

object LeadTrusteeIndType {


  implicit val leadTrusteeIndTypeFormat: Format[LeadTrusteeIndType] = Json.format[LeadTrusteeIndType]

}

case class LeadTrusteeOrgType(
                               name: String,
                               phoneNumber: String,
                               email: Option[String] = None,
                               identification: IdentificationOrgType
                             )

object LeadTrusteeOrgType {
  implicit val leadTrusteeOrgTypeFormat: Format[LeadTrusteeOrgType] = Json.format[LeadTrusteeOrgType]
}

case class LeadTrusteeType(
                            leadTrusteeInd : Option[LeadTrusteeIndType] = None,
                            leadTrusteeOrg : Option[LeadTrusteeOrgType] = None
                          )

object LeadTrusteeType {
  implicit val leadTrusteeTypeReads:Format[LeadTrusteeType] = Json.format[LeadTrusteeType]
}


case class BeneficiaryType(individualDetails: Option[List[IndividualDetailsType]],
                           company: Option[List[CompanyType]],
                           trust: Option[List[BeneficiaryTrustType]],
                           charity: Option[List[CharityType]],
                           unidentified: Option[List[UnidentifiedType]],
                           large: Option[List[LargeType]],
                           other: Option[List[OtherType]])

object BeneficiaryType {
  implicit val beneficiaryTypeFormat: Format[BeneficiaryType] = Json.format[BeneficiaryType]
}

case class IndividualDetailsType(name: NameType,
                                 dateOfBirth: Option[LocalDate],
                                 vulnerableBeneficiary: Boolean,
                                 beneficiaryType: Option[String],
                                 beneficiaryDiscretion: Boolean,
                                 beneficiaryShareOfIncome: Option[String],
                                 identification: Option[IdentificationType])

object IndividualDetailsType {

  implicit val individualDetailsTypeFormat: Format[IndividualDetailsType] = Json.format[IndividualDetailsType]
}

case class BeneficiaryTrustType(organisationName: String,
                                beneficiaryDiscretion: Option[Boolean],
                                beneficiaryShareOfIncome: Option[String],
                                identification: IdentificationOrgType)

object BeneficiaryTrustType {
  implicit val beneficiaryTrustTypeFormat: Format[BeneficiaryTrustType] = Json.format[BeneficiaryTrustType]
}

case class IdentificationOrgType(utr: Option[String],
                                          address: Option[AddressType])

object IdentificationOrgType {
  implicit val trustBeneficiaryIdentificationFormat: Format[IdentificationOrgType] = Json.format[IdentificationOrgType]
}

case class Identification(nino: Option[String],
                          address: Option[AddressType])

object Identification {
  implicit val identificationFormat: Format[Identification] = Json.format[Identification]
}

case class CharityType(organisationName: String,
                       beneficiaryDiscretion: Option[Boolean],
                       beneficiaryShareOfIncome: Option[String],
                       identification: IdentificationOrgType)

object CharityType {
  implicit val charityTypeFormat: Format[CharityType] = Json.format[CharityType]
}

case class TrustBeneficiaryCharityIdentification(utr: Option[String],
                                                 address: Option[AddressType])

object TrustBeneficiaryCharityIdentification {
  implicit val trustBeneficiaryCharityIdentificationFormat: Format[TrustBeneficiaryCharityIdentification] = Json.format[TrustBeneficiaryCharityIdentification]
}

case class UnidentifiedType(description: String,
                            beneficiaryDiscretion: Option[Boolean],
                            beneficiaryShareOfIncome: Option[String])

object UnidentifiedType {
  implicit val unidentifiedTypeFormat: Format[UnidentifiedType] = Json.format[UnidentifiedType]
}

case class LargeType(organisationName: String,
                     description: String,
                     description1: Option[String],
                     description2: Option[String],
                     description3: Option[String],
                     description4: Option[String],
                     numberOfBeneficiary: String,
                     identification: Option[IdentificationOrgType],
                     beneficiaryDiscretion: Option[Boolean],
                     beneficiaryShareOfIncome: Option[String])

object LargeType {
  implicit val largeTypeFormat: Format[LargeType] = Json.format[LargeType]
}

case class OtherType(description: String,
                     address: Option[AddressType],
                     beneficiaryDiscretion: Option[Boolean],
                     beneficiaryShareOfIncome: Option[String])

object OtherType {
  implicit val otherTypeFormat: Format[OtherType] = Json.format[OtherType]
}

case class CompanyType(organisationName: String,
                       beneficiaryDiscretion: Option[Boolean],
                       beneficiaryShareOfIncome: Option[String],
                       identification: IdentificationOrgType)

object CompanyType {
  implicit val companyTypeFormat: Format[CompanyType] = Json.format[CompanyType]
}

case class NaturalPersonType(name: NameType,
                             dateOfBirth: Option[LocalDate],
                             identification: Option[IdentificationType])

object NaturalPersonType {

  implicit val naturalPersonTypeFormat: Format[NaturalPersonType] = Json.format[NaturalPersonType]
}

case class IdentificationType(nino: Option[String],
                              passport: Option[PassportType],
                              address: Option[AddressType])

object IdentificationType {
  implicit val identificationTypeFormat: Format[IdentificationType] = Json.format[IdentificationType]
}

case class TrustDetailsType(startDate: LocalDate,
                            lawCountry: Option[String],
                            administrationCountry: Option[String],
                            residentialStatus: Option[ResidentialStatusType],
                            typeOfTrust: TypeOfTrust,
                            deedOfVariation: Option[DeedOfVariation],
                            interVivos: Option[Boolean],
                            efrbsStartDate: Option[LocalDate])

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


case class PropertyLandType(buildingLandName: Option[String],
                            address: Option[AddressType],
                            valueFull: Long,
                            valuePrevious: Long) extends Asset

object PropertyLandType {
  implicit val propertyLandTypeFormat: Format[PropertyLandType] = Json.format[PropertyLandType]
}

case class BusinessAssetType(orgName: String,
                             businessDescription: Option[String],
                             address: AddressType,
                             businessValue: Long)

object BusinessAssetType {
  implicit val businessAssetTypeFormat: Format[BusinessAssetType] = Json.format[BusinessAssetType]
}

case class OtherAssetType(description: String,
                          value: Long)

object OtherAssetType {
  implicit val otherAssetTypeFormat: Format[OtherAssetType] = Json.format[OtherAssetType]
}

case class PartnershipType(description: String,
                           partnershipStart: LocalDate)

object PartnershipType {

  implicit val partnershipTypeFormat: Format[PartnershipType] = Json.format[PartnershipType]
}

case class SharesType(numberOfShares: String,
                      orgName: String,
                      shareClass: String,
                      typeOfShare: String,
                      value: Long)

object SharesType {
  implicit val sharesTypeFormat: Format[SharesType] = Json.format[SharesType]
}

case class YearReturnType(taxReturnYear: String, taxConsequence: Boolean)

object YearReturnType {
  implicit val yearReturnTypeFormat: Format[YearReturnType] = Json.format[YearReturnType]
}

case class PassportType(number: String,
                        expirationDate: LocalDate,
                        countryOfIssue: String)

object PassportType {

  implicit val passportTypeFormat: Format[PassportType] = Json.format[PassportType]
}


case class WillType(name: NameType,
                    dateOfBirth: Option[LocalDate],
                    dateOfDeath: Option[LocalDate],
                    identification: Option[Identification])

object WillType {

  implicit val willTypeFormat: Format[WillType] = Json.format[WillType]
}

case class AgentDetails(arn: String,
                        agentName: String,
                        agentAddress: AddressType,
                        agentTelephoneNumber: String,
                        clientReference: String)

object AgentDetails {
  implicit val agentDetailsFormat: Format[AgentDetails] = Json.format[AgentDetails]
}

