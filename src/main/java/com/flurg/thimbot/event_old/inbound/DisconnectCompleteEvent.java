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

package com.flurg.thimbot.event_old.inbound;

import com.flurg.thimbot.ThimBot;
import com.flurg.thimbot.event_old.AbstractEvent;
import com.flurg.thimbot.event_old.EventHandler;
import com.flurg.thimbot.event_old.EventHandlerContext;

/**
 * An event signalling that the client is fully disconnected from the server (both the read and write sides have
 * hung up).  A new connection attempt may be initiated.
 */
public final class DisconnectCompleteEvent extends AbstractEvent implements InboundEvent {

    public DisconnectCompleteEvent(final ThimBot bot) {
        super(bot);
    }

    public void dispatch(final EventHandlerContext context, final EventHandler handler) throws Exception {
        handler.handleEvent(context, this);
    }
}