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

package models.headers

import base.SpecBase
import play.api.test.FakeRequest
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.play.language.LanguageUtils

class PdfHeadersSpec extends SpecBase {

  private val languageUtils: LanguageUtils = injector.instanceOf[LanguageUtils]

  "Pdf headers" must {
    val number10 = 10L

    "parse header and modify filename in english" in {
      val headers = PdfHeaders(
        contentDisposition = "inline; filename=1234567890-2020-10-10.pdf",
        contentType = "application/pdf",
        contentLength = number10
      )

      val fileName = headers.fileNameWithServiceName(fakeRequest, languageUtils, messagesApi)

      fileName mustBe "inline; filename=1234567890-2020-10-10 - Manage a Trust - GOV.UK.pdf"
    }

    "parse header and modify filename in welsh" in {
      val headers = PdfHeaders(
        contentDisposition = "inline; filename=1234567890-2020-10-10.pdf",
        contentType = "application/pdf",
        contentLength = number10
      )

      val fakeRequestInWelsh = FakeRequest()
        .withHeaders(HeaderNames.ACCEPT_LANGUAGE -> "cy")

      val fileName = headers.fileNameWithServiceName(fakeRequestInWelsh, languageUtils, messagesApi)

      fileName mustBe "inline; filename=1234567890-2020-10-10 - Rheoli ymddiriedolaeth - GOV.UK.pdf"
    }

  }

}
