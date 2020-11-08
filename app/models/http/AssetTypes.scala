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

import models.Constant.dateTimePattern
import models.pages.{ShareClass, ShareType}
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

sealed trait Asset

case class DisplayTrustAssets(monetary: List[AssetMonetaryAmount],
                              propertyOrLand: List[PropertyLandType],
                              shares: List[DisplaySharesType],
                              business: List[DisplayBusinessAssetType],
                              partnerShip: List[DisplayTrustPartnershipType],
                              other: List[DisplayOtherAssetType])

object DisplayTrustAssets {

  implicit val assetReads : Reads[DisplayTrustAssets] = (
    (__ \ "monetary").read[List[AssetMonetaryAmount]].orElse(Reads.pure(Nil)) and
      (__ \ "propertyOrLand").read[List[PropertyLandType]].orElse(Reads.pure(Nil)) and
      (__ \ "shares").read[List[DisplaySharesType]].orElse(Reads.pure(Nil)) and
      (__ \ "business").read[List[DisplayBusinessAssetType]].orElse(Reads.pure(Nil)) and
      (__ \ "partnerShip").read[List[DisplayTrustPartnershipType]].orElse(Reads.pure(Nil)) and
      (__ \ "other").read[List[DisplayOtherAssetType]].orElse(Reads.pure(Nil))

    )(DisplayTrustAssets.apply _)

  implicit val assetWrites : Writes[DisplayTrustAssets] = Json.writes[DisplayTrustAssets]

}

case class AssetMonetaryAmount(assetMonetaryAmount: Long) extends Asset

object AssetMonetaryAmount {
  implicit val assetMonetaryAmountFormat: Format[AssetMonetaryAmount] = Json.format[AssetMonetaryAmount]
}

case class PropertyLandType(buildingLandName: Option[String],
                            address: Option[AddressType],
                            valueFull: Long,
                            valuePrevious: Option[Long]) extends Asset

object PropertyLandType {
  implicit val propertyLandTypeFormat: Format[PropertyLandType] = Json.format[PropertyLandType]
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

case class DisplayTrustPartnershipType(utr: Option[String],
                                       description: String,
                                       partnershipStart: Option[DateTime]) extends Asset

object DisplayTrustPartnershipType {
  implicit val dateFormat: Format[DateTime] = Format[DateTime](JodaReads.jodaDateReads(dateTimePattern), JodaWrites.jodaDateWrites(dateTimePattern))
  implicit val partnershipTypeFormat: Format[DisplayTrustPartnershipType] = Json.format[DisplayTrustPartnershipType]
}

case class DisplayOtherAssetType(description: String,
                                 value: Option[Long]) extends Asset

object DisplayOtherAssetType {
  implicit val otherAssetTypeFormat: Format[DisplayOtherAssetType] = Json.format[DisplayOtherAssetType]
}
