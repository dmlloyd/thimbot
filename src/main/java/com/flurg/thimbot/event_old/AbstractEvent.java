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

import java.util.Arrays;

import com.flurg.thimbot.ThimBot;
import com.flurg.thimbot.event_old.inbound.FromUserEvent;

/**
 * A base class which is useful for implementing events.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public abstract class AbstractEvent implements Event {

    private final ThimBot bot;

    private final long seq;
    private final long clockTime;

    protected AbstractEvent(final ThimBot bot) {
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
