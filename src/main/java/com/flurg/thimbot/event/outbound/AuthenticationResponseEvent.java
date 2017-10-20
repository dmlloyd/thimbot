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

package com.flurg.thimbot.event.outbound;

import static java.lang.Math.min;

import java.io.IOException;

import com.flurg.thimbot.IRCStrings;
import com.flurg.thimbot.Priority;
import com.flurg.thimbot.ThimBot;
import com.flurg.thimbot.event.AbstractEvent;
import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;
import com.flurg.thimbot.event.inbound.AuthenticationChallengeEvent;
import com.flurg.thimbot.event.inbound.AuthenticationFailedEvent;
import com.flurg.thimbot.event.inbound.AuthenticationMechanismsEvent;
import com.flurg.thimbot.event.inbound.AuthenticationSuccessfulEvent;
import com.flurg.thimbot.event.inbound.InboundEvent;
import com.flurg.thimbot.raw.IRCOutput;
import com.flurg.thimbot.util.IRCBase64;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class AuthenticationResponseEvent extends AbstractEvent implements OutboundProtocolEvent {

    private final byte[] bytes;
    private final Priority priority;

    public AuthenticationResponseEvent(final ThimBot bot, final Priority priority, final byte[] bytes) {
        super(bot);
        this.priority = priority;
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void dispatch(final EventHandlerContext context, final EventHandler handler) throws Exception {
        handler.handleEvent(context, this);
    }

    public Priority getPriority() {
        return priority;
    }

    protected void toStringAddendum(final StringBuilder b) {
        b.append(':').append(' ').append(bytes.length).append(" bytes");
    }

    public void writeProtocolMessage(final IRCOutput target) throws IOException {
        final byte[] response = bytes;
        final int length = response.length;
        if (length == 0) {
            target.write(IRCStrings.AUTHENTICATE);
            target.write(' ');
            target.write('+');
        } else for (int i = 0; i < length; i += 400) {
            final int start = i;
            target.write(IRCStrings.AUTHENTICATE);
            target.write(' ');
            IRCBase64.encode(response, start, min(400, length - start), target);
        }
    }

    public boolean isAcknowledgedBy(final InboundEvent event) {
        return
            event instanceof AuthenticationChallengeEvent ||
            event instanceof AuthenticationFailedEvent ||
            event instanceof AuthenticationMechanismsEvent ||
            event instanceof AuthenticationSuccessfulEvent;
    }
}
