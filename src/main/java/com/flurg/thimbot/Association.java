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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;

import javax.net.SocketFactory;

import com.flurg.thimbot.event.Event;
import com.flurg.thimbot.event.irc.cap.CapEvent;

/**
 */
public class Association {
    private final Object lock = new Object();
    private List<InetSocketAddress> serverList = Collections.emptyList();
    private Connection connection;
    private int connectTimeout = 10;
    private ScheduledExecutorService scheduledExecutor;
    private String preferredNickName = "thimbot";
    private String realName = "ThimBot";

    public Association() {
    }

    public List<InetSocketAddress> getServerList() {
        return serverList;
    }

    public void setServerList(final List<InetSocketAddress> serverList) {
        this.serverList = serverList;
    }

    public Connection getConnection() {
        return connection;
    }

    void startConnection() throws IOException {
        final InetSocketAddress address;
        final Socket socket;
        synchronized (lock) {
            address = serverList.get(ThreadLocalRandom.current().nextInt(serverList.size()));
            socket = SocketFactory.getDefault().createSocket();
            try {
                socket.connect(address, connectTimeout);
            } catch (Throwable t) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.addSuppressed(t);
                    throw e;
                }
            }
            connection = new Connection(socket);
        }
    }

    public ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    public String getPreferredNickName() {
        return preferredNickName;
    }

    public void setPreferredNickName(final String preferredNickName) {
        this.preferredNickName = preferredNickName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(final String realName) {
        this.realName = realName;
    }

    public void dispatch(final Event event) {

    }
}
