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

/*
 * Copyright 2022 HM Revenue & Customs
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

import com.google.inject.Inject
import config.AppConfig
import connectors.SubscriptionConnector
import controllers.auth.AuthAction
import models.SafeId
import models.subscription.DisplaySubscriptionForCBCRequest
import models.subscription.request.CreateSubscriptionForCBCRequest
import play.api.libs.json.{JsResult, JsValue}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.api.{Logger, Logging}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionController @Inject() (
  val config: AppConfig,
  authenticate: AuthAction,
  subscriptionConnector: SubscriptionConnector,
  override val controllerComponents: ControllerComponents
)(implicit executionContext: ExecutionContext)
    extends BackendController(controllerComponents)
    with Logging {

  def createSubscription: Action[JsValue] = authenticate(parse.json).async { implicit request =>
    val subscriptionSubmissionResult: JsResult[CreateSubscriptionForCBCRequest] =
      request.body.validate[CreateSubscriptionForCBCRequest]

    subscriptionSubmissionResult.fold(
      invalid = _ =>
        Future.successful(
          BadRequest("CreateSubscriptionForCBCRequest is invalid")
        ),
      valid = sub =>
        for {
          response <- subscriptionConnector.sendSubscriptionInformation(sub)
        } yield convertToResult(response)(implicitly[Logger](logger))
    )
  }

  def readSubscription(safeId: SafeId): Action[AnyContent] =
    authenticate.async { implicit request =>
      for {
        response <- subscriptionConnector.readSubscriptionInformation(
          DisplaySubscriptionForCBCRequest(safeId)
        )
      } yield convertToResult(response)(implicitly[Logger](logger))
    }

}
