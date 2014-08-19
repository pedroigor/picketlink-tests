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
package org.picketlink.test.authentication.web;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import org.apache.commons.httpclient.HttpStatus;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.common.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.picketlink.test.authentication.web.Resources.DEFAULT_USERNAME;
import static org.picketlink.test.authentication.web.Resources.DEFAULT_USER_PASSWD;

/**
 * @author pedroigor
 */
@RunWith(Arquillian.class)
public class BasicAuthenticationSchemeFromAjaxClientTestCase extends AbstractAuthenticationSchemeTestCase {

    @Deployment (name = "default", testable = false)
    public static Archive<?> deployDefault() {
        return create("default.war", (String) null, BasicHttpSecurityConfiguration.class);
    }

    @Test
    @OperateOnDeployment("default")
    public void testSuccessfulAuthentication() throws Exception {
        WebClient client = new WebClient();
        WebRequestSettings request = new WebRequestSettings(getProtectedResourceURL());

        prepareAjaxRequest(request);

        WebResponse response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusCode());

        String authenticateHeader = response.getResponseHeaderValue("WWW-Authenticate");

        assertNotNull(authenticateHeader);
        assertTrue(authenticateHeader.contains("Basic realm=\"Test Realm\""));

        prepareAuthenticationRequest(request, DEFAULT_USERNAME, DEFAULT_USER_PASSWD);

        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("Protected Page", response.getContentAsString());

        request.setUrl(getContextPath());
        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("Index Page", response.getContentAsString());

        request.setUrl(getProtectedResourceURL());
        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("Protected Page", response.getContentAsString());
    }

    @Test
    @OperateOnDeployment("default")
    public void testUnsuccessfulAuthentication() throws Exception {
        WebClient client = new WebClient();
        WebRequestSettings request = new WebRequestSettings(getProtectedResourceURL());

        prepareAjaxRequest(request);

        WebResponse response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusCode());

        prepareAuthenticationRequest(request, DEFAULT_USERNAME, "bad_passwd");

        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusCode());
    }

    private void prepareAuthenticationRequest(WebRequestSettings request, String john, String passwd) {
        // here we indicate that this is an ajax request
        prepareAjaxRequest(request);
        request.addAdditionalHeader("Authorization", new String("Basic " + Base64.encodeBytes(String.valueOf(john + ":" + passwd).getBytes())));
    }

    private void prepareAjaxRequest(WebRequestSettings request) {
        // here we indicate that this is an ajax request
        request.addAdditionalHeader("X-Requested-With", "XMLHttpRequest");
    }

}