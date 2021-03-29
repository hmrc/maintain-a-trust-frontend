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

package utils.print.sections.protectors

import models.UserAnswers
import play.api.i18n.Messages
import viewmodels.AnswerSection

import javax.inject.Inject

class AllProtectorsPrinter @Inject()(individualProtectorPrinter: IndividualProtectorPrinter,
                                     businessProtectorPrinter: BusinessProtectorPrinter) {

  def allProtectors(userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] = {
    val protectors: Seq[AnswerSection] = Seq(
      individualProtectorPrinter.entities(userAnswers),
      businessProtectorPrinter.entities(userAnswers)
    ).flatten

    if (protectors.nonEmpty) {
      AnswerSection(sectionKey = Some(messages("answerPage.section.protectors.heading"))) +: protectors
    } else {
      Nil
    }
  }

}
