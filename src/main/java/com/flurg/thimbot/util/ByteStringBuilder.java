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

import static java.lang.Math.max;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * A builder for byte strings.
 */
public final class ByteStringBuilder {
    private byte[] bytes;
    private int start, end;

    public ByteStringBuilder() {
        this(16);
    }

    public ByteStringBuilder(final int length) {
        bytes = new byte[max(16, length)];
        start = 0;
        end = 0;
    }

    public ByteStringBuilder appendByte(int b) {
        byte[] bytes = this.bytes;
        reserve(1);
        bytes[end++] = (byte) b;
        return this;
    }

    public ByteStringBuilder append(ByteString byteString) {
        final int len = byteString.len;
        reserve(len);
        System.arraycopy(byteString.bytes, byteString.off, bytes, end, len);
        end += len;
        return this;
    }

    public ByteStringBuilder append(byte[] b, int off, int len) {
        reserve(len);
        System.arraycopy(b, off, bytes, end, len);
        end += len;
        return this;
    }

    public ByteStringBuilder append(byte[] b) {
        return append(b, 0, b.length);
    }

    private void reserve(final int len) {
        byte[] bytes = this.bytes;
        final int length = bytes.length;
        final int end = this.end;
        final int start = this.start;
        if (length <= end + len) {
            this.bytes = Arrays.copyOfRange(bytes, start, start + length + (length >> 2));
            this.end = end - start;
            this.start = 0;
        }
    }

    public ByteStringBuilder trimFromEnd(int count) {
        final int length = end - start;
        if (length < count) {
            throw new IllegalArgumentException("count is too big");
        }
        if (length == count) {
            end = start = 0;
        } else {
            end -= length;
        }
        return this;
    }

    public ByteStringBuilder trimFromStart(int count) {
        final int length = end - start;
        if (length < count) {
            throw new IllegalArgumentException("count is too big");
        }
        if (length == count) {
            end = start = 0;
        } else {
            start += length;
        }
        return this;
    }

    public ByteStringBuilder setLength(int newLength) {
        if (newLength < 0) throw new IllegalArgumentException("newLength is less than 0");
        final int length = end - start;
        if (newLength > length) {
            throw new IllegalArgumentException("newLength is too big");
        }
        if (length != newLength) {
            if (newLength == 0) {
                end = start = 0;
            }
            end -= length - newLength;
        }
        return this;
    }

    public ByteString toByteString() {
        return toByteString(0, end - start);
    }

    public ByteString toByteString(int off, int len) {
        if (off < 0 || len < 0 || off > len || off > end - start || len > end - start) throw new IllegalArgumentException();
        return len == 0 ? ByteString.EMPTY : new ByteString(bytes, start + off, len, true);
    }

    public ByteStringBuilder append(final String str, final Charset charset) {
        return append(str.getBytes(charset));
    }

    public boolean isEmpty() {
        return length() > 0;
    }

    public int length() {
        return end - start;
    }

    public void clear() {
        start = end = 0;
    }

    public byte[] toByteArray() {
        return Arrays.copyOfRange(bytes, start, end);
    }
}
