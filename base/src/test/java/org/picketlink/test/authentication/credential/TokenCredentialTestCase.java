/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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
package org.picketlink.test.authentication.credential;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.Identity;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.DefaultToken;
import org.picketlink.idm.credential.Token;
import org.picketlink.idm.credential.TokenCredential;
import org.picketlink.idm.credential.handler.TokenCredentialHandler;
import org.picketlink.idm.credential.storage.TokenCredentialStorage;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.annotation.StereotypeProperty;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.authentication.AbstractAuthenticationTestCase;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.picketlink.idm.model.basic.BasicModel.getUser;

/**
 * @author pedroigor
 */
@RunWith(Arquillian.class)
public class TokenCredentialTestCase extends AbstractAuthenticationTestCase {

    private Token token;

    @Deployment
    public static WebArchive deploy() {
        return create(TokenCredentialTestCase.class);
    }

    @Before
    public void onSetup() {
        super.onSetup();

        IdentityManager identityManager = getIdentityManager();
        User user = getUser(identityManager, USER_NAME);
        this.token = createToken(user);

        identityManager.updateCredential(user, token);
    }

    @Test
    public void testSuccessfullAuthentication() {
        DefaultLoginCredentials credentials = getCredentials();

        credentials.setCredential(new TokenCredential(this.token));

        Identity identity = getIdentity();

        identity.login();

        assertTrue(identity.isLoggedIn());
        assertEquals(getAccount(), identity.getAccount());
    }

    @Test
    public void testUnsuccessfullAuthentication() {
        DefaultLoginCredentials credentials = getCredentials();

        credentials.setCredential(new TokenCredential(new SimpleToken("invalid")));

        Identity identity = getIdentity();

        identity.login();

        assertFalse(identity.isLoggedIn());
    }

    @ApplicationScoped
    public static class IDMConfiguration {

        @Inject
        private SimpleTokenConsumer tokenConsumer;

        @Produces
        public IdentityConfiguration produceConfiguration() {
            IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

            builder
                .named("custom-config")
                    .stores()
                        .file()
                        .setCredentialHandlerProperty(TokenCredentialHandler.TOKEN_CONSUMER, this.tokenConsumer)
                        .supportAllFeatures();

            return builder.build();
        }
    }


    public static class SimpleToken extends DefaultToken {

        public SimpleToken(String token) {
            super(token);
        }
    }

    @ApplicationScoped
    public static class SimpleTokenConsumer implements Token.Consumer<SimpleToken> {

        @Inject
        private PartitionManager partitionManager;

        @Override
        public Account getAccount(SimpleToken token) {
            String[] claims = token.getToken().split(";");

            if (claims.length != 3) {
                return null;
            }

            String realmName = claims[2].substring("issuer".length() + 1);
            String subject = claims[1].substring("subject".length() + 1);
            Realm partition = this.partitionManager.getPartition(Realm.class, realmName);
            IdentityManager identityManager = this.partitionManager.createIdentityManager(partition);

            IdentityQuery<User> query = identityManager.createIdentityQuery(User.class)
                .setParameter(User.LOGIN_NAME, subject);

            List<User> result = query.getResultList();

            if (!result.isEmpty()) {
                return result.get(0);
            }

            return null;
        }

        @Override
        public Class<SimpleToken> getTokenType() {
            return null;
        }

        private IdentityManager getIdentityManager(User user) {
            return this.partitionManager.createIdentityManager(user.getPartition());
        }

        @Override
        public boolean validate(SimpleToken token) {
            User user = (User) getAccount(token);
            TokenCredentialStorage tokenStorage = getIdentityManager(user)
                .retrieveCurrentCredential(user, TokenCredentialStorage.class);

            return tokenStorage.getValue().equals(token.getToken());
        }

        @Override
        public <I extends IdentityType> I extractIdentity(SimpleToken token, Class<I> identityType, StereotypeProperty.Property stereotypeProperty, Object identifier) {
            return null;
        }

        @Override
        public boolean supports(SimpleToken token) {
            return SimpleToken.class.equals(token.getClass());
        }
    }

    public static <T extends Token> T createToken(User user) {
        StringBuilder builder = new StringBuilder();

        builder
            .append("id=").append(UUID.randomUUID().toString())
            .append(";")
            .append("subject=").append(user.getLoginName())
            .append(";")
            .append("issuer=").append(user.getPartition().getName());

        return (T) new SimpleToken(builder.toString());
    }
}