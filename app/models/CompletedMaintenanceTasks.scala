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

package models

import _root_.pages.makechanges._
import play.api.libs.json.{Format, Json}

case class CompletedMaintenanceTasks(trustDetails: Boolean,
                                     assets: Boolean,
                                     taxLiability: Boolean,
                                     trustees: Boolean,
                                     beneficiaries: Boolean,
                                     settlors: Boolean,
                                     protectors: Boolean,
                                     other: Boolean,
                                     nonEeaCompany: Boolean)

object CompletedMaintenanceTasks {

  implicit val formats: Format[CompletedMaintenanceTasks] = Json.format[CompletedMaintenanceTasks]

  def apply(): CompletedMaintenanceTasks = CompletedMaintenanceTasks(
    trustDetails = false,
    assets = false,
    taxLiability = false,
    trustees = false,
    beneficiaries = false,
    settlors = false,
    protectors = false,
    other = false,
    nonEeaCompany = false
  )

  def from(userAnswers: UserAnswers): Option[CompletedMaintenanceTasks] = for {
    trustDetails <- userAnswers.getWithDefault(UpdateTrustDetailsYesNoPage, false)
    assets <- userAnswers.getWithDefault(UpdateAssetsYesNoPage, false)
    taxLiability <- userAnswers.getWithDefault(UpdateTaxLiabilityYesNoPage, false)
    trustees <- userAnswers.get(UpdateTrusteesYesNoPage)
    beneficiaries <- userAnswers.get(UpdateBeneficiariesYesNoPage)
    settlors <- userAnswers.get(UpdateSettlorsYesNoPage)
    protectors <- userAnswers.get(AddOrUpdateProtectorYesNoPage)
    otherIndividuals <- userAnswers.get(AddOrUpdateOtherIndividualsYesNoPage)
    nonEeaCompanies <- userAnswers.getWithDefault(AddOrUpdateNonEeaCompanyYesNoPage, false)
  } yield {
    CompletedMaintenanceTasks(!trustDetails, !assets, !taxLiability, !trustees, !beneficiaries, !settlors, !protectors, !otherIndividuals, !nonEeaCompanies)
  }

}
