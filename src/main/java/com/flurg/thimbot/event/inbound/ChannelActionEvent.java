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

package com.flurg.thimbot.event.inbound;

import java.io.IOException;

import com.flurg.thimbot.Priority;
import com.flurg.thimbot.ThimBot;
import com.flurg.thimbot.event.AbstractTextEvent;
import com.flurg.thimbot.event.ChannelEvent;
import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;
import com.flurg.thimbot.event.MessageRespondableEvent;
import com.flurg.thimbot.util.IRCStringUtil;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class ChannelActionEvent extends AbstractTextEvent implements InboundEvent, FromUserEvent, ChannelEvent, MessageRespondableEvent {

    private final String nick;
    private final String user;
    private final String channel;
    private final boolean fromMe;

    public ChannelActionEvent(final ThimBot bot, final String user, final String channel, final String rawMessage) {
        super(bot, rawMessage);
        this.channel = channel;
        this.user = user;
        nick = IRCStringUtil.nickOf(user);
        fromMe = getBot().getBotNick().equals(nick);
    }

    public void dispatch(final EventHandlerContext context, final EventHandler handler) throws Exception {
        handler.handleEvent(context, this);
    }

    public String getChannel() {
        return channel;
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

    public void sendMessageResponse(final Priority priority, final String message) throws IOException {
        getBot().sendMessage(priority, channel, message);
    }

    public void sendMessageResponse(final String message) throws IOException {
        sendMessageResponse(Priority.NORMAL, message);
    }

    public void sendActionResponse(final Priority priority, final String message) throws IOException {
        getBot().sendAction(priority, channel, message);
    }

    public void sendActionResponse(final String message) throws IOException {
        sendActionResponse(Priority.NORMAL, message);
    }

    public String[] getResponseTargets() {
        return new String[] { channel };
    }
}
