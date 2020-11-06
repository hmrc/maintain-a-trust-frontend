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

import java.time.LocalDate

import com.google.inject.{ImplementedBy, Inject}
import connectors.TrustConnector
import mapping.PlaybackImplicits._
import models.http.{AgentDetails, DeclarationResponse}
import models.{Address, AgentDeclaration, FullName, IndividualDeclaration}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class DeclarationServiceImpl @Inject()(connector: TrustConnector) extends DeclarationService {

  override def agentDeclaration(utr: String,
                                declaration: AgentDeclaration,
                                arn: String,
                                agencyAddress: Address,
                                agentFriendlyName: String,
                                endDate: Option[LocalDate]
                               )(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse] = {

    val agentDetails = AgentDetails(
      arn,
      agentFriendlyName,
      agencyAddress.convert,
      declaration.telephoneNumber,
      declaration.crn
    )

    declare(declaration.name, utr, Some(agentDetails), endDate)
  }

  override def individualDeclaration(utr: String,
                                     declaration: IndividualDeclaration,
                                     endDate: Option[LocalDate]
                                    )(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse] = {

    declare(declaration.name, utr, None, endDate)
  }

  private def declare(name: FullName, utr: String,
                      agentDetails: Option[AgentDetails],
                      endDate: Option[LocalDate]
                     )(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse] = {

    val payload = getPayload(name, agentDetails, endDate)
    connector.declare(utr, payload)
  }

  private def getPayload(name: FullName,
                         agentDetails: Option[AgentDetails],
                         endDate: Option[LocalDate]): JsValue = {
    Json.toJson(
      models.http.DeclarationForApi(
        models.http.Declaration(name),
        agentDetails,
        endDate
      )
    )
  }
}

@ImplementedBy(classOf[DeclarationServiceImpl])
trait DeclarationService {
  def agentDeclaration(utr: String,
                       declaration: AgentDeclaration,
                       arn: String,
                       agencyAddress: Address,
                       agentFriendlyName: String,
                       endDate: Option[LocalDate]
                      )(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse]

  def individualDeclaration(utr: String,
                            declaration: IndividualDeclaration,
                            endDate: Option[LocalDate]
                           )(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse]
}