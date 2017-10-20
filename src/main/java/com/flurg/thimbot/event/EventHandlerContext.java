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

import java.util.ArrayDeque;
import java.util.Deque;

import com.flurg.thimbot.event.inbound.InboundEvent;
import com.flurg.thimbot.event.outbound.OutboundEvent;
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
