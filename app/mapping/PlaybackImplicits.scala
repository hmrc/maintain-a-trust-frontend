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

package mapping

import java.time.LocalDate

import models._
import models.http.{AddressType, NameType}
import org.joda.time.DateTime

object PlaybackImplicits {

  private def convertAddress(address: AddressType) : Address = address.postCode match {
    case Some(post) =>
      UKAddress(
        line1 = address.line1,
        line2 = address.line2,
        line3 = address.line3,
        line4 = address.line4,
        postcode = post
      )
    case None =>
      InternationalAddress(
        line1 = address.line1,
        line2 = address.line2,
        line3 = address.line3,
        country = address.country
      )
  }

  implicit class FullNameConverter(name: NameType) {
    def convert : FullName = FullName(name.firstName, name.middleName, name.lastName)
  }

  implicit class DateTimeConverter(date : DateTime) {
    def convert : LocalDate = LocalDate.of(date.getYear, date.getMonthOfYear, date.getDayOfMonth)
  }

  implicit class PassportTypeConverter(passport: PassportType) {
    def convert : PassportOrIdCardDetails =
      PassportOrIdCardDetails(passport.countryOfIssue, passport.number, passport.expirationDate)
  }

  implicit class AddressOptionConverter(address : Option[AddressType]) {
    def convert : Option[Address] = address.map(convertAddress)
  }

  implicit class AddressConverter(address : AddressType) {
    def convert : Address = convertAddress(address)
  }

}