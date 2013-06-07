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

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class DefaultableReference<T> {
    private final AtomicReference<T> ref = new AtomicReference<>();
    private final DefaultableReference<T> defaultVal;
    private final T fixedRef;

    public DefaultableReference(final DefaultableReference<T> defaultVal) {
        this.defaultVal = defaultVal;
        fixedRef = null;
    }

    public DefaultableReference(final T t) {
        defaultVal = null;
        fixedRef = t;
    }

    private T getDefault() {
        return fixedRef != null ? fixedRef : defaultVal.get();
    }

    public T get() {
        final T t = ref.get();
        return t == null ? getDefault() : t;
    }

    public boolean compareAndSet(final T expect, final T update) {
        if (update == null) {
            throw new IllegalArgumentException("update is null");
        }
        T oldVal;
        oldVal = get();
        if (oldVal == null) {
            final T def = getDefault();
            if (def != expect) {
                return false;
            }
        } else if (oldVal != expect) {
            return false;
        }
        return ref.compareAndSet(oldVal, update);
    }

    public boolean setIfDefault(final T update) {
        return ref.compareAndSet(null, update);
    }

    public boolean setIfNotDefault(final T update) {
        T oldVal;
        do {
            oldVal = ref.get();
            if (oldVal == null) {
                return false;
            }
        } while (! ref.compareAndSet(oldVal, update));
        return true;
    }

    public void lazySet(final T newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException("newValue is null");
        }
        ref.lazySet(newValue);
    }

    public void lazyClear() {
        ref.lazySet(null);
    }

    public void set(final T newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException("newValue is null");
        }
        ref.set(newValue);
    }

    public void clear() {
        ref.set(null);
    }

    public T getAndSet(final T newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException("newValue is null");
        }
        final T t = ref.getAndSet(newValue);
        return t == null ? getDefault() : t;
    }

    public T getAndClear() {
        final T t = ref.getAndSet(null);
        return t == null ? getDefault() : t;
    }
}
