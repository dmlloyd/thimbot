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

package com.flurg.thimbot.raw;

import com.flurg.thimbot.Charsets;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class StringEmitter {
    private final byte[] bytes;

    public StringEmitter(String original, Charset charset) {
        bytes = original.getBytes(charset);
    }

    public StringEmitter(String original, String charset) {
        this(original, Charset.forName(charset));
    }

    public StringEmitter(String original) {
        this(original, Charsets.LATIN_1);
    }

    public void emit(ByteOutput output) throws IOException {
        output.write(bytes);
    }
}
