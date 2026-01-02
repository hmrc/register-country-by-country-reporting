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

import config.AppConfig
import connectors.SubscriptionConnector
import controllers.auth.{IdentifierAuthAction, IdentifierAuthActionImpl, IdentifierRequest}
import org.apache.pekko.actor.ActorSystem
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{doAnswer, spy, when}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, BodyParsers, Request, Result}
import play.api.test.*
import play.api.test.Helpers.*
import play.api.{Application, Configuration, inject}
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.HttpVerbs.POST
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterAll {

  implicit val actorSystem: ActorSystem = ActorSystem()

  override protected def afterAll(): Unit = {
    actorSystem.terminate()
    super.afterAll()
  }

  private val validCreateSubscriptionRequest =
    """
      |{
      |  "createSubscriptionForCBCRequest": {
      |    "requestCommon": {
      |      "regime": "CBC",
      |      "receiptDate": "2025-01-01T00:00:00Z",
      |      "acknowledgementReference": "ABC123456789",
      |      "originatingSystem": "MDTP"
      |    },
      |    "requestDetail": {
      |      "IDType": "UTR",
      |      "IDNumber": "1234567890",
      |      "isGBUser": true,
      |      "primaryContact": {
      |        "organisation": {
      |          "organisationName": "Test Organisation Ltd"
      |        },
      |        "email": "test@example.com"
      |      }
      |    }
      |  }
      |}
      |""".stripMargin

  private def unauthorisedApp(): Application = new GuiceApplicationBuilder().build()

  private def authorisedApp(): Application = {

    val appConfig = new AppConfig(mock[Configuration], mock[ServicesConfig]) {
      override val enrolmentKey: String => String = _ => "HMRC-AGENT-AGENT"
    }

    val realAuthAction = new IdentifierAuthActionImpl(
      authConnector = mock[AuthConnector],
      parser = new BodyParsers.Default(),
      config = appConfig
    )

    val authAction = spy(realAuthAction)

    doAnswer { invocation =>
      val request = invocation.getArgument(0, classOf[Request[AnyContent]])

      val block = invocation.getArgument(1).asInstanceOf[IdentifierRequest[AnyContent] => Future[Result]]

      val identifierRequest =
        IdentifierRequest(
          request = request,
          affinityGroup = Agent,
          arn = None
        )

      block(identifierRequest)
    }.when(authAction).invokeBlock(any(), any())

    val mockSubscriptionConnector = mock[SubscriptionConnector]

    when(mockSubscriptionConnector.readSubscriptionInformation(any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "{}")))
    when(mockSubscriptionConnector.sendSubscriptionInformation(any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "{}")))

    new GuiceApplicationBuilder()
      .overrides(
        inject.bind[IdentifierAuthAction].toInstance(authAction),
        inject.bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
      )
      .build()
  }

  "POST /subscription/create-subscription" should {

    "reject unauthenticated requests" in {
      val request = FakeRequest(POST, "/register-country-by-country-reporting/subscription/create-subscription")
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

      val jsonBody = Json.parse(validCreateSubscriptionRequest)

      val request = FakeRequest(POST, "/register-country-by-country-reporting/subscription/create-subscription")
        .withHeaders(CONTENT_TYPE -> "application/json")
        .withBody(jsonBody)

      val app = authorisedApp()

      running(app) {
        val result = route(app, request).getOrElse(fail("Route not defined"))

        status(result) shouldBe OK
      }
    }
  }

  "POST /subscription/read-subscription/:safeId" should {

    "reject unauthenticated requests" in {
      val request = FakeRequest(POST, "/register-country-by-country-reporting/subscription/read-subscription/safeId")
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
      val request = FakeRequest(POST, "/register-country-by-country-reporting/subscription/read-subscription/safeId")
        .withHeaders(CONTENT_TYPE -> "application/json")
        .withBody(Json.obj("someField" -> "someValue"))

      val app = authorisedApp()

      running(app) {
        val result = route(app, request).getOrElse(fail("Route not defined"))

        status(result) shouldBe OK
      }
    }
  }
}
