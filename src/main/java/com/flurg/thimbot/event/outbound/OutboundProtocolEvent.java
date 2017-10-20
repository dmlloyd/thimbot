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

package com.flurg.thimbot.event.outbound;

import java.io.IOException;

import com.flurg.thimbot.event.inbound.InboundEvent;
import com.flurg.thimbot.raw.IRCOutput;

/**
 * An outbound event that corresponds to a protocol message which is emitted when the event is processed.
 */
public interface OutboundProtocolEvent extends OutboundEvent {

    void writeProtocolMessage(IRCOutput target) throws IOException;

    /**
     * Determine whether this outbound event is acknowledged by the given inbound event.
     *
     * @param event the inbound event
     * @return {@code true} if the outbound event should be marked as acknowledged, {@code false} otherwise
     */
    boolean isAcknowledgedBy(InboundEvent event);
}
