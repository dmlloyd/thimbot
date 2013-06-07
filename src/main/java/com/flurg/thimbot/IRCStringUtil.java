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
            "\\x03\\d{0,2},\\d{0,2}" + // standard mIRC colors
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
}
