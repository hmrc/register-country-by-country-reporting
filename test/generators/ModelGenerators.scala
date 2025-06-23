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

package generators

import models._
import models.audit.{SubscriptionAuditDetails, SubscriptionResponseAuditDetails}
import models.subscription._
import models.subscription.common._
import models.subscription.request._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.auth.core.AffinityGroup

import java.time.{LocalDate, LocalDateTime}

trait ModelGenerators {
  self: Generators =>

  def nonEmptyString: Gen[String] =
    Gen.nonEmptyListOf(Gen.alphaChar).map(_.mkString)

  implicit lazy val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary {
    datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1))
  }

  implicit val arbitraryRequestCommon: Arbitrary[RequestCommon] = Arbitrary {
    for {
      receiptDate        <- nonEmptyString
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
      requestCommon  <- arbitrary[RequestCommon]
      name           <- nonEmptyString
      address        <- arbitrary[Address]
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
      addressLine1 <- nonEmptyString
      addressLine2 <- Gen.option(nonEmptyString)
      addressLine3 <- nonEmptyString
      addressLine4 <- Gen.option(nonEmptyString)
      postalCode   <- Gen.option(nonEmptyString)
      countryCode  <- nonEmptyString
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
      phoneNumber  <- Gen.option(nonEmptyString)
      mobileNumber <- Gen.option(nonEmptyString)
      faxNumber    <- Gen.option(nonEmptyString)
      emailAddress <- Gen.option(nonEmptyString)
    } yield ContactDetails(
      phoneNumber = phoneNumber,
      mobileNumber = mobileNumber,
      faxNumber = faxNumber,
      emailAddress = emailAddress
    )
  }

  implicit val arbitraryIdentification: Arbitrary[Identification] = Arbitrary {
    for {
      idNumber           <- nonEmptyString
      issuingInstitution <- nonEmptyString
      issuingCountryCode <- nonEmptyString
    } yield Identification(
      idNumber = idNumber,
      issuingInstitution = issuingInstitution,
      issuingCountryCode = issuingCountryCode
    )
  }

  implicit val arbitraryRegisterWithID: Arbitrary[RegisterWithID] =
    Arbitrary {
      for {
        registerWithIDRequest <- arbitrary[RegisterWithIDRequest]
      } yield RegisterWithID(registerWithIDRequest)
    }

  implicit val arbitraryRegisterWithIDRequest: Arbitrary[RegisterWithIDRequest] = Arbitrary {
    for {
      requestCommon <- arbitrary[RequestCommon]
      requestDetail <- arbitrary[RequestWithIDDetails]
    } yield RegisterWithIDRequest(requestCommon, requestDetail)
  }

  implicit val arbitraryRequestWithIDDetails: Arbitrary[RequestWithIDDetails] =
    Arbitrary {
      for {
        idType            <- nonEmptyString
        idNumber          <- nonEmptyString
        requiresNameMatch <- arbitrary[Boolean]
        isAnAgent         <- arbitrary[Boolean]
        partnerDetails    <- arbitrary[WithIDOrganisation]
      } yield RequestWithIDDetails(
        idType,
        idNumber,
        requiresNameMatch,
        isAnAgent,
        Option(partnerDetails)
      )
    }

  implicit val arbitraryWithIDOrganisation: Arbitrary[WithIDOrganisation] =
    Arbitrary {
      for {
        organisationName <- nonEmptyString
        organisationType <- Gen.oneOf(
          Seq("0000", "0001", "0002", "0003", "0004")
        )
      } yield WithIDOrganisation(organisationName, organisationType)
    }

  implicit val arbitraryRequestCommonForSubscription: Arbitrary[RequestCommonForSubscription] =
    Arbitrary {
      for {
        receiptDate        <- nonEmptyString
        acknowledgementRef <- stringsWithMaxLength(32)
      } yield RequestCommonForSubscription(
        regime = "CBC",
        conversationID = None,
        receiptDate = receiptDate,
        acknowledgementReference = acknowledgementRef,
        originatingSystem = "MDTP",
        None
      )
    }

  implicit val arbitraryOrganisationDetails: Arbitrary[OrganisationDetails] =
    Arbitrary {
      for {
        name <- nonEmptyString
      } yield OrganisationDetails(organisationName = name)
    }

  implicit val arbitraryContactInformationForOrganisation: Arbitrary[ContactInformationForOrganisation] = Arbitrary {
    for {
      organisation <- arbitrary[OrganisationDetails]
      email        <- nonEmptyString
      phone        <- Gen.option(nonEmptyString)
      mobile       <- Gen.option(nonEmptyString)
    } yield ContactInformationForOrganisation(
      organisation,
      email,
      phone,
      mobile
    )
  }

  implicit val arbitraryPrimaryContact: Arbitrary[PrimaryContact] = Arbitrary {
    for {
      contactInformation <- arbitrary[ContactInformationForOrganisation]
    } yield PrimaryContact(contactInformation)
  }

  implicit val arbitrarySecondaryContact: Arbitrary[SecondaryContact] =
    Arbitrary {
      for {
        contactInformation <- arbitrary[ContactInformationForOrganisation]
      } yield SecondaryContact(contactInformation)
    }

  implicit val arbitraryRequestDetail: Arbitrary[RequestDetail] = Arbitrary {
    for {
      idType           <- nonEmptyString
      idNumber         <- nonEmptyString
      tradingName      <- Gen.option(nonEmptyString)
      isGBUser         <- arbitrary[Boolean]
      primaryContact   <- arbitrary[PrimaryContact]
      secondaryContact <- Gen.option(arbitrary[SecondaryContact])
    } yield RequestDetail(
      IDType = idType,
      IDNumber = idNumber,
      tradingName = tradingName,
      isGBUser = isGBUser,
      primaryContact = primaryContact,
      secondaryContact = secondaryContact
    )
  }

  implicit val arbitraryCreateSubscriptionForCBCRequest: Arbitrary[CreateSubscriptionForCBCRequest] =
    Arbitrary {
      for {
        requestCommon <- arbitrary[RequestCommonForSubscription]
        requestDetail <- arbitrary[RequestDetail]
      } yield CreateSubscriptionForCBCRequest(
        SubscriptionRequest(requestCommon, requestDetail)
      )
    }

  implicit val arbitraryReadSubscriptionRequestDetail: Arbitrary[ReadSubscriptionRequestDetail] = Arbitrary {
    for {
      idType   <- nonEmptyString
      idNumber <- nonEmptyString
    } yield ReadSubscriptionRequestDetail(
      IDType = idType,
      IDNumber = idNumber
    )
  }

  implicit val arbitraryReadSubscriptionForCBCRequest: Arbitrary[DisplaySubscriptionForCBCRequest] =
    Arbitrary {
      for {
        requestCommon <- arbitrary[RequestCommonForSubscription]
        requestDetail <- arbitrary[ReadSubscriptionRequestDetail]
      } yield DisplaySubscriptionForCBCRequest(
        DisplaySubscriptionDetails(requestCommon, requestDetail)
      )
    }

  implicit val arbitraryCreateSubscriptionForCBCResponseCommon: Arbitrary[ResponseCommon] =
    Arbitrary {
      for {
        status         <- nonEmptyString
        processingDate <- arbitrary[LocalDateTime]
      } yield ResponseCommon(status, processingDate)
    }

  implicit val arbitraryCreateSubscriptionForCBCResponseDetail: Arbitrary[ResponseDetail] =
    Arbitrary(validSubscriptionID.map(ResponseDetail.apply))

  implicit val arbitraryCreateSubscriptionForCBCResponse: Arbitrary[CreateSubscriptionForCBCResponse] =
    Arbitrary {
      for {
        responseDetail <- arbitrary[ResponseDetail]
        responseCommon <- arbitrary[ResponseCommon]
      } yield CreateSubscriptionForCBCResponse(responseCommon, responseDetail)
    }

  implicit val arbitraryCreateSubscriptionResponse: Arbitrary[CreateSubscriptionResponse] =
    Arbitrary(arbitrary[CreateSubscriptionForCBCResponse].map(CreateSubscriptionResponse.apply))

  implicit val arbitrarySubscriptionAuditDetails: Arbitrary[SubscriptionAuditDetails[SubscriptionResponseAuditDetails]] =
    Arbitrary {
      for {
        request  <- arbitrary[CreateSubscriptionForCBCRequest]
        response <- arbitrary[CreateSubscriptionResponse]
        affinity <- arbitrary[AffinityGroup]
      } yield SubscriptionAuditDetails.fromSubscriptionRequestAndResponse(request, response)
    }

  implicit val arbitraryAffinityGroup: Arbitrary[AffinityGroup] = Arbitrary {
    Gen.oneOf(
      Seq(
        AffinityGroup.Organisation,
        AffinityGroup.Individual,
        AffinityGroup.Agent
      )
    )
  }
}
