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
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationDataType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.test.integration.federation.saml.util.SAMLTracer;

import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.picketlink.test.integration.federation.saml.QuickstartArchiveUtil.resolveFromFederation;

/**
 * @author Pedro Igor
 */
@RunWith (Arquillian.class)
@RunAsClient
public class SAML2IDPInitiatedTestCase extends AbstractSAML2IDPInitiatedTestCase {

    @ArquillianResource
    @OperateOnDeployment("service-provider")
    private URL serviceProviderPostURL;

    @Deployment(name = "idp")
    public static WebArchive deployIdentityProvider() {
        WebArchive deployment = resolveFromFederation("picketlink-federation-saml-idp-basic");

        deployment.add(getIdPConfig(null, false, false, null, null, false, false), "WEB-INF/picketlink.xml");

        return deployment;
    }

    @Deployment(name = "service-provider")
    public static WebArchive deployServiceProvider() {
        WebArchive serviceProvider = resolveFromFederation("picketlink-federation-saml-sp-post-basic");

        serviceProvider.add(new StringAsset("Back to the original requested resource."), "savedRequest/savedRequest.jsp");

        return serviceProvider;
    }

    @Test
    @OperateOnDeployment("idp")
    public void testPostOriginalRequest(@ArquillianResource URL url) throws Exception {
        WebRequest request = new GetMethodWebRequest(formatUrl(url));
        WebConversation conversation = createWebConversation();
        WebResponse response = conversation.getResponse(request);

        WebForm webForm = response.getForms()[0];

        webForm.setParameter("j_username", "tomcat");
        webForm.setParameter("j_password", "tomcat");

        webForm.getSubmitButtons()[0].click();

        request = new GetMethodWebRequest(formatUrl(url) + "?SAML_VERSION=2.0&TARGET=" + formatUrl(this.serviceProviderPostURL) + "savedRequest/savedRequest.jsp&SAML_BINDING=POST");

        response = conversation.getResponse(request);

        assertTrue(response.getText().contains("Back to the original requested resource."));

        SAMLTracer samlTracer = getSamlTracer();
        List<SAMLTracer.SAMLMessage> messages = samlTracer.getMessages();
        SAMLTracer.SAMLMessage samlMessage = messages.get(messages.size() - 1);
        SAML2Object samlObject = samlMessage.getSamlObject();

        assertTrue(ResponseType.class.isInstance(samlObject));

        ResponseType responseType = (ResponseType) samlObject;

        assertNull(responseType.getInResponseTo());

        ResponseType.RTChoiceType rtChoiceType = responseType.getAssertions().get(0);
        AssertionType assertionType = rtChoiceType.getAssertion();
        SubjectType subject = assertionType.getSubject();

        for (SubjectConfirmationType confirmationType : subject.getConfirmation()) {
            SubjectConfirmationDataType subjectConfirmationData = confirmationType.getSubjectConfirmationData();

            if (subjectConfirmationData != null) {
                assertNull(subjectConfirmationData.getInResponseTo());
            }
        }
    }

    @Test
    @OperateOnDeployment("idp")
    public void testRedirectOriginalRequest(@ArquillianResource URL url) throws Exception {
        WebRequest request = new GetMethodWebRequest(formatUrl(url));
        WebConversation conversation = new WebConversation();
        WebResponse response = conversation.getResponse(request);

        WebForm webForm = response.getForms()[0];

        webForm.setParameter("j_username", "tomcat");
        webForm.setParameter("j_password", "tomcat");

        webForm.getSubmitButtons()[0].click();

        request = new GetMethodWebRequest(formatUrl(url) + "?SAML_VERSION=2.0&TARGET=" + formatUrl(this.serviceProviderPostURL) + "savedRequest/savedRequest.jsp&SAML_BINDING=REDIRECT");

        response = conversation.getResponse(request);

        assertTrue(response.getText().contains("Back to the original requested resource."));
        assertTrue(response.getURL().toString().startsWith(formatUrl(this.serviceProviderPostURL) + "savedRequest/savedRequest.jsp?SAMLResponse="));
    }
}
