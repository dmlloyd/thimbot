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
import java.util.Iterator;
import java.util.Set;

/**
 * A view collection, generally useful only for iteration.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class ViewSet<O, E> extends AbstractSet<E> {
    private final Mapper<O, E> mapper;
    private final Set<O> original;

    public ViewSet(final Mapper<O, E> mapper, final Set<O> original) {
        this.mapper = mapper;
        this.original = original;
    }

    public Iterator<E> iterator() {
        final Iterator<O> iterator = original.iterator();
        return new Iterator<E>() {
            public boolean hasNext() {
                return iterator.hasNext();
            }

            public E next() {
                return mapper.map(iterator.next());
            }

            public void remove() {
                iterator.remove();
            }
        };
    }

    public void clear() {
        original.clear();
    }

    public int size() {
        return original.size();
    }
}
