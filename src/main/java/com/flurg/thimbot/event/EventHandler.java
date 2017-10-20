/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
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

package com.flurg.thimbot.event;

import com.flurg.thimbot.event.inbound.AccountChangeEvent;
import com.flurg.thimbot.event.inbound.AuthenticationChallengeEvent;
import com.flurg.thimbot.event.inbound.AuthenticationFailedEvent;
import com.flurg.thimbot.event.inbound.AuthenticationMechanismsEvent;
import com.flurg.thimbot.event.inbound.AuthenticationSuccessfulEvent;
import com.flurg.thimbot.event.inbound.CapabilityAckEvent;
import com.flurg.thimbot.event.inbound.CapabilityListEvent;
import com.flurg.thimbot.event.inbound.CapabilityNakEvent;
import com.flurg.thimbot.event.inbound.ChannelActionEvent;
import com.flurg.thimbot.event.inbound.ChannelCTCPCommandEvent;
import com.flurg.thimbot.event.inbound.ChannelCTCPResponseEvent;
import com.flurg.thimbot.event.inbound.ChannelJoinEvent;
import com.flurg.thimbot.event.inbound.ChannelKickEvent;
import com.flurg.thimbot.event.inbound.ChannelMessageEvent;
import com.flurg.thimbot.event.inbound.ChannelCurrentModeEvent;
import com.flurg.thimbot.event.inbound.ChannelNoTopicEvent;
import com.flurg.thimbot.event.inbound.ChannelNoticeEvent;
import com.flurg.thimbot.event.inbound.ChannelPartEvent;
import com.flurg.thimbot.event.inbound.ChannelRedirectEvent;
import com.flurg.thimbot.event.inbound.ChannelTimestampEvent;
import com.flurg.thimbot.event.inbound.ChannelTopicEvent;
import com.flurg.thimbot.event.inbound.ConnectEvent;
import com.flurg.thimbot.event.inbound.ConnectFailedEvent;
import com.flurg.thimbot.event.inbound.DisconnectCompleteEvent;
import com.flurg.thimbot.event.inbound.HangUpEvent;
import com.flurg.thimbot.event.inbound.ErrorEvent;
import com.flurg.thimbot.event.inbound.LoggedInEvent;
import com.flurg.thimbot.event.inbound.LoggedOutEvent;
import com.flurg.thimbot.event.inbound.MOTDEndEvent;
import com.flurg.thimbot.event.inbound.MOTDLineEvent;
import com.flurg.thimbot.event.inbound.NickChangeEvent;
import com.flurg.thimbot.event.inbound.PrivateActionEvent;
import com.flurg.thimbot.event.inbound.PrivateCTCPCommandEvent;
import com.flurg.thimbot.event.inbound.PrivateCTCPResponseEvent;
import com.flurg.thimbot.event.inbound.PrivateMessageEvent;
import com.flurg.thimbot.event.inbound.PrivateNoticeEvent;
import com.flurg.thimbot.event.inbound.QuitEvent;
import com.flurg.thimbot.event.inbound.SaslMechanismListEvent;
import com.flurg.thimbot.event.inbound.ServerPingEvent;
import com.flurg.thimbot.event.inbound.ServerPongEvent;
import com.flurg.thimbot.event.inbound.UserAwayEvent;
import com.flurg.thimbot.event.inbound.UserBackEvent;
import com.flurg.thimbot.event.inbound.UserPingEvent;
import com.flurg.thimbot.event.inbound.UserPongEvent;
import com.flurg.thimbot.event.inbound.WelcomeEvent;
import com.flurg.thimbot.event.outbound.AuthenticationRequestEvent;
import com.flurg.thimbot.event.outbound.AuthenticationResponseEvent;
import com.flurg.thimbot.event.outbound.CapabilityEndEvent;
import com.flurg.thimbot.event.outbound.CapabilityListRequestEvent;
import com.flurg.thimbot.event.outbound.CapabilityRequestEvent;
import com.flurg.thimbot.event.outbound.ChannelJoinRequestEvent;
import com.flurg.thimbot.event.outbound.ChannelModeRequestEvent;
import com.flurg.thimbot.event.outbound.ChannelPartRequestEvent;
import com.flurg.thimbot.event.outbound.ChannelTopicChangeRequestEvent;
import com.flurg.thimbot.event.outbound.ChannelTopicRequestEvent;
import com.flurg.thimbot.event.outbound.ConnectRequestEvent;
import com.flurg.thimbot.event.outbound.HangUpRequestEvent;
import com.flurg.thimbot.event.outbound.IRCUserEvent;
import com.flurg.thimbot.event.outbound.NickChangeRequestEvent;
import com.flurg.thimbot.event.outbound.OutboundActionEvent;
import com.flurg.thimbot.event.outbound.OutboundAwayRequestEvent;
import com.flurg.thimbot.event.outbound.OutboundBackRequestEvent;
import com.flurg.thimbot.event.outbound.OutboundCTCPCommandEvent;
import com.flurg.thimbot.event.outbound.OutboundCTCPResponseEvent;
import com.flurg.thimbot.event.outbound.OutboundMessageEvent;
import com.flurg.thimbot.event.outbound.OutboundNoticeEvent;
import com.flurg.thimbot.event.outbound.OutboundPingEvent;
import com.flurg.thimbot.event.outbound.OutboundPongEvent;
import com.flurg.thimbot.event.outbound.OutboundServerPingEvent;
import com.flurg.thimbot.event.outbound.OutboundServerPongEvent;
import com.flurg.thimbot.event.outbound.QuitRequestEvent;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public abstract class EventHandler {

    public void handleEvent(final EventHandlerContext context, final Event event) throws Exception {
        context.proceed();
    }

    public void handleEvent(final EventHandlerContext context, final AccountChangeEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final AuthenticationChallengeEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final AuthenticationFailedEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final AuthenticationRequestEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final AuthenticationResponseEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final AuthenticationSuccessfulEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final AuthenticationMechanismsEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final OutboundAwayRequestEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final OutboundBackRequestEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final CapabilityAckEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final CapabilityEndEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final CapabilityListEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final CapabilityListRequestEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final CapabilityNakEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final CapabilityRequestEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelActionEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelCTCPCommandEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelCTCPResponseEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelJoinEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelJoinRequestEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelMessageEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelNoticeEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelPartEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelKickEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelPartRequestEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelRedirectEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ConnectEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ConnectRequestEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final HangUpEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final HangUpRequestEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ErrorEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final IRCUserEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final LoggedInEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final LoggedOutEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final MOTDEndEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final MOTDLineEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final NickChangeEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final NickChangeRequestEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final OutboundActionEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final OutboundCTCPCommandEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final OutboundCTCPResponseEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final OutboundMessageEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final OutboundNoticeEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final OutboundPingEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final OutboundPongEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final OutboundServerPingEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final OutboundServerPongEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final PrivateActionEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final PrivateCTCPCommandEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final PrivateCTCPResponseEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final PrivateMessageEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final PrivateNoticeEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final QuitEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final QuitRequestEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final SaslMechanismListEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ServerPingEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ServerPongEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final UserAwayEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final UserBackEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final UserPingEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final UserPongEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelTopicEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelNoTopicEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelTimestampEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelTopicRequestEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelTopicChangeRequestEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelModeRequestEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelCurrentModeEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final WelcomeEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final DisconnectCompleteEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ConnectFailedEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }
}
