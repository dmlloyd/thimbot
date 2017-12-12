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
public final class CTCPResponseEvent extends CTCPEvent {
    public CTCPResponseEvent(final String source, final String command, final String rawMessage, final List<String> targets, final boolean outbound) {
        super(source, command, rawMessage, targets, outbound);
    }

    public CTCPResponseEvent(final NewProtocolCommandEvent event, final TextBuffer parseBuffer) {
        super(event, parseBuffer);
    }

    public void dispatch(final EventHandlerContext context, final NewEventHandler eventHandler) throws Exception {

    }

    public NewNoticeEvent toProtocolCommand() {

    }

    /**
     * Extract CTCP events from the given notice event, and return the transformed notice event.
     *
     * @param noticeEvent the notice event (must not be {@code null})
     * @param dest the list to append any found CTCP events to (must not be {@code null})
     * @return the new notice event, or {@code null} if the content was only CTCP messages
     */
    public static NewNoticeEvent fromMessageEvent(NewNoticeEvent noticeEvent, List<CTCPResponseEvent> dest) {

    }
}
