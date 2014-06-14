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

package com.flurg.thimbot;

import com.flurg.thimbot.raw.StringEmitter;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class IRCStrings {

    public static final StringEmitter ACTION = new StringEmitter("ACTION");
    public static final StringEmitter AUTHENTICATE = new StringEmitter("AUTHENTICATE");
    public static final StringEmitter CAP = new StringEmitter("CAP");
    public static final StringEmitter JOIN = new StringEmitter("JOIN");
    public static final StringEmitter MODE = new StringEmitter("MODE");
    public static final StringEmitter NICK = new StringEmitter("NICK");
    public static final StringEmitter NOTICE = new StringEmitter("NOTICE");
    public static final StringEmitter PART = new StringEmitter("PART");
    public static final StringEmitter PING = new StringEmitter("PING");
    public static final StringEmitter PONG = new StringEmitter("PONG");
    public static final StringEmitter PRIVMSG = new StringEmitter("PRIVMSG");
    public static final StringEmitter QUIT = new StringEmitter("QUIT");
    public static final StringEmitter USER = new StringEmitter("USER");

    public static final StringEmitter ACK = new StringEmitter("ACK");
    public static final StringEmitter CLEAR = new StringEmitter("CLEAR");
    public static final StringEmitter END = new StringEmitter("END");
    public static final StringEmitter LIST = new StringEmitter("LIST");
    public static final StringEmitter LS = new StringEmitter("LS");
    public static final StringEmitter NAK = new StringEmitter("NAK");
    public static final StringEmitter REQ = new StringEmitter("REQ");

    private IRCStrings() {
    }
}
