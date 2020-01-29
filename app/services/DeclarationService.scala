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
import models.{AgentDeclaration, Declaration, IndividualDeclaration}
import models.http.DeclarationResponse
import models.http.DeclarationResponse.InternalServerError
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class DeclarationServiceImpl @Inject()(connector: TrustConnector) extends DeclarationService {

  override def declareNoChange(utr: String, payload : Declaration)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse] = {
    payload match {
      case AgentDeclaration(name, crn, email) =>
        Future.successful(InternalServerError)
      case IndividualDeclaration(name, email) =>
        val payload = Json.parse(
          s"""
            |{
            | "name": {
            |   "firstName": "${name.firstName}",
            |   "lastName": "${name.lastName}"
            | },
            | "address": {
            |   "line1": "Line 1",
            |   "line2": "Line 2",
            |   "postCode": "NE981ZZ",
            |   "country": "GB"
            | }
            |}
            |""".stripMargin)

        connector.declare(utr, payload)
    }
  }
}

@ImplementedBy(classOf[DeclarationServiceImpl])
trait DeclarationService {
  def declareNoChange(utr: String, payload : Declaration)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse]
}