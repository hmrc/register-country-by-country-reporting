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

import config.AppConfig
import play.api.Logging
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuditService @Inject() (
  appConfig: AppConfig,
  auditConnector: AuditConnector
) extends Logging {

  def sendAuditEvent(eventName: String, detail: JsValue)(implicit
    hc: HeaderCarrier,
    ex: ExecutionContext
  ): Future[Unit] =
    auditConnector
      .sendExtendedEvent(
        ExtendedDataEvent(
          auditSource = appConfig.appName,
          auditType = eventName,
          detail = detail,
          tags = AuditExtensions.auditHeaderCarrier(hc).toAuditDetails()
        )
      )
      .map { auditResult: AuditResult =>
        auditResult match {
          case Failure(msg, _) =>
            logger.warn(s"Failed to send audit event $eventName: $msg")
          case Disabled =>
            logger.warn(s"Failed to send audit event $eventName: Auditing is disabled")
          case Success =>
            logger.info(s"Audit event $eventName sent")
          case unexpected =>
            logger.warn(s"Unexpected audit result $unexpected received for event $eventName")
        }
      }
}
