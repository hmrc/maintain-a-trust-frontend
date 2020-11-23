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

import javax.inject.Inject
import models.UserAnswers
import pages.trustdetails._
import play.api.i18n.Messages
import viewmodels.AnswerSection

class TrustDetailsPrinter @Inject()(converter: AnswerRowConverter)
                                   (implicit messages: Messages) {

  def print(userAnswers: UserAnswers): Seq[AnswerSection] = Seq(
      AnswerSection(
        headingKey = None,
        Seq(
          converter.stringQuestion(TrustNamePage, userAnswers, "trustName"),
          converter.dateQuestion(WhenTrustSetupPage, userAnswers, "whenTrustSetup"),
          converter.utr(userAnswers, "trustUniqueTaxReference")
        ).flatten,
        sectionKey = Some(messages("answerPage.section.trustsDetails.heading"))
      )
    )

}
