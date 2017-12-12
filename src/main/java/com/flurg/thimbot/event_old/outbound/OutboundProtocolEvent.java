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

package com.flurg.thimbot.event_old.outbound;

import java.io.IOException;

import com.flurg.thimbot.event_old.inbound.InboundEvent;
import com.flurg.thimbot.raw.IRCOutput;

/**
 * An outbound event that corresponds to a protocol message which is emitted when the event is processed.
 */
public interface OutboundProtocolEvent extends OutboundEvent {

    void writeProtocolMessage(IRCOutput target) throws IOException;

    /**
     * Determine whether this outbound event is acknowledged by the given inbound event.
     *
     * @param event the inbound event
     * @return {@code true} if the outbound event should be marked as acknowledged, {@code false} otherwise
     */
    boolean isAcknowledgedBy(InboundEvent event);
}
