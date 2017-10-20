/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.flurg.thimbot.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.flurg.thimbot.raw.IRCOutput;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class IRCBase64 {

    private static final byte[] alphabet = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
        'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
        'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z', '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9', '+', '/'
    };

    private IRCBase64() {
    }

    public static void encode(byte[] original, int offset, int len, IRCOutput target) throws IOException {
        final byte[] alphabet = IRCBase64.alphabet;
        int count = 0;
        byte s;
        while (count < len) {
            s = original[offset + count++];
            // first the top 6 bits of the first byte
            target.write(alphabet[s >>> 2]);
            if (count == len) {
                // bottom 2 bits + 4 zero bits
                target.write(alphabet[s << 4 & 0x3f]);
                target.write('=');
                target.write('=');
                return;
            }
            // bottom 2 bits + top 4 bits of second byte
            target.write(alphabet[(s << 4 | (s = original[offset + count++]) >>> 4) & 0x3f]);
            if (count == len) {
                // bottom 4 bits + 2 zero bits
                target.write(alphabet[s << 2 & 0x3f]);
                target.write('=');
                return;
            }
            // bottom 4 bits + top 2 bits of third byte
            target.write(alphabet[(s << 2 | (s = original[offset + count++]) >>> 6) & 0x3f]);
            // bottom 6 bits of third byte
            target.write(alphabet[s & 0x3f]);
        }
        // ended right on a boundary, handy
    }

    public static void encode(byte[] original, IRCOutput target) throws IOException {
        encode(original, 0, original.length, target);
    }

    private static int decodeByte(byte b) {
        if (b >= 'A' && b <= 'Z') {
            return b - 'A';
        } else if (b >= 'a' && b <= 'z') {
            return b - 'a' + 26;
        } else if (b >= '0' && b <= '9') {
            return b - '0' + 52;
        } else if (b == '+') {
            return 62;
        } else if (b == '/') {
            return 63;
        } else if (b == '=') {
            return -2;
        } else {
            return -1;
        }
    }

    public static int decode(byte[] encoded, int offset, int len, IRCOutput target) throws IllegalArgumentException, IOException {
        int count = 0;
        int t1, t2;
        while (count < len) {
            // top 6 bits of the first byte
            t1 = decodeByte(encoded[offset + count++]);
            if (t1 == -1) return count - 1;
            if (t1 == -2) throw unexpectedPadding();
            if (count == len) throw truncatedInput();

            // bottom 2 bits + top 4 bits of the second byte
            t2 = decodeByte(encoded[offset + count++]);
            if (t2 == -1) throw truncatedInput();
            if (t2 == -2) throw unexpectedPadding();
            if (count == len) throw truncatedInput();
            target.write((byte) (t1 << 2 | t2 >>> 4));

            // bottom 4 bits + top 2 bits of the third byte - or == if it's the end
            t1 = decodeByte(encoded[offset + count++]);
            if (t1 == -1) throw truncatedInput();
            if (count == len) throw truncatedInput();
            if (t1 == -2) {
                // expect one more byte of padding
                assert count < len;
                if (encoded[offset + count++] != '=') {
                    throw missingRequiredPadding();
                }
                return count;
            }
            target.write((byte) (t2 << 4 | t1 >>> 4));

            // bottom 6 bits of the third byte - or = if it's the end
            t2 = decodeByte(encoded[offset + count++]);
            if (t2 == -1) throw truncatedInput();
            if (t2 == -2) return count;
            target.write((byte) (t1 << 6 | t2));
        }
        return count;
    }

    public static int decode(byte[] encoded, int offset, IRCOutput target) throws IllegalArgumentException, IOException {
        return decode(encoded, offset, encoded.length - offset, target);
    }

    public static int decode(String encoded, int offset, int len, IRCOutput target) throws IllegalArgumentException, IOException {
        byte[] bytes = encoded.substring(offset, len).getBytes(StandardCharsets.UTF_8);
        return decode(bytes, 0, bytes.length, target);
    }

    public static int decode(String encoded, int offset, IRCOutput target) throws IllegalArgumentException, IOException {
        byte[] bytes = encoded.substring(offset).getBytes(StandardCharsets.UTF_8);
        return decode(bytes, 0, bytes.length, target);
    }

    private static IllegalArgumentException missingRequiredPadding() {
        return new IllegalArgumentException("Missing required padding");
    }

    private static IllegalArgumentException unexpectedPadding() {
        return new IllegalArgumentException("Unexpected padding");
    }

    private static IllegalArgumentException truncatedInput() {
        return new IllegalArgumentException("Truncated input");
    }
}
