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
import models.http.DeclarationResponse.CannotDeclareError
import models.http.{AddressType, DeclarationResponse, NameType}
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

  override def declareNoChange(utr: String, declaration : Declaration, userAnswers: UserAnswers)
                              (implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse] = {

    declaration match {
      case AgentDeclaration(name, _, _, _) =>
        val address = userAnswers.get(AgencyRegisteredAddressUkYesNoPage) map {
          case true => userAnswers.get(AgencyRegisteredAddressUkPage)
          case false => userAnswers.get(AgencyRegisteredAddressInternationalPage)
        } getOrElse(None)

        declare(name, address, utr)

      case IndividualDeclaration(name, _) =>
        getIndexOfLeadTrustee(userAnswers) match {
          case None =>
            Logger.error("Cannot declare as no lead trustee found.")
            Future.successful(CannotDeclareError)
          case Some(index) =>
            declare(
              name,
              userAnswers.get(
                TrusteeAddressPage(index)
              ),
              utr
            )
        }
    }
  }

  def getIndexOfLeadTrustee(userAnswers: UserAnswers): Option[Int] = {
    for {
      trusteesAsJson <- userAnswers.get(Trustees)
      zipped = trusteesAsJson.value.zipWithIndex
      lead <- zipped.find(x => (x._1 \ "isThisLeadTrustee").as[Boolean])
    } yield lead._2
  }

  private def declare(name: NameType, address: Option[Address], utr: String)
                     (implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse] = {
    address match {
      case Some(address) =>
        val payload = getPayload(name, convertToAddressType(address))
        connector.declare(utr, payload)
      case None =>
        Future.successful(CannotDeclareError)
    }
  }

  private def getPayload(name: NameType, address: AddressType): JsValue = {
    Json.toJson(models.http.Declaration(name, address))
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
  def declareNoChange(utr: String, declaration: Declaration, userAnswers: UserAnswers)
                     (implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse]
}