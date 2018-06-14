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

import static java.lang.Math.abs;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * A text buffer with separate read and write points.
 */
public final class TextBuffer {
    private final char[] buf;
    private int r, w;

    public TextBuffer(int size) {
        buf = new char[size];
    }

    public int size() {
        return w - r;
    }

    public void compact() {
        int r = this.r;
        if (r > 0) {
            final int count = w - r;
            if (count > 0) {
                System.arraycopy(buf, this.r, buf, 0, count);
            }
            this.r = 0;
            w = count;
        }
    }

    public boolean hasNext() {
        return size() > 0;
    }

    public boolean hasRoom() {
        return size() < buf.length;
    }

    public int readCodePoint() {
        final int r = this.r;
        final int w = this.w;
        if (r == w) {
            throw bufferUnderflow();
        }
        final int cp = Character.codePointAt(buf, r);
        final int charCount = Character.charCount(cp);
        final int rn = r + charCount;
        if (rn > w) {
            throw bufferUnderflow();
        } else if (rn == w) {
            clear();
        } else /* rn < w */ {
            this.r = rn;
        }
        return cp;
    }

    public char readChar() {
        final int r = this.r;
        final int w = this.w;
        if (r == w) {
            throw bufferUnderflow();
        }
        final char cp = buf[r];
        if (r + 1 == w) {
            clear();
        } else {
            this.r = r + 1;
        }
        return cp;
    }

    public int peekCodePoint() {
        final int r = this.r;
        final int cp = Character.codePointAt(buf, r);
        final int w = this.w;
        if (Character.charCount(cp) > w - r) {
            throw bufferUnderflow();
        }
        return cp;
    }

    public char peekChar() {
        final int r = this.r;
        final int w = this.w;
        if (r == w) {
            throw bufferUnderflow();
        }
        return buf[r];
    }

    public int peekLastCodePoint() {
        final int r = this.r;
        final int w = this.w;
        if (w == 0) {
            throw bufferUnderflow();
        }
        final int cp = Character.codePointBefore(buf, w);
        if (Character.charCount(cp) > w - r) {
            throw bufferUnderflow();
        }
        return cp;
    }

    public char peekLastChar() {
        final int r = this.r;
        final int w = this.w;
        if (r == w) {
            throw bufferUnderflow();
        }
        return buf[w - 1];
    }

    public boolean contentStartsWith(String arg) {
        final int r = this.r;
        final int w = this.w;
        final int length = arg.length();
        if (w - r < length) {
            return false;
        }
        final char[] buf = this.buf;
        for (int i = 0; i < length; i ++) {
            if (arg.charAt(i) != buf[r + i]) {
                return false;
            }
        }
        return true;
    }

    public boolean contentEquals(String arg) {
        final int r = this.r;
        final int w = this.w;
        final int length = arg.length();
        if (w - r != length) {
            return false;
        }
        final char[] buf = this.buf;
        for (int i = 0; i < length; i ++) {
            if (arg.charAt(i) != buf[r + i]) {
                return false;
            }
        }
        return true;
    }

    public String getRemainder() {
        final int r = this.r;
        final int w = this.w;
        clear();
        return new String(buf, r, w - r);
    }

    public String getToken(char delim) {
        final int r = this.r;
        final int w = this.w;
        if (r == w) return "";
        for (int i = 0; i < w - r; i ++) {
            if (buf[r + i] == delim) {
                this.r += i - 1;
                return i == 0 ? "" : new String(buf, r, i - 1);
            }
        }
        clear();
        return new String(buf, r, w - r);
    }

    public String getToken(char delim1, char delim2) {
        final int r = this.r;
        final int w = this.w;
        if (r == w) return "";
        for (int i = 0; i < w - r; i ++) {
            if (buf[r + i] == delim1 || buf[r + i] == delim2) {
                this.r += i - 1;
                return i == 0 ? "" : new String(buf, r, i - 1);
            }
        }
        clear();
        return new String(buf, r, w - r);
    }

    public void appendCodePoint(int cp) {
        final int charCount = Character.charCount(cp);
        int w = this.w;
        if (w > buf.length - charCount) {
            if (r <= charCount - 1) {
                throw bufferFull();
            }
            compact();
            w = this.w;
        }
        Character.toChars(cp, buf, w);
        this.w = w + charCount;
    }

    public void appendChar(char cp) {
        int w = this.w;
        if (w == buf.length) {
            if (r == 0) {
                throw bufferFull();
            }
            compact();
            w = this.w;
        }
        buf[w] = cp;
        this.w = w + 1;
    }

    public void appendNumber(int number) {
        if (number == Integer.MIN_VALUE) {
            // special case
            append("-2147483648");
            return;
        }
        if (number < 0) {
            appendChar('-');
        }
        number = abs(number);
        if (number >= 1_000_000_000) {
            appendChar((char) ('0' + number / 1_000_000_000));
        }
        if (number >= 100_000_000) {
            appendChar((char) ('0' + number / 100_000_000 % 10));
        }
        if (number >= 10_000_000) {
            appendChar((char) ('0' + number / 10_000_000 % 10));
        }
        if (number >= 1_000_000) {
            appendChar((char) ('0' + number / 1_000_000 % 10));
        }
        if (number >= 100_000) {
            appendChar((char) ('0' + number / 100_000 % 10));
        }
        if (number >= 10_000) {
            appendChar((char) ('0' + number / 10_000 % 10));
        }
        if (number >= 1_000) {
            appendChar((char) ('0' + number / 1_000 % 10));
        }
        if (number >= 100) {
            appendChar((char) ('0' + number / 100 % 10));
        }
        if (number >= 10) {
            appendChar((char) ('0' + number / 10 % 10));
        }
        appendChar((char) ('0' + number % 10));
    }

    public void append(String string) {
        final int length = string.length();
        if (size() + length >= buf.length) {
            throw bufferFull();
        }
        int w = this.w;
        string.getChars(0, length, buf, w);
        this.w = w + length;
    }

    /**
     * Remove characters from the end of the string.
     *
     * @param count the number of characters to remove
     */
    public void trimFromEnd(int count) {
        int w = this.w;
        if (count > w - r) {
            throw bufferUnderflow();
        }
        this.w = w - count;
    }

    /**
     * Trim all whitespace from the end of the string.
     */
    public void trimWhitespaceFromEnd() {
        final int r = this.r;
        int w = this.w;
        int cp, cc;
        while (w > r) {
            cp = Character.codePointBefore(buf, w);
            cc = Character.charCount(cp);
            if (Character.isSpaceChar(cp) && w - r >= cc) {
                w -= cc;
            } else {
                break;
            }
        }
        this.w = w;
    }

    /**
     * Trim the line terminator from the end of the string.
     */
    public void chomp() {
        int w = this.w;
        final char[] buf = this.buf;
        if (w > r && (buf[w - 1] == 10 || buf[w - 1] == 13)) {
            this.w = w - 1;
        }
    }

    public void writeTo(Writer writer) throws IOException {
        final int r = this.r;
        final int w = this.w;
        if (w - r == 0) {
            return;
        }
        writer.write(buf, r, w - r);
        clear();
    }

    public void clear() {
        r = w = 0;
    }

    /**
     * Read a line from the given reader.  If the line is larger than the remaining buffer space, it is truncated,
     * and the actual size of the line is returned.  If the line length is greater than {@code Integer.MAX_VALUE}, then
     * {@code Integer.MAX_VALUE} is returned.
     * <p>
     * The line separator (which may be CR or LF) is included in the message and message count.
     *
     * @param reader the reader to read from (must not be {@code null})
     * @return -1 if the end of file is reached, or the number of characters that are actually in the line
     * @throws IOException
     */
    public int readLineFrom(Reader reader) throws IOException {
        int ch;
        int count = 0;
        int r = this.r, w = this.w;
        for (;;) {
            if (w == buf.length) {
                compact();
                r = this.r;
                w = this.w;
            }
            if (w == buf.length) {
                // full, now we're just discarding and counting
                for (;;) {
                    if (count == Integer.MAX_VALUE) {
                        // hit count limit, now we're just discarding
                        for (;;) {
                            ch = reader.read();
                            if (ch == -1 || ch == 10 || ch == 13) {
                                this.w = w;
                                return count;
                            }
                        }
                    }
                    ch = reader.read();
                    if (ch == -1 || ch == 10 || ch == 13) {
                        this.w = w;
                        return count;
                    }
                    count++;
                }
            }
            ch = reader.read();
            if (ch == -1) {
                this.w = w;
                return count;
            }
            buf[w++] = (char) ch;
        }

    }

    private static IllegalStateException bufferUnderflow() {
        return new IllegalStateException("Buffer underflow");
    }

    private static IllegalArgumentException bufferFull() {
        return new IllegalArgumentException("Buffer is full");
    }
}
