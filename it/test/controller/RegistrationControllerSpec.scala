/*
 * Copyright 2026 HM Revenue & Customs
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

package controller

import connectors.RegistrationConnector
import controllers.auth.{AuthAction, AuthActionImpl}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{doAnswer, spy, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, BodyParsers, Request, Result}
import play.api.test.*
import play.api.test.Helpers.*
import play.api.{Application, inject}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.HttpVerbs.POST

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegistrationControllerSpec extends AnyWordSpec with Matchers with MockitoSugar {

  private val validRegistrationWithoutIdRequest =
    """
      |{
      |  "registerWithoutIDRequest": {
      |    "requestCommon": {
      |      "receiptDate": "2025-01-01",
      |      "regime": "CBC",
      |      "acknowledgementReference": "ABC123"
      |    },
      |    "requestDetail": {
      |      "organisation": {
      |        "organisationName": "Test Org"
      |      },
      |      "address": {
      |        "addressLine1": "1 Street",
      |        "addressLine3": "City",
      |        "countryCode": "GB"
      |      },
      |      "contactDetails": {}
      |    }
      |  }
      |}
      |""".stripMargin

  private val validRegistrationWithIdRequest =
    """
      |  {
      |    "registerWithIDRequest": {
      |      "requestCommon": {
      |        "receiptDate": "2025-01-01",
      |        "regime": "CBC",
      |        "acknowledgementReference": "ABC123"
      |      },
      |      "requestDetail": {
      |        "IDType": "UTR",
      |        "IDNumber": "1234567890",
      |        "requiresNameMatch": false,
      |        "isAnAgent": false
      |      }
      |    }
      |  }
      |""".stripMargin

  private def unauthorisedApp(): Application = new GuiceApplicationBuilder().build()

  private def authorisedApp(): Application = {

    val realAuthAction = new AuthActionImpl(
      authConnector = mock[AuthConnector],
      parser = mock[BodyParsers.Default],
    )

    val authAction = spy(realAuthAction)

    doAnswer { invocation =>
      val request = invocation.getArgument(0, classOf[Request[AnyContent]])

      val block = invocation.getArgument(1).asInstanceOf[Request[AnyContent] => Future[Result]]

      block(request)
    }.when(authAction).invokeBlock(any(), any())

    val mockRegistrationConnector = mock[RegistrationConnector]

    when(
      mockRegistrationConnector.sendWithoutIDInformation(any())(any(), any())
    ).thenReturn(
      Future.successful(HttpResponse(OK, "{}"))
    )

    when(mockRegistrationConnector.sendWithID(any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "{}")))

    new GuiceApplicationBuilder()
      .overrides(
        inject.bind[AuthAction].toInstance(authAction),
        inject.bind[RegistrationConnector].toInstance(mockRegistrationConnector)
      )
      .build()
  }

  "POST /registration/noId" should {

    "reject unauthenticated requests" in {
      val request =
        FakeRequest(POST, "/register-country-by-country-reporting/registration/noId")
          .withHeaders(CONTENT_TYPE -> "application/json")
          .withBody(
            Json.obj(
              "someField" -> "someValue"
            )
          )

      val app = unauthorisedApp()

      running(app) {
        val result = route(app, request).getOrElse {
          fail("Route not defined.")
        }

        status(result) should (be(UNAUTHORIZED) or be(FORBIDDEN))
      }
    }

    "return 200 OK when authorised" in {

      val jsonBody = Json.parse(validRegistrationWithoutIdRequest)

      val request =
        FakeRequest(POST, "/register-country-by-country-reporting/registration/noId")
          .withHeaders(CONTENT_TYPE -> "application/json")
          .withBody(jsonBody)

      val app = authorisedApp()

      running(app) {
        val result =
          route(app, request).getOrElse(fail("Route not defined"))

        status(result) shouldBe OK
      }
    }
  }

  "POST /registration/utr " should {

    "reject unauthenticated requests" in {
      val request = FakeRequest(POST, "/register-country-by-country-reporting/registration/utr")
        .withHeaders(CONTENT_TYPE -> "application/json")
        .withBody(Json.obj("someField" -> "someValue"))

      val app = unauthorisedApp()

      running(app) {
        val result = route(app, request).getOrElse {
          fail("Route not defined.")
        }

        status(result) should (be(UNAUTHORIZED) or be(FORBIDDEN))
      }
    }

    "return 200 OK when authorised" in {

      val jsonBody = Json.parse(validRegistrationWithIdRequest)

      val request = FakeRequest(POST, "/register-country-by-country-reporting/registration/utr")
        .withHeaders(CONTENT_TYPE -> "application/json")
        .withBody(jsonBody)

      val app = authorisedApp()

      running(app) {
        val result = route(app, request).getOrElse(fail("Route not defined"))

        status(result) shouldBe OK
      }
    }

  }

}
