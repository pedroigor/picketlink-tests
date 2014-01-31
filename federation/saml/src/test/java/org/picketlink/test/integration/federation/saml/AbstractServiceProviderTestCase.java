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
public abstract class AbstractServiceProviderTestCase {

    @Test
    @OperateOnDeployment("service-provider")
    public void testAuthentication(@ArquillianResource URL url) throws Exception {
        WebConversation conversation = new WebConversation();
        HttpUnitOptions.setLoggingHttpHeaders(true);
        WebRequest request = new GetMethodWebRequest(formatUrl(url));
        WebResponse response = conversation.getResponse(request);

        assertTrue(response.getURL().getPath().startsWith(getIdPContextPath()));
        assertEquals(1, response.getForms().length);

        WebForm webForm = response.getForms()[0];

        webForm.setParameter("j_username", "tomcat");
        webForm.setParameter("j_password", "tomcat");

        webForm.getSubmitButtons()[0].click();

        response = conversation.getCurrentPage();

        doAssertAuthentication(response);
    }

    @Test
    @OperateOnDeployment("service-provider")
    public void testLogout(@ArquillianResource URL url) throws Exception {
        WebRequest request = new GetMethodWebRequest(formatUrl(url));
        WebConversation conversation = new WebConversation();
        WebResponse response = conversation.getResponse(request);

        assertTrue(response.getURL().getPath().startsWith(getIdPContextPath()));
        assertEquals(1, response.getForms().length);

        WebForm webForm = response.getForms()[0];

        webForm.setParameter("j_username", "tomcat");
        webForm.setParameter("j_password", "tomcat");

        webForm.getSubmitButtons()[0].click();

        response = conversation.getCurrentPage();

        doAssertAuthentication(response);

        for (WebLink link: response.getLinks()) {
            if (link.getURLString().contains("GLO=true")) {
                link.click();
            }
        }

        response = conversation.getResponse(conversation.getCurrentPage().getRefreshRequest());

        assertTrue(response.getURL().getPath().startsWith(getIdPContextPath()));
        assertEquals(1, response.getForms().length);
    }

    protected abstract String getIdPContextPath();
    protected abstract void doAssertAuthentication(WebResponse response);

    /**
     * <p>Make sure the URL's host is always localhost if original host is 127.0.0.1. Necessary when running the tests against Undertow as
     * it does not properly returns the welcome page for SPs after a successful authentication.</p>
     *
     * @param url
     * @return
     */
    private String formatUrl(URL url) {
        String stringUrl = url.toString();

        if (stringUrl.contains("127.0.0.1")) {
            return stringUrl.replace("127.0.0.1", "localhost");
        }

        return stringUrl;
    }
}
