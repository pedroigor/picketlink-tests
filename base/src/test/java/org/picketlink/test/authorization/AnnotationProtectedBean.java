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
package org.picketlink.test.authorization;

import org.picketlink.authorization.annotations.GroupsAllowed;
import org.picketlink.authorization.annotations.LoggedIn;
import org.picketlink.authorization.annotations.PartitionsAllowed;
import org.picketlink.authorization.annotations.RequiresPermission;
import org.picketlink.authorization.annotations.RolesAllowed;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Tier;
import org.picketlink.test.authorization.permission.AnotherEntity;
import org.picketlink.test.authorization.permission.SomeEntity;

/**
 * @author Pedro Igor
 */
public class AnnotationProtectedBean {

    @LoggedIn
    public void protectedFromUnauthenticatedUsers(){

    }

    @RequiresPermission(resource = "profile", operation = "read")
    public void protectedWithResourcePermission() {

    }

    @RequiresPermission(resource = "profile", operation = "write")
    public void protectedWithResourceWithoutPermission() {

    }

    @RequiresPermission(resourceClass = SomeEntity.class, operation = "create")
    public void protectedWithResourceClassPermission() {

    }

    @RequiresPermission(resourceClass = SomeEntity.class, operation = "delete")
    public void protectedWithoutResourceClassPermission() {

    }

    @RequiresPermission(resourceClass = SomeEntity.class, resourceIdentifier = "1", operation = "write")
    public void protectedWithResourceClassIdentifierPermission() {

    }

    @RequiresPermission(resourceClass = SomeEntity.class, resourceIdentifier = "2", operation = "create")
    public void protectedWithoutResourceClassIdentifierPermission() {

    }

    @RequiresPermission(resourceClass = AnotherEntity.class, operation = "load")
    public void protectedWithResourceClassPermissionAnotherEntity() {

    }

    @RequiresPermission(resourceClass = AnotherEntity.class, operation = "write")
    public void protectedWithoutResourceClassPermissionAnotherEntity() {

    }

    @RequiresPermission(resourceClass = AnotherEntity.class, resourceIdentifier = "1", operation = "delete")
    public void protectedWithResourceClassIdentifierPermissionAnotherEntity() {

    }

    @RequiresPermission(resourceClass = AnotherEntity.class, resourceIdentifier = "2", operation = "load")
    public void protectedWithoutResourceClassIdentifierPermissionAnotherEntity() {

    }

    @RolesAllowed("Tester")
    public void protectedWithRequiredRole() {

    }

    @RolesAllowed("Invalid Role")
    public void protectedWithRequiredInvalidRole() {

    }

    @RolesAllowed("Inherited Role")
    public void protectedWithInheritedRole() {

    }

    @GroupsAllowed("QA")
    public void protectedWithRequiredGroup() {

    }

    @GroupsAllowed("Another QA")
    public void protectedWithRequiredInvalidGroup() {

    }

    @GroupsAllowed("QA")
    @RolesAllowed("Tester")
    public void protectedWithRequiredMemberAndRole() {

    }

    @GroupsAllowed("QA")
    @RolesAllowed("Invalid Tester")
    public void protectedWithRequiredMemberAndInvalidRole() {

    }

    @PartitionsAllowed(name = "default")
    public void protectedWithRequiredPartitionName() {

    }

    @PartitionsAllowed(name = "invalid partition")
    public void protectedWithInvalidPartitionName() {

    }

    @PartitionsAllowed(type = Realm.class)
    public void protectedWithRequiredPartitionType() {

    }

    @PartitionsAllowed(type = Tier.class)
    public void protectedWithInvalidPartitionType() {

    }

    @PartitionsAllowed(type = Realm.class, name = "default")
    public void protectedWithRequiredPartitionTypeAndName() {

    }
}
