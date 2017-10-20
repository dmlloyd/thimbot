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

import java.io.IOException;

import com.flurg.thimbot.IRCStrings;
import com.flurg.thimbot.Priority;
import com.flurg.thimbot.ThimBot;
import com.flurg.thimbot.event.AbstractEvent;
import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;
import com.flurg.thimbot.event.inbound.CapabilityListEvent;
import com.flurg.thimbot.event.inbound.InboundEvent;
import com.flurg.thimbot.raw.IRCOutput;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class CapabilityListRequestEvent extends AbstractEvent implements OutboundProtocolEvent {

    private final Priority priority;

    public CapabilityListRequestEvent(final ThimBot bot, final Priority priority) {
        super(bot);
        this.priority = priority;
    }

    public void dispatch(final EventHandlerContext context, final EventHandler handler) throws Exception {
        handler.handleEvent(context, this);
    }

    public Priority getPriority() {
        return priority;
    }

    public void writeProtocolMessage(final IRCOutput target) throws IOException {
        target.write(IRCStrings.CAP);
        target.write(' ');
        target.write(IRCStrings.LS);
    }

    public boolean isAcknowledgedBy(final InboundEvent event) {
        return event instanceof CapabilityListEvent;
    }
}
