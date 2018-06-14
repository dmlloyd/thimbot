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

package com.flurg.thimbot.event.handler;

import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.flurg.thimbot.ConnectionLocal;
import com.flurg.thimbot.event.*;
import com.flurg.thimbot.event.irc.cap.*;
import com.flurg.thimbot.event.irc.command.*;
import com.flurg.thimbot.event.irc.response.*;
import com.flurg.thimbot.util.ByteString;
import com.flurg.thimbot.util.ByteStringBuilder;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.RealmChoiceCallback;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslClientFactory;
import javax.security.sasl.SaslException;

/**
 * A simple SASL-based authentication handler.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public class AuthenticationHandler extends EventHandler {
    private static final byte[] NO_BYTES = new byte[0];
    private static final Capability SASL_CAP = new Capability("sasl");
    private final String userName;
    private final char[] password;

    private final CallbackHandler callbackHandler;
    private final SaslClientFactory saslClientFactory;

    private static final ConnectionLocal<AuthState> AUTH_STATE = new ConnectionLocal<>(AuthState::new, AuthState::dispose);

    static final class AuthState {
        private final ArrayDeque<String> mechanisms = new ArrayDeque<>();
        private boolean supported;
        private SaslClient client;
        private ByteStringBuilder challengeBuilder;

        AuthState() {
        }

        public boolean isSupported() {
            return supported;
        }

        public void setSupported(final boolean supported) {
            this.supported = supported;
        }

        public ArrayDeque<String> getMechanisms() {
            return mechanisms;
        }

        public SaslClient getClient() {
            return client;
        }

        public void setClient(final SaslClient client) {
            this.client = client;
        }

        public ByteStringBuilder getChallengeBuilder() {
            return challengeBuilder;
        }

        public void setChallengeBuilder(final ByteStringBuilder challengeBuilder) {
            this.challengeBuilder = challengeBuilder;
        }

        public void dispose() {
            final SaslClient client = this.client;
            if (client != null) try {
                client.dispose();
            } catch (SaslException e) {
                e.printStackTrace();
            } finally {
                this.client = null;
            }
        }
    }

    public AuthenticationHandler(final String userName, final char[] password, final SaslClientFactory saslClientFactory) {
        this.userName = userName;
        this.password = password;
        this.saslClientFactory = saslClientFactory;
        callbackHandler = callbacks -> {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(userName);
                } else if (callback instanceof PasswordCallback) {
                    ((PasswordCallback) callback).setPassword(password.clone());
                } else if (callback instanceof RealmCallback) {
                    final RealmCallback realmCallback = (RealmCallback) callback;
                    // accept default
                    realmCallback.setText(realmCallback.getDefaultText());
                } else if (callback instanceof RealmChoiceCallback) {
                    final RealmChoiceCallback realmChoiceCallback = (RealmChoiceCallback) callback;
                    // accept default
                    realmChoiceCallback.setSelectedIndex(realmChoiceCallback.getDefaultChoice());
                }
            }
        };
    }

    public void handleEvent(final EventHandlerContext context, final CapListSupportedEvent event) {
        final Capability sasl = event.getCapability("sasl");
        final AuthState authState = AUTH_STATE.get(context.getConnection());
        if (sasl != null) {
            authState.setSupported(true);
            final String[] values = sasl.getValueOrDefault("PLAIN").split(",");
            Collections.addAll(authState.getMechanisms(), values);
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final CapRequestEvent event) {
        if (event.isOutbound() && ! event.hasCapabilityNamed("sasl") && AUTH_STATE.get(context.getConnection()).isSupported()) {
            event.getCapabilities().add(SASL_CAP);
        }
    }

    public void handleEvent(final EventHandlerContext context, final CapAckEvent event) {
        if (event.isInbound() && event.hasCapabilityNamed("sasl")) {
            synchronized (callbackHandler) {
                tryNextMechanism(context);
            }
        } else {
            super.handleEvent(context, event);
        }
    }

    public void handleEvent(final EventHandlerContext context, final NoticeEvent event) {
        if (event.getSource().equals("NickServ")) {
            if (event.getDecodedText().contains("This nickname is registered")) {
                final MessageEvent idEvent = new MessageEvent(true, null, Collections.singletonList("NickServ"));
                idEvent.setDecodedText(String.format("identify %s %s", userName, new String(password)));
                context.dispatch(idEvent);
            } else if (event.getDecodedText().contains("You are now identified for")) {
                context.dispatch(new ResponseEvent(null, ResponseCode.LOGGED_IN));
            } else {
                super.handleEvent(context, event);
            }
        } else {
            super.handleEvent(context, event);
        }
    }

    public void handleEvent(final EventHandlerContext context, final AuthenticateEvent event) {
        synchronized (callbackHandler) {
            final AuthState authState = AUTH_STATE.get(context.getConnection());

            final String decodedText = event.getDecodedText();
            if (decodedText.length() == 400) {
                ByteStringBuilder challengeBuilder = authState.getChallengeBuilder();
                if (challengeBuilder == null) {
                    authState.setChallengeBuilder(challengeBuilder = new ByteStringBuilder(800));
                }
                challengeBuilder.append(Base64.getDecoder().decode(decodedText));
                return;
            }

            final byte[] challenge;
            final ByteStringBuilder challengeBuilder = authState.getChallengeBuilder();
            if (decodedText.equals("+")) {
                if (challengeBuilder == null) {
                    challenge = NO_BYTES;
                } else {
                    challenge = challengeBuilder.toByteString().toByteArray();
                    authState.setChallengeBuilder(null);
                }
            } else {
                if (challengeBuilder == null) {
                    challenge = Base64.getDecoder().decode(decodedText);
                } else {
                    challenge = challengeBuilder.append(Base64.getDecoder().decode(decodedText)).toByteArray();
                    authState.setChallengeBuilder(null);
                }
            }

            final SaslClient client = authState.getClient();
            try {
                final byte[] response = client.evaluateChallenge(challenge);
                final ByteString encoded = ByteString.fromBytes(Base64.getEncoder().encode(response));
                int idx = 0;
                while (encoded.length() - idx > 400) {
                    final AuthenticateEvent authenticateEvent = new AuthenticateEvent(true);
                    authenticateEvent.setEncodedText(encoded.substring(idx, idx + 400));
                    context.dispatch(authenticateEvent);
                    idx += 400;
                }
                if (encoded.length() - idx == 0) {
                    final AuthenticateEvent authenticateEvent = new AuthenticateEvent(true);
                    authenticateEvent.setDecodedText("+");
                    context.dispatch(authenticateEvent);
                }
                return;
            } catch (SaslException e) {
                authState.dispose();
                tryNextMechanism(context);
                return;
            }
        }
    }

    public void handleEvent(final EventHandlerContext context, final CapEndEvent event) {
        if (AUTH_STATE.get(context.getConnection()).isSupported()) {
            // don't propagate
            return;
        }
        super.handleEvent(context, event);
    }

    public void handleEvent(final EventHandlerContext context, final ResponseEvent event) {
        switch (event.getCode()) {
            case ResponseCode.LOGGED_IN: {
                // ok, fine
                context.dispatch(new CapEndEvent(null, Collections.emptyList()));
                return;
            }
            case ResponseCode.LOGGED_OUT: {
                AUTH_STATE.get(context.getConnection()).dispose();
                tryNextMechanism(context);
                return;
            }
            case ResponseCode.ERR_SASL_ABORTED:
            case ResponseCode.ERR_SASL_FAIL:
            case ResponseCode.ERR_SASL_TOO_LONG: {
                AUTH_STATE.get(context.getConnection()).dispose();
                tryNextMechanism(context);
                return;
            }
            case ResponseCode.ERR_SASL_ALREADY_AUTHED: {
                return;
            }
            case ResponseCode.SASL_MECHS: {
                // add mechs?
                return;
            }
        }
        super.handleEvent(context, event);
    }

    private void tryNextMechanism(final EventHandlerContext context) {
        Map<String, Object> props = new HashMap<>();
        props.put(Sasl.POLICY_NOACTIVE, "false");
        props.put(Sasl.POLICY_NOPLAINTEXT, "false");
        props.put(Sasl.POLICY_NODICTIONARY, "false");
        props.put(Sasl.POLICY_NOANONYMOUS, "true");
        final AuthState authState = AUTH_STATE.get(context.getConnection());
        final ArrayDeque<String> mechanisms = authState.getMechanisms();
        while (! mechanisms.isEmpty()) {
            final String mechName = mechanisms.pollFirst();
            final SaslClient client;
            try {
                client = saslClientFactory.createSaslClient(
                    new String[] { mechName },
                    userName,
                    "irc",
                    context.getConnection().getServerName(),
                    props,
                    callbackHandler
                );
            } catch (SaslException e) {
                // log & try again
                e.printStackTrace();
                continue;
            }
            if (client == null) continue;
            authState.setClient(client);
            final AuthenticateEvent authenticateEvent = new AuthenticateEvent(true);
            authenticateEvent.setDecodedText(mechName);
            context.dispatch(authenticateEvent);
            return;
        }
        // give up
    }
}
