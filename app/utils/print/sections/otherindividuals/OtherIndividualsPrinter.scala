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

package utils.print.sections.otherindividuals

import javax.inject.Inject
import models.UserAnswers
import play.api.i18n.Messages
import sections.Natural
import utils.print.sections.AnswerRowConverter
import viewmodels.AnswerSection

class OtherIndividualsPrinter @Inject()(answerRowConverter: AnswerRowConverter)
                                       (userAnswers: UserAnswers)
                                       (implicit messages: Messages) {

  def allOtherIndividuals : Seq[AnswerSection] = {
    val size = userAnswers
      .get(Natural)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        val heading = Seq(AnswerSection(sectionKey = Some(messages("answerPage.section.otherIndividuals.heading"))))
        val individuals = (for (index <- 0 to size) yield new OtherIndividualPrinter(answerRowConverter).print(index, userAnswers)).flatten

        heading ++ individuals
    }
  }
}
