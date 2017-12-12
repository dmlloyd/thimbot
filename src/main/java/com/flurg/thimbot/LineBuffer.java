/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017 Red Hat, Inc., and individual contributors
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

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class LineBuffer {
    private final byte[] bytes;
    private int r, w;

    public LineBuffer() {
        bytes = new byte[512];
    }

    public int readableCount() {
        return w - r;
    }

    public int writableCount() {
        return 512 - w;
    }

    public int getByte() {
        checkRead();
        return bytes[r++] & 0xff;
    }

    public LineBuffer putByte(int b) {
        if (r == w) {
            r = w = 0;
        }
        if (w == 512) {
            if (r == 0) {
                throw new IndexOutOfBoundsException();
            }
            System.arraycopy(bytes, r, bytes, 0, w - r);
            w = w - r;
            r = 0;
        }
        bytes[w++] = (byte) b;
        return this;
    }

    private void checkRead() {
        if (readableCount() == 0) throw new IndexOutOfBoundsException();
    }
}
