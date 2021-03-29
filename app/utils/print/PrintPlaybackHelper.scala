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

package utils.print

import models.UserAnswers
import play.api.i18n.Messages
import utils.print.sections.beneficiaries.AllBeneficiariesPrinter
import utils.print.sections.otherindividuals.OtherIndividualsPrinter
import utils.print.sections.protectors.AllProtectorsPrinter
import utils.print.sections.settlors.AllSettlorsPrinter
import utils.print.sections.trustees.AllTrusteesPrinter
import utils.print.sections.{CloseDatePrinter, TrustDetailsPrinter}
import viewmodels.AnswerSection

import javax.inject.Inject

class PrintPlaybackHelper @Inject()(closeDatePrinter: CloseDatePrinter,
                                    settlorsPrinter: AllSettlorsPrinter,
                                    trusteesPrinter: AllTrusteesPrinter,
                                    beneficiariesPrinter: AllBeneficiariesPrinter,
                                    protectorsPrinter: AllProtectorsPrinter,
                                    otherIndividualsPrinter: OtherIndividualsPrinter,
                                    trustDetailsPrinter: TrustDetailsPrinter) {

  def closeDate(userAnswers: UserAnswers)(implicit messages: Messages): AnswerSection =
    closeDatePrinter.print(userAnswers)

  def entities(userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] = List(
    settlorsPrinter.allSettlors(userAnswers),
    trusteesPrinter.allTrustees(userAnswers),
    beneficiariesPrinter.allBeneficiaries(userAnswers),
    protectorsPrinter.allProtectors(userAnswers),
    otherIndividualsPrinter.allOtherIndividuals(userAnswers)
  ).flatten

  def trustDetails(userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] =
    trustDetailsPrinter.print(userAnswers)

}
