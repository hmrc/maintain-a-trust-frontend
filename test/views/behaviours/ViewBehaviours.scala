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

import play.twirl.api.HtmlFormat
import views.ViewSpecBase

trait ViewBehaviours extends ViewSpecBase {

  def normalPage(view: HtmlFormat.Appendable,
                 messageKeyPrefix: String,
                 expectedGuidanceKeys: String*): Unit = {

    "behave like a normal page" when {

      "rendered" must {

        "have the correct banner title" in {

          val doc = asDocument(view)
          val bannerTitle = doc.getElementsByClass("hmrc-header__service-name hmrc-header__service-name--linked")
          bannerTitle.html() mustBe messages("service.name")
        }

        "display the correct browser title" in {

          val doc = asDocument(view)
          assertEqualsMessage(doc, "title", s"$messageKeyPrefix.title")
        }

        "display the correct page title" in {

          val doc = asDocument(view)
          assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix.heading")
        }

        "display the correct guidance" in {

          val doc = asDocument(view)
          for (key <- expectedGuidanceKeys) assertContainsText(doc, messages(s"$messageKeyPrefix.$key"))
        }

        "display language toggles" in {

          val doc = asDocument(view)
          assertRenderedByCssSelector(doc, "a[lang=cy]")
        }
      }
    }
  }

  def normalPageTitleWithCaption(view: HtmlFormat.Appendable,
                                 messageKeyPrefix: String,
                                 captionKey: String,
                                 captionParam: String,
                                 expectedGuidanceKeys: String*): Unit = {

    "behave like a normal page" when {

      "rendered" must {

        "have the correct banner title" in {

          val doc = asDocument(view)
          val bannerTitle = doc.getElementsByClass("hmrc-header__service-name hmrc-header__service-name--linked")
          bannerTitle.html() mustBe messages("service.name")
        }

        "display the correct browser title" in {

          val doc = asDocument(view)
          assertEqualsMessage(doc, "title", s"$messageKeyPrefix.title")
        }

        "display the correct page title" in {

          val doc = asDocument(view)

          assertPageTitleWithCaptionEqualsMessages(doc,
            expectedCaptionMessageKey = s"$captionKey.caption",
            captionParam = captionParam,
            expectedMessageKey = s"$messageKeyPrefix.heading"
          )
        }

        "display the correct guidance" in {

          val doc = asDocument(view)
          for (key <- expectedGuidanceKeys) assertContainsText(doc, messages(s"$messageKeyPrefix.$key"))
        }

        "display language toggles" in {

          val doc = asDocument(view)
          assertRenderedByCssSelector(doc, "a[lang=cy]")
        }
      }
    }
  }

  def dynamicTitlePage(view: HtmlFormat.Appendable,
                       messageKeyPrefix: String,
                       messageKeyParam: String,
                       expectedGuidanceKeys: String*): Unit = {

    "behave like a dynamic title page" when {

      "rendered" must {

        "have the correct banner title" in {

          val doc = asDocument(view)
          val bannerTitle = doc.getElementsByClass("hmrc-header__service-name hmrc-header__service-name--linked")
          bannerTitle.html() mustBe messages("service.name")
        }

        "display the correct browser title" in {

          val doc = asDocument(view)
          assertEqualsMessage(doc, "title", s"$messageKeyPrefix.title", messageKeyParam)
        }

        "display the correct page title" in {

          val doc = asDocument(view)
          assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix.heading", messageKeyParam)
        }

        "display the correct guidance" in {

          val doc = asDocument(view)
          for (key <- expectedGuidanceKeys) assertContainsText(doc, messages(s"$messageKeyPrefix.$key"))
        }

        "display language toggles" in {

          val doc = asDocument(view)
          assertRenderedByCssSelector(doc, "a[lang=cy]")
        }

      }
    }
  }

  def pageWithBackLink(view: HtmlFormat.Appendable): Unit = {

    "behave like a page with a back link" must {

      "have a back link" in {

        val doc = asDocument(view)
        assertRenderedById(doc, "back-link")
      }
    }
  }

  def pageWithASubmitButton(view: HtmlFormat.Appendable): Unit = {

    "behave like a page with a submit button" must {
      "have a submit button" in {
        val doc = asDocument(view)
        assertRenderedById(doc, "submit")
      }
    }
  }

  def pageWithPrintButton(view: HtmlFormat.Appendable): Unit = {
    "behave like a page with a print button" must {
      "have a print button" in {
        val doc = asDocument(view)
        assertRenderedById(doc, "print-button")
      }
    }
  }

  def pageWithContinueButton(view: HtmlFormat.Appendable, expectedTextKey: Option[String] = None): Unit = {

    "behave like a page with a Continue button" must {
      "have a continue button" in {
        val doc = asDocument(view)
        assertContainsTextForId(
          doc,
          "submit",
          expectedTextKey match {
            case Some(messageKey) => messages(messageKey)
            case None => "Continue"
          }
        )
      }
    }
  }

  def pageWithWarning(view: HtmlFormat.Appendable): Unit = {

    "behave like a page with warning text" in {

      val doc = asDocument(view)

      assertContainsClass(doc, "govuk-warning-text")
      assertContainsClass(doc, "govuk-warning-text__icon")
      assertContainsClass(doc, "govuk-warning-text__text")
      assertContainsClass(doc, "govuk-warning-text__assistive")
    }
  }

  def pageWithReturnToTopLink(view: HtmlFormat.Appendable): Unit = {

    "behave like a page with a return to top link" in {
      val doc = asDocument(view)

      assertRenderedById(doc, "return-to-top")
    }

  }

  def pageWithHint(view: HtmlFormat.Appendable): Unit = {
    "behave like a page with a hint" must {
      "have a hint" in {
        val doc = asDocument(view)
        assertContainsClass(doc, "govuk-hint")
      }
    }
  }

  def pageWithLink(view: HtmlFormat.Appendable,
                   linkUrl: String,
                   linkText: String): Unit = {

    s"behave like a page with a link with url $linkUrl and text $linkText" in {

      val doc = asDocument(view)
      assertContainsLink(doc, linkUrl, linkText)
    }
  }

}
