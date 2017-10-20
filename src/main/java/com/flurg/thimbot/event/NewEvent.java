/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017 Red Hat, Inc., and individual contributors
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
