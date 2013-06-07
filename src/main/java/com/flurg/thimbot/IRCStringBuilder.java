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

package com.flurg.thimbot;

/**
 * A string builder wrapper that supports colors.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class IRCStringBuilder {
    private final StringBuilder b = new StringBuilder();

    public IRCStringBuilder clear() {
        b.setLength(0);
        return this;
    }

    /**
     * Set foreground color.
     *
     * @param color the color 0-15
     * @return this builder
     */
    public IRCStringBuilder fc(int color) {
        if (color >= 0 && color <= 15) {
            b.append((char)3).append(color);
        }
        return this;
    }

    /**
     * Set background color; might restore foreground color depending on the client.
     *
     * @param color the color 0-15
     * @return this builder
     */
    public IRCStringBuilder bc(int color) {
        if (color >= 0 && color <= 15) {
            b.append((char)3).append(',').append(color);
        }
        return this;
    }

    /**
     * Set foreground and background color.
     *
     * @param color the fg color 0-15
     * @param color2 the bg color 0-15
     * @return this builder
     */
    public IRCStringBuilder c(int color, int color2) {
        if (color >= 0 && color <= 15 && color2 >=0 && color2 <= 15) {
            b.append((char)3).append(color).append(',').append(color2);
        }
        return this;
    }



    /**
     * Set normal (default) colors.  On some clients this also resets boldface and other attributes.
     *
     * @return this builder
     */
    public IRCStringBuilder nc() {
        b.append((char)15);
        return this;
    }

    /**
     * Set fixed-pitch font.  Not supported by all clients.
     *
     * @return this builder
     */
    public IRCStringBuilder f() {
        b.append((char)17);
        return this;
    }

    /**
     * Toggle reverse video.  Not supported by all clients.  On some clients this just swaps FG and BG color.
     *
     * @return this builder
     */
    public IRCStringBuilder iv() {
        b.append((char)18);
        return this;
    }

    /**
     * Toggle boldface.
     *
     * @return this builder
     */
    public IRCStringBuilder b() {
        b.append((char)2);
        return this;
    }

    /**
     * Toggle underline.
     *
     * @return this builder
     */
    public IRCStringBuilder u() {
        b.append((char)31);
        return this;
    }

    /**
     * Toggle inverse or italics, depending on the client.
     *
     * @return this builder
     */
    public IRCStringBuilder i() {
        b.append((char)22);
        return this;
    }

    /**
     * Toggle italics.  Not supported by all clients.
     *
     * @return this builder
     */
    public IRCStringBuilder it() {
        b.append((char)29);
        return this;
    }

    public IRCStringBuilder append(final Object obj) {
        b.append(obj);
        return this;
    }

    public IRCStringBuilder append(final String str) {
        b.append(str);
        return this;
    }

    public IRCStringBuilder append(final StringBuffer sb) {
        b.append(sb);
        return this;
    }

    public IRCStringBuilder append(final CharSequence s) {
        b.append(s);
        return this;
    }

    public IRCStringBuilder append(final CharSequence s, final int start, final int end) {
        b.append(s, start, end);
        return this;
    }

    public IRCStringBuilder append(final char[] str) {
        b.append(str);
        return this;
    }

    public IRCStringBuilder append(final char[] str, final int offset, final int len) {
        b.append(str, offset, len);
        return this;
    }

    public IRCStringBuilder append(final boolean b) {
        this.b.append(b);
        return this;
    }

    public IRCStringBuilder append(final char c) {
        b.append(c);
        return this;
    }

    public IRCStringBuilder append(final int i) {
        b.append(i);
        return this;
    }

    public IRCStringBuilder append(final long lng) {
        b.append(lng);
        return this;
    }

    public IRCStringBuilder append(final float f) {
        b.append(f);
        return this;
    }

    public IRCStringBuilder append(final double d) {
        b.append(d);
        return this;
    }

    public IRCStringBuilder appendCodePoint(final int codePoint) {
        b.appendCodePoint(codePoint);
        return this;
    }

    public int length() {
        return b.length();
    }

    public int capacity() {
        return b.capacity();
    }

    public void trimToSize() {
        b.trimToSize();
    }

    public void ensureCapacity(final int minimumCapacity) {
        b.ensureCapacity(minimumCapacity);
    }

    public void setLength(final int newLength) {
        b.setLength(newLength);
    }

    public String toString() {
        return b.toString();
    }
}
