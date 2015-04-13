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
import com.meterware.httpunit.WebClient;
import com.meterware.httpunit.WebClientListener;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;

import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.picketlink.test.integration.federation.saml.QuickstartArchiveUtil.resolveFromFederation;

/**
 * @author Pedro Igor
 */
@RunWith (Arquillian.class)
@RunAsClient
public class SAMLForceAuthnTestCase extends AbstractFederationTestCase {

    @ArquillianResource
    @OperateOnDeployment("idp")
    private URL identityProviderUrl;

    @Deployment(name = "idp")
    public static WebArchive deployIdentityProvider() {
        WebArchive deployment = resolveFromFederation("picketlink-federation-saml-idp-basic");

        deployment.add(getIdPConfig(null, false, false, null, null, false, true), "WEB-INF/picketlink.xml");

        return deployment;
    }

    @Deployment(name = "service-provider")
    public static WebArchive deployServiceProvider() {
        WebArchive deployment = resolveFromFederation("picketlink-federation-saml-sp-redirect-basic");

        deployment.add(getSpConfig(null, "http://localhost:8080/employee/", false, false, true), "WEB-INF/picketlink.xml");

        return deployment;
    }

    @Test
    @OperateOnDeployment("service-provider")
    public void testAuthentication(@ArquillianResource URL url) throws Exception {
        WebConversation conversation = createWebConversation();
        HttpUnitOptions.setLoggingHttpHeaders(true);
        WebRequest request = new GetMethodWebRequest(formatUrl(url));
        WebResponse response = conversation.getResponse(request);

        assertEquals(1, response.getForms().length);

        WebForm webForm = response.getForms()[0];

        webForm.setParameter("j_username", "tomcat");
        webForm.setParameter("j_password", "tomcat");

        webForm.getSubmitButtons()[0].click();

        response = conversation.getCurrentPage();

        assertTrue(response.getText().contains("EmployeeDashboard"));

        request = new GetMethodWebRequest(formatUrl(this.identityProviderUrl));
        response = conversation.getResponse(request);

        assertEquals(1, response.getForms().length);
    }

    protected WebConversation createWebConversation() {
        WebConversation conversation = new WebConversation();

        conversation.addClientListener(new WebClientListener() {
            @Override
            public void requestSent(WebClient src, WebRequest req) {
            }

            @Override
            public void responseReceived(WebClient src, WebResponse resp) {
                ResponseType responseType = getResponseType(resp);

                if (responseType != null) {
                    List<ResponseType.RTChoiceType> assertions = responseType.getAssertions();

                    assertNotNull(assertions);
                    assertEquals(1, assertions.size());

                    ResponseType.RTChoiceType rtChoiceType = assertions.get(0);

                    assertNotNull(rtChoiceType);

                    AssertionType assertion = rtChoiceType.getAssertion();

                    assertNotNull(assertion);

                    NameIDType subjectName = (NameIDType) assertion.getSubject().getSubType().getBaseID();

                    assertEquals("tomcat", subjectName.getValue());

                }
            }
        });

        return conversation;
    }
}
