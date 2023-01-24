/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.libs.json._
import play.api.Logging
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import org.mongodb.scala.model.Filters.equal
import config.FrontendAppConfig
import org.mongodb.scala.model.{FindOneAndUpdateOptions, IndexModel, IndexOptions, ReplaceOptions, ReturnDocument, Updates}
import javax.inject.{Inject, Singleton}
import org.bson.conversions.Bson
import org.mongodb.scala.model.Indexes.ascending
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PlaybackRepositoryImpl @Inject()(
                                        val mongoComponent: MongoComponent,
                                        val config: FrontendAppConfig
                                      )(implicit val ec: ExecutionContext)
  extends PlayMongoRepository[UserAnswers](
    collectionName = "user-answers",
    mongoComponent = mongoComponent,
    domainFormat = Format(UserAnswers.reads,UserAnswers.writes),
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
      )
    ), replaceIndexes = config.dropIndexes

  ) with Logging with PlaybackRepository {

  private def selector(internalId: String, identifier: String, sessionId: String): Bson =
    equal("newId",s"$internalId-$identifier-$sessionId")


  def get(internalId: String, identifier: String, sessionId: String): Future[Option[UserAnswers]] = {

    val modifier = Updates.set("updatedAt",LocalDateTime.now)

    val updateOption = new FindOneAndUpdateOptions().upsert(false).returnDocument(ReturnDocument.BEFORE)
    collection.findOneAndUpdate(selector(internalId,identifier,sessionId),modifier,updateOption).toFutureOption()

  }

  override def set(userAnswers: UserAnswers): Future[Boolean] =
    collection.replaceOne(selector(userAnswers.internalId,userAnswers.identifier,userAnswers.sessionId),
                userAnswers.copy(updatedAt = LocalDateTime.now),ReplaceOptions().upsert(true))
               .head().map(_.wasAcknowledged())

  override def resetCache(internalId: String, identifier: String, sessionId: String): Future[Option[Boolean]] = {

    collection.deleteOne(selector(internalId, identifier, sessionId)).toFutureOption().map(_.map(_.wasAcknowledged()))

  }

}