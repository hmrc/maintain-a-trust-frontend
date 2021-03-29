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

package utils.print.sections.settlors

import models.UserAnswers
import models.pages.IndividualOrBusiness
import pages.settlors.living_settlor.SettlorIndividualOrBusinessPage
import play.api.i18n.Messages
import viewmodels.AnswerSection

import javax.inject.Inject

class AllSettlorsPrinter @Inject()(deceasedSettlorPrinter: DeceasedSettlorPrinter,
                                   individualSettlorPrinter: SettlorIndividualPrinter,
                                   companySettlorPrinter: SettlorCompanyPrinter) {

  def allSettlors(userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] = {

    lazy val deceasedSettlors: Seq[AnswerSection] = deceasedSettlorPrinter.print(userAnswers) match {
      case Nil => Nil
      case x => AnswerSection(sectionKey = Some("answerPage.section.deceasedSettlor.heading")) +: x
    }

    lazy val livingSettlors: Seq[AnswerSection] = {

      def livingSettlor(index: Int): Seq[AnswerSection] = {
        userAnswers.get(SettlorIndividualOrBusinessPage(index)).flatMap { individualOrBusiness =>
          individualOrBusiness match {
            case IndividualOrBusiness.Individual => individualSettlorPrinter.print(index, userAnswers)
            case IndividualOrBusiness.Business => companySettlorPrinter.print(index, userAnswers)
          }
        }.getOrElse(Nil)
      }

      val size = userAnswers.get(_root_.sections.settlors.LivingSettlors).map(_.value.size).getOrElse(0)
      size match {
        case 0 => Nil
        case _ =>
          AnswerSection(sectionKey = Some("answerPage.section.settlors.heading")) +:
            (for (index <- 0 to size) yield livingSettlor(index)).flatten
      }
    }

    deceasedSettlors ++ livingSettlors
  }

}
