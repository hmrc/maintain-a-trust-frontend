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

package repositories

import cats.data.EitherT
import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import models.IdentifierSession
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
class ActiveSessionRepositoryImpl @Inject()(
                                             val mongoComponent: MongoComponent,
                                             val config: FrontendAppConfig
                                           )(implicit val ec: ExecutionContext)
  extends PlayMongoRepository[IdentifierSession](
    collectionName = "session",
    mongoComponent = mongoComponent,
    domainFormat = Format(IdentifierSession.reads, IdentifierSession.writes),
    indexes = Seq(
      IndexModel(
        ascending("updatedAt"),
        IndexOptions()
          .unique(false)
          .name("session-updated-at-index")
          .expireAfter(config.cachettlSessionInSeconds, TimeUnit.SECONDS)),
      IndexModel(
        ascending("identifier"),
        IndexOptions()
          .unique(false)
          .name("identifier-index")
      )
    ), replaceIndexes = config.dropIndexes

  ) with Logging with ActiveSessionRepository with RepositoryHelper {

  private val className = getClass.getSimpleName

  def get(internalId: String): TrustEnvelope[Option[IdentifierSession]] = EitherT {

    val selector = equal("internalId", internalId)
    val modifier = Updates.set("updatedAt", LocalDateTime.now())
    val updateOption = new FindOneAndUpdateOptions().upsert(false)

    collection.findOneAndUpdate(selector, modifier, updateOption)
      .toFutureOption()
      .map(Right(_))
      .recover {
        mongoRecover(
          repository = className,
          method = "get",
          sessionId = internalId,
          message = s"operation failed due to exception from Mongo"
        )
      }
  }

  def set(session: IdentifierSession): TrustEnvelope[Boolean] = EitherT {

    val selector = equal("internalId", session.internalId)

    collection.replaceOne(selector, session.copy(updatedAt = LocalDateTime.now), ReplaceOptions().upsert(true))
      .head()
      .map(updateResult => Right(updateResult.wasAcknowledged()))
      .recover {
        mongoRecover(
          repository = className,
          method = "set",
          sessionId = session.internalId,
          message = s"operation failed due to exception from Mongo"
        )
    }
  }

}

@ImplementedBy(classOf[ActiveSessionRepositoryImpl])
trait ActiveSessionRepository {

  def get(internalId: String): TrustEnvelope[Option[IdentifierSession]]

  def set(session: IdentifierSession): TrustEnvelope[Boolean]
}
