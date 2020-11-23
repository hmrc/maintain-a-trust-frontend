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

package utils.print.sections.trustees

import javax.inject.Inject
import models.UserAnswers
import models.pages.IndividualOrBusiness
import pages.trustees.{IsThisLeadTrusteePage, TrusteeIndividualOrBusinessPage}
import play.api.i18n.Messages
import utils.print.sections.AnswerRowConverter
import utils.print.sections.trustees.lead_trustee.{LeadTrusteeBusinessPrinter, LeadTrusteeIndividualPrinter}
import viewmodels.AnswerSection

class AllTrusteesPrinter @Inject()(answerRowConverter: AnswerRowConverter)
                                  (userAnswers: UserAnswers)
                                  (implicit messages: Messages) {

  def allTrustees : Seq[AnswerSection] = {

    val size = userAnswers.get(_root_.sections.Trustees).map(_.value.size).getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield trustee(index)).flatten
    }
  }

  private def trustee(index: Int): Seq[AnswerSection] = {
    userAnswers.get(IsThisLeadTrusteePage(index)) flatMap { isLeadTrustee =>
      userAnswers.get(TrusteeIndividualOrBusinessPage(index)) flatMap { individualOrBusiness =>
        if (isLeadTrustee) {
          individualOrBusiness match {
            case IndividualOrBusiness.Individual => new LeadTrusteeIndividualPrinter(answerRowConverter).print(index, userAnswers)
            case IndividualOrBusiness.Business => new LeadTrusteeBusinessPrinter(answerRowConverter).print(index, userAnswers)
          }
        } else {
          individualOrBusiness match {
            case IndividualOrBusiness.Individual => new TrusteeIndividualPrinter(answerRowConverter).print(index, userAnswers)
            case IndividualOrBusiness.Business => new TrusteeOrganisationPrinter(answerRowConverter).print(index, userAnswers)
          }
        }
      }
    }
  }.getOrElse(Nil)

}
