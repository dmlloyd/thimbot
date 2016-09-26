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

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class Arrays2 {

    private Arrays2() {
    }

    public static boolean equals(byte[] a1, int offs1, int len1, byte[] a2, int offs2, int len2) {
        if (len1 != len2) {
            return false;
        }
        for (int i = 0; i < len1; i ++) {
            if (a1[offs1 + i] != a2[offs2 + i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean equals(byte[] a1, int offs, int len, byte[] a2) {
        return equals(a1, offs, len, a2, 0, a2.length);
    }

    public static boolean equals(byte[] a1, int offs, byte[] a2) {
        return equals(a1, offs, a2.length, a2, 0, a2.length);
    }

    public static String[] of(final String... s) {
        return s;
    }
}
