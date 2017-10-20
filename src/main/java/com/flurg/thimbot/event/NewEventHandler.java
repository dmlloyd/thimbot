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
