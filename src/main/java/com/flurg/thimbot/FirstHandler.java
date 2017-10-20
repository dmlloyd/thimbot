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

import java.nio.charset.StandardCharsets;
import java.util.Set;

import com.flurg.thimbot.event.Event;
import com.flurg.thimbot.event.outbound.AuthenticationRequestEvent;
import com.flurg.thimbot.event.outbound.AuthenticationResponseEvent;
import com.flurg.thimbot.event.inbound.CapabilityAckEvent;
import com.flurg.thimbot.event.outbound.CapabilityEndEvent;
import com.flurg.thimbot.event.inbound.CapabilityListEvent;
import com.flurg.thimbot.event.outbound.CapabilityListRequestEvent;
import com.flurg.thimbot.event.inbound.CapabilityNakEvent;
import com.flurg.thimbot.event.outbound.CapabilityRequestEvent;
import com.flurg.thimbot.event.inbound.ChannelJoinEvent;
import com.flurg.thimbot.event.outbound.ChannelJoinRequestEvent;
import com.flurg.thimbot.event.inbound.ChannelKickEvent;
import com.flurg.thimbot.event.outbound.ChannelModeRequestEvent;
import com.flurg.thimbot.event.inbound.ChannelPartEvent;
import com.flurg.thimbot.event.outbound.ChannelPartRequestEvent;
import com.flurg.thimbot.event.outbound.ChannelTopicChangeRequestEvent;
import com.flurg.thimbot.event.outbound.ChannelTopicRequestEvent;
import com.flurg.thimbot.event.inbound.ConnectEvent;
import com.flurg.thimbot.event.inbound.HangUpEvent;
import com.flurg.thimbot.event.outbound.HangUpRequestEvent;
import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;
import com.flurg.thimbot.event.outbound.OutboundEvent;
import com.flurg.thimbot.util.IRCBase64;
import com.flurg.thimbot.event.outbound.IRCUserEvent;
import com.flurg.thimbot.event.inbound.NickChangeEvent;
import com.flurg.thimbot.event.outbound.NickChangeRequestEvent;
import com.flurg.thimbot.event.outbound.OutboundActionEvent;
import com.flurg.thimbot.event.outbound.OutboundCTCPCommandEvent;
import com.flurg.thimbot.event.outbound.OutboundCTCPResponseEvent;
import com.flurg.thimbot.event.outbound.OutboundMessageEvent;
import com.flurg.thimbot.event.outbound.OutboundNoticeEvent;
import com.flurg.thimbot.event.outbound.OutboundPingEvent;
import com.flurg.thimbot.event.outbound.OutboundPongEvent;
import com.flurg.thimbot.event.outbound.OutboundServerPingEvent;
import com.flurg.thimbot.event.outbound.OutboundServerPongEvent;
import com.flurg.thimbot.event.inbound.PrivateActionEvent;
import com.flurg.thimbot.event.inbound.PrivateMessageEvent;
import com.flurg.thimbot.event.inbound.PrivateNoticeEvent;
import com.flurg.thimbot.event.outbound.QuitRequestEvent;
import com.flurg.thimbot.event.inbound.ServerPongEvent;
import com.flurg.thimbot.raw.EmissionKey;
import com.flurg.thimbot.raw.StringEmitter;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class FirstHandler extends EventHandler {
    private static final String[] NO_STRINGS = new String[0];

    private volatile boolean multiPrefix;
    private volatile boolean accountNotify;
    private volatile boolean extendedJoin;
    private volatile boolean inviteNotify;

    public void handleEvent(final EventHandlerContext context, final Event event) throws Exception {
        if (event instanceof OutboundEvent) {
            System.out.println("<<< " + event);
        }
        super.handleEvent(context, event);
    }

    // connection

    public void handleEvent(final EventHandlerContext context, final ConnectEvent event) throws Exception {
        multiPrefix = accountNotify = extendedJoin = inviteNotify = false;
        final ThimBot bot = event.getBot();
        context.dispatch(new CapabilityListRequestEvent(bot, Priority.HIGH));
        context.dispatch(new NickChangeRequestEvent(bot, Priority.HIGH, bot.getDesiredNick()));
        context.dispatch(new IRCUserEvent(bot, Priority.HIGH, bot.getDesiredNick(), bot.getRealName()));
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final CapabilityAckEvent event) throws Exception {
        super.handleEvent(context, event);
        final Set<String> capabilities = event.getCapabilities();
        multiPrefix |= capabilities.contains("multi-prefix");
        accountNotify |= capabilities.contains("account-notify");
        extendedJoin |= capabilities.contains("extended-join");
        inviteNotify |= capabilities.contains("invite-notify");
        context.dispatch(new CapabilityEndEvent(event.getBot(), Priority.HIGH));
    }

    public void handleEvent(final EventHandlerContext context, final CapabilityEndEvent event) throws Exception {
        final ThimBot bot = event.getBot();
        synchronized (bot.lock) {
            bot.getConnection().queueMessage(event.getPriority(), (context1, target, seq) -> {
                target.write(IRCStrings.CAP);
                target.write(' ');
                target.write(IRCStrings.END);
            });
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final CapabilityListEvent event) throws Exception {
        Set<String> capabilities = event.getCapabilityNames();
        super.handleEvent(context, event);
        Set<String> desiredCapabilities = event.getBot().getDesiredCapabilities();
        desiredCapabilities.retainAll(capabilities);
        desiredCapabilities.remove("batch"); // not yet supported
        desiredCapabilities.remove("cap-notify"); // unsupported
        desiredCapabilities.remove("server-time"); // not yet supported
        desiredCapabilities.remove("userhost-in-names"); // not yet supported
        event.getBot().sendCapReq(desiredCapabilities);
    }

    public void handleEvent(final EventHandlerContext context, final CapabilityListRequestEvent event) throws Exception {
        final ThimBot bot = event.getBot();
        synchronized (bot.lock) {
            bot.getConnection().queueMessage(event.getPriority(), (bot1, target, seq) -> {
                target.write(IRCStrings.CAP);
                target.write(' ');
                target.write(IRCStrings.LS);
            });
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final CapabilityNakEvent event) throws Exception {
        // not much we can do here...
        context.dispatch(new CapabilityEndEvent(event.getBot(), Priority.NORMAL));
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final CapabilityRequestEvent event) throws Exception {
        final Set<String> desiredCapabilities = event.getCapabilities();
        assert desiredCapabilities.size() > 0;
        final String[] caps = desiredCapabilities.toArray(NO_STRINGS);
        final ThimBot bot = event.getBot();
        synchronized (bot.lock) {
            bot.getConnection().queueMessage(event.getPriority(), (bot1, target, seq) -> {
                target.write(IRCStrings.CAP);
                target.write(' ');
                target.write(IRCStrings.REQ);
                target.write(' ');
                target.write(':');
                target.write(caps[0]);
                for (int i = 1; i < caps.length; i++) {
                    target.write(' ');
                    target.write(caps[i]);
                }
            });
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final HangUpRequestEvent event) throws Exception {
        super.handleEvent(context, event);
        event.getBot().disconnect();
    }

    public void handleEvent(final EventHandlerContext context, final HangUpEvent event) throws Exception {
        super.handleEvent(context, event);
        // we probably want to disconnect the outbound side now, if it wasn't already disconnected
        event.getBot().disconnect();
    }

    public void handleEvent(final EventHandlerContext context, final QuitRequestEvent event) throws Exception {
        synchronized (event.getBot().lock) {
            event.getBot().getConnection().queueMessage(event.getPriority(), (context1, target, seq) -> {
                target.write(IRCStrings.QUIT);
                final String reason = event.getRawText();
                if (reason != null) {
                    target.write(' ');
                    target.write(':');
                    target.write(reason.getBytes(event.getBot().getCharset()));
                }
            });
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final NickChangeRequestEvent event) throws Exception {
        synchronized (event.getBot().lock) {
            event.getBot().getConnection().queueMessage(event.getPriority(), (context1, target, seq) -> {
                target.write(IRCStrings.NICK);
                target.write(' ');
                target.write(event.getNewNick().getBytes(StandardCharsets.ISO_8859_1));
            });
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final IRCUserEvent event) throws Exception {
        synchronized (event.getBot().lock) {
            event.getBot().getConnection().queueMessage(event.getPriority(), (context1, target, seq) -> {
                target.write(IRCStrings.USER);
                target.write(' ');
                target.write(event.getUserName());
                target.write(' ');
                target.write('8');
                target.write(' ');
                target.write('*');
                target.write(' ');
                target.write(':');
                target.write(event.getRealName().getBytes(StandardCharsets.ISO_8859_1));
            });
        }
        super.handleEvent(context, event);
    }

    // flow control - messages and actions

    public void handleEvent(final EventHandlerContext context, final OutboundActionEvent event) throws Exception {
        event.getBot().sendRawMultiTarget(event.getPriority(), ThimBot.CmdType.CTCP_PRIVMSG, event.getTargets(), IRCStrings.ACTION, new StringEmitter(event.getRawText()));
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final PrivateActionEvent event) throws Exception {
        if (event.isFromMe()) {
            EmissionKey key = new EmissionKey(IRCStrings.ACTION, new StringEmitter(event.getRawText()));
            event.getBot().acknowledge(key);
        } else {
            super.handleEvent(context, event);
        }
    }

    public void handleEvent(final EventHandlerContext context, final OutboundMessageEvent event) throws Exception {
        event.getBot().sendRawMultiTarget(event.getPriority(), ThimBot.CmdType.SIMPLE, event.getTargets(), IRCStrings.PRIVMSG, new StringEmitter(event.getRawText()));
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final PrivateMessageEvent event) throws Exception {
        if (event.isFromMe()) {
            EmissionKey key = new EmissionKey(IRCStrings.PRIVMSG, new StringEmitter(event.getRawText()));
            event.getBot().acknowledge(key);
        } else {
            super.handleEvent(context, event);
        }
    }

    public void handleEvent(final EventHandlerContext context, final OutboundNoticeEvent event) throws Exception {
        event.getBot().sendRawMultiTarget(event.getPriority(), ThimBot.CmdType.SIMPLE, event.getTargets(), IRCStrings.NOTICE, new StringEmitter(event.getRawText()));
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final PrivateNoticeEvent event) throws Exception {
        if (event.isFromMe()) {
            EmissionKey key = new EmissionKey(IRCStrings.NOTICE, new StringEmitter(event.getRawText()));
            event.getBot().acknowledge(key);
        } else {
            super.handleEvent(context, event);
        }
    }

    public void handleEvent(final EventHandlerContext context, final OutboundCTCPCommandEvent event) throws Exception {
        event.getBot().sendRawMultiTarget(event.getPriority(), ThimBot.CmdType.CTCP_PRIVMSG, event.getTargets(), new StringEmitter(event.getCommand()), new StringEmitter(event.getArgument()));
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final OutboundCTCPResponseEvent event) throws Exception {
        event.getBot().sendRawMultiTarget(event.getPriority(), ThimBot.CmdType.CTCP_NOTICE, event.getTargets(), new StringEmitter(event.getCommand()), new StringEmitter(event.getArgument()));
        super.handleEvent(context, event);
    }

    // nick tracking

    public void handleEvent(final EventHandlerContext context, final NickChangeEvent event) throws Exception {
        final ThimBot bot = event.getBot();
        if (event.isFromMe()) {
            bot.setBotNick(event.getNewNick());
        }
        super.handleEvent(context, event);
    }

    // channel tracking

    public void handleEvent(final EventHandlerContext context, final ChannelJoinRequestEvent event) throws Exception {
        final Priority priority = event.getPriority();
        final String channel = event.getChannel();
        final ThimBot bot = event.getBot();
        synchronized (bot.lock) {
            bot.getConnection().queueMessage(priority, (bot1, target, seq) -> {
                target.write(IRCStrings.JOIN);
                target.write(' ');
                target.write(new StringEmitter(channel));
            });
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelPartRequestEvent event) throws Exception {
        final Priority priority = event.getPriority();
        final String channel = event.getChannel();
        final String reason = event.getRawText();
        final ThimBot bot = event.getBot();
        synchronized (bot.lock) {
            bot.getConnection().queueMessage(priority, (context1, target, seq) -> {
                target.write(IRCStrings.PART);
                target.write(' ');
                target.write(new StringEmitter(channel));
                if (reason != null) {
                    target.write(' ');
                    target.write(':');
                    target.write(reason.getBytes(context1.getCharset()));
                }
            });
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelJoinEvent event) throws Exception {
        final ThimBot bot = event.getBot();
        if (event.isFromMe()) {
            bot.addJoinedChannel(event.getChannel());
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelPartEvent event) throws Exception {
        final ThimBot bot = event.getBot();
        if (event.isFromMe()) {
            bot.removeJoinedChannel(event.getChannel());
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelKickEvent event) throws Exception {
        final ThimBot bot = event.getBot();
        if (event.getTargets().contains(bot.getBotNick())) {
            bot.removeJoinedChannel(event.getChannel());
        }
        super.handleEvent(context, event);
    }

    // channel topic

    public void handleEvent(final EventHandlerContext context, final ChannelTopicRequestEvent event) throws Exception {
        final String channel = event.getChannel();
        synchronized (event.getBot().lock) {
            event.getBot().getConnection().queueMessage(event.getPriority(), (context1, target, seq) -> {
                target.write(IRCStrings.TOPIC);
                target.write(' ');
                target.write(new StringEmitter(channel));
            });
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final ChannelTopicChangeRequestEvent event) throws Exception {
        final ThimBot bot = event.getBot();
        synchronized (bot.lock) {
            bot.getConnection().queueMessage(event.getPriority(), (context1, target, seq) -> {
                target.write(IRCStrings.TOPIC);
                target.write(' ');
                target.write(new StringEmitter(event.getChannel()));
                target.write(' ');
                target.write(':');
                target.write(event.getRawText().getBytes(context1.getCharset()));
            });
        }
        super.handleEvent(context, event);
    }

    // channel mode

    public void handleEvent(final EventHandlerContext context, final ChannelModeRequestEvent event) throws Exception {
        final ThimBot bot = event.getBot();
        synchronized (bot.lock) {
            bot.getConnection().queueMessage(event.getPriority(), (context1, bo, seq) -> {
                bo.write(IRCStrings.MODE);
                bo.write(' ');
                bo.write(event.getChannel());
            });
        }
    }

    // server pings

    public void handleEvent(final EventHandlerContext context, final OutboundServerPingEvent event) throws Exception {
        final ThimBot bot = event.getBot();
        synchronized (bot.lock) {
            bot.getConnection().queueMessage(event.getPriority(), (context1, target, seq) -> {
                target.write(IRCStrings.PING);
                target.write(' ');
                target.write(event.getRawText().getBytes(StandardCharsets.UTF_8));
            });
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final OutboundServerPongEvent event) throws Exception {
        final ThimBot bot = event.getBot();
        synchronized (bot.lock) {
            bot.getConnection().queueMessage(event.getPriority(), (context1, target, seq) -> {
                target.write(IRCStrings.PONG);
                target.write(' ');
                target.write(event.getRawText().getBytes(StandardCharsets.UTF_8));
            });
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final ServerPongEvent event) throws Exception {
        final String payload = event.getRawText();
        if (payload.charAt(0) == 'Q') try {
            final int seq = Integer.parseInt(payload.substring(1));
            event.getBot().acknowledge(seq);
            return;
        } catch (NumberFormatException ignored) {
            // not one of our flow control pings after all
        }
        super.handleEvent(context, event);
    }

    // user pings

    public void handleEvent(final EventHandlerContext context, final OutboundPongEvent event) throws Exception {
        event.getBot().sendRawMultiTarget(event.getPriority(), ThimBot.CmdType.CTCP_NOTICE, event.getTargets(), IRCStrings.PONG, new StringEmitter(event.getRawText()));
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final OutboundPingEvent event) throws Exception {
        event.getBot().sendRawMultiTarget(event.getPriority(), ThimBot.CmdType.CTCP_NOTICE, event.getTargets(), IRCStrings.PING, new StringEmitter(event.getRawText()));
        super.handleEvent(context, event);
    }

    // SASL

    public void handleEvent(final EventHandlerContext context, final AuthenticationRequestEvent event) throws Exception {
        final String mechanismName = event.getMechanism();
        final ThimBot bot = event.getBot();
        synchronized (bot.lock) {
            bot.getConnection().queueMessage(event.getPriority(), (context1, target, seq) -> {
                target.write(IRCStrings.AUTHENTICATE);
                target.write(' ');
                target.write(mechanismName);
            });
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final AuthenticationResponseEvent event) throws Exception {
        final ThimBot bot = event.getBot();
        final byte[] response = event.getBytes();
        synchronized (bot.lock) {
            final int length = response.length;
            if (length == 0) {
                bot.getConnection().queueMessage(event.getPriority(), (context1, target, seq) -> {
                    target.write(IRCStrings.AUTHENTICATE);
                    target.write(' ');
                    target.write('+');
                });
            } else for (int i = 0; i < length; i += 400) {
                final int start = i;
                bot.getConnection().queueMessage(event.getPriority(), (context1, target, seq) -> {
                    target.write(IRCStrings.AUTHENTICATE);
                    target.write(' ');
                    IRCBase64.encode(response, start, min(400, length - start), target);
                });
            }
        }
        super.handleEvent(context, event);
    }
}
