package ru.spb.iac.cud.context;

import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spb.iac.cud.idp.core.util.IDPUtilManagerLocal;

public class ContextIDPUtilManager {

	static Context ctx;
	IDPUtilManagerLocal iml = null;

	final static Logger logger = LoggerFactory
			.getLogger(ContextIDPUtilManager.class);

	/*
	 * static{ try{
	 * 
	 * Properties env = new Properties();
	 * env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
	 * "org.jnp.interfaces.NamingContextFactory");
	 * env.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming");
	 * env.setProperty(Context.PROVIDER_URL, "localhost:1099"); ctx = new
	 * InitialContext(env);
	 * 
	 * }catch(Exception e){ logger.error("error:"+e); } }
	 */
	public static void initContext(String jboss_jndi_port) {
		try {
			logger.info("initContext:jboss_jndi_port:" + jboss_jndi_port);
			ctx = new InitialContext();

		} catch (Exception e) {
			logger.error("initContext:error:" + e);
		}
	}

	public ContextIDPUtilManager() {
		try {
			this.iml = (IDPUtilManagerLocal) ctx
					.lookup("java:global/AuthServices/IDPUtilManager!ru.spb.iac.cud.idp.core.util.IDPUtilManagerLocal");

		} catch (Exception e) {
			logger.error("error:" + e);
		}
	}

	public X509Certificate system_cert(String domain_name) throws Exception {

		return iml.system_cert(domain_name);

	}

}
