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

package views.print

import viewmodels.AnswerSection
import views.behaviours.ViewBehaviours
import views.html.print.PrintMaintainFinalDeclaredAnswersView

class PrintMaintainFinalDeclaredAnswersViewSpec extends ViewBehaviours {

  "PrintMaintainFinalDeclaredAnswersView view" must {

    val application = applicationBuilder().build()

    val view = application.injector.instanceOf[PrintMaintainFinalDeclaredAnswersView]

    val applyView = view.apply(AnswerSection(), Nil, Nil, "tvn", "crn", "27 January 2020", isAgent = true)(fakeRequest, messages)

    behave like normalPage(applyView, "playbackFinalDeclarationAnswers")

    behave like pageWithPrintButton(applyView)

    behave like pageWithBackLink(applyView)

    "render correct content" in {
      val doc = asDocument(applyView)

      assertContainsText(doc,
        "Final declared copy of updates to this trust"
      )

      assertContainsText(doc,
        "Declaration reference number is: tvn"
      )

      assertContainsText(doc,
        "Client reference number: crn"
      )

      assertContainsText(doc,
        "The trust’s final declaration was sent on 27 January 2020"
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
