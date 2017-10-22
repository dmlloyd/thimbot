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

import java.util.Iterator;
import java.util.List;

import com.flurg.thimbot.raw.TextBuffer;

/**
 *
 */
public abstract class NewProtocolCommandEvent extends NewProtocolEvent {
    protected NewProtocolCommandEvent(final String source, final String rawMessage, final List<String> targets, final boolean outbound) {
        super(source, rawMessage, targets, outbound);
    }

    public abstract boolean isAcknowledgedBy(final NewEvent inboundEvent, final String myNick);

    public void toString(final StringBuilder builder) {

    }

    protected void appendTargets(TextBuffer output, final List<String> targets, char delimiter) {
        final Iterator<String> iterator = getTargets().iterator();
        if (iterator.hasNext()) {
            output.append(iterator.next());
            while (iterator.hasNext()) {
                output.appendChar(delimiter);
                output.append(iterator.next());
            }
        }
    }

    public abstract String getCommand();

    public abstract void writeCommand(TextBuffer output, final List<String> targets);
}
