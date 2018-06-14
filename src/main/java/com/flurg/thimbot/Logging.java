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

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class Logging {

    private Logging() {
    }

    static class Count {
        int count;

        Count() {
        }

        Count(final Count parentValue) {
            count = parentValue.count;
        }
    }

    private static final ThreadLocal<Count> SUPPRESS = new InheritableThreadLocal<Count>() {
        protected Count childValue(final Count parentValue) {
            return new Count(parentValue);
        }

        protected Count initialValue() {
            return new Count();
        }
    };

    public static void off() {
        SUPPRESS.get().count++;
    }

    public static void on() {
        SUPPRESS.get().count--;
    }

    public static boolean suppressed() {
        return SUPPRESS.get().count > 0;
    }
}
