/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package ru.spb.iac.cud.idp.web.util;

import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.util.StringUtil;
import org.picketlink.identity.federation.core.constants.AttributeConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.X500SAMLProfileConstants;
import org.picketlink.identity.federation.core.saml.v2.util.StatementUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.AuthnContextClassRefType;
import org.picketlink.identity.federation.saml.v2.assertion.AuthnContextType;
import org.picketlink.identity.federation.saml.v2.assertion.AuthnContextType.AuthnContextTypeSequence;
import org.picketlink.identity.federation.saml.v2.assertion.AuthnStatementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CUDStatementUtil extends StatementUtil {

	final static Logger logger = LoggerFactory
			.getLogger(CUDStatementUtil.class);

	public static AttributeStatementType createAttributeStatementForRoles(
			List<String> roles, boolean multivalued) {
		if (multivalued == false) {
			return createAttributeStatement(roles);
		}
		AttributeStatementType attrStatement = new AttributeStatementType();
		// AttributeType attr = new
		// AttributeType(AttributeConstants.ROLE_IDENTIFIER_ASSERTION);
		AttributeType attr = new AttributeType("USER_ROLES");
		for (String role : roles) {
			attr.addAttributeValue(role);
		}
		attrStatement.addAttribute(new ASTChoiceType(attr));
		return attrStatement;
	}

	public static AttributeStatementType createAttributeStatementForResources(
			List<String> resources, boolean multivalued) {
		if (multivalued == false) {
			return createAttributeStatement(resources);
		}

		AttributeStatementType attrStatement = new AttributeStatementType();
		// AttributeType attr = new
		// AttributeType(AttributeConstants.ROLE_IDENTIFIER_ASSERTION);
		AttributeType attr = new AttributeType("RESOURCES");
		for (String role : resources) {
			attr.addAttributeValue(role);
		}
		attrStatement.addAttribute(new ASTChoiceType(attr));

		logger.info("createAttributeStatementForResources:03");

		return attrStatement;
	}

}