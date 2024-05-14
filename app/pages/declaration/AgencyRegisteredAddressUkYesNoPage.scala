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

package pages.declaration

import models.UserAnswers
import models.errors.TrustErrors
import pages.QuestionPage
import play.api.libs.json.JsPath

case object AgencyRegisteredAddressUkYesNoPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "agencyRegisteredAddressInUkYesNo"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Either[TrustErrors, UserAnswers] = {
    value match {
      case Some(false) =>
        userAnswers.remove(AgencyRegisteredAddressUkPage)
      case Some(true) =>
        userAnswers.remove(AgencyRegisteredAddressInternationalPage)
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }
}
