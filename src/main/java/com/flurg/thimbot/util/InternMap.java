/*
 * Copyright 2018 by David M. Lloyd and contributors
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

import static java.lang.Math.max;

import java.util.Arrays;

/**
 */
public class InternMap<T> {
    final Object lock = new Object();
    final int sizeLimit;
    Entry newest, oldest;
    Entry[] array;
    int size;

    public InternMap(int sizeLimit) {
        this.sizeLimit = max(8, sizeLimit);
        array = new Entry[64];
    }

    public int size() {
        synchronized (lock) {
            return size;
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public void clear() {
        synchronized (lock) {
            newest = oldest = null;
            Arrays.fill(array, null);
            size = 0;
        }
    }

    @SuppressWarnings("unchecked")
    public T get(T original, boolean addIfNotFound) {
        if (original == null) return null;
        final int hc = original.hashCode();
        synchronized (lock) {
            final Entry[] array = this.array;
            final int length = array.length;
            final int idx = hc & (length - 1);
            Entry prev = null;
            Entry entry = array[idx];
            while (entry != null) {
                if (entry.getHashCode() == hc) {
                    final Object entryItem = entry.getItem();
                    if (entryItem.equals(original)) {
                        refresh(entry);
                        return (T) entryItem;
                    }
                }
                prev = entry;
                entry = entry.getStoreNext();
            }
            if (addIfNotFound) {
                // add it
                final Entry newEntry = new Entry(original, hc);
                if (prev == null) {
                    array[idx] = newEntry;
                } else {
                    prev.setStoreNext(newEntry);
                }
                registerNewEntry(newEntry);
            }
        }
        return original;
    }

    void registerNewEntry(final Entry newEntry) {
        assert Thread.holdsLock(lock);
        final Entry[] array = this.array;
        final int length = array.length;
        final int oldSize = size;
        final Entry oldNewest = newest;
        newEntry.setAgeNext(oldNewest);
        oldNewest.setAgePrev(newEntry);
        newest = newEntry;
        if (oldSize == sizeLimit) {
            // remove eldest
            final Entry oldOldest = oldest;
            oldest = oldOldest.getAgePrev();
            oldOldest.getAgePrev().setAgeNext(null);
            final int hc = oldOldest.getHashCode();
            final int idx = hc & (length - 1);
            Entry prev = null;
            Entry entry = array[idx];
            while (entry != null) {
                if (entry == oldOldest) {
                    if (prev != null) {
                        prev.setStoreNext(entry.getStoreNext());
                    } else {
                        array[idx] = entry.getStoreNext();
                    }
                    break;
                }
                prev = entry;
                entry = entry.getStoreNext();
            }
        } else {
            if (oldSize >= (length >> 1) + (length >> 2)) {
                // grow array
                final int newLength = oldSize << 1;
                final int newMask = newLength - 1;
                final Entry[] newArray = Arrays.copyOf(array, newLength);
                for (int idx = 0; idx < array.length; idx++) {
                    Entry entry = array[idx];
                    Entry prevEven = null, prevOdd = null;
                    while (entry != null) {
                        if ((entry.getHashCode() & length) != 0) {
                            newArray[idx + length] = entry;
                            Entry tmp = entry;
                            entry = entry.getStoreNext();
                            tmp.setStoreNext(null);
                            prevOdd = entry;
                        } else {

                        }
                    }
                }
            }
            size = oldSize + 1;
        }
    }

    void refresh(Entry entry) {
        assert Thread.holdsLock(lock);
        final Entry oldNewest = newest;
        if (oldNewest == entry) {
            return;
        }
        // not newest
        final Entry oldOldest = oldest;
        if (oldOldest == entry) {
            oldest = entry.getAgePrev();
            assert entry.getAgeNext() == null; // because it's the oldest
        }
        final Entry oldEntryPrev = entry.agePrev;
        entry.agePrev = null;
        final Entry oldEntryNext = entry.ageNext;
        if (oldEntryPrev != null) {
            oldEntryPrev.setAgeNext(oldEntryNext);
        }
        if (oldEntryNext != null) {
            oldEntryNext.setAgePrev(oldEntryPrev);
        }
        entry.setAgeNext(oldNewest);
        newest = entry;
    }

    static class Entry {
        private Entry storeNext;
        private Entry ageNext, agePrev;
        private final Object item;
        private final int hashCode;

        Entry(final Object item, final int hashCode) {
            this.item = item;
            this.hashCode = hashCode;
        }

        public Entry getAgeNext() {
            return ageNext;
        }

        public void setAgeNext(final Entry ageNext) {
            this.ageNext = ageNext;
        }

        public Entry getAgePrev() {
            return agePrev;
        }

        public void setAgePrev(final Entry agePrev) {
            this.agePrev = agePrev;
        }

        public Entry getStoreNext() {
            return storeNext;
        }

        public void setStoreNext(final Entry storeNext) {
            this.storeNext = storeNext;
        }

        public Object getItem() {
            return item;
        }

        public int getHashCode() {
            return hashCode;
        }
    }
}
