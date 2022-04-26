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

import models.{MongoDateTimeFormats, UserAnswers}
import play.api.libs.json._
import play.api.{Configuration, Logging}
import reactivemongo.api.WriteConcern
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONSerializationPack
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json.collection.Helpers.idWrites
import reactivemongo.play.json.collection.JSONCollection

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PlaybackRepositoryImpl @Inject()(
                                        override val mongo: MongoDriver,
                                        config: Configuration
                                      )(override implicit val ec: ExecutionContext)
  extends PlaybackRepository
    with IndexManager
    with Logging {

  override val collectionName: String = "user-answers"

  override val dropIndexes: Boolean =
    config.get[Boolean]("microservice.services.features.mongo.dropIndexes")

  private val cacheTtl = config.get[Int]("mongodb.playback.ttlSeconds")

  private val dropIndexFeature: Boolean = config.get[Boolean]("microservice.services.features.mongo.dropIndexes")

  private def collection: Future[JSONCollection] =
    for {
      _ <- ensureIndexes
      res <- mongo.api.database.map(_.collection[JSONCollection](collectionName))
    } yield res

  private val lastUpdatedIndex = Index.apply(BSONSerializationPack)(
    key = Seq("updatedAt" -> IndexType.Ascending),
    name = Some("user-answers-updated-at-index"),
    expireAfterSeconds = Some(cacheTtl),
    options = BSONDocument.empty,
    unique = false,
    background = false,
    dropDups = false,
    sparse = false,
    version = None,
    partialFilter = None,
    storageEngine = None,
    weights = None,
    defaultLanguage = None,
    languageOverride = None,
    textIndexVersion = None,
    sphereIndexVersion = None,
    bits = None,
    min = None,
    max = None,
    bucketSize = None,
    collation = None,
    wildcardProjection = None
  )

  private val internalIdAndUtrAndSessionIdIndex = Index.apply(BSONSerializationPack)(
    key = Seq("newId" -> IndexType.Ascending),
    name = Some("internal-id-and-utr-and-sessionId-compound-index"),
    expireAfterSeconds = None,
    options = BSONDocument.empty,
    unique = false,
    background = false,
    dropDups = false,
    sparse = false,
    version = None,
    partialFilter = None,
    storageEngine = None,
    weights = None,
    defaultLanguage = None,
    languageOverride = None,
    textIndexVersion = None,
    sphereIndexVersion = None,
    bits = None,
    min = None,
    max = None,
    bucketSize = None,
    collation = None,
    wildcardProjection = None
  )

  private lazy val ensureIndexes = {
    for {
      collection              <- mongo.api.database.map(_.collection[JSONCollection](collectionName))
      createdLastUpdatedIndex <- collection.indexesManager.ensure(lastUpdatedIndex)
      createdIdIndex          <- collection.indexesManager.ensure(internalIdAndUtrAndSessionIdIndex)
    } yield createdLastUpdatedIndex && createdIdIndex
  }

  private final def logIndex: Future[Unit] = {
    for {
      collection <- mongo.api.database.map(_.collection[JSONCollection](collectionName))
      indices <- collection.indexesManager.list()
    } yield {
      ()
    }
  }

  final val dropIndex: Unit = {
    for {
      _ <- logIndex
      _ <- if (dropIndexFeature) {
         for {
           collection <- mongo.api.database.map(_.collection[JSONCollection](collectionName))
           _ <- collection.indexesManager.dropAll()
           _ <- Future.successful(logger.info(s"[PlaybackRepositoryImpl][PlaybackRepository] dropped indexes on collection $collectionName"))
           _ <- logIndex
         } yield ()
      } else {
        logger.info(s"[PlaybackRepositoryImpl][PlaybackRepository] indexes not modified")
        Future.successful(())
      }
    } yield ()
  }

  private def selector(internalId: String, identifier: String, sessionId: String): JsObject = Json.obj(
    "newId" -> s"$internalId-$identifier-$sessionId"
  )

  override def get(internalId: String, identifier: String, sessionId: String): Future[Option[UserAnswers]] = {

    val modifier = Json.obj(
      "$set" -> Json.obj(
        "updatedAt" -> MongoDateTimeFormats.localDateTimeWrite.writes(LocalDateTime.now)
      )
    )

    for {
      col <- collection
      r <- col.findAndUpdate(
        selector = selector(internalId, identifier, sessionId),
        update = modifier,
        fetchNewObject = true,
        upsert = false,
        sort = None,
        fields = None,
        bypassDocumentValidation = false,
        writeConcern = WriteConcern.Default,
        maxTime = None,
        collation = None,
        arrayFilters = Nil
      )
    } yield r.result[UserAnswers]
  }

  override def set(userAnswers: UserAnswers): Future[Boolean] = {

    val modifier = Json.obj(
      "$set" -> userAnswers.copy(updatedAt = LocalDateTime.now)
    )

    for {
      col <- collection
      r <- col.update(ordered = false).one(
        selector(userAnswers.internalId, userAnswers.identifier, userAnswers.sessionId), modifier, upsert = true, multi = false)
    } yield r.ok
  }

  override def resetCache(internalId: String, identifier: String, sessionId: String): Future[Option[JsObject]] = {
    
    for {
      col <- collection
      r <- col.findAndRemove(selector(internalId, identifier, sessionId), None, None, WriteConcern.Default, None, None, Seq.empty)
    } yield r.value
  }
}

trait PlaybackRepository {

  def get(internalId: String, identifier: String, sessionId: String): Future[Option[UserAnswers]]

  def set(userAnswers: UserAnswers): Future[Boolean]

  def resetCache(internalId: String, identifier: String, sessionId: String): Future[Option[JsObject]]
}
