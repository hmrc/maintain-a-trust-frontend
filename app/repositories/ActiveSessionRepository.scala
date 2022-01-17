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

import com.google.inject.ImplementedBy
import models.{IdentifierSession, MongoDateTimeFormats}
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
class ActiveSessionRepositoryImpl @Inject()(
                                             override val mongo: MongoDriver,
                                             config: Configuration
                                           )(override implicit val ec: ExecutionContext)
  extends ActiveSessionRepository
    with IndexManager
    with Logging {

  override val collectionName: String = "session"

  override val dropIndexes: Boolean =
    config.get[Boolean]("microservice.services.features.mongo.dropIndexes")

  private val cacheTtl = config.get[Int]("mongodb.session.ttlSeconds")

  private def collection: Future[JSONCollection] =
    for {
      _ <- ensureIndexes
      res <- mongo.api.database.map(_.collection[JSONCollection](collectionName))
    } yield res

  private val lastUpdatedIndex = Index.apply(BSONSerializationPack)(
    key = Seq("updatedAt" -> IndexType.Ascending),
    name = Some("session-updated-at-index"),
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

  private val identifierIndex = Index.apply(BSONSerializationPack)(
    key = Seq("identifier" -> IndexType.Ascending),
    name = Some("identifier-index"),
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
    logger.info("Ensuring collection indexes")
    for {
      collection              <- mongo.api.database.map(_.collection[JSONCollection](collectionName))
      createdLastUpdatedIndex <- collection.indexesManager.ensure(lastUpdatedIndex)
      createdIdIndex          <- collection.indexesManager.ensure(identifierIndex)
    } yield createdLastUpdatedIndex && createdIdIndex
  }

  override def get(internalId: String): Future[Option[IdentifierSession]] = {

    logger.debug(s"ActiveIdentifierRepository getting active identifier for $internalId")

    val selector = Json.obj("internalId" -> internalId)

    val modifier = Json.obj(
      "$set" -> Json.obj(
        "updatedAt" -> MongoDateTimeFormats.localDateTimeWrite.writes(LocalDateTime.now)
      )
    )

    for {
      col <- collection
      r <- col.findAndUpdate(
        selector = selector,
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
    } yield r.result[IdentifierSession]
  }

  override def set(session: IdentifierSession): Future[Boolean] = {

    val selector = Json.obj("internalId" -> session.internalId)

    val modifier = Json.obj(
      "$set" -> session.copy(updatedAt = LocalDateTime.now)
    )

    for {
      col <- collection
      r <- col.update(ordered = false).one(selector, modifier, upsert = true, multi = false)
    } yield r.ok
  }
}

@ImplementedBy(classOf[ActiveSessionRepositoryImpl])
trait ActiveSessionRepository {

  def get(internalId: String): Future[Option[IdentifierSession]]

  def set(session: IdentifierSession): Future[Boolean]
}
