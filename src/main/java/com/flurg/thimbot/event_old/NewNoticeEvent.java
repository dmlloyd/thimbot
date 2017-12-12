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
public final class NewNoticeEvent extends NewProtocolCommandEvent {

    public NewNoticeEvent(final String source, final String rawMessage, final List<String> targets, final boolean outbound) {
        super(source, rawMessage, targets, outbound);
    }

    public boolean isAcknowledgedBy(final NewEvent inboundEvent, final String myNick) {
        return inboundEvent instanceof NewNoticeEvent && getTargets().contains(myNick) && inboundEvent.getTargets().contains(myNick) && inboundEvent.getMessage().equals(getMessage()) && myNick.equals(inboundEvent.getSourceNick());
    }

    public String getCommand() {
        return "NOTICE";
    }

    public void writeCommand(final TextBuffer output, final List<String> targets) {
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("At least one target is required for " + this);
        }
        output.append("NOTICE ");
        appendTargets(output, targets, ',');
        output.append(" :");
        output.append(getRawMessage());
    }

    public void dispatch(final EventHandlerContext context, final NewEventHandler eventHandler) throws Exception {
        eventHandler.handleEvent(context, this);
    }
}
