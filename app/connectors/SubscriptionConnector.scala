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

import com.google.inject.Inject
import config.AppConfig
import models.subscription.DisplaySubscriptionForCBCRequest
import models.subscription.request.CreateSubscriptionForCBCRequest
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionConnector @Inject() (
    val config: AppConfig,
    val http: HttpClient
) {

  def sendSubscriptionInformation(
      subscription: CreateSubscriptionForCBCRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val serviceName = "create-subscription"
    http.POST[CreateSubscriptionForCBCRequest, HttpResponse](
      config.baseUrl(serviceName),
      subscription,
      headers = extraHeaders(config, serviceName)
    )(
      wts = CreateSubscriptionForCBCRequest.format,
      rds = httpReads,
      hc = hc,
      ec = ec
    )
  }

  def readSubscriptionInformation(
      subscription: DisplaySubscriptionForCBCRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val serviceName = "read-subscription"
    http.POST[DisplaySubscriptionForCBCRequest, HttpResponse](
      config.baseUrl(serviceName),
      subscription,
      headers = extraHeaders(config, serviceName)
    )(
      wts = DisplaySubscriptionForCBCRequest.format,
      rds = httpReads,
      hc = hc,
      ec = ec
    )
  }

}
