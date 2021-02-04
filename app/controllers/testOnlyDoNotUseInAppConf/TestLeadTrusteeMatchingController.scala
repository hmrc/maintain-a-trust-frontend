/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.testOnlyDoNotUseInAppConf

import java.util.UUID

import config.FrontendAppConfig
import controllers.actions.Actions
import javax.inject.Inject
import play.api.Logging
import play.api.libs.json.{Format, JsObject, JsString, JsValue, Json, OWrites, Reads, __}
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext

class TestLeadTrusteeMatchingController @Inject()(actions: Actions,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  http: HttpClient,
                                                  config: FrontendAppConfig
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with Logging {

  final case class IdMatchRequest(id: String, nino: String, surname: String, forename: String, birthDate: String)

  object IdMatchRequest {

    implicit lazy val formats: Format[IdMatchRequest] = Json.format[IdMatchRequest]

  }

  def matchLeadTrustee(nino: String,
                       forename: String,
                       surname: String,
                       dob: String) = actions.auth.async {
    implicit request =>

      val url = s"${config.trustsIndividualCheck}/trusts-individual-check/individual-check"

      val payload = IdMatchRequest(UUID.randomUUID().toString, nino, surname.capitalize, forename.capitalize, dob)

      logger.info(s"[TestLeadTrusteeMatching] sending payload to trusts-individual-check service $payload")

      // Implicitly passes through the current header carrier
      http.POST[IdMatchRequest, JsValue](url, payload, Seq(
        "Content-Type" -> "application/json"
      )).map(Ok(_))
  }

}
