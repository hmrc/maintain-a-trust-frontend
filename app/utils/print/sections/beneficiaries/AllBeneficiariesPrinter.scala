/*
 * Copyright 2023 HM Revenue & Customs
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

package utils.print.sections.beneficiaries

import models.UserAnswers
import play.api.i18n.Messages
import utils.print.sections.PrinterHelper
import viewmodels.AnswerSection

import javax.inject.Inject

class AllBeneficiariesPrinter @Inject()(individualBeneficiaryPrinter: IndividualBeneficiaryPrinter,
                                        classOfBeneficiaryPrinter: ClassOfBeneficiaryPrinter,
                                        charityBeneficiaryPrinter: CharityBeneficiaryPrinter,
                                        companyBeneficiaryPrinter: CompanyBeneficiaryPrinter,
                                        largeBeneficiaryPrinter: LargeBeneficiaryPrinter,
                                        trustBeneficiaryPrinter: TrustBeneficiaryPrinter,
                                        otherBeneficiaryPrinter: OtherBeneficiaryPrinter) extends PrinterHelper {

  def entities(userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] = {
    val answerSections: Seq[AnswerSection] = Seq(
      individualBeneficiaryPrinter.entities(userAnswers),
      classOfBeneficiaryPrinter.entities(userAnswers),
      charityBeneficiaryPrinter.entities(userAnswers),
      companyBeneficiaryPrinter.entities(userAnswers),
      largeBeneficiaryPrinter.entities(userAnswers),
      trustBeneficiaryPrinter.entities(userAnswers),
      otherBeneficiaryPrinter.entities(userAnswers)
    ).flatten

    prependHeadingToAnswerSections(answerSections, userAnswers.isTrustMigratingFromNonTaxableToTaxable)
  }

  override def headingKey(migratingFromNonTaxableToTaxable: Boolean): Option[String] = Some("beneficiaries")
}
