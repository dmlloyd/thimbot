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

import java.io.IOException;

import com.flurg.thimbot.IRCStrings;
import com.flurg.thimbot.Priority;
import com.flurg.thimbot.ThimBot;
import com.flurg.thimbot.event_old.AbstractEvent;
import com.flurg.thimbot.event_old.EventHandler;
import com.flurg.thimbot.event_old.EventHandlerContext;
import com.flurg.thimbot.event_old.inbound.AuthenticationChallengeEvent;
import com.flurg.thimbot.event_old.inbound.AuthenticationFailedEvent;
import com.flurg.thimbot.event_old.inbound.AuthenticationMechanismsEvent;
import com.flurg.thimbot.event_old.inbound.AuthenticationSuccessfulEvent;
import com.flurg.thimbot.event_old.inbound.InboundEvent;
import com.flurg.thimbot.raw.IRCOutput;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class AuthenticationRequestEvent extends AbstractEvent implements OutboundProtocolEvent {

    private final String mechanism;
    private final Priority priority;

    public AuthenticationRequestEvent(final ThimBot bot, final Priority priority, final String mechanism) {
        super(bot);
        this.priority = priority;
        this.mechanism = mechanism;
    }

    public String getMechanism() {
        return mechanism;
    }

    public void dispatch(final EventHandlerContext context, final EventHandler handler) throws Exception {
        handler.handleEvent(context, this);
    }

    public Priority getPriority() {
        return priority;
    }

    protected void toStringAddendum(final StringBuilder b) {
        b.append(':').append(' ').append(mechanism);
    }

    public void writeProtocolMessage(final IRCOutput target) throws IOException {
        target.write(IRCStrings.AUTHENTICATE);
        target.write(' ');
        target.write(mechanism);
    }

    public boolean isAcknowledgedBy(final InboundEvent event) {
        return
            event instanceof AuthenticationChallengeEvent ||
            event instanceof AuthenticationFailedEvent ||
            event instanceof AuthenticationMechanismsEvent ||
            event instanceof AuthenticationSuccessfulEvent;
    }
}
