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

package utils

import java.time.format.DateTimeFormatter

import models.pages.KindOfTrust
import models.pages.KindOfTrust._
import models.{Address, FullName, InternationalAddress, PassportOrIdCardDetails, UKAddress, UserAnswers}
import pages.beneficiaries.individual.IndividualBeneficiaryNamePage
import pages.settlors.deceased_settlor.SettlorNamePage
import pages.settlors.living_settlor.SettlorIndividualNamePage
import pages.trustees.TrusteeNamePage
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.domain.Nino
import utils.countryoptions.CountryOptions

object CheckAnswersFormatters {

  val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  def utr(answer: String)(implicit messages: Messages): Html = {
    HtmlFormat.escape(answer)
  }

  def yesOrNo(answer: Boolean)(implicit messages: Messages): Html = {
    if (answer) {
      HtmlFormat.escape(messages("site.yes"))
    } else {
      HtmlFormat.escape(messages("site.no"))
    }
  }

  def formatNino(nino: String): String = try {
    Nino(nino).formatted
  } catch {
    case _: IllegalArgumentException => nino
  }

  def country(code: String, countryOptions: CountryOptions): String =
    countryOptions.options.find(_.value.equals(code)).map(_.label).getOrElse("")

  def currency(value: String): Html = escape(s"£$value")

  def percentage(value: String): Html = escape(s"$value%")

  def trusteeName(index: Int, userAnswers: UserAnswers): String =
    userAnswers.get(TrusteeNamePage(index)).map(_.toString).getOrElse("")

  def answer[T](key: String, answer: T)(implicit messages: Messages): Html =
    HtmlFormat.escape(messages(s"$key.$answer"))

  def escape(x: String) = HtmlFormat.escape(x)

  def deceasedSettlorName(userAnswers: UserAnswers): String =
    userAnswers.get(SettlorNamePage).map(_.toString).getOrElse("")

  def indBeneficiaryName(index: Int, userAnswers: UserAnswers): String = {
    userAnswers.get(IndividualBeneficiaryNamePage(index)).map(_.toString).getOrElse("")
  }

  def livingSettlorName(index: Int, userAnswers: UserAnswers): String = {
    userAnswers.get(SettlorIndividualNamePage(index)).map(_.toString).getOrElse("")
  }

  def ukAddress(address: UKAddress): Html = {
    val lines =
      Seq(
        Some(HtmlFormat.escape(address.line1)),
        Some(HtmlFormat.escape(address.line2)),
        address.line3.map(HtmlFormat.escape),
        address.line4.map(HtmlFormat.escape),
        Some(HtmlFormat.escape(address.postcode))
      ).flatten

    Html(lines.mkString("<br />"))
  }

  def internationalAddress(address: InternationalAddress, countryOptions: CountryOptions): Html = {
    val lines =
      Seq(
        Some(HtmlFormat.escape(address.line1)),
        Some(HtmlFormat.escape(address.line2)),
        address.line3.map(HtmlFormat.escape),
        Some(country(address.country, countryOptions))
      ).flatten

    Html(lines.mkString("<br />"))
  }

  def addressFormatter(address: Address, countryOptions: CountryOptions): Html = {
    address match {
      case a:UKAddress => ukAddress(a)
      case a:InternationalAddress => internationalAddress(a, countryOptions)
    }
  }

  def passportOrIDCard(passportOrIdCard: PassportOrIdCardDetails, countryOptions: CountryOptions): Html = {
    val lines =
      Seq(
        Some(country(passportOrIdCard.country, countryOptions)),
        Some(HtmlFormat.escape(passportOrIdCard.cardNumber)),
        Some(HtmlFormat.escape(passportOrIdCard.expiryDate.format(dateFormatter)))
      ).flatten

    Html(lines.mkString("<br />"))
  }

  def fullName(fullname: FullName) = {
    val middle = fullname.middleName.map(" " + _ + " ").getOrElse(" ")
    s"${fullname.firstName}${middle}${fullname.lastName}"
  }

  def kindOfTrust(kindOfTrust: KindOfTrust, messages: Messages) = {
    kindOfTrust match {
      case Intervivos =>
        messages("kindOfTrust.Lifetime")
      case Deed =>
        messages("kindOfTrust.Deed")
      case Employees =>
        messages("kindOfTrust.Employees")
      case FlatManagement =>
        messages("kindOfTrust.Building")
      case HeritageMaintenanceFund =>
        messages("kindOfTrust.Repair")
    }
  }

}
