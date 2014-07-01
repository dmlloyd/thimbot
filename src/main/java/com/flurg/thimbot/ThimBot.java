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

import static java.lang.Math.min;

import com.flurg.thimbot.event.AuthenticationRequestEvent;
import com.flurg.thimbot.event.AuthenticationResponseEvent;
import com.flurg.thimbot.event.CapabilityEndEvent;
import com.flurg.thimbot.event.CapabilityRequestEvent;
import com.flurg.thimbot.event.ChannelJoinRequestEvent;
import com.flurg.thimbot.event.ChannelPartRequestEvent;
import com.flurg.thimbot.event.ConnectEvent;
import com.flurg.thimbot.event.ConnectRequestEvent;
import com.flurg.thimbot.event.DisconnectEvent;
import com.flurg.thimbot.event.DisconnectRequestEvent;
import com.flurg.thimbot.event.Event;
import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;
import com.flurg.thimbot.event.IRCBase64;
import com.flurg.thimbot.event.OutboundActionEvent;
import com.flurg.thimbot.event.OutboundCTCPCommandEvent;
import com.flurg.thimbot.event.OutboundCTCPResponseEvent;
import com.flurg.thimbot.event.OutboundMessageEvent;
import com.flurg.thimbot.event.OutboundNoticeEvent;
import com.flurg.thimbot.event.QuitRequestEvent;
import com.flurg.thimbot.raw.AckEmittableByteArrayOutputStream;
import com.flurg.thimbot.raw.ByteOutput;
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

    static class SentBytes {
        final StringEmitter emitted;
        final long seq;

        SentBytes(final StringEmitter emitted, final long seq) {
            this.emitted = emitted;
            this.seq = seq;
        }
    }

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
        dispatch(new ConnectRequestEvent(this));
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
            connection.queueMessage(Priority.NORMAL, new LineOutputCallback() {
                public void writeLine(final ThimBot context, final ByteOutput target, final long seq) throws IOException {
                    target.write(IRCStrings.NICK);
                    target.write(' ');
                    target.write(desiredNick.getBytes(StandardCharsets.ISO_8859_1));
                }
            });
            connection.queueMessage(Priority.NORMAL, new LineOutputCallback() {
                public void writeLine(final ThimBot context, final ByteOutput target, final long seq) throws IOException {
                    target.write(IRCStrings.USER);
                    target.write(' ');
                    target.write(realName);
                    target.write(' ');
                    target.write('8');
                    target.write(' ');
                    target.write('*');
                    target.write(' ');
                    target.write(':');
                    target.write(realName.getBytes(StandardCharsets.ISO_8859_1));
                }
            });
            connection.start();
            this.connection = connection;
        }
        dispatch(new ConnectEvent(this));
    }

    public void dispatch(final Event event) {
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
        dispatch(new DisconnectEvent(this));
    }

    public void disconnect() throws IOException {
        synchronized (lock) {
            if (connection != null) connection.terminate();
        }
        dispatch(new DisconnectRequestEvent(this));
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
        do {
            current = new StringEmitter(iterator.next());
            if (baos.size() + current.length() >= 256) {
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
                getConnection().queueMessage(priority, baos);
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
                current.emit((ByteArrayOutputStream) baos);
            } else {
                baos.write(',');
                current.emit((ByteArrayOutputStream) baos);
            }
        } while (iterator.hasNext());
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
        HashSet<String> set = new HashSet<>(targets);
        sendRawMultiTarget(priority, CmdType.SIMPLE, set, IRCStrings.PRIVMSG, new StringEmitter(message));
        dispatch(new OutboundMessageEvent(this, set, message));
    }

    public void sendMessage(final Priority priority, final String target, final String message, final boolean redispatch) throws IOException {
        Set<String> set = Collections.singleton(target);
        sendRawMultiTarget(priority, CmdType.SIMPLE, set, IRCStrings.PRIVMSG, new StringEmitter(message));
        if (redispatch) dispatch(new OutboundMessageEvent(this, set, message));
    }

    public void sendMessage(final String target, final String message, final boolean redispatch) throws IOException {
        sendMessage(Priority.NORMAL, target, message, redispatch);
    }

    public void sendMessage(final Priority priority, final String target, final String message) throws IOException {
        sendMessage(priority, target, message, true);
    }

    public void sendMessage(final String target, final String message) throws IOException {
        sendMessage(Priority.NORMAL, target, message, true);
    }

    public void sendMessage(final Priority priority, final String[] targets, final String message) throws IOException {
        sendMessage(priority, Arrays.asList(targets), message);
    }

    // Action

    public void sendAction(final Priority priority, final Collection<String> targets, final String message) throws IOException {
        Set<String> set = new HashSet<>(targets);
        sendRawMultiTarget(priority, CmdType.CTCP_PRIVMSG, set, IRCStrings.ACTION, new StringEmitter(message));
        dispatch(new OutboundActionEvent(this, set, message));
    }

    public void sendAction(final Priority priority, final String target, final String message) throws IOException {
        Set<String> set = Collections.singleton(target);
        sendRawMultiTarget(priority, CmdType.CTCP_PRIVMSG, set, IRCStrings.ACTION, new StringEmitter(message));
        dispatch(new OutboundActionEvent(this, set, message));
    }

    public void sendAction(final String target, final String message) throws IOException {
        sendAction(Priority.NORMAL, target, message);
    }

    // Notice

    public void sendNotice(final Priority priority, final String target, final String message) throws IOException {
        Set<String> set = Collections.singleton(target);
        sendRawMultiTarget(priority, CmdType.SIMPLE, set, IRCStrings.NOTICE, new StringEmitter(message));
        dispatch(new OutboundNoticeEvent(this, set, message));
    }

    public void sendNotice(final String target, final String message) throws IOException {
        sendNotice(Priority.NORMAL, target, message);
    }

    // CTCP command

    public void sendCTCPCommand(final Priority priority, final String target, final StringEmitter command, final StringEmitter argument) throws IOException {
        Set<String> set = Collections.singleton(target);
        sendRawMultiTarget(priority, CmdType.CTCP_PRIVMSG, set, command, argument);
        dispatch(new OutboundCTCPCommandEvent(this, set, command.toString(), argument.toString()));
    }

    public void sendCTCPCommand(final Priority priority, final String target, final String command, final String argument) throws IOException {
        Set<String> set = Collections.singleton(target);
        sendRawMultiTarget(priority, CmdType.CTCP_PRIVMSG, set, new StringEmitter(command), new StringEmitter(argument));
        dispatch(new OutboundCTCPCommandEvent(this, set, command, argument));
    }

    public void sendCTCPCommand(final String target, final String command, final String argument) throws IOException {
        sendCTCPCommand(Priority.NORMAL, target, command, argument);
    }

    private LineProtocolConnection getConnection() throws IOException {
        final LineProtocolConnection connection = this.connection;
        if (connection == null) {
            throw notConnected();
        }
        return connection;
    }

    // CTCP response

    public void sendCTCPResponse(final Priority priority, final String target, final StringEmitter response, final StringEmitter argument) throws IOException {
        Set<String> set = Collections.singleton(target);
        sendRawMultiTarget(priority, CmdType.CTCP_NOTICE, set, response, argument);
        dispatch(new OutboundCTCPResponseEvent(this, set, response.toString(), argument.toString()));
    }

    public void sendCTCPResponse(final Priority priority, final String target, final String response, final String argument) throws IOException {
        Set<String> set = Collections.singleton(target);
        sendRawMultiTarget(priority, CmdType.CTCP_NOTICE, set, new StringEmitter(response), new StringEmitter(argument));
        dispatch(new OutboundCTCPResponseEvent(this, set, response, argument));
    }

    public void sendCTCPResponse(final String target, final String response, final String argument) throws IOException {
        sendCTCPResponse(Priority.NORMAL, target, response, argument);
    }

    // ping

    public void sendPing(final Priority priority, final String target, final StringEmitter argument) throws IOException {
        sendCTCPCommand(priority, target, IRCStrings.PING, argument);
    }

    public void sendPing(final Priority priority, final String target, final String argument) throws IOException {
        sendPing(priority, target, new StringEmitter(argument));
    }

    public void sendPing(final String target, final String argument) throws IOException {
        sendPing(Priority.NORMAL, target, argument);
    }

    // pong

    public void sendPong(final Priority priority, final String target, final StringEmitter argument) throws IOException {
        sendCTCPResponse(priority, target, IRCStrings.PING, argument);
    }

    public void sendPong(final Priority priority, final String target, final String argument) throws IOException {
        sendPong(priority, target, new StringEmitter(argument));
    }

    public void sendPong(final String target, final String argument) throws IOException {
        sendPong(Priority.NORMAL, target, argument);
    }

    public void sendPong(final Priority priority, final String payload) throws IOException {
        synchronized (lock) {
            getConnection().queueMessage(priority, new LineOutputCallback() {
                public void writeLine(final ThimBot context, final ByteOutput target, final long seq) throws IOException {
                    target.write(IRCStrings.PONG);
                    target.write(' ');
                    target.write(':');
                    target.write(payload.getBytes(StandardCharsets.UTF_8));
                }
            });
        }
    }

    public void sendPong(final String payload) throws IOException {
        sendPong(Priority.NORMAL, payload);
    }

    // join

    public void sendJoin(final String name) throws IOException {
        sendJoin(Priority.NORMAL, name);
    }

    public void sendJoin(final Priority priority, final String channel) throws IOException {
        synchronized (lock) {
            getConnection().queueMessage(priority, new LineOutputCallback() {
                public void writeLine(final ThimBot context, final ByteOutput target, final long seq) throws IOException {
                    target.write(IRCStrings.JOIN);
                    target.write(' ');
                    target.write(new StringEmitter(channel));
                }
            });
        }
        dispatch(new ChannelJoinRequestEvent(this, channel));
    }

    // part

    public void sendPart(final String channel, final String reason) throws IOException {
        sendPart(Priority.NORMAL, channel, reason);
    }

    public void sendPart(final Priority priority, final String channel, final String reason) throws IOException {
        synchronized (lock) {
            getConnection().queueMessage(priority, new LineOutputCallback() {
                public void writeLine(final ThimBot context, final ByteOutput target, final long seq) throws IOException {
                    target.write(IRCStrings.PART);
                    target.write(' ');
                    target.write(new StringEmitter(channel));
                    if (reason != null) {
                        target.write(' ');
                        target.write(':');
                        target.write(reason.getBytes(getCharset()));
                    }
                }
            });
        }
        dispatch(new ChannelPartRequestEvent(this, channel, reason));
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
        synchronized (lock) {
            getConnection().queueMessage(priority, new LineOutputCallback() {
                public void writeLine(final ThimBot context, final ByteOutput target, final long seq) throws IOException {
                    target.write(IRCStrings.QUIT);
                    if (reason != null) {
                        target.write(' ');
                        target.write(':');
                        target.write(reason.getBytes(getCharset()));
                    }
                }
            });
        }
        dispatch(new QuitRequestEvent(this, reason));
    }

    // mode

    public void sendModeRequest(final String target) throws IOException {
        sendModeRequest(Priority.NORMAL, target);
    }

    public void sendModeRequest(final Priority priority, final String target) throws IOException {
        synchronized (lock) {
            getConnection().queueMessage(priority, new LineOutputCallback() {
                public void writeLine(final ThimBot context, final ByteOutput bo, final long seq) throws IOException {
                    bo.write(IRCStrings.MODE);
                    bo.write(' ');
                    bo.write(target.getBytes(StandardCharsets.UTF_8));
                }
            });
        }
    }

    // SASL authenticate

    public void saslAuthRequest(final String mechanismName) throws IOException {
        saslAuthRequest(Priority.HIGH, mechanismName);
    }

    public void saslAuthRequest(final Priority priority, final String mechanismName) throws IOException {
        synchronized (lock) {
            getConnection().queueMessage(priority, new LineOutputCallback() {
                public void writeLine(final ThimBot context, final ByteOutput target, final long seq) throws IOException {
                    target.write(IRCStrings.AUTHENTICATE);
                    target.write(' ');
                    target.write(mechanismName);
                }
            });
        }
        dispatch(new AuthenticationRequestEvent(this, mechanismName));
    }

    // SASL response

    public void saslResponse(final byte[] response) throws IOException {
        saslResponse(Priority.HIGH, response);
    }

    public void saslResponse(final Priority priority, final byte[] response) throws IOException {
        synchronized (lock) {
            final int length = response.length;
            if (length == 0) {
                getConnection().queueMessage(priority, new LineOutputCallback() {
                    public void writeLine(final ThimBot context, final ByteOutput target, final long seq) throws IOException {
                        target.write(IRCStrings.AUTHENTICATE);
                        target.write(' ');
                        target.write('+');
                    }
                });
            } else for (int i = 0; i < length; i += 400) {
                final int start = i;
                getConnection().queueMessage(priority, new LineOutputCallback() {
                    public void writeLine(final ThimBot context, final ByteOutput target, final long seq) throws IOException {
                        target.write(IRCStrings.AUTHENTICATE);
                        target.write(' ');
                        IRCBase64.encode(response, start, min(400, length - start), target);
                    }
                });
            }
        }
        dispatch(new AuthenticationResponseEvent(this, response));
    }

    // capabilities

    void sendCapList() throws IOException {
        synchronized (lock) {
            getConnection().queueMessage(Priority.HIGH, new LineOutputCallback() {
                public void writeLine(final ThimBot context, final ByteOutput target, final long seq) throws IOException {
                    target.write(IRCStrings.CAP);
                    target.write(' ');
                    target.write(IRCStrings.LS);
                }
            });
        }
    }


    void sendCapReq(final Set<String> desiredCapabilities) throws IOException {
        final String[] caps = desiredCapabilities.toArray(new String[desiredCapabilities.size()]);
        synchronized (lock) {
            getConnection().queueMessage(Priority.HIGH, new LineOutputCallback() {
                public void writeLine(final ThimBot context, final ByteOutput target, final long seq) throws IOException {
                    target.write(IRCStrings.CAP);
                    target.write(' ');
                    if (caps.length == 0) {
                        target.write(IRCStrings.END);
                    } else {
                        target.write(IRCStrings.REQ);
                        target.write(' ');
                        target.write(':');
                        target.write(caps[0]);
                        for (int i = 1; i < caps.length; i++) {
                            target.write(' ');
                            target.write(caps[i]);
                        }
                    }
                }
            });
        }
        dispatch(caps.length == 0 ? new CapabilityEndEvent(this) : new CapabilityRequestEvent(this, caps));
    }

    void sendCapEndNoDispatch() throws IOException {
        synchronized (lock) {
            getConnection().queueMessage(Priority.HIGH, new LineOutputCallback() {
                public void writeLine(final ThimBot context, final ByteOutput target, final long seq) throws IOException {
                    target.write(IRCStrings.CAP);
                    target.write(' ');
                    target.write(IRCStrings.END);
                }
            });
        }
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
