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

package controllers

import base.SpecBase
import connectors.RegistrationConnector
import controllers.auth.{AuthAction, FakeAuthAction}
import generators.Generators
import models._
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.any
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, status, POST, _}
import play.api.{Application, Configuration}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class RegistrationControllerSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {
  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  val mockRegistrationConnector: RegistrationConnector =
    mock[RegistrationConnector]

  val application: Application = new GuiceApplicationBuilder()
    .configure(
      Configuration("metrics.enabled" -> "false", "auditing.enabled" -> false)
    )
    .overrides(
      bind[RegistrationConnector].toInstance(mockRegistrationConnector),
      bind[AuthConnector].toInstance(mockAuthConnector),
      bind[AuthAction].to[FakeAuthAction]
    )
    .build()

  val routeWithoutID: String = routes.RegistrationController.withoutOrgID.url

  val routeWithID = routes.RegistrationController.withOrgUTR.url

  "Registration Controller" - {
    "for a user without id" - {
      "should send data and return ok" in {
        when(
          mockRegistrationConnector
            .sendWithoutIDInformation(any[RegisterWithoutId]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(200, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[RegisterWithoutId]) { individualNoIdRegistration =>
          val request =
            FakeRequest(
              POST,
              routeWithoutID
            )
              .withJsonBody(Json.toJson(individualNoIdRegistration))

          val result = route(application, request).value
          status(result) mustEqual OK
        }
      }

      "should return bad request when one is encountered" in {
        when(
          mockRegistrationConnector
            .sendWithoutIDInformation(any[RegisterWithoutId]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(400, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[RegisterWithoutId]) { individualNoIdSubscription =>
          val request =
            FakeRequest(
              POST,
              routeWithoutID
            )
              .withJsonBody(Json.toJson(individualNoIdSubscription))

          val result = route(application, request).value
          status(result) mustEqual BAD_REQUEST
        }
      }

      "should return bad request when Json cannot be validated" in {
        when(
          mockRegistrationConnector
            .sendWithoutIDInformation(any[RegisterWithoutId]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(200, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[RegisterWithoutId]) { _ =>
          val request =
            FakeRequest(
              POST,
              routeWithoutID
            )
              .withJsonBody(Json.parse("""{"value": "field"}"""))

          val result = route(application, request).value
          status(result) mustEqual BAD_REQUEST
        }
      }

      "should return not found when one is encountered" in {
        when(
          mockRegistrationConnector
            .sendWithoutIDInformation(any[RegisterWithoutId]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(404, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[RegisterWithoutId]) { individualNoIdSubscription =>
          val request =
            FakeRequest(
              POST,
              routeWithoutID
            )
              .withJsonBody(Json.toJson(individualNoIdSubscription))

          val result = route(application, request).value
          status(result) mustEqual NOT_FOUND
        }
      }

      "should return forbidden error when authorisation is invalid" in {
        when(
          mockRegistrationConnector
            .sendWithoutIDInformation(any[RegisterWithoutId]())(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(403, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[RegisterWithoutId]) { individualNoIdRegistration =>
          val request =
            FakeRequest(
              POST,
              routeWithoutID
            )
              .withJsonBody(Json.toJson(individualNoIdRegistration))

          val result = route(application, request).value
          status(result) mustEqual FORBIDDEN
        }
      }
    }

    "for a user with id" - {
      "should send data and return ok" in {
        when(
          mockRegistrationConnector.sendWithID(any[RegisterWithID]())(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(200, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[RegisterWithID]) { withIDRegistration =>
          val request =
            FakeRequest(
              POST,
              routeWithID
            )
              .withJsonBody(Json.toJson(withIDRegistration))

          val result = route(application, request).value
          status(result) mustEqual OK
        }
      }

      "should return bad request when one is encountered" in {
        when(
          mockRegistrationConnector.sendWithID(any[RegisterWithID]())(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(400, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[RegisterWithID]) { withIdSubscription =>
          val request =
            FakeRequest(
              POST,
              routeWithID
            )
              .withJsonBody(Json.toJson(withIdSubscription))

          val result = route(application, request).value
          status(result) mustEqual BAD_REQUEST
        }
      }

      "should return bad request when Json cannot be validated" in {
        when(
          mockRegistrationConnector.sendWithID(any[RegisterWithID]())(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(200, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[RegisterWithID]) { _ =>
          val request =
            FakeRequest(
              POST,
              routeWithID
            )
              .withJsonBody(Json.parse("""{"value": "field"}"""))

          val result = route(application, request).value
          status(result) mustEqual BAD_REQUEST
        }
      }

      "should return not found when one is encountered" in {
        when(
          mockRegistrationConnector.sendWithID(any[RegisterWithID]())(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(404, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[RegisterWithID]) { withIdSubscription =>
          val request =
            FakeRequest(
              POST,
              routeWithID
            )
              .withJsonBody(Json.toJson(withIdSubscription))

          val result = route(application, request).value
          status(result) mustEqual NOT_FOUND
        }
      }

      "should return forbidden error when authorisation is invalid" in {
        val errorDetails = ErrorDetails(
          ErrorDetail(
            DateTime.now().toString,
            Some("xx"),
            "403",
            "FORBIDDEN",
            "",
            Some(SourceFaultDetail(Seq("a", "b")))
          )
        )
        when(
          mockRegistrationConnector.sendWithID(any[RegisterWithID]())(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(
                403,
                Json.toJson(errorDetails),
                Map.empty[String, Seq[String]]
              )
            )
          )

        forAll(arbitrary[RegisterWithID]) { withIdSubscription =>
          val request =
            FakeRequest(
              POST,
              routeWithID
            )
              .withJsonBody(Json.toJson(withIdSubscription))

          val result = route(application, request).value
          status(result) mustEqual FORBIDDEN
        }
      }

      "downstream errors should be recoverable when not in json" in {
        when(
          mockRegistrationConnector.sendWithID(any[RegisterWithID]())(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(503, "Not Available", Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[RegisterWithID]) { withIdSubscription =>
          val request =
            FakeRequest(
              POST,
              routeWithID
            )
              .withJsonBody(Json.toJson(withIdSubscription))

          val result = route(application, request).value
          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}
