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

package models.subscription

import models.SafeId
import models.subscription.request.RequestCommonForSubscription
import play.api.libs.json.{Json, OFormat}

case class ReadSubscriptionRequestDetail(IDType: String, IDNumber: String)

object ReadSubscriptionRequestDetail {
  implicit val format: OFormat[ReadSubscriptionRequestDetail] =
    Json.format[ReadSubscriptionRequestDetail]

  private val idType: String = "SAFE"

  def apply(safeId: SafeId): ReadSubscriptionRequestDetail =
    new ReadSubscriptionRequestDetail(idType, safeId.value)

}

case class DisplaySubscriptionDetails(
    requestCommon: RequestCommonForSubscription,
    requestDetail: ReadSubscriptionRequestDetail
)

object DisplaySubscriptionDetails {
  implicit val format: OFormat[DisplaySubscriptionDetails] =
    Json.format[DisplaySubscriptionDetails]

  def apply(safeId: SafeId): DisplaySubscriptionDetails = {
    DisplaySubscriptionDetails(
      RequestCommonForSubscription.createRequestCommonForSubscription(),
      ReadSubscriptionRequestDetail(safeId)
    )
  }
}

case class DisplaySubscriptionForCBCRequest(
    displaySubscriptionForCBCRequest: DisplaySubscriptionDetails
)

object DisplaySubscriptionForCBCRequest {
  implicit val format: OFormat[DisplaySubscriptionForCBCRequest] =
    Json.format[DisplaySubscriptionForCBCRequest]

  def apply(safeId: SafeId): DisplaySubscriptionForCBCRequest =
    DisplaySubscriptionForCBCRequest(DisplaySubscriptionDetails(safeId))
}
