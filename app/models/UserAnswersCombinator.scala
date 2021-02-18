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

package models

import java.time.LocalDateTime
import cats.kernel.Semigroup
import scala.util.{Success, Try}

object UserAnswersCombinator {

  implicit val userAnswersSemigroup : Semigroup[UserAnswers] = new Semigroup[UserAnswers] {

    override def combine(x: UserAnswers, y: UserAnswers): UserAnswers = {
        UserAnswers(
          data = x.data.deepMerge(y.data),
          internalId = x.internalId,
          identifier = x.identifier,
          isIdentifierUtr = x.isIdentifierUtr,
          updatedAt = LocalDateTime.now
        )
    }
  }

  implicit class Combinator(answers: List[UserAnswers]) {

    def combine: Option[UserAnswers] = {
      Semigroup[UserAnswers].combineAllOption(answers)
    }

  }

  implicit class UserAnswersCollector(answers: List[Try[UserAnswers]]) {

    def collectAnswers : List[UserAnswers] = answers.collect {
      case Success(answer) => answer
    }

  }

}
