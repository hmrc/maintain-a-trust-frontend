/*
 * Copyright 2025 HM Revenue & Customs
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

package pages.trustdetails

import pages.QuestionPage
import play.api.libs.json.JsPath
import sections.settlors.Settlors

case object SetUpAfterSettlorDiedYesNoPage extends QuestionPage[Boolean] {

  // TODO - change to path: JsPath = JsPath \ TrustDetails \ toString but deploy out of hours
  override def path: JsPath = Settlors.path \ toString

  override def toString: String = "setUpAfterSettlorDiedYesNo"

}
