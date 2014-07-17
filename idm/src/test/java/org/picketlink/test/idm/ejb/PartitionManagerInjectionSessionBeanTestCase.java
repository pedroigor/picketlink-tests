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
package org.picketlink.test.idm.ejb;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.test.AbstractJPADeploymentTestCase;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author pedroigor
 */
@RunWith(Arquillian.class)
public class PartitionManagerInjectionSessionBeanTestCase extends AbstractJPADeploymentTestCase {

    @Inject
    private SessionScopedBean sessionScopedBean;

    @Deployment
    public static WebArchive deploy() {
        return deploy(PartitionManagerInjectionSessionBeanTestCase.class, SessionScopedBean.class);
    }

    @Test
    public void testInjection() throws Exception {
        Partition partition = this.sessionScopedBean.getDefaultPartition();

        assertNotNull(partition);
        assertEquals(Realm.DEFAULT_REALM, partition.getName());
    }

}
