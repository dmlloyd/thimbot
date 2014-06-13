/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class ThreeSet<E> extends AbstractSet<E> {
    private final E one;
    private final E two;
    private final E three;

    public ThreeSet(final E one, final E two, final E three) {
        this.one = one;
        this.two = two;
        this.three = three;
    }

    public boolean contains(final Object o) {
        return one.equals(o) || two.equals(o) || three.equals(o);
    }

    public Object[] toArray() {
        return new Object[] { one, two, three };
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < 3) {
            a = Arrays.copyOf(a, 3);
        }
        a[0] = (T) one;
        a[1] = (T) two;
        a[2] = (T) three;
        return a;
    }

    public Iterator<E> iterator() {
        return new Iterator<E>() {
            int s = 0;

            public boolean hasNext() {
                return s < 3;
            }

            public E next() {
                switch (s) {
                    case 0: s = 1; return one;
                    case 1: s = 2; return two;
                    case 2: s = 3; return three;
                    default: throw new NoSuchElementException();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public int size() {
        return 3;
    }
}
