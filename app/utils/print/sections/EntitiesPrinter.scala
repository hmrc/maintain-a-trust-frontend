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
import play.api.libs.json.{JsArray, JsValue, Reads}
import viewmodels.AnswerSection

trait EntitiesPrinter[T <: JsValue] extends PrinterHelper {

  def entities(userAnswers: UserAnswers)(implicit messages: Messages, rds: Reads[T]): Seq[AnswerSection] = {

    val answerSections: Seq[AnswerSection] = (userAnswers.get(section) match {
      case Some(array: JsArray) => array
      case Some(value: JsValue) => JsArray(Seq(value))
      case _ => JsArray()
    }).value.zipWithIndex.foldLeft[Seq[AnswerSection]](Nil)((acc, entity) => {
      printSection(entity._2, userAnswers) match {
        case Some(value) => acc :+ value
        case _ => acc
      }
    })

    prependHeadingToAnswerSections(answerSections, userAnswers.isTrustTaxable)
  }

  def printSection(index: Int, userAnswers: UserAnswers)(implicit messages: Messages): Option[AnswerSection]

  def section: QuestionPage[T]

}
