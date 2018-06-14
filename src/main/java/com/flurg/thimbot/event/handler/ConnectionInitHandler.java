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

import java.util.Collections;

import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;
import com.flurg.thimbot.event.connection.ConnectEvent;
import com.flurg.thimbot.event.connection.ReadyEvent;
import com.flurg.thimbot.event.irc.cap.CapListSupportedEvent;
import com.flurg.thimbot.event.irc.command.NickEvent;
import com.flurg.thimbot.event.irc.command.UserEvent;
import com.flurg.thimbot.event.irc.response.ResponseEvent;

/**
 */
public class ConnectionInitHandler extends EventHandler {
    public void handleEvent(final EventHandlerContext context, final ConnectEvent event) {
        super.handleEvent(context, event);
        final String nickName = context.getAssociation().getPreferredNickName();
        final String realName = context.getAssociation().getRealName();
        context.dispatch(new CapListSupportedEvent(true, null, Collections.emptyList()));
        // XXX specify a source from the IRC state; prefer bot.changeNick(context) etc maybe
        context.dispatch(new NickEvent(true, null, nickName));
        context.dispatch(new UserEvent(nickName, false, false, realName));
    }

    public void handleEvent(final EventHandlerContext context, final ResponseEvent event) {
        super.handleEvent(context, event);
        if (event.getCode() == 1) {
            context.dispatch(new ReadyEvent());
        }
    }
}
