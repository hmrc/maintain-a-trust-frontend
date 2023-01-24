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

package views.declaration

import uk.gov.hmrc.auth.core.AffinityGroup
import views.behaviours.ViewBehaviours
import views.html.declaration.ProblemDeclaringView

class ProblemDeclaringViewSpec extends ViewBehaviours {

  "ProblemDeclaring view" must {

    val view = viewFor[ProblemDeclaringView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(AffinityGroup.Agent)(fakeRequest, messages)

    behave like normalPage(applyView,
      "problemDeclaring",
      "p1",
      "p2",
      "contact.link",
      "p3",
      "return.link"
    )
  }
}
