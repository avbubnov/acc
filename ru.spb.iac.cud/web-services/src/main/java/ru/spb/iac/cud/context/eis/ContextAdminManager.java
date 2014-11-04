package ru.spb.iac.cud.context.eis;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.jws.WebParam;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spb.iac.cud.core.AccessManagerLocal;
import ru.spb.iac.cud.core.UtilManagerLocal;
import ru.spb.iac.cud.core.eis.AdminManagerLocal;
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

public class ContextAdminManager {

	final static Logger LOGGER = LoggerFactory.getLogger(ContextAdminManager.class);

	static Context ctx;
	AdminManagerLocal aml = null;

	static {
		try {
			ctx = new InitialContext();

		} catch (Exception e) {
			LOGGER.error("error",e);
		}
	}

	

	public ContextAdminManager() {
		try {
			this.aml = (AdminManagerLocal) ctx
					.lookup("java:global/AuthServices/EisAdminManager!ru.spb.iac.cud.core.eis.AdminManagerLocal");

		} catch (Exception e) {
			LOGGER.error("ContextAdminManager:error:", e);
		}
	}

	public void access(String codeSystem, List<String> uidsUsers,
			String modeExec, List<String> codesRoles, Long idUserAuth,
			String IPAddress) throws GeneralFailure {
		LOGGER.debug("access");

		aml.access(codeSystem, uidsUsers, modeExec, codesRoles, idUserAuth,
				IPAddress);
	}

	public void access_groups(String codeSystem, List<String> uidsUsers,
			String modeExec, List<String> codesGroups, Long idUserAuth,
			String IPAddress) throws GeneralFailure {
		LOGGER.debug("access_groups");

		aml.access_groups(codeSystem, uidsUsers, modeExec, codesGroups,
				idUserAuth, IPAddress);
	}

	public void cert_change(String codeSystem, String newCert, Long idUserAuth,
			String IPAddress) throws GeneralFailure {
		LOGGER.debug("cert_change");

		aml.cert_change(codeSystem, newCert, idUserAuth, IPAddress);
	}

}
