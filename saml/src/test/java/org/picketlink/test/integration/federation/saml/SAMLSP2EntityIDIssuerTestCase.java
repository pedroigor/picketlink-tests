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

import com.meterware.httpunit.*;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.identity.federation.saml.v2.assertion.*;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;

import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;
import static org.picketlink.test.integration.federation.saml.QuickstartArchiveUtil.resolveFromFederation;

/**
 * @author Pedro Igor
 */
@RunWith (Arquillian.class)
@RunAsClient
public class SAMLSP2EntityIDIssuerTestCase extends AbstractFederationTestCase {

    @Deployment(name = "idp")
    public static WebArchive deployIdentityProvider() {
        WebArchive deployment = resolveFromFederation("picketlink-federation-saml-idp-basic");

        deployment.add(getIdPConfig(null, false, false, "localhost,127.0.0.1,urn:samltest:picketlink-wildfly8", null, false, false), "WEB-INF/picketlink.xml");

        return deployment;
    }

    @Deployment(name = "service-provider")
    public static WebArchive deployServiceProvider() {
        WebArchive deployment = resolveFromFederation("picketlink-federation-saml-sp-redirect-basic");

        deployment.add(getSpConfig("urn:samltest:picketlink-wildfly8", null, "http://localhost:8080/employee/", false, false, false, "REDIRECT"), "WEB-INF/picketlink.xml");

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

                    SubjectType subject = assertion.getSubject();
                    NameIDType subjectName = (NameIDType) subject.getSubType().getBaseID();

                    assertEquals("tomcat", subjectName.getValue());

                    for (ConditionAbstractType conditionAbstractType : assertion.getConditions().getConditions()) {
                        assertTrue(AudienceRestrictionType.class.isInstance(conditionAbstractType));

                        AudienceRestrictionType audienceRestrictionType = (AudienceRestrictionType) conditionAbstractType;

                        assertEquals("urn:samltest:picketlink-wildfly8", audienceRestrictionType.getAudience().get(0).toString());
                    }
                }
            }
        });

        return conversation;
    }
}
