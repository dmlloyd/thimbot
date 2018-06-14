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

package com.flurg.thimbot.event.irc.cap;

import java.util.List;

import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;
import com.flurg.thimbot.event.irc.command.IrcCommandEvent;

/**
 */
public abstract class CapEvent extends IrcCommandEvent {
    private final List<Capability> capabilities;
    private boolean more;

    CapEvent(final boolean outbound, final String source, final List<String> targets, final List<Capability> capabilities) {
        super(outbound, source, targets);
        this.capabilities = capabilities;
    }

    public String getCommand() {
        return "CAP";
    }

    public abstract String getSubCommand();

    protected void addToString(final StringBuilder b) {
        super.addToString(b);
        b.append(' ').append(getSubCommand());
    }

    public void accept(final EventHandlerContext context, final EventHandler eventHandler) {
        eventHandler.handleEvent(context, this);
    }

    public List<Capability> getCapabilities() {
        return capabilities;
    }

    public boolean hasCapabilityNamed(String name) {
        for (Capability capability : capabilities) {
            if (capability.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean getMore() {
        return more;
    }

    public void setMore(final boolean more) {
        this.more = more;
    }

    public Capability getCapability(final String name) {
        for (Capability capability : capabilities) {
            if (capability.getName().equals(name)) {
                return capability;
            }
        }
        return null;
    }
}
