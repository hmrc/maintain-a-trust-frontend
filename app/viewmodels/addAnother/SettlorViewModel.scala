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

import models.pages.Status

import scala.language.implicitConversions

trait SettlorViewModel {

  val status : Status

}

object SettlorViewModel {

  import play.api.libs.json._

  implicit class ReadsWithContravariantOr[A](a: Reads[A]) {

    def or[B >: A](b: Reads[B]): Reads[B] =
      a.map[B](identity).orElse(b)
  }

  implicit def convertToSupertype[A, B >: A](a: Reads[A]): Reads[B] =
    a.map(identity)

  implicit lazy val reads : Reads[SettlorViewModel] = {
    SettlorLivingViewModel.reads or
      DefaultSettlorViewModel.reads
  }

}