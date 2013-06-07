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

package com.flurg.thimbot;

import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;
import com.flurg.thimbot.event.NickChangeEvent;
import com.flurg.thimbot.event.PingEvent;
import com.flurg.thimbot.event.ServerPingEvent;
import com.flurg.thimbot.event.ServerPongEvent;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class DefaultHandler extends EventHandler {

    public void handleEvent(final EventHandlerContext context, final ServerPingEvent event) throws Exception {
        try {
            event.getBot().sendPong(Priority.HIGH, event.getPayload());
        } finally {
            super.handleEvent(context, event);
        }
    }

    public void handleEvent(final EventHandlerContext context, final ServerPongEvent event) throws Exception {
        try {
            final String payload = event.getPayload();
            if (payload.charAt(0) == 'Q') {
                final int seq = Integer.parseInt(payload.substring(1));
                event.getBot().acknowledge(seq);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final PingEvent event) throws Exception {
        try {
            event.getBot().sendPong(Priority.LOW, event.getSource().getNick(), event.getPayload());
        } finally {
            super.handleEvent(context, event);
        }
    }

    public void handleEvent(final EventHandlerContext context, final NickChangeEvent event) throws Exception {
        final ThimBot bot = event.getBot();
        if (event.isFromMe()) {
            bot.setBotNick(event.getRegarding());
        }
        super.handleEvent(context, event);
    }
}
