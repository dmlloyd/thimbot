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

import com.flurg.thimbot.event_old.inbound.AccountChangeEvent;
import com.flurg.thimbot.event_old.inbound.AuthenticationChallengeEvent;
import com.flurg.thimbot.event_old.inbound.AuthenticationFailedEvent;
import com.flurg.thimbot.event_old.inbound.AuthenticationMechanismsEvent;
import com.flurg.thimbot.event_old.inbound.AuthenticationSuccessfulEvent;
import com.flurg.thimbot.event_old.inbound.ChannelActionEvent;
import com.flurg.thimbot.event_old.inbound.ChannelCurrentModeEvent;
import com.flurg.thimbot.event_old.inbound.ChannelNoTopicEvent;
import com.flurg.thimbot.event_old.inbound.ChannelTimestampEvent;
import com.flurg.thimbot.event_old.inbound.ChannelTopicEvent;
import com.flurg.thimbot.util.IRCBase64;
import com.flurg.thimbot.event_old.inbound.LoggedInEvent;
import com.flurg.thimbot.event_old.inbound.ChannelCTCPCommandEvent;
import com.flurg.thimbot.event_old.inbound.ChannelCTCPResponseEvent;
import com.flurg.thimbot.event_old.inbound.CapabilityAckEvent;
import com.flurg.thimbot.event_old.inbound.CapabilityListEvent;
import com.flurg.thimbot.event_old.inbound.CapabilityNakEvent;
import com.flurg.thimbot.event_old.inbound.ChannelJoinEvent;
import com.flurg.thimbot.event_old.inbound.ChannelMessageEvent;
import com.flurg.thimbot.event_old.inbound.ChannelNoticeEvent;
import com.flurg.thimbot.event_old.inbound.ChannelPartEvent;
import com.flurg.thimbot.event_old.inbound.ErrorEvent;
import com.flurg.thimbot.event_old.AbstractEvent;
import com.flurg.thimbot.event_old.inbound.LoggedOutEvent;
import com.flurg.thimbot.event_old.inbound.MOTDEndEvent;
import com.flurg.thimbot.event_old.inbound.MOTDLineEvent;
import com.flurg.thimbot.event_old.inbound.NickChangeEvent;
import com.flurg.thimbot.event_old.inbound.PrivateActionEvent;
import com.flurg.thimbot.event_old.inbound.PrivateCTCPCommandEvent;
import com.flurg.thimbot.event_old.inbound.PrivateCTCPResponseEvent;
import com.flurg.thimbot.event_old.inbound.PrivateMessageEvent;
import com.flurg.thimbot.event_old.inbound.PrivateNoticeEvent;
import com.flurg.thimbot.event_old.inbound.QuitEvent;
import com.flurg.thimbot.event_old.inbound.ServerPingEvent;
import com.flurg.thimbot.event_old.inbound.ServerPongEvent;
import com.flurg.thimbot.event_old.inbound.UserAwayEvent;
import com.flurg.thimbot.event_old.inbound.UserBackEvent;
import com.flurg.thimbot.event_old.inbound.UserPongEvent;
import com.flurg.thimbot.event_old.inbound.WelcomeEvent;
import com.flurg.thimbot.raw.EmittableByteArrayOutputStream;
import com.flurg.thimbot.raw.LineListener;
import com.flurg.thimbot.raw.LineProtocolConnection;
import com.flurg.thimbot.util.IRCStringUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
* @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
*/
@SuppressWarnings("SpellCheckingInspection")
class IRCParser implements LineListener {

    private static final byte[] NO_BYTES = new byte[0];

    private final StringBuilder b = new StringBuilder();
    private final EmittableByteArrayOutputStream authBlock = new EmittableByteArrayOutputStream();

    IRCParser() {
    }

    public void handleLine(final ThimBot bot, final LineProtocolConnection connection, final byte[] buffer, final int offs, final int len) {
        final Input is = new Input(buffer, offs, len);
        int ch;
        final String source = parsePrefix(is);
        final String command = tokenize(is, ' ');
        switch (command) {
            case "CAP": {
                tokenize(is, ' ');
                final String subCommand = tokenize(is, ' ');
                switch (subCommand) {
                    case "LIST": {
                        // todo active capabilities
                        break;
                    }
                    case "LS": {
                        if (is.read() == ':') {
                            ArrayList<String> list = new ArrayList<>();
                            String s;
                            while (! (s = tokenize(is, ' ')).isEmpty()) {
                                list.add(s);
                            }
                            final CapabilityListEvent event = new CapabilityListEvent(bot, list);
                            bot.dispatch(event);
                        }
                        break;
                    }
                    case "ACK": {
                        if (is.read() == ':') {
                            ArrayList<String> list = new ArrayList<>();
                            String s;
                            while (! (s = tokenize(is, ' ')).isEmpty()) {
                                list.add(s);
                            }
                            final CapabilityAckEvent event = new CapabilityAckEvent(bot, list);
                            bot.dispatch(event);
                            break;
                        }
                    }
                    case "NAK": {
                        final CapabilityNakEvent event = new CapabilityNakEvent(bot);
                        bot.dispatch(event);
                        break;
                    }
                }
                break;
            }
            case "ACCOUNT": {
                if (IRCStringUtil.isUser(source)) {
                    final AccountChangeEvent event = new AccountChangeEvent(bot, source, tokenize(is, ' '));
                    bot.dispatch(event);
                }
                break;
            }
            case "AUTHENTICATE": {
                final String base64 = is.getRemaining(bot.getCharset());
                if (base64.equals("+")) {
                    final AuthenticationChallengeEvent event = new AuthenticationChallengeEvent(bot, NO_BYTES);
                    bot.dispatch(event);
                } else if (base64.length() == 400) {
                    try {
                        IRCBase64.decode(base64, 0, authBlock);
                    } catch (IOException e) {
                        throw new IllegalStateException();
                    }
                } else {
                    try {
                        IRCBase64.decode(base64, 0, authBlock);
                    } catch (IOException e) {
                        throw new IllegalStateException();
                    }
                    final AuthenticationChallengeEvent event = new AuthenticationChallengeEvent(bot, authBlock.toByteArray());
                    bot.dispatch(event);
                    authBlock.reset();
                }
                break;
            }
            case "AWAY": {
                if (IRCStringUtil.isUser(source)) {
                    if (is.read() == ':') {
                        final UserAwayEvent event = new UserAwayEvent(bot, source, is.getRemaining(bot.getCharset()));
                        bot.dispatch(event);
                    } else {
                        final UserBackEvent event = new UserBackEvent(bot, source);
                        bot.dispatch(event);
                    }
                }
                break;
            }
            case "INVITE": {
                break;
            }
            case "JOIN": {
                if (IRCStringUtil.isUser(source)) {
                    final ChannelJoinEvent event = new ChannelJoinEvent(bot, source, tokenize(is, ' '));
                    bot.dispatch(event);
                }
                break;
            }
            case "KICK": {
                break;
            }
            case "MODE": {
                /*
                << MODE #dmlloyd +c
                >> :niven.freenode.net 742 dmlloyd #dmlloyd c ipstrc :MODE cannot be set due to channel having an active MLOCK restriction policy
                << MODE #dmlloyd +m
                >> :dmlloyd!~dmlloyd@redhat/jboss/dmlloyd MODE #dmlloyd +m
                << MODE #dmlloyd -m
                >> :dmlloyd!~dmlloyd@redhat/jboss/dmlloyd MODE #dmlloyd -m
                 */
                if (IRCStringUtil.isUser(source)) {
                    String channel = tokenize(is, ' ');
                    if (IRCStringUtil.isChannel(channel)) {
                        String modes = is.getRemaining(bot.getCharset());
                        Modes modeMap = Modes.fromString(modes);

                    }
                }
                break;
            }
            case "NICK": {
                if (IRCStringUtil.isUser(source) && is.read() == ':') {
                    final NickChangeEvent event = new NickChangeEvent(bot, source, tokenize(is, ' '));
                    bot.dispatch(event);
                }
                break;
            }
            case "NOTICE": {
                if (IRCStringUtil.isUser(source)) {
                    is.mark(0);
                    ch = is.read();
                    final String target = tokenize(is, ' ');
                    if (is.read() == ':') {
                        is.mark(0);
                        if (is.read() == 1) {
                            final String subcommand = tokenize(is, ' ');
                            switch (subcommand) {
                                case "PONG": {
                                    final UserPongEvent event = new UserPongEvent(bot, source, tokenize(is, (char) 1));
                                    bot.dispatch(event);
                                    break;
                                }
                                default: {
                                    final AbstractEvent event;
                                    if (IRCStringUtil.isChannel(target)) {
                                        event = new ChannelCTCPResponseEvent(bot, source, target, tokenize(is, ' '), tokenize(is, (char) 1));
                                    } else {
                                        event = new PrivateCTCPResponseEvent(bot, source, tokenize(is, ' '), tokenize(is, (char) 1));
                                    }
                                    bot.dispatch(event);
                                    break;
                                }
                            }
                        } else {
                            is.reset();
                            final AbstractEvent event;
                            if (IRCStringUtil.isChannel(target)) {
                                event = new ChannelNoticeEvent(bot, source, target, is.getRemaining(bot.getCharset()));
                            } else {
                                event = new PrivateNoticeEvent(bot, source, is.getRemaining(bot.getCharset()));
                            }
                            bot.dispatch(event);
                        }
                    }
                }
                break;
            }
            case "PART": {
                if (IRCStringUtil.isUser(source)) {
                    final ChannelPartEvent event = new ChannelPartEvent(bot, source, tokenize(is, ' '), is.read() == ':' ? is.getRemaining(bot.getCharset()) : "");
                    bot.dispatch(event);
                }
                break;
            }
            case "PING": {
                is.mark(0);
                ch = is.read();
                while (ch != ':' && ch != -1) {
                    is.reset();
                    tokenize(is, ' ');
                    is.mark(0);
                    ch = is.read();
                }
                final ServerPingEvent event = new ServerPingEvent(bot, is.getRemaining(StandardCharsets.ISO_8859_1));
                bot.dispatch(event);
                break;
            }
            case "PONG": {
                is.mark(0);
                ch = is.read();
                while (ch != ':' && ch != -1) {
                    is.reset();
                    tokenize(is, ' ');
                    is.mark(0);
                    ch = is.read();
                }
                final ServerPongEvent event = new ServerPongEvent(bot, is.getRemaining(StandardCharsets.ISO_8859_1));
                bot.dispatch(event);
                break;
            }
            case "PRIVMSG": {
                if (IRCStringUtil.isUser(source)) {
                    is.mark(0);
                    final String target = tokenize(is, ' ');
                    if (is.read() == ':') {
                        is.mark(0);
                        if (is.read() == 1) {
                            final String subcommand = tokenize(is, ' ');
                            switch (subcommand) {
                                case "PONG": {
                                    final UserPongEvent event = new UserPongEvent(bot, source, tokenize(is, (char) 1));
                                    bot.dispatch(event);
                                    break;
                                }
                                case "ACTION": {
                                    final AbstractEvent event;
                                    if (IRCStringUtil.isChannel(target)) {
                                        event = new ChannelActionEvent(bot, source, target, tokenize(is, (char) 1));
                                    } else {
                                        event = new PrivateActionEvent(bot, source, tokenize(is, (char) 1));
                                    }
                                    bot.dispatch(event);
                                }
                                default: {
                                    final AbstractEvent event;
                                    if (IRCStringUtil.isChannel(target)) {
                                        event = new ChannelCTCPCommandEvent(bot, source, target, tokenize(is, ' '), tokenize(is, (char) 1));
                                    } else {
                                        event = new PrivateCTCPCommandEvent(bot, source, tokenize(is, ' '), tokenize(is, (char) 1));
                                    }
                                    bot.dispatch(event);
                                    break;
                                }
                            }
                        } else {
                            is.reset();
                            final AbstractEvent event;
                            if (IRCStringUtil.isChannel(target)) {
                                event = new ChannelMessageEvent(bot, source, target, is.getRemaining(bot.getCharset()));
                            } else {
                                event = new PrivateMessageEvent(bot, source, is.getRemaining(bot.getCharset()));
                            }
                            bot.dispatch(event);
                        }
                    }
                }
                break;
            }
            case "QUIT": {
                if (IRCStringUtil.isUser(source)) {
                    final String reason;
                    if (is.read() == ':') {
                        reason = is.getRemaining(bot.getCharset());
                    } else {
                        reason = "";
                    }
                    final QuitEvent event = new QuitEvent(bot, source, reason);
                    bot.dispatch(event);
                }
                break;
            }
            case "ERROR": {
                final ErrorEvent event = new ErrorEvent(bot, is.getRemaining(bot.getCharset()));
                bot.dispatch(event);
                break;
            }
            case "TOPIC": {
                // should only be client to server, ignore
                break;
            }
            case "WALLOPS": {
                break;
            }

            // Numerics start here

            case "001": { // welcome
                tokenize(is, ' ');
                if (is.read() == ':') {
                    bot.dispatch(new WelcomeEvent(bot, is.getRemaining(bot.getCharset())));
                } else {
                    bot.dispatch(new WelcomeEvent(bot, ""));
                }
                break;
            }
            case "002": { // your host is...
                break;
            }
            case "003": { // server created on...
                break;
            }
            case "004": { // server info...
                break;
            }
            case "005": { // bounce
                break;
            }

            case "301": { // RPL_AWAY: <nick> :<message>
                break;
            }
            case "302": { // RPL_USERHOST: <nick>[*]=(+|-)<hostname>
                // - away, + present
                break;
            }
            case "303": { // RPL_ISON: :<nick> [ <nick>[...]]
                break;
            }
            case "305": { // RPL_UNAWAY: :<message>
                // i.e. self "back"
                break;
            }
            case "306": { // RPL_NOWAWAY: :<message>
                // i.e. self "away"
                break;
            }

            case "311": { // RPL_WHOISUSER: <nick> <user> <host> * :<real name>
                break;
            }
            case "312": { // RPL_WHOISSERVER: <nick> <server> :<server info>
                break;
            }
            case "313": { // RPL_WHOISOPERATOR: <nick> :is an operator
                break;
            }
            case "314": { // RPL_WHOWASUSER: <nick> <user> <host> * :<real name>
                break;
            }
            case "315": { // RPL_ENDOFWHO: <name> :End of /WHO list
                break;
            }

            case "317": { // RPL_WHOISIDLE: <nick> <integer> :seconds idle
                break;
            }
            case "318": { // RPL_ENDOFWHOIS: <nick> :end of WHOIS list
                break;
            }
            case "319": { // RPL_WHOISCHANNELS: <nick> :[@|+]<channel>[ [@|+]<channel>[...]]
                break;
            }


            case "321": { // RPL_LISTSTART: obsolete
                break;
            }
            case "322": { // RPL_LIST: <channel> <count> :<topic>
                break;
            }
            case "323": { // RPL_LISTEND: :End of /LIST
                break;
            }

            case "324": { // RPL_CHANNEL_MODE_IS: <channel> <mode> <mode params>
                final String channel = tokenize(is, ' ');
                if (IRCStringUtil.isChannel(channel)) {
                    final Modes modeMap = Modes.fromString(is.getRemaining(bot.getCharset()));
                    bot.dispatch(new ChannelCurrentModeEvent(bot, channel, modeMap));
                }
                break;
            }
            case "325": { // RPL_UNIQOPIS: <channel> <nickname>
                break;
            }
            case "329": { // channel create timestamp
                final String channel = tokenize(is, ' ');
                if (IRCStringUtil.isChannel(channel)) try {
                    final long ts = Long.parseLong(tokenize(is, ':'));
                    Instant instant = Instant.ofEpochSecond(ts);
                    bot.dispatch(new ChannelTimestampEvent(bot, channel, instant));
                } catch (NumberFormatException ignored) {}
                break;
            }
            case "331": { // RPL_NOTOPIC: <channel> :No topic is set
                final String channel = tokenize(is, ' ');
                if (IRCStringUtil.isChannel(channel)) {
                    if (is.read() == ':') {
                        bot.dispatch(new ChannelNoTopicEvent(bot, channel));
                    }
                }
                break;
            }
            case "332": { // RPL_TOPIC: <channel> :<topic>
                final String channel = tokenize(is, ' ');
                if (IRCStringUtil.isChannel(channel)) {
                    if (is.read() == ':') {
                        bot.dispatch(new ChannelTopicEvent(bot, channel, is.getRemaining(bot.getCharset())));
                    }
                }
                break;
            }
            case "341": { // RPL_INVITING: <channel> <nick>
                break;
            }
            case "342": { // RPL_SUMMONING: <user> :Summoning to IRC
                break;
            }

            case "346": { // RPL_INVITELIST: <channel> <mask>
                break;
            }
            case "347": { // RPL_ENDOFINVITELIST: <channel> :End of list
                break;
            }

            case "348": { // RPL_EXCEPTLIST: <channel> <mask>
                break;
            }
            case "349": { // RPL_ENDOFEXCEPTLIST: <channel> :End of list
                break;
            }

            case "351": { // RPL_VERSION: <version>.<debuglevel> <server> :<comments>
                break;
            }
            case "352": { // RPL_WHOREPLY: <channel> <user> <host> <server> <nick> (H|G)[*[(@|+)]]
                break;
            }
            case "353": { // RPL_NAMREPLY: (=|*|@) <channel> :[(@\+)nick [(@\+)nick [...]]]
                break;
            }

            case "364": { // RPL_LINKS: <mask> <server> :<hopcount> <server info>
                break;
            }
            case "365": { // RPL_ENDOFLINKS: <mask> :End of LINKS list
                break;
            }
            case "366": { // RPL_ENDOFNAMES: <channel> :End names list
                break;
            }
            case "367": { // RPL_BANLIST: <channel> <banmask>
                break;
            }
            case "368": { // RPL_ENDOFBANLIST: <channel> :end of /BAN
                break;
            }
            case "369": { // RPL_ENDOFWHOWAS: <nick> :end of /WHOWAS
                break;
            }

            case "371": { // RPL_INFO: :<string>
                break;
            }
            case "374": { // RPL_ENDOFINFO: :End of /INFO
                break;
            }

            case "375": { // RPL_MOTDSTART: :- <server> blah MOTD
                tokenize(is, ' ');
                if (is.read() == ':') {
                    final MOTDLineEvent event = new MOTDLineEvent(bot, is.getRemaining(bot.getCharset()));
                    bot.dispatch(event);
                }
                break;
            }
            case "372": { // RPL_MOTD: :- <text>
                tokenize(is, ' ');
                if (is.read() == ':') {
                    final MOTDLineEvent event = new MOTDLineEvent(bot, is.getRemaining(bot.getCharset()));
                    bot.dispatch(event);
                }
                break;
            }
            case "376": { // RPL_ENDOFMOTD: :End of /MOTD
                tokenize(is, ' ');
                final MOTDEndEvent event = new MOTDEndEvent(bot);
                bot.dispatch(event);
                break;
            }

            case "670": { // RPL_STARTTLS
                // todo
                // SSLSocketFactory f = ...;
                // socket = f.createSocket(socket, host, port, true);
                break;
            }
            case "900": { // RPL_LOGGEDIN
                tokenize(is, ' ');
                if (is.read() == ':') {
                    bot.dispatch(new LoggedInEvent(bot, is.getRemaining(bot.getCharset())));
                } else {
                    bot.dispatch(new LoggedInEvent(bot, ""));
                }
                break;
            }
            case "901": { // RPL_LOGGEDOUT
                tokenize(is, ' ');
                if (is.read() == ':') {
                    bot.dispatch(new LoggedOutEvent(bot, is.getRemaining(bot.getCharset())));
                } else {
                    bot.dispatch(new LoggedOutEvent(bot, ""));
                }
                break;
            }
            case "462": // cannot reregister?
            case "902": // ERR_NICKLOCKED
            case "904": // ERR_SASLFAIL
            case "905": // ERR_SASLTOOLONG
            case "906": // ERR_SASLABORTED
            case "907": { // ERR_SASLALREADY
                tokenize(is, ' ');
                if (is.read() == ':') {
                    bot.dispatch(new AuthenticationFailedEvent(bot, is.getRemaining(bot.getCharset())));
                } else {
                    bot.dispatch(new AuthenticationFailedEvent(bot, ""));
                }
                break;
            }
            case "903": { // RPL_SASLSUCCESS
                tokenize(is, ' ');
                if (is.read() == ':') {
                    bot.dispatch(new AuthenticationSuccessfulEvent(bot, is.getRemaining(bot.getCharset())));
                } else {
                    bot.dispatch(new AuthenticationSuccessfulEvent(bot, ""));
                }
                break;
            }
            case "908": { // SASL mechanism list
                final String list = tokenize(is, ' ');
                final List<String> mechanisms = list.isEmpty() ? Collections.emptyList() : Arrays.asList(list.split(","));
                if (is.read() == ':') {
                    bot.dispatch(new AuthenticationMechanismsEvent(bot, mechanisms, is.getRemaining(bot.getCharset())));
                } else {
                    bot.dispatch(new AuthenticationMechanismsEvent(bot, mechanisms, ""));
                }
                break;
            }

            default: { // unknown
                break;
            }

        }
    }

    private String tokenize(final ByteArrayInputStream is, final char delim1) {
        final StringBuilder b = this.b;
        b.setLength(0);
        int ch;
        ch = is.read();
        while (ch != delim1 && ch != -1) {
            b.append((char) ch);
            ch = is.read();
        }
        try {
            return b.toString();
        } finally {
            b.setLength(0);
        }
    }

    @SuppressWarnings("unused")
    private String tokenize(final ByteArrayInputStream is, final char delim1, final char delim2) {
        final StringBuilder b = this.b;
        b.setLength(0);
        int ch;
        ch = is.read();
        while (ch != delim1 && ch != delim2 && ch != -1) {
            b.append((char) ch);
            ch = is.read();
        }
        try {
            return b.toString();
        } finally {
            b.setLength(0);
        }
    }

    private String parsePrefix(final ByteArrayInputStream is) {
        final StringBuilder b = this.b;
        b.setLength(0);
        int ch;
        is.mark(0);
        ch = is.read();
        if (ch != ':') {
            is.reset();
            return "";
        }
        ch = is.read();
        while (ch != ' ' && ch != -1) {
            b.append((char) ch);
            if (ch == '!') {
                ch = is.read();
                while (ch != ' ' && ch != -1) {
                    b.append((char) ch);
                    if (ch == '@') {
                        ch = is.read();
                        while (ch != ' ' && ch != -1) {
                            b.append((char) ch);
                            ch = is.read();
                        }
                        final String user = b.toString();
                        b.setLength(0);
                        return user;
                    } else {
                        ch = is.read();
                    }
                }
                break;
            } else {
                ch = is.read();
            }
        }
        b.setLength(0);
        return "";
    }

    public void terminated(final ThimBot bot, final LineProtocolConnection connection) {
        bot.terminated(connection);
    }

    static final class Input extends ByteArrayInputStream {

        Input(final byte[] buf) {
            super(buf);
        }

        Input(final byte[] buf, final int offset, final int length) {
            super(buf, offset, length);
        }

        public byte[] getBuf() {
            return buf;
        }

        public int getCount() {
            return count;
        }

        public int getMark() {
            return mark;
        }

        public int getPos() {
            return pos;
        }

        public String getRemaining(Charset charset) {
            final int length = count - pos;
            if (length == 0) return "";
            return new String(buf, pos, length, charset);
        }
    }
}
