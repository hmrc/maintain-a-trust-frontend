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

package views

import views.behaviours.ViewBehaviours
import views.html.RefSentByPostAgentView

class RefSentByPostAgentViewSpec extends ViewBehaviours {

  "RefSentByPost Agent view" must {

    val view = viewFor[RefSentByPostAgentView](Some(emptyUserAnswers))

    val applyView = view.apply()(fakeRequest, messages)

    behave like normalPage(applyView,
      "refSentByPost.agent", "paragraph1",
      "paragraph2",
      "bullet1",
      "bullet2",
      "paragraph3",
      "paragraph4",
      "paragraph4.link",
      "paragraph5",
      "paragraph5.link"
    )

    behave like pageWithBackLink(applyView)
  }
}
