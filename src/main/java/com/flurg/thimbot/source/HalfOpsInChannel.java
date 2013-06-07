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

import java.io.IOException;
import java.util.prefs.Preferences;

import com.flurg.thimbot.raw.ByteOutput;
import com.flurg.thimbot.raw.StringEmitter;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public class HalfOpsInChannel implements FullTarget {
    private final Channel channel;

    public HalfOpsInChannel(final Channel channel) {
        this.channel = channel;
    }

    public String getName() {
        return channel.getName();
    }

    public void performSendNotice(final ByteOutput target, final StringEmitter contents) throws IOException {
        channel.performSendNotice(target, '%', contents);
    }

    public void performSendCTCPCommand(final ByteOutput target, final StringEmitter command, final StringEmitter argument) throws IOException {
        channel.performSendCTCPCommand(target, '%', command, argument);
    }

    public void performSendCTCPResponse(final ByteOutput target, final StringEmitter command, final StringEmitter argument) throws IOException {
        channel.performSendCTCPResponse(target, '%', command, argument);
    }

    public void performSendMessage(final ByteOutput target, final StringEmitter contents) throws IOException {
        channel.performSendMessage(target, '%', contents);
    }

    public void performSendAction(final ByteOutput target, final StringEmitter contents) throws IOException {
        channel.performSendAction(target, '%', contents);
    }

    public Preferences getPreferences(final Preferences ourRoot) {
        return channel.getPreferences(ourRoot);
    }

    public void writeName(final ByteOutput target) throws IOException {
        target.write('%');
        channel.writeName(target);
    }

    public String toString() {
        return "%" + channel;
    }
}
