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

import com.flurg.thimbot.ThimBot;
import com.flurg.thimbot.source.Channel;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class ChannelRedirectEvent extends Event implements Event.To<Channel>, Event.Regarding<Channel> {

    private final Channel regarding;
    private final Channel to;
    private final String rawReason;
    private final String reason;

    public ChannelRedirectEvent(final ThimBot bot, final Channel regarding, final Channel to, final String rawReason) {
        super(bot);
        this.regarding = regarding;
        this.to = to;
        this.rawReason = rawReason;
        reason = MessageUtil.deformat(rawReason);
    }

    public String getRawReason() {
        return rawReason;
    }

    public String getReason() {
        return reason;
    }

    public Channel getRegarding() {
        return regarding;
    }

    public boolean isRegardingMe() {
        return false;
    }

    public Channel getTarget() {
        return to;
    }

    public boolean isToMe() {
        return false;
    }

    public void dispatch(final EventHandlerContext context, final EventHandler handler) throws Exception {
        handler.handleEvent(context, this);
    }
}
