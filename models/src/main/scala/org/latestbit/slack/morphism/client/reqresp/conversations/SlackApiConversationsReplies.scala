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

package org.latestbit.slack.morphism.client.reqresp.conversations

import org.latestbit.slack.morphism.client.streaming.SlackApiScrollableResponse
import org.latestbit.slack.morphism.common.SlackApiResponseMetadata
import org.latestbit.slack.morphism.messages.SlackMessage

/**
 * Request of https://api.slack.com/methods/conversations.replies
 */
case class SlackApiConversationsRepliesRequest(
    channel: String,
    ts: String,
    cursor: Option[String],
    limit: Option[Int] = None,
    inclusive: Option[Boolean] = None,
    latest: Option[String] = None,
    oldest: Option[String] = None
)

/**
 * Response of https://api.slack.com/methods/conversations.replies
 */
case class SlackApiConversationsRepliesResponse(
    messages: List[SlackMessage] = List(),
    has_more: Option[Boolean] = None,
    response_metadata: Option[SlackApiResponseMetadata] = None
) extends SlackApiScrollableResponse[SlackMessage, String] {

  override def items: List[SlackMessage] = messages
  override def getLatestPos: Option[String] = response_metadata.flatMap( _.next_cursor )

}
