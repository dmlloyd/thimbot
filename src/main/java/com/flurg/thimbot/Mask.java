/*
 * Copyright 2017 by David M. Lloyd and contributors
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Mask {
    private static final Pattern ALL = Pattern.compile(".*");

    private final Pattern nickMask;
    private final Pattern nameMask;
    private final Pattern hostMask;
    private static final Pattern MASK_CHARS = Pattern.compile("(\\*)|(\\?)|[^*?]+");

    public Mask(String mask) {
        final int nickSep = mask.indexOf('!');
        final int hostSep = mask.indexOf('@', nickSep + 1);
        if (nickSep == -1) {
            nickMask = ALL;
        } else {
            nickMask = maskToPattern(mask.substring(0, nickSep));
        }
        if (hostSep == -1) {
            nameMask = ALL;
            hostMask = maskToPattern(mask.substring(nickSep + 1));
        } else {
            nameMask = maskToPattern(mask.substring(nickSep + 1, hostSep));
            hostMask = maskToPattern(mask.substring(hostSep + 1));
        }
    }

    public boolean matches(String nick, String login, String hostName) {
        if (! nickMask.matcher(nick).matches()) {
            return false;
        }
        if (! nameMask.matcher(login).matches()) {
            return false;
        }
        if (! hostMask.matcher(hostName).matches()) {
            return false;
        }
        return true;
    }

    public static Pattern maskToPattern(String mask) {
        final StringBuilder b = new StringBuilder();
        final Matcher m = MASK_CHARS.matcher(mask);
        while (m.find()) {
            if (m.group(1) != null) {
                b.append(".*");
            } else if (m.group(2) != null) {
                b.append(".");
            } else {
                b.append(Pattern.quote(m.group()));
            }
        }
        return Pattern.compile(b.toString());
    }
}
