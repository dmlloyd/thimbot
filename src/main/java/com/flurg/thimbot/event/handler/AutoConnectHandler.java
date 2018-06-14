/*
 * Copyright 2017 by David M. Lloyd and contributors
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

package com.flurg.thimbot.event.handler;

import java.util.concurrent.TimeUnit;

import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;
import com.flurg.thimbot.event.connection.ConnectEvent;
import com.flurg.thimbot.event.connection.ConnectFailedEvent;
import com.flurg.thimbot.event.connection.DisconnectEvent;
import com.flurg.thimbot.event.connection.ReadyEvent;

/**
 */
public final class AutoConnectHandler extends EventHandler {
    public static final AutoConnectHandler INSTANCE = new AutoConnectHandler();

    private AutoConnectHandler() {}

    public void handleEvent(final EventHandlerContext context, final ReadyEvent event) {
        context.dispatch(new ConnectEvent(true));
    }

    public void handleEvent(final EventHandlerContext context, final ConnectFailedEvent event) {
        context.getAssociation().getScheduledExecutor().schedule(() -> context.dispatch(new ConnectEvent(true)), 10, TimeUnit.SECONDS);
    }

    public void handleEvent(final EventHandlerContext context, final DisconnectEvent event) {
        context.getAssociation().getScheduledExecutor().schedule(() -> context.dispatch(new ConnectEvent(true)), 3, TimeUnit.SECONDS);
    }

    public void handleEvent(final EventHandlerContext context, final ConnectEvent event) {
        super.handleEvent(context, event);
    }
}
