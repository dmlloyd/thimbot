/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class Capability {
    private final String name;
    private final boolean sticky;
    private final boolean disable;
    private final boolean ack;
    private final int hashCode;

    public Capability(final String name, final boolean sticky, final boolean disable, final boolean ack) {
        this.name = name;
        this.sticky = sticky;
        this.disable = disable;
        this.ack = ack;
        int hashCode = name.hashCode();
        if (sticky) {
            hashCode += 5;
        }
        if (disable) {
            hashCode += 13;
        }
        if (ack) {
            hashCode += 23;
        }
        this.hashCode = hashCode;
    }

    public String getName() {
        return name;
    }

    public boolean isSticky() {
        return sticky;
    }

    public boolean isDisable() {
        return disable;
    }

    public boolean isAck() {
        return ack;
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(Object other) {
        return other instanceof Capability && equals((Capability)other);
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(Capability other) {
        return this == other || other != null && hashCode == other.hashCode && name.equals(other.name) && sticky == other.sticky && disable == other.disable && ack == other.ack;
    }

    public int hashCode() {
        return hashCode;
    }

    public String toString() {
        StringBuilder b = new StringBuilder(name.length() + 3);
        if (disable) b.append('-');
        if (sticky) b.append('=');
        if (ack) b.append('~');
        b.append(name);
        return b.toString();
    }

    public static Capability fromString(String str) {
        int idx = 0;
        boolean disable = false;
        boolean sticky = false;
        boolean ack = false;
        int cp;
        while (idx < str.length()) {
            cp = str.codePointAt(idx);
            if (cp == '-') {
                disable = true;
                idx = str.offsetByCodePoints(idx, 1);
            } else if (cp == '=') {
                sticky = true;
                idx = str.offsetByCodePoints(idx, 1);
            } else if (cp == '~') {
                ack = true;
                idx = str.offsetByCodePoints(idx, 1);
            } else {
                return new Capability(str.substring(idx), sticky, disable, ack);
            }
        }
        return null;
    }
}
