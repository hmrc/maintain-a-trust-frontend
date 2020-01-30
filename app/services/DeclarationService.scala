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

package services

import com.google.inject.{ImplementedBy, Inject}
import connectors.TrustConnector
import models.http.{AddressType, DeclarationResponse}
import models.{Address, AgentDeclaration, Declaration, FullName, IndividualDeclaration, InternationalAddress, UKAddress}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class DeclarationServiceImpl @Inject()(connector: TrustConnector) extends DeclarationService {

  override def declareNoChange(utr: String, declaration : Declaration, ukOrInternationalAddress: Address)
                              (implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse] = {

    val address: AddressType = convertToAddressType(ukOrInternationalAddress)

    declaration match {
      case AgentDeclaration(name, _, _) =>
        val payload = getPayload(name, address)
        connector.declare(utr, payload)
      case IndividualDeclaration(name, _) =>
        val payload = getPayload(name, address)
        connector.declare(utr, payload)
    }
  }

  private def getPayload(name: FullName, address: AddressType): JsValue = {
    Json.parse(
      s"""
         |{
         | "name": {
         |   "firstName": "${name.firstName}",
         |   "middleName": "${name.middleName}",
         |   "lastName": "${name.lastName}"
         | },
         | "address": {
         |   "line1": "${address.line1}",
         |   "line2": "${address.line2}",
         |   "line3": "${address.line3}",
         |   "line4": "${address.line4}",
         |   "postCode": "${address.postCode}",
         |   "country": "${address.country}"
         | }
         |}
         |""".stripMargin)
  }

  private def convertToAddressType(address: Address): AddressType = {
    address match {
      case UKAddress(line1, line2, line3, line4, postcode) =>
        AddressType(
          line1,
          line2,
          line3,
          line4,
          postCode = Some(postcode),
          country = "GB"
        )
      case InternationalAddress(line1, line2, line3, country) =>
        AddressType(
          line1,
          line2,
          line3,
          line4 = None,
          postCode = None,
          country = country
        )
    }
  }
}

@ImplementedBy(classOf[DeclarationServiceImpl])
trait DeclarationService {
  def declareNoChange(utr: String, payload : Declaration, address: Address)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse]
}