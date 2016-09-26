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

import java.io.IOException;

import com.flurg.thimbot.ThimBot;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public class AckEmittableByteArrayOutputStream extends EmittableByteArrayOutputStream {
    private final EmissionKey key;

    public AckEmittableByteArrayOutputStream(final EmissionKey key) {
        this.key = key;
    }

    public AckEmittableByteArrayOutputStream(final int size, final EmissionKey key) {
        super(size);
        this.key = key;
    }

    public void writeLine(final ThimBot context, final ByteOutput target, final long seq) throws IOException {
        context.registerOutboundMessage(key, seq);
        super.writeLine(context, target, seq);
    }
}
