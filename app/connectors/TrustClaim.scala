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

package connectors

import play.api.Logging
import play.api.http.Status.{OK, NOT_FOUND}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import play.api.libs.json.{Json, OFormat}

import scala.language.implicitConversions

case class TrustClaim(utr:String, managedByAgent: Boolean, trustLocked:Boolean)

object TrustClaim extends Logging {

  implicit val formats : OFormat[TrustClaim] = Json.format[TrustClaim]

  implicit def httpReads(utr : String): HttpReads[Option[TrustClaim]] =
    new HttpReads[Option[TrustClaim]] {
      override def read(method: String, url: String, response: HttpResponse): Option[TrustClaim] = {
        logger.info(s"[UTR: $utr] response status received from trusts store api: ${response.status}")

        response.status match {
          case OK =>
            response.json.asOpt[TrustClaim] match {
              case validClaim @ Some(c) =>
                if (c.utr.toLowerCase.trim == utr.toLowerCase.trim) {
                  if (c.trustLocked) {
                    logger.info(s"[UTR: $utr] User has been locked out of Trust IV for 30 minutes")
                  } else {
                    logger.info(s"[UTR: $utr] User has not been locked out of Trust IV")
                  }
                  validClaim
                } else {
                  logger.info(s"[UTR: $utr] There was a problem in the data from trusts-store so unable to determine if there is a claim")
                  None
                }
              case None =>
                logger.info(s"[UTR: $utr] there was a problem in the data from trusts-store so unable to determine if there is a claim")
                None
            }
          case NOT_FOUND =>
            logger.info(s"[UTR: $utr] User has not been locked out of Trust IV for 30 minutes, continuing into claiming/verifying the trust")
            None
          case _ =>
            logger.info(s"[UTR: $utr] User is unable to continue TRUST IV due to the following response - ${response.body}")
            None
        }
      }
    }


}