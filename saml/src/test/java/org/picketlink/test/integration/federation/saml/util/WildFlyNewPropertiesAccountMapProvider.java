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

import org.picketlink.identity.federation.web.config.IdentityURLConfigurationProvider;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author pedroigor
 */
public class WildFlyNewPropertiesAccountMapProvider implements IdentityURLConfigurationProvider {

    public static final String WEB_INF_PROP_FILE_NAME = "/WEB-INF/newidpmap.properties";

    private ServletContext servletContext = null;


    @Override
    public void setClassLoader(ClassLoader classLoader) {}

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public Map<String, String> getIDPMap() throws IOException {
        Map<String, String> idpMap = new HashMap<String, String>();
        InputStream inputStream = null;
        Properties properties = new Properties();

        if (this.servletContext != null) {
            inputStream = this.servletContext.getResourceAsStream(WEB_INF_PROP_FILE_NAME);
        }

        if (inputStream != null) {
            properties.load(inputStream);
            Set<Object> keyset = properties.keySet();
            for (Object key : keyset) {
                idpMap.put((String) key, (String) properties.get(key));
            }
        }

        return idpMap;
    }
}
