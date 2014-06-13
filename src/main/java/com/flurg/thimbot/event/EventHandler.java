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

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public abstract class EventHandler {

    public void handleEvent(final EventHandlerContext context, final Event event) throws Exception {
        context.next(event);
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

    public void handleEvent(final EventHandlerContext context, final DisconnectEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final DisconnectRequestEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ErrorEvent event) throws Exception {
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
}
