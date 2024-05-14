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

package mapping

import models.UserAnswers
import models.errors.TrustErrors

trait ConditionalExtractor {

  def extractIfTaxable(answers: UserAnswers)(block: Either[TrustErrors, UserAnswers]): Either[TrustErrors, UserAnswers] = {
    if (answers.isTrustTaxable) {
      block
    } else {
      Right(answers)
    }
  }

  def extractIfTaxableOrMigratingToTaxable(answers: UserAnswers)(block: Either[TrustErrors, UserAnswers]): Either[TrustErrors, UserAnswers] = {
    if (answers.isTrustTaxableOrMigratingToTaxable) {
      block
    } else {
      Right(answers)
    }
  }

  def extractIf5mldTrustIn5mldMode(answers: UserAnswers)(block: Either[TrustErrors, UserAnswers]): Either[TrustErrors, UserAnswers] = {
    if (answers.is5mldTrustIn5mldMode) {
      block
    } else {
      Right(answers)
    }
  }
}
