/*
 * Copyright 2020 HM Revenue & Customs
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
import utils.countryoptions.CountryOptions
import viewmodels.AnswerSection

class OtherIndividualsPrinter(userAnswers: UserAnswers, countryOptions: CountryOptions)(implicit messages: Messages) {

  def allOtherIndividuals : Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.natural.Individual)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        val heading = Seq(AnswerSection(sectionKey = Some(messages("answerPage.section.otherIndividuals.heading"))))
        val individuals = (for (index <- 0 to size) yield OtherIndividualPrinter.print(index, userAnswers, countryOptions)).flatten

        heading ++ individuals
    }
  }
}
