/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.test.integration.federation.saml.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.jboss.as.network.NetworkUtils;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pedroigor
 */
public class JBoss7Util {

    /**
     * Generates content of the jboss-deployment-structure.xml deployment descriptor as an ShrinkWrap asset. It fills the given
     * dependencies (module names) into it.
     *
     * @param dependencies AS module names
     * @return
     */
    public static Asset getJBossDeploymentStructure(String... dependencies) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<jboss-deployment-structure><deployment><dependencies>");
        if (dependencies != null) {
            for (String moduleName : dependencies) {
                sb.append("\n\t<module name='").append(moduleName).append("'").append(" ").append("services=\"import\"").append("/>");
            }
        }
        sb.append("\n</dependencies></deployment></jboss-deployment-structure>");
        return new StringAsset(sb.toString());
    }

    /**
     * Generates content of jboss-web.xml file as an ShrinkWrap asset with the given security domain name and given valve class.
     *
     * @param securityDomain security domain name (not-<code>null</code>)
     * @param valveClassNames valve class (e.g. an Authenticator) which should be added to jboss-web file (may be <code>null</code>)
     * @return Asset instance
     */
    public static Asset getJBossWebXmlAsset(final String securityDomain, final String... valveClassNames) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<jboss-web>");
        sb.append("\n\t<security-domain>").append(securityDomain).append("</security-domain>");
        if (valveClassNames != null) {
            for (String valveClassName : valveClassNames) {
                if (valveClassName != null && !valveClassName.isEmpty()) {
                    sb.append("\n\t<valve><class-name>").append(valveClassName).append("</class-name></valve>");
                }
            }
        }
        sb.append("\n</jboss-web>");
        return new StringAsset(sb.toString());
    }

    /**
     * Replace variables in PicketLink configurations files with given values and set ${hostname} variable from system property:
     * node0
     *
     * @param stream Stream to perform replacement on. The stream is expected to be a text file in UTF-8 encoding
     * @param deploymentName Value of property <code>deployment</code> in replacement
     * @param bindingType Value of property <code>bindingType</code> in replacement
     * @param idpContextPath Value of property <code>idpContextPath</code> in replacement
     * @return Contents of the input stream with replaced values
     */
    public static String propertiesReplacer(InputStream stream, String deploymentName, String bindingType, String idpContextPath) {

        String hostname = getHostname();

        final Map<String, String> map = new HashMap<String, String>();
        String content = "";
        map.put("hostname", hostname);
        map.put("deployment", deploymentName);
        map.put("bindingType", bindingType);
        map.put("idpContextPath", idpContextPath);

        try {
            content = StrSubstitutor.replace(IOUtils.toString(stream, "UTF-8"), map);
        } catch (IOException ex) {
            String message = "Cannot find or modify input stream, error: " + ex.getMessage();
            throw new RuntimeException(ex);
        }
        return content;
    }

    /**
     * Set ${hostname} variable from system property: node0
     *
     * @return Value of hostname
     */
    public static String getHostname() {
        String hostname = System.getProperty("node0");

        //expand possible IPv6 address
        try {
            hostname = NetworkUtils.formatPossibleIpv6Address(InetAddress.getByName(hostname).getHostAddress());
        } catch (UnknownHostException ex) {
            String message = "Cannot resolve host address: " + hostname + " , error : " + ex.getMessage();
            throw new RuntimeException(ex);
        }

        if ("127.0.0.1".equals(hostname)) {
            return "localhost";
        }

        return hostname;
    }

}
