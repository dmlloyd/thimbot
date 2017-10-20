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

/**
 *
 */
public final class CTCPCommandEvent extends AbstractCTCPEvent {
    public CTCPCommandEvent(final String source, final String rawMessage, final List<String> targets, final boolean outbound) {
        super(source, command, rawMessage, targets, outbound);
    }

    public void dispatch(final EventHandlerContext context, final NewEventHandler eventHandler) throws Exception {

    }

    public NewProtocolCommandEvent toProtocolCommand() {

    }

    /**
     * Extract CTCP events from the given message event, and return the transformed message event.
     *
     * @param messageEvent the message event (must not be {@code null})
     * @param dest the list to append any found CTCP events to (must not be {@code null})
     * @return the new message event, or {@code null} if the content was only CTCP messages
     */
    public static NewMessageEvent fromMessageEvent(NewMessageEvent messageEvent, List<CTCPCommandEvent> dest) {

    }
}
