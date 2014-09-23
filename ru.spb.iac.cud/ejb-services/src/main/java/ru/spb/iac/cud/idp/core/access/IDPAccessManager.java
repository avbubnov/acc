package ru.spb.iac.cud.idp.core.access;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spb.iac.cud.core.util.CUDConstants;

/**
 * EJB для предоставления информации по пользователю при авторизации
 * 
 * @author bubnov
 * 
 */

@Stateless
public class IDPAccessManager implements IDPAccessManagerLocal,
		IDPAccessManagerRemote {

	@PersistenceContext(unitName = "AuthServices")
	EntityManager em;

	Logger logger = LoggerFactory.getLogger(IDPAccessManager.class);

	/**
	 * получение аттрибутов пользователя
	 */
	public Map<String, String> attributes(String login) throws Exception {

		Map<String, String> result = new HashMap<String, String>();

		List<Object[]> lo = null;

		try {
			logger.info("attributes:login:" + login);

			lo = em.createNativeQuery(
					/*"select t1.t1_id, t1.t1_login, t1.t1_cert, t1.t1_usr_code, t1.t1_fio, t1.t1_tel, t1.t1_email,t1.t1_pos, t1.t1_dep_name, "
							+ "t1.t1_org_code, t1.t1_org_name, t1.t1_org_adr, t1.t1_org_tel, t1.t1_start, t1.t1_end, t1.t1_status, "
							+ "t1.t1_crt_date, t1.t1_crt_usr_login, t1.t1_upd_date, t1.t1_upd_usr_login, "
							+ "t1.t1_dep_code, t1.t1_org_status, t1.t1_usr_status, t1.t1_dep_status, "
							+ "t1.t1_org_okato, t1.t1_dep_adr, t1_oktmo "
							+ "from( "
							+ "select AU_FULL.ID_SRV t1_id, AU_FULL.login t1_login, AU_FULL.CERTIFICATE t1_cert, t2.CL_USR_CODE t1_usr_code, "
							+ "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.SURNAME||' '||AU_FULL.NAME_ ||' '|| AU_FULL.PATRONYMIC,  CL_USR_FULL.FIO ) t1_fio, "
							+ "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.PHONE, CL_USR_FULL.PHONE ) t1_tel, "
							+ "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.E_MAIL, CL_USR_FULL.EMAIL) t1_email, "
							+ "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.POSITION, CL_USR_FULL.POSITION)t1_pos, "
							+ "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.DEPARTMENT, decode(substr(CL_DEP_FULL.sign_object,4,2), '00', null, CL_DEP_FULL.FULL_)) t1_dep_name, "
							+ "t1.CL_ORG_CODE t1_org_code, CL_ORG_FULL.FULL_ t1_org_name, "
							+ "CL_ORG_FULL.PREFIX || decode(CL_ORG_FULL.HOUSE, null, null, ','  ||CL_ORG_FULL.HOUSE  ) t1_org_adr, "
							+ "CL_ORG_FULL.PHONE t1_org_tel, "
							+ "to_char(AU_FULL.START_ACCOUNT, 'DD.MM.YY HH24:MI:SS') t1_start, "
							+ "to_char(AU_FULL.END_ACCOUNT, 'DD.MM.YY HH24:MI:SS') t1_end, "
							+ "AU_FULL.STATUS t1_status, "
							+ "AU_FULL.CREATED t1_crt_date, "
							+ "USR_CRT.LOGIN t1_crt_usr_login, "
							+ "to_char(AU_FULL.MODIFIED, 'DD.MM.YY HH24:MI:SS') t1_upd_date, "
							+ "USR_UPD.LOGIN t1_upd_usr_login, "
							+ "decode(AU_FULL.UP_SIGN_USER, null, null, decode(substr(CL_DEP_FULL.sign_object,4,2), '00', null, CL_DEP_FULL.sign_object)) t1_dep_code, "
							+ "CL_ORG_FULL.STATUS t1_org_status,  CL_usr_FULL.STATUS t1_usr_status, "
							+ "decode(AU_FULL.UP_SIGN_USER, null, null, decode(substr(CL_DEP_FULL.sign_object,4,2), '00', null, CL_DEP_FULL.STATUS)) t1_dep_status, "
							+ "CL_ORG_FULL.SIGN_OKATO t1_org_okato, CL_DEP_FULL.PREFIX || decode(CL_DEP_FULL.HOUSE, null, null, ','  ||CL_DEP_FULL.HOUSE  ) t1_dep_adr, "
							+ "mun.SIGN_OKTMO t1_oktmo "
							+ "from "
							+ "(select max(CL_ORG.ID_SRV) CL_ORG_ID,  CL_ORG.SIGN_OBJECT  CL_ORG_CODE "
							+ "from ISP_BSS_T cl_org, "
							+ "AC_USERS_KNL_T au "
							+ "where AU.UP_SIGN = CL_ORG.SIGN_OBJECT "
							+ "group by CL_ORG.SIGN_OBJECT) t1, "
							+ "(select max(CL_usr.ID_SRV) CL_USR_ID,  CL_USR.SIGN_OBJECT  CL_USR_CODE "
							+ "from ISP_BSS_T cl_usr, "
							+ "AC_USERS_KNL_T au "
							+ "where AU.UP_SIGN_USER  = CL_usr.SIGN_OBJECT "
							+ "group by CL_usr.SIGN_OBJECT) t2, "
							+ "(select max(CL_dep.ID_SRV) CL_DEP_ID,  CL_DEP.SIGN_OBJECT  CL_DEP_CODE "
							+ "from ISP_BSS_T cl_dep, "
							+ "AC_USERS_KNL_T au "
							+ "where substr(au.UP_SIGN_USER,1,5)||'000'  =cl_dep.SIGN_OBJECT(+) "
							+ "group by CL_DEP.SIGN_OBJECT) t3, "
							+ "ISP_BSS_T cl_org_full, "
							+ "ISP_BSS_T cl_usr_full, "
							+ "ISP_BSS_T cl_dep_full, "
							+ "AC_USERS_KNL_T au_full, "
							+ "AC_USERS_KNL_T usr_crt, "
							+ "AC_USERS_KNL_T usr_upd, "
							+ "MUNIC_BSS_T mun "
							+ "where cl_org_full.ID_SRV= CL_ORG_ID "
							+ "and cl_usr_full.ID_SRV(+)=CL_USR_ID "
							+ "and cl_DEP_full.ID_SRV(+)=CL_DEP_ID "
							+ "and au_full.UP_SIGN = CL_ORG_CODE "
							+ "and au_full.UP_SIGN_USER  =  CL_USR_CODE(+) "
							+ "and substr(au_full.UP_SIGN_USER,1,5)||'000'  =  CL_DEP_CODE(+) "
							+ "and au_full.CREATOR=USR_CRT.ID_SRV "
							+ "and au_full.MODIFICATOR=USR_UPD.ID_SRV(+) "
							+ "and au_full.login=? "
							+ "and AU_FULL.UP_MUNIC=mun.id_srv(+) )t1 ")
							*/
							"select t1.t1_id, t1.t1_login, t1.t1_cert, t1.t1_usr_code, t1.t1_fio, t1.t1_tel, t1.t1_email,t1.t1_pos, t1.t1_dep_name, "
							+ "t1.t1_org_code, t1.t1_org_name, t1.t1_org_adr, t1.t1_org_tel, t1.t1_start, t1.t1_end, t1.t1_status, "
							+ "t1.t1_crt_date, t1.t1_crt_usr_login, t1.t1_upd_date, t1.t1_upd_usr_login, "
							+ "t1.t1_dep_code, t1.t1_org_status, t1.t1_usr_status, t1.t1_dep_status, "
							+ "t1.t1_org_okato, t1.t1_dep_adr "
							+ "from( "
							+ "select AU_FULL.ID_SRV t1_id, AU_FULL.login t1_login, AU_FULL.CERTIFICATE t1_cert, t2.CL_USR_CODE t1_usr_code, "
							+ "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.SURNAME||' '||AU_FULL.NAME_ ||' '|| AU_FULL.PATRONYMIC,  CL_USR_FULL.FIO ) t1_fio, "
							+ "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.PHONE, CL_USR_FULL.PHONE ) t1_tel, "
							+ "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.E_MAIL, CL_USR_FULL.EMAIL) t1_email, "
							+ "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.POSITION, CL_USR_FULL.POSITION)t1_pos, "
							+ "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.DEPARTMENT, decode(substr(CL_DEP_FULL.sign_object,4,2), '00', null, CL_DEP_FULL.FULL_)) t1_dep_name, "
							+ "t1.CL_ORG_CODE t1_org_code, CL_ORG_FULL.FULL_ t1_org_name, "
							+ "CL_ORG_FULL.PREFIX || decode(CL_ORG_FULL.HOUSE, null, null, ','  ||CL_ORG_FULL.HOUSE  ) t1_org_adr, "
							+ "CL_ORG_FULL.PHONE t1_org_tel, "
							+ "to_char(AU_FULL.START_ACCOUNT, 'DD.MM.YY HH24:MI:SS') t1_start, "
							+ "to_char(AU_FULL.END_ACCOUNT, 'DD.MM.YY HH24:MI:SS') t1_end, "
							+ "AU_FULL.STATUS t1_status, "
							+ "AU_FULL.CREATED t1_crt_date, "
							+ "USR_CRT.LOGIN t1_crt_usr_login, "
							+ "to_char(AU_FULL.MODIFIED, 'DD.MM.YY HH24:MI:SS') t1_upd_date, "
							+ "USR_UPD.LOGIN t1_upd_usr_login, "
							+ "decode(AU_FULL.UP_SIGN_USER, null, null, decode(substr(CL_DEP_FULL.sign_object,4,2), '00', null, CL_DEP_FULL.sign_object)) t1_dep_code, "
							+ "CL_ORG_FULL.STATUS t1_org_status,  CL_usr_FULL.STATUS t1_usr_status, "
							+ "decode(AU_FULL.UP_SIGN_USER, null, null, decode(substr(CL_DEP_FULL.sign_object,4,2), '00', null, CL_DEP_FULL.STATUS)) t1_dep_status, "
							+ "CL_ORG_FULL.SIGN_OKATO t1_org_okato, CL_DEP_FULL.PREFIX || decode(CL_DEP_FULL.HOUSE, null, null, ','  ||CL_DEP_FULL.HOUSE  ) t1_dep_adr "
							+ "from "
							+ "(select max(CL_ORG.ID_SRV) CL_ORG_ID,  CL_ORG.SIGN_OBJECT  CL_ORG_CODE "
							+ "from ISP_BSS_T cl_org, "
							+ "AC_USERS_KNL_T au "
							+ "where AU.UP_SIGN = CL_ORG.SIGN_OBJECT "
							+ "group by CL_ORG.SIGN_OBJECT) t1, "
							+ "(select max(CL_usr.ID_SRV) CL_USR_ID,  CL_USR.SIGN_OBJECT  CL_USR_CODE "
							+ "from ISP_BSS_T cl_usr, "
							+ "AC_USERS_KNL_T au "
							+ "where AU.UP_SIGN_USER  = CL_usr.SIGN_OBJECT "
							+ "group by CL_usr.SIGN_OBJECT) t2, "
							+ "(select max(CL_dep.ID_SRV) CL_DEP_ID,  CL_DEP.SIGN_OBJECT  CL_DEP_CODE "
							+ "from ISP_BSS_T cl_dep, "
							+ "AC_USERS_KNL_T au "
							+ "where substr(au.UP_SIGN_USER,1,5)||'000'  =cl_dep.SIGN_OBJECT(+) "
							+ "group by CL_DEP.SIGN_OBJECT) t3, "
							+ "ISP_BSS_T cl_org_full, "
							+ "ISP_BSS_T cl_usr_full, "
							+ "ISP_BSS_T cl_dep_full, "
							+ "AC_USERS_KNL_T au_full, "
							+ "AC_USERS_KNL_T usr_crt, "
							+ "AC_USERS_KNL_T usr_upd "
							+ "where cl_org_full.ID_SRV= CL_ORG_ID "
							+ "and cl_usr_full.ID_SRV(+)=CL_USR_ID "
							+ "and cl_DEP_full.ID_SRV(+)=CL_DEP_ID "
							+ "and au_full.UP_SIGN = CL_ORG_CODE "
							+ "and au_full.UP_SIGN_USER  =  CL_USR_CODE(+) "
							+ "and substr(au_full.UP_SIGN_USER,1,5)||'000'  =  CL_DEP_CODE(+) "
							+ "and au_full.CREATOR=USR_CRT.ID_SRV "
							+ "and au_full.MODIFICATOR=USR_UPD.ID_SRV(+) "
							+ "and au_full.login=? " + ")t1 ")
					.setParameter(1, login).getResultList();

			for (Object[] objectArray : lo) {

				String name = null;

				for (int i = 0; i < objectArray.length; i++) {

					switch (i) {

					case 0: {
						name = "USER_UID";
						break;
					}
					case 1: {
						name = "USER_LOGIN";
						break;
					}
					case 4: {
						name = "USER_FIO";
						break;
					}
					case 5: {
						name = "USER_PHONE";
						break;
					}
					case 6: {
						name = "USER_EMAIL";
						break;
					}
					case 10: {
						name = "ORG_NAME";
						break;
					}
					case 9: {
						name = "ORG_CODE_IOGV";
						break;
					}
					case 11: {
						name = "ORG_ADDRESS";
						break;
					}
					case 12: {
						name = "ORG_PHONE";
						break;
					}
					case 8: {
						name = "DEP_NAME";
						break;
					}
					case 7: {
						name = "USER_POSITION";
						break;
					}
					case 24: {
						name = "ORG_CODE_OKATO";
						break;
					}
					case 25: {
						name = "DEP_ADDRESS";
						break;
					}
					case 26: {
						name = "CODE_OKTMO";
						break;
					}
					default:
						name = null;
					}

					if (name != null) {

						result.put(
								name,
								(objectArray[i] != null ? objectArray[i]
										.toString() : ""));
					}
				}
			}

			if (result != null) {
				logger.info("attributes:result:" + result.size());
			}
		} catch (Exception e) {
			logger.error("attributes:error:" + e);
		}

		// если result = null, то будет выброшено исключение
		// в KeyStoreKeyManager.getValidatingKey() -
		// throw logger.keyStoreMissingDomainAlias(domain);

		return result;
	}

	/**
	 * получение кодов ролей пользователя
	 */
	public List<String> rolesCodes(String login, String domain)
			throws Exception {

		List<String> result = new ArrayList<String>();
		try {
			logger.info("rolesCodes:domain:" + domain);

			if (domain != null
					&& (domain.startsWith(CUDConstants.armPrefix) || domain
							.startsWith(CUDConstants.subArmPrefix))) {
				// if(CUDConstants.armPrefix.equals(domain)||CUDConstants.subArmPrefix.equals(domain)){

				result = (List<String>) em
						.createNativeQuery(
								"select ROL.SIGN_OBJECT "
										+ "from  AC_IS_BSS_T sys, "
										+ "         AC_ROLES_BSS_T rol, "
										+ "         AC_USERS_LINK_KNL_T url, "
										+ "         AC_USERS_KNL_T AU, "
										+ "         AC_SUBSYSTEM_CERT_BSS_T subsys, "
										+ "         LINK_GROUP_USERS_ROLES_KNL_T lugr, "
										+ "         LINK_GROUP_USERS_USERS_KNL_T lugu "
										+ "where (SYS.SIGN_OBJECT= :idIs or  SUBSYS.SUBSYSTEM_CODE=:idIs) "
										+ "      and (ROL.ID_SRV = URL.UP_ROLES or ROL.ID_SRV = LUGR.UP_ROLES ) "
										+ "      and LUGU.UP_GROUP_USERS= LUGR.UP_GROUP_USERS(+) "
										+ "      and LUGU.UP_USERS(+)  = AU.ID_SRV "
										+ "      and URL.UP_USERS(+)  = AU.ID_SRV "
										+ "      and ROL.UP_IS=sys.ID_SRV "
										+ "      and AU.LOGIN= :login "
										+ "      and  SUBSYS.UP_IS(+) =SYS.ID_SRV "
										+ "group by ROL.SIGN_OBJECT ")
						.setParameter("idIs", domain)
						.setParameter("login", login).getResultList();

			} else if (domain != null
					&& domain.startsWith(CUDConstants.groupArmPrefix)) {

				result = (List<String>) em
						.createNativeQuery(
								/*"select '['||sys_code||']' ||role_code role_full_code "
										+ "from( "
										+ "select SYS.SIGN_OBJECT sys_code,  ROL.SIGN_OBJECT role_code "
										+ "from GROUP_SYSTEMS_KNL_T gsys, "
										+ "AC_IS_BSS_T sys, "
										+ "AC_ROLES_BSS_T rol, "
										+ "LINK_GROUP_SYS_SYS_KNL_T lgr, "
										+ "LINK_GROUP_USERS_ROLES_KNL_T lugr, "
										+ "LINK_GROUP_USERS_USERS_KNL_T lugu, "
										+ "AC_USERS_LINK_KNL_T url, "
										+ "AC_USERS_KNL_T AU "
										+ "where GSYS.GROUP_CODE=:idIs "
										+ "and GSYS.ID_SRV=LGR.UP_GROUP_SYSTEMS "
										+ "and LGR.UP_SYSTEMS=SYS.ID_SRV "
										+ "and ROL.UP_IS= SYS.ID_SRV "
										+ "and (ROL.ID_SRV = URL.UP_ROLES or ROL.ID_SRV = LUGR.UP_ROLES ) "
										+ "and LUGU.UP_GROUP_USERS = LUGR.UP_GROUP_USERS(+) "
										+ "and LUGU.UP_USERS(+)  = AU.ID_SRV "
										+ "and URL.UP_USERS(+)  = AU.ID_SRV "
										+ "and AU.LOGIN= :login "
										+ "group by SYS.SIGN_OBJECT,  ROL.SIGN_OBJECT) "
										+ "order by sys_code ")
										*/
										
										"SELECT '[' || sys_code || ']' || role_code role_full_code " + 
										"    FROM (  SELECT SYS.SIGN_OBJECT sys_code, ROL.SIGN_OBJECT role_code " + 
										"              FROM GROUP_SYSTEMS_KNL_T gsys, " + 
										"                   AC_IS_BSS_T sys, " + 
										"                   AC_ROLES_BSS_T rol, " + 
										"                   LINK_GROUP_SYS_SYS_KNL_T lgr, " + 
										"                   AC_USERS_LINK_KNL_T url, " + 
										"                   AC_USERS_KNL_T AU " + 
										"             WHERE     GSYS.GROUP_CODE = :idIs " + 
										"                   AND GSYS.ID_SRV = LGR.UP_GROUP_SYSTEMS " + 
										"                   AND LGR.UP_SYSTEMS = SYS.ID_SRV " + 
										"                   AND ROL.UP_IS = SYS.ID_SRV " + 
										"                   AND ROL.ID_SRV = URL.UP_ROLES " + 
										"                   AND URL.UP_USERS  = AU.ID_SRV " + 
										"                   AND AU.LOGIN = :login " + 
										"          GROUP BY SYS.SIGN_OBJECT, ROL.SIGN_OBJECT " + 
										"          UNION ALL " + 
										"            SELECT SYS.SIGN_OBJECT sys_code, ROL.SIGN_OBJECT role_code " + 
										"              FROM GROUP_SYSTEMS_KNL_T gsys, " + 
										"                   AC_IS_BSS_T sys, " + 
										"                   AC_ROLES_BSS_T rol, " + 
										"                   LINK_GROUP_SYS_SYS_KNL_T lgr, " + 
										"                   LINK_GROUP_USERS_ROLES_KNL_T lugr, " + 
										"                   LINK_GROUP_USERS_USERS_KNL_T lugu, " + 
										"                   AC_USERS_KNL_T AU " + 
										"             WHERE     GSYS.GROUP_CODE = :idIs " + 
										"                   AND GSYS.ID_SRV = LGR.UP_GROUP_SYSTEMS " + 
										"                   AND LGR.UP_SYSTEMS = SYS.ID_SRV " + 
										"                   AND ROL.UP_IS = SYS.ID_SRV " + 
										"                   AND ROL.ID_SRV = LUGR.UP_ROLES " + 
										"                   AND LUGU.UP_GROUP_USERS = LUGR.UP_GROUP_USERS(+) " + 
										"                   AND LUGU.UP_USERS = AU.ID_SRV " + 
										"                   AND AU.LOGIN = :login " + 
										"          GROUP BY SYS.SIGN_OBJECT, ROL.SIGN_OBJECT) " + 
										"GROUP BY sys_code, role_code " + 
										"ORDER BY sys_code ")
										
										
						.setParameter("idIs", domain)
						.setParameter("login", login).getResultList();

			}

			if (result != null) {
				logger.info("rolesCodes:result:" + result.size());
			}
		} catch (Exception e) {
			logger.error("rolesCodes:error:" + e);
		}

		return result;
	}

	/**
	 * получение списка подсистем
	 */
	@SuppressWarnings("unchecked")
	public List<String> resources(String login, String domain) throws Exception {

		List<String> result = new ArrayList<String>();
		try {
			logger.info("resources:01:" + domain);

			if (domain != null
					&& (domain.startsWith(CUDConstants.armPrefix) || domain
							.startsWith(CUDConstants.subArmPrefix))) {

				// закомментированно, т.к. ресурсы отдаём
				// только для групп систем
				/*
				 * result = (List<String>) em.createNativeQuery(
				 * "select '['||sys_full.SIGN_OBJECT||']' || '['||sys_full.FULL_||']' || '['||sys_full.LINKS ||']' from AC_IS_BSS_T sys_full, "
				 * + " (select   SYS.ID_SRV sys_id " +
				 * "                     from  AC_IS_BSS_T sys,     " +
				 * "                              AC_ROLES_BSS_T rol,     " +
				 * "                              AC_USERS_LINK_KNL_T url,     "
				 * + "                              AC_USERS_KNL_T AU,     " +
				 * "                              AC_SUBSYSTEM_CERT_BSS_T subsys,   "
				 * +
				 * "                              LINK_GROUP_USERS_ROLES_KNL_T lugr,   "
				 * +
				 * "                              LINK_GROUP_USERS_USERS_KNL_T lugu     "
				 * +
				 * "                     where (SYS.SIGN_OBJECT= :idIs or  SUBSYS.SUBSYSTEM_CODE=:idIs)     "
				 * +
				 * "                           and (ROL.ID_SRV = URL.UP_ROLES or ROL.ID_SRV = LUGR.UP_ROLES )   "
				 * +
				 * "                           and LUGU.UP_GROUP_USERS= LUGR.UP_GROUP_USERS(+)   "
				 * +
				 * "                           and LUGU.UP_USERS(+)  = AU.ID_SRV   "
				 * +
				 * "                           and URL.UP_USERS(+)  = AU.ID_SRV   "
				 * + "                           and ROL.UP_IS=sys.ID_SRV    " +
				 * "                           and AU.LOGIN= :login     " +
				 * "                           and  SUBSYS.UP_IS(+) =SYS.ID_SRV     "
				 * + "                     group by SYS.ID_SRV  ) t1 " +
				 * "                     " +
				 * "                     where t1.sys_id = SYS_FULL.ID_SRV ")
				 * .setParameter("idIs", domain) .setParameter("login", login)
				 * .getResultList();
				 */

			} else if (domain != null
					&& domain.startsWith(CUDConstants.groupArmPrefix)) {

				result = (List<String>) em
						.createNativeQuery(
								" select '['||sys_full.SIGN_OBJECT||']' || '['||sys_full.FULL_||']' || '['|| sys_full.LINKS ||']' "
										+ "                         from AC_IS_BSS_T sys_full, (   "
										+ "                         select SYS.ID_SRV sys_id  "
										+ "                         from GROUP_SYSTEMS_KNL_T gsys,   "
										+ "                         AC_IS_BSS_T sys,   "
										+ "                          AC_ROLES_BSS_T rol,   "
										+ "                          LINK_GROUP_SYS_SYS_KNL_T lgr,   "
										+ "                          LINK_GROUP_USERS_ROLES_KNL_T lugr,   "
										+ "                          LINK_GROUP_USERS_USERS_KNL_T lugu,   "
										+ "                          AC_USERS_LINK_KNL_T url,   "
										+ "                          AC_USERS_KNL_T AU    "
										+ "                         where GSYS.GROUP_CODE=:idIs   "
										+ "                         and GSYS.ID_SRV=LGR.UP_GROUP_SYSTEMS   "
										+ "                         and LGR.UP_SYSTEMS=SYS.ID_SRV   "
										+ "                         and ROL.UP_IS= SYS.ID_SRV   "
										+ "                         and (ROL.ID_SRV = URL.UP_ROLES or ROL.ID_SRV = LUGR.UP_ROLES )   "
										+ "                         and LUGU.UP_GROUP_USERS = LUGR.UP_GROUP_USERS(+)   "
										+ "                         and LUGU.UP_USERS(+)  = AU.ID_SRV   "
										+ "                         and URL.UP_USERS(+)  = AU.ID_SRV   "
										+ "                         and AU.LOGIN= :login    "
										+ "                         group by SYS.ID_SRV )   t1 "
										+ "                          where t1.sys_id = SYS_FULL.ID_SRV "
										+ "                         order by sys_full.SIGN_OBJECT")
						.setParameter("idIs", domain)
						.setParameter("login", login).getResultList();

			}

			if (result != null) {
				logger.info("resources:result:" + result.size());
			}
		} catch (Exception e) {
			logger.error("resources:error:" + e);
			e.printStackTrace(System.out);
		}

		return result;
	}

	public void saveTokenID(String tokenID, String userID) throws Exception{
		
	 try{
		logger.error("saveTokenID:01");
		
		em.createNativeQuery(
				 "insert into TOKEN_KNL_T "
				 + "(ID_SRV, UP_USERS, SIGN_OBJECT,  CREATED ) "
				 + "values "
				 + "(TOKEN_KNL_SEQ.nextval, ?, ?, sysdate) ")
				 .setParameter(1, userID)
				 .setParameter(2, tokenID)
				 .executeUpdate();
	} catch (Exception e) {
		logger.error("saveTokenID:error:" + e);
	}
	}
}
