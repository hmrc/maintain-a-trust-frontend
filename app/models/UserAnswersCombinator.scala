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

import cats.kernel.Semigroup
import play.api.libs.json._

import scala.util.{Success, Try}

object UserAnswersCombinator {

  implicit val userAnswersSemigroup: Semigroup[UserAnswers] = (x: UserAnswers, y: UserAnswers) => {
    UserAnswers(
      data = x.data.deepMerge(y.data),
      internalId = x.internalId,
      identifier = x.identifier
    )
  }

  implicit class Combinator(answers: List[UserAnswers]) {

    def combine: Option[UserAnswers] = {
      Semigroup[UserAnswers].combineAllOption(answers)
    }

    def combineArraysWithPath(path: JsPath): Option[UserAnswers] = {

      val combinedArray = answers.foldLeft(JsArray())((acc, ua) => {
        ua.data.transform(path.json.pick[JsArray]) match {
          case JsSuccess(array, _) => acc ++ array
          case _ => acc
        }
      })

      def createJsObject(path: JsPath, value: JsValue) = {
        val initial = Json.obj()
        initial.transform(path.json.put(value)) match {
          case JsSuccess(value, _) => value
          case _ => initial
        }
      }

      answers.map { ua =>
        if (combinedArray.value.isEmpty) {
          ua
        } else {
          ua.copy(data = createJsObject(path, combinedArray))
        }
      }.combine
    }

  }

  implicit class UserAnswersCollector(answers: List[Try[UserAnswers]]) {

    def collectAnswers: List[UserAnswers] = answers.collect {
      case Success(answer) => answer
    }

  }

}
