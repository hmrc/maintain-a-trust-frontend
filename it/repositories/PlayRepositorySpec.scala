/*
 * Copyright 2022 HM Revenue & Customs
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

package repositories

import models.UserAnswers
import org.mongodb.scala.bson.BsonDocument
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.mongo.test.MongoSupport
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class PlayRepositorySpec extends AnyWordSpec with Matchers
  with ScalaFutures with OptionValues with MongoSupport with MongoSuite with BeforeAndAfterEach {

  override def beforeEach() = Await.result(repository.collection.deleteMany(BsonDocument()).toFuture(),Duration.Inf)

  lazy val repository: PlaybackRepositoryImpl = new PlaybackRepositoryImpl(mongoComponent, config)

  "a session repository" should {

    "must return None when no answer exists" in {
      val internalId = "Int-328969d0-557e-4559-sdba-074d0597107e"
      val identifier = "Testing"
      val sessionId = "Test"

      repository.get(internalId, identifier, sessionId).futureValue mustBe None
    }

    "must return the userAnswers after insert" in {
      val internalId = "Int-328969d0-557e-4559-sdba-074d0597107e"
      val identifier = "Testing"
      val sessionId = "Test"
      val newId = s"$internalId-$identifier-$sessionId"
      val userAnswers: UserAnswers = UserAnswers(internalId,identifier,sessionId,newId)

      repository.get(internalId, identifier, sessionId).futureValue mustBe None

      repository.set(userAnswers).futureValue mustBe true

      val userAnswerstest = repository.get(internalId, identifier, sessionId).futureValue
      userAnswerstest.map(_.copy(updatedAt = userAnswers.updatedAt)) mustBe Some(userAnswers)

    }

    "must return the userAnswers after update" in {
      val internalId = "Int-328969d0-557e-4559-sdba-074d0597107e"
      val identifier = "Testing"
      val sessionId = "Test"
      val newId = s"$internalId-$identifier-$sessionId"
      val userAnswers: UserAnswers = UserAnswers(internalId,identifier,sessionId,newId)
      val userAnswers2 = userAnswers.copy(data = Json.obj("key" -> "123"), isUnderlyingData5mld = true, isUnderlyingDataTaxable = false)

      repository.get(internalId, identifier, sessionId).futureValue mustBe None

      repository.set(userAnswers).futureValue mustBe true

      val userAnswerstest = repository.get(internalId, identifier, sessionId).futureValue
      userAnswerstest.map(_.copy(updatedAt = userAnswers.updatedAt)) mustBe Some(userAnswers)

      //update

      repository.set(userAnswers2).futureValue mustBe true

      val userAnswerstest2 = repository.get(internalId, identifier, sessionId).futureValue
      userAnswerstest2.map(_.copy(updatedAt = userAnswers.updatedAt)) mustBe Some(userAnswers2)

    }
  }

}