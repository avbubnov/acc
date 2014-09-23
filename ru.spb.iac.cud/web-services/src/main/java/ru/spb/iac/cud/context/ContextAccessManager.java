package ru.spb.iac.cud.context;

import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spb.iac.cud.core.AccessManagerLocal;
import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.InvalidCredentials;
import ru.spb.iac.cud.items.AuditFunction;

public class ContextAccessManager {

	static Context ctx;
	AccessManagerLocal aml = null;

	Logger logger = LoggerFactory.getLogger(ContextAccessManager.class);

	static {
		try { 
			ctx = new InitialContext();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * public static void initContext(String jboss_jndi_port){ try{
	 * logger.info("initContext:jboss_jndi_port:"+jboss_jndi_port);
	 * 
	 * jboss_jndi_port=(jboss_jndi_port!=null?jboss_jndi_port:"1099");
	 * 
	 * Properties env = new Properties();
	 * env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
	 * "org.jnp.interfaces.NamingContextFactory");
	 * env.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming");
	 * env.setProperty(Context.PROVIDER_URL, "localhost:"+jboss_jndi_port); ctx
	 * = new InitialContext(env);
	 * 
	 * }catch(Exception e){ logger.error("initContext:error:"+e); } }
	 */

	public ContextAccessManager() {
		try {
			this.aml = (AccessManagerLocal) ctx
					.lookup("java:global/AuthServices/AccessManager!ru.spb.iac.cud.core.AccessManagerLocal");

		} catch (Exception e) {
			logger.error("ContextAccessManager:error:" + e);
		}
	}

	public void audit(String idIS, String login,
			List<AuditFunction> userFunctions, Long idUserAuth, String IPAddress)
			throws GeneralFailure {
		logger.info("audit");

		aml.audit_pro(idIS, login, userFunctions, idUserAuth, IPAddress);
	}

	public void change_password(String login, String password,
			String new_password, String IPAddress) throws GeneralFailure,
			InvalidCredentials {
		logger.info("change_password");
		aml.change_password(login, password, new_password, IPAddress);
	}

}
