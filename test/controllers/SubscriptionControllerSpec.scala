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

package controllers

import base.SpecBase
import connectors.SubscriptionConnector
import controllers.auth.{FakeIdentifierAuthAction, IdentifierAuthAction}
import generators.Generators
import models.audit.{AuditType, SubscriptionAuditDetails}
import models.subscription.request.CreateSubscriptionForCBCRequest
import models.subscription.{CreateSubscriptionResponse, DisplaySubscriptionForCBCRequest}
import models.{ErrorDetail, ErrorDetails, SafeId, SourceFaultDetail}
import org.mockito.ArgumentMatchers.{any, eq => mEq}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.audit.AuditService
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.time.ZonedDateTime
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionControllerSpec extends SpecBase with BeforeAndAfterEach with Generators with ScalaCheckPropertyChecks {

  val mockAuthConnector: AuthConnector = mock[AuthConnector]
  val mockAuditService: AuditService   = mock[AuditService]

  val mockSubscriptionConnector: SubscriptionConnector =
    mock[SubscriptionConnector]

  val application: Application = applicationBuilder()
    .overrides(
      bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
      bind[AuthConnector].toInstance(mockAuthConnector),
      bind[AuditService].toInstance(mockAuditService),
      bind[IdentifierAuthAction].to[FakeIdentifierAuthAction]
    )
    .build()

  override def beforeEach(): Unit = reset(mockAuthConnector, mockAuditService, mockSubscriptionConnector)

  "SubscriptionController" - {

    "createSubscription" - {
      "should create a subscription and send an audit event" in {
        forAll { (subscriptionRequest: CreateSubscriptionForCBCRequest, subscriptionResponse: CreateSubscriptionResponse) =>
          val subscriptionAuditDetails = SubscriptionAuditDetails
            .fromSubscriptionRequestAndResponse(subscriptionRequest, subscriptionResponse)

          val subscriptionEventDetail = Json.toJson(subscriptionAuditDetails)

          when(mockSubscriptionConnector.sendSubscriptionInformation(mEq(subscriptionRequest))(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(HttpResponse(OK, Json.toJson(subscriptionResponse), Map.empty)))

          when(mockAuditService.sendAuditEvent(mEq(AuditType.SubscriptionEvent), mEq(subscriptionEventDetail))(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.unit)

          val request = FakeRequest(POST, routes.SubscriptionController.createSubscription.url)
            .withJsonBody(Json.toJson(subscriptionRequest))

          val result = route(application, request).value
          status(result) mustEqual OK
        }
      }

      "should return OK but not send audit when subscription request returns OK status but response could not be validated" in {
        forAll { subscriptionRequest: CreateSubscriptionForCBCRequest =>
          when(mockSubscriptionConnector.sendSubscriptionInformation(mEq(subscriptionRequest))(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(HttpResponse(OK, Json.parse("{}"), Map.empty)))
          when(mockAuditService.sendAuditEvent(mEq(AuditType.SubscriptionEvent), any[JsValue])(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.unit)

          val request = FakeRequest(POST, routes.SubscriptionController.createSubscription.url)
            .withJsonBody(Json.toJson(subscriptionRequest))

          val result = route(application, request).value
          status(result) mustEqual OK
        }
      }

      "should return BAD_REQUEST when subscriptionForCBCRequest is invalid" in {
        when(
          mockSubscriptionConnector
            .sendSubscriptionInformation(
              any[CreateSubscriptionForCBCRequest]()
            )(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(400, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        val request =
          FakeRequest(
            POST,
            routes.SubscriptionController.createSubscription.url
          )
            .withJsonBody(Json.parse("""{"value": "field"}"""))

        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        verifyZeroInteractions(mockAuditService)
      }

      "should return BAD_REQUEST when one is encountered" in {
        when(
          mockSubscriptionConnector
            .sendSubscriptionInformation(
              any[CreateSubscriptionForCBCRequest]()
            )(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(400, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        when(mockAuditService.sendAuditEvent(mEq(AuditType.SubscriptionEvent), any[JsValue])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.unit)

        forAll(arbitrary[CreateSubscriptionForCBCRequest]) { subscriptionForCBCRequest =>
          val request =
            FakeRequest(
              POST,
              routes.SubscriptionController.createSubscription.url
            )
              .withJsonBody(Json.toJson(subscriptionForCBCRequest))

          val result = route(application, request).value
          status(result) mustEqual BAD_REQUEST
        }
      }

      "should return FORBIDDEN when authorisation is invalid" in {
        when(
          mockSubscriptionConnector
            .sendSubscriptionInformation(
              any[CreateSubscriptionForCBCRequest]()
            )(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(403, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        when(mockAuditService.sendAuditEvent(mEq(AuditType.SubscriptionEvent), any[JsValue])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.unit)

        forAll(arbitrary[CreateSubscriptionForCBCRequest]) { subscriptionForCBCRequest =>
          val request =
            FakeRequest(
              POST,
              routes.SubscriptionController.createSubscription.url
            )
              .withJsonBody(Json.toJson(subscriptionForCBCRequest))

          val result = route(application, request).value
          status(result) mustEqual FORBIDDEN
        }
      }

      "should return SERVICE_UNAVAILABLE when EIS becomes unavailable" in {
        when(
          mockSubscriptionConnector
            .sendSubscriptionInformation(
              any[CreateSubscriptionForCBCRequest]()
            )(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(503, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        when(mockAuditService.sendAuditEvent(mEq(AuditType.SubscriptionEvent), any[JsValue])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.unit)

        forAll(arbitrary[CreateSubscriptionForCBCRequest]) { subscriptionForCBCRequest =>
          val request =
            FakeRequest(
              POST,
              routes.SubscriptionController.createSubscription.url
            )
              .withJsonBody(Json.toJson(subscriptionForCBCRequest))

          val result = route(application, request).value
          status(result) mustEqual SERVICE_UNAVAILABLE
        }
      }

      "should return INTERNAL_SERVER_ERROR when EIS fails" in {
        when(
          mockSubscriptionConnector
            .sendSubscriptionInformation(
              any[CreateSubscriptionForCBCRequest]()
            )(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(
                BAD_GATEWAY,
                Json.obj(),
                Map.empty[String, Seq[String]]
              )
            )
          )

        when(mockAuditService.sendAuditEvent(mEq(AuditType.SubscriptionEvent), any[JsValue])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.unit)

        forAll(arbitrary[CreateSubscriptionForCBCRequest]) { subscriptionForCBCRequest =>
          val request =
            FakeRequest(
              POST,
              routes.SubscriptionController.createSubscription.url
            )
              .withJsonBody(Json.toJson(subscriptionForCBCRequest))

          val result = route(application, request).value
          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }

      "should return CONFLICT when one occurs" in {
        val errorDetails = ErrorDetails(
          ErrorDetail(
            ZonedDateTime.now().toString,
            Some("xx"),
            "409",
            "CONFLICT",
            "",
            Some(SourceFaultDetail(Seq("a", "b")))
          )
        )
        when(
          mockSubscriptionConnector
            .sendSubscriptionInformation(
              any[CreateSubscriptionForCBCRequest]()
            )(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(
                409,
                Json.toJson(errorDetails),
                Map.empty[String, Seq[String]]
              )
            )
          )

        when(mockAuditService.sendAuditEvent(mEq(AuditType.SubscriptionEvent), any[JsValue])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.unit)

        forAll(arbitrary[CreateSubscriptionForCBCRequest]) { subscriptionForCBCRequest =>
          val request =
            FakeRequest(
              POST,
              routes.SubscriptionController.createSubscription.url
            )
              .withJsonBody(Json.toJson(subscriptionForCBCRequest))

          val result = route(application, request).value
          status(result) mustEqual CONFLICT
        }
      }

      "should return NOT_FOUND for unspecified errors" in {
        when(
          mockSubscriptionConnector
            .sendSubscriptionInformation(
              any[CreateSubscriptionForCBCRequest]()
            )(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(404, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        when(mockAuditService.sendAuditEvent(mEq(AuditType.SubscriptionEvent), any[JsValue])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.unit)

        forAll(arbitrary[CreateSubscriptionForCBCRequest]) { subscriptionForCBCRequest =>
          val request =
            FakeRequest(
              POST,
              routes.SubscriptionController.createSubscription.url
            )
              .withJsonBody(Json.toJson(subscriptionForCBCRequest))

          val result = route(application, request).value
          status(result) mustEqual NOT_FOUND
        }
      }

      "downstream errors should be recoverable when not in json" in {
        when(
          mockSubscriptionConnector
            .sendSubscriptionInformation(
              any[CreateSubscriptionForCBCRequest]()
            )(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(503, "Not Available", Map.empty[String, Seq[String]])
            )
          )

        when(mockAuditService.sendAuditEvent(mEq(AuditType.SubscriptionEvent), any[JsValue])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.unit)

        forAll(arbitrary[CreateSubscriptionForCBCRequest]) { subscriptionForCBCRequest =>
          val request =
            FakeRequest(
              POST,
              routes.SubscriptionController.createSubscription.url
            )
              .withJsonBody(Json.toJson(subscriptionForCBCRequest))

          val result = route(application, request).value
          status(result) mustEqual SERVICE_UNAVAILABLE
        }
      }
    }

    "readSubscription" - {

      val safeId = SafeId("safeId")
      "should return BAD_REQUEST when DisplaySubscriptionForCBCRequest is invalid" in {
        when(
          mockSubscriptionConnector
            .readSubscriptionInformation(
              any[DisplaySubscriptionForCBCRequest]()
            )(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(400, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        val request =
          FakeRequest(
            POST,
            routes.SubscriptionController.readSubscription(safeId).url
          )
            .withJsonBody(Json.parse("""{"value": "field"}"""))

        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }

      "should return BAD_REQUEST when one is encountered" in {
        when(
          mockSubscriptionConnector
            .readSubscriptionInformation(
              any[DisplaySubscriptionForCBCRequest]()
            )(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(400, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[DisplaySubscriptionForCBCRequest]) { displaySubscriptionForCBCRequest =>
          val request =
            FakeRequest(
              POST,
              routes.SubscriptionController.readSubscription(safeId).url
            )
              .withJsonBody(Json.toJson(displaySubscriptionForCBCRequest))

          val result = route(application, request).value
          status(result) mustEqual BAD_REQUEST
        }
      }

      "should return FORBIDDEN when authorisation is invalid" in {
        when(
          mockSubscriptionConnector
            .readSubscriptionInformation(
              any[DisplaySubscriptionForCBCRequest]()
            )(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(403, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[DisplaySubscriptionForCBCRequest]) { displaySubscriptionForCBCRequest =>
          val request =
            FakeRequest(
              POST,
              routes.SubscriptionController.readSubscription(safeId).url
            )
              .withJsonBody(Json.toJson(displaySubscriptionForCBCRequest))

          val result = route(application, request).value
          status(result) mustEqual FORBIDDEN
        }
      }

      "should return SERVICE_UNAVAILABLE when EIS becomes unavailable" in {
        when(
          mockSubscriptionConnector
            .readSubscriptionInformation(
              any[DisplaySubscriptionForCBCRequest]()
            )(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(503, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[DisplaySubscriptionForCBCRequest]) { displaySubscriptionForCBCRequest =>
          val request =
            FakeRequest(
              POST,
              routes.SubscriptionController.readSubscription(safeId).url
            )
              .withJsonBody(Json.toJson(displaySubscriptionForCBCRequest))

          val result = route(application, request).value
          status(result) mustEqual SERVICE_UNAVAILABLE
        }
      }

      "should return INTERNAL_SERVER_ERROR when EIS fails" in {
        when(
          mockSubscriptionConnector
            .readSubscriptionInformation(
              any[DisplaySubscriptionForCBCRequest]()
            )(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(
                BAD_GATEWAY,
                Json.obj(),
                Map.empty[String, Seq[String]]
              )
            )
          )

        forAll(arbitrary[DisplaySubscriptionForCBCRequest]) { displaySubscriptionForCBCRequest =>
          val request =
            FakeRequest(
              POST,
              routes.SubscriptionController.readSubscription(safeId).url
            )
              .withJsonBody(Json.toJson(displaySubscriptionForCBCRequest))

          val result = route(application, request).value
          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }

      "should return NOT_FOUND for unspecified errors" in {
        when(
          mockSubscriptionConnector
            .readSubscriptionInformation(
              any[DisplaySubscriptionForCBCRequest]()
            )(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(404, Json.obj(), Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[DisplaySubscriptionForCBCRequest]) { displaySubscriptionForCBCRequest =>
          val request =
            FakeRequest(
              POST,
              routes.SubscriptionController.readSubscription(safeId).url
            )
              .withJsonBody(Json.toJson(displaySubscriptionForCBCRequest))

          val result = route(application, request).value
          status(result) mustEqual NOT_FOUND
        }
      }

      "downstream errors should be recoverable when not in json" in {
        when(
          mockSubscriptionConnector
            .readSubscriptionInformation(
              any[DisplaySubscriptionForCBCRequest]()
            )(
              any[HeaderCarrier](),
              any[ExecutionContext]()
            )
        )
          .thenReturn(
            Future.successful(
              HttpResponse(503, "Not Available", Map.empty[String, Seq[String]])
            )
          )

        forAll(arbitrary[DisplaySubscriptionForCBCRequest]) { displaySubscriptionForCBCRequest =>
          val request =
            FakeRequest(
              POST,
              routes.SubscriptionController.readSubscription(safeId).url
            )
              .withJsonBody(Json.toJson(displaySubscriptionForCBCRequest))

          val result = route(application, request).value
          status(result) mustEqual SERVICE_UNAVAILABLE
        }
      }
    }
  }
}
