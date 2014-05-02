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

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * @author Pedro Igor
 */
public class QuickstartArchiveUtil {

    public static <T extends Archive> T resolveFromFederation(String artifactId) {
        return (T) Maven.resolver().resolve("org.picketlink.quickstarts:" + artifactId + ":war:" + getFederationBindingClassifier() + ":" + getVersion())
                   .withoutTransitivity()
                   .asSingle(WebArchive.class);
    }

    private static String getFederationBindingClassifier() {
        if (isWildFlyContainer()) {
            return "wildfly";
        }

        return "jboss-eap";
    }

    public static boolean isWildFlyContainer() {
        String arquillianLunch = System.getProperty("arquillian.launch");

        if (arquillianLunch != null) {
            if (arquillianLunch.startsWith("jboss-eap-")) {
                return false;
            }
        }

        return true;
    }

    private static String getVersion() {
        return System.getProperty("project.version", "2.6.0-SNAPSHOT");
    }
}
