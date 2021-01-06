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

package views.status

import views.behaviours.ViewBehaviours
import views.html.status.PlaybackProblemContactHMRCView

class PlaybackProblemContactHMRCViewSpec extends ViewBehaviours {

  val utr = "0987654321"

  "PlaybackProblemContactHMRC view" must {

    val view = viewFor[PlaybackProblemContactHMRCView](Some(emptyUserAnswers))

    val applyView = view.apply(utr)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(applyView,
      "playbackProblemContactHMRC",
      utr,
      "p1.beforeLink", "p1.link", "p1.afterLink","p2")

  }
}
