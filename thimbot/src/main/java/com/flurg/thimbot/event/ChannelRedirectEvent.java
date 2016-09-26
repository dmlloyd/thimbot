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

import com.flurg.thimbot.ThimBot;
import com.flurg.thimbot.util.IRCStringUtil;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class ChannelRedirectEvent extends Event implements ChannelEvent, InboundEvent, TextEvent {

    private final String from;
    private final String to;
    private final String rawReason;
    private final String reason;

    public ChannelRedirectEvent(final ThimBot bot, final String from, final String to, final String rawReason) {
        super(bot);
        this.from = from;
        this.to = to;
        this.rawReason = rawReason;
        reason = IRCStringUtil.deformat(rawReason);
    }

    public String getRawText() {
        return rawReason;
    }

    public String getText() {
        return reason;
    }

    public String getChannel() {
        return from;
    }

    public String getTarget() {
        return to;
    }

    protected void toStringAddendum(final StringBuilder b) {
        b.append(" to ").append(to);
    }

    public void dispatch(final EventHandlerContext context, final EventHandler handler) throws Exception {
        handler.handleEvent(context, this);
    }
}
