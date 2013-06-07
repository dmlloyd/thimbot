/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
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

package com.flurg.thimbot.source;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public class User implements Source {
    private final Nick nick;
    private final String login;
    private final String host;

    public User(final Nick nick, final String login, final String host) {
        this.nick = nick;
        this.login = login;
        this.host = host;
    }

    public String getName() {
        return nick.getName();
    }

    public Nick getNick() {
        return nick;
    }

    public String getLogin() {
        return login;
    }

    public String getHost() {
        return host;
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(Object other) {
        return other instanceof User && equals((User)other);
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(User other) {
        return this == other || other != null && nick.equals(other.nick) && login.equals(other.login) && host.equals(other.host);
    }

    public int hashCode() {
        int result = nick.hashCode();
        result = 31 * result + login.hashCode();
        result = 31 * result + host.hashCode();
        return result;
    }

    public String toString() {
        return String.format("%s!%s@%s", nick, login, host);
    }
}
