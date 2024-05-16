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

package test.repositories

import models.UserAnswers
import models.errors.MongoError
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.{MongoException, MongoTimeoutException}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterEach, EitherValues, OptionValues}
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import repositories.PlaybackRepositoryImpl
import uk.gov.hmrc.mongo.test.MongoSupport

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class PlaybackRepositorySpec extends AnyWordSpec with Matchers
  with ScalaFutures with OptionValues with MongoSupport with MongoSuite with BeforeAndAfterEach with EitherValues {

  override def beforeEach(): Unit = Await.result(repository.collection.deleteMany(BsonDocument()).toFuture(),Duration.Inf)

  private lazy val repository: PlaybackRepositoryImpl = new PlaybackRepositoryImpl(mongoComponent, config)

  "a session repository" should {

    "must return None when no answer exists" in {
      val internalId = "Int-328969d0-557e-4559-sdba-074d0597107e"
      val identifier = "Testing"
      val sessionId = "Test"

      repository.get(internalId, identifier, sessionId).value.futureValue mustBe Right(None)
    }

    "must return the userAnswers after insert" in {
      val internalId = "Int-328969d0-557e-4559-sdba-074d0597107e"
      val identifier = "Testing"
      val sessionId = "Test"
      val newId = s"$internalId-$identifier-$sessionId"
      val userAnswers: UserAnswers = UserAnswers(internalId,identifier,sessionId,newId)

      repository.get(internalId, identifier, sessionId).value.futureValue mustBe Right(None)

      repository.set(userAnswers).value.futureValue mustBe Right(true)

      val userAnswerstest = repository.get(internalId, identifier, sessionId).value.futureValue
      userAnswerstest.value.map(_.copy(updatedAt = userAnswers.updatedAt)) mustBe Some(userAnswers)

    }

    "must return the userAnswers after update" in {
      val internalId = "Int-328969d0-557e-4559-sdba-074d0597107e"
      val identifier = "Testing"
      val sessionId = "Test"
      val newId = s"$internalId-$identifier-$sessionId"
      val userAnswers: UserAnswers = UserAnswers(internalId,identifier,sessionId,newId)
      val userAnswers2 = userAnswers.copy(data = Json.obj("key" -> "123"), isUnderlyingData5mld = true, isUnderlyingDataTaxable = false)

      repository.get(internalId, identifier, sessionId).value.futureValue mustBe Right(None)

      repository.set(userAnswers).value.futureValue mustBe Right(true)

      val userAnswersTest = repository.get(internalId, identifier, sessionId).value.futureValue
      userAnswersTest.value.map(_.copy(updatedAt = userAnswers.updatedAt)) mustBe Some(userAnswers)

      //update

      repository.set(userAnswers2).value.futureValue mustBe Right(true)

      val userAnswersTest2 = repository.get(internalId, identifier, sessionId).value.futureValue
      userAnswersTest2.value.map(_.copy(updatedAt = userAnswers.updatedAt)) mustBe Some(userAnswers2)

    }

    "delete one userAnswers from the collection when resetCache is called" in {
      val internalId = "Int-328969d0-557e-4559-sdba-074d0597107e"
      val identifier = "Testing"
      val sessionId = "Test"
      val newId = s"$internalId-$identifier-$sessionId"
      val userAnswers: UserAnswers = UserAnswers(internalId,identifier,sessionId,newId)
      val userAnswers2 = UserAnswers("internalId","identifier","sessionId", "internalId-identifier-sessionId",
        updatedAt = LocalDateTime.of(2023, 4, 19, 12, 0))

      //setting user answers
      repository.get(internalId, identifier, sessionId).value.futureValue mustBe Right(None)
      repository.set(userAnswers).value.futureValue mustBe Right(true)
      repository.set(userAnswers2).value.futureValue mustBe Right(true)

      // remove one set of user answers

      await(repository.resetCache(internalId, identifier, sessionId).value).value mustBe Some(true)
      await(repository.get(internalId, identifier, sessionId).value).value mustBe None
      await(repository.get("internalId", "identifier", "sessionId").value)
        .value.map(_.copy(updatedAt = userAnswers2.updatedAt)) mustBe Some(userAnswers2)
    }

    Seq(new MongoTimeoutException("test message"), new MongoException("test message"), new Exception, new RuntimeException("test message"),
      new NullPointerException("test message"), new NoSuchElementException("test message"), new IndexOutOfBoundsException("test message")).foreach { exception =>
      s"return a Left(MongoError) when there's an exception from Mongo ($exception)" in {
        val result = Future.failed(exception)
          .recover(repository.mongoRecover("test repository", "test method","test message", "test sessionId"))

        await(result) mustBe Left(MongoError)
      }
    }
  }

}
