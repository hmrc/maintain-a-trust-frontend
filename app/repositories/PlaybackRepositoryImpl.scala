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

package repositories

import cats.data.EitherT
import config.FrontendAppConfig
import models.UserAnswers
import org.bson.conversions.Bson
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model._
import play.api.Logging
import play.api.libs.json._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import utils.TrustEnvelope.TrustEnvelope

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class PlaybackRepositoryImpl @Inject()(
                                        val mongoComponent: MongoComponent,
                                        val config: FrontendAppConfig
                                      )(implicit val ec: ExecutionContext)
  extends PlayMongoRepository[UserAnswers](
    collectionName = "user-answers",
    mongoComponent = mongoComponent,
    domainFormat = Format(UserAnswers.reads, UserAnswers.writes),
    indexes = Seq(
      IndexModel(
        ascending("updatedAt"),
        IndexOptions()
          .unique(false)
          .name("user-answers-updated-at-index")
          .expireAfter(config.cachettlplaybackInSeconds, TimeUnit.SECONDS)),
      IndexModel(
        ascending("newId"),
        IndexOptions()
          .unique(false)
          .name("internal-id-and-utr-and-sessionId-compound-index")
      ),
      IndexModel(
        ascending("internalId"),
        IndexOptions()
          .unique(false)
          .name("internal-id-index")
      ),
      IndexModel(
        ascending("identifier"),
        IndexOptions()
          .unique(false)
          .name("identifier-index")
      ),
      IndexModel(
        ascending("sessionId"),
        IndexOptions()
          .unique(false)
          .name("session-id-index")
      )
    ), replaceIndexes = config.dropIndexes

  ) with Logging with PlaybackRepository with RepositoryHelper {

  private val className = getClass.getSimpleName

  private def selector(internalId: String, identifier: String, sessionId: String): Bson =
    equal("newId", s"$internalId-$identifier-$sessionId")


  def get(internalId: String, identifier: String, sessionId: String): TrustEnvelope[Option[UserAnswers]] = EitherT {

    val modifier = Updates.set("updatedAt", LocalDateTime.now)
    val updateOption = new FindOneAndUpdateOptions().upsert(false).returnDocument(ReturnDocument.BEFORE)

    collection.findOneAndUpdate(selector(internalId, identifier, sessionId), modifier, updateOption)
      .toFutureOption()
      .map(Right(_))
      .recover {
        mongoRecover(
          repository = className,
          method = "get",
          sessionId = sessionId,
          message = "operation failed due to exception from Mongo"
        )
      }
  }

  override def set(userAnswers: UserAnswers): TrustEnvelope[Boolean] = EitherT {
    collection.replaceOne(
      selector(userAnswers.internalId, userAnswers.identifier, userAnswers.sessionId),
      userAnswers.copy(updatedAt = LocalDateTime.now),
      ReplaceOptions().upsert(true)
    )
      .head()
      .map(updateResult => Right(updateResult.wasAcknowledged()))
      .recover {
        mongoRecover(
          repository = className,
          method = "set",
          sessionId = userAnswers.internalId,
          message = "operation failed due to exception from Mongo"
        )
      }
  }

  override def resetCache(internalId: String, identifier: String, sessionId: String): TrustEnvelope[Option[Boolean]] = EitherT {

    collection.deleteOne(selector(internalId, identifier, sessionId))
      .toFutureOption()
      .map(optDeleteResult => Right(optDeleteResult
        .map(_.wasAcknowledged())
      ))
      .recover {
        mongoRecover(
          repository = className,
          method = "resetCache",
          sessionId = sessionId,
          message = "operation failed due to exception from Mongo")
      }
  }

}
