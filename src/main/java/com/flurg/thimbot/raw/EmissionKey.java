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

package com.flurg.thimbot.raw;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class EmissionKey {
    private final StringEmitter command;
    private final StringEmitter message;

    public EmissionKey(final StringEmitter command, final StringEmitter message) {
        this.command = command;
        this.message = message;
    }

    public StringEmitter getCommand() {
        return command;
    }

    public StringEmitter getMessage() {
        return message;
    }

    public boolean equals(final Object obj) {
        return obj instanceof EmissionKey && equals((EmissionKey) obj);
    }

    private boolean equals(final EmissionKey key) {
        // message is less likely to be equal
        return message.equals(key.message) && command.equals(key.command);
    }

    public int hashCode() {
        return command.hashCode() * 17 + message.hashCode();
    }
}
