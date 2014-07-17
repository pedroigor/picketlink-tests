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
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.interfaces.AttributeManager;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusCodeType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusType;

import java.net.URL;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.picketlink.test.integration.federation.saml.QuickstartArchiveUtil.resolveFromFederation;

/**
 * @author Pedro Igor
 */
@RunWith (Arquillian.class)
@RunAsClient
public class IDPAuthenticationFailedTestCase extends AbstractFederationTestCase {

    @Deployment(name = "idp-authn-failed")
    public static WebArchive deployAuthnFailedIdP() {
        WebArchive deployment = resolveFromFederation("picketlink-federation-saml-idp-basic");

        deployment.add(getIdPConfig(null, false, false, null, BadAttributeManager.class, false), "WEB-INF/picketlink.xml");
        deployment.addClass(BadAttributeManager.class);
        deployment.addClass(IDPAuthenticationFailedTestCase.class);
        deployment.addClass(AbstractFederationTestCase.class);

        return deployment;
    }

    @Deployment(name = "service-provider")
    public static WebArchive deployEmployee() {
        return resolveFromFederation("picketlink-federation-saml-sp-redirect-basic");
    }

    @Test
    @OperateOnDeployment("service-provider")
    public void testRedirectOriginalRequest(final @ArquillianResource URL url) throws Exception {
        WebRequest request = new GetMethodWebRequest(formatUrl(url) + "/savedRequest/savedRequest.html");
        WebConversation conversation = new WebConversation();

        conversation.addClientListener(new WebClientListener() {
            @Override
            public void requestSent(WebClient src, WebRequest req) {
            }

            @Override
            public void responseReceived(WebClient src, WebResponse resp) {
                ResponseType responseType = getResponseType(resp);

                if (responseType != null) {
                    StatusType status = responseType.getStatus();

                    assertNotNull(status);

                    StatusCodeType topLevelCode = status.getStatusCode();

                    assertNotNull(topLevelCode);
                    assertEquals(JBossSAMLURIConstants.STATUS_RESPONDER.get(), topLevelCode.getValue().toString());

                    StatusCodeType secondLevelCode = topLevelCode.getStatusCode();

                    assertNotNull(secondLevelCode);
                    assertEquals(JBossSAMLURIConstants.STATUS_AUTHNFAILED.get(), secondLevelCode.getValue().toString());
                }
            }
        });

        WebResponse response = conversation.getResponse(request);

        WebForm webForm = response.getForms()[0];

        webForm.setParameter("j_username", "tomcat");
        webForm.setParameter("j_password", "tomcat");

        webForm.getSubmitButtons()[0].click();

        response = conversation.getCurrentPage();

        assertTrue(response.getText().contains("The Identity Provider could not process the authentication request."));
    }

    public static class BadAttributeManager implements AttributeManager {

        @Override
        public Map<String, Object> getAttributes(Principal userPrincipal, List<String> attributeKeys) {
            throw new IllegalStateException("Bad AttributeManager !");
        }
    }
}
