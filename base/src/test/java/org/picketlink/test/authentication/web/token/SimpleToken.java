/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.test.authentication.web.token;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.credential.DefaultToken;
import org.picketlink.idm.credential.Token;
import org.picketlink.idm.credential.storage.TokenCredentialStorage;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.annotation.StereotypeProperty;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

/**
 * @author Pedro Igor
 */
public class SimpleToken extends DefaultToken {

    public SimpleToken(String token) {
        super(token);
    }

    @ApplicationScoped
    public static class SimpleTokenProvider implements Token.Provider<SimpleToken> {

        @Inject
        private PartitionManager partitionManager;

        @Override
        public SimpleToken issue(Account account) {
            User user = (User) account;
            SimpleToken token = createToken(user);

            getIdentityManager(user).updateCredential(account, token);

            return token;
        }

        @Override
        public SimpleToken renew(Account account, SimpleToken currentToken) {
            return issue(account);
        }

        @Override
        public void invalidate(Account account) {

        }

        @Override
        public Class<SimpleToken> getTokenType() {
            return SimpleToken.class;
        }

        private IdentityManager getIdentityManager(User user) {
            return this.partitionManager.createIdentityManager(user.getPartition());
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
            return SimpleToken.class;
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

        private IdentityManager getIdentityManager(User user) {
            return this.partitionManager.createIdentityManager(user.getPartition());
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
