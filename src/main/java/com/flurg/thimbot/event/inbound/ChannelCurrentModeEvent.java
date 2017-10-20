/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017 Red Hat, Inc., and individual contributors
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

package com.flurg.thimbot.event.inbound;

import com.flurg.thimbot.Modes;
import com.flurg.thimbot.ThimBot;
import com.flurg.thimbot.event.AbstractEvent;
import com.flurg.thimbot.event.ChannelEvent;
import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;

/**
 *
 */
public final class ChannelCurrentModeEvent extends AbstractEvent implements InboundEvent, ChannelEvent {
    private final String channel;
    private final Modes modeMap;

    public ChannelCurrentModeEvent(final ThimBot bot, final String channel, final Modes modeMap) {
        super(bot);
        this.channel = channel;
        this.modeMap = modeMap;
    }

    public String getChannel() {
        return channel;
    }

    public Modes getModeMap() {
        return modeMap;
    }

    public void dispatch(final EventHandlerContext context, final EventHandler handler) throws Exception {
        handler.handleEvent(context, this);
    }

    protected void toStringAddendum(final StringBuilder b) {
        b.append(':').append(' ');
        modeMap.toString(b);
    }
}
