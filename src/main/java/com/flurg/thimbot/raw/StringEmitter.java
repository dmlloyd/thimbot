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

package com.flurg.thimbot.raw;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class StringEmitter implements Emittable {
    private final byte[] bytes;
    private final int hashCode;

    public StringEmitter(String original, Charset charset) {
        bytes = original.getBytes(charset);
        int hc = 0;
        for (byte b : bytes) {
            hc = ((hc << 4) + hc) + (b & 0xff);
        }
        hashCode = hc;
    }

    public StringEmitter(String original, String charset) {
        this(original, Charset.forName(charset));
    }

    public StringEmitter(String original) {
        this(original, StandardCharsets.UTF_8);
    }

    public void emit(IRCOutput output) throws IOException {
        output.write(bytes);
    }

    public void emit(ByteArrayOutputStream output) {
        output.write(bytes, 0, bytes.length);
    }

    public int length() {
        return bytes.length;
    }

    public boolean equals(final Object obj) {
        return obj instanceof StringEmitter && equals((StringEmitter) obj);
    }

    private boolean equals(final StringEmitter obj) {
        return hashCode == obj.hashCode && Arrays.equals(bytes, obj.bytes);
    }

    public int hashCode() {
        return hashCode;
    }

    public String toString() {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
