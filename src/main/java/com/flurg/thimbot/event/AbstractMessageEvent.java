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

import java.io.IOException;

import com.flurg.thimbot.Priority;
import com.flurg.thimbot.ThimBot;
import com.flurg.thimbot.source.Source;
import com.flurg.thimbot.source.Target;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public abstract class AbstractMessageEvent<S extends Source, T extends Target> extends Event implements Event.From<S>, Event.To<T> {

    private final String rawMessage;
    private final String message;
    private final S source;
    private final T target;

    protected AbstractMessageEvent(final ThimBot bot, final S source, final T target, final String rawMessage) {
        super(bot);
        this.rawMessage = rawMessage;
        this.source = source;
        this.target = target;
        message = MessageUtil.deformat(rawMessage);
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public String getMessage() {
        return message;
    }

    public S getSource() {
        return source;
    }

    public boolean isFromMe() {
        return getBot().getBotNick().equals(source);
    }

    public T getTarget() {
        return target;
    }

    public boolean isToMe() {
        return getBot().getBotNick().equals(target);
    }

    public void respond(String message) throws IOException {
        respond(Priority.NORMAL, message);
    }

    public void respond(Priority priority, String message) throws IOException {
        getBot().sendMessage(priority, isToMe() && source instanceof Target ? (Target) source : target, message);
    }

    public String toString() {
        return super.toString() + " \"" + message + "\"";
    }
}
