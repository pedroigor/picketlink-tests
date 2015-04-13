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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.test.integration.federation.saml.util.CustomAttributeManager;
import org.picketlink.test.integration.federation.saml.util.SAMLTracer;
import org.picketlink.test.integration.federation.saml.util.WriteSAMLAttributesServlet;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.picketlink.test.integration.federation.saml.QuickstartArchiveUtil.resolveFromFederation;
import static org.picketlink.test.integration.federation.saml.util.SAMLTracer.SAMLMessage;

/**
 * @author Pedro Igor
 */
@RunWith (Arquillian.class)
@RunAsClient
public class CustomAttributeTestCase extends AbstractFederationTestCase {

    @Deployment(name = "idp")
    public static WebArchive deployInvalidTrustDomainIdP() {
        WebArchive deployment = resolveFromFederation("picketlink-federation-saml-idp-basic");

        deployment.add(getIdPConfig(null, false, false, null, CustomAttributeManager.class, false, true), "WEB-INF/picketlink.xml");
        deployment.addClass(CustomAttributeManager.class);
        deployment.addClass(WriteSAMLAttributesServlet.class);

        return deployment;
    }

    @Deployment(name = "service-provider")
    public static WebArchive deployEmployee() {
        return resolveFromFederation("picketlink-federation-saml-sp-post-basic");
    }

    @Test
    @OperateOnDeployment("service-provider")
    public void testRedirectOriginalRequest(@ArquillianResource URL url) throws Exception {
        WebRequest request = new GetMethodWebRequest(formatUrl(url));
        WebConversation conversation = createWebConversation();
        WebResponse response = conversation.getResponse(request);

        WebForm webForm = response.getForms()[0];

        webForm.setParameter("j_username", "tomcat");
        webForm.setParameter("j_password", "tomcat");

        webForm.getSubmitButtons()[0].click();

        SAMLTracer samlTracer = getSamlTracer();

        SAMLMessage samlMessage = samlTracer.getMessages().get(1);
        ResponseType responseType = (ResponseType) samlMessage.getSamlObject();

        assertEquals(1, responseType.getAssertions().size());

        AssertionType assertion = responseType.getAssertions().get(0).getAssertion();
        Map<String, AttributeType> attributes = new HashMap<String, AttributeType>();

        for (AttributeStatementType attributeStatementType : assertion.getAttributeStatements()) {
            for (AttributeStatementType.ASTChoiceType choiceType : attributeStatementType.getAttributes()) {
                AttributeType attribute = choiceType.getAttribute();

                attributes.put(attribute.getName(), attribute);
            }
        }

        AttributeType attribute1 = attributes.get("attribute1");

        assertNotNull(attribute1);
        assertTrue(attribute1.getAttributeValue().contains("attributeValue1"));

        AttributeType attribute2 = attributes.get("attribute2");

        assertNotNull(attribute2);
        assertTrue(attribute2.getAttributeValue().contains("attributeValue2"));

        AttributeType attribute3 = attributes.get("attribute3");

        assertNotNull(attribute3);
        assertTrue(attribute3.getAttributeValue().contains("attributeValue3"));

        AttributeType attribute4 = attributes.get("attribute4");

        assertNotNull(attribute4);
        assertEquals("customNameFormat", attribute4.getNameFormat());
        assertTrue(attribute4.getAttributeValue().contains("value1"));
        assertTrue(attribute4.getAttributeValue().contains("value2"));
        assertTrue(attribute4.getAttributeValue().contains("value3"));
        assertTrue(attribute4.getAttributeValue().contains("value4"));
    }
}
