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

package test.repositories

import models.IdentifierSession
import models.errors.MongoError
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.{MongoException, MongoTimeoutException}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterEach, EitherValues, OptionValues}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import repositories.ActiveSessionRepositoryImpl
import uk.gov.hmrc.mongo.test.MongoSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class ActiveSessionRepositorySpec extends AnyWordSpec with Matchers
  with ScalaFutures with OptionValues with MongoSupport with MongoSuite with BeforeAndAfterEach with EitherValues {

  override def beforeEach(): Unit = Await.result(repository.collection.deleteMany(BsonDocument()).toFuture(),Duration.Inf)

  private lazy val repository: ActiveSessionRepositoryImpl = new ActiveSessionRepositoryImpl(mongoComponent, config)

  "a session repository" should {

    "must return None when no cache exists" in  {

        val internalId = "Int-328969d0-557e-4559-sdba-074d0597107e"

        repository.get(internalId).value.futureValue mustBe Right(None)
    }

    "must return a UtrSession when one exists" in {

        val internalId = "Int-328969d0-557e-2559-96ba-074d0597107e"

        val session = IdentifierSession(internalId, "utr")

        val initial = repository.set(session).value.futureValue

        initial mustBe Right(true)

        repository.get(internalId).value.futureValue.value.map(_.identifier) mustBe Some("utr")
    }

    "must override an existing session for an internalId" in {

        val internalId = "Int-328969d0-557e-4559-96ba-0d4d0597107e"

        val session = IdentifierSession(internalId, "utr")

        repository.set(session).value.futureValue

        repository.get(internalId).value.futureValue.value.map(_.identifier) mustBe Some("utr")
        repository.get(internalId).value.futureValue.value.map(_.internalId) mustBe Some(internalId)

        // update

        val session2 = IdentifierSession(internalId, "utr2")

        repository.set(session2).value.futureValue

        repository.get(internalId).value.futureValue.value.map(_.identifier) mustBe Some("utr2")
        repository.get(internalId).value.futureValue.value.map(_.internalId) mustBe Some(internalId)
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
