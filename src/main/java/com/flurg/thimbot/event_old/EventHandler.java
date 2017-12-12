/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.flurg.thimbot.event_old;

import com.flurg.thimbot.event_old.inbound.AccountChangeEvent;
import com.flurg.thimbot.event_old.inbound.AuthenticationChallengeEvent;
import com.flurg.thimbot.event_old.inbound.AuthenticationFailedEvent;
import com.flurg.thimbot.event_old.inbound.AuthenticationMechanismsEvent;
import com.flurg.thimbot.event_old.inbound.AuthenticationSuccessfulEvent;
import com.flurg.thimbot.event_old.inbound.CapabilityAckEvent;
import com.flurg.thimbot.event_old.inbound.CapabilityListEvent;
import com.flurg.thimbot.event_old.inbound.CapabilityNakEvent;
import com.flurg.thimbot.event_old.inbound.ChannelActionEvent;
import com.flurg.thimbot.event_old.inbound.ChannelCTCPCommandEvent;
import com.flurg.thimbot.event_old.inbound.ChannelCTCPResponseEvent;
import com.flurg.thimbot.event_old.inbound.ChannelJoinEvent;
import com.flurg.thimbot.event_old.inbound.ChannelKickEvent;
import com.flurg.thimbot.event_old.inbound.ChannelMessageEvent;
import com.flurg.thimbot.event_old.inbound.ChannelCurrentModeEvent;
import com.flurg.thimbot.event_old.inbound.ChannelNoTopicEvent;
import com.flurg.thimbot.event_old.inbound.ChannelNoticeEvent;
import com.flurg.thimbot.event_old.inbound.ChannelPartEvent;
import com.flurg.thimbot.event_old.inbound.ChannelRedirectEvent;
import com.flurg.thimbot.event_old.inbound.ChannelTimestampEvent;
import com.flurg.thimbot.event_old.inbound.ChannelTopicEvent;
import com.flurg.thimbot.event_old.inbound.ConnectEvent;
import com.flurg.thimbot.event_old.inbound.ConnectFailedEvent;
import com.flurg.thimbot.event_old.inbound.DisconnectCompleteEvent;
import com.flurg.thimbot.event_old.inbound.HangUpEvent;
import com.flurg.thimbot.event_old.inbound.ErrorEvent;
import com.flurg.thimbot.event_old.inbound.LoggedInEvent;
import com.flurg.thimbot.event_old.inbound.LoggedOutEvent;
import com.flurg.thimbot.event_old.inbound.MOTDEndEvent;
import com.flurg.thimbot.event_old.inbound.MOTDLineEvent;
import com.flurg.thimbot.event_old.inbound.NickChangeEvent;
import com.flurg.thimbot.event_old.inbound.PrivateActionEvent;
import com.flurg.thimbot.event_old.inbound.PrivateCTCPCommandEvent;
import com.flurg.thimbot.event_old.inbound.PrivateCTCPResponseEvent;
import com.flurg.thimbot.event_old.inbound.PrivateMessageEvent;
import com.flurg.thimbot.event_old.inbound.PrivateNoticeEvent;
import com.flurg.thimbot.event_old.inbound.QuitEvent;
import com.flurg.thimbot.event_old.inbound.SaslMechanismListEvent;
import com.flurg.thimbot.event_old.inbound.ServerPingEvent;
import com.flurg.thimbot.event_old.inbound.ServerPongEvent;
import com.flurg.thimbot.event_old.inbound.UserAwayEvent;
import com.flurg.thimbot.event_old.inbound.UserBackEvent;
import com.flurg.thimbot.event_old.inbound.UserPingEvent;
import com.flurg.thimbot.event_old.inbound.UserPongEvent;
import com.flurg.thimbot.event_old.inbound.WelcomeEvent;
import com.flurg.thimbot.event_old.outbound.AuthenticationRequestEvent;
import com.flurg.thimbot.event_old.outbound.AuthenticationResponseEvent;
import com.flurg.thimbot.event_old.outbound.CapabilityEndEvent;
import com.flurg.thimbot.event_old.outbound.CapabilityListRequestEvent;
import com.flurg.thimbot.event_old.outbound.CapabilityRequestEvent;
import com.flurg.thimbot.event_old.outbound.ChannelJoinRequestEvent;
import com.flurg.thimbot.event_old.outbound.ChannelModeRequestEvent;
import com.flurg.thimbot.event_old.outbound.ChannelPartRequestEvent;
import com.flurg.thimbot.event_old.outbound.ChannelTopicChangeRequestEvent;
import com.flurg.thimbot.event_old.outbound.ChannelTopicRequestEvent;
import com.flurg.thimbot.event_old.outbound.ConnectRequestEvent;
import com.flurg.thimbot.event_old.outbound.HangUpRequestEvent;
import com.flurg.thimbot.event_old.outbound.IRCUserEvent;
import com.flurg.thimbot.event_old.outbound.NickChangeRequestEvent;
import com.flurg.thimbot.event_old.outbound.OutboundActionEvent;
import com.flurg.thimbot.event_old.outbound.OutboundAwayRequestEvent;
import com.flurg.thimbot.event_old.outbound.OutboundBackRequestEvent;
import com.flurg.thimbot.event_old.outbound.OutboundCTCPCommandEvent;
import com.flurg.thimbot.event_old.outbound.OutboundCTCPResponseEvent;
import com.flurg.thimbot.event_old.outbound.OutboundMessageEvent;
import com.flurg.thimbot.event_old.outbound.OutboundNoticeEvent;
import com.flurg.thimbot.event_old.outbound.OutboundPingEvent;
import com.flurg.thimbot.event_old.outbound.OutboundPongEvent;
import com.flurg.thimbot.event_old.outbound.OutboundServerPingEvent;
import com.flurg.thimbot.event_old.outbound.OutboundServerPongEvent;
import com.flurg.thimbot.event_old.outbound.QuitRequestEvent;

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
