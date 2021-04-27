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

package utils.print.sections.trustees

import models.UserAnswers
import models.pages.IndividualOrBusiness._
import pages.QuestionPage
import pages.trustees.{IsThisLeadTrusteePage, TrusteeIndividualOrBusinessPage}
import play.api.i18n.Messages
import play.api.libs.json.JsArray
import sections.Trustees
import utils.print.sections.EntitiesPrinter
import utils.print.sections.trustees.leadtrustee.{LeadTrusteeBusinessPrinter, LeadTrusteeIndividualPrinter}
import utils.print.sections.trustees.trustee.{TrusteeIndividualPrinter, TrusteeOrganisationPrinter}
import viewmodels.AnswerSection

import javax.inject.Inject

class AllTrusteesPrinter @Inject()(leadTrusteeIndividualPrinter: LeadTrusteeIndividualPrinter,
                                   leadTrusteeBusinessPrinter: LeadTrusteeBusinessPrinter,
                                   trusteeIndividualPrinter: TrusteeIndividualPrinter,
                                   trusteeOrganisationPrinter: TrusteeOrganisationPrinter) extends EntitiesPrinter[JsArray] {

  override def printSection(index: Int, userAnswers: UserAnswers)
                           (implicit messages: Messages): Option[AnswerSection] = {
    (for {
      isLeadTrustee <- userAnswers.get(IsThisLeadTrusteePage(index))
      individualOrBusiness <- userAnswers.get(TrusteeIndividualOrBusinessPage(index))
    } yield {
      (isLeadTrustee, individualOrBusiness) match {
        case (true, Individual) => leadTrusteeIndividualPrinter.printAnswerRows(index, userAnswers)
        case (true, Business) => leadTrusteeBusinessPrinter.printAnswerRows(index, userAnswers)
        case (false, Individual) => trusteeIndividualPrinter.printAnswerRows(index, userAnswers)
        case (false, Business) => trusteeOrganisationPrinter.printAnswerRows(index, userAnswers)
      }
    }).flatten
  }

  override val section: QuestionPage[JsArray] = Trustees

  override def headingKey(migratingFromNonTaxableToTaxable: Boolean): Option[String] = Some("trustees")

}
