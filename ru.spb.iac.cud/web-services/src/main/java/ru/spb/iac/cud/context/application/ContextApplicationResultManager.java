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
import ru.spb.iac.cud.core.app.ApplicationResultManagerLocal;
import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.InvalidCredentials;
import ru.spb.iac.cud.exceptions.TokenExpired;
import ru.spb.iac.cud.items.Attribute;
import ru.spb.iac.cud.items.AuditFunction;
import ru.spb.iac.cud.items.Function;
import ru.spb.iac.cud.items.ISUsers;
import ru.spb.iac.cud.items.Role;
import ru.spb.iac.cud.items.Token;
import ru.spb.iac.cud.items.UserAttributes;
import ru.spb.iac.cud.items.app.AppAccept;
import ru.spb.iac.cud.items.app.AppResult;
import ru.spb.iac.cud.items.app.AppResultRequest;
import ru.spb.iac.cud.items.app.AppSystemResult;
import ru.spb.iac.cud.items.app.AppTypeClassif;

public class ContextApplicationResultManager {

	static Context ctx;
	ApplicationResultManagerLocal aml = null;

	private static final Long user_default = 1L;

	Logger logger = LoggerFactory
			.getLogger(ContextApplicationResultManager.class);

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

	public ContextApplicationResultManager() {
		try {
			this.aml = (ApplicationResultManagerLocal) ctx
					.lookup("java:global/AuthServices/ApplicationResultManager!ru.spb.iac.cud.core.app.ApplicationResultManagerLocal");

		} catch (Exception e) {
			logger.error("ContextApplicationResultManager:error:" + e);
		}
	}

	public List<AppResult> result(List<AppResultRequest> request_list,
			Long idUserAuth, String IPAddress) throws GeneralFailure {

		if (request_list == null || request_list.isEmpty()) {
			throw new GeneralFailure("Некорректные данные в заявке!");
		}

		return aml.result(request_list, idUserAuth, IPAddress);
	}

	private void number_secret_valid(String number, String secret, String type)
			throws GeneralFailure {
		logger.info("number_exist:01");
		aml.number_secret_valid(number, secret, type);
	}

}
