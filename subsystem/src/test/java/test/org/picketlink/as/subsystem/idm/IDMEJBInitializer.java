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
package test.org.picketlink.as.subsystem.idm;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * @author Pedro Igor
 */
@Singleton
@Startup
public class IDMEJBInitializer {

    @Resource(mappedName = "java:/picketlink/JPADSBasedPartitionManager")
    private PartitionManager jpaDSBasedPartitionManager;

    @PostConstruct
    public void init() {
        Realm defaultRealm = this.jpaDSBasedPartitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM);

        if (defaultRealm == null) {
            defaultRealm = new Realm(Realm.DEFAULT_REALM);

            this.jpaDSBasedPartitionManager.add(defaultRealm);
        }

        IdentityManager identityManager = this.jpaDSBasedPartitionManager.createIdentityManager();

        User user = BasicModel.getUser(identityManager, "johny");

        if (user != null) {
            identityManager.remove(user);
        }

        user = new User("johny");

        identityManager.add(user);

        Password password = new Password("abcd1234");

        identityManager.updateCredential(user, password);

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user.getLoginName(), password);

        Role role = BasicModel.getRole(identityManager, "admin");

        if (role != null) {
            identityManager.remove(role);
        }

        role = new Role("admin");

        identityManager.add(role);

        RelationshipManager relationshipManager = this.jpaDSBasedPartitionManager.createRelationshipManager();

        BasicModel.grantRole(relationshipManager, user, role);

        user.setAttribute(new Attribute<String>("testAttribute", "value"));

        identityManager.update(user);

        user = BasicModel.getUser(identityManager, "johny");
    }

}
