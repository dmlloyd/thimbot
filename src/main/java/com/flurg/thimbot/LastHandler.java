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

package com.flurg.thimbot;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import com.flurg.thimbot.event_old.Event;
import com.flurg.thimbot.event_old.EventHandler;
import com.flurg.thimbot.event_old.EventHandlerContext;
import com.flurg.thimbot.event_old.inbound.CapabilityListEvent;
import com.flurg.thimbot.event_old.inbound.ConnectFailedEvent;
import com.flurg.thimbot.event_old.inbound.DisconnectCompleteEvent;
import com.flurg.thimbot.event_old.inbound.HangUpEvent;
import com.flurg.thimbot.event_old.inbound.InboundEvent;
import com.flurg.thimbot.event_old.inbound.ServerPingEvent;
import com.flurg.thimbot.event_old.inbound.UserPingEvent;
import com.flurg.thimbot.event_old.outbound.CapabilityRequestEvent;
import com.flurg.thimbot.event_old.outbound.ConnectRequestEvent;
import com.flurg.thimbot.event_old.outbound.HangUpRequestEvent;
import com.flurg.thimbot.event_old.outbound.OutboundPongEvent;
import com.flurg.thimbot.event_old.outbound.OutboundServerPongEvent;

/**
 * The last handler, which implements the default behavior for inbound events.  Since this is the last handler,
 * it does not process any outbound requests.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class LastHandler extends EventHandler {
    public void handleEvent(final EventHandlerContext context, final Event event) throws Exception {
        if (event instanceof InboundEvent) {
            System.out.println(">>> " + event);
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final CapabilityListEvent event) throws Exception {
        context.dispatch(new CapabilityRequestEvent(event.getBot(), Priority.HIGH, Collections.emptySet()));
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final UserPingEvent event) throws Exception {
        context.dispatch(new OutboundPongEvent(event.getBot(), Priority.LOW, Collections.singleton(event.getFromNick()), event.getRawText()));
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final ServerPingEvent event) throws Exception {
        context.dispatch(new OutboundServerPongEvent(event.getBot(), Priority.HIGH, event.getRawText()));
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final HangUpEvent event) throws Exception {
        // read side was disconnected, now disconnect the write side if we haven't already done so
        context.dispatch(new HangUpRequestEvent(event.getBot(), Priority.NORMAL));
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final DisconnectCompleteEvent event) throws Exception {
        // fully disconnected, now reconnect (after configured delay)
        context.dispatchAfterDelay(10, TimeUnit.SECONDS, new ConnectRequestEvent(event.getBot(), Priority.HIGH));
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final ConnectFailedEvent event) throws Exception {
        // fully disconnected, now reconnect (after configured delay)
        context.dispatchAfterDelay(10, TimeUnit.SECONDS, new ConnectRequestEvent(event.getBot(), Priority.HIGH));
        super.handleEvent(context, event);
    }
}
