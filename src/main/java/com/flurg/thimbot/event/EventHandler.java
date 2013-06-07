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

import com.flurg.thimbot.source.Target;
import com.flurg.thimbot.source.User;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public abstract class EventHandler {

    public void handleEvent(final EventHandlerContext context, final Event event) throws Exception {
        context.next(event);
    }

    public void handleEvent(final EventHandlerContext context, final ServerPingEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ServerPongEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final DisconnectEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelRedirectEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final QuitEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelJoinEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelPartEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final NoticeEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final NickChangeEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final AbstractMessageEvent<?, ?> event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final MessageEvent event) throws Exception {
        handleEvent(context, (AbstractMessageEvent<User, Target>) event);
    }

    public void handleEvent(final EventHandlerContext context, final ActionEvent event) throws Exception {
        handleEvent(context, (AbstractMessageEvent<User, Target>) event);
    }

    public void handleEvent(final EventHandlerContext context, final CTCPCommandEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final CTCPResponseEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final MOTDLineEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final MOTDEndEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final ErrorEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final PingEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }

    public void handleEvent(final EventHandlerContext context, final PongEvent event) throws Exception {
        handleEvent(context, (Event) event);
    }
}
