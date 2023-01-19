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
import play.api.libs.json._

import java.time.LocalDate

sealed trait TrusteeType extends EntityType

case class DisplayTrustLeadTrusteeType(leadTrusteeInd: Option[DisplayTrustLeadTrusteeIndType] = None,
                                       leadTrusteeOrg: Option[DisplayTrustLeadTrusteeOrgType] = None)

object DisplayTrustLeadTrusteeType {
  implicit val format: Format[DisplayTrustLeadTrusteeType] = Json.format[DisplayTrustLeadTrusteeType]
}

case class DisplayTrustLeadTrusteeIndType(lineNo: Option[String],
                                          bpMatchStatus: Option[String],
                                          name: FullName,
                                          dateOfBirth: LocalDate,
                                          nationality: Option[String],
                                          countryOfResidence: Option[String],
                                          legallyIncapable: Option[Boolean],
                                          phoneNumber: String,
                                          email: Option[String] = None,
                                          identification: DisplayTrustIdentificationType,
                                          entityStart: String) extends TrusteeType

object DisplayTrustLeadTrusteeIndType {
  implicit val leadTrusteeIndTypeFormat: Format[DisplayTrustLeadTrusteeIndType] = Json.format[DisplayTrustLeadTrusteeIndType]
}

case class DisplayTrustLeadTrusteeOrgType(lineNo: Option[String],
                                          bpMatchStatus: Option[String],
                                          name: String,
                                          countryOfResidence: Option[String],
                                          phoneNumber: String,
                                          email: Option[String] = None,
                                          identification: DisplayTrustIdentificationOrgType,
                                          entityStart: String) extends TrusteeType

object DisplayTrustLeadTrusteeOrgType {
  implicit val leadTrusteeOrgTypeFormat: Format[DisplayTrustLeadTrusteeOrgType] = Json.format[DisplayTrustLeadTrusteeOrgType]
}

case class DisplayTrustTrusteeType(trusteeInd: Option[DisplayTrustTrusteeIndividualType],
                                   trusteeOrg: Option[DisplayTrustTrusteeOrgType])

object DisplayTrustTrusteeType {
  implicit val trusteeTypeFormat: Format[DisplayTrustTrusteeType] = Json.format[DisplayTrustTrusteeType]
}

case class DisplayTrustTrusteeIndividualType(lineNo: Option[String],
                                             bpMatchStatus: Option[String],
                                             name: FullName,
                                             dateOfBirth: Option[LocalDate],
                                             nationality: Option[String],
                                             countryOfResidence: Option[String],
                                             legallyIncapable: Option[Boolean],
                                             phoneNumber: Option[String],
                                             identification: Option[DisplayTrustIdentificationType],
                                             entityStart: String) extends TrusteeType

object DisplayTrustTrusteeIndividualType {
  implicit val trusteeIndividualTypeFormat: Format[DisplayTrustTrusteeIndividualType] = Json.format[DisplayTrustTrusteeIndividualType]
}

case class DisplayTrustTrusteeOrgType(lineNo: Option[String],
                                      bpMatchStatus: Option[String],
                                      name: String,
                                      countryOfResidence: Option[String],
                                      phoneNumber: Option[String] = None,
                                      email: Option[String] = None,
                                      identification: Option[DisplayTrustIdentificationOrgType],
                                      entityStart: String) extends TrusteeType

object DisplayTrustTrusteeOrgType {
  implicit val trusteeOrgTypeFormat: Format[DisplayTrustTrusteeOrgType] = Json.format[DisplayTrustTrusteeOrgType]
}
