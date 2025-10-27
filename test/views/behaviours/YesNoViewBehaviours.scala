/*
 * Copyright 2024 HM Revenue & Customs
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

package views.behaviours

import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.ViewUtils

trait YesNoViewBehaviours extends QuestionViewBehaviours[Boolean] {

  //scalastyle:off
  def yesNoPage(form: Form[Boolean],
                createView: Form[Boolean] => HtmlFormat.Appendable,
                messageKeyPrefix: String,
                headingAndTitleKey: Option[String] = None): Unit = {

    "behave like a page with a Yes/No question" when {

      "rendered" must {

        "contain a legend for the question" in {

          val expectedLegendText = if (headingAndTitleKey.isDefined) {
            s"$messageKeyPrefix.question"
          } else {
            s"$messageKeyPrefix.heading"
          }

          val doc = asDocument(createView(form))
          val legends = doc.getElementsByTag("legend")
          legends.size mustBe 1
          legends.first.text must include(messages(expectedLegendText))
        }

        "contain an input for the value" in {

          val doc = asDocument(createView(form))
          assertRenderedById(doc, "value-yes")
          assertRenderedById(doc, "value-no")
        }

        "have no values checked when rendered with no form" in {

          val doc = asDocument(createView(form))
          assert(!doc.getElementById("value-yes").hasAttr("checked"))
          assert(!doc.getElementById("value-no").hasAttr("checked"))
        }

        "not render an error summary" in {

          val doc = asDocument(createView(form))
          assertNotRenderedByClass(doc, "govuk-error-summary")
        }
      }

      "rendered with a value of true" must {

        behave like answeredYesNoPage(form, createView, answer = true)
      }

      "rendered with a value of false" must {

        behave like answeredYesNoPage(form, createView, answer = false)
      }

      "rendered with an error" must {

        "show an error summary" in {

          val doc = asDocument(createView(form.withError(error)))
          assertRenderedByClass(doc, "govuk-error-summary")
        }

        "show an error in the value field's label" in {

          val doc = asDocument(createView(form.withError(error)))
          val errorSpan = doc.getElementsByClass("govuk-error-message").first
          errorSpan.text mustBe s"""${messages("site.error")} ${messages(errorMessage)}"""
        }

        "show an error prefix in the browser title" in {

          val title = headingAndTitleKey.getOrElse(messageKeyPrefix)

          val doc = asDocument(createView(form.withError(error)))
          assertEqualsValue(doc, "title", ViewUtils.breadcrumbTitle(s"""${messages("error.browser.title.prefix")} ${messages(s"$title.title")}"""))
        }
      }
    }
  }


  def answeredYesNoPage(form: Form[Boolean],
                        createView: Form[Boolean] => HtmlFormat.Appendable,
                        answer: Boolean): Unit = {

    "have only the correct value checked" in {
      val doc = asDocument(createView(form.fill(answer)))
      assert(doc.getElementById("value-yes").hasAttr("checked") == answer)
      assert(doc.getElementById("value-no").hasAttr("checked") != answer)
    }

    "not render an error summary" in {
      val doc = asDocument(createView(form.fill(answer)))
      assertNotRenderedByClass(doc, "govuk-error-summary")
    }
  }

  def pageWithQuestionSubHeading(view: HtmlFormat.Appendable, expectedQuestionSubHeadingKey: String, expectedText: String) : Unit = {

    "have the expected question sub heading" in {

      val doc = asDocument(view)

      val subHeadingText = doc.getElementsByClass("govuk-heading-m").first().text()

      assert(subHeadingText == messages(expectedQuestionSubHeadingKey))
      assert(subHeadingText == expectedText)

    }
  }
}
