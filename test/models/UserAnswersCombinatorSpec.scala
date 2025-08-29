/*
 * Copyright 2025 HM Revenue & Customs
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

import base.SpecBase
import models.UserAnswersCombinator._
import play.api.libs.json.{JsObject, JsPath, Json}

class UserAnswersCombinatorSpec extends SpecBase {

  "UserAnswersCombinator" when {

    val path = JsPath \ "array"

    val array: JsObject = Json.parse(
      """
        |{
        |  "array": [
        |    {
        |      "foo": "bar"
        |    }
        |  ]
        |}
        |""".stripMargin
    ).as[JsObject]

    ".combineArraysWithPath" when {

      "empty list of user answers" must {
        "return None" in {
          val result = Nil.combineArraysWithPath(path)
          result mustBe None
        }
      }

      "no arrays at path" must {
        "return first in list" in {
          val x = UserAnswers(internalId = "internalId1", identifier = "utr1", sessionId = "session1", newId = "internalId1-utr1-session1")
          val y = UserAnswers(internalId = "internalId2", identifier = "utr2", sessionId = "session2", newId = "internalId2-utr2-session2")
          val z = UserAnswers(internalId = "internalId3", identifier = "utr3", sessionId = "session3", newId = "internalId3-utr3-session3")
          val userAnswers = List(x, y, z)

          val result = userAnswers.combineArraysWithPath(path).get
          result mustBe x
        }
      }

      "first input has array at path" must {
        "return first in list" in {
          val x = UserAnswers(internalId = "internalId1", identifier = "utr1", sessionId = "session1", newId = "internalId1-utr1-session1",  data = array)
          val y = UserAnswers(internalId = "internalId2", identifier = "utr2", sessionId = "session2", newId = "internalId2-utr2-session2")
          val z = UserAnswers(internalId = "internalId3", identifier = "utr3", sessionId = "session3", newId = "internalId3-utr3-session3" )
          val userAnswers = List(x, y, z)

          val result = userAnswers.combineArraysWithPath(path).get
          result mustBe x
        }
      }

      "a different input has array at path" must {
        "return first in list with updated data" in {
          val x = UserAnswers(internalId = "internalId1", identifier = "utr1", sessionId = "session1", newId = "internalId1-utr1-session1")
          val y = UserAnswers(internalId = "internalId2", identifier = "utr2", sessionId = "session2", newId = "internalId2-utr2-session2")
          val z = UserAnswers(internalId = "internalId3", identifier = "utr3", sessionId = "session3", newId = "internalId3-utr3-session3", data = array)
          val userAnswers = List(x, y, z)

          val result = userAnswers.combineArraysWithPath(path).get
          result mustBe x.copy(data = array)
        }
      }

      "all inputs have array at path" must {
        "combine the arrays" in {
          val combinedArrays: JsObject = Json.parse(
            """
              |{
              |  "array": [
              |    {
              |      "foo": "bar"
              |    },
              |    {
              |      "foo": "bar"
              |    },
              |    {
              |      "foo": "bar"
              |    }
              |  ]
              |}
              |""".stripMargin
          ).as[JsObject]

          val x = UserAnswers(internalId = "internalId1", identifier = "utr1", sessionId = "session1", newId = "internalId1-utr1-session1", data = array)
          val y = UserAnswers(internalId = "internalId2", identifier = "utr2", sessionId = "session2", newId = "internalId2-utr2-session2", data = array)
          val z = UserAnswers(internalId = "internalId3", identifier = "utr3", sessionId = "session3", newId = "internalId3-utr3-session3", data = array)
          val userAnswers = List(x, y, z)

          val result = userAnswers.combineArraysWithPath(path).get
          result mustBe x.copy(data = combinedArrays)
        }
      }
    }
  }

}
