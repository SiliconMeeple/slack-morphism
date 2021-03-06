/*
 * Copyright 2019 Abdulla Abdurakhmanov (abdulla@latestbit.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package org.latestbit.slack.morphism.client.streaming

import org.latestbit.slack.morphism.client.SlackApiClientError

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration._

/**
 * Lazy collection support for Scala 2.12 based on Stream
 * @tparam IT item type
 * @tparam PT position/cursor type
 */
trait LazyScalaCollectionSupport[IT, PT] { self: SlackApiResponseScroller[IT, PT] =>

  type SyncStreamType = Stream[IT]

  type AsyncItemType = Either[SlackApiClientError, SlackApiScrollableResponse[IT, PT]]
  type AsyncValueType = Either[SlackApiClientError, Iterable[IT]]

  /**
   * Lazy load data synchronously batching using standard lazy container
   *
   * @param scrollerTimeout timeout to receive next batch
   * @return lazy stream of data
   */
  def toSyncScroller(
      scrollerTimeout: FiniteDuration = 60.seconds
  )( implicit ec: ExecutionContext ): Future[Either[SlackApiClientError, SyncStreamType]] = {

    def loadNext( scrollableResp: SlackApiScrollableResponse[IT, PT] ): SyncStreamType = {
      val loadedStream = scrollableResp.items.toStream
      scrollableResp.getLatestPos
        .map { latestPos =>
          lazy val scrollNext: SyncStreamType =
            Await
              .result(
                self.next( latestPos ),
                scrollerTimeout
              )
              .toOption
              .map( loadNext )
              .getOrElse( Stream.empty )

          loadedStream #::: scrollNext
        }
        .getOrElse( loadedStream )
    }

    self.first().map( _.map( loadNext ) )
  }
}
