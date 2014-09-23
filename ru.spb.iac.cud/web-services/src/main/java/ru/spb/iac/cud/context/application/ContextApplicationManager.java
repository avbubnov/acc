package ru.spb.iac.cud.context.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.jws.WebParam;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spb.iac.cud.core.app.ApplicationManagerLocal;
import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.InvalidCredentials;
import ru.spb.iac.cud.exceptions.TokenExpired;
import ru.spb.iac.cud.items.app.AppAttribute;
import ru.spb.iac.cud.items.AuditFunction;
import ru.spb.iac.cud.items.Function;
import ru.spb.iac.cud.items.ISUsers;
import ru.spb.iac.cud.items.Role;
import ru.spb.iac.cud.items.Token;
import ru.spb.iac.cud.items.UserAttributes;
import ru.spb.iac.cud.items.app.AppAccept;
import ru.spb.iac.cud.items.app.AppTypeClassif;

public class ContextApplicationManager {

	static Context ctx;
	ApplicationManagerLocal aml = null;

	Logger logger = LoggerFactory.getLogger(ContextApplicationManager.class);

	static {
		try {
			ctx = new InitialContext();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * public static void initContext(String jboss_jndi_port){ try{
	 * System.out.println
	 * ("CudServices:ContextUtilManager:initContext:jboss_jndi_port:"
	 * +jboss_jndi_port);
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
	 * }catch(Exception e){
	 * System.out.println("authServices.ContextAccessWebManager:error:"+e); } }
	 */

	public ContextApplicationManager() {
		try {
			this.aml = (ApplicationManagerLocal) ctx
					.lookup("java:global/AuthServices/ApplicationManager!ru.spb.iac.cud.core.app.ApplicationManagerLocal");

		} catch (Exception e) {
			logger.error("ContextApplicationManager:error:" + e);
		}
	}

	public AppAccept system_registration(List<AppAttribute> attributes,
			Long idUserAuth, String IPAddress) throws GeneralFailure {

		logger.info("system_registration");

		return aml.system_registration(attributes, null, idUserAuth, IPAddress);
	}

	public AppAccept user_registration(List<AppAttribute> attributes,
			Long idUserAuth, String IPAddress) throws GeneralFailure {

		logger.info("user_registration");

		return aml.user_registration(attributes, null, idUserAuth, IPAddress);
	}

	public AppAccept access_roles(String modeExec, String loginUser,
			String codeSystem, List<String> codesRoles, Long idUserAuth,
			String IPAddress) throws GeneralFailure {

		logger.info("access_roles");
		return aml.access_roles(modeExec, loginUser, codeSystem, codesRoles,
				null, idUserAuth, IPAddress);
	}

	public AppAccept access_groups(String modeExec, String loginUser,
			String codeSystem, List<String> codesGroups, Long idUserAuth,
			String IPAddress) throws GeneralFailure {

		logger.info("access_groups");

		return aml.access_groups(modeExec, loginUser, codeSystem, codesGroups,
				null, idUserAuth, IPAddress);
	}

	public AppAccept block(String modeExec, String loginUser,
			String blockReason, Long idUserAuth, String IPAddress)
			throws GeneralFailure {

		logger.info("block");

		return aml.block(modeExec, loginUser, blockReason, null, idUserAuth,
				IPAddress);
	}

	public AppAccept system_modification(String codeSystem,
			List<AppAttribute> attributes, Long idUserAuth, String IPAddress)
			throws GeneralFailure {

		return aml.system_modification(codeSystem, attributes, null,
				idUserAuth, IPAddress);
	}

	public AppAccept user_modification(String loginUser,
			List<AppAttribute> attributes, Long idUserAuth, String IPAddress)
			throws GeneralFailure {

		return aml.user_modification(loginUser, attributes, null, idUserAuth,
				IPAddress);
	}

	public AppAccept user_identity_modification(String loginUser, String login,
			String password, Long idUserAuth, String IPAddress)
			throws GeneralFailure {

		return aml.user_identity_modification(loginUser, login, password,
				idUserAuth, IPAddress);
	}

	public AppAccept user_cert_modification(String modeExec, String loginUser,
			String certBase64, Long idUserAuth, String IPAddress)
			throws GeneralFailure {

		return aml.user_cert_modification(modeExec, loginUser, certBase64,
				idUserAuth, IPAddress);
	}

	public AppAccept user_dep_modification(String loginUser,
			List<AppAttribute> attributes, Long idUserAuth, String IPAddress)
			throws GeneralFailure {

		return aml.user_dep_modification(loginUser, attributes, null,
				idUserAuth, IPAddress);
	}

}
