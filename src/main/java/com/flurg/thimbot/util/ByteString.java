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

package com.flurg.thimbot.util;

import java.io.UnsupportedEncodingException;

import com.flurg.thimbot.charset.Charset;

/**
 */
public final class ByteString {
    private static final byte[] NO_BYTES = new byte[0];
    public static final ByteString EMPTY = new ByteString(NO_BYTES, 0, 0);
    private final byte[] bytes;
    private final int off, len;

    ByteString(final byte[] bytes, final int off, final int len) {
        this.bytes = bytes;
        this.off = off;
        this.len = len;
    }

    ByteString(final byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    public StringBuilder toString(StringBuilder b, Charset charset) {
        charset.appendTo(b, bytes, off, off + len);
        return b;
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
        return new ByteString(bytes, off + start, end - start);
    }

    public String toString(final Charset charset) {
        if (len == 0) return "";
        return charset.toString(bytes, off, off + len);
    }

    public String toString() {
        return new String(bytes, off, len);
    }

    public static ByteString fromString(final String s, final Charset charset) {
        try {
            return new ByteString(s.getBytes(charset.getName()));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
