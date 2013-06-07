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
import com.flurg.thimbot.event.CTCPCommandEvent;
import com.flurg.thimbot.event.CTCPResponseEvent;
import com.flurg.thimbot.event.ChannelJoinEvent;
import com.flurg.thimbot.event.ChannelPartEvent;
import com.flurg.thimbot.event.ErrorEvent;
import com.flurg.thimbot.event.MOTDEndEvent;
import com.flurg.thimbot.event.MOTDLineEvent;
import com.flurg.thimbot.event.MessageEvent;
import com.flurg.thimbot.event.NickChangeEvent;
import com.flurg.thimbot.event.NoticeEvent;
import com.flurg.thimbot.event.PingEvent;
import com.flurg.thimbot.event.PongEvent;
import com.flurg.thimbot.event.QuitEvent;
import com.flurg.thimbot.event.ServerPingEvent;
import com.flurg.thimbot.event.ServerPongEvent;
import com.flurg.thimbot.raw.LineListener;
import com.flurg.thimbot.raw.LineProtocolConnection;
import com.flurg.thimbot.source.Channel;
import com.flurg.thimbot.source.FullTarget;
import com.flurg.thimbot.source.HalfOpsInChannel;
import com.flurg.thimbot.source.Nick;
import com.flurg.thimbot.source.OpsInChannel;
import com.flurg.thimbot.source.Source;
import com.flurg.thimbot.source.User;
import com.flurg.thimbot.source.VoicedInChannel;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

/**
* @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
*/
@SuppressWarnings("SpellCheckingInspection")
class IRCParser implements LineListener<ThimBot> {
    private final Source serverSource;
    private final StringBuilder b = new StringBuilder();

    IRCParser(final Source serverSource) {
        this.serverSource = serverSource;
    }

    public void handleLine(final ThimBot bot, final LineProtocolConnection connection, final byte[] buffer, final int offs, final int len) {
        final Input is = new Input(buffer, offs, len);
        int ch;
        final Source source = parsePrefix(is);
        final String command = tokenize(is, ' ');
        switch (command) {
            case "INVITE": {
                break;
            }
            case "JOIN": {
                if (source instanceof User) {
                    bot.dispatch(new ChannelJoinEvent(bot, (User) source, new Channel(tokenize(is, ' '))));
                }
                break;
            }
            case "KICK": {
                break;
            }
            case "MODE": {
                break;
            }
            case "NICK": {
                if (source instanceof User && is.read() == ':') {
                    bot.dispatch(new NickChangeEvent(bot, (User) source, new Nick(tokenize(is, ' '))));
                }
                break;
            }
            case "NOTICE": {
                if (source instanceof User) {
                    final User user = (User) source;
                    final FullTarget target;
                    is.mark(0);
                    ch = is.read();
                    if (ch == '+') {
                        target = new VoicedInChannel(new Channel(tokenize(is, ' ')));
                    } else if (ch == '%') {
                        target = new HalfOpsInChannel(new Channel(tokenize(is, ' ')));
                    } else if (ch == '@') {
                        target = new OpsInChannel(new Channel(tokenize(is, ' ')));
                    } else if (ch == '#' || ch == '!' || ch == '&') {
                        is.reset();
                        target = new Channel(tokenize(is, ' '));
                    } else {
                        is.reset();
                        target = new Nick(tokenize(is, ' '));
                    }
                    if (is.read() == ':') {
                        is.mark(0);
                        if (is.read() == 1) {
                            final String subcommand = tokenize(is, ' ');
                            switch (subcommand) {
                                case "PONG": {
                                    bot.dispatch(new PongEvent(bot, user, target, tokenize(is, (char)1)));
                                    break;
                                }
                                default: {
                                    bot.dispatch(new CTCPResponseEvent(bot, user, target, tokenize(is, ' '), tokenize(is, (char)1)));
                                    break;
                                }
                            }
                        } else {
                            is.reset();
                            bot.dispatch(new NoticeEvent(bot, user, target, is.getRemaining(bot.getCharset(target))));
                        }
                    }
                }
                break;
            }
            case "PART": {
                if (source instanceof User) {
                    bot.dispatch(new ChannelPartEvent(bot, (User) source, new Channel(tokenize(is, ' ')), is.read() == ':' ? is.getRemaining(bot.getCharset(((User) source).getNick())) : ""));
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
                final ServerPingEvent event = new ServerPingEvent(bot, is.getRemaining(Charsets.LATIN_1));
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
                bot.dispatch(new ServerPongEvent(bot, is.getRemaining(Charsets.LATIN_1)));
                break;
            }
            case "PRIVMSG": {
                if (source instanceof User) {
                    final User user = (User) source;
                    final FullTarget target;
                    is.mark(0);
                    ch = is.read();
                    if (ch == '+') {
                        target = new VoicedInChannel(new Channel(tokenize(is, ' ')));
                    } else if (ch == '%') {
                        target = new HalfOpsInChannel(new Channel(tokenize(is, ' ')));
                    } else if (ch == '@') {
                        target = new OpsInChannel(new Channel(tokenize(is, ' ')));
                    } else if (ch == '#' || ch == '!' || ch == '&') {
                        is.reset();
                        target = new Channel(tokenize(is, ' '));
                    } else {
                        is.reset();
                        target = new Nick(tokenize(is, ' '));
                    }
                    if (is.read() == ':') {
                        is.mark(0);
                        if (is.read() == 1) {
                            final String subcommand = tokenize(is, ' ');
                            switch (subcommand) {
                                case "ACTION": {
                                    bot.dispatch(new ActionEvent(bot, user, target, tokenize(is, (char)1)));
                                    break;
                                }
                                case "PING": {
                                    bot.dispatch(new PingEvent(bot, user, target, tokenize(is, (char)1)));
                                    break;
                                }
                                default: {
                                    bot.dispatch(new CTCPCommandEvent(bot, user, target, tokenize(is, ' '), tokenize(is, (char)1)));
                                    break;
                                }
                            }
                        } else {
                            is.reset();
                            bot.dispatch(new MessageEvent(bot, user, target, is.getRemaining(bot.getCharset(target))));
                        }
                    }
                }
                break;
            }
            case "QUIT": {
                if (source instanceof User) {
                    final User user = (User) source;
                    final String reason;
                    if (is.read() == ':') {
                        reason = is.getRemaining(bot.getCharset(user.getNick()));
                    } else {
                        reason = "";
                    }
                    bot.dispatch(new QuitEvent(bot, user, reason));
                }
                break;
            }
            case "ERROR": {
                bot.dispatch(new ErrorEvent(bot, is.getRemaining(bot.getCharset())));
                break;
            }
            case "TOPIC": {
                break;
            }
            case "WALLOPS": {
                break;
            }

            // Numerics start here

            case "001": { // welcome
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
                break;
            }
            case "325": { // RPL_UNIQOPIS: <channel> <nickname>
                break;
            }

            case "331": { // RPL_NOTOPIC: <channel> :No topic is set
                break;
            }
            case "332": { // RPL_TOPIC: <channel> :<topic>
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
                final String nick = tokenize(is, ' ');
                if (is.read() == ':') {
                    bot.dispatch(new MOTDLineEvent(bot, is.getRemaining(bot.getCharset())));
                }
                break;
            }
            case "372": { // RPL_MOTD: :- <text>
                final String nick = tokenize(is, ' ');
                if (is.read() == ':') {
                    bot.dispatch(new MOTDLineEvent(bot, is.getRemaining(bot.getCharset())));
                }
                break;
            }
            case "376": { // RPL_ENDOFMOTD: :End of /MOTD
                final String nick = tokenize(is, ' ');
                bot.dispatch(new MOTDEndEvent(bot));
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

    private Source parsePrefix(final ByteArrayInputStream is) {
        final StringBuilder b = this.b;
        b.setLength(0);
        int ch;
        is.mark(0);
        ch = is.read();
        if (ch != ':') {
            is.reset();
            return serverSource;
        }
        ch = is.read();
        while (ch != ' ' && ch != -1) {
            if (ch == '!') {
                final String nick = b.toString();
                b.setLength(0);
                ch = is.read();
                while (ch != ' ' && ch != -1) {
                    if (ch == '@') {
                        final String login = b.toString();
                        b.setLength(0);
                        ch = is.read();
                        while (ch != ' ' && ch != -1) {
                            b.append((char) ch);
                            ch = is.read();
                        }
                        final String host = b.toString();
                        b.setLength(0);
                        return new User(new Nick(nick), login, host);
                    } else {
                        b.append((char) ch);
                        ch = is.read();
                    }
                }
                break;
            } else {
                b.append((char) ch);
                ch = is.read();
            }
        }
        b.setLength(0);
        return serverSource;
    }

    public void terminated(final ThimBot bot, final LineProtocolConnection<ThimBot> connection) {
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
