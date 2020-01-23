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

import models.FullName
import models.enums.IndividualOrBusiness.Individual
import models.enums.Status.InProgress
import models.enums.{IndividualOrBusiness, Status}

sealed trait SettlorLivingViewModel extends SettlorViewModel

final case class SettlorLivingIndividualViewModel(`type` : IndividualOrBusiness,
                                                  name : String,
                                                  override val status : Status) extends SettlorLivingViewModel
object SettlorLivingViewModel {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit lazy val reads: Reads[SettlorLivingViewModel] = {

    val individualNameReads: Reads[SettlorLivingViewModel] =
    {
        (__ \ "individualOrBusiness").read[IndividualOrBusiness].filter(x => x == Individual).flatMap { _ =>
          ((__ \ "name").read[FullName].map(_.toString) and
            (__ \ "status").readWithDefault[Status](InProgress)
            ) ((name, status) => {
            SettlorLivingIndividualViewModel(
              Individual,
              name,
              status)
          })
        }
    }

    individualNameReads

  }

}