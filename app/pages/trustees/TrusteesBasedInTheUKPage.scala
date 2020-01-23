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

package pages.trustees

import models.UserAnswers
import models.pages.TrusteesBasedInTheUK
import models.pages.TrusteesBasedInTheUK._
import pages._
import pages.entitystatus.TrustDetailsStatus
import pages.settlors.SettlorsBasedInTheUKPage
import pages.trustdetails._
import play.api.libs.json.JsPath
import sections.TrustDetails

import scala.util.Try

case object TrusteesBasedInTheUKPage extends QuestionPage[TrusteesBasedInTheUK] {

  override def path: JsPath = JsPath \ TrustDetails \ toString

  override def toString: String = "trusteesBasedInTheUK"

  override def cleanup(value: Option[TrusteesBasedInTheUK], userAnswers: UserAnswers): Try[UserAnswers] = {
    value match {
      case Some(UKBasedTrustees) | Some(InternationalAndUKTrustees) =>
        userAnswers.remove(EstablishedUnderScotsLawPage)
          .flatMap(_.remove(TrustResidentOffshorePage))
          .flatMap(_.remove(TrustPreviouslyResidentPage))
          .flatMap(_.remove(TrustDetailsStatus))
          .flatMap(_.remove(SettlorsBasedInTheUKPage))
      case Some(NonUkBasedTrustees) | Some(InternationalAndUKTrustees) =>
        userAnswers.remove(RegisteringTrustFor5APage)
          .flatMap(_.remove(InheritanceTaxActPage))
          .flatMap(_.remove(NonResidentTypePage))
          .flatMap(_.remove(AgentOtherThanBarristerPage))
          .flatMap(_.remove(TrustDetailsStatus))
          .flatMap(_.remove(SettlorsBasedInTheUKPage))
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }
}
