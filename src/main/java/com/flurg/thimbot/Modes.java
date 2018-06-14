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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A map of modes, parameters, and add/remove.
 */
public final class Modes implements Iterable<Modes.Entry> {
    private final List<Entry> entries;

    public Modes(Modes other) {
        entries = new ArrayList<>(other.entries);
    }

    public Modes(int size) {
        entries = new ArrayList<>(size);
    }

    public Modes() {
        entries = new ArrayList<>();
    }

    public Entry getMode(char modeChar) {
        for (Entry entry : entries) {
            if (entry.getModeChar() == modeChar) {
                return entry;
            }
        }
        return null;
    }

    public int size() {
        return entries.size();
    }

    public Entry get(final int index) {
        return entries.get(index);
    }

    public boolean hasMode(char modeChar) {
        return getMode(modeChar) != null;
    }

    public Iterator<Entry> iterator() {
        return entries.iterator();
    }

    public void addMode(char modeChar, String argument, boolean add) {
        entries.add(new Entry(modeChar, argument, add));
    }

    public void addMode(char modeChar, String argument) {
        entries.add(new Entry(modeChar, argument, true));
    }

    public void addMode(char modeChar, boolean add) {
        entries.add(new Entry(modeChar, null, add));
    }

    public void addMode(char modeChar) {
        entries.add(new Entry(modeChar, null, true));
    }

    public Entry removeMode(char modeChar) {
        final Iterator<Entry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            final Entry entry = iterator.next();
            if (entry.getModeChar() == modeChar) {
                iterator.remove();
                return entry;
            }
        }
        return null;
    }

    public String toString() {
        final StringBuilder b = new StringBuilder();
        toString(b);
        return b.toString();
    }

    public void toString(StringBuilder builder) {
        Iterator<Entry> iterator = entries.iterator();
        if (iterator.hasNext()) {
            Entry entry = iterator.next();
            boolean add = entry.isAdd();
            builder.append(add ? '+' : '-');
            builder.append(entry.getModeChar());
            while (iterator.hasNext()) {
                entry = iterator.next();
                if (entry.isAdd() != add) {
                    builder.append((add = entry.isAdd()) ? '+' : '-');
                }
                builder.append(entry.getModeChar());
            }
            iterator = entries.iterator();
            while (iterator.hasNext()) {
                final String argument = iterator.next().getArgument();
                if (argument != null) {
                    builder.append(' ').append(argument);
                }
            }
        }
    }

    public static Modes fromString(String string) {
        boolean add = true;
        Modes modeMap = new Modes(8);
        int state = 0;
        // state 0 == mode chars
        // state 1 == argument spaces
        // state 2 == argument chars
        int start = 0;
        List<String> arguments = null;
        for (int idx = 0; idx < string.length(); idx = string.offsetByCodePoints(idx, 1)) {
            char cp = string.charAt(idx);
            if (Character.isSpaceChar(cp)) {
                if (state == 2) {
                    if (arguments == null) arguments = new ArrayList<>();
                    arguments.add(string.substring(start, idx));
                }
                state = 1;
            } else if (state == 0 && cp == '+') {
                add = true;
            } else if (state == 0 && cp == '-') {
                add = false;
            } else if (state == 0 && (cp >= '0' && cp <= '9' || cp >= 'A' && cp <= 'Z' || cp >= 'a' && cp <= 'z')) {
                modeMap.addMode(cp, add);
            } else if (state == 1) {
                start = idx;
                state = 2;
            } else if (state == 0) {
                throw new IllegalArgumentException("Invalid mode character '" + cp + "'");
            }
            // else skip
        }
        if (arguments != null) {
            final int argSize = arguments.size();
            final int modeSize = modeMap.size();
            for (int idx = 0; idx < argSize; idx ++) {
                modeMap.get(modeSize - argSize + idx).setArgument(arguments.get(idx));
            }
        }
        return modeMap;
    }

    public static final class Entry {
        private final char modeChar;
        private final boolean add;
        private String argument;

        Entry(final char modeChar, final String argument, final boolean add) {
            this.modeChar = modeChar;
            this.argument = argument;
            this.add = add;
        }

        public char getModeChar() {
            return modeChar;
        }

        public String getArgument() {
            return argument;
        }

        public Entry setArgument(final String argument) {
            this.argument = argument;
            return this;
        }

        public boolean isAdd() {
            return add;
        }
    }
}
