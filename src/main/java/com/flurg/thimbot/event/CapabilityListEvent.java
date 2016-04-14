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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.flurg.thimbot.ThimBot;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class CapabilityListEvent extends Event implements InboundEvent {
    private final Map<String, Capability> capabilities;

    public CapabilityListEvent(final ThimBot bot, final String... capabilities) {
        super(bot);
        final HashMap<String, Capability> map = new HashMap<>();
        for (String capability : capabilities) {
            final Capability cap = Capability.fromString(capability);
            if (cap != null) {
                map.put(cap.getName(), cap);
            }
        }
        this.capabilities = Collections.unmodifiableMap(map);
    }

    public CapabilityListEvent(final ThimBot bot, final Collection<String> capabilities) {
        super(bot);
        final HashMap<String, Capability> map = new HashMap<>();
        for (String capability : capabilities) {
            final Capability cap = Capability.fromString(capability);
            if (cap != null) {
                map.put(cap.getName(), cap);
            }
        }
        this.capabilities = Collections.unmodifiableMap(map);
    }

    public void dispatch(final EventHandlerContext context, final EventHandler handler) throws Exception {
        handler.handleEvent(context, this);
    }

    public Set<String> getCapabilityNames() {
        return capabilities.keySet();
    }

    public Collection<Capability> getCapabilities() {
        return capabilities.values();
    }

    public Capability getCapabilityByName(String name) {
        return capabilities.get(name);
    }
}
