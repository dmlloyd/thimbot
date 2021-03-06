/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import java.util.Comparator;

import com.flurg.thimbot.Priority;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public interface OutboundEvent extends CommonEvent {
    /**
     * Get the priority of this event.
     *
     * @return the priority of this event
     */
    Priority getPriority();

    Comparator<OutboundEvent> PRIORITY_COMPARATOR = new Comparator<OutboundEvent>() {
        public int compare(final OutboundEvent o1, final OutboundEvent o2) {
            return o2.getPriority().compareTo(o1.getPriority());
        }
    };
}
