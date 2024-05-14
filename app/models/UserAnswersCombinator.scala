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

package models

import cats.kernel.Semigroup
import play.api.libs.json._

object UserAnswersCombinator {

  implicit class Combinator(answers: List[UserAnswers]) {

    def combine: Option[UserAnswers] = {
      implicit val userAnswersSemigroup: Semigroup[UserAnswers] = (x: UserAnswers, y: UserAnswers) => {
        x.copy(data = x.data.deepMerge(y.data))
      }
      Semigroup[UserAnswers].combineAllOption(answers)
    }

    def combineArraysWithPath(path: JsPath): Option[UserAnswers] = {
      implicit val userAnswersSemigroup: Semigroup[UserAnswers] = (x: UserAnswers, y: UserAnswers) => {
        x.copy(data = x.data.mergeArrays(y.data, path))
      }
      Semigroup[UserAnswers].combineAllOption(answers)
    }
  }

  implicit class ArrayCombinator(x: JsObject) {
    def mergeArrays(y: JsObject, path: JsPath): JsObject = {

      val pick = (obj: JsObject) => obj.transform(path.json.pick[JsArray])
      val mergedArrays: JsArray = Seq(pick(x), pick(y)).foldLeft(JsArray())((acc, pickResult) => {
        pickResult match {
          case JsSuccess(arr, _) => acc ++ arr
          case _ => acc
        }
      })

      if (mergedArrays.value.nonEmpty) {
        val replaceArray = path.json.prune andThen __.json.update(path.json.put(mergedArrays))
        x.transform(replaceArray) match {
          case JsSuccess(updated, _) => updated
          case _ => x
        }
      } else {
        x
      }
    }
  }

}
