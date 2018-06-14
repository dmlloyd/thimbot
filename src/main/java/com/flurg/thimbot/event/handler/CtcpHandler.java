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

package com.flurg.thimbot.event.handler;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Locale.ROOT;

import java.util.function.BiFunction;

import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;
import com.flurg.thimbot.event.ctcp.ActionEvent;
import com.flurg.thimbot.event.ctcp.CtcpCommandEvent;
import com.flurg.thimbot.event.ctcp.CtcpEvent;
import com.flurg.thimbot.event.ctcp.CtcpPingEvent;
import com.flurg.thimbot.event.ctcp.CtcpPingResponseEvent;
import com.flurg.thimbot.event.ctcp.CtcpResponseEvent;
import com.flurg.thimbot.event.ctcp.CtcpTimeEvent;
import com.flurg.thimbot.event.ctcp.CtcpTimeResponseEvent;
import com.flurg.thimbot.event.ctcp.CtcpVersionEvent;
import com.flurg.thimbot.event.ctcp.CtcpVersionResponseEvent;
import com.flurg.thimbot.event.irc.command.IrcCommandEvent;
import com.flurg.thimbot.event.irc.command.MessageEvent;
import com.flurg.thimbot.event.irc.command.NoticeEvent;
import com.flurg.thimbot.util.ByteString;
import com.flurg.thimbot.util.ByteStringBuilder;

/**
 * Event handler which handles translation of CTCP events to IRC events.
 */
public final class CtcpHandler extends EventHandler {

    public static final CtcpHandler INSTANCE = new CtcpHandler();

    private CtcpHandler() {}

    // outbound

    public void handleEvent(final EventHandlerContext context, final CtcpCommandEvent event) {
        if (event.isOutbound()) {
            context.dispatch(initEvent(event, new MessageEvent(true, event.getSource(), event.getTargets())));
        } else {
            super.handleEvent(context, event);
        }
    }

    public void handleEvent(final EventHandlerContext context, final CtcpResponseEvent event) {
        if (event.isOutbound()) {
            context.dispatch(initEvent(event, new NoticeEvent(true, event.getSource(), event.getTargets())));
        } else {
            super.handleEvent(context, event);
        }
    }

    private static <E extends IrcCommandEvent> E initEvent(final CtcpEvent ctcpEvent, final E targetEvent) {
        targetEvent.setTimestamp(ctcpEvent.getTimestamp());
        final ByteStringBuilder b = new ByteStringBuilder();
        b.appendByte(1).append(ctcpEvent.getCommand(), US_ASCII).appendByte(' ').append(ctcpEvent.getEncodedText()).appendByte(1);
        targetEvent.setEncodedText(b.toByteString());
        return targetEvent;
    }

    // inbound

    public void handleEvent(final EventHandlerContext context, final MessageEvent event) {
        if (event.isInbound()) {
            handleEvent(context, event, CtcpHandler::getCommandEvent);
        } else {
            super.handleEvent(context, event);
        }
    }

    public void handleEvent(final EventHandlerContext context, final NoticeEvent event) {
        if (event.isInbound()) {
            handleEvent(context, event, CtcpHandler::getResponseEvent);
        } else {
            super.handleEvent(context, event);
        }
    }

    private static CtcpEvent getCommandEvent(String command, IrcCommandEvent sourceEvent) {
        switch (command) {
            case "PING": return new CtcpPingEvent(false, sourceEvent.getSource(), sourceEvent.getTargets());
            case "ACTION": return new ActionEvent(false, sourceEvent.getSource(), sourceEvent.getTargets());
            case "TIME": return new CtcpTimeEvent(false, sourceEvent.getSource(), sourceEvent.getTargets());
            case "VERSION": return new CtcpVersionEvent(false, sourceEvent.getSource(), sourceEvent.getTargets());
            default: return null;
        }
    }

    private static CtcpEvent getResponseEvent(String command, IrcCommandEvent sourceEvent) {
        switch (command) {
            case "PING": return new CtcpPingResponseEvent(false, sourceEvent.getSource(), sourceEvent.getTargets());
            case "TIME": return new CtcpTimeResponseEvent(false, sourceEvent.getSource(), sourceEvent.getTargets());
            case "VERSION": return new CtcpVersionResponseEvent(false, sourceEvent.getSource(), sourceEvent.getTargets());
            default: return null;
        }
    }

    private static final int ST_INIT = 0;
    private static final int ST_GOT_1 = 1;
    private static final int ST_GOT_CMD = 2;

    private void handleEvent(EventHandlerContext context, IrcCommandEvent event, BiFunction<String, IrcCommandEvent, CtcpEvent> eventFactory) {
        final ByteString encodedText = event.getEncodedText();
        int state = ST_INIT;
        int start = 0;
        int start2 = 0;
        boolean dispatchedOne = false;
        for (int i = 0; i < encodedText.length(); i++) {
            final int b = encodedText.byteAt(i);
            if (b == 1) {
                if (state == ST_INIT) {
                    if (i > start) {
                        // some message characters exist
                        final IrcCommandEvent clone = event.clone();
                        clone.setEncodedText(encodedText.substring(start, i));
                        context.dispatch(clone);
                        dispatchedOne = true;
                    }
                    // could be a CTCP start sequence
                    state = ST_GOT_1;
                    start = i + 1;
                } else if (state == ST_GOT_1) {
                    // a command-only event
                    final CtcpEvent ctcpEvent = eventFactory.apply(encodedText.substring(start, i).toString(US_ASCII).toUpperCase(ROOT), event);
                    ctcpEvent.setTextCharset(event.getTextCharset());
                    context.dispatch(ctcpEvent);
                    dispatchedOne = true;
                    state = ST_INIT;
                } else if (state == ST_GOT_CMD) {
                    // command plus argument
                    final CtcpEvent ctcpEvent = eventFactory.apply(encodedText.substring(start, start2 - 1).toString(US_ASCII).toUpperCase(ROOT), event);
                    ctcpEvent.setTextCharset(event.getTextCharset());
                    ctcpEvent.setEncodedText(encodedText.substring(start2, i));
                    context.dispatch(ctcpEvent);
                    dispatchedOne = true;
                    state = ST_INIT;
                } else {
                    throw new IllegalStateException();
                }
            } else if (state == ST_GOT_1 && b == ' ') {
                start2 = i + 1;
                state = ST_GOT_CMD;
            }
        }
        if (! dispatchedOne) {
            // it was a legitimately empty message, or consisted only of 0x01
            event.setEncodedText(ByteString.EMPTY);
            context.dispatch(event);
        }
    }
}
