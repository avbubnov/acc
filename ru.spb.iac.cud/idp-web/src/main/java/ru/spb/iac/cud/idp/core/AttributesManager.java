package ru.spb.iac.cud.idp.core;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap; import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.picketlink.identity.federation.core.interfaces.AttributeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spb.iac.cud.context.ContextIDPAccessManager;

public class AttributesManager implements AttributeManager {

	final static Logger LOGGER = LoggerFactory
			.getLogger(AttributesManager.class);

	private String typeAuth;

	public Map<String, Object> getAttributes(Principal principal,
			List<String> attributeKeys) {

		Map<String, Object> attributes = new HashMap<String, Object>();
		try {

			 

			Map<String, String> roles = (new ContextIDPAccessManager())
					.attributes(principal.getName());

			attributes.putAll(roles);

		} catch (Exception e) {
			LOGGER.error("getAttributes:error:", e);
		}

		return attributes;
	}

	public String getTypeAuth() {
		return typeAuth;
	}

	public void setTypeAuth(String typeAuth) {
		this.typeAuth = typeAuth;
	}

}