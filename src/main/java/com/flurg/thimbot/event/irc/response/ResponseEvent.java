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

package com.flurg.thimbot.event.irc.response;

import java.util.Collections;

import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;
import com.flurg.thimbot.event.irc.ProtocolEvent;

/**
 */
public class ResponseEvent extends ProtocolEvent {
    private final int code;

    public ResponseEvent(String source, final int code) {
        super(false, source, Collections.emptyList());
        if (code < 0 || code > 999) throw new IllegalArgumentException("Invalid code");
        this.code = code;
    }

    protected void addToString(final StringBuilder b) {
        b.append(' ').append(getCode());
    }

    public void accept(final EventHandlerContext context, final EventHandler eventHandler) {
        eventHandler.handleEvent(context, this);
    }

    public ResponseEvent clone() {
        return (ResponseEvent) super.clone();
    }

    public int getCode() {
        return code;
    }
}
