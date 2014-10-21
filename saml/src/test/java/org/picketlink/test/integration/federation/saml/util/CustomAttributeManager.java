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

import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2AttributeManager;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.picketlink.identity.federation.core.saml.v2.util.StatementUtil.createAttributeStatement;

/**
 * @author Pedro Igor
 */
public class CustomAttributeManager implements SAML2AttributeManager {

    @Override
    public Set<AttributeStatementType> getAttributes(AuthnRequestType authnRequestType, Principal userPrincipal) {
        Set<AttributeStatementType> attributeStatementTypes = new HashSet<AttributeStatementType>();

        attributeStatementTypes.add(createAttributeStatement("attribute1", "attributeValue1"));
        attributeStatementTypes.add(createAttributeStatement("attribute2", "attributeValue2"));
        attributeStatementTypes.add(createAttributeStatement("attribute3", "attributeValue3"));

        AttributeStatementType attrStatement = new AttributeStatementType();
        AttributeType attr = new AttributeType("attribute4");
        attr.setNameFormat("customNameFormat");
        attr.addAttributeValue("value1");
        attr.addAttributeValue("value2");
        attr.addAttributeValue("value3");
        attr.addAttributeValue("value4");
        attrStatement.addAttribute(new AttributeStatementType.ASTChoiceType(attr));

        attributeStatementTypes.add(attrStatement);

        return attributeStatementTypes;
    }

    @Override
    public Map<String, Object> getAttributes(Principal userPrincipal, List<String> attributeKeys) {
        return null;
    }
}
