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

package views.print

import views.behaviours.ViewBehaviours
import views.html.print.PrintLastDeclaredAnswersView

class PrintLastDeclaredAnswersViewSpec extends ViewBehaviours {

  lazy val continueUrl: String =  controllers.routes.WhatIsNextController.onPageLoad().url

  "PrintLastDeclaredAnswersView view" must {

    val view = viewFor[PrintLastDeclaredAnswersView](Some(emptyUserAnswersForUtr))

    val applyView = view.apply(Nil, Nil)(fakeRequest, messages)

    behave like normalPage(applyView, "playbackAnswers")

    behave like pageWithContinueButton(applyView, Some("site.button.continue.maintaining"))

    behave like pageWithPrintButton(applyView)

    behave like pageWithBackLink(applyView)

    behave like pageWithReturnToTopLink(applyView)

    "render correct content" in {
      val doc = asDocument(applyView)

      assertContainsText(doc,
        "Last declared copy of the trust’s registration"
      )

      assertContainsText(doc,
        "You only need to declare the trust is up to date every year if there is a tax liability"
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
