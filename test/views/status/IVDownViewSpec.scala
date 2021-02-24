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
import views.html.status.IVDownView

class IVDownViewSpec extends ViewBehaviours {

  val utr = "0987654321"

  "IVDown view" must {

    val view = viewFor[IVDownView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(utr)(fakeRequest, messages)

    behave like normalPageTitleWithCaption(view = applyView,
      messageKeyPrefix =  "ivDown",
      None,
      captionParam = utr,
      "p1")
  }
}
