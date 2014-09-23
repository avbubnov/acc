package ru.spb.iac.cud.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spb.iac.cud.core.AccessManagerLocal;
import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.InvalidCredentials;
import ru.spb.iac.cud.exceptions.RevokedCertificate;
import ru.spb.iac.cud.exceptions.TokenExpired;
import ru.spb.iac.cud.items.Attribute;
import ru.spb.iac.cud.items.AuditFunction;
import ru.spb.iac.cud.items.AuthMode;
import ru.spb.iac.cud.items.Function;
import ru.spb.iac.cud.items.Role;
import ru.spb.iac.cud.items.Token;

public class ContextAccessWebManager {

	static Context ctx;
	AccessManagerLocal aml = null;

	final static Logger logger = LoggerFactory
			.getLogger(ContextAccessWebManager.class);

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
	 * }catch(Exception e){
	 * logger.error("error:"+e); } }
	 */
	public static void initContext(String jboss_jndi_port) {
		try {
			logger.info("initContext:jboss_jndi_port:" + jboss_jndi_port);

			ctx = new InitialContext();

		} catch (Exception e) {
			logger.error("initContext:error:" + e);
		}
	}

	public ContextAccessWebManager() {
		try {

			this.aml = (AccessManagerLocal) ctx
					.lookup("java:global/AuthServices/AccessManager!ru.spb.iac.cud.core.AccessManagerLocal");

		} catch (Exception e) {
			logger.error("error:" + e);
		}
	}

	public String authenticate_cert_sn(String sn, String IPAddress,
			String codeSys) throws GeneralFailure, InvalidCredentials,
			RevokedCertificate {
		logger.info("authenticate_cert_sn");
		return aml.authenticate_cert_sn(sn, AuthMode.HTTP_REDIRECT, IPAddress,
				codeSys);
	}

	public String authenticate_login(String login, String password,
			AuthMode authMode, String IPAddress, String codeSys)
			throws GeneralFailure, InvalidCredentials {
		logger.info("authenticate_login");
		return aml.authenticate_login(login, password, authMode, IPAddress,
				codeSys);
	}

	public void sys_audit_public(Long idServ, String inp_param, String result,
			String ip_adr, Long idUser, String loginUser, String codeSys) {
		logger.info("sys_audit_public");
		aml.sys_audit_public(idServ, inp_param, result, ip_adr, idUser,
				loginUser, codeSys);
	}

}
