/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.picketlink.test.authentication.web.module;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PermissionManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.User;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

@Singleton
@Startup
public class SecurityInitializer {

    private static final String AGNES_LOGIN_NAME = "agnes";
    private static final String AGNES_EMAIL = "agnes@doe.com";
    private static final String AGNES_FIRST_NAME = "Agnes";
    private static final String AGNES_LAST_NAME = "Doe";
    private static final String AGNES_PASSWORD = "agnes_pass";
    private static final String AGNES_READ_PERMISSION_FILENAME = "readable-for-agnes.txt";
    private static final String AGNES_WRITE_PERMISSION_FILENAME = "writable-for-agnes.txt";
    private static final String AGNES_READ_PERMISSION_OPERATION = "read";
    private static final String AGNES_WRITE_PERMISSION_OPERATION = "write";

    @Inject
    private IdentityManager identityManager;

    @Inject
    PermissionManager permissionManager;

    @PostConstruct
    public void create() {
        User agnes = newUser(AGNES_LOGIN_NAME, AGNES_EMAIL, AGNES_FIRST_NAME, AGNES_LAST_NAME);
        identityManager.updateCredential(agnes, new Password(AGNES_PASSWORD));
        permissionManager.grantPermission(agnes, AGNES_READ_PERMISSION_FILENAME, AGNES_READ_PERMISSION_OPERATION);
        permissionManager.grantPermission(agnes, AGNES_WRITE_PERMISSION_FILENAME, AGNES_WRITE_PERMISSION_OPERATION);


        User user = new User("jane");
        user.setEmail("jane@doe.com");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        this.identityManager.add(user);
        this.identityManager.updateCredential(user, new Password("abcd1234"));
    }

    private User newUser(String loginName, String email, String firstName, String lastName) {
        User user = new User(loginName);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);

        this.identityManager.add(user);
        return user;
    }
}