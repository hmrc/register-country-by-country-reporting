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

package connectors

import base.{SpecBase, WireMockServerHandler}
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.Generators
import models.subscription.DisplaySubscriptionForCBCRequest
import models.subscription.request.CreateSubscriptionForCBCRequest
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status.OK

import scala.concurrent.ExecutionContext.Implicits.global

class SubscriptionConnectorSpec extends SpecBase with WireMockServerHandler with Generators with ScalaCheckPropertyChecks {

  override lazy val app: Application = applicationBuilder()
    .configure(
      conf = "microservice.services.create-subscription.port" -> server.port(),
      "microservice.services.create-subscription-cbc.port" -> server.port(),
      "microservice.services.read-subscription.port"       -> server.port()
    )
    .build()

  lazy val connector: SubscriptionConnector =
    app.injector.instanceOf[SubscriptionConnector]

  private val errorCodes: Gen[Int] = Gen.chooseNum(400, 599)

  "SubscriptionConnector" - {
    "create subscription CBC" - {
      "must return status as OK for submission of Subscription" in {
        stubResponse(
          "/dac6/dct50c/v1",
          OK
        )

        forAll(arbitrary[CreateSubscriptionForCBCRequest]) { sub =>
          val result = connector.sendSubscriptionInformation(sub)
          result.futureValue.status mustBe OK
        }
      }

      "must return an error status for submission of invalid subscription Data" in {

        forAll(arbitrary[CreateSubscriptionForCBCRequest], errorCodes) { (sub, errorCode) =>
          stubResponse(
            "/dac6/dct50c/v1",
            errorCode
          )

          val result = connector.sendSubscriptionInformation(sub)
          result.futureValue.status mustBe errorCode
        }
      }
    }

    "read subscription" - {
      "must return status as OK for read Subscription" in {
        stubResponse(
          "/dac6/dct50d/v1",
          OK
        )

        forAll(arbitrary[DisplaySubscriptionForCBCRequest]) { sub =>
          val result = connector.readSubscriptionInformation(sub)
          result.futureValue.status mustBe OK
        }
      }

      "must return an error status for  invalid read Subscription" in {

        forAll(arbitrary[DisplaySubscriptionForCBCRequest], errorCodes) { (sub, errorCode) =>
          stubResponse(
            "/dac6/dct50d/v1",
            errorCode
          )

          val result = connector.readSubscriptionInformation(sub)
          result.futureValue.status mustBe errorCode
        }
      }
    }
  }

  private def stubResponse(
    expectedUrl: String,
    expectedStatus: Int
  ): StubMapping =
    server.stubFor(
      post(urlEqualTo(expectedUrl))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
        )
    )
}
