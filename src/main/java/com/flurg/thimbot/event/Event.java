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
import com.flurg.thimbot.source.Source;
import com.flurg.thimbot.source.Target;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public abstract class Event {

    public interface To<T extends Target> {
        T getTarget();

        boolean isToMe();
    }

    public interface From<T extends Source> {
        T getSource();

        boolean isFromMe();
    }

    public interface Regarding<T extends Target> {
        T getRegarding();

        boolean isRegardingMe();
    }

    private final int seq;
    private final ThimBot bot;

    protected Event(final ThimBot bot) {
        this.seq = bot.getEventSequence();
        this.bot = bot;
    }

    public int getSeq() {
        return seq;
    }

    public ThimBot getBot() {
        return bot;
    }

    public abstract void dispatch(EventHandlerContext context, EventHandler handler) throws Exception;

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getSimpleName());
        if (this instanceof From) {
            b.append(" from ").append(((From<?>)this).getSource());
        }
        if (this instanceof To) {
            b.append(" to ").append(((To<?>)this).getTarget());
        }
        if (this instanceof Regarding) {
            b.append(" regarding ").append(((Regarding<?>)this).getRegarding());
        }
        return b.toString();
    }
}
