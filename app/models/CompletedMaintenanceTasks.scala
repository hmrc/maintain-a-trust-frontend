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

package models

import _root_.pages.makechanges._
import models.pages.Tag
import models.pages.Tag._
import play.api.libs.json.{Format, Json}

case class CompletedMaintenanceTasks(trustDetails: Tag,
                                     assets: Tag,
                                     taxLiability: Tag,
                                     trustees: Tag,
                                     beneficiaries: Tag,
                                     settlors: Tag,
                                     protectors: Tag,
                                     other: Tag)

object CompletedMaintenanceTasks {

  implicit val formats: Format[CompletedMaintenanceTasks] = Json.format[CompletedMaintenanceTasks]

  def apply(): CompletedMaintenanceTasks = CompletedMaintenanceTasks(
    trustDetails = NotStarted,
    assets = NotStarted,
    taxLiability = NotStarted,
    trustees = NotStarted,
    beneficiaries = NotStarted,
    settlors = NotStarted,
    protectors = NotStarted,
    other = NotStarted
  )
  
  private def tagForDecision(needToAmend: Boolean): Tag = if (needToAmend) NotStarted else Completed

  def from(userAnswers: UserAnswers): Option[CompletedMaintenanceTasks] = for {
    trustDetails <- userAnswers.getWithDefault(UpdateTrustDetailsYesNoPage, false)
    assets <- userAnswers.getWithDefault(AddOrUpdateNonEeaCompanyYesNoPage, false)
    taxLiability <- userAnswers.getWithDefault(UpdateTaxLiabilityYesNoPage, false)
    trustees <- userAnswers.get(UpdateTrusteesYesNoPage)
    beneficiaries <- userAnswers.get(UpdateBeneficiariesYesNoPage)
    settlors <- userAnswers.get(UpdateSettlorsYesNoPage)
    protectors <- userAnswers.get(AddOrUpdateProtectorYesNoPage)
    otherIndividuals <- userAnswers.get(AddOrUpdateOtherIndividualsYesNoPage)
  } yield {
    CompletedMaintenanceTasks(
      tagForDecision(trustDetails),
      tagForDecision(assets),
      tagForDecision(taxLiability),
      tagForDecision(trustees),
      tagForDecision(beneficiaries),
      tagForDecision(settlors),
      tagForDecision(protectors),
      tagForDecision(otherIndividuals)
    )
  }

}
