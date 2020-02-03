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
import models.http.DeclarationResponse.CannotDeclareError
import models.http.{AddressType, DeclarationResponse, NameType}
import models.requests.DataRequest
import models.{Address, AgentDeclaration, Declaration, IndividualDeclaration, InternationalAddress, UKAddress, UserAnswers}
import pages.declaration.AgencyRegisteredAddressUkYesNoPage
import pages.trustees.TrusteeAddressPage
import pages.{AgencyRegisteredAddressInternationalPage, AgencyRegisteredAddressUkPage}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import sections.Trustees
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class DeclarationServiceImpl @Inject()(connector: TrustConnector) extends DeclarationService {

  override def declareNoChange[A](utr: String, declaration: Declaration, request: DataRequest[A], arn: Option[String])
                              (implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse] = {

    val userAnswers = request.userAnswers

    declaration match {
      case AgentDeclaration(name, telephoneNumber, crn, _) =>

         getAgencyRegisteredAddress(userAnswers) match {
          case None =>
            Logger.error("Cannot declare as no agency address.")
            Future.successful(CannotDeclareError)
          case Some(address) =>

            val agentDetails = request.user.agentInformation.flatMap(_.agentFriendlyName) map { agentFriendlyName =>
              AgentDetails(
                arn.get,
                agentFriendlyName,
                convertToAddressType(address),
                telephoneNumber,
                crn
              )
            }

            declare(name, address, utr, agentDetails)
        }

      case IndividualDeclaration(name, _) =>
        getIndexOfLeadTrustee(userAnswers) match {
          case None =>
            Logger.error("Cannot declare as no lead trustee found.")
            Future.successful(CannotDeclareError)
          case Some(index) =>
            userAnswers.get(TrusteeAddressPage(index)) match {
              case None =>
                Logger.error("Cannot declare as no lead trustee address.")
                Future.successful(CannotDeclareError)
              case Some(address) =>
                declare(name, address, utr, None)
            }
        }
    }
  }

  private def getAgencyRegisteredAddress(userAnswers: UserAnswers): Option[Address] = {
    userAnswers.get(AgencyRegisteredAddressUkYesNoPage) flatMap {
      case true => userAnswers.get(AgencyRegisteredAddressUkPage)
      case false => userAnswers.get(AgencyRegisteredAddressInternationalPage)
    }
  }

  private def getIndexOfLeadTrustee(userAnswers: UserAnswers): Option[Int] = {
    for {
      trusteesAsJson <- userAnswers.get(Trustees)
      zipped = trusteesAsJson.value.zipWithIndex
      lead <- zipped.find(x => (x._1 \ "isThisLeadTrustee").as[Boolean])
    } yield lead._2
  }

  private def declare(name: NameType, address: Address, utr: String, agentDetails: Option[AgentDetails])
                     (implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse] = {

    val payload = getPayload(name, convertToAddressType(address), agentDetails)
    connector.declare(utr, payload)
  }

  private def getPayload(name: NameType, address: AddressType, agentDetails: Option[AgentDetails]): JsValue = {
    Json.toJson(
      models.http.DeclarationForApi(
        models.http.Declaration(name, address),
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
  def declareNoChange[A](utr: String, declaration: Declaration, request: DataRequest[A], arn: Option[String])
                     (implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse]
}