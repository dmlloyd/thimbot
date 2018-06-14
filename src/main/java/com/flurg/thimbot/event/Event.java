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

package com.flurg.thimbot.event;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

import com.flurg.thimbot.util.ByteString;

/**
 */
public abstract class Event implements Cloneable {
    private final boolean outbound;
    private final String source;
    private final List<String> targets;
    private long timestamp = System.currentTimeMillis();
    private ByteString encodedText;
    private Charset textCharset = StandardCharsets.UTF_8;
    private String decodedText;

    protected Event(final boolean outbound, final String source, final List<String> targets) {
        if (targets == null) throw new IllegalArgumentException("targets is null");
        this.source = source;
        this.outbound = outbound;
        this.targets = targets;
    }

    public boolean isOutbound() {
        return outbound;
    }

    public boolean isInbound() {
        return ! outbound;
    }

    public boolean hasSource() {
        return source != null;
    }

    public String getSource() {
        return source;
    }

    public List<String> getTargets() {
        return targets;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    public void copyTextFrom(final Event event) {
        encodedText = event.encodedText;
        decodedText = event.decodedText;
        textCharset = event.textCharset;
    }

    public String getDecodedText() {
        String decodedText = this.decodedText;
        if (decodedText == null) {
            ByteString encodedText = this.encodedText;
            if (encodedText == null) {
                return null;
            }
            decodedText = this.decodedText = encodedText.toString(textCharset);
        }
        return decodedText;
    }

    public void setDecodedText(String text) {
        encodedText = null;
        decodedText = text;
    }

    public ByteString getEncodedText() {
        ByteString encodedText = this.encodedText;
        if (encodedText == null) {
            String decodedText = this.decodedText;
            if (decodedText == null) {
                return null;
            }
            encodedText = this.encodedText = ByteString.fromString(decodedText, textCharset);
        }
        return encodedText;
    }

    public void setEncodedText(ByteString text) {
        decodedText = null;
        encodedText = text;
    }

    public Charset getTextCharset() {
        return textCharset;
    }

    public void setTextCharset(final Charset textCharset) {
        this.textCharset = textCharset;
    }

    protected void addToString(StringBuilder b) {}

    public StringBuilder toString(StringBuilder b) {
        if (isOutbound()) b.append("<<< "); else b.append(">>> ");
        b.append(getClass().getSimpleName());
        final List<String> targets = getTargets();
        final Iterator<String> iterator = targets.iterator();
        if (iterator.hasNext()) {
            b.append(' ');
            b.append(iterator.next());
            while (iterator.hasNext()) {
                b.append(',').append(iterator.next());
            }
        }
        addToString(b);
        final String decodedText = getDecodedText();
        if (! decodedText.isEmpty()) {
            b.append(' ').append(':').append(decodedText);
        }
        return b;
    }

    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    public abstract void accept(final EventHandlerContext context, final EventHandler eventHandler);

    public Event clone() {
        try {
            return (Event) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}
