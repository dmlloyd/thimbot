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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 */
public final class ByteString {
    private static final byte[] NO_BYTES = new byte[0];
    public static final ByteString EMPTY = new ByteString(NO_BYTES, 0, 0, false);
    final byte[] bytes;
    final int off, len;
    private int hashCode;
    private String toStringUtf8;
    private String toStringAscii;

    ByteString(final byte[] bytes, final int off, final int len, final boolean clone) {
        if (clone) {
            this.bytes = Arrays.copyOfRange(bytes, off, off + len);
            this.off = 0;
            this.len = len;
        } else {
            this.bytes = bytes;
            this.off = off;
            this.len = len;
        }
    }

    ByteString(final byte[] bytes, final boolean clone) {
        this(bytes, 0, bytes.length, clone);
    }

    ByteString(final String s, final Charset charset) {
        this(s.getBytes(charset), false);
        if (StandardCharsets.UTF_8.equals(charset)) {
            toStringUtf8 = s;
        } else if (StandardCharsets.US_ASCII.equals(charset)) {
            toStringAscii = s;
        }
    }

    public StringBuilder toString(StringBuilder b, Charset charset) {
        return b.append(new String(bytes, off, len, charset));
    }

    public int byteAt(int index) {
        if (index < 0 || index > len) throw new IndexOutOfBoundsException();
        return bytes[off + index] & 0xff;
    }

    public int length() {
        return len;
    }

    public ByteString substring(int start) {
        return substring(start, len);
    }

    public ByteString substring(int start, int end) {
        if (start == 0 && end == len) return this;
        if (start > len || end < 0 || end < start) throw new IndexOutOfBoundsException();
        if (start == end) return EMPTY;
        return new ByteString(bytes, off + start, end - start, false);
    }

    public String toString(final Charset charset) {
        if (len == 0) return "";
        if (StandardCharsets.UTF_8.equals(charset)) {
            final String s = this.toStringUtf8;
            if (s == null) {
                return toStringUtf8 = new String(bytes, off, off + len, charset);
            } else {
                return s;
            }
        }
        if (StandardCharsets.US_ASCII.equals(charset)) {
            final String s = this.toStringAscii;
            if (s == null) {
                return toStringAscii = new String(bytes, off, off + len, charset);
            } else {
                return s;
            }
        }
        return new String(bytes, off, off + len, charset);
    }

    public String toString() {
        return toString(Charset.defaultCharset());
    }

    public static ByteString fromString(final String s, final Charset charset) {
        return new ByteString(s, charset);
    }

    public static ByteString fromBytes(final byte[] b, final int off, final int len) {
        return new ByteString(b, off, len, true);
    }

    public static ByteString fromBytes(final byte[] b) {
        return fromBytes(b, 0, b.length);
    }

    public void writeTo(final ByteBuffer buffer) {
        buffer.put(bytes, off, len);
    }

    public int hashCode() {
        final int hashCode = this.hashCode;
        if (hashCode == 0) {
            return this.hashCode = arrayHashCode(bytes, 0, len);
        }
        return hashCode;
    }

    static int arrayHashCode(final byte[] bytes, final int off, final int len) {
        int hashCode = 0;
        for (int i = 0; i < len; i ++) {
            hashCode = 19 * hashCode + (bytes[i+off] & 0xff);
        }
        if (hashCode == 0) hashCode = 1 << 31;
        return hashCode;
    }

    static boolean arrayEquals(final byte[] b1, final int off1, final byte[] b2, final int off2, final int len) {
        for (int i = 0; i < len; i ++) {
            if (b1[off1+i] != b2[off2+i]) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(final Object obj) {
        return obj instanceof ByteString && equals((ByteString) obj);
    }

    public boolean equals(final ByteString other) {
        return other != null && (this == other || len == other.len && hashCode() == other.hashCode() && arrayEquals(bytes, off, other.bytes, other.off, len));
    }

    public boolean contentEquals(final byte[] b, final int off, final int len) {
        if (off < 0 || len < 0 || len + off > b.length) throw new IllegalArgumentException();
        return this.len == len && arrayEquals(bytes, this.off, b, off, len);
    }

    public byte[] toByteArray() {
        return Arrays.copyOfRange(bytes, off, off + len);
    }
}
