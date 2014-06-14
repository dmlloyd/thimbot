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

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;

import com.flurg.thimbot.Priority;
import com.flurg.thimbot.event.AuthenticationChallengeEvent;
import com.flurg.thimbot.event.AuthenticationFailedEvent;
import com.flurg.thimbot.event.AuthenticationRequestEvent;
import com.flurg.thimbot.event.AuthenticationResponseEvent;
import com.flurg.thimbot.event.CapabilityAckEvent;
import com.flurg.thimbot.event.CapabilityRequestEvent;
import com.flurg.thimbot.event.ConnectEvent;
import com.flurg.thimbot.event.ConnectRequestEvent;
import com.flurg.thimbot.event.DisconnectEvent;
import com.flurg.thimbot.event.Event;
import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;
import com.flurg.thimbot.event.LoggedInEvent;
import com.flurg.thimbot.event.LoggedOutEvent;
import com.flurg.thimbot.event.PrivateNoticeEvent;
import com.flurg.thimbot.event.SaslMechanismListEvent;
import com.flurg.thimbot.util.Arrays2;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
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
    private final String userName;
    private final char[] password;

    volatile int state = 0;

    private final Deque<String> mechanisms = new ArrayDeque<>();
    private final CallbackHandler callbackHandler;

    private SaslClient client;

    public AuthenticationHandler(final String userName, final char[] password) {
        this.userName = userName;
        this.password = password;
        callbackHandler = new CallbackHandler() {
            public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
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
            }
        };
    }

    public void handleEvent(final EventHandlerContext context, final CapabilityAckEvent event) throws Exception {
        if (event.getCapabilities().contains("sasl")) {
            initial(event);
        } else {
            state = STATE_NO_SASL;
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final PrivateNoticeEvent event) throws Exception {
        if (event.getFromNick().equals("NickServ")) {
            if (event.getText().startsWith("This nickname is registered.")) {
                event.getBot().sendMessage(Priority.HIGH, "NickServ", String.format("identify %s %s", userName, password));
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

    public void handleEvent(final EventHandlerContext context, final ConnectEvent event) throws Exception {
        synchronized (mechanisms) {
            mechanisms.clear();
            if (client != null) {
                client.dispose();
                client = null;
            }
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final DisconnectEvent event) throws Exception {
        synchronized (mechanisms) {
            mechanisms.clear();
            if (client != null) {
                client.dispose();
                client = null;
            }
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final AuthenticationChallengeEvent event) throws Exception {
        synchronized (mechanisms) {
            try {
                event.getBot().saslResponse(event.process(client));
            } catch (SaslException ignored) {
                client.dispose();
                client = null;
                tryNextMechanism(event);
            }
        }
    }

    public void handleEvent(final EventHandlerContext context, final AuthenticationFailedEvent event) throws Exception {
        synchronized (mechanisms) {
            tryNextMechanism(event);
        }
    }

    public void handleEvent(final EventHandlerContext context, final AuthenticationRequestEvent event) throws Exception {
        // suppress
    }

    public void handleEvent(final EventHandlerContext context, final AuthenticationResponseEvent event) throws Exception {
        // suppress
    }

    public void handleEvent(final EventHandlerContext context, final LoggedInEvent event) throws Exception {
        synchronized (mechanisms) {
            if (client != null) {
                client.dispose();
                client = null;
            }
            mechanisms.clear();
        }
        state = STATE_AUTH_DONE;
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final LoggedOutEvent event) throws Exception {
        initial(event);
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final SaslMechanismListEvent event) throws Exception {
        synchronized (mechanisms) {
            mechanisms.addAll(event.getMechanisms());
            tryNextMechanism(event);
        }
    }

    private void tryNextMechanism(final Event event) throws IOException {
        for (;;) {
            final String mechanismName = mechanisms.pollFirst();
            if (mechanismName == null) {
                event.getBot().disconnect();
                return;
            }
            client = Sasl.createSaslClient(Arrays2.of(mechanismName), null, "irc", event.getBot().getServerName(), Collections.<String, Object>emptyMap(), callbackHandler);
            if (client != null) {
                state = STATE_AUTH_REQ;
                try {
                    event.getBot().saslResponse(client.evaluateChallenge(new byte[0]));
                    return;
                } catch (SaslException ignored) {
                    try {
                        client.dispose();
                    } catch (SaslException ignored1) {}
                }
            }
        }
    }

    private void initial(final Event event) throws IOException {
        state = STATE_AUTH_QUERY;
        event.getBot().saslAuthRequest("BOGUS");
    }
}
