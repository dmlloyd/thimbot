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
