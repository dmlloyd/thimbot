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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.flurg.thimbot.IRCStrings;
import com.flurg.thimbot.Priority;
import com.flurg.thimbot.ThimBot;
import com.flurg.thimbot.event.AbstractEvent;
import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;
import com.flurg.thimbot.event.inbound.CapabilityAckEvent;
import com.flurg.thimbot.event.inbound.CapabilityNakEvent;
import com.flurg.thimbot.event.inbound.InboundEvent;
import com.flurg.thimbot.raw.IRCOutput;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class CapabilityRequestEvent extends AbstractEvent implements OutboundProtocolEvent {
    private static final String[] NO_STRINGS = new String[0];

    private final Set<String> capabilities;
    private final Priority priority;

    public CapabilityRequestEvent(final ThimBot bot, final Priority priority, final String... capabilities) {
        super(bot);
        this.priority = priority;
        this.capabilities = new HashSet<>(Arrays.asList(capabilities));
    }

    public CapabilityRequestEvent(final ThimBot bot, final Priority priority, final Collection<String> capabilities) {
        super(bot);
        this.priority = priority;
        this.capabilities = new HashSet<>(capabilities);
    }

    public void dispatch(final EventHandlerContext context, final EventHandler handler) throws Exception {
        handler.handleEvent(context, this);
    }

    public Set<String> getCapabilities() {
        return capabilities;
    }

    public Priority getPriority() {
        return priority;
    }

    protected void toStringAddendum(final StringBuilder b) {
        final Iterator<String> iterator = capabilities.iterator();
        if (iterator.hasNext()) {
            b.append(": ");
            b.append(iterator.next());
            while (iterator.hasNext()) {
                b.append(' ').append(iterator.next());
            }
        }
    }

    public void writeProtocolMessage(final IRCOutput target) throws IOException {
        final Set<String> desiredCapabilities = getCapabilities();
        assert desiredCapabilities.size() > 0;
        final String[] caps = desiredCapabilities.toArray(NO_STRINGS);
        target.write(IRCStrings.CAP);
        target.write(' ');
        target.write(IRCStrings.REQ);
        target.write(' ');
        target.write(':');
        target.write(caps[0]);
        for (int i = 1; i < caps.length; i++) {
            target.write(' ');
            target.write(caps[i]);
        }
    }

    public boolean isAcknowledgedBy(final InboundEvent event) {
        final Set<String> ackedCaps;
        if (event instanceof CapabilityAckEvent) {
            ackedCaps = ((CapabilityAckEvent) event).getCapabilities();
        } else if (event instanceof CapabilityNakEvent) {
            return true;
        } else {
            return false;
        }
        for (String ackedCap : ackedCaps) {
            if (capabilities.contains(ackedCap)) {
                return true;
            }
        }
        return false;
    }
}
