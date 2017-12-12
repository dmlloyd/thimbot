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

/**
 *
 */
public abstract class NewEventHandler {
    protected NewEventHandler() {
    }

    // all events

    public void handleEvent(final EventHandlerContext context, final NewEvent event) throws Exception {
        context.dispatch(event);
    }

    // simple events

    public void handleEvent(final EventHandlerContext context, final NewStartupEvent event) throws Exception {
        handleEvent(context, (NewEvent) event);
    }

    public void handleEvent(final EventHandlerContext context, final NewReadyEvent event) throws Exception {
        handleEvent(context, (NewEvent) event);
    }

    public void handleEvent(final EventHandlerContext context, final NewConnectEvent event) throws Exception {
        handleEvent(context, (NewEvent) event);
    }

    public void handleEvent(final EventHandlerContext context, final NewHangUpEvent event) throws Exception {
        handleEvent(context, (NewEvent) event);
    }

    public void handleEvent(final EventHandlerContext context, final NewDisconnectEvent event) throws Exception {
        handleEvent(context, (NewEvent) event);
    }

    public void handleEvent(final EventHandlerContext context, final NewHandlerExceptionEvent event) throws Exception {
        handleEvent(context, (NewEvent) event);
    }

    public void handleEvent(final EventHandlerContext context, final NewActionEvent event) throws Exception {
        handleEvent(context, (NewEvent) event);
    }

    public void handleEvent(final EventHandlerContext context, final CTCPCommandEvent event) throws Exception {
        handleEvent(context, (NewEvent) event);
    }

    public void handleEvent(final EventHandlerContext context, final CTCPResponseEvent event) throws Exception {
        handleEvent(context, (NewEvent) event);
    }

    // all protocol events

    public void handleEvent(final EventHandlerContext context, final NewProtocolEvent event) throws Exception {
        handleEvent(context, (NewEvent) event);
    }

    // protocol events

    // all protocol response events

    public void handleEvent(final EventHandlerContext context, final NewProtocolResponseEvent event) throws Exception {
        handleEvent(context, (NewProtocolEvent) event);
    }

    // protocol response events

    // all protocol command events

    public void handleEvent(final EventHandlerContext context, final NewProtocolCommandEvent event) throws Exception {
        handleEvent(context, (NewProtocolEvent) event);
    }

    // protocol command events

    public void handleEvent(final EventHandlerContext context, final NewMessageEvent event) throws Exception {
        handleEvent(context, (NewProtocolCommandEvent) event);
    }

    public void handleEvent(final EventHandlerContext context, final NewNoticeEvent event) throws Exception {
        handleEvent(context, (NewProtocolCommandEvent) event);
    }
}
