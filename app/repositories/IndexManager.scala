/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.Logging
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

trait IndexManager extends Logging {

  val collectionName: String

  val mongo: MongoDriver

  val dropIndexes: Boolean

  implicit val ec: ExecutionContext

  private final def logIndex: Future[Unit] = {
    for {
      collection <- mongo.api.database.map(_.collection[JSONCollection](collectionName))
      indices <- collection.indexesManager.list()
    } yield {
      logger.info(s"[$collectionName] indices found on collection $indices")
      ()
    }
  }

  /**
   * dropIndexesOnStartup gets evaluated when the application compiles for the first time
   * This will occur on:
   * - on deployment
   * - on container kill and new instance starting up
   *
   * This will drop the current indexes on the collection which will be re-created again
   * Only drops indexes if feature flag is enabled
   **/
  final val dropIndexesOnStartup: Future[Unit] = {
    for {
      _ <- logIndex
      _ <- if (dropIndexes) {
        for {
          collection <- mongo.api.database.map(_.collection[JSONCollection](collectionName))
          _ <- collection.indexesManager.dropAll()
          _ <- Future.successful(logger.info(s"[$collectionName] dropped indexes"))
          _ <- logIndex
        } yield ()
      } else {
        logger.info(s"[$collectionName] indexes not modified")
        Future.successful(())
      }
    } yield ()
  }

}
