/*
 * Copyright 2022 HM Revenue & Customs
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

package models

import _root_.pages._
import base.SpecBase
import forms.Validation
import models.pages.WhatIsNext.{NeedsToPayTax, NoLongerTaxable}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.{Lang, MessagesImpl}
import play.api.libs.json.Json
import wolfendale.scalacheck.regexp.RegexpGen

class UserAnswersSpec extends SpecBase with ScalaCheckPropertyChecks {

  "UserAnswers" when {

    ".identifierType" must {

      "return UTR when identifier matches UTR regex" in {
        forAll(RegexpGen.from(Validation.utrRegex)) {
          utr =>
            val userAnswers = emptyUserAnswersForUtr.copy(identifier = utr)
            userAnswers.identifierType mustBe UTR
        }
      }

      "return URN when identifier matches URN regex" in {
        forAll(RegexpGen.from(Validation.urnRegex)) {
          urn =>
            val userAnswers = emptyUserAnswersForUrn.copy(identifier = urn)
            userAnswers.identifierType mustBe URN
        }
      }
    }

    ".trustTaxability and .isTrustTaxable" must {
      "return taxability of trust" when {

        "taxable" in {
          val userAnswers = emptyUserAnswersForUtr

          userAnswers.trustTaxability mustBe Taxable
          userAnswers.isTrustTaxable mustBe true
          userAnswers.isTrustMigratingFromNonTaxableToTaxable mustBe false
          userAnswers.isTrustTaxableOrMigratingToTaxable mustBe true
        }

        "non-taxable" in {
          val userAnswers = emptyUserAnswersForUrn

          userAnswers.trustTaxability mustBe NonTaxable
          userAnswers.isTrustTaxable mustBe false
          userAnswers.isTrustMigratingFromNonTaxableToTaxable mustBe false
          userAnswers.isTrustTaxableOrMigratingToTaxable mustBe false
        }

        "migrating from non-taxable to taxable" in {
          val userAnswers = emptyUserAnswersForUrn.set(WhatIsNextPage, NeedsToPayTax).success.value

          userAnswers.trustTaxability mustBe MigratingFromNonTaxableToTaxable
          userAnswers.isTrustTaxable mustBe false
          userAnswers.isTrustMigratingFromNonTaxableToTaxable mustBe true
          userAnswers.isTrustTaxableOrMigratingToTaxable mustBe true
        }

        "migrating from taxable to non-taxable" in {
          val userAnswers = emptyUserAnswersForUtr.set(WhatIsNextPage, NoLongerTaxable).success.value

          userAnswers.trustTaxability mustBe MigratingFromTaxableToNonTaxable
          userAnswers.isTrustTaxable mustBe false
          userAnswers.isTrustMigratingFromNonTaxableToTaxable mustBe false
          userAnswers.isTrustTaxableOrMigratingToTaxable mustBe false
        }
      }
    }

    ".trustMldStatus" must {
      "return correct MLD status of the trust" when {

        "5mld" when {

          "underlying data is 4mld" in {
            val userAnswers = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = false)
            userAnswers.trustMldStatus mustBe Underlying4mldTrustIn5mldMode
          }

          "underlying data is 5mld" when {

            "taxable" in {
              val userAnswers = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)
              userAnswers.trustMldStatus mustBe Underlying5mldTaxableTrustIn5mldMode
            }

            "non-taxable" in {
              val userAnswers = emptyUserAnswersForUrn
              userAnswers.trustMldStatus mustBe Underlying5mldNonTaxableTrustIn5mldMode
            }
          }
        }
      }
    }

    ".leadTrusteeName" must {
      "return name of lead trustee" when {

        "individual" in {

          val userAnswers = emptyUserAnswersForUtr
            .set(trustees.IsThisLeadTrusteePage(0), false).success.value
            .set(trustees.TrusteeNamePage(0), FullName("Joe", None, "Bloggs")).success.value
            .set(trustees.IsThisLeadTrusteePage(1), true).success.value
            .set(trustees.TrusteeNamePage(1), FullName("John", None, "Doe")).success.value

          userAnswers.leadTrusteeName mustBe "John Doe"
        }

        "organisation" in {

          val userAnswers = emptyUserAnswersForUtr
            .set(trustees.IsThisLeadTrusteePage(0), false).success.value
            .set(trustees.TrusteeOrgNamePage(0), "Amazon").success.value
            .set(trustees.IsThisLeadTrusteePage(1), true).success.value
            .set(trustees.TrusteeOrgNamePage(1), "Google").success.value

          userAnswers.leadTrusteeName mustBe "Google"
        }

        "lead trustee not found" when {

          "english" in {
            implicit val messages: MessagesImpl = MessagesImpl(Lang("en"), messagesApi)
            emptyUserAnswersForUtr.leadTrusteeName mustBe "the lead trustee"
          }

          "welsh" in {
            implicit val messages: MessagesImpl = MessagesImpl(Lang("cy"), messagesApi)
            emptyUserAnswersForUtr.leadTrusteeName mustBe "y prif ymddiriedolwr"
          }
        }
      }
    }

    ".clearData" must {

      "clear answers" in {

        val previousAnswers = emptyUserAnswersForUtr
          .set(ViewLastDeclarationYesNoPage, false).success.value

        val result = previousAnswers.clearData

        result.data mustBe Json.obj()
      }
    }
  }
}
