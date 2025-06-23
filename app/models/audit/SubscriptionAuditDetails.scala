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

package models.audit

import models.subscription.CreateSubscriptionResponse
import models.subscription.common.ContactInformationForOrganisation
import models.subscription.request.CreateSubscriptionForCBCRequest
import play.api.libs.json.{JsValue, Json, OFormat}

final case class SubscriptionAuditDetails[T](
  idType: String,
  idNumber: String,
  isGBUser: Boolean,
  organisationName: String,
  firstContactName: String,
  firstContactEmail: String,
  firstContactPhoneNumber: Option[String],
  secondContactName: Option[String],
  secondaryContactEmail: Option[String],
  secondContactPhoneNumber: Option[String],
  response: T,
  tradingName: Option[String]
)

object SubscriptionAuditDetails {
  implicit val format: OFormat[SubscriptionAuditDetails[SubscriptionResponseAuditDetails]] =
    Json.format[SubscriptionAuditDetails[SubscriptionResponseAuditDetails]]
  implicit val formatJs: OFormat[SubscriptionAuditDetails[JsValue]] = Json.format[SubscriptionAuditDetails[JsValue]]

  def fromSubscriptionRequestAndResponse(
    request: CreateSubscriptionForCBCRequest,
    response: CreateSubscriptionResponse
  ): SubscriptionAuditDetails[SubscriptionResponseAuditDetails] = {
    val requestDetail              = request.createSubscriptionForCBCRequest.requestDetail
    val createSubscriptionResponse = response.createSubscriptionForCBCResponse

    val firstContactInfo = requestDetail.primaryContact.contactInformation match {
      case ContactInformationForOrganisation(organisation, email, phone, mobile) =>
        (organisation.organisationName, email, phone.orElse(mobile))
    }
    val secondContactInfo: Option[(String, String, Option[String])] = requestDetail.secondaryContact.map(_.contactInformation match {
      case ContactInformationForOrganisation(organisation, email, phone, mobile) =>
        (organisation.organisationName, email, phone.orElse(mobile))
    })

    SubscriptionAuditDetails(
      idType = requestDetail.IDType,
      idNumber = requestDetail.IDNumber,
      isGBUser = requestDetail.isGBUser,
      organisationName = firstContactInfo._1,
      firstContactName = firstContactInfo._1, //no access to this?
      firstContactEmail = firstContactInfo._2,
      firstContactPhoneNumber = firstContactInfo._3,
      secondContactName = secondContactInfo.map(_._1),
      secondaryContactEmail = secondContactInfo.map(_._2),
      secondContactPhoneNumber = secondContactInfo.flatMap(_._3),
      response = SubscriptionResponseAuditDetails(
        Some(createSubscriptionResponse.responseDetail.subscriptionID),
        createSubscriptionResponse.responseCommon.processingDate,
        Some(createSubscriptionResponse.responseCommon.status)
      ),
      tradingName = requestDetail.tradingName
    )

  }

  def fromSubscriptionRequestAndResponse(
    request: CreateSubscriptionForCBCRequest,
    response: JsValue
  ): SubscriptionAuditDetails[JsValue] = {
    val requestDetail = request.createSubscriptionForCBCRequest.requestDetail
    val firstContactInfo = requestDetail.primaryContact.contactInformation match {
      case ContactInformationForOrganisation(organisation, email, phone, mobile) =>
        (organisation.organisationName, email, phone.orElse(mobile))
    }
    val secondContactInfo: Option[(String, String, Option[String])] = requestDetail.secondaryContact.map(_.contactInformation match {
      case ContactInformationForOrganisation(organisation, email, phone, mobile) =>
        (organisation.organisationName, email, phone.orElse(mobile))
    })
    SubscriptionAuditDetails(
      idType = requestDetail.IDType,
      idNumber = requestDetail.IDNumber,
      isGBUser = requestDetail.isGBUser,
      organisationName = firstContactInfo._1,
      firstContactName = firstContactInfo._1, //no access to this?
      firstContactEmail = firstContactInfo._2,
      firstContactPhoneNumber = firstContactInfo._3,
      secondContactName = secondContactInfo.map(_._1),
      secondaryContactEmail = secondContactInfo.map(_._2),
      secondContactPhoneNumber = secondContactInfo.flatMap(_._3),
      response = response,
      tradingName = requestDetail.tradingName
    )
  }
}
