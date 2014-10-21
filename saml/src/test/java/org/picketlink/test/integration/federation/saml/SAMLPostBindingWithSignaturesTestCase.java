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

import com.meterware.httpunit.WebResponse;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.LogoutRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusResponseType;
import org.picketlink.test.integration.federation.saml.util.SAMLTracer;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.test.integration.federation.saml.QuickstartArchiveUtil.resolveFromFederation;
import static org.picketlink.test.integration.federation.saml.util.SAMLTracer.SAMLMessage;

/**
 * @author Pedro Igor
 */
@RunWith (Arquillian.class)
@RunAsClient
public class SAMLPostBindingWithSignaturesTestCase extends AbstractServiceProviderTestCase {

    @Deployment(name = "idp")
    public static WebArchive deployIdentityProvider() {
        return resolveFromFederation("picketlink-federation-saml-idp-with-signature");
    }

    @Deployment(name = "service-provider")
    public static WebArchive deployServiceProvider() {
        return resolveFromFederation("picketlink-federation-saml-sp-post-with-signature");
    }

    @Override
    protected void doAssertAuthentication(WebResponse response, URL serviceProviderUrl) {
        try {
            assertTrue(response.getText().contains("SalesTool"));
        } catch (IOException e) {
            fail();
        }

        assertSAMLMessageFlow(serviceProviderUrl);
    }

    @Override
    protected void doAssertLogout(WebResponse response, URL serviceProviderUrl) {
        SAMLTracer samlTracer = getSamlTracer();

        SAMLMessage samlMessage = samlTracer.getMessages().get(2);
        LogoutRequestType logoutRequestType = (LogoutRequestType) samlMessage.getSamlObject();

        assertEquals("2.0", logoutRequestType.getVersion());
        assertFalse(isNullOrEmpty(logoutRequestType.getID()));
        assertEquals(formatUrl(getIdpUrl()), logoutRequestType.getDestination().toString());
        assertEquals(formatUrl(serviceProviderUrl), logoutRequestType.getIssuer().getValue());
        assertNotNull(logoutRequestType.getSignature());

        samlMessage = samlTracer.getMessages().get(3);
        StatusResponseType statusResponseType = (StatusResponseType) samlMessage.getSamlObject();

        assertEquals("2.0", statusResponseType.getVersion());
        assertFalse(isNullOrEmpty(statusResponseType.getID()));
        assertEquals(formatUrl(serviceProviderUrl), statusResponseType.getDestination().toString());
        assertEquals(formatUrl(getIdpUrl()), statusResponseType.getIssuer().getValue());
        assertEquals(logoutRequestType.getID(), statusResponseType.getInResponseTo());
        assertEquals(JBossSAMLURIConstants.STATUS_SUCCESS.get(), statusResponseType.getStatus().getStatusCode().getValue().toString());
    }

    protected void assertSAMLMessageFlow(URL serviceProviderUrl) {
        SAMLTracer samlTracer = getSamlTracer();
        List<SAMLMessage> messages = samlTracer.getMessages();

        assertFalse(messages.isEmpty());
        assertEquals(2, messages.size());

        SAMLMessage authnRequest = messages.get(0);
        AuthnRequestType authnRequestType = (AuthnRequestType) authnRequest.getSamlObject();

        assertEquals("2.0", authnRequestType.getVersion());
        assertFalse(isNullOrEmpty(authnRequestType.getID()));
        assertEquals(JBossSAMLURIConstants.SAML_HTTP_POST_BINDING.get(), authnRequestType.getProtocolBinding().toString());
        assertEquals(formatUrl(serviceProviderUrl), authnRequestType.getAssertionConsumerServiceURL().toString());
        assertEquals(formatUrl(getIdpUrl()), authnRequestType.getDestination().toString());
        assertEquals(formatUrl(serviceProviderUrl), authnRequestType.getIssuer().getValue());
        assertNotNull(authnRequestType.getSignature());

        SAMLMessage samlResponse = messages.get(1);
        ResponseType responseType = (ResponseType) samlResponse.getSamlObject();

        assertEquals("2.0", responseType.getVersion());
        assertFalse(isNullOrEmpty(responseType.getID()));
        assertEquals(formatUrl(serviceProviderUrl), responseType.getDestination().toString());
        assertEquals(formatUrl(getIdpUrl()), responseType.getIssuer().getValue());
        assertEquals(authnRequestType.getID(), responseType.getInResponseTo());
        assertEquals(JBossSAMLURIConstants.STATUS_SUCCESS.get(), responseType.getStatus().getStatusCode().getValue().toString());
        assertEquals(1, responseType.getAssertions().size());
        assertNotNull(responseType.getSignature());

        ResponseType.RTChoiceType rtChoiceType = responseType.getAssertions().get(0);
        AssertionType assertionType = rtChoiceType.getAssertion();

        assertEquals("2.0", assertionType.getVersion());
        assertFalse(isNullOrEmpty(assertionType.getID()));
        assertEquals(formatUrl(getIdpUrl()), assertionType.getIssuer().getValue());
        assertEquals("tomcat", ((NameIDType) assertionType.getSubject().getSubType().getBaseID()).getValue());
        assertFalse(assertionType.getAttributeStatements().isEmpty());
    }
}
