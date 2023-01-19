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

package models

import play.api.libs.json._

import java.time.LocalDateTime

final case class IdentifierSession(internalId: String,
                                   identifier: String,
                                   updatedAt: LocalDateTime = LocalDateTime.now)

object IdentifierSession {

  implicit lazy val reads: Reads[IdentifierSession] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "internalId").read[String] and
        (__ \ "identifier").read[String] and
        (__ \ "updatedAt").read(MongoDateTimeFormats.localDateTimeRead)
      ) (IdentifierSession.apply _)
  }

  implicit lazy val writes: OWrites[IdentifierSession] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "internalId").write[String] and
        (__ \ "identifier").write[String] and
        (__ \ "updatedAt").write(MongoDateTimeFormats.localDateTimeWrite)
      ) (unlift(IdentifierSession.unapply))
  }

}
