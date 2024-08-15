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

package models.subscription

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

final case class CreateSubscriptionResponse(
  createSubscriptionForCBCResponse: CreateSubscriptionForCBCResponse
)

object CreateSubscriptionResponse {
  implicit val format: OFormat[CreateSubscriptionResponse] = Json.format[CreateSubscriptionResponse]
}

final case class CreateSubscriptionForCBCResponse(
  responseCommon: ResponseCommon,
  responseDetail: ResponseDetail
)

object CreateSubscriptionForCBCResponse {
  implicit val format: OFormat[CreateSubscriptionForCBCResponse] = Json.format[CreateSubscriptionForCBCResponse]
}

final case class ResponseCommon(
  status: String,
  processingDate: LocalDateTime
)

object ResponseCommon {
  implicit val format: OFormat[ResponseCommon] = Json.format[ResponseCommon]
}

final case class ResponseDetail(
  subscriptionID: String
)

object ResponseDetail {
  implicit val format: OFormat[ResponseDetail] = Json.format[ResponseDetail]
}
