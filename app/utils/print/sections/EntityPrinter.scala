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

package utils.print.sections

import models.UserAnswers
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, Reads}
import viewmodels.{AnswerRow, AnswerSection}

trait EntityPrinter[A] {

  def printAnswerRows(index: Int, userAnswers: UserAnswers)
                     (implicit messages: Messages, rds: Reads[A]): Option[AnswerSection] = {

    userAnswers.getAtPath[A](namePath(index))
      .map(_.toString)
      .orElse(if (optionalName) Some("") else None)
      .map { name =>
        AnswerSection(
          headingKey = subHeadingKey.fold[Option[String]](None)(x =>
            Some(messages(s"answerPage.section.$x.subheading", index + 1))
          ),
          rows = answerRows(index, userAnswers, name).flatten
        )
      }
  }

  def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                (implicit messages: Messages): Seq[Option[AnswerRow]]

  def namePath(index: Int): JsPath

  val optionalName: Boolean = false

  val subHeadingKey: Option[String]

}
