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

package generators

import models._
import java.time.LocalDate
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {
  self: Generators =>

  implicit lazy val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary {
    datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1))
  }

  implicit val arbitraryRequestCommon: Arbitrary[RequestCommon] = Arbitrary {
    for {
      receiptDate <- arbitrary[String]
      acknowledgementRef <- stringsWithMaxLength(32)

    } yield RequestCommon(
      receiptDate = receiptDate,
      regime = "CBC",
      acknowledgementReference = acknowledgementRef,
      None
    )
  }

  implicit val arbitraryRegistration: Arbitrary[RegisterWithoutId] = Arbitrary {
    for {
      requestCommon <- arbitrary[RequestCommon]
      name <- arbitrary[String]
      address <- arbitrary[Address]
      contactDetails <- arbitrary[ContactDetails]
      identification <- Gen.option(arbitrary[Identification])
    } yield RegisterWithoutId(
      RegisterWithoutIDRequest(
        requestCommon,
        RequestDetails(
          NoIdOrganisation(name),
          address = address,
          contactDetails = contactDetails,
          identification = identification
        )
      )
    )
  }

  implicit val arbitraryAddress: Arbitrary[Address] = Arbitrary {
    for {
      addressLine1 <- arbitrary[String]
      addressLine2 <- Gen.option(arbitrary[String])
      addressLine3 <- arbitrary[String]
      addressLine4 <- Gen.option(arbitrary[String])
      postalCode <- Gen.option(arbitrary[String])
      countryCode <- arbitrary[String]
    } yield Address(
      addressLine1 = addressLine1,
      addressLine2 = addressLine2,
      addressLine3 = addressLine3,
      addressLine4 = addressLine4,
      postalCode = postalCode,
      countryCode = countryCode
    )
  }

  implicit val arbitraryContactDetails: Arbitrary[ContactDetails] = Arbitrary {
    for {
      phoneNumber <- Gen.option(arbitrary[String])
      mobileNumber <- Gen.option(arbitrary[String])
      faxNumber <- Gen.option(arbitrary[String])
      emailAddress <- Gen.option(arbitrary[String])
    } yield ContactDetails(
      phoneNumber = phoneNumber,
      mobileNumber = mobileNumber,
      faxNumber = faxNumber,
      emailAddress = emailAddress
    )
  }

  implicit val arbitraryIdentification: Arbitrary[Identification] = Arbitrary {
    for {
      idNumber <- arbitrary[String]
      issuingInstitution <- arbitrary[String]
      issuingCountryCode <- arbitrary[String]
    } yield Identification(
      idNumber = idNumber,
      issuingInstitution = issuingInstitution,
      issuingCountryCode = issuingCountryCode
    )
  }

}
