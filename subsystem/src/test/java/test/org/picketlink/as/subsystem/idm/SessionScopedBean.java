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

import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Realm;

import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

/**
 * @author Pedro Igor
 */
@SessionScoped
public class SessionScopedBean implements Serializable {

    @Resource(mappedName = "picketlink/JPADSBasedPartitionManager")
    private PartitionManager partitionManager;

    public Partition getDefaultPartition() {
        return this.partitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM);
    }

    public void createDefaultPartition() {
        this.partitionManager.add(new Realm(Realm.DEFAULT_REALM));
    }
}
