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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public class Attachable {

    static final Object[] NONE = new Object[0];
    static final AtomicInteger keySequence = new AtomicInteger();

    private Map<Key<?>, Object> attachments;

    public <T> T getAttachment(Key<T> key) {
        if (key == null) {
            return null;
        }
        synchronized (this) {
            Map<Key<?>, Object> attachments = this.attachments;
            if (attachments == null) {
                return null;
            }
            return key.cast(attachments.get(key));
        }
    }

    public <T> T removeAttachment(Key<T> key) {
        if (key == null) {
            return null;
        }
        synchronized (this) {
            Map<Key<?>, Object> attachments = this.attachments;
            if (attachments == null) {
                return null;
            }
            return key.cast(attachments.remove(key));
        }
    }

    public <T> boolean replaceAttachment(Key<T> key, T oldValue, T newValue) {
        if (key == null || oldValue == null) {
            return false;
        }
        if (newValue == null) {
            throw new IllegalArgumentException("newValue is null");
        }
        synchronized (this) {
            final Map<Key<?>, Object> attachments = this.attachments;
            if (attachments == null) return false;
            final Object obj = attachments.get(key);
            if (obj != null && oldValue.equals(obj)) {
                attachments.put(key, newValue);
                return true;
            } else {
                return false;
            }
        }
    }

    public <T> T replaceAttachment(Key<T> key, T newValue) {
        return putAttachment(key, newValue, When.PRESENT);
    }

    public <T> T putAttachment(Key<T> key, T newValue) {
        return putAttachment(key, newValue, When.ALWAYS);
    }

    public <T> T putAttachment(Key<T> key, T newValue, When when) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        if (newValue == null) {
            throw new IllegalArgumentException("newValue is null");
        }
        synchronized (this) {
            Map<Key<?>, Object> attachments = this.attachments;
            if (attachments == null) {
                if (when != When.PRESENT) {
                    attachments = this.attachments = new HashMap<>();
                    attachments.put(key, newValue);
                    return null;
                }
                return null;
            }
            if (when == When.ABSENT && attachments.containsKey(key)) return key.cast(attachments.get(key));
            if (when == When.PRESENT && ! attachments.containsKey(key)) return null;
            return key.cast(attachments.put(key, newValue));
        }
    }

    public enum When {
        ALWAYS,
        PRESENT,
        ABSENT,
    }

    public static final class Key<T> {

        private final TypeChecker<T> checker;
        private final int id = keySequence.getAndIncrement();

        Key(final TypeChecker<T> checker) {
            this.checker = checker;
        }

        public int getId() {
            return id;
        }

        public T cast(final Object orig) throws ClassCastException {
            return checker.cast(orig);
        }

        public T tryCast(final Object orig) {
            return checker.tryCast(orig);
        }
    }
}
