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

package generators

import models.http._
import models.pages.WhatIsNext
import models.{FullName, InternationalAddress, MigrationTaskStatus, PassportOrIdCardDetails, UKAddress}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

import java.time.LocalDate

trait ModelGenerators {

  implicit lazy val arbitraryPassport: Arbitrary[PassportOrIdCardDetails] =
    Arbitrary {
      for {
        field1 <- arbitrary[String]
        field2 <- arbitrary[String]
        field3 <- arbitrary[LocalDate]
      } yield PassportOrIdCardDetails(field1, field2, field3)
    }

  implicit lazy val arbitraryFullName: Arbitrary[FullName] = {
    Arbitrary {
      for {
        str <- arbitrary[String]
      } yield {
        FullName(str, Some(str), str)
      }
    }
  }

  implicit lazy val arbitraryInternationalAddress: Arbitrary[InternationalAddress] =
    Arbitrary {
      for {
        str <- arbitrary[String]
      } yield InternationalAddress(str, str, Some(str), str)
    }

  implicit lazy val arbitraryUkAddress: Arbitrary[UKAddress] =
    Arbitrary {
      for {
        line1 <- arbitrary[String]
        line2 <- arbitrary[String]
        line3 <- arbitrary[String]
        line4 <- arbitrary[String]
        postcode <- arbitrary[String]
      } yield UKAddress(line1, line2, Some(line3), Some(line4), postcode)
    }

  implicit lazy val arbitraryDate: Arbitrary[LocalDate] =
    Arbitrary {
      Gen.const(LocalDate.of(2010, 10, 10))
    }

  implicit lazy val arbitraryWhatIsNext: Arbitrary[WhatIsNext] =
    Arbitrary {
      Gen.oneOf(WhatIsNext.values)
    }

  implicit lazy val arbitraryMatchData: Arbitrary[MatchData] =
    Arbitrary {
      for {
        str <- arbitrary[String]
      } yield MatchData(Some(str), None)
    }

  implicit lazy val arbitraryAddress: Arbitrary[AddressType] =
    Arbitrary {
      for {
        line1 <- arbitrary[String]
        line2 <- arbitrary[String]
        line3 <- arbitrary[Option[String]]
        line4 <- arbitrary[Option[String]]
        postcode <- arbitrary[Option[String]]
        country <- arbitrary[String]
      } yield AddressType(line1, line2, line3, line4, postcode, country)
    }

  implicit lazy val arbitraryCorrespondence: Arbitrary[Correspondence] =
    Arbitrary {
      for {
        abroadIndicator <- arbitrary[Boolean]
        name <- arbitrary[String]
        address <- arbitrary[AddressType]
        bpMatchStatus <- arbitrary[Option[String]]
        phoneNumber <- arbitrary[String]
      } yield Correspondence(abroadIndicator, name, address, bpMatchStatus, phoneNumber)
    }

  implicit lazy val arbitraryDeclaration: Arbitrary[Declaration] =
    Arbitrary {
      for {
        name <- arbitrary[FullName]
      } yield Declaration(name)
    }

  implicit lazy val arbitraryTrustDetails: Arbitrary[TrustDetailsType] =
    Arbitrary {
      for {
        startDate <- arbitrary[LocalDate]
      } yield TrustDetailsType(startDate, None, None, None, None, None, None, None, None, None, None, None, None, None)
    }

  implicit lazy val arbitraryBeneficiaries: Arbitrary[DisplayTrustBeneficiaryType] =
    Arbitrary {
      DisplayTrustBeneficiaryType(Nil, Nil, Nil, Nil, Nil, Nil, Nil)
    }

  implicit lazy val arbitraryLeadTrustee: Arbitrary[DisplayTrustLeadTrusteeType] =
    Arbitrary {
      DisplayTrustLeadTrusteeType(None, None)
    }

  implicit lazy val arbitraryEntities: Arbitrary[DisplayTrustEntitiesType] =
    Arbitrary {
      for {
        beneficiaries <- arbitrary[DisplayTrustBeneficiaryType]
        leadTrustee <- arbitrary[DisplayTrustLeadTrusteeType]
      } yield DisplayTrustEntitiesType(None, beneficiaries, None, leadTrustee, None, None, None)
    }

  implicit lazy val arbitraryDisplayTrust: Arbitrary[DisplayTrust] =
    Arbitrary {
      for {
        details <- arbitrary[TrustDetailsType]
        entities <- arbitrary[DisplayTrustEntitiesType]
      } yield DisplayTrust(details, entities, None)
    }

  implicit lazy val arbitraryGetTrust: Arbitrary[GetTrust] =
    Arbitrary {
      for {
        matchData <- arbitrary[MatchData]
        correspondence <- arbitrary[Correspondence]
        declaration <- arbitrary[Declaration]
        trust <- arbitrary[DisplayTrust]
      } yield GetTrust(matchData, correspondence, declaration, trust)
    }

  implicit lazy val arbitraryProcessedTrustResponse: Arbitrary[Processed] =
    Arbitrary {
      for {
        playback <- arbitrary[GetTrust]
        formBundleNumber <- arbitrary[String]
      } yield Processed(playback, formBundleNumber)
    }

  implicit lazy val arbitraryTrustsResponse: Arbitrary[TrustsResponse] =
    Arbitrary {
      for {
        processed <- arbitrary[Processed]
        response <- Gen.oneOf(
          Seq(
            Processing,
            Closed,
            processed,
            SorryThereHasBeenAProblem,
            IdentifierNotFound,
            TrustServiceUnavailable,
            ClosedRequestResponse,
            TrustsErrorResponse
          )
        )
      } yield
        response
    }

  implicit lazy val arbitraryMigrationStatus: Arbitrary[MigrationTaskStatus] =
    Arbitrary {
      Gen.oneOf(MigrationTaskStatus.values)
    }

}
