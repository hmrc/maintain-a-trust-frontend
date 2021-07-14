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

package models.headers

import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.play.language.LanguageUtils

import scala.util.matching.Regex

case class PdfHeaders(contentDisposition: String, contentType: String, contentLength: Long) {

  val DispositionRegex: Regex = "(((inline|download); filename=)(.+).pdf)".r

  /**
   * Content-Disposition: inline; filename=1234567890-2020-10-10.pdf
   * instruction will be "inline; filename="
   * fileName will be "1234567890-2020-10-10"
   */
  def fileNameWithServiceName(implicit request: Request[AnyContent],
                              languageUtils: LanguageUtils,
                              messagesApi: MessagesApi
                             ): String = contentDisposition match {
    case DispositionRegex(_, instruction, _, filename) =>
      val serviceName = messagesApi("service.name")(languageUtils.getCurrentLang)

      s"$instruction$filename - $serviceName - GOV.UK.pdf"
    case _ =>
      contentDisposition
  }

}