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
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.AbstractToken;
import org.picketlink.idm.credential.Token;
import org.picketlink.idm.credential.TokenCredential;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.authentication.AbstractAuthenticationTestCase;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

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

        @Produces
        public IdentityConfiguration produceConfiguration() {
            IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

            builder
                .named("custom-config")
                    .stores()
                        .file()
                        .supportAllFeatures();

            return builder.build();
        }
    }


    public static class SimpleToken extends AbstractToken {

        public SimpleToken(String token) {
            super(token);
        }

        @Override
        public String getSubject() {
            return getToken();
        }
    }

    public static <T extends Token> T createToken(User user) {
        StringBuilder builder = new StringBuilder();

        builder
            .append(user.getLoginName());

        return (T) new SimpleToken(builder.toString());
    }
}