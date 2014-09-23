package iac.cud.authmodule.session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import iac.cud.authmodule.dataitem.*;

import java.util.ArrayList;

//import org.jboss.ejb3.annotation.LocalBinding;
//import org.jboss.ejb3.annotation.RemoteBinding;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * для работы (маппинг ролей)
 * 
 * @author bubnov
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class AutzManager implements AutzManagerLocal, AutzManagerRemote {

	@PersistenceContext(unitName = "CUDAuthModule")
	EntityManager em;

	final static Logger logger = LoggerFactory.getLogger(AutzManager.class);

	public AutzManager() {
	}

	/**
	 * Аутентификация и авторизация пользователя
	 * 
	 * @param appCode
	 *            ид приложения
	 * @param login
	 *            логин
	 * @param password
	 *            пароль
	 * @return AuthItem, если упешно, иначе null
	 * @throws Exception
	 */
	public AuthItem getAccessComplete(Long appCode, List<String> roles)
			throws Exception {
		try {
			logger.info("getPermFromRoles:01");

			if (appCode == null || roles == null || roles.isEmpty()) {
				return null;
			}
			logger.info("getPermFromRoles:02");

			return createResourceTreeItem(appCode, roles, null);
		} catch (Exception e) {
			logger.error("authCompleteItem:Error:" + e);
			throw e;
		}
	}

	private AuthItem createResourceTreeItem(Long appCode, List<String> roles,
			String pageCode) throws Exception {

		logger.info("createResourceTreeItem:01");

		AuthItem result = new AuthItem();
		result.setIdUser(null);

		ArrayList<String> permList = new ArrayList<String>();
		String pageCode_prev = "", pageCode_curr = "";

		try {

			String roleLine = null;

			for (String role : roles) {
				if (roleLine == null) {
					roleLine = "'" + role + "'";
				} else {
					roleLine = roleLine + ", '" + role + "'";
				}
			}
			logger.info("createResourceTreeItem:roleLine:" + roleLine);

			List<Object[]> lo = em
					.createNativeQuery(
							"select * from( "
									+ "select AAD.PAGE_CODE, APL.ID_SRV perm_code  "
									+ "from AC_APP_DOMAINS_BSS_T aad, "
									+ "AC_LINK_ROLE_APP_DOM_PRM_KNL_T adp, "
									+ "AC_ROLES_BSS_T ar, "
									+ "AC_PERMISSIONS_LIST_BSS_T apl "
									+ "where AAD.UP_IS= ?1 "
									+ "and ADP.UP_PERMISS = APL.ID_SRV "
									+ "and adp.UP_DOM=AAD.ID_SRV "
									+ "and adp.UP_ROLES=ar.ID_SRV "
									+ "and AR.SIGN_OBJECT in ("
									+ roleLine
									+ ") "
									+ (pageCode != null ? "and AAD.PAGE_CODE= ?2 ) "
											: "and 1= ?2 ) ")
									+ "group by PAGE_CODE, perm_code "
									+ "order by PAGE_CODE, perm_code ")
					.setParameter(1, appCode)
					.setParameter(2, (pageCode != null ? pageCode : 1))
					.getResultList();

			logger.info("createResourceTreeItem:02:" + lo.size());

			for (Object[] objectArray : lo) {
				pageCode_curr = objectArray[0].toString();

				if (pageCode_curr.equals(pageCode_prev)
						|| pageCode_prev.equals("")) {

					permList.add(objectArray[1].toString());
				} else {

					result.getPageList().put(pageCode_prev,
							new PageItem(permList));

					permList = new ArrayList<String>();

					permList.add(objectArray[1].toString());
				}

				pageCode_prev = pageCode_curr;
			}

			logger.info("createResourceTreeItem:03");

			if (!pageCode_curr.equals("")) {

				logger.info("createResourceTreeItem:04");

				result.getPageList().put(pageCode_prev, new PageItem(permList));

			}

			logger.info("createResourceTreeItem:05");

		} catch (Exception e) {
			logger.error("createResourceTreeItem:Error:" + e);
			throw e;
		}
		return result;
	}

	/**
	 * Проверка прав пользователя к заданному ресурсу приложения
	 * 
	 * @param appCode
	 *            ид приложения
	 * @param pageCode
	 *            внутренний ид ресурса в приложении
	 * @param idUser
	 *            ид пользователя
	 * @return PageItem, если упешно, иначе null
	 * @throws Exception
	 */
	public PageItem getAccessPage(Long appCode, List<String> roles,
			String pageCode) throws Exception {
		logger.info("accessItem");
		if (appCode == null || pageCode == null || roles == null
				|| roles.isEmpty()) {
			return null;
		}
		return createResourceTreeItem(appCode, roles, pageCode).getPageList()
				.get(pageCode);
	}

	public boolean getAccessPage(Long appCode, List<String> roles,
			String pageCode, String permCode) throws Exception {
		logger.info("accessItem");
		if (appCode == null || pageCode == null || roles == null
				|| roles.isEmpty()) {
			throw new NullPointerException();
		}
		PageItem pi = createResourceTreeItem(appCode, roles, pageCode)
				.getPageList().get(pageCode);
		if (pi != null) {

			if (pi.getPermList().contains(permCode)) {
				return true;
			}
		}

		return false;
	}

}