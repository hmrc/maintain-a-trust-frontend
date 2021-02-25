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

import forms.WhichIdentifierFormProvider
import models.WhichIdentifier
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.WhichIdentifierView

class WhichIdentifierViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "whichIdentifier"

  val form = new WhichIdentifierFormProvider()()

  val view = viewFor[WhichIdentifierView](Some(emptyUserAnswersForUtr))

  def applyView(form: Form[_]): HtmlFormat.Appendable =
    view.apply(form)(fakeRequest, messages)

  "WhichIdentifierView" must {

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like pageWithASubmitButton(applyView(form))
  }

  "WhichIdentifierView" when {

    "rendered" must {

      "contain radio buttons for the value" in {

        val doc = asDocument(applyView(form))

        for (option <- WhichIdentifier.options) {
          assertContainsRadioButton(doc, option._1.id, "value", option._1.value, false)
          assertRadioButtonContainsHint(doc, option._1.id + ".hint", messages(option._2))
        }
      }
    }

    for (option <- WhichIdentifier.options) {

      s"rendered with a value of '${option._1.value}'" must {

        s"have the '${option._1.value}' radio button selected" in {

          val doc = asDocument(applyView(form.bind(Map("value" -> s"${option._1.value}"))))

          assertContainsRadioButton(doc, option._1.id, "value", option._1.value, true)
          assertRadioButtonContainsHint(doc, option._1.id + ".hint", messages(option._2))

          for (unselectedOption <- WhichIdentifier.options.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, unselectedOption._1.id, "value", unselectedOption._1.value, false)
            assertRadioButtonContainsHint(doc, unselectedOption._1.id + ".hint", messages(unselectedOption._2))
          }
        }
      }
    }
  }
}
