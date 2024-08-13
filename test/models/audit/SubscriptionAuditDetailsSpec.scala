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
      forAll { subscriptionAuditDetails: SubscriptionAuditDetails =>
        Json
          .toJson(subscriptionAuditDetails)
          .as[SubscriptionAuditDetails] mustBe subscriptionAuditDetails
      }
    }
  }
}
