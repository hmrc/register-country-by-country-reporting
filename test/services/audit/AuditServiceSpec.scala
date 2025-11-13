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

package services.audit

import base.SpecBase
import config.AppConfig
import generators.Generators
import models.audit.Audit
import models.subscription.request.RequestDetail
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.{inject, Application}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuditServiceSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val mockAuditConnector = mock[AuditConnector]
  override lazy val app: Application = applicationBuilder()
    .overrides(inject.bind[AuditConnector].toInstance(mockAuditConnector))
    .build()
  private lazy val auditService = app.injector.instanceOf[AuditService]
  private lazy val appConfig    = app.injector.instanceOf[AppConfig]

  private val auditEventArgCaptor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

  "Audit Service" - {
    "should send audit event" in {
      forAll { (eventName: String, createSubscriptionRequestDetail: RequestDetail) =>
        val eventDetail = Json.toJson(createSubscriptionRequestDetail)

        when(mockAuditConnector.sendExtendedEvent(auditEventArgCaptor.capture())(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(AuditResult.Success))

        noException should be thrownBy auditService.sendAuditEvent(Audit(eventName, eventDetail)).futureValue

        val event = auditEventArgCaptor.getValue
        event.auditSource mustBe appConfig.appName
        event.auditType mustBe eventName
        event.detail mustBe eventDetail
        event.tags mustBe AuditExtensions.auditHeaderCarrier(hc).toAuditDetails()
      }
    }

    "should not fail when audit connector returns Disabled" in {
      forAll { (eventName: String, createSubscriptionRequestDetail: RequestDetail) =>
        when(mockAuditConnector.sendExtendedEvent(any[ExtendedDataEvent])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(AuditResult.Disabled))

        noException should be thrownBy auditService
          .sendAuditEvent(Audit(eventName, Json.toJson(createSubscriptionRequestDetail)))
          .futureValue
      }
    }

    "should not fail when audit connector returns a Failure" in {
      forAll { (eventName: String, createSubscriptionRequestDetail: RequestDetail) =>
        when(mockAuditConnector.sendExtendedEvent(any[ExtendedDataEvent])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(AuditResult.Failure("Audit failed")))

        noException should be thrownBy auditService
          .sendAuditEvent(Audit(eventName, Json.toJson(createSubscriptionRequestDetail)))
          .futureValue
      }
    }
  }
}
