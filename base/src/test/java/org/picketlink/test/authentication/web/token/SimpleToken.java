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
import org.picketlink.idm.credential.AbstractToken;
import org.picketlink.idm.credential.Token;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.basic.User;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Pedro Igor
 */
public class SimpleToken extends AbstractToken {

    public SimpleToken(String token) {
        super(token);
    }

    @Override
    public String getSubject() {
        return getToken();
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

    public static <T extends Token> T createToken(User user) {
        StringBuilder builder = new StringBuilder();

        builder.append(user.getLoginName());

        return (T) new SimpleToken(builder.toString());
    }
}
