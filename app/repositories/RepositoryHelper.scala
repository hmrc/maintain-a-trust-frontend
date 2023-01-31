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

package repositories

import models.errors.{MongoError, TrustErrors}
import play.api.Logging

trait RepositoryHelper extends Logging {

  def mongoRecover[T](repository: String,
                      method: String,
                      message: String,
                      sessionId: String): PartialFunction[Throwable, Either[TrustErrors, T]] = new PartialFunction[Throwable, Either[TrustErrors, T]] {

    override def isDefinedAt(x: Throwable): Boolean = x.isInstanceOf[Exception]

    override def apply(e: Throwable): Either[TrustErrors, T] = {
      logger.warn(s"[$repository][$method][SessionId: $sessionId] $message. Error: ${e.getMessage}.")
      Left(MongoError)
    }
  }

}
