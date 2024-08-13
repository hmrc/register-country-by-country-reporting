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
import play.api.libs.json.{Json, OFormat}

final case class SubscriptionAuditDetails(
  idType: String,
  idNumber: String,
  isGBUser: Boolean,
  primaryContact: PrimaryContact,
  response: SubscriptionResponseAuditDetails,
  tradingName: Option[String],
  secondaryContact: Option[SecondaryContact]
)

object SubscriptionAuditDetails {
  implicit val format: OFormat[SubscriptionAuditDetails] = Json.format[SubscriptionAuditDetails]

  def fromSubscriptionRequestAndResponse(
    request: CreateSubscriptionForCBCRequest,
    response: CreateSubscriptionResponse
  ): SubscriptionAuditDetails = {
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
      requestDetail.secondaryContact
    )
  }
}
