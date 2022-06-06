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

package models

import play.api.libs.json.{JsString, Reads, Writes, __}
import play.api.mvc.PathBindable

case class SafeId(value: String)

object SafeId {

  implicit def reads: Reads[SafeId] = __.read[String] map SafeId.apply

  implicit def writes: Writes[SafeId] = Writes(safeId => JsString(safeId.value))

  implicit lazy val pathBindable: PathBindable[SafeId] =
    new PathBindable[SafeId] {
      override def bind(key: String, value: String): Either[String, SafeId] =
        implicitly[PathBindable[String]].bind(key, value).right.map(SafeId(_))

      override def unbind(key: String, value: SafeId): String =
        value.value
    }
}
