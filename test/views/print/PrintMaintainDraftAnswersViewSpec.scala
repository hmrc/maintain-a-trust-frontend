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

package views.print

import viewmodels.AnswerSection
import views.behaviours.ViewBehaviours
import views.html.print.PrintMaintainDraftAnswersView

class PrintMaintainDraftAnswersViewSpec extends ViewBehaviours {

  "PrintMaintainDraftAnswersView view" must {

    val application = applicationBuilder().build()

    val view = application.injector.instanceOf[PrintMaintainDraftAnswersView]

    val applyView = view.apply(AnswerSection(), Nil, Nil)(fakeRequest, messages)

    behave like normalPage(applyView, "playbackDraftAnswers")

    "render correct content" in {
      val doc = asDocument(applyView)

      assertContainsText(doc,
        "Draft copy of the trust's declaration"
      )

      assertContainsText(doc,
        "This is a draft copy confirming that the trust is up to date."
      )

      assertContainsText(doc,
        "If you have made any changes they must reflect what’s in the trust deed. You will need to re-enter and declare for them to be updated on this service."
      )

      assertContainsText(doc,
        "This is not a declaration."
      )

      assertContainsText(doc,
        "No further updates to trust details, assets and years of tax liability are required through this service."
      )
      assertContainsText(doc,
        "If you need to view the latest information HMRC holds about these sections or need to update them, use Self Assessment Online for trusts."
      )
    }

  }

}
