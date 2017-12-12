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

import java.util.ArrayDeque;
import java.util.Deque;

import com.flurg.thimbot.event_old.inbound.InboundEvent;
import com.flurg.thimbot.event_old.outbound.OutboundEvent;
import org.jboss.logging.Logger;

/**
 * The context for event handlers.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class EventHandlerContext {
    static final Logger log = Logger.getLogger("com.flurg.thimbot.event");

    private final EventHandler[] chain;
    private final boolean outbound;
    private final Event event;
    private int index;
    private final Deque<EventHandlerContext> pending = new ArrayDeque<>();

    public static EventHandlerContext create(final EventHandler[] chain, final Event event) {
        if (event instanceof InboundEvent) {
            return new EventHandlerContext(chain, (InboundEvent) event, 0);
        } else if (event instanceof OutboundEvent) {
            return new EventHandlerContext(chain, (OutboundEvent) event, chain.length);
        } else {
            throw new IllegalArgumentException("Event " + event.getClass() + " must be either inbound or outbound");
        }
    }

    EventHandlerContext(final EventHandler[] chain, final InboundEvent event, final int index) {
        this(chain, event, false, index);
    }

    EventHandlerContext(final EventHandler[] chain, final OutboundEvent event, final int index) {
        this(chain, event, true, index);
    }

    private EventHandlerContext(final EventHandler[] chain, final Event event, final boolean outbound, final int index) {
        this.chain = chain;
        this.event = event;
        this.outbound = outbound;
        this.index = index;
    }

    public void dispatch(NewEvent event) {

    }
    public void dispatch(InboundEvent event) {
        pending.add(new EventHandlerContext(chain, event, outbound ? index : index + 1));
    }

    public void dispatch(OutboundEvent event) {
        pending.add(new EventHandlerContext(chain, event, index));
    }

    /**
     * Tell the previous handler in the chain to handle the event, or send the event if no handlers remain.
     */
    public void proceed() {
        final Event event = this.event;
        if (event instanceof OutboundEvent) {
            if (index > 0) try {
                event.dispatch(this, chain[--index]);
            } catch (Throwable e) {
                log.error("An event handler threw an exception", e);
            } finally {
                index++;
            }
        } else {
            assert event instanceof InboundEvent;
            if (index < chain.length) try {
                event.dispatch(this, chain[index++]);
            } catch (Throwable e) {
                log.error("An event handler threw an exception", e);
            } finally {
                index--;
            }
        }
    }
}
