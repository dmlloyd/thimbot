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

import java.util.List;

import com.flurg.thimbot.raw.TextBuffer;

/**
 *
 */
public final class NewNoticeEvent extends NewProtocolCommandEvent {

    public NewNoticeEvent(final String source, final String rawMessage, final List<String> targets, final boolean outbound) {
        super(source, rawMessage, targets, outbound);
    }

    public boolean isAcknowledgedBy(final NewEvent inboundEvent, final String myNick) {
        return inboundEvent instanceof NewNoticeEvent && getTargets().contains(myNick) && inboundEvent.getTargets().contains(myNick) && inboundEvent.getMessage().equals(getMessage()) && myNick.equals(inboundEvent.getSourceNick());
    }

    public String getCommand() {
        return "NOTICE";
    }

    public void writeCommand(final TextBuffer output, final List<String> targets) {
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("At least one target is required for " + this);
        }
        output.append("NOTICE ");
        appendTargets(output, targets, ',');
        output.append(" :");
        output.append(getRawMessage());
    }

    public void dispatch(final EventHandlerContext context, final NewEventHandler eventHandler) throws Exception {
        eventHandler.handleEvent(context, this);
    }
}
