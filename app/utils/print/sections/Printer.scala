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

package utils.print.sections

import models.UserAnswers
import pages.QuestionPage
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, JsPath, Reads}
import viewmodels.{AnswerRow, AnswerSection}

trait Printer[T] {

  val section: QuestionPage[JsArray]

  val sectionKey: String

  def namePath(index: Int): JsPath

  def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                (implicit messages: Messages): Seq[Option[AnswerRow]]

  def print(index: Int, userAnswers: UserAnswers)(implicit messages: Messages, rds: Reads[T]): Option[Seq[AnswerSection]] = {
    userAnswers.getAtPath[T](namePath(index)).map(_.toString).map { name =>
      Seq(
        AnswerSection(
          headingKey = Some(messages(s"answerPage.section.$sectionKey.subheading", index + 1)),
          rows = answerRows(index, userAnswers, name).flatten
        )
      )
    }
  }

  def entities(userAnswers: UserAnswers)(implicit messages: Messages, rds: Reads[T]): Seq[AnswerSection] = {

    val size = userAnswers.get(section).map(_.value.size).getOrElse(0)

    size match {
      case 0 => Nil
      case _ => (for (index <- 0 to size) yield print(index, userAnswers).getOrElse(Nil)).flatten
    }
  }

}
