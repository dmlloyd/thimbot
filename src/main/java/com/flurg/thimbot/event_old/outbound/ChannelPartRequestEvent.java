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

package com.flurg.thimbot.event_old.outbound;

import com.flurg.thimbot.Priority;
import com.flurg.thimbot.ThimBot;
import com.flurg.thimbot.event_old.ChannelEvent;
import com.flurg.thimbot.event_old.AbstractEvent;
import com.flurg.thimbot.event_old.EventHandler;
import com.flurg.thimbot.event_old.EventHandlerContext;
import com.flurg.thimbot.event_old.TextEvent;
import com.flurg.thimbot.util.IRCStringUtil;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class ChannelPartRequestEvent extends AbstractEvent implements OutboundEvent, ChannelEvent, TextEvent {

    private final Priority priority;
    private final String channel;
    private final String rawReason;
    private final String reason;

    public ChannelPartRequestEvent(final ThimBot bot, final Priority priority, final String channel, final String rawReason) {
        super(bot);
        this.priority = priority;
        this.channel = channel;
        this.rawReason = rawReason;
        reason = IRCStringUtil.deformat(rawReason);
    }

    public String getChannel() {
        return channel;
    }

    public String getRawText() {
        return rawReason;
    }

    public String getText() {
        return reason;
    }

    public void dispatch(final EventHandlerContext context, final EventHandler handler) throws Exception {
        handler.handleEvent(context, this);
    }

    public Priority getPriority() {
        return priority;
    }
}