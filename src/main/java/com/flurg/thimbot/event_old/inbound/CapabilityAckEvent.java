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

package com.flurg.thimbot.event_old.inbound;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.flurg.thimbot.ThimBot;
import com.flurg.thimbot.event_old.AbstractEvent;
import com.flurg.thimbot.event_old.EventHandler;
import com.flurg.thimbot.event_old.EventHandlerContext;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class CapabilityAckEvent extends AbstractEvent implements InboundEvent {
    private final Set<String> capabilities;

    public CapabilityAckEvent(final ThimBot bot, final String... capabilities) {
        super(bot);
        this.capabilities = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(capabilities)));
    }

    public CapabilityAckEvent(final ThimBot bot, final Collection<String> capabilities) {
        super(bot);
        this.capabilities = Collections.unmodifiableSet(new HashSet<>(capabilities));
    }

    public void dispatch(final EventHandlerContext context, final EventHandler handler) throws Exception {
        handler.handleEvent(context, this);
    }

    public Set<String> getCapabilities() {
        return capabilities;
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
}
