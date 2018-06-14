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

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 */
public final class ConnectionLocal<T> {

    private final Supplier<T> initialValue;
    private final Consumer<T> terminateAction;

    public ConnectionLocal(final Supplier<T> initialValue, final Consumer<T> terminateAction) {
        this.initialValue = initialValue;
        this.terminateAction = terminateAction;
    }

    public ConnectionLocal(final Consumer<T> terminateAction) {
        this(() -> null, terminateAction);
    }

    public ConnectionLocal(final Supplier<T> initialValue) {
        this(initialValue, ignored -> {});
    }

    public ConnectionLocal() {
        this(() -> null, ignored -> {});
    }

    Supplier<T> getInitialValue() {
        return initialValue;
    }

    @SuppressWarnings("unchecked")
    public T get(Connection connection) {
        return (T) connection.localMap.computeIfAbsent(this, ConnectionLocal::getInitialValue);
    }

    @SuppressWarnings("unchecked")
    public T remove(Connection connection) {
        return (T) connection.localMap.remove(this);
    }

    @SuppressWarnings("unchecked")
    public T set(Connection connection, T newVal) {
        if (newVal == null) return remove(connection);
        return (T) connection.localMap.put(this, newVal);
    }

    @SuppressWarnings("unchecked")
    public T replace(Connection connection, T newVal) {
        if (newVal == null) return remove(connection);
        return (T) connection.localMap.replace(this, newVal);
    }

    @SuppressWarnings("unchecked")
    public boolean replace(Connection connection, T expect, T update) {
        if (update == null) return remove(connection, expect);
        return connection.localMap.replace(this, expect, update);
    }

    public boolean remove(final Connection connection, final T expect) {
        return connection.localMap.remove(this, expect);
    }

    @SuppressWarnings("unchecked")
    void terminate(Object item) {
        if (item != null) terminateAction.accept((T) item);
    }
}
