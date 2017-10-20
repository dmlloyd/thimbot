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

import java.util.Collections;

/**
 * An event indicating that the system is ready.  When this message is received, then normal IRC operations can commence
 * (joining channels, sending messages, etc.).  This event is generally sent with things like authentication are complete.
 */
public final class NewReadyEvent extends NewEvent {
    public NewReadyEvent() {
        super(null, null, Collections.emptyList(), false);
    }

    public void dispatch(final EventHandlerContext context, final NewEventHandler eventHandler) throws Exception {
        eventHandler.handleEvent(context, this);
    }
}
