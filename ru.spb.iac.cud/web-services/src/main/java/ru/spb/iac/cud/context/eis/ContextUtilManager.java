package ru.spb.iac.cud.context.eis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jws.WebParam;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spb.iac.cud.core.AccessManagerLocal;
import ru.spb.iac.cud.core.UtilManagerLocal;
import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.InvalidCredentials;
import ru.spb.iac.cud.exceptions.TokenExpired;
import ru.spb.iac.cud.items.Attribute;
import ru.spb.iac.cud.items.AuditFunction;
import ru.spb.iac.cud.items.Function;
import ru.spb.iac.cud.items.GroupsData;
import ru.spb.iac.cud.items.ISUsers;
import ru.spb.iac.cud.items.Resource;
import ru.spb.iac.cud.items.ResourceNU;
import ru.spb.iac.cud.items.ResourcesData;
import ru.spb.iac.cud.items.Role;
import ru.spb.iac.cud.items.Token;
import ru.spb.iac.cud.items.UserAttributes;
import ru.spb.iac.cud.items.UsersData;

public class ContextUtilManager {

	Logger logger = LoggerFactory.getLogger(ContextUtilManager.class);

	static Context ctx;
	UtilManagerLocal aml = null;

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

	public ContextUtilManager() {
		try {
			this.aml = (UtilManagerLocal) ctx
					.lookup("java:global/AuthServices/EisUtilManager!ru.spb.iac.cud.core.UtilManagerLocal");

		} catch (Exception e) {
			logger.error("ContextUtilManager:error:" + e);
		}
	}

	public UsersData users_data(String idIS, List<String> uidsUsers,
			String category, List<String> rolesCodes, List<String> groupsCodes,
			Integer start, Integer count, Map<String, String> settings,
			Long idUserAuth, String IPAddress) throws GeneralFailure {
		logger.info("users_data");

		return aml.users_data(idIS, uidsUsers, category, rolesCodes,
				groupsCodes, start, count, settings, idUserAuth, IPAddress);
	}

	public GroupsData groups_data(String idIS, List<String> groupsCodes,
			String category, List<String> rolesCodes, Integer start,
			Integer count, Map<String, String> settings, Long idUserAuth,
			String IPAddress) throws GeneralFailure {
		logger.info("groups_data");

		return aml.groups_data(idIS, groupsCodes, category, rolesCodes, start,
				count, settings, idUserAuth, IPAddress);
	}

	public ResourcesData resources_data(String idIS,
			List<String> resourcesCodes, String category,
			List<String> rolesCodes, Integer start, Integer count,
			Map<String, String> settings, Long idUserAuth, String IPAddress)
			throws GeneralFailure {
		logger.info("sys_resources");

		return aml.resources_data(idIS, resourcesCodes, category, rolesCodes,
				start, count, settings, idUserAuth, IPAddress);
	}

	public List<Resource> resources_data_subsys(String idIS, String category,
			Long idUserAuth, String IPAddress) throws GeneralFailure {
		return aml.resources_data_subsys(idIS, category, idUserAuth, IPAddress);

	}

	public List<Role> roles_data(String idIS, 
			String category,
			Long idUserAuth, String IPAddress)
			throws GeneralFailure {

		logger.info("roles_data");

		return aml.roles_data(idIS, category, idUserAuth, IPAddress);
	}
	
	private void is_exist(String idIS) throws GeneralFailure {
		logger.info(":is_exist:01");
		aml.is_exist(idIS);
	}

}
