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

package com.flurg.thimbot.event.inbound;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.flurg.thimbot.ThimBot;
import com.flurg.thimbot.event.AbstractEvent;
import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class SaslMechanismListEvent extends AbstractEvent implements InboundEvent {
    private final List<String> mechanisms;

    public SaslMechanismListEvent(final ThimBot bot, final String... mechanisms) {
        super(bot);
        this.mechanisms = Collections.unmodifiableList(Arrays.asList(mechanisms));
    }

    public List<String> getMechanisms() {
        return mechanisms;
    }

    public void dispatch(final EventHandlerContext context, final EventHandler handler) throws Exception {
        handler.handleEvent(context, this);
    }
}
