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

package views.declaration

import views.behaviours.ViewBehaviours
import views.html.declaration.PlaybackDeclaredAnswersView

class PlaybackDeclaredAnswersViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "playbackDeclarationAnswers"

  "PlaybackDeclaredAnswers view" must {

    val application = applicationBuilder().build()

    val view = application.injector.instanceOf[PlaybackDeclaredAnswersView]

    val applyView = view.apply(Nil, Nil, "tvn", "crn", "27 January 2020", isAgent = true)(fakeRequest, messages)

    behave like normalPage(applyView, "playbackDeclarationAnswers")
  }

}
