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

package com.flurg.thimbot.event.irc.command;

import java.util.Collections;

import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;

/**
 */
public final class WhoEvent extends IrcCommandEvent {
    private String mask;
    private boolean opersOnly;

    public WhoEvent() {
        super(true, null, Collections.emptyList());
    }

    public String getCommand() {
        return "WHO";
    }

    public void accept(final EventHandlerContext context, final EventHandler eventHandler) {
        eventHandler.handleEvent(context, this);
    }

    public WhoEvent clone() {
        return (WhoEvent) super.clone();
    }

    public String getMask() {
        return mask;
    }

    public void setMask(final String mask) {
        this.mask = mask;
    }

    public boolean isOpersOnly() {
        return opersOnly;
    }

    public void setOpersOnly(final boolean opersOnly) {
        this.opersOnly = opersOnly;
    }
}