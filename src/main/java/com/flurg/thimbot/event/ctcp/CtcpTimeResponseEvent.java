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

package com.flurg.thimbot.event.ctcp;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.List;

import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;

/**
 */
public final class CtcpTimeResponseEvent extends CtcpResponseEvent {

    private static final DateTimeFormatter CTIME = DateTimeFormatter.ofPattern("EEE LLL d HH:mm:ss yyyy");

    public CtcpTimeResponseEvent(final boolean outbound, final String source, final List<String> targets) {
        super(outbound, source, targets);
        setTimeUtc();
    }

    public String getCommand() {
        return "TIME";
    }

    public void accept(final EventHandlerContext context, final EventHandler eventHandler) {
        eventHandler.handleEvent(context, this);
    }

    public CtcpTimeResponseEvent clone() {
        return (CtcpTimeResponseEvent) super.clone();
    }

    public void setTimeUtc() {
        setTimeUtc(getTimestamp());
    }

    public void setTimeUtc(long timestampMillis) {
        setTimeUtc(Instant.ofEpochMilli(timestampMillis));
    }

    public void setTimeUtc(Instant instant) {
        setTime(ZonedDateTime.ofInstant(instant, ZoneOffset.UTC));
    }

    public void setTime(ZonedDateTime zonedDateTime) {
        setDecodedText(zonedDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }

    public ZonedDateTime getTime() throws DateTimeException {
        TemporalAccessor result;
        try {
            result = DateTimeFormatter.RFC_1123_DATE_TIME.parse(getDecodedText());
        } catch (DateTimeParseException e) {
            try {
                result = CTIME.parse(getDecodedText());
            } catch (DateTimeParseException e2) {
                e2.addSuppressed(e);
                throw e2;
            }
            try {
                return ZonedDateTime.from(result);
            } catch (DateTimeException e3) {
                e3.addSuppressed(e);
                throw e3;
            }
        }
        return ZonedDateTime.from(result);
    }
}
