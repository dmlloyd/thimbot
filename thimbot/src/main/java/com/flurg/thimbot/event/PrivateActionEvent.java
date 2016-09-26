/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import java.io.IOException;

import com.flurg.thimbot.Priority;
import com.flurg.thimbot.ThimBot;
import com.flurg.thimbot.util.IRCStringUtil;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class PrivateActionEvent extends AbstractTextEvent implements InboundEvent, FromUserEvent, MessageRespondableEvent {

    private final String user;
    private final String nick;
    private final boolean fromMe;

    public PrivateActionEvent(final ThimBot bot, final String user, final String rawMessage) {
        super(bot, rawMessage);
        this.user = user;
        nick = IRCStringUtil.nickOf(user);
        fromMe = bot.getBotNick().equals(nick);
    }

    public String getFromNick() {
        return nick;
    }

    public String getFromUser() {
        return user;
    }

    public boolean isFromMe() {
        return fromMe;
    }

    public void dispatch(final EventHandlerContext context, final EventHandler handler) throws Exception {
        handler.handleEvent(context, this);
    }

    public void sendMessageResponse(final Priority priority, final String message) throws IOException {
        getBot().sendMessage(priority, nick, message);
    }

    public void sendMessageResponse(final String message) throws IOException {
        sendMessageResponse(Priority.NORMAL, message);
    }

    public void sendActionResponse(final Priority priority, final String message) throws IOException {
        getBot().sendAction(priority, nick, message);
    }

    public void sendActionResponse(final String message) throws IOException {
        sendActionResponse(Priority.NORMAL, message);
    }

    public String[] getResponseTargets() {
        return new String[] { nick };
    }
}
