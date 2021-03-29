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
import play.api.libs.json.{JsArray, JsPath, JsValue, Reads}
import viewmodels.{AnswerRow, AnswerSection}

trait Printer[A] {

  def printAnswerRows(index: Int, userAnswers: UserAnswers)
                     (implicit messages: Messages, rds: Reads[A]): Option[AnswerSection] = {
    userAnswers.getAtPath[A](namePath(index)).map(_.toString).map { name =>
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

  val subHeadingKey: Option[String]

}

trait AllPrinter[T <: JsValue] {

  def entities(userAnswers: UserAnswers)(implicit messages: Messages, rds: Reads[T]): Seq[AnswerSection] = {

    val answerSections: Seq[AnswerSection] = (userAnswers.get(section) match {
      case Some(array: JsArray) => array
      case Some(value: JsValue) => JsArray(Seq(value))
      case _ => JsArray()
    }).value.zipWithIndex.foldLeft[Seq[AnswerSection]](Nil)((acc, entity) => {
      printSection(entity._2, userAnswers) match {
        case Some(value) => acc :+ value
        case None => acc
      }
    })

    if (answerSections.nonEmpty) {
      headingKey match {
        case Some(x) => AnswerSection(sectionKey = Some(messages(s"answerPage.section.$x.heading"))) +: answerSections
        case None => answerSections
      }
    } else {
      Nil
    }
  }

  def printSection(index: Int, userAnswers: UserAnswers)(implicit messages: Messages): Option[AnswerSection]

  def section: QuestionPage[T]

  val headingKey: Option[String]

}
