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
import models.subscription.common.{PrimaryContact, SecondaryContact}
import models.subscription.request.CreateSubscriptionForCBCRequest
import play.api.libs.json.{JsValue, Json, OFormat}
import uk.gov.hmrc.auth.core.AffinityGroup

final case class SubscriptionAuditDetails[T](
  idType: String,
  idNumber: String,
  isGBUser: Boolean,
  primaryContact: PrimaryContact,
  response: T,
  tradingName: Option[String],
  secondaryContact: Option[SecondaryContact],
  userType: Option[AffinityGroup] = None
)

object SubscriptionAuditDetails {
  implicit val format: OFormat[SubscriptionAuditDetails[SubscriptionResponseAuditDetails]] =
    Json.format[SubscriptionAuditDetails[SubscriptionResponseAuditDetails]]
  implicit val formatJs: OFormat[SubscriptionAuditDetails[JsValue]] = Json.format[SubscriptionAuditDetails[JsValue]]

  def fromSubscriptionRequestAndResponse(
    request: CreateSubscriptionForCBCRequest,
    response: CreateSubscriptionResponse,
    userType: AffinityGroup
  ): SubscriptionAuditDetails[SubscriptionResponseAuditDetails] = {
    val requestDetail              = request.createSubscriptionForCBCRequest.requestDetail
    val createSubscriptionResponse = response.createSubscriptionForCBCResponse

    SubscriptionAuditDetails(
      requestDetail.IDType,
      requestDetail.IDNumber,
      requestDetail.isGBUser,
      requestDetail.primaryContact,
      response = SubscriptionResponseAuditDetails(
        createSubscriptionResponse.responseDetail.subscriptionID,
        createSubscriptionResponse.responseCommon.status,
        createSubscriptionResponse.responseCommon.processingDate
      ),
      requestDetail.tradingName,
      requestDetail.secondaryContact,
      Some(userType)
    )
  }

  def fromSubscriptionRequestAndResponse(
    request: CreateSubscriptionForCBCRequest,
    response: JsValue,
    userType: AffinityGroup
  ): SubscriptionAuditDetails[JsValue] = {
    val requestDetail = request.createSubscriptionForCBCRequest.requestDetail

    SubscriptionAuditDetails(
      requestDetail.IDType,
      requestDetail.IDNumber,
      requestDetail.isGBUser,
      requestDetail.primaryContact,
      response = response,
      requestDetail.tradingName,
      requestDetail.secondaryContact,
      Some(userType)
    )
  }
}
