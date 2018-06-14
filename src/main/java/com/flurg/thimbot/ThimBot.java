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

import com.flurg.thimbot.event.*;
import com.flurg.thimbot.event.connection.*;
import com.flurg.thimbot.raw.AckEmittableByteArrayOutputStream;
import com.flurg.thimbot.raw.EmissionKey;
import com.flurg.thimbot.raw.EmittableByteArrayOutputStream;
import com.flurg.thimbot.raw.LineOutputCallback;
import com.flurg.thimbot.raw.LineProtocolConnection;
import com.flurg.thimbot.raw.StringEmitter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import com.flurg.thimbot.util.IRCStringBuilder;
import javax.net.SocketFactory;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class ThimBot {

    static final Logger IRC_LOGGER = Logger.getLogger("com.flurg.thimbot.irc");
    final Object lock = new Object();

    private volatile Charset charset = StandardCharsets.UTF_8;
    private volatile SocketAddress address;
    private volatile SocketFactory socketFactory = SocketFactory.getDefault();

    private final CopyOnWriteArrayList<EventHandler> handlers = new CopyOnWriteArrayList<>();

    private LineProtocolConnection connection;
    private long eventSeq;
    private final Preferences prefs;
    private String login = "thimbot";
    private String realName = "ThimBot";
    private String version = "A ThimBot!";
    private String desiredNick = "thimbot";
    private String currentNick = desiredNick;
    private StringEmitter nickEmitter = new StringEmitter(currentNick);

    private final Map<EmissionKey, Long> acks = Collections.synchronizedMap(new LinkedHashMap<EmissionKey, Long>() {
        protected boolean removeEldestEntry(final Map.Entry<EmissionKey, Long> eldest) {
            return size() > 64;
        }
    });

    private final Set<String> joinedChannels = Collections.synchronizedSet(new TreeSet<String>());
    private final Set<String> desiredCapabilities = Collections.synchronizedSet(new HashSet<String>());

    static void install(final ThimBot bot) {
        final Preferences preferences = bot.getPreferences().node("log");
        final Handler handler = new Handler() {
            public void publish(final LogRecord record) {
                if (!Logging.suppressed() && isLoggable(record)) {
                    Logging.off();
                    try {
                        String channels = preferences.get("targets", "");
                        if (!channels.isEmpty()) {
                            String[] split = channels.split("\\s*,\\s*");
                            if (split.length > 0) {
                                try {
                                    String message = record.getMessage();
                                    if (message != null && !message.isEmpty()) {
                                        IRCStringBuilder b = new IRCStringBuilder(message.length());
                                        b.append("(*) ").b().append(record.getLevel()).b().nc().append(' ').append(message);
                                        bot.sendMessage(Priority.LOW, Arrays.asList(split), message);
                                    }
                                } catch (IOException ignored) {
                                }
                            }
                        }
                    } finally {
                        Logging.on();
                    }
                }
            }

            public void flush() {
            }

            public void close() {
            }
        };
        PreferenceChangeListener listener = new PreferenceChangeListener() {
            public void preferenceChange(final PreferenceChangeEvent evt) {
                if (evt.getKey().equals("level")) {
                    String level = evt.getNewValue();
                    Level realLevel = Level.INFO;
                    if (level != null) try {
                        realLevel = Level.parse(level);
                    } catch (IllegalArgumentException ignored) {
                    }
                    try {
                        handler.setLevel(realLevel);
                    } catch (SecurityException ignored) {}
                }
            }
        };
        listener.preferenceChange(new PreferenceChangeEvent(preferences, "level", preferences.get("level", "INFO")));
        preferences.addPreferenceChangeListener(listener);
        Logger logger = IRC_LOGGER;
        logger.setLevel(Level.ALL);
        logger.addHandler(handler);
    }

    public void errorf(String fmt, Object... args) {
        IRC_LOGGER.log(Level.SEVERE, String.format(fmt, args));
    }

    public void warnf(String fmt, Object... args) {
        IRC_LOGGER.log(Level.WARNING, String.format(fmt, args));
    }

    public void infof(String fmt, Object... args) {
        IRC_LOGGER.log(Level.INFO, String.format(fmt, args));
    }

    public void debugf(String fmt, Object... args) {
        if (IRC_LOGGER.isLoggable(Level.FINE)) {
            IRC_LOGGER.log(Level.FINE, String.format(fmt, args));
        }
    }

    public void tracef(String fmt, Object... args) {
        if (IRC_LOGGER.isLoggable(Level.FINEST)) {
            IRC_LOGGER.log(Level.FINEST, String.format(fmt, args));
        }
    }

    public void registerOutboundMessage(final EmissionKey key, final long seq) {
        acks.put(key, Long.valueOf(seq));
    }

    public void acknowledge(final EmissionKey key) {
        Long seq = acks.remove(key);
        if (seq != null) {
            acknowledge(seq.longValue());
        }
    }

    void addJoinedChannel(final String channel) {
        joinedChannels.add(channel);
    }

    void removeJoinedChannel(final String channel) {
        joinedChannels.remove(channel);
    }

    public Set<String> getJoinedChannels() {
        synchronized (joinedChannels) {
            return new HashSet<>(joinedChannels);
        }
    }

    public String getServerName() {
        return connection.getServerName();
    }

    public void addDesiredCapability(final String name) {
        desiredCapabilities.add(name);
    }

    public Set<String> getDesiredCapabilities() {
        synchronized (desiredCapabilities) {
            return new HashSet<>(desiredCapabilities);
        }
    }

    public ThimBot(Preferences preferences, SocketAddress address, SocketFactory socketFactory) {
        prefs = preferences;
        this.address = address;
        this.socketFactory = socketFactory;
        handlers.add(new FirstHandler());
    }

    public ThimBot(SocketAddress address, SocketFactory socketFactory) {
        this(Preferences.userRoot().node("thimbot"), address, socketFactory);
    }

    public void connect() throws IOException {
        dispatch(new ConnectRequestEvent(this, Priority.NORMAL));
        synchronized (lock) {
            if (connection != null) {
                throw new IllegalStateException("Already connected");
            }
            setBotNick(desiredNick);
            final InetSocketAddress inetSocketAddress = (InetSocketAddress) address;
            final String hostName = inetSocketAddress.getHostName();
            final Socket socket = socketFactory.createSocket(hostName, inetSocketAddress.getPort());
            socket.setTcpNoDelay(true);
            final LineProtocolConnection connection = new LineProtocolConnection(this, new IRCParser(), socket, 16384);
            connection.start();
            this.connection = connection;
        }
        dispatch(new ConnectEvent(this));
    }

    public void dispatch(final AbstractEvent event) {
        EventHandlerContext.dispatch(handlers, event);
    }

    public void queueMessage(final Priority priority, final LineOutputCallback callback) {
        synchronized (lock) {
            final LineProtocolConnection connection = this.connection;
            if (connection != null) connection.queueMessage(priority, callback);
        }
    }

    public void acknowledge(final long ackSeq) {
        synchronized (lock) {
            final LineProtocolConnection connection = this.connection;
            if (connection != null) connection.acknowledge(ackSeq);
        }
    }

    public void setWindowSize(final int size) {
        synchronized (lock) {
            final LineProtocolConnection connection = this.connection;
            if (connection != null) connection.setWindowSize(size);
        }
    }

    public String getBotNick() {
        return currentNick;
    }

    public long getEventSequence() {
        synchronized (lock) {
            return eventSeq++;
        }
    }

    void terminated(final LineProtocolConnection connection) {
        synchronized (lock) {
            connection.detach();
            this.connection = null;
        }
        dispatch(new HangUpEvent(this));
    }

    public void disconnect() throws IOException {
        synchronized (lock) {
            if (connection != null) connection.terminate();
        }
    }

    enum CmdType {
        SIMPLE,
        CTCP_PRIVMSG,
        CTCP_NOTICE,
    }

    void sendRawMultiTarget(final Priority priority, final CmdType cmdType, final Collection<String> targets, final StringEmitter command, final StringEmitter message) throws IOException {
        if (message.length() == 0) {
            new Throwable("Empty message being emitted").printStackTrace();
            return;
        }
        if (targets.size() == 0) {
            new Throwable("No targets").printStackTrace();
            return;
        }
        // we will limit the cmd + target list to <= 256 characters, and the message to <= 256 characters (or whatever the server will accept).
        // we limit recipients to 4 at a time
        final Iterator<String> iterator = targets.iterator();
        assert iterator.hasNext();

        StringEmitter current;
        EmittableByteArrayOutputStream baos = new AckEmittableByteArrayOutputStream(256, new EmissionKey(command, message));
        if (cmdType == CmdType.CTCP_PRIVMSG) {
            IRCStrings.PRIVMSG.emit((ByteArrayOutputStream) baos);
        } else if (cmdType == CmdType.CTCP_NOTICE) {
            IRCStrings.NOTICE.emit((ByteArrayOutputStream) baos);
        } else if (cmdType == CmdType.SIMPLE) {
            command.emit((ByteArrayOutputStream) baos);
        } else {
            throw new IllegalStateException();
        }
        baos.write(' ');
        nickEmitter.emit((ByteArrayOutputStream) baos);
        int c = 1;
        int t = 0;
        do {
            current = new StringEmitter(iterator.next());
            if (current.length() == 0) continue;
            t ++;
            if (baos.size() + current.length() >= 256 || c == 4) {
                // flush
                baos.write(' ');
                baos.write(':');
                if (cmdType != CmdType.SIMPLE) {
                    baos.write(1);
                    command.emit((ByteArrayOutputStream) baos);
                    baos.write(' ');
                    message.emit((ByteArrayOutputStream) baos);
                    baos.write(1);
                } else {
                    message.emit((ByteArrayOutputStream) baos);
                }
                synchronized (lock) {
                    getConnection().queueMessage(priority, baos);
                }
                baos = new EmittableByteArrayOutputStream(256);
                if (cmdType == CmdType.CTCP_PRIVMSG) {
                    IRCStrings.PRIVMSG.emit((ByteArrayOutputStream) baos);
                } else if (cmdType == CmdType.CTCP_NOTICE) {
                    IRCStrings.NOTICE.emit((ByteArrayOutputStream) baos);
                } else if (cmdType == CmdType.SIMPLE) {
                    command.emit((ByteArrayOutputStream) baos);
                } else {
                    throw new IllegalStateException();
                }
                baos.write(' ');
                nickEmitter.emit((ByteArrayOutputStream) baos);
                c = 1;
            }
            baos.write(',');
            current.emit((ByteArrayOutputStream) baos);
            c ++;
        } while (iterator.hasNext());
        if (t == 0) return;
        baos.write(' ');
        baos.write(':');
        if (cmdType != CmdType.SIMPLE) {
            baos.write(1);
            command.emit((ByteArrayOutputStream) baos);
            baos.write(' ');
            message.emit((ByteArrayOutputStream) baos);
            baos.write(1);
        } else {
            message.emit((ByteArrayOutputStream) baos);
        }
        synchronized (lock) {
            getConnection().queueMessage(priority, baos);
        }
    }

    // Message

    public void sendMessage(final Priority priority, final Collection<String> targets, final String message) throws IOException {
        dispatch(new OutboundMessageEvent(this, priority, new HashSet<>(targets), message));
    }

    public void sendMessage(final Priority priority, final String target, final String message) throws IOException {
        dispatch(new OutboundMessageEvent(this, priority, Collections.singleton(target), message));
    }

    public void sendMessage(final String target, final String message) throws IOException {
        dispatch(new OutboundMessageEvent(this, Priority.NORMAL, Collections.singleton(target), message));
    }

    public void sendMessage(final Priority priority, final String[] targets, final String message) throws IOException {
        sendMessage(priority, Arrays.asList(targets), message);
    }

    // Action

    public void sendAction(final Priority priority, final Collection<String> targets, final String message) throws IOException {
        dispatch(new OutboundActionEvent(this, priority, new HashSet<>(targets), message));
    }

    public void sendAction(final Priority priority, final String target, final String message) throws IOException {
        dispatch(new OutboundActionEvent(this, priority, Collections.singleton(target), message));
    }

    public void sendAction(final String target, final String message) throws IOException {
        sendAction(Priority.NORMAL, target, message);
    }

    // Notice

    public void sendNotice(final Priority priority, final String target, final String message) throws IOException {
        dispatch(new OutboundNoticeEvent(this, priority, Collections.singleton(target), message));
    }

    public void sendNotice(final String target, final String message) throws IOException {
        sendNotice(Priority.NORMAL, target, message);
    }

    // CTCP command

    public void sendCTCPCommand(final Priority priority, final String target, final String command, final String argument) throws IOException {
        dispatch(new OutboundCTCPCommandEvent(this, priority, Collections.singleton(target), command, argument));
    }

    public void sendCTCPCommand(final String target, final String command, final String argument) throws IOException {
        sendCTCPCommand(Priority.NORMAL, target, command, argument);
    }

    LineProtocolConnection getConnection() throws IOException {
        final LineProtocolConnection connection = this.connection;
        if (connection == null) {
            throw notConnected();
        }
        return connection;
    }

    // CTCP response

    public void sendCTCPResponse(final Priority priority, final String target, final String response, final String argument) throws IOException {
        dispatch(new OutboundCTCPResponseEvent(this, priority, Collections.singleton(target), response, argument));
    }

    public void sendCTCPResponse(final String target, final String response, final String argument) throws IOException {
        sendCTCPResponse(Priority.NORMAL, target, response, argument);
    }

    // ping

    public void sendPing(final Priority priority, final String target, final String argument) throws IOException {
        dispatch(new OutboundPingEvent(this, priority, Collections.singleton(target), argument));
    }

    public void sendPing(final String target, final String argument) throws IOException {
        sendPing(Priority.NORMAL, target, argument);
    }

    // pong

    public void sendPong(final Priority priority, final String target, final String argument) throws IOException {
        dispatch(new OutboundPongEvent(this, priority, Collections.singleton(target), argument));
    }

    public void sendPong(final String target, final String argument) throws IOException {
        sendPong(Priority.NORMAL, target, argument);
    }

    public void sendPong(final Priority priority, final String payload) throws IOException {
        dispatch(new OutboundServerPongEvent(this, priority, payload));
    }

    public void sendPong(final String payload) throws IOException {
        sendPong(Priority.NORMAL, payload);
    }

    // join

    public void sendJoin(final String name) throws IOException {
        sendJoin(Priority.NORMAL, name);
    }

    public void sendJoin(final Priority priority, final String channel) throws IOException {
        dispatch(new ChannelJoinRequestEvent(this, priority, channel));
    }

    // part

    public void sendPart(final String channel, final String reason) throws IOException {
        sendPart(Priority.NORMAL, channel, reason);
    }

    public void sendPart(final Priority priority, final String channel, final String reason) throws IOException {
        dispatch(new ChannelPartRequestEvent(this, priority, channel, reason));
    }

    // topic

    public void sendTopicRequest(final String channel) throws IOException {
        sendTopicRequest(Priority.NORMAL, channel);
    }

    public void sendTopicRequest(final Priority priority, final String channel) throws IOException {
        dispatch(new ChannelTopicRequestEvent(this, priority, channel));
    }

    public void sendTopicChangeRequest(final String channel, final String topic) throws IOException {
        sendTopicChangeRequest(Priority.NORMAL, channel, topic);
    }

    public void sendTopicChangeRequest(final Priority priority, final String channel, final String topic) throws IOException {
        dispatch(new ChannelTopicChangeRequestEvent(this, priority, channel, topic));
    }

    // quit

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
        dispatch(new QuitRequestEvent(this, priority, reason));
    }

    // mode

    public void sendModeRequest(final String target) throws IOException {
        sendModeRequest(Priority.NORMAL, target);
    }

    public void sendModeRequest(final Priority priority, final String target) throws IOException {
        dispatch(new ChannelModeRequestEvent(this, priority, target));
    }

    // SASL authenticate

    public void saslAuthRequest(final String mechanismName) throws IOException {
        saslAuthRequest(Priority.HIGH, mechanismName);
    }

    public void saslAuthRequest(final Priority priority, final String mechanismName) throws IOException {
        dispatch(new AuthenticationRequestEvent(this, priority, mechanismName));
    }

    // SASL response

    public void saslResponse(final byte[] response) throws IOException {
        saslResponse(Priority.HIGH, response);
    }

    public void saslResponse(final Priority priority, final byte[] response) throws IOException {
        dispatch(new AuthenticationResponseEvent(this, priority, response));
    }

    // capabilities

    void sendCapList() throws IOException {
        dispatch(new CapabilityListRequestEvent(this, Priority.HIGH));
    }


    void sendCapReq(final Set<String> desiredCapabilities) throws IOException {
        final String[] caps = desiredCapabilities.toArray(new String[desiredCapabilities.size()]);
        dispatch(caps.length == 0 ? new CapabilityEndEvent(this, Priority.NORMAL) : new CapabilityRequestEvent(this, Priority.NORMAL, caps));
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

    public void setLogin(final String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public void setDesiredNick(final String desiredNick) {
        this.desiredNick = desiredNick;
    }

    public String getDesiredNick() {
        return desiredNick;
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

    void setBotNick(final String nick) {
        if (! nick.equals(currentNick)) {
            currentNick = nick;
            nickEmitter = new StringEmitter(nick);
        }
    }
}
