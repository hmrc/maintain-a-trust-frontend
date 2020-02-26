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
import mapping.AgentDetails
import models.http.{AddressType, DeclarationResponse, NameType}
import models.{Address, AgentDeclaration, IndividualDeclaration, InternationalAddress, UKAddress}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class DeclarationServiceImpl @Inject()(connector: TrustConnector) extends DeclarationService {

  override def agentDeclareNoChange[A](utr: String, declaration: AgentDeclaration, arn: String, agencyAddress: Address, agentFriendlyName: String)
                              (implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse] = {

    val agentDetails = AgentDetails(
      arn,
      agentFriendlyName,
      convertToAddressType(agencyAddress),
      declaration.telephoneNumber,
      declaration.crn
    )

    declare(declaration.name, utr, Some(agentDetails))
  }

  override def individualDeclareNoChange[A](utr: String, declaration: IndividualDeclaration)
                                      (implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse] = {

    declare(declaration.name, utr, None)
  }

  private def declare(name: NameType, utr: String, agentDetails: Option[AgentDetails])
                     (implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse] = {

    val payload = getPayload(name, agentDetails)
    connector.declare(utr, payload)
  }

  private def getPayload(name: NameType, agentDetails: Option[AgentDetails]): JsValue = {
    Json.toJson(
      models.http.DeclarationForApi(
        models.http.Declaration(name),
        agentDetails
      )
    )
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
  def agentDeclareNoChange[A](utr: String, declaration: AgentDeclaration, arn: String, agencyAddress: Address, agentFriendlyName: String)
                     (implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse]

  def individualDeclareNoChange[A](utr: String, declaration: IndividualDeclaration)
                        (implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse]
}