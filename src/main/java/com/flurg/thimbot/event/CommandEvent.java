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

import java.util.List;

import com.flurg.thimbot.event.irc.ProtocolEvent;

/**
 */
public abstract class CommandEvent extends ProtocolEvent {
    protected CommandEvent(final boolean outbound, final String source, final List<String> targets) {
        super(outbound, source, targets);
    }

    public abstract String getCommand();

    protected void addToString(final StringBuilder b) {
        b.append(' ').append(getCommand());
    }

    public CommandEvent clone() {
        return (CommandEvent) super.clone();
    }
}
