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

package navigation

import play.api.mvc.Call
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.AffinityGroup.Agent

object Navigator {

  def declarationUrl(affinity: AffinityGroup, isTrustMigratingFromNonTaxableToTaxable: Boolean): Call = {

    if (affinity == Agent) {
      controllers.declaration.routes.AgencyRegisteredAddressUkYesNoController.onPageLoad()
    } else {
      if (isTrustMigratingFromNonTaxableToTaxable) {
        controllers.transition.declaration.routes.IndividualDeclarationController.onPageLoad()
      } else {
        controllers.declaration.routes.IndividualDeclarationController.onPageLoad()
      }
    }

  }

  def agentDeclarationUrl(isTrustMigratingFromNonTaxableToTaxable: Boolean): String = {
    if (isTrustMigratingFromNonTaxableToTaxable) {
      controllers.transition.declaration.routes.AgentDeclarationController.onPageLoad().url
    } else {
      controllers.declaration.routes.AgentDeclarationController.onPageLoad().url
    }
  }

}
