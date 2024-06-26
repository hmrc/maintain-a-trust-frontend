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

package models

import play.api.libs.json.{Format, Json}

import java.time.LocalDate

case class TrustDetails(startDate: LocalDate, trustTaxable: Option[Boolean], expressTrust: Option[Boolean], schedule3aExempt: Option[Boolean]) {

  def is5mld: Boolean = expressTrust.isDefined

  def isTaxable: Boolean = !trustTaxable.contains(false)

  def hasSchedule3aExemptAnswer: Boolean = schedule3aExempt.isDefined

  def isExpress: Boolean = (is5mld && expressTrust.get)

}

object TrustDetails {

  implicit val formats: Format[TrustDetails] = Json.format[TrustDetails]

}
