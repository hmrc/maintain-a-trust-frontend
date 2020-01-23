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

package pages.settlors

import models.UserAnswers
import pages._
import pages.entitystatus.TrustDetailsStatus
import pages.trustdetails._
import play.api.libs.json.JsPath
import sections.TrustDetails

import scala.util.Try

case object SettlorsBasedInTheUKPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ TrustDetails \ toString

  override def toString: String = "settlorsBasedInTheUK"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    value match {
      case Some(false) =>
        userAnswers.remove(EstablishedUnderScotsLawPage)
          .flatMap(_.remove(TrustResidentOffshorePage))
          .flatMap(_.remove(TrustPreviouslyResidentPage))
          .flatMap(_.remove(TrustDetailsStatus))
      case Some(true) =>
        userAnswers.remove(RegisteringTrustFor5APage)
          .flatMap(_.remove(InheritanceTaxActPage))
          .flatMap(_.remove(NonResidentTypePage))
          .flatMap(_.remove(AgentOtherThanBarristerPage))
          .flatMap(_.remove(TrustDetailsStatus))
      case _ => super.cleanup(value, userAnswers)

    }
  }
}
