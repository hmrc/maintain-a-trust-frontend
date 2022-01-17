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

package pages.close.taxable

import models.UserAnswers
import pages.QuestionPage
import pages.close.basePath
import pages.makechanges._
import play.api.libs.json.JsPath

import scala.util.Try

case object DateLastAssetSharedOutYesNoPage extends QuestionPage[Boolean] {

  override def path: JsPath = basePath \ toString

  override def toString: String = "dateLastAssetSharedOutYesNo"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    value match {
      case Some(false) =>
        userAnswers.remove(DateLastAssetSharedOutPage)
          .flatMap(_.remove(UpdateTrusteesYesNoPage))
          .flatMap(_.remove(UpdateBeneficiariesYesNoPage))
          .flatMap(_.remove(UpdateSettlorsYesNoPage))
          .flatMap(_.remove(AddOrUpdateProtectorYesNoPage))
          .flatMap(_.remove(AddOrUpdateOtherIndividualsYesNoPage))
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }
}
