/*
 * Copyright 2017 by David M. Lloyd and contributors
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

import com.flurg.thimbot.Association;
import com.flurg.thimbot.Connection;

/**
 */
public final class EventHandlerContext {
    private final Association association;
    private final EventHandler eventHandler;
    private final EventHandlerContext next, prev;

    EventHandlerContext(final Association association, final EventHandler eventHandler, final EventHandlerContext next, final EventHandlerContext prev) {
        this.association = association;
        this.eventHandler = eventHandler;
        this.next = next;
        this.prev = prev;
    }

    public void dispatch(final Event event) {
        if (event == null) return;
        final EventHandlerContext next;
        if (event.isOutbound()) {
            next = this.next;
        } else {
            next = this.prev;
        }
        if (next != null) {
            next.handleEvent(event);
        }
    }

    private void handleEvent(final Event event) {
        try {
            event.accept(this, eventHandler);
        } catch (Throwable t) {
            // TODO: log it
        }
    }

    public Association getAssociation() {
        return association;
    }

    public Connection getConnection() {
        return association.getConnection();
    }
}
