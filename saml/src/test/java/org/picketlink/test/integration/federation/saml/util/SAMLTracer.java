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
package org.picketlink.test.integration.federation.saml.util;

import com.meterware.httpunit.WebRequest;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.picketlink.common.util.StringUtil.isNullOrEmpty;

/**
 * @author Pedro Igor
 */
public class SAMLTracer {

    private List<SAMLMessage> messages = new ArrayList<SAMLMessage>();

    public void addSAMLRequest(WebRequest samlRequest) {
        this.messages.add(new SAMLMessage(samlRequest));
    }

    public List<SAMLMessage> getMessages() {
        return this.messages;
    }

    public static class SAMLMessage {

        private final SAML2Object samlObject;
        private final WebRequest request;

        public SAMLMessage(WebRequest request) {
            try {
                String samlMessage = request.getParameter(GeneralConstants.SAML_REQUEST_KEY);

                if (isNullOrEmpty(samlMessage)) {
                    samlMessage = request.getParameter(GeneralConstants.SAML_RESPONSE_KEY);
                }


                if (isNullOrEmpty(samlMessage)) {
                    throw new RuntimeException("Request does not contain any SAML message.");
                }

                InputStream inputStream;

                if ("POST".equalsIgnoreCase(request.getMethod())) {
                    inputStream = PostBindingUtil.base64DecodeAsStream(samlMessage);
                } else {
                    inputStream = RedirectBindingUtil.urlBase64DeflateDecode(samlMessage);
                }

                this.samlObject = (SAML2Object) new SAMLParser().parse(inputStream);
                this.request = request;
            } catch (Exception e) {
                throw new RuntimeException("Could not parse SAML message.", e);
            }
        }

        public SAML2Object getSamlObject() {
            return this.samlObject;
        }

        public WebRequest getRequest() {
            return this.request;
        }
    }
}
