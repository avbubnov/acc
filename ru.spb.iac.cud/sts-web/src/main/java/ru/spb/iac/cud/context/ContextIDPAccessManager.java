package ru.spb.iac.cud.context;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spb.iac.cud.core.AccessManagerLocal;
import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.InvalidCredentials;
import ru.spb.iac.cud.exceptions.RevokedCertificate;
import ru.spb.iac.cud.exceptions.TokenExpired;
import ru.spb.iac.cud.idp.core.access.IDPAccessManagerLocal;
import ru.spb.iac.cud.idp.core.util.IDPUtilManagerLocal;
import ru.spb.iac.cud.items.AuthMode;
import ru.spb.iac.cud.items.Token;

public class ContextIDPAccessManager {

	static Context ctx;
	IDPAccessManagerLocal iml = null;

	final static Logger LOGGER = LoggerFactory
			.getLogger(ContextIDPAccessManager.class);

	
	public static void initContext(String jboss_jndi_port) {
		try {
			LOGGER.debug("initContext:jboss_jndi_port:" + jboss_jndi_port);
			ctx = new InitialContext();

		} catch (Exception e) {
			LOGGER.error("initContext::error:", e);
		}
	}

	public ContextIDPAccessManager() {
		try {

			this.iml = (IDPAccessManagerLocal) ctx
					.lookup("java:global/AuthServices/IDPAccessManager!ru.spb.iac.cud.idp.core.access.IDPAccessManagerLocal");

		} catch (Exception e) {
			LOGGER.error("error:", e);
		}
	}

	public Map<String, String> attributes(String login) throws Exception {
		return iml.attributes(login);
	}

	public List<String> rolesCodes(String login, String domain)
			throws Exception {
		return iml.rolesCodes(login, domain);
	}
  
	public void saveTokenID(String tokenID, String userID)
			throws Exception {
		 iml.saveTokenID(tokenID, userID);
	}

}
