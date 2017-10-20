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

import com.flurg.thimbot.ThimBot;
import com.flurg.thimbot.event.AbstractEvent;
import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;
import com.flurg.thimbot.util.IRCStringUtil;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class PrivateCTCPCommandEvent extends AbstractEvent implements FromUserEvent, InboundEvent {
    private final String nick;
    private final String user;
    private final String command;
    private final String argument;
    private final boolean fromMe;

    public PrivateCTCPCommandEvent(final ThimBot bot, final String user, final String command, final String argument) {
        super(bot);
        this.user = user;
        nick = IRCStringUtil.nickOf(user);
        this.command = command;
        this.argument = argument;
        fromMe = getBot().getBotNick().equals(nick);
    }

    public void dispatch(final EventHandlerContext context, final EventHandler handler) throws Exception {
        handler.handleEvent(context, this);
    }

    public boolean isFromMe() {
        return fromMe;
    }

    public String getFromNick() {
        return nick;
    }

    public String getFromUser() {
        return user;
    }

    public String getCommand() {
        return command;
    }

    public String getArgument() {
        return argument;
    }

    protected void toStringAddendum(final StringBuilder b) {
        b.append(" cmd=").append(command).append(" \"").append(argument).append('"');
    }
}
