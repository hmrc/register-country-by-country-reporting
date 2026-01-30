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

package controllers.auth

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.UNAUTHORIZED
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.*
import play.api.mvc.Results.Status
import play.api.test.FakeRequest
import play.api.{Application, Configuration}
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class IdentifierAuthActionSpec extends AnyWordSpec with GuiceOneAppPerSuite with MockitoSugar with BeforeAndAfterEach with ScalaFutures {
  val mockAuthConnector: AuthConnector   = mock[AuthConnector]
  implicit lazy val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  class Harness extends IdentifierAuthAction(mockAuthConnector)(ec) {
    def actionFilter(): Future[Option[Result]] = filter(FakeRequest())
  }

  val application: Application = new GuiceApplicationBuilder()
    .configure(
      Configuration("metrics.enabled" -> "false", "enrolmentKeys.cbc.key" -> "HMRC-CBC-ORG", "auditing.enabled" -> false)
    )
    .overrides(
      bind[AuthConnector].toInstance(mockAuthConnector)
    )
    .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector)
  }

  "Identifier Auth Action" when {
    "the user has no valid auth token" must {
      "must return unauthorised" in {
        when(mockAuthConnector.authorise(any[Predicate](), eqTo(Retrievals.affinityGroup))(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.failed(new MissingBearerToken))
        val result = new Harness().actionFilter()
        result.futureValue mustBe Some(Status(UNAUTHORIZED))
      }
    }

    "the user is logged in" must {
      "return ok for an Organisation using the service" in {
        when(mockAuthConnector.authorise(any[Predicate](), eqTo(Retrievals.affinityGroup))(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(AffinityGroup.Organisation)))
        val result = new Harness().actionFilter()
        result.futureValue mustBe None
      }

      "throw an error for a non-org affinity group" in {
        when(mockAuthConnector.authorise(any[Predicate](), eqTo(Retrievals.affinityGroup))(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(Some(AffinityGroup.Agent)))

        val result = new Harness().actionFilter()
        result.futureValue mustBe Some(Status(UNAUTHORIZED))
      }

    }
  }
}
