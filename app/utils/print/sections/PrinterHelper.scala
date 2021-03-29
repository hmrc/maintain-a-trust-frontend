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

import play.api.i18n.Messages
import viewmodels.{AnswerRow, AnswerSection}

trait PrinterHelper {

  def prependHeadingToAnswerSections(answerSections: Seq[AnswerSection])
                                    (implicit messages: Messages): Seq[AnswerSection] = {
    if (answerSections.nonEmpty) {
      headingKey match {
        case Some(x) => answerSectionWithRows() +: answerSections
        case None => answerSections
      }
    } else {
      Nil
    }
  }

  def answerSectionWithRows(answerRows: Seq[Option[AnswerRow]] = Seq())
                           (implicit messages: Messages): AnswerSection = AnswerSection(
    headingKey = None,
    rows = answerRows.flatten,
    sectionKey = heading
  )

  def heading(implicit messages: Messages): Option[String] = headingKey match {
    case Some(x) => Some(messages(s"answerPage.section.$x.heading"))
    case _ => None
  }

  val headingKey: Option[String]

}
