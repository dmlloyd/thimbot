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

package com.flurg.thimbot.util;

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class IRCStringUtil {

    private IRCStringUtil() {
    }

    private static final Pattern DEFORMAT = Pattern.compile(
        "(?:" +
            "[\\x02\\x0F\\x11\\x12\\x16\\x1d\\x1f]" + // single character color codes
            "|" +
            "\\x03\\d{0,2}(?:,\\d{0,2})?" + // standard mIRC colors
            "|" +
            "\\x04[0-9a-fA-F]{6}" + // VisualIRC-style RGB codes
            "|" +
            "\\x1b\\[[?=]?(?:\\d+(?:;\\d+)*)?[@-_]" + // ANSI argument sequences
        ")+");

    /**
     * Strip IRC colors and formatting from a string.
     *
     * @param original the original
     * @return the clean string
     */
    public static String deformat(String original) {
        return DEFORMAT.matcher(original).replaceAll("");
    }

    /**
     * Determine whether the given string is a channel name.
     *
     * @param name the target name
     * @return {@code true} if the name is a channel, {@code false} otherwise
     */
    public static boolean isChannel(String name) {
        if (name.length() < 2) {
            return false;
        }
        final char zero = name.charAt(0);
        switch (zero) {
            case '#':
            case '!':
            case '&':
                return true;
            case '+':
            case '@':
            case '%':
                switch (name.charAt(1)) {
                    case '#':
                    case '!':
                    case '&':
                        return true;
                    default:
                        return false;
                }
            default:
                return false;
        }
    }

    /**
     * Determine whether the given string is a wildcard.
     *
     * @param name the target name
     * @return {@code true} if the name is a wildcard, {@code false} otherwise
     */
    public static boolean isWildcard(String name) {
        return name.equals("*");
    }

    public static boolean isChannelVoiced(String name) {
        return isChannel(name) && name.charAt(0) == '+';
    }

    public static boolean isChannelOps(String name) {
        return isChannel(name) && name.charAt(0) == '@';
    }

    public static boolean isChannelHalfOps(String name) {
        return isChannel(name) && name.charAt(0) == '%';
    }

    public static String getRawChannelName(String name) {
        switch (name.charAt(0)) {
            case '+':
            case '@':
            case '%':
                return name.substring(1);
            default:
                return name;
        }
    }

    public static boolean isUser(final String name) {
        if (name.length() < 1) {
            return false;
        }
        final char zero = name.charAt(0);
        switch (zero) {
            case '#':
            case '!':
            case '&':
            case '*':
                return false;
            case '+':
            case '@':
            case '%':
                switch (name.charAt(1)) {
                    case '#':
                    case '!':
                    case '&':
                    case '*':
                        return false;
                    default: // sure
                        return true;
                }
            default:
                return true;
        }
    }

    public static String nickOf(final String source) {
        if (source.length() < 1) {
            return "";
        }
        final int si;
        switch (source.charAt(0)) {
            case '#':
            case '!':
            case '&':
            case '*':
                return "";
            case '+':
            case '@':
            case '%':
                si = 1;
                break;
            default:
                si = 0;
                break;
        }
        final int idx = source.indexOf('!');
        if (idx != -1) {
            return source.substring(si, idx);
        } else {
            return source;
        }
    }
}
