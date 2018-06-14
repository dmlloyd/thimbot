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
import com.flurg.thimbot.event.irc.ProtocolEvent;
import com.flurg.thimbot.event.irc.response.ResponseCode;
import com.flurg.thimbot.event.irc.response.ResponseEvent;

/**
 */
public final class NickEvent extends IrcCommandEvent {
    private String nickName;

    public NickEvent(final boolean outbound, final String source, final String nickName) {
        super(outbound, source, Collections.emptyList());
        this.nickName = nickName;
    }

    public String getCommand() {
        return "NICK";
    }

    public void accept(final EventHandlerContext context, final EventHandler eventHandler) {
        eventHandler.handleEvent(context, this);
    }

    public NickEvent clone() {
        return (NickEvent) super.clone();
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(final String nickName) {
        this.nickName = nickName;
    }

    public boolean isAcknowledgedBy(final ProtocolEvent incomingEvent) {
        if (incomingEvent instanceof ResponseEvent) {
            switch (((ResponseEvent) incomingEvent).getCode()) {
                case ResponseCode.ERR_EVENT_NICK_CHANGE:
                case ResponseCode.ERR_NO_NICK_GIVEN:
                case ResponseCode.ERR_INVALID_NICK:
                case ResponseCode.ERR_NICK_IN_USE:
                case ResponseCode.ERR_NICK_COLLISION:
                case ResponseCode.ERR_UNAVAILABLE_RESOURCE:
                case ResponseCode.ERR_NICK_TOO_FAST:
                case ResponseCode.ERR_NICK_LOCKED: {
                    return true;
                }
                default: {
                    return false;
                }
            }
        } else if (incomingEvent instanceof NickEvent) {
            return nickName.equals(((NickEvent) incomingEvent).getNickName());
        }
    }
}
