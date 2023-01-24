/*
 * Copyright 2023 HM Revenue & Customs
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
import models.subscription.request.{RequestCommonForSubscription, RequestParameters}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class DisplaySubscriptionForCBCRequestSpec extends AnyFreeSpec with Matchers {

  val safeId: SafeId = SafeId("number")

  val params: Option[Seq[RequestParameters]] = Some(
    Seq(RequestParameters("name", "value"))
  )

  val requestDetail: ReadSubscriptionRequestDetail =
    ReadSubscriptionRequestDetail("SAFE", "number")

  val requestCommon: RequestCommonForSubscription =
    RequestCommonForSubscription("regime", None, "date", "ref", "MDTP", params)

  val readSubscriptionRequest: DisplaySubscriptionDetails =
    DisplaySubscriptionDetails(requestCommon, requestDetail)

  "DisplaySubscriptionForCBCRequest" - {

    "DisplaySubscriptionForCBCRequest must serialise as expected" in {
      val displaySubscriptionRequest: DisplaySubscriptionForCBCRequest =
        DisplaySubscriptionForCBCRequest(readSubscriptionRequest)

      val expectedJson = Json.parse(
        """
          |{
          |  "displaySubscriptionForCBCRequest": {
          |    "requestCommon": {
          |      "regime": "regime",
          |      "receiptDate": "date",
          |      "acknowledgementReference": "ref",
          |      "originatingSystem": "MDTP",
          |      "requestParameters": [
          |        {
          |          "paramName": "name",
          |          "paramValue": "value"
          |        }
          |      ]
          |    },
          |    "requestDetail": {
          |      "IDType": "SAFE",
          |      "IDNumber": "number"
          |    }
          |  }
          |}""".stripMargin
      )

      Json.toJson(displaySubscriptionRequest) mustBe expectedJson
    }

    "create DisplaySubscriptionForCBCRequest from safeId " in {

      DisplaySubscriptionForCBCRequest
        .apply(safeId) mustBe a[DisplaySubscriptionForCBCRequest]

    }
  }
}
