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

import generators.Generators
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.libs.json.Json

class SubscriptionAuditDetailsSpec extends AnyFreeSpec with Generators with OptionValues with Matchers {

  "SubscriptionAuditDetails" - {
    "must serialise and de-serialise as expected" in {
      forAll { subscriptionAuditDetails: SubscriptionAuditDetails[SubscriptionResponseAuditDetails] =>
        Json
          .toJson(subscriptionAuditDetails)
          .as[SubscriptionAuditDetails[SubscriptionResponseAuditDetails]] mustBe subscriptionAuditDetails
      }
    }
  }
}
