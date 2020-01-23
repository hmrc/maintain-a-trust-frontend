/*
 * Copyright 2020 HM Revenue & Customs
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

package viewmodels.addAnother

import models.enums.Status.InProgress
import models.enums.{IndividualOrBusiness, Status}

final case class DefaultSettlorViewModel(`type` : IndividualOrBusiness,
                                         override val status : Status
                                        ) extends SettlorViewModel


object DefaultSettlorViewModel {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit lazy val reads: Reads[DefaultSettlorViewModel] =
    ((__ \ "individualOrBusiness").read[IndividualOrBusiness] and
      (__ \ "status").readWithDefault[Status](InProgress)
      )((kind, status) =>
      DefaultSettlorViewModel(kind, status)
    )
}