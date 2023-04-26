/*
 * Copyright 2023 HM Revenue & Customs
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


import models.errors.{ServerError, TrustErrors}
import play.api.Logging

trait ConnectorErrorResponseHandler extends Logging  {

  val className: String

  def handleError(statusCode: Int, methodName: String): TrustErrors = {
    logger.error(s"[$className][$methodName] Error with status: $statusCode")
    ServerError()
  }

  def handleError(ex: Throwable, methodName: String): TrustErrors = {
    logger.error(s"[$className][$methodName] Exception thrown with message ${ex.getMessage}")
    ServerError()
  }

}
