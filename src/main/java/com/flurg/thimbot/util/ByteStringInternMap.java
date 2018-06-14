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

/**
 * A map which is useful for caching {@link ByteString} instances.
 */
public final class ByteStringInternMap extends InternMap<ByteString> {

    public ByteStringInternMap(final int sizeLimit) {
        super(sizeLimit);
    }

    public ByteString get(byte[] b, int off, int len, boolean addIfNotFound) {
        if (off < 0 || off > b.length || len < 0 || len + off > b.length) throw new IllegalArgumentException();
        final int hc = ByteString.arrayHashCode(b, off, len);
        synchronized (lock) {
            final Entry[] array = this.array;
            final int length = array.length;
            final int idx = hc & (length - 1);
            Entry prev = null;
            Entry entry = array[idx];
            while (entry != null) {
                if (entry.getHashCode() == hc) {
                    final ByteString entryItem = (ByteString) entry.getItem();
                    if (entryItem.contentEquals(b, off, len)) {
                        refresh(entry);
                        return entryItem;
                    }
                }
                prev = entry;
                entry = entry.getStoreNext();
            }
            ByteString newString = ByteString.fromBytes(b, off, len);
            if (addIfNotFound) {
                // add it
                final Entry newEntry = new Entry(newString, hc);
                if (prev == null) {
                    array[idx] = newEntry;
                } else {
                    prev.setStoreNext(newEntry);
                }
                registerNewEntry(newEntry);
            }
            return newString;
        }
    }
}
