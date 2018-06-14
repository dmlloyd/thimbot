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

package com.flurg.thimbot;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 */
public class Connection {
    private final Socket socket;
    private String nickName;
    private Modes userModes;

    final ConcurrentMap<ConnectionLocal<?>, Object> localMap = new ConcurrentHashMap<>();

    public Connection(final Socket socket) {

        this.socket = socket;
    }

    public String getServerName() {
        return ((InetSocketAddress) socket.getRemoteSocketAddress()).getHostString();
    }
}
