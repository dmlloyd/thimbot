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
import com.flurg.thimbot.source.FullTarget;
import com.flurg.thimbot.source.User;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class PongEvent extends Event implements Event.From<User>, Event.To<FullTarget> {
    private final User source;
    private final FullTarget target;
    private final String payload;

    public PongEvent(final ThimBot bot, final User source, final FullTarget target, final String payload) {
        super(bot);
        this.source = source;
        this.target = target;
        this.payload = payload;
    }

    public void dispatch(final EventHandlerContext context, final EventHandler handler) throws Exception {
        handler.handleEvent(context, this);
    }

    public User getSource() {
        return source;
    }

    public boolean isFromMe() {
        return getBot().getBotNick().equals(source.getNick());
   }

    public FullTarget getTarget() {
        return target;
    }

    public boolean isToMe() {
        return getBot().getBotNick().equals(target);
    }

    public String getPayload() {
        return payload;
    }

    public String toString() {
        return super.toString() + " \"" + payload + "\"";
    }
}
