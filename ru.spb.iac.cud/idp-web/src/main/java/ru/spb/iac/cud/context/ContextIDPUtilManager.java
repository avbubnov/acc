package ru.spb.iac.cud.context;

import java.security.cert.X509Certificate;
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
import ru.spb.iac.cud.idp.core.util.IDPUtilManagerLocal;
import ru.spb.iac.cud.items.AuthMode;
import ru.spb.iac.cud.items.Token;

public class ContextIDPUtilManager {

	static Context ctx;
	IDPUtilManagerLocal iml = null;

	final static Logger LOGGER = LoggerFactory
			.getLogger(ContextIDPUtilManager.class);

	public static void initContext(String jboss_jndi_port) {
		try {
			LOGGER.debug("initContext:jboss_jndi_port:" + jboss_jndi_port);

			ctx = new InitialContext();

		} catch (Exception e) {
			LOGGER.error("initContext:error:", e);
		}
	}

	public ContextIDPUtilManager() {
		try {

			this.iml = (IDPUtilManagerLocal) ctx
					.lookup("java:global/AuthServices/IDPUtilManager!ru.spb.iac.cud.idp.core.util.IDPUtilManagerLocal");

		} catch (Exception e) {
			LOGGER.debug("error:", e);
		}
	}

	public X509Certificate system_cert(String domain_name) throws Exception {

		return iml.system_cert(domain_name);

	}
}
