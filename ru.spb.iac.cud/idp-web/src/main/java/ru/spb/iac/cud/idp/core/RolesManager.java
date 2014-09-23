package ru.spb.iac.cud.idp.core;

import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.picketlink.identity.federation.core.interfaces.RoleGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spb.iac.cud.context.ContextIDPAccessManager;
import ru.spb.iac.cud.idp.items.CUDRoleGenerator;

public class RolesManager implements CUDRoleGenerator {

	private String codeSystem;

	final static Logger logger = LoggerFactory.getLogger(RolesManager.class);

	public List<String> generateRoles(Principal principal) {

		List<String> roles = new ArrayList<String>();
		try {

			// logger.info("generateRoles:01:"+principal);
			// logger.info("generateRoles:02:"+this.codeSystem);

			String domain = this.codeSystem;

			roles = (new ContextIDPAccessManager()).rolesCodes(
					principal.getName(), domain);

		} catch (Exception e) {
			logger.error("generateRoles:error:" + e);
		}
		return roles;
	}

	public List<String> generateResources(Principal principal) {

		List<String> resources = new ArrayList<String>();
		try {

			logger.info("generateResources:01:" + principal);
			logger.info("generateResources:02:" + this.codeSystem);

			String domain = this.codeSystem;

			resources = (new ContextIDPAccessManager()).resources(
					principal.getName(), domain);

		} catch (Exception e) {
			logger.info("generateResources:error:" + e);
		}
		return resources;
	}

	public void setSystemCode(String codeSystem) {
		this.codeSystem = codeSystem;

	}
}
