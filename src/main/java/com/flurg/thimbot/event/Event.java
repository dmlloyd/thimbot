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

import java.util.Arrays;

import com.flurg.thimbot.ThimBot;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public abstract class Event implements CommonEvent {

    private final ThimBot bot;

    private final long seq;
    private final long clockTime;

    protected Event(final ThimBot bot) {
        this.bot = bot;
        clockTime = System.currentTimeMillis();
        seq = bot.getEventSequence();
    }

    public long getSeq() {
        return seq;
    }

    public long getClockTime() {
        return clockTime;
    }

    public ThimBot getBot() {
        return bot;
    }

    protected void toStringAddendum(StringBuilder b) {}

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getSimpleName());
        if (this instanceof FromUserEvent) {
            b.append(" from ").append(((FromUserEvent)this).getFromNick());
        }
        if (this instanceof MultiTargetEvent) {
            b.append(" to ").append(Arrays.toString(((MultiTargetEvent)this).getTargets().toArray()));
        }
        if (this instanceof ChannelEvent) {
            b.append(" of channel ").append(((ChannelEvent) this).getChannel());
        }
        if (this instanceof MessageRespondableEvent) {
            b.append(" responses to ").append(Arrays.toString(((MessageRespondableEvent)this).getResponseTargets()));
        }
        toStringAddendum(b);
        if (this instanceof TextEvent) {
            b.append(" (\"").append(((TextEvent)this).getText()).append("\")");
        }
        return b.toString();
    }
}
