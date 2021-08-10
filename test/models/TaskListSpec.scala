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

import base.SpecBase
import models.pages.Tag
import models.pages.Tag._
import viewmodels.{Link, Task}

class TaskListSpec extends SpecBase {

  private def fakeTask(status: Tag) = Task(Link("text", None), status)

  "TaskList" when {
    ".isAbleToDeclare" must {

      "return true" when {
        "empty lists" in {
          val taskList = TaskList(Nil, Nil)
          taskList.isAbleToDeclare mustBe true
        }

        "all tasks complete" in {
          val taskList = TaskList(List(fakeTask(Completed)), List(fakeTask(Completed)))
          taskList.isAbleToDeclare mustBe true
        }
      }

      "return false" when {
        "there is a task in progress" in {
          val taskList = TaskList(List(fakeTask(Completed)), List(fakeTask(InProgress)))
          taskList.isAbleToDeclare mustBe false
        }

        "there is a task that hasn't been started" in {
          val taskList = TaskList(List(fakeTask(Completed)), List(fakeTask(NotStarted)))
          taskList.isAbleToDeclare mustBe false
        }

        "there is a task that cannot be started" in {
          val taskList = TaskList(List(fakeTask(Completed)), List(fakeTask(CannotStartYet)))
          taskList.isAbleToDeclare mustBe false
        }
      }
    }
  }

}
