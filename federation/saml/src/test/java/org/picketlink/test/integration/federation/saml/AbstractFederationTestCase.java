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
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.impl.base.io.IOUtil;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.core.impl.EmptyAttributeManager;
import org.picketlink.identity.federation.core.interfaces.AttributeManager;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;

import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.fail;

/**
 * @author Pedro Igor
 */
public abstract class AbstractFederationTestCase {

    /**
     * <p>Make sure the URL's host is always localhost if original host is 127.0.0.1. Necessary when running the tests against Undertow as
     * it does not properly returns the welcome page for SPs after a successful authentication.</p>
     *
     * @param url
     * @return
     */
    protected String formatUrl(URL url) {
        String stringUrl = url.toString();

        if (stringUrl.contains("127.0.0.1")) {
            return stringUrl.replace("127.0.0.1", "localhost");
        }

        return stringUrl;
    }

    protected ResponseType getResponseType(WebResponse response) {
        String query = response.getURL().getQuery();

        if (query != null && query.contains(GeneralConstants.SAML_RESPONSE_KEY)) {
            String samlResponse = query.substring(query.indexOf("=") + 1);

            try {
                return (ResponseType) new SAML2Response()
                    .getSAML2ObjectFromStream(RedirectBindingUtil.urlBase64DeflateDecode(samlResponse));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        return null;
    }

    protected static StringAsset getIdPConfig(String identityUrl, boolean supportSignatures,
        boolean supportEncryption,
        String trustedDomains,
        Class<? extends AttributeManager> attributeManager) {
        InputStream inputStream = IDPAuthenticationFailedTestCase.class.getResourceAsStream("/config/picketlink-template.xml");
        String config = new String(IOUtil.asByteArray(inputStream));

        if (trustedDomains == null) {
            trustedDomains = "localhost, 127.0.0.1";
        }

        if (attributeManager == null) {
            attributeManager = EmptyAttributeManager.class;
        }

        if (identityUrl == null) {
            identityUrl = "http://localhost:8080/idp";
        }

        config = config.replace("${identity-url}", identityUrl);
        config = config.replace("${trust-domains}", trustedDomains);
        config = config.replace("${support-signatures}", Boolean.valueOf(supportSignatures).toString());
        config = config.replace("${support-encryption}", Boolean.valueOf(supportEncryption).toString());
        config = config.replace("${attribute-manager}", attributeManager.getName());

        return new StringAsset(config);
    }

}
