/*
 * Copyright 2024 HM Revenue & Customs
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
import mapping.PlaybackImplicits._
import models.http.{AgentDetails, Declaration, DeclarationForApi, TVNResponse}
import models.{Address, AgentDeclaration, FullName, IndividualDeclaration}
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HeaderCarrier
import utils.TrustEnvelope.TrustEnvelope

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class DeclarationServiceImpl @Inject()(connector: TrustConnector) extends DeclarationService {

  override def agentDeclaration(
                                 utr: String,
                                 declaration: AgentDeclaration,
                                 arn: String,
                                 agencyAddress: Address,
                                 agentFriendlyName: String,
                                 endDate: Option[LocalDate]
                               )(implicit request: RequestHeader, hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[TVNResponse] = {

    val agentDetails = AgentDetails(
      arn,
      agentFriendlyName,
      agencyAddress.convert,
      declaration.telephoneNumber,
      declaration.crn
    )

    declare(declaration.name, utr, Some(agentDetails), endDate)
  }

  override def individualDeclaration(utr: String, declaration: IndividualDeclaration, endDate: Option[LocalDate])
                                    (implicit request: RequestHeader, hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[TVNResponse] = {

    declare(declaration.name, utr, None, endDate)
  }

  private def declare(name: FullName, utr: String, agentDetails: Option[AgentDetails], endDate: Option[LocalDate])
                     (implicit request: RequestHeader, hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[TVNResponse] = {

    val payload = DeclarationForApi(
      declaration = Declaration(name),
      agentDetails = agentDetails,
      endDate = endDate
    )

    connector.declare(utr, payload)
  }
}

@ImplementedBy(classOf[DeclarationServiceImpl])
trait DeclarationService {

  def agentDeclaration(
                        utr: String,
                        declaration: AgentDeclaration,
                        arn: String,
                        agencyAddress: Address,
                        agentFriendlyName: String,
                        endDate: Option[LocalDate]
                      )(implicit request: RequestHeader, hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[TVNResponse]

  def individualDeclaration(utr: String, declaration: IndividualDeclaration, endDate: Option[LocalDate])
                           (implicit request: RequestHeader, hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[TVNResponse]

}
