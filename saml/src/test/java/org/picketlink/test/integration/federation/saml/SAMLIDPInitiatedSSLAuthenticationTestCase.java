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
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.junit.Assert.assertTrue;
import static org.picketlink.test.integration.federation.saml.QuickstartArchiveUtil.resolveFromFederation;

/**
 * @author Pedro Igor
 */
@RunWith (Arquillian.class)
@RunAsClient
public class SAMLIDPInitiatedSSLAuthenticationTestCase extends AbstractFederationTestCase {

    @Deployment(name = "identity-provider")
    public static WebArchive deployIdentityProvider() {
        WebArchive deployment = resolveFromFederation("picketlink-federation-saml-idp-with-ssl");

        deployment.add(getIdPJBossWeb("idp-ssl", "idp-ssl"), "WEB-INF/jboss-web.xml");

        return deployment;
    }

    @Test
    @OperateOnDeployment("identity-provider")
    public void testIdPInitiatedSSO() throws Exception {
        KeyStore keyStore = getKeyStore(System.getProperty("jboss.config.dir") + "/client.keystore", "PKCS12");
        KeyStore trustStore = getKeyStore(System.getProperty("jboss.config.dir") + "/client.truststore", KeyStore.getDefaultType());

        SSLContext sslcontext = SSLContexts.custom()
            .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
            .loadKeyMaterial(keyStore, "change_it".toCharArray())
            .build();

        SSLContext.setDefault(sslcontext);

        WebRequest request = new GetMethodWebRequest("https://localhost:8443/idp-ssl");

        WebConversation conversation = new WebConversation();

        WebResponse response = conversation.getResponse(request);

        assertTrue(response.getText().contains("SAML 2.0 IdP-Initiated SSO"));
    }

    private KeyStore getKeyStore(String trustStorePath, String type) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore trustStore  = KeyStore.getInstance(type);
        FileInputStream instream = new FileInputStream(new File(trustStorePath));

        try {
            trustStore.load(instream, "change_it".toCharArray());
        } finally {
            instream.close();
        }
        return trustStore;
    }
}
