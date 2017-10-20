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

package com.flurg.thimbot.event;

import java.util.List;

import com.flurg.thimbot.raw.TextBuffer;

/**
 *
 */
public abstract class AbstractCTCPEvent extends NewEvent {
    private final String command;

    AbstractCTCPEvent(final String source, final String command, final String rawMessage, final List<String> targets, final boolean outbound) {
        super(source, rawMessage, targets, outbound);
        this.command = command;
    }

    AbstractCTCPEvent(final NewProtocolCommandEvent event, final TextBuffer parseBuffer) {
        this(event.getSource(), parseCommandFrom(parseBuffer), parseMessageFrom(parseBuffer), event.getTargets(), event.isOutbound());
    }

    private static String parseCommandFrom(final TextBuffer parseBuffer) {
        return parseBuffer.getToken(' ', (char) 1);
    }

    private static String parseMessageFrom(final TextBuffer parseBuffer) {
        int cp;
        do {
            if (! parseBuffer.hasNext()) return null;
            cp = parseBuffer.readCodePoint();
        } while (cp != ':' && cp != 1);
        if (cp == 1) {
            return null;
        }
        String msg = parseBuffer.getToken((char) 1);
        if (parseBuffer.hasNext()) parseBuffer.readChar(); // 1
        return msg;
    }

    public String getCommand() {
        return command;
    }

    public abstract NewProtocolCommandEvent toProtocolCommand();
}
