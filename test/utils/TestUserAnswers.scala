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

package utils

import models.UserAnswers
import org.scalatest.TryValues

object TestUserAnswers extends TryValues {

  lazy val draftId = "id"
  lazy val userInternalId = "internalId"
  lazy val utr = "1234567890"
  lazy val urn = "XATRUST12345678"
  lazy val sessionId = "sessionId"

  def emptyUserAnswersForUtr: UserAnswers = UserAnswers(userInternalId, utr, sessionId, s"$userInternalId-$utr-$sessionId")
  def emptyUserAnswersForUrn: UserAnswers = UserAnswers(userInternalId, urn, sessionId, s"$userInternalId-$urn-$sessionId",
                                             isUnderlyingData5mld = true, isUnderlyingDataTaxable = false)
}
