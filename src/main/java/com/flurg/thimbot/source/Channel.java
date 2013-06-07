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

import com.flurg.thimbot.IRCStrings;
import com.flurg.thimbot.raw.ByteOutput;
import com.flurg.thimbot.raw.StringEmitter;
import java.io.IOException;
import java.util.prefs.Preferences;

import static com.flurg.thimbot.IRCStrings.*;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public class Channel implements FullTarget {

    private final StringEmitter nameEmitter;
    private final String name;

    public Channel(final String name) {
        this.name = name;
        nameEmitter = new StringEmitter(name);
    }

    public String getName() {
        return name;
    }

    void performSendNotice(final ByteOutput target, final char flagChar, final StringEmitter contents) throws IOException {
        target.write(NOTICE);
        target.write(' ');
        if (flagChar != 0) target.write(flagChar);
        target.write(nameEmitter);
        target.write(' ');
        target.write(':');
        target.write(contents);
    }

    public void performSendNotice(final ByteOutput target, final StringEmitter contents) throws IOException {
        performSendNotice(target, (char) 0, contents);
    }

    void performSendCTCPCommand(final ByteOutput target, final char flagChar, final StringEmitter command, final StringEmitter argument) throws IOException {
        target.write(PRIVMSG);
        target.write(' ');
        if (flagChar != 0) target.write(flagChar);
        target.write(nameEmitter);
        target.write(' ');
        target.write(':');
        target.write(1);
        target.write(command);
        if (argument != null) {
            target.write(' ');
            target.write(argument);
        }
        target.write(1);
    }

    public void performSendCTCPCommand(final ByteOutput target, final StringEmitter command, final StringEmitter argument) throws IOException {
        performSendCTCPCommand(target, (char)0, command, argument);
    }

    void performSendCTCPResponse(final ByteOutput target, final char flagChar, final StringEmitter command, final StringEmitter argument) throws IOException {
        target.write(NOTICE);
        target.write(' ');
        if (flagChar != 0) target.write(flagChar);
        target.write(nameEmitter);
        target.write(' ');
        target.write(':');
        target.write(1);
        target.write(command);
        if (argument != null) {
            target.write(' ');
            target.write(argument);
        }
        target.write(1);
    }

    public void performSendCTCPResponse(final ByteOutput target, final StringEmitter command, final StringEmitter argument) throws IOException {
        performSendCTCPCommand(target, (char)0, command, argument);
    }

    void performSendMessage(final ByteOutput target, final char flagChar, final StringEmitter contents) throws IOException {
        target.write(PRIVMSG);
        target.write(' ');
        if (flagChar != 0) target.write(flagChar);
        target.write(nameEmitter);
        target.write(' ');
        target.write(':');
        target.write(contents);
    }

    public void performSendMessage(final ByteOutput target, final StringEmitter contents) throws IOException {
        performSendMessage(target, (char)0, contents);
    }

    void performSendAction(final ByteOutput target, final char flagChar, final StringEmitter contents) throws IOException {
        target.write(PRIVMSG);
        target.write(' ');
        if (flagChar != 0) target.write(flagChar);
        target.write(nameEmitter);
        target.write(' ');
        target.write(':');
        target.write(1);
        target.write(IRCStrings.ACTION);
        target.write(' ');
        target.write(contents);
        target.write(1);
    }

    public void performSendAction(final ByteOutput target, final StringEmitter contents) throws IOException {
        performSendAction(target, (char) 0, contents);
    }

    public Preferences getPreferences(final Preferences ourRoot) {
        return ourRoot.node("channels").node(name);
    }

    public void writeName(final ByteOutput target) throws IOException {
        target.write(nameEmitter);
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(Object other) {
        return other instanceof Channel && equals((Channel)other);
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(Channel other) {
        return this == other || other != null && name.equals(other.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return name;
    }
}
