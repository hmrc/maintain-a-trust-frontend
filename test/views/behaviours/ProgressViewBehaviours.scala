/*
 * Copyright 2022 HM Revenue & Customs
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

package views.behaviours

import play.twirl.api.HtmlFormat
import viewmodels.Task
import views.ViewSpecBase

trait ProgressViewBehaviours extends ViewSpecBase {

  def taskListHeading(view: HtmlFormat.Appendable): Unit = {

    "behave list a page with a task list" when {

      "rendered" must {

        "contain a heading" in {
          val doc = asDocument(view)
          assertRenderedById(doc, "task-list--heading--mandatory")
          assertRenderedById(doc, "task-list--heading--additional")
        }
      }
    }
  }

  def taskList(view: HtmlFormat.Appendable,
               expectedSections: List[Task]): Unit = {

    expectedSections.foreach {
      section =>

        s"${section.link.text}" must {

          "render a list item" in {
            val doc = asDocument(view)
            assertRenderedById(doc, s"task-list__item--${section.link.text}")
          }

          "render a link" in {
            val id = s"task-list__task-link--${section.link.text}"

            val doc = asDocument(view)
            doc.getElementById(id).hasAttr("href")
          }

          "render a tag" in {
            val doc = asDocument(view)
            assertRenderedById(doc, s"task-list__task--${section.link.text}__tag")
          }
        }
    }
  }

  def taskListWithNotActiveLink(view: HtmlFormat.Appendable,
                                expectedSections: List[Task]): Unit = {

    expectedSections.foreach {
      section =>

        s"${section.link.text}" must {

          "render a list item" in {
            val doc = asDocument(view)
            assertRenderedById(doc, s"task-list__item--${section.link.text}")
          }

          "not render as link" in {
            val id = s"task-list__task--${section.link.text}"
            val doc = asDocument(view)
            assert(!doc.getElementById(id).hasAttr("href"))
          }

          "render a tag" in {
            val doc = asDocument(view)
            assertRenderedById(doc, s"task-list__task--${section.link.text}__tag")
          }
        }
    }
  }

}
