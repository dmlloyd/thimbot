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

package com.flurg.thimbot.raw;

import static java.lang.Math.min;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.flurg.thimbot.ThimBot;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public class EmittableByteArrayOutputStream extends ByteArrayOutputStream implements Emittable, LineOutputCallback, ByteOutput {

    public EmittableByteArrayOutputStream() {
    }

    public EmittableByteArrayOutputStream(final int size) {
        super(size);
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity - buf.length > 0) {
            int oldCapacity = buf.length;
            int newCapacity = oldCapacity << 1;
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity < 0) {
                if (minCapacity < 0) {
                    throw new OutOfMemoryError();
                }
                newCapacity = Integer.MAX_VALUE;
            }
            buf = Arrays.copyOf(buf, newCapacity);
        }
    }

    public void write(final int b) {
        int count = this.count;
        ensureCapacity(this.count = count + 1);
        buf[count] = (byte) b;
    }

    public void write(final byte[] b) {
        write(b, 0, b.length);
    }

    public void write(final byte[] b, final int off, final int len) {
        final int bLen = b.length;
        if ((off < 0) || (off > bLen) || (len < 0) ||
            ((off + len) - bLen > 0)) {
            throw new IndexOutOfBoundsException();
        }
        int count = this.count;
        ensureCapacity(count + len);
        System.arraycopy(b, off, buf, count, len);
        this.count = count + len;
    }

    public void writeTo(final OutputStream out) throws IOException {
        out.write(buf, 0, count);
    }

    public void reset() {
        count = 0;
    }

    public void reset(int cnt) {
        count = min(count, cnt);
    }

    public int size() {
        return count;
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(buf, count);
    }

    public String toString() {
        return new String(buf, 0, count);
    }

    public String toString(final String charsetName) throws UnsupportedEncodingException {
        return new String(buf, 0, count, charsetName);
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public String toString(final int hibyte) {
        return new String(buf, hibyte, 0, count);
    }

    public void emit(final ByteOutput output) throws IOException {
        output.write(buf, 0, count);
    }

    public void emit(final ByteArrayOutputStream output) {
        output.write(buf, 0, count);
    }

    public void writeLine(final ThimBot context, final ByteOutput target, final long seq) throws IOException {
        target.write(this);
    }

    public void write(final Emittable emitter) throws IOException {
        emitter.emit((ByteArrayOutputStream) this);
    }

    public void write(final StringEmitter emitter) throws IOException {
        emitter.emit((ByteArrayOutputStream) this);
    }

    public void write(final String string, final Charset charset) throws IOException {
        write(string.getBytes(charset));
    }

    public void write(final String string) throws IOException {
        write(string, StandardCharsets.UTF_8);
    }
}
