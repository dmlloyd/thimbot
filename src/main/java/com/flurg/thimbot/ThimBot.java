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

import com.flurg.thimbot.event.ActionEvent;
import com.flurg.thimbot.event.DisconnectEvent;
import com.flurg.thimbot.event.Event;
import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;
import com.flurg.thimbot.event.MessageEvent;
import com.flurg.thimbot.raw.ByteOutput;
import com.flurg.thimbot.raw.LineOutputCallback;
import com.flurg.thimbot.raw.LineProtocolConnection;
import com.flurg.thimbot.raw.StringEmitter;
import com.flurg.thimbot.source.Channel;
import com.flurg.thimbot.source.FullTarget;
import com.flurg.thimbot.source.Nick;
import com.flurg.thimbot.source.Server;
import com.flurg.thimbot.source.Target;
import com.flurg.thimbot.source.User;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.Preferences;

import javax.net.SocketFactory;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class ThimBot {

    final Object lock = new Object();

    private volatile Nick botNick = new Nick("thimbot");
    private volatile Charset charset = Charsets.UTF_8;
    private volatile SocketAddress address;
    private volatile SocketFactory socketFactory = SocketFactory.getDefault();

    private final CopyOnWriteArrayList<EventHandler> handlers = new CopyOnWriteArrayList<>();
    private final Map<Channel, JoinCallback> pendingJoins = new HashMap<>();

    private LineProtocolConnection<ThimBot> connection;
    private int eventSeq;
    private final Preferences prefs;
    private String login = "thimbot";
    private String initialNick = "thimbot";
    private String realName = "ThimBot";
    private String version = "A ThimBot!";

    public ThimBot(Preferences preferences, SocketAddress address, SocketFactory socketFactory) {
        prefs = preferences;
        this.address = address;
        this.socketFactory = socketFactory;
        handlers.add(new DefaultHandler());
    }

    public ThimBot(SocketAddress address, SocketFactory socketFactory) {
        this(Preferences.userRoot().node("thimbot"), address, socketFactory);
    }

    public void connect() throws IOException {
        synchronized (lock) {
            if (connection != null) {
                throw new IllegalStateException("Already connected");
            }
            setBotNick(new Nick(initialNick));
            final InetSocketAddress inetSocketAddress = (InetSocketAddress) address;
            final String hostName = inetSocketAddress.getHostName();
            final Socket socket = socketFactory.createSocket(hostName, inetSocketAddress.getPort());
            socket.setTcpNoDelay(true);
            final LineProtocolConnection<ThimBot> connection = new LineProtocolConnection<>(this, new IRCParser(new Server(hostName)), socket, 16384);
            connection.queueMessage(Priority.HIGH, new LineOutputCallback<ThimBot>() {
                public void writeLine(final ThimBot context, final ByteOutput target, final int seq) throws IOException {
                    target.write(IRCStrings.NICK);
                    target.write(' ');
                    target.write(initialNick.getBytes(Charsets.LATIN_1));
                }
            });
            connection.queueMessage(Priority.HIGH, new LineOutputCallback<ThimBot>() {
                public void writeLine(final ThimBot context, final ByteOutput target, final int seq) throws IOException {
                    target.write(IRCStrings.USER);
                    target.write(' ');
                    target.write(realName);
                    target.write(' ');
                    target.write('8');
                    target.write(' ');
                    target.write('*');
                    target.write(' ');
                    target.write(':');
                    target.write(realName.getBytes(Charsets.LATIN_1));
                }
            });
            connection.start();
            this.connection = connection;
        }
    }

    public EventHandlerContext createEventHandlerContext() {
        return new EventHandlerContext(handlers);
    }

    public void dispatch(final Event event) {
        dispatch(createEventHandlerContext(), event);
    }

    public void dispatch(final EventHandlerContext context, final Event event) {
        context.next(event);
    }

    JoinCallback takePendingJoin(String channel) {
        synchronized (lock) {
            return pendingJoins.remove(channel);
        }
    }

    public void queueMessage(final Priority priority, final LineOutputCallback<ThimBot> callback) {
        synchronized (lock) {
            final LineProtocolConnection<ThimBot> connection = this.connection;
            if (connection != null) connection.queueMessage(priority, callback);
        }
    }

    public void acknowledge(final int ackSeq) {
        synchronized (lock) {
            final LineProtocolConnection<ThimBot> connection = this.connection;
            if (connection != null) connection.acknowledge(ackSeq);
        }
    }

    public void setWindowSize(final int size) {
        synchronized (lock) {
            final LineProtocolConnection<ThimBot> connection = this.connection;
            if (connection != null) connection.setWindowSize(size);
        }
    }

    public Nick getBotNick() {
        return botNick;
    }

    public User getBotUser() {
        return new User(getBotNick(), "", "");
    }

    public int getEventSequence() {
        synchronized (lock) {
            return eventSeq++;
        }
    }

    void terminated(final LineProtocolConnection<ThimBot> connection) {
        synchronized (lock) {
            connection.detach();
            this.connection = null;
            dispatch(new DisconnectEvent(this));
        }
    }

    public void sendMessage(final Priority priority, final Target target, final String message) throws IOException {
        if (message.isEmpty()) {
            new Throwable("Empty message being emitted").printStackTrace();
            return;
        }
        final Preferences preferences = target.getPreferences(prefs);
        final String encoding = preferences.get("encoding", "UTF-8");
        final StringEmitter contents = new StringEmitter(message, encoding);
        synchronized (lock) {
            getConnection().queueMessage(priority, new LineOutputCallback<ThimBot>() {
                public void writeLine(final ThimBot context, final ByteOutput output, final int seq) throws IOException {
                    target.performSendMessage(output, contents);
                }
            });
        }
        dispatch(new MessageEvent(this, getBotUser(), target, message));
    }

    public void sendMessage(final Target target, final String message) throws IOException {
        sendMessage(Priority.NORMAL, target, message);
    }

    public void sendAction(final Priority priority, final Target target, final String message) throws IOException {
        final Preferences preferences = target.getPreferences(prefs);
        final String encoding = preferences.get("encoding", "UTF-8");
        final StringEmitter contents = new StringEmitter(message, encoding);
        synchronized (lock) {
            getConnection().queueMessage(priority, new LineOutputCallback<ThimBot>() {
                public void writeLine(final ThimBot context, final ByteOutput output, final int seq) throws IOException {
                    target.performSendAction(output, contents);
                }
            });
        }
        dispatch(new ActionEvent(this, getBotUser(), target, message));
    }

    public void sendAction(final Target target, final String message) throws IOException {
        sendAction(Priority.NORMAL, target, message);
    }

    public void sendNotice(final Priority priority, final FullTarget target, final String message) throws IOException {
        final Preferences preferences = target.getPreferences(prefs);
        final String encoding = preferences.get("encoding", "UTF-8");
        final StringEmitter contents = new StringEmitter(message, encoding);
        synchronized (lock) {
            getConnection().queueMessage(priority, new LineOutputCallback<ThimBot>() {
                public void writeLine(final ThimBot context, final ByteOutput output, final int seq) throws IOException {
                    target.performSendNotice(output, contents);
                }
            });
        }
    }

    public void sendNotice(final FullTarget target, final String message) throws IOException {
        sendNotice(Priority.NORMAL, target, message);
    }

    public void sendCTCPCommand(final Priority priority, final FullTarget target, final StringEmitter command, final StringEmitter argument) throws IOException {
        synchronized (lock) {
            getConnection().queueMessage(priority, new LineOutputCallback<ThimBot>() {
                public void writeLine(final ThimBot context, final ByteOutput output, final int seq) throws IOException {
                    target.performSendCTCPCommand(output, command, argument);
                }
            });
        }
    }

    public void sendCTCPCommand(final Priority priority, final FullTarget target, final String command, final String argument) throws IOException {
        final Preferences preferences = target.getPreferences(prefs);
        final String encoding = preferences.get("encoding", "UTF-8");
        final StringEmitter commandEmitter = new StringEmitter(command, encoding);
        final StringEmitter argumentEmitter = argument == null ? null : new StringEmitter(argument, encoding);
        sendCTCPCommand(priority, target, commandEmitter, argumentEmitter);
    }

    public void sendCTCPCommand(final FullTarget target, final String command, final String argument) throws IOException {
        sendCTCPCommand(Priority.NORMAL, target, command, argument);
    }

    public void sendCTCPResponse(final Priority priority, final FullTarget target, final StringEmitter response, final StringEmitter argument) throws IOException {
        synchronized (lock) {
            getConnection().queueMessage(priority, new LineOutputCallback<ThimBot>() {
                public void writeLine(final ThimBot context, final ByteOutput output, final int seq) throws IOException {
                    target.performSendCTCPResponse(output, response, argument);
                }
            });
        }
    }

    private LineProtocolConnection<ThimBot> getConnection() throws IOException {
        final LineProtocolConnection<ThimBot> connection = this.connection;
        if (connection == null) {
            throw notConnected();
        }
        return connection;
    }

    public void sendCTCPResponse(final Priority priority, final FullTarget target, final String response, final String argument) throws IOException {
        final Preferences preferences = target.getPreferences(prefs);
        final String encoding = preferences.get("encoding", "UTF-8");
        final StringEmitter responseEmitter = new StringEmitter(response, encoding);
        final StringEmitter argumentEmitter = argument == null ? null : new StringEmitter(argument, encoding);
        sendCTCPResponse(priority, target, responseEmitter, argumentEmitter);
    }

    public void sendCTCPResponse(final FullTarget target, final String response, final String argument) throws IOException {
        sendCTCPResponse(Priority.NORMAL, target, response, argument);
    }

    public void sendPing(final Priority priority, final FullTarget target, final StringEmitter argument) throws IOException {
        sendCTCPCommand(priority, target, IRCStrings.PING, argument);
    }

    public void sendPing(final Priority priority, final FullTarget target, final String argument) throws IOException {
        final Preferences preferences = target.getPreferences(prefs);
        final String encoding = preferences.get("encoding", "UTF-8");
        sendPing(priority, target, new StringEmitter(argument, encoding));
    }

    public void sendPing(final FullTarget target, final String argument) throws IOException {
        sendPing(Priority.NORMAL, target, argument);
    }

    public void sendPong(final Priority priority, final FullTarget target, final StringEmitter argument) throws IOException {
        sendCTCPResponse(priority, target, IRCStrings.PING, argument);
    }

    public void sendPong(final Priority priority, final FullTarget target, final String argument) throws IOException {
        final Preferences preferences = target.getPreferences(prefs);
        final String encoding = preferences.get("encoding", "UTF-8");
        sendPong(priority, target, new StringEmitter(argument, encoding));
    }

    public void sendPong(final FullTarget target, final String argument) throws IOException {
        sendPong(Priority.NORMAL, target, argument);
    }

    public void sendPong(final Priority priority, final String payload) throws IOException {
        getConnection().queueMessage(priority, new LineOutputCallback<ThimBot>() {
            public void writeLine(final ThimBot context, final ByteOutput target, final int seq) throws IOException {
                target.write(IRCStrings.PONG);
                target.write(' ');
                target.write(':');
                target.write(payload.getBytes(Charsets.LATIN_1));
            }
        });
    }

    public void sendPong(final String payload) throws IOException {
        sendPong(Priority.NORMAL, payload);
    }

    public void join(final Priority priority, final Channel channel) throws IOException {
        getConnection().queueMessage(priority, new JoinCallback(channel));
    }

    public void join(final Channel channel) throws IOException {
        join(Priority.NORMAL, channel);
    }

    public void part(final Priority priority, final Channel channel, final String reason) throws IOException {
        getConnection().queueMessage(priority, new LineOutputCallback<ThimBot>() {
            public void writeLine(final ThimBot context, final ByteOutput target, final int seq) throws IOException {
                target.write(IRCStrings.PART);
                target.write(' ');
                channel.writeName(target);
                if (reason != null) {
                    target.write(' ');
                    target.write(':');
                    target.write(reason.getBytes(getCharset(channel)));
                }
            }
        });
    }

    public void part(Channel channel, String reason) throws IOException {
        part(Priority.NORMAL, channel, reason);
    }

    public void part(Priority priority, Channel channel) throws IOException {
        part(priority, channel, null);
    }

    public void part(Channel channel) throws IOException {
        part(Priority.NORMAL, channel, null);
    }

    public void requestMode(Priority priority, final FullTarget target) throws IOException {
        getConnection().queueMessage(priority, new LineOutputCallback<ThimBot>() {
            public void writeLine(final ThimBot context, final ByteOutput output, final int seq) throws IOException {
                output.write(IRCStrings.MODE);
                output.write(' ');
                target.writeName(output);
            }
        });
    }

    public void quit() throws IOException {
        quit(Priority.NORMAL, null);
    }

    public void quit(String reason) throws IOException {
        quit(Priority.NORMAL, reason);
    }

    public void quit(Priority priority) throws IOException {
        quit(priority, null);
    }

    public void quit(Priority priority, final String reason) throws IOException {
        getConnection().queueMessage(priority, new LineOutputCallback<ThimBot>() {
            public void writeLine(final ThimBot context, final ByteOutput target, final int seq) throws IOException {
                target.write(IRCStrings.QUIT);
                if (reason != null) {
                    target.write(' ');
                    target.write(':');
                    target.write(reason.getBytes(getCharset()));
                }
            }
        });
    }

    public void requestMode(final FullTarget target) throws IOException {
        requestMode(Priority.NORMAL, target);
    }

    private static IOException notConnected() {
        return new IOException("Not connected");
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(final Charset charset) {
        this.charset = charset;
    }

    public Charset getCharset(final Target target) {
        return Charset.forName(target.getPreferences(prefs).get("encoding", "UTF-8"));
    }

    public void setLogin(final String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public void setInitialNick(final String initialNick) {
        this.initialNick = initialNick;
    }

    public String getInitialNick() {
        return initialNick;
    }

    public void setRealName(final String realName) {
        this.realName = realName;
    }

    public String getRealName() {
        return realName;
    }

    public void setVersion(final String version) {
        this.version = version;
    }


    public String getVersion() {
        return version;
    }

    public void addEventHandler(final EventHandler eventHandler) {
        if (eventHandler == null) {
            throw new IllegalArgumentException("eventHandler is null");
        }
        handlers.add(eventHandler);
    }

    public boolean removeEventHandler(final EventHandler eventHandler) {
        return handlers.remove(eventHandler);
    }

    public Preferences getPreferences() {
        return prefs;
    }

    void setBotNick(final Nick botNick) {
        this.botNick = botNick;
        initialNick = botNick.getName();
    }

    class JoinCallback implements LineOutputCallback<ThimBot> {

        private final Channel channel;
        private volatile int seq;

        JoinCallback(final Channel channel) {
            this.channel = channel;
        }

        public void writeLine(final ThimBot context, final ByteOutput target, final int seq) throws IOException {
            target.write(IRCStrings.JOIN);
            target.write(' ');
            channel.writeName(target);
            this.seq = seq;
            pendingJoins.put(channel, this);
        }

        void ack() {
            acknowledge(seq);
        }
    }
}
