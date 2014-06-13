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

import com.flurg.thimbot.event.ChannelJoinEvent;
import com.flurg.thimbot.event.ChannelKickEvent;
import com.flurg.thimbot.event.ChannelPartEvent;
import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;
import com.flurg.thimbot.event.NickChangeEvent;
import com.flurg.thimbot.event.PrivateActionEvent;
import com.flurg.thimbot.event.PrivateMessageEvent;
import com.flurg.thimbot.event.ServerPingEvent;
import com.flurg.thimbot.event.ServerPongEvent;
import com.flurg.thimbot.event.UserPingEvent;
import com.flurg.thimbot.raw.EmissionKey;
import com.flurg.thimbot.raw.StringEmitter;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class DefaultHandler extends EventHandler {

    // flow control - messages and actions

    public void handleEvent(final EventHandlerContext context, final PrivateActionEvent event) throws Exception {
        if (event.isFromMe()) {
            EmissionKey key = new EmissionKey(IRCStrings.ACTION, new StringEmitter(event.getRawText()));
            event.getBot().acknowledge(key);
        } else {
            super.handleEvent(context, event);
        }
    }

    public void handleEvent(final EventHandlerContext context, final PrivateMessageEvent event) throws Exception {
        if (event.isFromMe()) {
            EmissionKey key = new EmissionKey(IRCStrings.PRIVMSG, new StringEmitter(event.getRawText()));
            event.getBot().acknowledge(key);
        } else {
            super.handleEvent(context, event);
        }
    }

    // nick tracking

    public void handleEvent(final EventHandlerContext context, final NickChangeEvent event) throws Exception {
        final ThimBot bot = event.getBot();
        if (event.isFromMe()) {
            bot.setBotNick(event.getNewNick());
        }
        super.handleEvent(context, event);
    }

    // channel tracking

    public void handleEvent(final EventHandlerContext context, final ChannelJoinEvent event) throws Exception {
        final ThimBot bot = event.getBot();
        if (event.isFromMe()) {
            bot.addJoinedChannel(event.getChannel());
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelPartEvent event) throws Exception {
        final ThimBot bot = event.getBot();
        if (event.isFromMe()) {
            bot.removeJoinedChannel(event.getChannel());
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelKickEvent event) throws Exception {
        final ThimBot bot = event.getBot();
        if (event.getTargets().contains(bot.getBotNick())) {
            bot.removeJoinedChannel(event.getChannel());
        }
        super.handleEvent(context, event);
    }

    // server pings

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

    // user pings

    public void handleEvent(final EventHandlerContext context, final UserPingEvent event) throws Exception {
        try {
            event.getBot().sendPong(Priority.LOW, event.getFromNick(), event.getPayload());
        } finally {
            super.handleEvent(context, event);
        }
    }
}
