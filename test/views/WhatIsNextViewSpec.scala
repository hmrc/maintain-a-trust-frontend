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

import forms.WhatIsNextFormProvider
import models.pages.WhatIsNext
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.WhatIsNextView

class WhatIsNextViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "declarationWhatNext"

  val form = new WhatIsNextFormProvider()()

  val view: WhatIsNextView = viewFor[WhatIsNextView](Some(emptyUserAnswersForUtr))

  def applyView(form: Form[_], is5mldEnabled: Boolean): HtmlFormat.Appendable =
    view.apply(form, is5mldEnabled)(fakeRequest, messages)

  "WhatIsNextView" when {

    "4mld" must {

      val is5mldEnabled = false

      behave like normalPage(applyView(form, is5mldEnabled), messageKeyPrefix)

      behave like pageWithBackLink(applyView(form, is5mldEnabled))

      behave like pageWithASubmitButton(applyView(form, is5mldEnabled))

      "render radio buttons with hint text" in {

        val doc = asDocument(applyView(form, is5mldEnabled))

        for (option <- WhatIsNext.options(is5mldEnabled)) {
          assertContainsRadioButton(doc, option._1.id, "value", option._1.value, isChecked = false)
          assertRadioButtonContainsHint(doc, option._1.id + ".hint", messages(option._2))
        }
      }

      "render selected radio button with hint text" when {

        for (option <- WhatIsNext.options(is5mldEnabled)) {

          s"value is '${option._1.value}'" must {

            s"have the '${option._1.value}' radio button selected" in {

              val doc = asDocument(applyView(form.bind(Map("value" -> s"${option._1.value}")), is5mldEnabled))

              assertContainsRadioButton(doc, option._1.id, "value", option._1.value, isChecked = true)
              assertRadioButtonContainsHint(doc, option._1.id + ".hint", messages(option._2))

              for (unselectedOption <- WhatIsNext.options(is5mldEnabled).filterNot(_ == option)) {
                assertContainsRadioButton(doc, unselectedOption._1.id, "value", unselectedOption._1.value, isChecked = false)
                assertRadioButtonContainsHint(doc, unselectedOption._1.id + ".hint", messages(unselectedOption._2))
              }
            }
          }
        }
      }
    }

    "5mld" must {

      val is5mldEnabled = true

      behave like normalPage(applyView(form, is5mldEnabled), messageKeyPrefix)

      behave like pageWithBackLink(applyView(form, is5mldEnabled))

      behave like pageWithASubmitButton(applyView(form, is5mldEnabled))

      "render radio buttons with hint text" in {

        val doc = asDocument(applyView(form, is5mldEnabled))

        for (option <- WhatIsNext.options(is5mldEnabled)) {
          assertContainsRadioButton(doc, option._1.id, "value", option._1.value, isChecked = false)
          assertRadioButtonContainsHint(doc, option._1.id + ".hint", messages(option._2))
        }
      }

      "render selected radio button with hint text" when {

        for (option <- WhatIsNext.options(is5mldEnabled)) {

          s"value is '${option._1.value}'" must {

            s"have the '${option._1.value}' radio button selected" in {

              val doc = asDocument(applyView(form.bind(Map("value" -> s"${option._1.value}")), is5mldEnabled))

              assertContainsRadioButton(doc, option._1.id, "value", option._1.value, isChecked = true)
              assertRadioButtonContainsHint(doc, option._1.id + ".hint", messages(option._2))

              for (unselectedOption <- WhatIsNext.options(is5mldEnabled).filterNot(_ == option)) {
                assertContainsRadioButton(doc, unselectedOption._1.id, "value", unselectedOption._1.value, isChecked = false)
                assertRadioButtonContainsHint(doc, unselectedOption._1.id + ".hint", messages(unselectedOption._2))
              }
            }
          }
        }
      }
    }
  }
}
