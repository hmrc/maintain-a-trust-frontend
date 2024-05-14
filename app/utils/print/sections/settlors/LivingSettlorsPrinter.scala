/*
 * Copyright 2024 HM Revenue & Customs
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
import models.pages.IndividualOrBusiness._
import pages.QuestionPage
import pages.settlors.living_settlor.SettlorIndividualOrBusinessPage
import play.api.i18n.Messages
import play.api.libs.json.JsArray
import sections.settlors.LivingSettlors
import utils.print.sections.EntitiesPrinter
import viewmodels.AnswerSection

import javax.inject.Inject

class LivingSettlorsPrinter @Inject()(individualSettlorPrinter: SettlorIndividualPrinter,
                                      companySettlorPrinter: SettlorCompanyPrinter) extends EntitiesPrinter[JsArray] {

  override def printSection(index: Int, userAnswers: UserAnswers)
                           (implicit messages: Messages): Option[AnswerSection] = {
    userAnswers.get(SettlorIndividualOrBusinessPage(index)).flatMap {
      case Individual => individualSettlorPrinter.printAnswerRows(index, userAnswers)
      case Business => companySettlorPrinter.printAnswerRows(index, userAnswers)
    }
  }

  override def section: QuestionPage[JsArray] = LivingSettlors

  override def headingKey(migratingFromNonTaxableToTaxable: Boolean): Option[String] = Some("settlors")

}
