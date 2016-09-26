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
import com.flurg.thimbot.util.IRCStringUtil;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public abstract class AbstractTextEvent extends Event implements TextEvent {

    private final String rawMessage;
    private final String message;

    protected AbstractTextEvent(final ThimBot bot, final String rawMessage) {
        super(bot);
        this.rawMessage = rawMessage;
        message = IRCStringUtil.deformat(rawMessage);
    }

    public String getRawText() {
        return rawMessage;
    }

    public String getText() {
        return message;
    }
}
