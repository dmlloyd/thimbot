/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

package com.flurg.thimbot.handler;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import com.flurg.thimbot.Priority;
import com.flurg.thimbot.event_old.inbound.AuthenticationChallengeEvent;
import com.flurg.thimbot.event_old.inbound.AuthenticationFailedEvent;
import com.flurg.thimbot.event_old.inbound.AuthenticationMechanismsEvent;
import com.flurg.thimbot.event_old.inbound.CapabilityAckEvent;
import com.flurg.thimbot.event_old.inbound.ConnectEvent;
import com.flurg.thimbot.event_old.outbound.CapabilityRequestEvent;
import com.flurg.thimbot.event_old.outbound.ConnectRequestEvent;
import com.flurg.thimbot.event_old.inbound.HangUpEvent;
import com.flurg.thimbot.event_old.outbound.HangUpRequestEvent;
import com.flurg.thimbot.event_old.AbstractEvent;
import com.flurg.thimbot.event_old.EventHandler;
import com.flurg.thimbot.event_old.EventHandlerContext;
import com.flurg.thimbot.event_old.inbound.LoggedInEvent;
import com.flurg.thimbot.event_old.inbound.LoggedOutEvent;
import com.flurg.thimbot.event_old.outbound.OutboundEvent;
import com.flurg.thimbot.event_old.inbound.PrivateNoticeEvent;
import com.flurg.thimbot.event_old.inbound.SaslMechanismListEvent;
import com.flurg.thimbot.event_old.TextEvent;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.RealmChoiceCallback;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

/**
 * A simple SASL-based authentication handler.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public class AuthenticationHandler extends EventHandler {
    private static final int STATE_AUTH_QUERY = 0;
    private static final int STATE_AUTH_REQ = 1;
    private static final int STATE_AUTH_DONE = 2;
    private static final int STATE_NO_SASL = 3;
    private static final byte[] NO_BYTES = new byte[0];
    private static final String[] NO_STRINGS = new String[0];
    private final String userName;
    private final char[] password;

    volatile int state = STATE_AUTH_QUERY;

    private final CallbackHandler callbackHandler;

    private LinkedHashSet<String> mechanisms = new LinkedHashSet<>();
    private SaslClient client;
    private CapabilityAckEvent capAck;

    public AuthenticationHandler(final String userName, final char[] password) {
        this.userName = userName;
        this.password = password;
        callbackHandler = callbacks -> {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(userName);
                } else if (callback instanceof PasswordCallback) {
                    ((PasswordCallback) callback).setPassword(password);
                } else if (callback instanceof RealmCallback) {
                    // accept default
                } else if (callback instanceof RealmChoiceCallback) {
                    // accept default
                }
            }
        };
    }

    public void handleEvent(final EventHandlerContext context, final AbstractEvent event) throws Exception {
        final int state = this.state;
        if (state == STATE_AUTH_QUERY || state == STATE_AUTH_REQ) {
            if (event instanceof OutboundEvent && event instanceof TextEvent) {
                // discard
                return;
            }
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final CapabilityAckEvent event) throws Exception {
        if (event.getCapabilities().contains("sasl")) {
            startAuthentication(context, event);
        } else {
            state = STATE_NO_SASL;
            super.handleEvent(context, event);
        }
    }

    public void handleEvent(final EventHandlerContext context, final PrivateNoticeEvent event) throws Exception {
        if (event.getFromNick().equals("NickServ")) {
            if (event.getText().startsWith("This nickname is registered.")) {
                event.getBot().sendMessage(Priority.HIGH, "NickServ", String.format("identify %s %s", userName, new String(password)));
            } else if (event.getText().startsWith("You are now identified for")) {
                state = STATE_AUTH_DONE;
                super.handleEvent(context, new LoggedInEvent(event.getBot(), event.getText()));
            } else {
                super.handleEvent(context, event);
            }
        } else {
            super.handleEvent(context, event);
        }
    }

    public void handleEvent(final EventHandlerContext context, final ConnectRequestEvent event) throws Exception {
        event.getBot().addDesiredCapability("sasl");
    }

    public void handleEvent(final EventHandlerContext context, final CapabilityRequestEvent event) throws Exception {
        event.getCapabilities().add("sasl");
    }

    public void handleEvent(final EventHandlerContext context, final ConnectEvent event) throws Exception {
        synchronized (callbackHandler) {
            state = STATE_AUTH_QUERY;
            mechanisms.clear();
            if (client != null) {
                client.dispose();
                client = null;
            }
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final HangUpEvent event) throws Exception {
        synchronized (callbackHandler) {
            mechanisms.clear();
            if (client != null) {
                client.dispose();
                client = null;
            }
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final AuthenticationChallengeEvent event) throws Exception {
        synchronized (callbackHandler) {
            try {
                event.getBot().saslResponse(event.process(client));
            } catch (SaslException ignored) {
                client.dispose();
                client = null;
                tryNextMechanism(context, event);
            }
        }
    }

    public void handleEvent(final EventHandlerContext context, final AuthenticationFailedEvent event) throws Exception {
        synchronized (callbackHandler) {
            mechanisms.remove(client.getMechanismName());
            tryNextMechanism(context, event);
        }
    }

    public void handleEvent(final EventHandlerContext context, final AuthenticationMechanismsEvent event) throws Exception {
        final List<String> mechanisms = event.getMechanisms();
        if (! mechanisms.isEmpty()) {
            synchronized (callbackHandler) {
                this.mechanisms.retainAll(mechanisms);
            }
        }
    }

    public void handleEvent(final EventHandlerContext context, final LoggedInEvent event) throws Exception {
        synchronized (callbackHandler) {
            if (client != null) {
                client.dispose();
                client = null;
            }
            mechanisms.clear();
            state = STATE_AUTH_DONE;
        }
        final CapabilityAckEvent capAck = this.capAck;
        this.capAck = null;
        if (capAck != null) super.handleEvent(context, capAck);
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final LoggedOutEvent event) throws Exception {
        synchronized (callbackHandler) {
            tryNextMechanism(context, event);
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final SaslMechanismListEvent event) throws Exception {
        synchronized (callbackHandler) {
            mechanisms.addAll(event.getMechanisms());
            tryNextMechanism(context, event);
        }
    }

    private void tryNextMechanism(final EventHandlerContext context, final AbstractEvent event) throws Exception {
        client = Sasl.createSaslClient(mechanisms.toArray(NO_STRINGS), null, "irc", event.getBot().getServerName(), Collections.<String, Object>emptyMap(), callbackHandler);
        if (client != null) {
            state = STATE_AUTH_REQ;
            try {
                event.getBot().saslAuthRequest(client.getMechanismName());
            } catch (SaslException ignored) {
                try {
                    client.dispose();
                } catch (SaslException ignored1) {}
            }
        } else {
            capAck = null;
            context.dispatch(new HangUpRequestEvent(event.getBot(), Priority.NORMAL));
        }
        return;
    }

    private void startAuthentication(final EventHandlerContext context, final CapabilityAckEvent event) throws Exception {
        synchronized (callbackHandler) {
            LinkedHashSet<String> mechs = new LinkedHashSet<>();
//            final Enumeration<SaslClientFactory> factories = Sasl.getSaslClientFactories();
//            Map<String, Object> props = new HashMap<>();
//            props.put(Sasl.POLICY_NOACTIVE, "false");
//            props.put(Sasl.POLICY_NOPLAINTEXT, "false");
//            props.put(Sasl.POLICY_NODICTIONARY, "false");
//            props.put(Sasl.POLICY_NOANONYMOUS, "true");
//            while (factories.hasMoreElements()) {
//                Collections.addAll(mechs, factories.nextElement().getMechanismNames(props));
//            }
//            mechs.remove("EXTERNAL");
//            mechs.remove("GSSAPI");
            mechs.add("PLAIN");
            mechanisms = mechs;
            capAck = event;
            tryNextMechanism(context, event);
        }
    }
}
