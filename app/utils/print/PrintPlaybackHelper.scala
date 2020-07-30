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

package utils.print

import javax.inject.Inject
import models.UserAnswers
import play.api.i18n.Messages
import utils.countryoptions.CountryOptions
import utils.print.sections.beneficiaries.AllBeneficiariesPrinter
import utils.print.sections.protectors.AllProtectorsPrinter
import utils.print.sections.settlors.AllSettlorsPrinter
import utils.print.sections.trustees.AllTrusteesPrinter
import utils.print.sections.{CloseDatePrinter, OtherIndividualsPrinter, TrustDetailsPrinter}
import viewmodels.AnswerSection

class PrintPlaybackHelper @Inject()(countryOptions: CountryOptions){

  def closeDate(userAnswers: UserAnswers)(implicit messages: Messages) : AnswerSection =
    CloseDatePrinter.print(userAnswers)

  def people(userAnswers: UserAnswers)(implicit messages: Messages) : Seq[AnswerSection] = List(
      new AllSettlorsPrinter(userAnswers, countryOptions).allSettlors,
      new AllTrusteesPrinter(userAnswers, countryOptions).allTrustees,
      new AllBeneficiariesPrinter(userAnswers, countryOptions).allBeneficiaries,
      new AllProtectorsPrinter(userAnswers, countryOptions).allProtectors,
      new OtherIndividualsPrinter(userAnswers, countryOptions).allOtherIndividuals
    ).flatten

  def trustDetails(userAnswers: UserAnswers)(implicit messages: Messages) : Seq[AnswerSection] =
    TrustDetailsPrinter.print(userAnswers, countryOptions)

}
