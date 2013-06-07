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

package com.flurg.thimbot.source;

import com.flurg.thimbot.raw.ByteOutput;
import com.flurg.thimbot.raw.StringEmitter;

import java.io.IOException;
import java.util.prefs.Preferences;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public interface Target {
    String getName();

    void performSendMessage(ByteOutput target, StringEmitter contents) throws IOException;

    void performSendAction(ByteOutput target, StringEmitter contents) throws IOException;

    Preferences getPreferences(Preferences ourRoot);

    void writeName(ByteOutput target) throws IOException;
}
