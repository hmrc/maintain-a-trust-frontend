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

package views

import forms.WhatIsNextFormProvider
import models._
import models.pages.WhatIsNext
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.WhatIsNextView

class WhatIsNextViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "declarationWhatNext"

  val form = new WhatIsNextFormProvider()()

  def applyTests(trustMldStatus: TrustMldStatus): Unit = {

    def applyView(form: Form[_]): HtmlFormat.Appendable = {
      val view: WhatIsNextView = viewFor[WhatIsNextView](Some(emptyUserAnswersForUtr))
      view.apply(form, trustMldStatus)(fakeRequest, messages)
    }

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like pageWithASubmitButton(applyView(form))

    "render radio buttons with hint text" in {

      val doc = asDocument(applyView(form))

      for (option <- WhatIsNext.options(trustMldStatus)) {
        assertContainsRadioButton(doc, option._1.id, "value", option._1.value, isChecked = false)
        assertRadioButtonContainsHint(doc, option._1.id + "-item-hint", messages(option._2))
      }
    }

    "render selected radio button with hint text" when {

      for (option <- WhatIsNext.options(trustMldStatus)) {

        s"value is '${option._1.value}'" must {

          s"have the '${option._1.value}' radio button selected" in {

            val doc = asDocument(applyView(form.bind(Map("value" -> s"${option._1.value}"))))

            assertContainsRadioButton(doc, option._1.id, "value", option._1.value, isChecked = true)
            assertRadioButtonContainsHint(doc, option._1.id + "-item-hint", messages(option._2))

            for (unselectedOption <- WhatIsNext.options(trustMldStatus).filterNot(_ == option)) {
              assertContainsRadioButton(doc, unselectedOption._1.id, "value", unselectedOption._1.value, isChecked = false)
              assertRadioButtonContainsHint(doc, unselectedOption._1.id + "-item-hint", messages(unselectedOption._2))
            }
          }
        }
      }
    }
  }

  "WhatIsNextView" when {

      "underlying trust is 4mld" must {
        applyTests(Underlying4mldTrustIn5mldMode)
      }

      "underlying trust is 5mld" when {

        "taxable" must {
          applyTests(Underlying5mldTaxableTrustIn5mldMode)
        }

        "non-taxable" must {
          applyTests(Underlying5mldNonTaxableTrustIn5mldMode)
        }
      }
  }

}
