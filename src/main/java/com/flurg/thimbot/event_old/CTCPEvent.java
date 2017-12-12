/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.flurg.thimbot.event_old;

import java.util.List;

import com.flurg.thimbot.raw.TextBuffer;

/**
 *
 */
public abstract class CTCPEvent extends NewEvent {
    private final String command;

    CTCPEvent(final String source, final String command, final String rawMessage, final List<String> targets, final boolean outbound) {
        super(source, rawMessage, targets, outbound);
        this.command = command;
    }

    CTCPEvent(final NewProtocolCommandEvent event, final TextBuffer parseBuffer) {
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
