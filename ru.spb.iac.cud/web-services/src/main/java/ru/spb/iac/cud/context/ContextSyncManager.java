package ru.spb.iac.cud.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spb.iac.cud.core.AccessManagerLocal;
import ru.spb.iac.cud.core.SyncManagerLocal;
import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.items.Attribute;
import ru.spb.iac.cud.items.Function;
import ru.spb.iac.cud.items.Group;
import ru.spb.iac.cud.items.Resource;
import ru.spb.iac.cud.items.Role;
import ru.spb.iac.cud.items.Token;

public class ContextSyncManager {

	static Context ctx;
	SyncManagerLocal aml = null;
	Logger logger = LoggerFactory.getLogger(ContextSyncManager.class);

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

	public ContextSyncManager() {
		try {

			this.aml = (SyncManagerLocal) ctx
					.lookup("java:global/AuthServices/SyncManager!ru.spb.iac.cud.core.SyncManagerLocal");

		} catch (Exception e) {
			logger.error("ContextSyncManager:error:" + e);
		}
	}

	public void sync_roles(String idIS, List<Role> roles, String modeExec,
			Long idUserAuth, String IPAddress) throws GeneralFailure {
		logger.info("sync_roles");

		aml.sync_roles(idIS, roles, modeExec, idUserAuth, IPAddress);
	}

	public void sync_functions(String idIS, List<Function> functions,
			String modeExec, Long idUserAuth, String IPAddress)
			throws GeneralFailure {
		logger.info("sync_functions");

		aml.sync_functions(idIS, functions, modeExec, idUserAuth, IPAddress);
	}

	public List<Role> is_roles(String idIS, Long idUserAuth, String IPAddress)
			throws GeneralFailure {

		logger.info("is_roles");

		return aml.is_roles(idIS, idUserAuth, IPAddress);
	}

	public List<Function> is_functions(String idIS, Long idUserAuth,
			String IPAddress) throws GeneralFailure {

		logger.info("is_functions");

		return aml.is_functions(idIS, idUserAuth, IPAddress);
	}

	public void sync_groups(String idIS, List<Group> groups, String modeExec,
			Long idUserAuth, String IPAddress) throws GeneralFailure {
		logger.info("sync_groups");

		aml.sync_groups(idIS, groups, modeExec, idUserAuth, IPAddress);
	}

	public void sync_groups_roles(String idIS, List<String> codesGroups,
			List<String> codesRoles, String modeExec, Long idUserAuth,
			String IPAddress) throws GeneralFailure {
		logger.info("sync_groups_roles");

		aml.sync_groups_roles(idIS, codesGroups, codesRoles, modeExec,
				idUserAuth, IPAddress);
	}

	public void sync_resources(String idIS, List<Resource> resources,
			String modeExec, Long idUserAuth, String IPAddress)
			throws GeneralFailure {
		logger.info("sync_resources");

		aml.sync_resources(idIS, resources, modeExec, idUserAuth, IPAddress);
	}

	public void sync_resources_roles(String idIS, List<String> codesResources,
			List<String> codesRoles, String modeExec, Long idUserAuth,
			String IPAddress) throws GeneralFailure {
		logger.info("sync_resources_roles");

		aml.sync_resources_roles(idIS, codesResources, codesRoles, modeExec,
				idUserAuth, IPAddress);
	}

	private void is_exist(String idIS) throws GeneralFailure {
		logger.info("is_exist:01");
		aml.is_exist(idIS);
	}
}
