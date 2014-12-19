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
package org.picketlink.test.integration.federation.saml;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Pedro Igor
 */
public abstract class AbstractServiceProviderTestCase extends AbstractFederationTestCase {

    @ArquillianResource
    @OperateOnDeployment("idp")
    private URL idpUrl;

    @Test
    @OperateOnDeployment("service-provider")
    public void testAuthentication(@ArquillianResource URL serviceProviderUrl) throws Exception {
        WebConversation conversation = createWebConversation();
        HttpUnitOptions.setLoggingHttpHeaders(true);
        WebRequest request = new GetMethodWebRequest(formatUrl(serviceProviderUrl));
        WebResponse response = conversation.getResponse(request);

        assertTrue(response.getURL().getPath().startsWith(getIdPContextPath()));
        assertEquals(1, response.getForms().length);

        WebForm webForm = response.getForms()[0];

        webForm.setParameter("j_username", "tomcat");
        webForm.setParameter("j_password", "tomcat");

        webForm.getSubmitButtons()[0].click();

        response = conversation.getCurrentPage();

        doAssertAuthentication(response, serviceProviderUrl);
    }

    @Test
    @OperateOnDeployment("service-provider")
    public void testLogout(@ArquillianResource URL serviceProviderUrl) throws Exception {
        WebRequest request = new GetMethodWebRequest(formatUrl(serviceProviderUrl));
        WebConversation conversation = createWebConversation();
        WebResponse response = conversation.getResponse(request);

        assertTrue(response.getURL().getPath().startsWith(getIdPContextPath()));
        assertEquals(1, response.getForms().length);

        WebForm webForm = response.getForms()[0];

        webForm.setParameter("j_username", "tomcat");
        webForm.setParameter("j_password", "tomcat");

        webForm.getSubmitButtons()[0].click();

        response = conversation.getCurrentPage();

        doAssertAuthentication(response, serviceProviderUrl);

        for (WebLink link: response.getLinks()) {
            if (link.getURLString().contains("GLO=true")) {
                link.click();
            }
        }

        response = conversation.getResponse(conversation.getCurrentPage().getRefreshRequest());

        assertTrue(response.getURL().getPath().startsWith(getIdPContextPath()));
        assertEquals(1, response.getForms().length);

        doAssertLogout(response, serviceProviderUrl);
    }

    protected void doAssertLogout(WebResponse response, URL serviceProviderUrl) {

    }

    protected String getIdPContextPath() {
        return this.idpUrl.getPath();
    }

    protected abstract void doAssertAuthentication(WebResponse response, URL url) throws Exception;

    public URL getIdpUrl() {
        return this.idpUrl;
    }
}