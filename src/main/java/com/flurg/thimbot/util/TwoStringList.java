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

package com.flurg.thimbot.util;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public final class TwoStringList extends AbstractList<String> {
    private final String s1, s2;

    private TwoStringList(final String s1, final String s2) {
        this.s1 = s1;
        this.s2 = s2;
    }

    public static List<String> create(String s1, String s2) {
        if (s1.equals(s2)) {
            return Collections.singletonList(s1);
        } else {
            return new TwoStringList(s1, s2);
        }
    }

    public String get(final int index) {
        if (index == 0) {
            return s1;
        } else if (index == 1) {
            return s2;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public int indexOf(final Object o) {
        if (o instanceof String) {
            final String s = (String) o;
            if (s.equals(s1)) return 0;
            if (s.equals(s2)) return 1;
        }
        return -1;
    }

    public int lastIndexOf(final Object o) {
        if (o instanceof String) {
            final String s = (String) o;
            if (s.equals(s2)) return 1;
            if (s.equals(s1)) return 0;
        }
        return -1;
    }

    public List<String> subList(final int fromIndex, final int toIndex) {
        if (fromIndex > toIndex) throw new IllegalArgumentException();
        if (fromIndex == 0) {
            if (toIndex == 2) {
                return this;
            } else if (toIndex == 1) {
                return Collections.singletonList(s1);
            } else if (toIndex == 0) {
                return Collections.emptyList();
            }
        } else if (fromIndex == 1) {
            if (toIndex == 2) {
                return Collections.singletonList(s2);
            } else if (toIndex == 1) {
                return Collections.emptyList();
            }
        } else if (fromIndex == 2) {
            if (toIndex == 2) {
                return Collections.emptyList();
            }
        }
        throw new IndexOutOfBoundsException();
    }

    public int size() {
        return 2;
    }
}
