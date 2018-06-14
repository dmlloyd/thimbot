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

package com.flurg.thimbot.event.irc.command;

import java.util.Collections;

import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;

/**
 */
public final class InviteEvent extends IrcCommandEvent {
    private final String invitee;
    private final String channel;

    public InviteEvent(final boolean outbound, final String source, final String invitee, final String channel) {
        super(outbound, source, Collections.emptyList());
        this.invitee = invitee;
        this.channel = channel;
    }

    public String getCommand() {
        return "INVITE";
    }

    public void accept(final EventHandlerContext context, final EventHandler eventHandler) {
        eventHandler.handleEvent(context, this);
    }

    public InviteEvent clone() {
        return (InviteEvent) super.clone();
    }

    public String getInvitee() {
        return invitee;
    }

    public String getChannel() {
        return channel;
    }
}
