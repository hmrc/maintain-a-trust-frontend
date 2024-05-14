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

package models.pages

import models.pages.Tag.Completed
import models.{Enumerable, WithName}

sealed trait Tag {
  def isCompleted: Boolean = this == Completed
}

object Tag extends Enumerable.Implicits {

  case object Completed extends WithName("completed") with Tag
  case object InProgress extends WithName("in-progress") with Tag
  case object NotStarted extends WithName("not-started") with Tag
  case object CannotStartYet extends WithName("cannot-start-yet") with Tag
  case object NoActionNeeded extends WithName("no-action-needed") with Tag

  val values: Set[Tag] = Set(
    Completed, InProgress, NotStarted, CannotStartYet, NoActionNeeded
  )

  implicit val enumerable: Enumerable[Tag] =
    Enumerable(values.toSeq.map(v => v.toString -> v): _*)

  def tagFor(status: Tag, featureEnabled: Boolean = true): Tag = {
    if (!featureEnabled) Completed else status
  }
}
