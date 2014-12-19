/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import org.apache.http.HttpStatus;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.common.util.Base64;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Pedro Igor
 */
@RunWith(Arquillian.class)
@RunAsClient
@Ignore("WFLY-4185")
public class ServletListenerFromModuleTestCase {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap
                .create(WebArchive.class, "test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource(new StringAsset("<jboss-deployment-structure>\n"
                        + "    <deployment>\n"
                        + "        <dependencies>\n"
                        + "            <module name=\"org.picketlink.idm\" meta-inf=\"import\"/>\n"
                        + "            <module name=\"org.picketlink.core\" meta-inf=\"import\" annotations=\"true\" />\n"
                        + "            <module name=\"org.picketlink.core.api\" meta-inf=\"import\"/>\n"
                        + "            <module name=\"org.picketlink.idm.api\" meta-inf=\"import\"/>\n"
                        + "            <module name=\"org.picketlink.idm.schema\" />\n"
                        + "        </dependencies>\n"
                        + "    </deployment>\n"
                        + "</jboss-deployment-structure>"), "jboss-deployment-structure.xml")
                .addClass(ServletListenerFromModuleTestCase.class)
                .addClass(HttpSecurityConfiguration.class)
                .addClass(SecurityInitializer.class)
                .addClass(ProtectedServlet.class);
    }

    @Test
    public void testBasicAuthentication(@ArquillianResource URL deploymentUrl) throws Exception {
        WebClient client = new WebClient();
        WebRequestSettings request = new WebRequestSettings(deploymentUrl);
        WebResponse response = client.loadWebResponse(request);

        assertEquals(401, response.getStatusCode());

        String authenticateHeader = response.getResponseHeaderValue("WWW-Authenticate");

        assertNotNull(authenticateHeader);
        assertTrue(authenticateHeader.contains("Basic realm=\"PicketLink HTTP Basic Quickstart Realm\""));

        request.addAdditionalHeader("Authorization",
                new String("Basic " + Base64.encodeBytes(String.valueOf("jane:abcd1234").getBytes())));

        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("Authenticated user is: jane", response.getContentAsString());
    }
}
