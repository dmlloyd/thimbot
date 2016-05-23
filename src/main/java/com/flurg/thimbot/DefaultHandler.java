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
import java.util.Collections;
import java.util.Set;

import com.flurg.thimbot.event.AuthenticationRequestEvent;
import com.flurg.thimbot.event.AuthenticationResponseEvent;
import com.flurg.thimbot.event.CapabilityAckEvent;
import com.flurg.thimbot.event.CapabilityEndEvent;
import com.flurg.thimbot.event.CapabilityListEvent;
import com.flurg.thimbot.event.CapabilityListRequestEvent;
import com.flurg.thimbot.event.CapabilityNakEvent;
import com.flurg.thimbot.event.CapabilityRequestEvent;
import com.flurg.thimbot.event.ChannelJoinEvent;
import com.flurg.thimbot.event.ChannelJoinRequestEvent;
import com.flurg.thimbot.event.ChannelKickEvent;
import com.flurg.thimbot.event.ChannelModeRequestEvent;
import com.flurg.thimbot.event.ChannelPartEvent;
import com.flurg.thimbot.event.ChannelPartRequestEvent;
import com.flurg.thimbot.event.ChannelTopicChangeRequestEvent;
import com.flurg.thimbot.event.ChannelTopicRequestEvent;
import com.flurg.thimbot.event.ConnectEvent;
import com.flurg.thimbot.event.DisconnectEvent;
import com.flurg.thimbot.event.DisconnectRequestEvent;
import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;
import com.flurg.thimbot.event.IRCBase64;
import com.flurg.thimbot.event.NickChangeEvent;
import com.flurg.thimbot.event.OutboundActionEvent;
import com.flurg.thimbot.event.OutboundCTCPCommandEvent;
import com.flurg.thimbot.event.OutboundCTCPResponseEvent;
import com.flurg.thimbot.event.OutboundMessageEvent;
import com.flurg.thimbot.event.OutboundNoticeEvent;
import com.flurg.thimbot.event.OutboundPingEvent;
import com.flurg.thimbot.event.OutboundPongEvent;
import com.flurg.thimbot.event.OutboundServerPingEvent;
import com.flurg.thimbot.event.OutboundServerPongEvent;
import com.flurg.thimbot.event.PrivateActionEvent;
import com.flurg.thimbot.event.PrivateMessageEvent;
import com.flurg.thimbot.event.PrivateNoticeEvent;
import com.flurg.thimbot.event.QuitRequestEvent;
import com.flurg.thimbot.event.ServerPingEvent;
import com.flurg.thimbot.event.ServerPongEvent;
import com.flurg.thimbot.event.UserPingEvent;
import com.flurg.thimbot.raw.EmissionKey;
import com.flurg.thimbot.raw.StringEmitter;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class DefaultHandler extends EventHandler {
    private static final String[] NO_STRINGS = new String[0];

    private volatile boolean multiPrefix;
    private volatile boolean accountNotify;
    private volatile boolean extendedJoin;
    private volatile boolean inviteNotify;

    // connection

    public void handleEvent(final EventHandlerContext context, final ConnectEvent event) throws Exception {
        multiPrefix = accountNotify = extendedJoin = inviteNotify = false;
        context.redispatch(new CapabilityListRequestEvent(event.getBot(), Priority.HIGH));
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final CapabilityAckEvent event) throws Exception {
        super.handleEvent(context, event);
        final Set<String> capabilities = event.getCapabilities();
        multiPrefix |= capabilities.contains("multi-prefix");
        accountNotify |= capabilities.contains("account-notify");
        extendedJoin |= capabilities.contains("extended-join");
        inviteNotify |= capabilities.contains("invite-notify");
        context.redispatch(new CapabilityEndEvent(event.getBot(), Priority.NORMAL));
    }

    public void handleEvent(final EventHandlerContext context, final CapabilityEndEvent event) throws Exception {
        final ThimBot bot = event.getBot();
        synchronized (bot.lock) {
            bot.getConnection().queueMessage(Priority.HIGH, (context1, target, seq) -> {
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
        context.redispatch(new CapabilityEndEvent(event.getBot(), Priority.NORMAL));
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final CapabilityRequestEvent event) throws Exception {
        final Set<String> desiredCapabilities = event.getCapabilities();
        assert desiredCapabilities.size() > 0;
        final String[] caps = desiredCapabilities.toArray(NO_STRINGS);
        final ThimBot bot = event.getBot();
        synchronized (bot.lock) {
            bot.getConnection().queueMessage(Priority.HIGH, (bot1, target, seq) -> {
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

    public void handleEvent(final EventHandlerContext context, final DisconnectRequestEvent event) throws Exception {
        super.handleEvent(context, event);
        event.getBot().disconnect();
    }

    public void handleEvent(final EventHandlerContext context, final DisconnectEvent event) throws Exception {
        super.handleEvent(context, event);
        // we probably want to disconnect the outbound side now, if it wasn't already disconnected
        event.getBot().disconnect();
    }

    public void handleEvent(final EventHandlerContext context, final QuitRequestEvent event) throws Exception {
        synchronized (event.getBot().lock) {
            event.getBot().getConnection().queueMessage(event.getPriority(), (context1, target, seq) -> {
                target.write(IRCStrings.QUIT);
                final String reason = event.getRawReason();
                if (reason != null) {
                    target.write(' ');
                    target.write(':');
                    target.write(reason.getBytes(event.getBot().getCharset()));
                }
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
                target.write(event.getPayload().getBytes(StandardCharsets.UTF_8));
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
                target.write(event.getPayload().getBytes(StandardCharsets.UTF_8));
            });
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final ServerPingEvent event) throws Exception {
        context.redispatch(new OutboundServerPongEvent(event.getBot(), Priority.HIGH, event.getPayload()));
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final ServerPongEvent event) throws Exception {
        try {
            final String payload = event.getPayload();
            if (payload.charAt(0) == 'Q') {
                final int seq = Integer.parseInt(payload.substring(1));
                event.getBot().acknowledge(seq);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        super.handleEvent(context, event);
    }

    // user pings

    public void handleEvent(final EventHandlerContext context, final OutboundPongEvent event) throws Exception {
        event.getBot().sendRawMultiTarget(Priority.LOW, ThimBot.CmdType.CTCP_NOTICE, event.getTargets(), IRCStrings.PING, new StringEmitter(event.getPayload()));
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final OutboundPingEvent event) throws Exception {
        event.getBot().sendRawMultiTarget(event.getPriority(), ThimBot.CmdType.CTCP_NOTICE, event.getTargets(), IRCStrings.PONG, new StringEmitter(event.getPayload()));
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final UserPingEvent event) throws Exception {
        context.redispatch(new OutboundPongEvent(event.getBot(), Priority.LOW, Collections.singleton(event.getFromNick()), event.getPayload()));
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
