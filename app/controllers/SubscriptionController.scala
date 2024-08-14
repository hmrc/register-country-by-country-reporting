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

import com.google.inject.Inject
import config.AppConfig
import connectors.SubscriptionConnector
import controllers.auth.AuthAction
import models.SafeId
import models.audit.{AuditType, SubscriptionAuditDetails}
import models.subscription.request.CreateSubscriptionForCBCRequest
import models.subscription.{CreateSubscriptionResponse, DisplaySubscriptionForCBCRequest}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.api.{Logger, Logging}
import services.audit.AuditService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionController @Inject() (
  val config: AppConfig,
  authenticate: AuthAction,
  subscriptionConnector: SubscriptionConnector,
  auditService: AuditService,
  override val controllerComponents: ControllerComponents
)(implicit executionContext: ExecutionContext)
    extends BackendController(controllerComponents)
    with Logging {

  implicit private val logging: Logger = logger

  def createSubscription: Action[JsValue] = authenticate(parse.json).async { implicit request =>
    request.body
      .validate[CreateSubscriptionForCBCRequest]
      .fold(
        invalid = _ => Future.successful(BadRequest("CreateSubscriptionForCBCRequest is invalid")),
        valid = subscriptionRequest =>
          for {
            response <- subscriptionConnector.sendSubscriptionInformation(subscriptionRequest)
            _        <- if (response.status == OK) sendAuditEvent(subscriptionRequest, response) else Future.unit
          } yield convertToResult(response)
      )
  }

  def readSubscription(safeId: SafeId): Action[AnyContent] =
    authenticate.async { implicit request =>
        subscriptionConnector
          .readSubscriptionInformation(DisplaySubscriptionForCBCRequest(safeId))
          .map(convertToResult(_))
    }

  private def sendAuditEvent(
    subscriptionRequest: CreateSubscriptionForCBCRequest,
    subscriptionResponse: HttpResponse
  )(implicit hc: HeaderCarrier): Future[Unit] =
    subscriptionResponse.json
      .validate[CreateSubscriptionResponse]
      .fold(
        invalid = _ => {
          logger.warn("Failed to validate subscription create response")
          Future.unit
        },
        valid = subscriptionResponse => {
          val auditEventDetail = SubscriptionAuditDetails.fromSubscriptionRequestAndResponse(subscriptionRequest, subscriptionResponse)
          auditService.sendAuditEvent(AuditType.SubscriptionEvent, Json.toJson(auditEventDetail))
        }
      )

}
