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

package models

import models.MigrationStatus.NeedsUpdating

sealed trait MigrationStatus {
  def upToDate: Boolean = this != NeedsUpdating
}

object MigrationStatus extends Enumerable.Implicits {

  case object NothingToUpdate extends WithName("nothing-to-update") with MigrationStatus
  case object NeedsUpdating extends WithName("needs-updating") with MigrationStatus
  case object Updated extends WithName("updated") with MigrationStatus

  val values: List[MigrationStatus] = List(
    NothingToUpdate, NeedsUpdating, Updated
  )

  implicit val enumerable: Enumerable[MigrationStatus] = Enumerable(values.map(v => v.toString -> v): _*)

}
