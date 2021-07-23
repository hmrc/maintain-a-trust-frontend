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

import models.pages.{ShareClass, ShareType}
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

sealed trait Asset

case class DisplayTrustAssets(monetary: List[AssetMonetaryAmount],
                              propertyOrLand: List[PropertyLandType],
                              shares: List[DisplaySharesType],
                              business: List[DisplayBusinessAssetType],
                              partnerShip: List[DisplayTrustPartnershipType],
                              other: List[DisplayOtherAssetType],
                              nonEEABusiness: List[DisplayNonEEABusinessType])

object DisplayTrustAssets {

  implicit val reads : Reads[DisplayTrustAssets] = (
    (__ \ "monetary").readWithDefault[List[AssetMonetaryAmount]](Nil) and
      (__ \ "propertyOrLand").readWithDefault[List[PropertyLandType]](Nil) and
      (__ \ "shares").readWithDefault[List[DisplaySharesType]](Nil) and
      (__ \ "business").readWithDefault[List[DisplayBusinessAssetType]](Nil) and
      (__ \ "partnerShip").readWithDefault[List[DisplayTrustPartnershipType]](Nil) and
      (__ \ "other").readWithDefault[List[DisplayOtherAssetType]](Nil) and
      (__ \ "nonEEABusiness").readWithDefault[List[DisplayNonEEABusinessType]](Nil)
    )(DisplayTrustAssets.apply _)

  implicit val writes : Writes[DisplayTrustAssets] = Json.writes[DisplayTrustAssets]

}

case class AssetMonetaryAmount(assetMonetaryAmount: Long) extends Asset with EntityType {

  override val bpMatchStatus: Option[String] = None
  override val lineNo: Option[String] = None
  override val entityStart: String = LocalDate.now.toString
}

object AssetMonetaryAmount {
  implicit val assetMonetaryAmountFormat: Format[AssetMonetaryAmount] = Json.format[AssetMonetaryAmount]
}

case class PropertyLandType(buildingLandName: Option[String],
                            address: Option[AddressType],
                            valueFull: Long,
                            valuePrevious: Option[Long]) extends Asset with EntityType {

  override val bpMatchStatus: Option[String] = None
  override val lineNo: Option[String] = None
  override val entityStart: String = LocalDate.now.toString
  
}

object PropertyLandType {
  implicit val propertyLandTypeFormat: Format[PropertyLandType] = Json.format[PropertyLandType]
}

case class DisplaySharesType(numberOfShares: Option[String],
                             orgName: String,
                             utr: Option[String],
                             shareClassDisplay: Option[ShareClass],
                             typeOfShare: Option[ShareType],
                             value: Option[Long],
                             isPortfolio: Option[Boolean]) extends Asset with EntityType {

  override val bpMatchStatus: Option[String] = None
  override val lineNo: Option[String] = None
  override val entityStart: String = LocalDate.now.toString

}

object DisplaySharesType {
  implicit val sharesTypeFormat: Format[DisplaySharesType] = Json.format[DisplaySharesType]
}

case class DisplayBusinessAssetType(orgName: String,
                                    utr: Option[String],
                                    businessDescription: String,
                                    address: Option[AddressType],
                                    businessValue: Option[Long]) extends Asset with EntityType {

  override val bpMatchStatus: Option[String] = None
  override val lineNo: Option[String] = None
  override val entityStart: String = LocalDate.now.toString

}

object DisplayBusinessAssetType {
  implicit val businessAssetTypeFormat: Format[DisplayBusinessAssetType] = Json.format[DisplayBusinessAssetType]
}

case class DisplayTrustPartnershipType(utr: Option[String],
                                       description: String,
                                       partnershipStart: Option[LocalDate]) extends Asset with EntityType {

  override val bpMatchStatus: Option[String] = None
  override val lineNo: Option[String] = None
  override val entityStart: String = LocalDate.now.toString

}

object DisplayTrustPartnershipType {
  implicit val partnershipTypeFormat: Format[DisplayTrustPartnershipType] = Json.format[DisplayTrustPartnershipType]
}

case class DisplayOtherAssetType(description: String,
                                 value: Option[Long]) extends Asset with EntityType {

  override val bpMatchStatus: Option[String] = None
  override val lineNo: Option[String] = None
  override val entityStart: String = LocalDate.now.toString

}

object DisplayOtherAssetType {
  implicit val otherAssetTypeFormat: Format[DisplayOtherAssetType] = Json.format[DisplayOtherAssetType]
}

case class DisplayNonEEABusinessType(lineNo: Option[String],
                                     orgName: String,
                                     address: AddressType,
                                     govLawCountry: String,
                                     startDate: LocalDate,
                                     endDate: Option[LocalDate]) extends Asset with EntityType {

  override val bpMatchStatus: Option[String] = None
  override val entityStart: String = startDate.toString
}

object DisplayNonEEABusinessType {
  implicit val nonEeaBusinessTypeFormat: Format[DisplayNonEEABusinessType] = Json.format[DisplayNonEEABusinessType]
}
