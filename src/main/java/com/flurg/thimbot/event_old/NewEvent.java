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

import java.util.List;

import com.flurg.thimbot.util.IRCStringUtil;

/**
 * A basic event.
 */
public abstract class NewEvent {
    private final String source;
    private final String rawMessage;
    private String message;
    private final List<String> targets;

    private final long timestamp = System.currentTimeMillis();
    private final boolean outbound;

    protected NewEvent(final String source, final String rawMessage, final List<String> targets, final boolean outbound) {
        this.source = source;
        this.rawMessage = rawMessage;
        this.targets = targets;
        this.outbound = outbound;
    }

    public String getSource() {
        return source;
    }

    public String getSourceNick() {
        final String source = this.source;
        return source != null && IRCStringUtil.isUser(source) ? IRCStringUtil.nickOf(source) : null;
    }

    public String getSourceServer() {
        final String source = this.source;
        return source != null && ! IRCStringUtil.isUser(source) ? source : null;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public String getMessage() {
        final String message = this.message;
        if (message == null) {
            final String rawMessage = this.rawMessage;
            if (rawMessage != null) {
                return this.message = IRCStringUtil.deformat(rawMessage);
            }
        }
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isOutbound() {
        return outbound;
    }

    public boolean isInbound() {
        return ! outbound;
    }

    public List<String> getTargets() {
        return targets;
    }

    public abstract void dispatch(EventHandlerContext context, NewEventHandler eventHandler) throws Exception;
}
