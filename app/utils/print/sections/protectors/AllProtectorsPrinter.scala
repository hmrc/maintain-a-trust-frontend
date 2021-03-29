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
import models.pages.IndividualOrBusiness.{Business, Individual}
import pages.protectors.ProtectorIndividualOrBusinessPage
import play.api.i18n.Messages
import viewmodels.AnswerSection

import javax.inject.Inject

class AllProtectorsPrinter @Inject()(individualProtectorPrinter: IndividualProtectorPrinter,
                                     businessProtectorPrinter: BusinessProtectorPrinter) {

  def allProtectors(userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.Protectors)
      .map(_.value.size)
      .getOrElse(0)

    val protectors = size match {
      case 0 => Nil
      case _ =>

        (for (index <- 0 to size) yield {
          userAnswers.get(ProtectorIndividualOrBusinessPage(index)).map {
            case Individual =>
              individualProtectorPrinter.print(index, userAnswers)
            case Business =>
              businessProtectorPrinter.print(index, userAnswers)
          }.getOrElse(Nil)
        }).flatten
    }

    if (protectors.nonEmpty) {
      Seq(
        Seq(AnswerSection(sectionKey = Some(messages("answerPage.section.protectors.heading")))),
        protectors
      ).flatten
    } else {
      Nil
    }
  }

}
