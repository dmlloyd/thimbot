/*
 * Copyright 2017 by David M. Lloyd and contributors
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

import com.flurg.thimbot.event.connection.*;
import com.flurg.thimbot.event.ctcp.*;
import com.flurg.thimbot.event.irc.*;
import com.flurg.thimbot.event.irc.command.*;
import com.flurg.thimbot.event.irc.cap.*;
import com.flurg.thimbot.event.irc.response.*;

/**
 */
public abstract class EventHandler {

    public void handleEvent(EventHandlerContext context, Event event) {
        context.dispatch(event);
    }


    public void handleEvent(EventHandlerContext context, ConnectEvent event) {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(EventHandlerContext context, ConnectFailedEvent event) {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(EventHandlerContext context, DisconnectEvent event) {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(EventHandlerContext context, HangUpEvent event) {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(EventHandlerContext context, ReadyEvent event) {
        handleEvent(context, (Event) event);
    }


    public void handleEvent(EventHandlerContext context, CtcpEvent event) {
        handleEvent(context, (Event) event);
    }


    public void handleEvent(EventHandlerContext context, CtcpCommandEvent event) {
        handleEvent(context, (CtcpEvent) event);
    }

    public void handleEvent(EventHandlerContext context, ActionEvent event) {
        handleEvent(context, (CtcpCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, CtcpPingEvent event) {
        handleEvent(context, (CtcpCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, CtcpTimeEvent event) {
        handleEvent(context, (CtcpCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, CtcpVersionEvent event) {
        handleEvent(context, (CtcpCommandEvent) event);
    }


    public void handleEvent(EventHandlerContext context, CtcpResponseEvent event) {
        handleEvent(context, (CtcpEvent) event);
    }

    public void handleEvent(EventHandlerContext context, CtcpPingResponseEvent event) {
        handleEvent(context, (CtcpResponseEvent) event);
    }

    public void handleEvent(EventHandlerContext context, CtcpTimeResponseEvent event) {
        handleEvent(context, (CtcpResponseEvent) event);
    }

    public void handleEvent(EventHandlerContext context, CtcpVersionResponseEvent event) {
        handleEvent(context, (CtcpResponseEvent) event);
    }


    public void handleEvent(EventHandlerContext context, ProtocolEvent event) {
        handleEvent(context, (Event) event);
    }


    public void handleEvent(EventHandlerContext context, IrcCommandEvent event) {
        handleEvent(context, (ProtocolEvent) event);
    }

    public void handleEvent(EventHandlerContext context, AcceptEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, AccountEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, AuthenticateEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, AwayEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, BanEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, InviteEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, JoinEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, KickEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, MessageEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, ModeEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, NickEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, NoticeEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, PartEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, PassEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, QuitEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, ServerErrorEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, ServerPingEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, ServerPongEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, TopicEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, UserEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, WhoEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, WhoIsEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, WhoWasEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }


    public void handleEvent(EventHandlerContext context, CapEvent event) {
        handleEvent(context, (IrcCommandEvent) event);
    }

    public void handleEvent(EventHandlerContext context, CapAckEvent event) {
        handleEvent(context, (CapEvent) event);
    }

    public void handleEvent(EventHandlerContext context, CapDelEvent event) {
        handleEvent(context, (CapEvent) event);
    }

    public void handleEvent(EventHandlerContext context, CapEndEvent event) {
        handleEvent(context, (CapEvent) event);
    }

    public void handleEvent(EventHandlerContext context, CapListActiveEvent event) {
        handleEvent(context, (CapEvent) event);
    }

    public void handleEvent(EventHandlerContext context, CapListSupportedEvent event) {
        handleEvent(context, (CapEvent) event);
    }

    public void handleEvent(EventHandlerContext context, CapNakEvent event) {
        handleEvent(context, (CapEvent) event);
    }

    public void handleEvent(EventHandlerContext context, CapNewEvent event) {
        handleEvent(context, (CapEvent) event);
    }

    public void handleEvent(EventHandlerContext context, CapRequestEvent event) {
        handleEvent(context, (CapEvent) event);
    }


    public void handleEvent(EventHandlerContext context, ResponseEvent event) {
        handleEvent(context, (ProtocolEvent) event);
    }

    public void handleEvent(EventHandlerContext context, ChannelResponseEvent event) {
        handleEvent(context, (ResponseEvent) event);
    }

}
