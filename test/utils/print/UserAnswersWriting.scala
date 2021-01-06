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

package utils.print

import cats.data.State
import generators.ModelGenerators
import models.UserAnswers
import models.pages.IndividualOrBusiness
import org.scalacheck.Arbitrary
import org.scalatest.TryValues
import pages.trustees._
import play.api.libs.json.Writes
import queries.Settable

import scala.language.implicitConversions

trait UserAnswersWriting extends TryValues with ModelGenerators {
  class SettableWriterOps[T : Writes](s: Settable[T]) {
    def is(value: T): State[UserAnswers, Unit] = writeUA(s, value)
    def :=(value: T): State[UserAnswers, Unit] = is(value)
    def withArbitraryValue(implicit arb: Arbitrary[T]): State[UserAnswers, Unit] = writeArbUA(s)
    def isRemoved: State[UserAnswers, Unit] = remove(s)
  }

  implicit def settableStuff[T:Writes](s: Settable[T]) : SettableWriterOps[T] = new SettableWriterOps[T](s)

  def writeUA[T](s: Settable[T], value: T)(implicit writes: Writes[T]): State[UserAnswers, Unit] = {
    State(_.set(s, value).success.value -> Unit)
  }

  def writeArbUA[T](s: Settable[T])(implicit writes: Writes[T], arb: Arbitrary[T]): State[UserAnswers, Unit] = {
    arb.arbitrary.sample
      .map(t => writeUA(s, t))
      .getOrElse(throw new Exception(s"Test value generation failure for ${s.path}"))
  }

  def remove(s: Settable[_]): State[UserAnswers, Unit] = {
    State(_.remove(s).success.value -> Unit)
  }

  def individualUKTrustee(index: Int): State[UserAnswers, Unit] = for {
    _ <- IsThisLeadTrusteePage(index) is false
    _ <- TrusteeIndividualOrBusinessPage(index) is IndividualOrBusiness.Individual
    _ <- TrusteeNamePage(index).withArbitraryValue
    _ <- TrusteeDateOfBirthYesNoPage(index).withArbitraryValue
    _ <- TrusteeDateOfBirthPage(index).withArbitraryValue
    _ <- TrusteeNinoYesNoPage(index) is true
    _ <- TrusteeAUKCitizenPage(index) is true
    _ <- TrusteeNinoPage(index) is "AA000000A"
    _ <- TrusteeAddressYesNoPage(index) is true
    _ <- TrusteeAddressInTheUKPage(index) is true
    _ <- TrusteeUkAddressPage(index).withArbitraryValue
    _ <- TrusteeTelephoneNumberPage(index).withArbitraryValue
    _ <- TrusteeEmailPage(index).withArbitraryValue
  } yield Unit

  def individualNonUkTrustee(index: Int): State[UserAnswers, Unit] = for {
    _ <- individualUKTrustee(index)
    _ <- moveIndividualOutOfUK(index)
  } yield Unit

  def moveIndividualOutOfUK(index: Int): State[UserAnswers, Unit] = for {
    _ <- TrusteeNinoYesNoPage(index) is false
    _ <- TrusteeAUKCitizenPage(index) is false
    _ <- TrusteePassportIDCardYesNoPage(index).withArbitraryValue
    _ <- TrusteePassportIDCardPage(index).withArbitraryValue
    _ <- TrusteeAddressInTheUKPage(index) is false
    _ <- TrusteeInternationalAddressPage(index).withArbitraryValue
    _ <- TrusteeNinoPage(index).isRemoved
    _ <- TrusteeUkAddressPage(index).isRemoved
  } yield Unit

  def ukCompanyTrustee(index: Int): State[UserAnswers, Unit] = for {
  _ <- TrusteeIndividualOrBusinessPage(index) is IndividualOrBusiness.Business
  _ <- IsThisLeadTrusteePage(index) is false
  _ <- TrusteeOrgNamePage(index).withArbitraryValue
  _ <- TrusteeUtrYesNoPage(index).withArbitraryValue
  _ <- TrusteeUtrPage(index).withArbitraryValue
  } yield Unit
}
