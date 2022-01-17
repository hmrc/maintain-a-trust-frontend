/*
 * Copyright 2022 HM Revenue & Customs
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

import models.http.{DeclarationErrorResponse, DeclarationResponse, TVNResponse}
import models.{Address, AgentDeclaration, IndividualDeclaration}
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class FakeDeclarationService extends DeclarationService {

  override def agentDeclaration(
                                 utr: String,
                                 declaration: AgentDeclaration,
                                 arn: String,
                                 agencyAddress: Address,
                                 agentFriendlyName: String,
                                 endDate: Option[LocalDate]
                               )(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Future[DeclarationResponse] = {
    Future.successful(TVNResponse("123456"))
  }

  override def individualDeclaration(
                                      utr: String,
                                      declaration: IndividualDeclaration,
                                      endDate: Option[LocalDate]
                                    )(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Future[DeclarationResponse] = {
    Future.successful(TVNResponse("123456"))
  }

}

class FakeFailingDeclarationService extends DeclarationService {

  override def agentDeclaration(
                                 utr: String,
                                 declaration: AgentDeclaration,
                                 arn: String,
                                 agencyAddress: Address,
                                 agentFriendlyName: String,
                                 endDate: Option[LocalDate]
                               )(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Future[DeclarationResponse] = {
    Future.successful(DeclarationErrorResponse)
  }

  override def individualDeclaration(
                                      utr: String,
                                      declaration: IndividualDeclaration,
                                      endDate: Option[LocalDate]
                                    )(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Future[DeclarationResponse] = {
    Future.successful(DeclarationErrorResponse)
  }

}
