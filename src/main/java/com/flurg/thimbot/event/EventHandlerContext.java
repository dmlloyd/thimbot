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
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * The context for event handlers.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class EventHandlerContext {
    private final List<EventHandler> chain;
    private final ListIterator<EventHandler> iterator;
    private final Map<HandlerKey<?>, Object> contextMap = new HashMap<>();
    private final Deque<Event> pending = new ArrayDeque<>();

    /**
     * Construct a new instance.
     *
     * @param chain the chain to pass events through
     */
    public EventHandlerContext(final List<EventHandler> chain) {
        this.chain = chain;
        iterator = chain.listIterator();
    }

    /**
     * Tell the next handler in the chain to handle the given event, or return immediately if no more handlers remain.
     * This method may be used to dispatch any number of events to subsequent handlers.
     *
     * @param event the event
     */
    public void next(Event event) {
        if (iterator.hasNext()) try {
            event.dispatch(this, iterator.next());
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            iterator.previous();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getContextValue(HandlerKey<T> key) {
        Object value = contextMap.get(key);
        if (value == null) {
            value = key.initialValue();
            if (value != null) {
                contextMap.put(key, value);
            }
        }
        return (T) value;
    }

    @SuppressWarnings("unchecked")
    public <T> T putContextValue(HandlerKey<T> key, T value) {
        return (T) contextMap.put(key, value);
    }

    private static final ThreadLocal<EventHandlerContext> C = new ThreadLocal<>();

    /**
     * Re-dispatch the given event to the start of the chain.
     *
     * @param event the event
     */
    public void redispatch(Event event) {
        if (event == null) return;
        EventHandlerContext current = C.get();
        if (current != null) {
            current.pending.add(event);
        } else {
            C.set(this);
            try {
                do {
                    next(event);
                } while ((event = pending.pollFirst()) != null);
            } finally {
                C.remove();
            }
        }
    }

    public static void dispatch(final List<EventHandler> chain, final Event event) {
        EventHandlerContext context = C.get();
        if (context == null) {
            context = new EventHandlerContext(chain);
        }
        context.redispatch(event);
    }
}
