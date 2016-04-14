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
import java.util.Set;

import com.flurg.thimbot.Priority;
import com.flurg.thimbot.ThimBot;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class OutboundActionEvent extends AbstractTextEvent implements OutboundEvent, MultiTargetEvent, MessageRespondableEvent {

    private final Set<String> targets;
    private final Priority priority;

    public OutboundActionEvent(final ThimBot bot, final Priority priority, final Set<String> targets, final String rawMessage) {
        super(bot, rawMessage);
        this.priority = priority;
        this.targets = targets;
    }

    public void dispatch(final EventHandlerContext context, final EventHandler handler) throws Exception {
        handler.handleEvent(context, this);
    }

    public Set<String> getTargets() {
        return targets;
    }


    public void sendMessageResponse(final Priority priority, final String message) throws IOException {
        getBot().sendMessage(priority, targets, message);
    }

    public void sendMessageResponse(final String message) throws IOException {
        sendMessageResponse(Priority.NORMAL, message);
    }

    public void sendActionResponse(final Priority priority, final String message) throws IOException {
        getBot().sendAction(priority, targets, message);
    }

    public void sendActionResponse(final String message) throws IOException {
        sendActionResponse(Priority.NORMAL, message);
    }

    public String[] getResponseTargets() {
        return targets.toArray(new String[targets.size()]);
    }

    public Priority getPriority() {
        return priority;
    }
}
