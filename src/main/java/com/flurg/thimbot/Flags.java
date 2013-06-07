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

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class Flags {

    private Flags() {
    }

    public static int getBitPosition(final char flag) {
        int sh;
        if (flag >= '0' && flag <= '9') {
            sh = flag - '0';
        } else if (flag >= 'A' && flag <= 'Z') {
            sh = flag - 'A' + 10;
        } else if (flag >= 'a' && flag <= 'z') {
            sh = flag - 'a' + 36;
        } else {
            throw new IllegalArgumentException("Invalid flag character '" + flag + "'");
        }
        return sh;
    }

    public static long getBits(final char flag) {
        return 1L << getBitPosition(flag);
    }

    public static long getBits(final char flag1, final char flag2) {
        return getBits(flag1) | getBits(flag2);
    }

    public static long getBits(final char... flags) {
        long t = 0L;
        if (flags != null) for (final char flag : flags) {
            t |= getBits(flag);
        }
        return t;
    }

    public static long setFlags(long orig, char flag) {
        return orig | getBits(flag);
    }

    public static long setFlags(long orig, char flag1, char flag2) {
        return orig | getBits(flag1, flag2);
    }

    public static long setFlags(long orig, char... flags) {
        return orig | getBits(flags);
    }

    public static long clearFlags(long orig, char flag) {
        return orig & ~getBits(flag);
    }

    public static long clearFlags(long orig, char flag1, char flag2) {
        return orig & ~getBits(flag1, flag2);
    }

    public static long clearFlags(long orig, char... flags) {
        return orig & ~getBits(flags);
    }

    public static final int AWAY_FLAG_POS = 63;
    public static final long AWAY_FLAG = 1L << AWAY_FLAG_POS;
}
