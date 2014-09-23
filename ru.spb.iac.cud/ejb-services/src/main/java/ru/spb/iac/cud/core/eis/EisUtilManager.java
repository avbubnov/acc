package ru.spb.iac.cud.core.eis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import java.util.ArrayList;

import ru.spb.iac.cud.core.UtilManagerLocal;
import ru.spb.iac.cud.core.UtilManagerRemote;
import ru.spb.iac.cud.core.util.CUDConstants;
import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.items.Attribute;
import ru.spb.iac.cud.items.Group;
import ru.spb.iac.cud.items.GroupsData;
import ru.spb.iac.cud.items.ISOrganisations;
import ru.spb.iac.cud.items.Resource;
import ru.spb.iac.cud.items.ResourceNU;
import ru.spb.iac.cud.items.ResourcesData;
import ru.spb.iac.cud.items.Role;
import ru.spb.iac.cud.items.User;
import ru.spb.iac.cud.items.UsersData;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EJB для справочного сервиса
 * 
 * @author bubnov
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class EisUtilManager implements UtilManagerLocal, UtilManagerRemote {

	@PersistenceContext(unitName = "AuthServices")
	EntityManager em;

	private static final Integer MAX_CONT_USERS = 50;

	private static final Integer MAX_CONT_GROUPS = 50;

	private static final Integer MAX_CONT_RES = 50;

	private static final String LINKS_SEPARATOR = ",";

	Logger logger = LoggerFactory.getLogger(EisUtilManager.class);

	public EisUtilManager() {
	}

	/**
	 * данные по пользователям
	 */
	public UsersData users_data(String idIS, List<String> uidsUsers,
			String category, List<String> rolesCodes, List<String> groupsCodes,
			Integer start, Integer count, Map<String, String> settings,
			Long idUserAuth, String IPAddress) throws GeneralFailure {

		logger.info("users_data:01");

		// onlyISUsers условие сильнее, чем rolesCodes
		// то есть, если стоит onlyISUsers = false [все пользователи],
		// то rolesCodes уже не учитывается

		Integer result_count = 0;
		List<User> result = new ArrayList<User>();
		Map<String, User> result_ids = new HashMap<String, User>();
		User uat = null;
		List<Attribute> atlist = null;
		Attribute at = null;

		List<Object[]> lo = null;
		String uid = null;
		// Long id_rec = null;
		String id_rec = null;

		UsersData isu = new UsersData();

		String rolesLine = null;
		String groupsLine = null;
		String usersIdsLine = null;
	
		try {

			if (idIS == null) {
				logger.info("users_data:return:1");
				throw new GeneralFailure("idIS is null!");
			}

			if (count == null) {
				count = MAX_CONT_USERS;
			}
			if (start == null) {
				start = 0;
			}

			if (MAX_CONT_USERS < count) {
				logger.info("users_data:return:2");
				throw new GeneralFailure("'count' should be less than "
						+ MAX_CONT_USERS);
			}

			logger.info("users_data:idIS1:" + idIS);

			String uidsLine = null;

			if (uidsUsers != null && !uidsUsers.isEmpty()) {

				for (String uid_user : uidsUsers) {
					if (uidsLine == null) {
						uidsLine = "'" + uid_user + "'";
					} else {
						uidsLine = uidsLine + ", '" + uid_user + "'";
					}
				}
			}

			if (CUDConstants.categorySYS.equals(category) && rolesCodes != null
					&& !rolesCodes.isEmpty()) {

				for (String role : rolesCodes) {
					if (rolesLine == null) {
						rolesLine = "'" + role + "'";
					} else {
						rolesLine = rolesLine + ", '" + role + "'";
					}
				}
			}

			if (/* CUDConstants.categorySYS.equals(category)&& */
			groupsCodes != null && !groupsCodes.isEmpty()) {

				for (String group : groupsCodes) {
					if (groupsLine == null) {
						groupsLine = "'" + group + "'";
					} else {
						groupsLine = groupsLine + ", '" + group + "'";
					}
				}
			}

			if (idIS.startsWith(CUDConstants.armPrefix)
					|| idIS.startsWith(CUDConstants.subArmPrefix)) {

				// !!!
				idIS = get_code_is(idIS);

				logger.info("is_users:idIS2:" + idIS);

				String filterSt = null;

				if (rolesLine != null && groupsLine != null) {
					filterSt = "and( " + "((ROL.SIGN_OBJECT IN (" + rolesLine
							+ ") and ais.SIGN_OBJECT = :idIS)  "
							+ "   or (ROL_group.SIGN_OBJECT IN (" + rolesLine
							+ " )  and ais_group.SIGN_OBJECT = :idIS) ) "
							+ "or (GR.SIGN_OBJECT IN (" + groupsLine + ") ) "
							+ ") ";
				} else if (rolesLine != null) {

					filterSt = "and " + "((ROL.SIGN_OBJECT IN (" + rolesLine
							+ ") and ais.SIGN_OBJECT = :idIS) "
							+ "   or (ROL_group.SIGN_OBJECT IN (" + rolesLine
							+ " )  and ais_group.SIGN_OBJECT = :idIS) ) ";
				} else if (groupsLine != null) {
					filterSt = "and " + "GR.SIGN_OBJECT IN (" + groupsLine
							+ ") ";
				}

				// системы и подсистемы
				// 1.пользователи

				lo = em.createNativeQuery(
						"select t1.t1_id, t1.t1_login, t1.t1_cert, t1.t1_usr_code, t1.t1_fio, t1.t1_tel, t1.t1_email,t1.t1_pos, t1.t1_dep_name, "
								+ "t1.t1_org_code, t1.t1_org_name, t1.t1_org_adr, t1.t1_org_tel, t1.t1_start, t1.t1_end, t1.t1_status, "
								+ "t1.t1_crt_date, t1.t1_crt_usr_login, t1.t1_upd_date, t1.t1_upd_usr_login, "
								+ "t1.t1_dep_code, t1.t1_org_status, t1.t1_usr_status, t1.t1_dep_status, t1.t1_org_okato, t1.t1_dep_adr "
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
								+ "CL_ORG_FULL.SIGN_OKATO t1_org_okato, CL_DEP_FULL.PREFIX || decode(CL_DEP_FULL.HOUSE, null, null, ','  ||CL_DEP_FULL.HOUSE  ) t1_dep_adr  "
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
								+ "AC_USERS_KNL_T au  "
								+ "where substr(au.UP_SIGN_USER,1,5)||'000'  =cl_dep.SIGN_OBJECT(+) "
								+ "group by CL_DEP.SIGN_OBJECT) t3, "
								+ "( "
								+
								// core-1-
								"select t_core.t_core_id "
								+ "from( "
								+ "select AU_FULL.ID_SRV t_core_id "
								+ "from "
								+ "AC_USERS_KNL_T au_full, "
								+ "LINK_GROUP_USERS_USERS_KNL_T lgu, "
								+ "AC_USERS_LINK_KNL_T lur, "
								+ "AC_ROLES_BSS_T rol, "
								+ "AC_ROLES_BSS_T rol_group, "
								+ "LINK_GROUP_USERS_ROLES_KNL_T lgr, "
								+ "AC_IS_BSS_T ais, "
								+ "AC_IS_BSS_T ais_group, "
								+ "GROUP_USERS_KNL_T gr "
								+ "where "
								+ "lur.UP_USERS(+)=AU_FULL.ID_SRV and "
								+ "lgu.UP_USERS(+)=AU_FULL.ID_SRV "
								+ "and ROL.ID_SRV(+)=LUR.UP_ROLES "
								+
								// "and lgr.UP_GROUP_USERS(+)=lgu.UP_GROUP_USERS "
								// +

								"and lgr.UP_GROUP_USERS(+)=GR.ID_SRV "
								+ "and lgu.UP_GROUP_USERS=GR.ID_SRV(+) "
								+

								"and rol_group.ID_SRV(+) =LGR.UP_ROLES "
								+ "and ROL.UP_IS=ais.ID_SRV(+) "
								+ "and ROL_group.UP_IS=ais_group.ID_SRV(+) "
								+ "and (ais.SIGN_OBJECT  = :idIS "
								+ "or ais_group.SIGN_OBJECT = :idIS "
								+ "or -1 = :onlyISUsers ) "
								+

								(uidsLine != null ? "and to_char(au_full.ID_SRV) in ("
										+ uidsLine + ") "
										: " ")
								+

								(filterSt != null ? filterSt : " ")
								+

								"order by t_core_id "
								+ ")t_core "
								+ "group by t_core_id "
								+ "order by t_core.t_core_id "
								+

								// core-2-
								") t4, "
								+

								"ISP_BSS_T cl_org_full, "
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
								+ "and AU_FULL.ID_SRV=t4.t_core_id "
								+ ")t1 "
								+ "order by t1_fio ")
						.setParameter("idIS", idIS)
						.setParameter(
								"onlyISUsers",
								(CUDConstants.categorySYS.equals(category) ? 1
										: -1))
						.setFirstResult(start != null ? start.intValue() : 0)
						.setMaxResults(
								count != null ? count.intValue() : 1000000)
						.getResultList();

				for (Object[] objectArray : lo) {

					// uid=objectArray[1].toString();
					// !!!
					// Проверить
					uid = objectArray[0].toString();

					id_rec = objectArray[0].toString();

					uat = new User();
					atlist = new ArrayList<Attribute>();
					String name = null;

					for (int i = 0; i < objectArray.length; i++) {

						at = new Attribute();

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

						default:
							name = null;
						}

						if (name != null) {

							at.setName(name);
							at.setValue((objectArray[i] != null ? objectArray[i]
									.toString() : ""));

							atlist.add(at);
						}
					}

					uat.setUid(uid);
					uat.setAttributes(atlist);

					result.add(uat); // для сохранения сортировки из запроса
					result_ids.put(id_rec, uat);

					if (usersIdsLine == null) {
						usersIdsLine = "'" + id_rec + "'";
					} else {
						usersIdsLine = usersIdsLine + ", '" + id_rec + "'";
					}
				}

				logger.info("users_data:02");

				// 2.роли

				lo = em.createNativeQuery(
						"select AU.ID_SRV, ROL.SIGN_OBJECT "
								+ "from  AC_IS_BSS_T sys, "
								+ "        AC_ROLES_BSS_T rol, "
								+ "        AC_USERS_LINK_KNL_T url, "
								+ "        AC_USERS_KNL_T AU, "
								+ "        AC_SUBSYSTEM_CERT_BSS_T subsys, "
								+ "        LINK_GROUP_USERS_ROLES_KNL_T lugr, "
								+ "        LINK_GROUP_USERS_USERS_KNL_T lugu "
								+ "where (SYS.SIGN_OBJECT= :idIS or  SUBSYS.SUBSYSTEM_CODE=:idIS) "
								+ "     and (ROL.ID_SRV = URL.UP_ROLES or ROL.ID_SRV = LUGR.UP_ROLES )  "
								+ "     and LUGU.UP_GROUP_USERS= LUGR.UP_GROUP_USERS(+)  "
								+ "     and LUGU.UP_USERS(+)  = AU.ID_SRV  "
								+ "     and URL.UP_USERS(+)  = AU.ID_SRV "
								+ "     and ROL.UP_IS=sys.ID_SRV   "
								+ "     and AU.ID_SRV IN(" + usersIdsLine
								+ ")  "
								+ "     and  SUBSYS.UP_IS(+) =SYS.ID_SRV  "
								+ "group by AU.ID_SRV, ROL.SIGN_OBJECT  "
								+ "order by AU.ID_SRV, ROL.SIGN_OBJECT ")
						.setParameter("idIS", idIS).getResultList();

				for (Object[] objectArray : lo) {

					if (result_ids.get(objectArray[0].toString())
							.getCodesRoles() == null) {
						result_ids.get(objectArray[0].toString())
								.setCodesRoles(new ArrayList<String>());
					}

					result_ids.get(objectArray[0].toString()).getCodesRoles()
							.add(objectArray[1].toString());
				}

				// 3.группы

				lo = em.createNativeQuery(
						"select LGU.UP_USERS, GR.SIGN_OBJECT "
								+ "from  GROUP_USERS_KNL_T gr, "
								+ " LINK_GROUP_USERS_USERS_KNL_T lgu "
								+ " where LGU.UP_GROUP_USERS=GR.ID_SRV "
								+ " and  LGU.UP_USERS in (" + usersIdsLine
								+ ") "
								+ " order by LGU.UP_USERS, GR.SIGN_OBJECT ")
						.getResultList();

				for (Object[] objectArray : lo) {

					if (result_ids.get(objectArray[0].toString())
							.getCodesGroups() == null) {
						result_ids.get(objectArray[0].toString())
								.setCodesGroups(new ArrayList<String>());
					}

					if(objectArray[1]!=null){
					  result_ids.get(objectArray[0].toString()).getCodesGroups()
							.add(objectArray[1].toString());
					}

				}

				// 4.количество

				logger.info("users_data:03");

				result_count = ((java.math.BigDecimal) em
						.createNativeQuery(
								"select count(*) from ( "
										+ "select t_core.t_core_id "
										+ "from( "
										+ "select AU_FULL.ID_SRV t_core_id "
										+ "from "
										+ "AC_USERS_KNL_T au_full, "
										+ "LINK_GROUP_USERS_USERS_KNL_T lgu, "
										+ "AC_USERS_LINK_KNL_T lur, "
										+ "AC_ROLES_BSS_T rol, "
										+ "AC_ROLES_BSS_T rol_group, "
										+ "LINK_GROUP_USERS_ROLES_KNL_T lgr, "
										+ "AC_IS_BSS_T ais, "
										+ "AC_IS_BSS_T ais_group, "
										+ "GROUP_USERS_KNL_T gr "
										+ "where "
										+ "lur.UP_USERS(+)=AU_FULL.ID_SRV and "
										+ "lgu.UP_USERS(+)=AU_FULL.ID_SRV "
										+ "and ROL.ID_SRV(+)=LUR.UP_ROLES "
										+
										// "and lgr.UP_GROUP_USERS(+)=lgu.UP_GROUP_USERS "
										// +

										"and lgr.UP_GROUP_USERS(+)=GR.ID_SRV "
										+ "and lgu.UP_GROUP_USERS=GR.ID_SRV(+) "
										+

										"and rol_group.ID_SRV(+) =LGR.UP_ROLES "
										+ "and ROL.UP_IS=ais.ID_SRV(+) "
										+ "and ROL_group.UP_IS=ais_group.ID_SRV(+) "
										+ "and (ais.SIGN_OBJECT  = :idIS "
										+ "or ais_group.SIGN_OBJECT = :idIS "
										+ "or -1 = :onlyISUsers ) "
										+

										(uidsLine != null ? "and to_char(au_full.ID_SRV) in ("
												+ uidsLine + ") "
												: " ") +

										(filterSt != null ? filterSt : " ") +

										"order by t_core_id " + ")t_core "
										+ "group by t_core_id "
										+ "order by t_core.t_core_id )")
						.setParameter("idIS", idIS)
						.setParameter(
								"onlyISUsers",
								(CUDConstants.categorySYS.equals(category) ? 1
										: -1)).getSingleResult()).intValue();

			} else if (idIS.startsWith(CUDConstants.groupArmPrefix)) {

				String filterSt = null;

				if (rolesLine != null && groupsLine != null) {
					filterSt = "and( " + "((ROL.SIGN_OBJECT IN (" + rolesLine
							+ ") and ais.SIGN_OBJECT = :idIS)  "
							+ "   or (ROL_group.SIGN_OBJECT IN (" + rolesLine
							+ " )  and LINKSYSGROUP.UP_SYSTEMS=AIS.ID_SRV) ) "
							+ "or (GR.SIGN_OBJECT IN (" + groupsLine + ") ) "
							+ ") ";
				} else if (rolesLine != null) {

					filterSt = "and " + "((ROL.SIGN_OBJECT IN (" + rolesLine
							+ ") and LINKSYSGROUP.UP_SYSTEMS=AIS.ID_SRV) "
							+ "   or (ROL_group.SIGN_OBJECT IN (" + rolesLine
							+ " ) and  LINKSYSGROUP.UP_SYSTEMS=AIS.ID_SRV) ) ";
				} else if (groupsLine != null) {
					filterSt = "and " + "GR.SIGN_OBJECT IN (" + groupsLine
							+ ") ";
				}

				// группы
				// 1. пользователи

				logger.info("users_data:05+");

				lo = em.createNativeQuery(
						"select t1.t1_id, t1.t1_login, t1.t1_cert, t1.t1_usr_code, t1.t1_fio, t1.t1_tel, t1.t1_email,t1.t1_pos, t1.t1_dep_name,  "
								+ "                        t1.t1_org_code, t1.t1_org_name, t1.t1_org_adr, t1.t1_org_tel, t1.t1_start, t1.t1_end, t1.t1_status,  "
								+ "                         t1.t1_crt_date, t1.t1_crt_usr_login, t1.t1_upd_date, t1.t1_upd_usr_login,  "
								+ "                         t1.t1_dep_code, t1.t1_org_status, t1.t1_usr_status, t1.t1_dep_status, t1.t1_org_okato, t1.t1_dep_adr   "
								+ "                        from( "
								+ "                        select AU_FULL.ID_SRV t1_id, AU_FULL.login t1_login, AU_FULL.CERTIFICATE t1_cert, t2.CL_USR_CODE t1_usr_code,  "
								+ "                         decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.SURNAME||' '||AU_FULL.NAME_ ||' '|| AU_FULL.PATRONYMIC,  CL_USR_FULL.FIO ) t1_fio,    "
								+ "                          decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.PHONE, CL_USR_FULL.PHONE ) t1_tel,     "
								+ "                          decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.E_MAIL, CL_USR_FULL.EMAIL) t1_email,    "
								+ "                          decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.POSITION, CL_USR_FULL.POSITION)t1_pos,    "
								+ "                          decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.DEPARTMENT, decode(substr(CL_DEP_FULL.sign_object,4,2), '00', null, CL_DEP_FULL.FULL_)) t1_dep_name,   "
								+ "                          t1.CL_ORG_CODE t1_org_code, CL_ORG_FULL.FULL_ t1_org_name,  "
								+ "                          CL_ORG_FULL.PREFIX || decode(CL_ORG_FULL.HOUSE, null, null, ','  ||CL_ORG_FULL.HOUSE  ) t1_org_adr,  "
								+ "                          CL_ORG_FULL.PHONE t1_org_tel,  "
								+ "                          to_char(AU_FULL.START_ACCOUNT, 'DD.MM.YY HH24:MI:SS') t1_start,   "
								+ "                          to_char(AU_FULL.END_ACCOUNT, 'DD.MM.YY HH24:MI:SS') t1_end,    "
								+ "                          AU_FULL.STATUS t1_status,    "
								+ "                          AU_FULL.CREATED t1_crt_date,   "
								+ "                          USR_CRT.LOGIN t1_crt_usr_login,   "
								+ "                          to_char(AU_FULL.MODIFIED, 'DD.MM.YY HH24:MI:SS') t1_upd_date,   "
								+ "                          USR_UPD.LOGIN t1_upd_usr_login,   "
								+ "                          decode(AU_FULL.UP_SIGN_USER, null, null, decode(substr(CL_DEP_FULL.sign_object,4,2), '00', null, CL_DEP_FULL.sign_object)) t1_dep_code,   "
								+ "                          CL_ORG_FULL.STATUS t1_org_status,  CL_usr_FULL.STATUS t1_usr_status,   "
								+ "                          decode(AU_FULL.UP_SIGN_USER, null, null, decode(substr(CL_DEP_FULL.sign_object,4,2), '00', null, CL_DEP_FULL.STATUS)) t1_dep_status,  "
								+ "                          CL_ORG_FULL.SIGN_OKATO t1_org_okato, CL_DEP_FULL.PREFIX || decode(CL_DEP_FULL.HOUSE, null, null, ','  ||CL_DEP_FULL.HOUSE  ) t1_dep_adr        "
								+ "                        from  "
								+ "                        (select max(CL_ORG.ID_SRV) CL_ORG_ID,  CL_ORG.SIGN_OBJECT  CL_ORG_CODE  "
								+ "                        from ISP_BSS_T cl_org,  "
								+ "                        AC_USERS_KNL_T au  "
								+ "                        where AU.UP_SIGN = CL_ORG.SIGN_OBJECT  "
								+ "                        group by CL_ORG.SIGN_OBJECT) t1,  "
								+ "                        (select max(CL_usr.ID_SRV) CL_USR_ID,  CL_USR.SIGN_OBJECT  CL_USR_CODE  "
								+ "                        from ISP_BSS_T cl_usr,  "
								+ "                        AC_USERS_KNL_T au  "
								+ "                        where AU.UP_SIGN_USER  = CL_usr.SIGN_OBJECT  "
								+ "                        group by CL_usr.SIGN_OBJECT) t2,  "
								+ "                        (select max(CL_dep.ID_SRV) CL_DEP_ID,  CL_DEP.SIGN_OBJECT  CL_DEP_CODE  "
								+ "                        from ISP_BSS_T cl_dep,  "
								+ "                        AC_USERS_KNL_T au  "
								+ "                        where substr(au.UP_SIGN_USER,1,5)||'000'  =cl_dep.SIGN_OBJECT(+)  "
								+ "                        group by CL_DEP.SIGN_OBJECT) t3,  "
								+ "                        (  "
								+ "                        "
								+ "                       select t_core.t_core_id  "
								+ "                        from(   "
								+ "                        select AU_FULL.ID_SRV t_core_id   "
								+ "                        from   "
								+ "                        AC_USERS_KNL_T au_full,   "
								+ "                        LINK_GROUP_USERS_USERS_KNL_T lgu,   "
								+ "                       AC_USERS_LINK_KNL_T lur,  "
								+ "                        AC_ROLES_BSS_T rol,  "
								+ "                        AC_ROLES_BSS_T rol_group,  "
								+ "                        LINK_GROUP_USERS_ROLES_KNL_T lgr,  "
								+ "                        AC_IS_BSS_T ais,  "
								+ "                        AC_IS_BSS_T ais_group, "
								+ "                        GROUP_SYSTEMS_KNL_T sysgroup, "
								+ "                        LINK_GROUP_SYS_SYS_KNL_T linksysgroup, "
								+ "                        GROUP_USERS_KNL_T gr "
								+ "                        where  "
								+ "                        lur.UP_USERS(+)=AU_FULL.ID_SRV and  "
								+ "                        lgu.UP_USERS(+)=AU_FULL.ID_SRV  "
								+ "                        and ROL.ID_SRV(+)=LUR.UP_ROLES  "
								+
								// "                        and lgr.UP_GROUP_USERS(+)=lgu.UP_GROUP_USERS  "
								// +

								"and lgr.UP_GROUP_USERS(+)=GR.ID_SRV "
								+ "and lgu.UP_GROUP_USERS=GR.ID_SRV(+) "
								+

								"                        and rol_group.ID_SRV(+) =LGR.UP_ROLES  "
								+ "                        and ROL.UP_IS=ais.ID_SRV(+)   "
								+ "                        and ROL_group.UP_IS=ais_group.ID_SRV(+)   "
								+ "                       and SYSGROUP.ID_SRV = LINKSYSGROUP.UP_GROUP_SYSTEMS "
								+ "                       and ( "
								+ "                        (LINKSYSGROUP.UP_SYSTEMS=AIS.ID_SRV "
								+ "                         or LINKSYSGROUP.UP_SYSTEMS=AIS_GROUP.ID_SRV) "
								+ "                         and SYSGROUP.GROUP_CODE = :idIS "
								+ "                         or -1 = :onlyISUsers   "
								+ "                        ) "
								+

								(uidsLine != null ? "and to_char(au_full.ID_SRV) in ("
										+ uidsLine + ") "
										: " ")
								+

								(filterSt != null ? filterSt : " ")
								+

								"                      order by t_core_id  "
								+ "                      )t_core   "
								+ "                       group by t_core_id  "
								+ "                       order by t_core.t_core_id  "
								+ "                         "
								+ "                        ) t4, "
								+ "                         "
								+ "                        ISP_BSS_T cl_org_full,  "
								+ "                        ISP_BSS_T cl_usr_full,  "
								+ "                        ISP_BSS_T cl_dep_full,  "
								+ "                        AC_USERS_KNL_T au_full,  "
								+ "                        AC_USERS_KNL_T usr_crt,    "
								+ "                        AC_USERS_KNL_T usr_upd "
								+ "                        where cl_org_full.ID_SRV= CL_ORG_ID  "
								+ "                        and cl_usr_full.ID_SRV(+)=CL_USR_ID "
								+ "                        and cl_DEP_full.ID_SRV(+)=CL_DEP_ID  "
								+ "                        and au_full.UP_SIGN = CL_ORG_CODE  "
								+ "                        and au_full.UP_SIGN_USER  =  CL_USR_CODE(+)  "
								+ "                        and substr(au_full.UP_SIGN_USER,1,5)||'000'  =  CL_DEP_CODE(+)  "
								+ "                        and au_full.CREATOR=USR_CRT.ID_SRV   "
								+ "                        and au_full.MODIFICATOR=USR_UPD.ID_SRV(+)   "
								+ "                        and AU_FULL.ID_SRV=t4.t_core_id "
								+ "                        )t1  "
								+ "                        order by t1_fio   ")
						.setParameter("idIS", idIS)
						.setParameter(
								"onlyISUsers",
								(CUDConstants.categorySYS.equals(category) ? 1
										: -1))
						.setFirstResult(start != null ? start.intValue() : 0)
						.setMaxResults(
								count != null ? count.intValue() : 1000000)
						.getResultList();

				for (Object[] objectArray : lo) {

					uid = objectArray[1].toString();

					id_rec = objectArray[0].toString();

					uat = new User();
					atlist = new ArrayList<Attribute>();
					String name = null;

					for (int i = 0; i < objectArray.length; i++) {

						at = new Attribute();

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

						default:
							name = null;
						}

						if (name != null) {

							at.setName(name);
							at.setValue((objectArray[i] != null ? objectArray[i]
									.toString() : ""));

							atlist.add(at);
						}
					}

					uat.setUid(uid);
					uat.setAttributes(atlist);

					result.add(uat); // для сохранения сортировки из запроса
					result_ids.put(id_rec, uat);

					if (usersIdsLine == null) {
						usersIdsLine = "'" + id_rec + "'";
					} else {
						usersIdsLine = usersIdsLine + ", '" + id_rec + "'";
					}
				}

				logger.info("users_data:04");

				// 2. роли

				lo = em.createNativeQuery(
						"SELECT user_id, '[' || sys_code || ']' || role_code role_full_code "
								+ "FROM (  SELECT AU.ID_SRV user_id, SYS.SIGN_OBJECT sys_code, ROL.SIGN_OBJECT role_code "
								+ "FROM GROUP_SYSTEMS_KNL_T gsys, "
								+ "    AC_IS_BSS_T sys, "
								+ "    AC_ROLES_BSS_T rol, "
								+ "    LINK_GROUP_SYS_SYS_KNL_T lgr, "
								+ "    LINK_GROUP_USERS_ROLES_KNL_T lugr, "
								+ "    LINK_GROUP_USERS_USERS_KNL_T lugu, "
								+ "    AC_USERS_LINK_KNL_T url, "
								+ "    AC_USERS_KNL_T AU "
								+ "WHERE     GSYS.GROUP_CODE = :idIS "
								+ "    AND GSYS.ID_SRV = LGR.UP_GROUP_SYSTEMS "
								+ "    AND LGR.UP_SYSTEMS = SYS.ID_SRV "
								+ "    AND ROL.UP_IS = SYS.ID_SRV "
								+ "    AND (ROL.ID_SRV = URL.UP_ROLES OR ROL.ID_SRV = LUGR.UP_ROLES) "
								+ "    AND LUGU.UP_GROUP_USERS = LUGR.UP_GROUP_USERS(+) "
								+ "    AND LUGU.UP_USERS(+) = AU.ID_SRV "
								+ "    AND URL.UP_USERS(+) = AU.ID_SRV "
								+ "     and AU.ID_SRV IN("
								+ usersIdsLine
								+ ")  "
								+ "GROUP BY AU.ID_SRV, SYS.SIGN_OBJECT, ROL.SIGN_OBJECT) "
								+ "ORDER BY user_id, sys_code ")
						.setParameter("idIS", idIS).getResultList();

				for (Object[] objectArray : lo) {

					if (result_ids.get(objectArray[0].toString())
							.getCodesRoles() == null) {
						result_ids.get(objectArray[0].toString())
								.setCodesRoles(new ArrayList<String>());
					}

					result_ids.get(objectArray[0].toString()).getCodesRoles()
							.add(objectArray[1].toString());
				}

				// 3.группы

				lo = em.createNativeQuery(
						"select LGU.UP_USERS, GR.SIGN_OBJECT "
								+ "from  GROUP_USERS_KNL_T gr, "
								+ " LINK_GROUP_USERS_USERS_KNL_T lgu "
								+ " where LGU.UP_GROUP_USERS=GR.ID_SRV "
								+ " and  LGU.UP_USERS in (" + usersIdsLine
								+ ") "
								+ " order by LGU.UP_USERS, GR.SIGN_OBJECT ")
						.getResultList();

				for (Object[] objectArray : lo) {

					if (result_ids.get(objectArray[0].toString())
							.getCodesGroups() == null) {
						result_ids.get(objectArray[0].toString())
								.setCodesGroups(new ArrayList<String>());
					}
					
					if(objectArray[1]!=null){
					  result_ids.get(objectArray[0].toString()).getCodesGroups()
							.add(objectArray[1].toString());
					}
				}

				// 4. количество

				logger.info("users_data:05");

				result_count = ((java.math.BigDecimal) em
						.createNativeQuery(
								"select count(*) from ( "
										+ "select t_core.t_core_id "
										+ "from( "
										+ "select AU_FULL.ID_SRV t_core_id "
										+ "from "
										+ "AC_USERS_KNL_T au_full, "
										+ "LINK_GROUP_USERS_USERS_KNL_T lgu, "
										+ "AC_USERS_LINK_KNL_T lur, "
										+ "AC_ROLES_BSS_T rol, "
										+ "AC_ROLES_BSS_T rol_group, "
										+ "LINK_GROUP_USERS_ROLES_KNL_T lgr, "
										+ "AC_IS_BSS_T ais, "
										+ "AC_IS_BSS_T ais_group, "
										+ "GROUP_SYSTEMS_KNL_T sysgroup, "
										+ "LINK_GROUP_SYS_SYS_KNL_T linksysgroup, "
										+ "GROUP_USERS_KNL_T gr "
										+ "where "
										+ "lur.UP_USERS(+)=AU_FULL.ID_SRV and "
										+ "lgu.UP_USERS(+)=AU_FULL.ID_SRV "
										+ "and ROL.ID_SRV(+)=LUR.UP_ROLES "
										+
										// "and lgr.UP_GROUP_USERS(+)=lgu.UP_GROUP_USERS "
										// +

										"and lgr.UP_GROUP_USERS(+)=GR.ID_SRV "
										+ "and lgu.UP_GROUP_USERS=GR.ID_SRV(+) "
										+

										"and rol_group.ID_SRV(+) =LGR.UP_ROLES "
										+ "and ROL.UP_IS=ais.ID_SRV(+) "
										+ "and ROL_group.UP_IS=ais_group.ID_SRV(+) "
										+ "and SYSGROUP.ID_SRV = LINKSYSGROUP.UP_GROUP_SYSTEMS "
										+ "and ( "
										+ "(LINKSYSGROUP.UP_SYSTEMS=AIS.ID_SRV "
										+ "or LINKSYSGROUP.UP_SYSTEMS=AIS_GROUP.ID_SRV) "
										+ "and SYSGROUP.GROUP_CODE = :idIS "
										+ "or -1 = :onlyISUsers   "
										+ ") "
										+

										(uidsLine != null ? "and to_char(au_full.ID_SRV) in ("
												+ uidsLine + ") "
												: " ") +

										(filterSt != null ? filterSt : " ") +

										"order by t_core_id " + ")t_core "
										+ "group by t_core_id "
										+ "order by t_core.t_core_id )")
						.setParameter("idIS", idIS)
						.setParameter(
								"onlyISUsers",
								(CUDConstants.categorySYS.equals(category) ? 1
										: -1)).getSingleResult()).intValue();

			}

			// isu.setUserAttributesRoles(new
			// ArrayList<UserAttributesRoles>(result_ids.values()));
			isu.setUsers(result);
			isu.setCount(result_count);

			sys_audit(72L, /* "token:"+subject.getId()+ */"idIS:" + idIS
					+ "result_count:" + result_count, "true; ", IPAddress,
					idUserAuth);

		} catch (Exception e) {
			sys_audit(72L, /* "token:"+subject.getId()+ */"; idIS:" + idIS,
					"error", IPAddress, idUserAuth);

			logger.error("users_data+:Error:" + e);
			
			e.printStackTrace(System.out);
			
			throw new GeneralFailure(e.getMessage());
			
			
			
		}
		return isu;
		// return new ArrayList<UserAttributes>(result.values());
	}

	/**
	 * данные по группам
	 */
	public GroupsData groups_data(String idIS, List<String> groupsCodes,
			String category, List<String> rolesCodes, Integer start,
			Integer count, Map<String, String> settings, Long idUserAuth,
			String IPAddress) throws GeneralFailure {

		logger.info("groups_data:01");

		// onlyISUsers условие сильнее, чем rolesCodes
		// то есть, если стоит onlyISUsers = false [все пользователи],
		// то rolesCodes уже не учитывается

		// category:
		// ALL
		// SYS

		// + USER

		Integer result_count = 0;
		List<Group> result = new ArrayList<Group>();
		Map<String, Group> result_ids = new HashMap<String, Group>();
		Group uat = null;

		List<Object[]> lo = null;
		String id_rec = null;

		GroupsData isu = new GroupsData();

		String rolesLine = null;
		String usersIdsLine = null;
	
		try {

			if (idIS == null) {
				logger.info("groups_data:return:1");
				throw new GeneralFailure("idIS is null!");
			}

			if (count == null) {
				count = MAX_CONT_GROUPS;
			}
			if (start == null) {
				start = 0;
			}

			if (MAX_CONT_GROUPS < count) {
				logger.info("groups_data:return:2");
				throw new GeneralFailure("'count' should be less than "
						+ MAX_CONT_GROUPS);
			}

			logger.info("groups_data:idIS1:" + idIS);

			String uidsLine = null;

			if (groupsCodes != null && !groupsCodes.isEmpty()) {

				for (String uid_user : groupsCodes) {
					if (uidsLine == null) {
						uidsLine = "'" + uid_user + "'";
					} else {
						uidsLine = uidsLine + ", '" + uid_user + "'";
					}
				}
			}

			if (CUDConstants.categorySYS.equals(category) && rolesCodes != null
					&& !rolesCodes.isEmpty()) {

				for (String role : rolesCodes) {
					if (rolesLine == null) {
						rolesLine = "'" + role + "'";
					} else {
						rolesLine = rolesLine + ", '" + role + "'";
					}
				}

			}

			if (idIS.startsWith(CUDConstants.armPrefix)
					|| idIS.startsWith(CUDConstants.subArmPrefix)) {

				// !!!
				idIS = get_code_is(idIS);

				logger.info("groups_data:idIS2:" + idIS);

				// системы и подсистемы
				// 1.группы

				if (!CUDConstants.categoryUSER.equals(category)) {
					// от системы группы или весь список групп

					lo = em.createNativeQuery(

							"select gr_full.ID_SRV, gr_full.SIGN_OBJECT, gr_full.FULL_, gr_full.DESCRIPTION  "
									+ "from GROUP_USERS_KNL_T gr_full, "
									+ "( "
									+ "select GR.ID_SRV gr_id "
									+ "from GROUP_USERS_KNL_T gr, "
									+ "LINK_GROUP_USERS_ROLES_KNL_T lgr, "
									+ "AC_ROLES_BSS_T rol, "
									+ "AC_IS_BSS_T sys "
									+ "where GR.ID_SRV = LGR.UP_GROUP_USERS(+) "
									+ "and ROL.ID_SRV(+) = LGR.UP_ROLES "
									+ "and SYS.ID_SRV(+)=ROL.UP_IS "
									+ " "
									+ "and( SYS.SIGN_OBJECT = :idIS or -1 = :onlyISUsers ) "
									+

									(uidsLine != null ? "and GR.SIGN_OBJECT in ("
											+ uidsLine + ") "
											: " ")
									+

									(rolesLine != null ? "and (ROL.SIGN_OBJECT in ("
											+ rolesLine
											+ ") and SYS.SIGN_OBJECT = :idIS ) "
											: " ")
									+

									"group by GR.ID_SRV "
									+ ") "
									+ "where gr_id=gr_full.ID_SRV "
									+ "order by gr_full.FULL_ ")

							.setParameter("idIS", idIS)
							.setParameter(
									"onlyISUsers",
									(CUDConstants.categorySYS.equals(category) ? 1
											: -1))
							.setFirstResult(
									start != null ? start.intValue() : 0)
							.setMaxResults(
									count != null ? count.intValue() : 1000000)
							.getResultList();

				} else {
					// группы текущего пользователя

					lo = em.createNativeQuery(
							"select gr_full.ID_SRV, gr_full.SIGN_OBJECT, gr_full.FULL_, gr_full.DESCRIPTION  "
									+ "from GROUP_USERS_KNL_T gr_full, "
									+ "( "
									+ "select GR.ID_SRV gr_id "
									+ "from GROUP_USERS_KNL_T gr, "
									+ "LINK_GROUP_USERS_ROLES_KNL_T lgr, "
									+ "AC_ROLES_BSS_T rol, "
									+ "AC_IS_BSS_T sys, "
									+ "LINK_GROUP_USERS_USERS_KNL_T guu "
									+ "where GR.ID_SRV = LGR.UP_GROUP_USERS(+) "
									+ "and ROL.ID_SRV(+) = LGR.UP_ROLES "
									+ "and SYS.ID_SRV(+)=ROL.UP_IS "
									+ " "
									+ "and( SYS.SIGN_OBJECT = :idIS or -1 = :onlyISUsers ) "
									+ "and guu.UP_GROUP_USERS = GR.ID_SRV "
									+ "and guu.UP_USERS = :idUser "
									+

									(uidsLine != null ? "and GR.SIGN_OBJECT in ("
											+ uidsLine + ") "
											: " ")
									+

									"group by GR.ID_SRV "
									+ ") "
									+ "where gr_id=gr_full.ID_SRV "
									+ "order by gr_full.FULL_ ")

							.setParameter("idIS", idIS)
							.setParameter("onlyISUsers", 1)
							.setParameter("idUser", idUserAuth)
							.setFirstResult(
									start != null ? start.intValue() : 0)
							.setMaxResults(
									count != null ? count.intValue() : 1000000)
							.getResultList();

				}

				for (Object[] objectArray : lo) {

					id_rec = objectArray[0].toString();

					uat = new Group();

					uat.setCode((objectArray[1] != null ? objectArray[1]
							.toString() : ""));
					uat.setName((objectArray[2] != null ? objectArray[2]
							.toString() : ""));
					uat.setDescription((objectArray[3] != null ? objectArray[3]
							.toString() : ""));

					result.add(uat); // для сохранения сортировки из запроса
					result_ids.put(id_rec, uat);

					if (usersIdsLine == null) {
						usersIdsLine = "'" + id_rec + "'";
					} else {
						usersIdsLine = usersIdsLine + ", '" + id_rec + "'";
					}
				}

				logger.info("groups_data:02");

				// 2.роли

				lo = em.createNativeQuery(
						"  SELECT gr.ID_SRV, ROL.SIGN_OBJECT "
								+ "    FROM AC_IS_BSS_T sys, "
								+ "         AC_ROLES_BSS_T rol, "
								+ "         GROUP_USERS_KNL_T gr, "
								+ "         AC_SUBSYSTEM_CERT_BSS_T subsys, "
								+ "         LINK_GROUP_USERS_ROLES_KNL_T lugr "
								+ "   WHERE (SYS.SIGN_OBJECT = :idIS "
								+ "          OR SUBSYS.SUBSYSTEM_CODE = :idIS ) "
								+ "         AND ROL.ID_SRV = LUGR.UP_ROLES "
								+ "         AND LUGR.UP_GROUP_USERS = gr.ID_SRV "
								+ "         AND ROL.UP_IS = sys.ID_SRV "
								+ "         AND gr.ID_SRV IN (" + usersIdsLine
								+ ") "
								+ "         AND SUBSYS.UP_IS(+) = SYS.ID_SRV "
								+ "GROUP BY gr.ID_SRV, ROL.SIGN_OBJECT ")
						.setParameter("idIS", idIS).getResultList();

				for (Object[] objectArray : lo) {

					if (result_ids.get(objectArray[0].toString())
							.getCodesRoles() == null) {
						result_ids.get(objectArray[0].toString())
								.setCodesRoles(new ArrayList<String>());
					}

					result_ids.get(objectArray[0].toString()).getCodesRoles()
							.add(objectArray[1].toString());
				}

				// 3.количество

				logger.info("groups_data:03");

				if (!CUDConstants.categoryUSER.equals(category)) {
					// от системы группы или весь список групп

					result_count = ((java.math.BigDecimal) em
							.createNativeQuery(
									"select count(*) from  GROUP_USERS_KNL_T gr_full, "
											+ "( "
											+ "select GR.ID_SRV gr_id "
											+ "from GROUP_USERS_KNL_T gr, "
											+ "LINK_GROUP_USERS_ROLES_KNL_T lgr, "
											+ "AC_ROLES_BSS_T rol, "
											+ "AC_IS_BSS_T sys "
											+ "where GR.ID_SRV = LGR.UP_GROUP_USERS(+) "
											+ "and ROL.ID_SRV(+) = LGR.UP_ROLES "
											+ "and SYS.ID_SRV(+)=ROL.UP_IS "
											+ " "
											+ "and( SYS.SIGN_OBJECT = :idIS or -1 = :onlyISUsers ) "
											+

											(uidsLine != null ? "and GR.SIGN_OBJECT in ("
													+ uidsLine + ") "
													: " ")
											+

											(rolesLine != null ? "and ( ROL.SIGN_OBJECT in ("
													+ rolesLine
													+ ") and SYS.SIGN_OBJECT = :idIS ) "
													: " ") +

											"group by GR.ID_SRV " + ") "
											+ "where gr_id=gr_full.ID_SRV "
											+ "order by gr_full.FULL_ ")
							.setParameter("idIS", idIS)
							.setParameter(
									"onlyISUsers",
									(CUDConstants.categorySYS.equals(category) ? 1
											: -1)).getSingleResult())
							.intValue();

				} else {
					// группы текущего пользователя

					result_count = ((java.math.BigDecimal) em
							.createNativeQuery(
									"select count(*)  "
											+ "from GROUP_USERS_KNL_T gr_full, "
											+ "( "
											+ "select GR.ID_SRV gr_id "
											+ "from GROUP_USERS_KNL_T gr, "
											+ "LINK_GROUP_USERS_ROLES_KNL_T lgr, "
											+ "AC_ROLES_BSS_T rol, "
											+ "AC_IS_BSS_T sys, "
											+ "LINK_GROUP_USERS_USERS_KNL_T guu "
											+ "where GR.ID_SRV = LGR.UP_GROUP_USERS(+) "
											+ "and ROL.ID_SRV(+) = LGR.UP_ROLES "
											+ "and SYS.ID_SRV(+)=ROL.UP_IS "
											+ " "
											+ "and( SYS.SIGN_OBJECT = :idIS or -1 = :onlyISUsers ) "
											+ "and guu.UP_GROUP_USERS = GR.ID_SRV "
											+ "and guu.UP_USERS = :idUser "
											+

											(uidsLine != null ? "and GR.SIGN_OBJECT in ("
													+ uidsLine + ") "
													: " ") +

											"group by GR.ID_SRV " + ") "
											+ "where gr_id=gr_full.ID_SRV "
											+ "order by gr_full.FULL_ ")

							.setParameter("idIS", idIS)
							.setParameter("onlyISUsers", 1)
							.setParameter("idUser", idUserAuth)
							.getSingleResult()).intValue();

				}

			} else if (idIS.startsWith(CUDConstants.groupArmPrefix)) {

				// группы
				// 1. пользователи

				if (!CUDConstants.categoryUSER.equals(category)) {
					// от системы группы или весь список групп

					lo = em.createNativeQuery(

							"select gr_full.ID_SRV, gr_full.SIGN_OBJECT, gr_full.FULL_, gr_full.DESCRIPTION  "
									+ "from GROUP_USERS_KNL_T gr_full, "
									+ "( "
									+ "select GR.ID_SRV gr_id "
									+ "from GROUP_USERS_KNL_T gr, "
									+ "LINK_GROUP_USERS_ROLES_KNL_T lgr, "
									+ "AC_ROLES_BSS_T rol, "
									+ "AC_IS_BSS_T sys, "
									+ "GROUP_SYSTEMS_KNL_T sysgroup,  LINK_GROUP_SYS_SYS_KNL_T linksysgroup "
									+ "where GR.ID_SRV = LGR.UP_GROUP_USERS(+) "
									+ "and ROL.ID_SRV(+) = LGR.UP_ROLES "
									+ "and SYS.ID_SRV(+)=ROL.UP_IS "
									+

									"and SYSGROUP.ID_SRV = LINKSYSGROUP.UP_GROUP_SYSTEMS "
									+ "and ( "
									+ "LINKSYSGROUP.UP_SYSTEMS=SYS.ID_SRV  "
									+ "and SYSGROUP.GROUP_CODE = :idIS "
									+ "or -1 = :onlyISUsers  "
									+ " ) "
									+

									(uidsLine != null ? "and GR.SIGN_OBJECT in ("
											+ uidsLine + ") "
											: " ")
									+

									(rolesLine != null ? "and ( ROL.SIGN_OBJECT in ("
											+ rolesLine
											+ ") and LINKSYSGROUP.UP_SYSTEMS=SYS.ID_SRV ) "
											: " ")
									+

									"group by GR.ID_SRV "
									+ ") "
									+ "where gr_id=gr_full.ID_SRV "
									+ "order by gr_full.FULL_ ")

							.setParameter("idIS", idIS)
							.setParameter(
									"onlyISUsers",
									(CUDConstants.categorySYS.equals(category) ? 1
											: -1))
							.setFirstResult(
									start != null ? start.intValue() : 0)
							.setMaxResults(
									count != null ? count.intValue() : 1000000)
							.getResultList();

				} else {
					// группы текущего пользователя

					lo = em.createNativeQuery(

							"select gr_full.ID_SRV, gr_full.SIGN_OBJECT, gr_full.FULL_, gr_full.DESCRIPTION  "
									+ "from GROUP_USERS_KNL_T gr_full, "
									+ "( "
									+ "select GR.ID_SRV gr_id "
									+ "from GROUP_USERS_KNL_T gr, "
									+ "LINK_GROUP_USERS_ROLES_KNL_T lgr, "
									+ "AC_ROLES_BSS_T rol, "
									+ "AC_IS_BSS_T sys, "
									+ "LINK_GROUP_USERS_USERS_KNL_T guu, "
									+ "GROUP_SYSTEMS_KNL_T sysgroup,  LINK_GROUP_SYS_SYS_KNL_T linksysgroup "
									+ "where GR.ID_SRV = LGR.UP_GROUP_USERS(+) "
									+ "and ROL.ID_SRV(+) = LGR.UP_ROLES "
									+ "and SYS.ID_SRV(+)=ROL.UP_IS "
									+

									"and guu.UP_GROUP_USERS = GR.ID_SRV "
									+ "and guu.UP_USERS = :idUser "
									+

									"and SYSGROUP.ID_SRV = LINKSYSGROUP.UP_GROUP_SYSTEMS "
									+ "and ( "
									+ "LINKSYSGROUP.UP_SYSTEMS=SYS.ID_SRV  "
									+ "and SYSGROUP.GROUP_CODE = :idIS "
									+ "or -1 = :onlyISUsers  "
									+ " ) "
									+

									(uidsLine != null ? "and GR.SIGN_OBJECT in ("
											+ uidsLine + ") "
											: " ")
									+

									"group by GR.ID_SRV "
									+ ") "
									+ "where gr_id=gr_full.ID_SRV "
									+ "order by gr_full.FULL_ ")

							.setParameter("idIS", idIS)
							.setParameter("onlyISUsers", 1)
							.setParameter("idUser", idUserAuth)
							.setFirstResult(
									start != null ? start.intValue() : 0)
							.setMaxResults(
									count != null ? count.intValue() : 1000000)
							.getResultList();

				}

				for (Object[] objectArray : lo) {

					id_rec = objectArray[0].toString();

					uat = new Group();

					uat.setCode((objectArray[1] != null ? objectArray[1]
							.toString() : ""));
					uat.setName((objectArray[2] != null ? objectArray[2]
							.toString() : ""));
					uat.setDescription((objectArray[3] != null ? objectArray[3]
							.toString() : ""));

					result.add(uat); // для сохранения сортировки из запроса
					result_ids.put(id_rec, uat);

					if (usersIdsLine == null) {
						usersIdsLine = "'" + id_rec + "'";
					} else {
						usersIdsLine = usersIdsLine + ", '" + id_rec + "'";
					}
				}

				logger.info("groups_data:04");

				// 2. роли

				lo = em.createNativeQuery(
						" SELECT gr_id, '[' || sys_code || ']' || role_code role_full_code "
								+ " FROM ( "
								+ " SELECT gr.ID_SRV gr_id, SYS.SIGN_OBJECT sys_code, ROL.SIGN_OBJECT role_code "
								+ "    FROM AC_IS_BSS_T sys, "
								+ "         AC_ROLES_BSS_T rol, "
								+ "         GROUP_USERS_KNL_T gr, "
								+ "         LINK_GROUP_USERS_ROLES_KNL_T lugr, "
								+ "         GROUP_SYSTEMS_KNL_T gsys, "
								+ "         LINK_GROUP_SYS_SYS_KNL_T lgr "
								+ "   WHERE  ROL.ID_SRV = LUGR.UP_ROLES "
								+ "         AND LUGR.UP_GROUP_USERS = gr.ID_SRV "
								+ "         AND ROL.UP_IS = sys.ID_SRV "
								+ "         AND gr.ID_SRV IN ("
								+ usersIdsLine
								+ ") "
								+ "   AND GSYS.GROUP_CODE = :idIS "
								+ "   AND GSYS.ID_SRV = LGR.UP_GROUP_SYSTEMS "
								+ "   AND LGR.UP_SYSTEMS = SYS.ID_SRV "
								+ "GROUP BY gr.ID_SRV, SYS.SIGN_OBJECT, ROL.SIGN_OBJECT ) "
								+ "ORDER BY gr_id, sys_code ")
						.setParameter("idIS", idIS).getResultList();

				for (Object[] objectArray : lo) {

					if (result_ids.get(objectArray[0].toString())
							.getCodesRoles() == null) {
						result_ids.get(objectArray[0].toString())
								.setCodesRoles(new ArrayList<String>());
					}

					result_ids.get(objectArray[0].toString()).getCodesRoles()
							.add(objectArray[1].toString());
				}

				// 3. количество

				logger.info("groups_data:05");

				if (!CUDConstants.categoryUSER.equals(category)) {
					// от системы группы или весь список групп

					result_count = ((java.math.BigDecimal) em
							.createNativeQuery(
									"select count(*) "
											+ "from GROUP_USERS_KNL_T gr_full, "
											+ "( "
											+ "select GR.ID_SRV gr_id "
											+ "from GROUP_USERS_KNL_T gr, "
											+ "LINK_GROUP_USERS_ROLES_KNL_T lgr, "
											+ "AC_ROLES_BSS_T rol, "
											+ "AC_IS_BSS_T sys, "
											+ "GROUP_SYSTEMS_KNL_T sysgroup,  LINK_GROUP_SYS_SYS_KNL_T linksysgroup "
											+ "where GR.ID_SRV = LGR.UP_GROUP_USERS(+) "
											+ "and ROL.ID_SRV(+) = LGR.UP_ROLES "
											+ "and SYS.ID_SRV(+)=ROL.UP_IS "
											+

											"and SYSGROUP.ID_SRV = LINKSYSGROUP.UP_GROUP_SYSTEMS "
											+ "and ( "
											+ "LINKSYSGROUP.UP_SYSTEMS=SYS.ID_SRV  "
											+ "and SYSGROUP.GROUP_CODE = :idIS "
											+ "or -1 = :onlyISUsers  "
											+ " ) "
											+

											(uidsLine != null ? "and GR.SIGN_OBJECT in ("
													+ uidsLine + ") "
													: " ")
											+

											(rolesLine != null ? "and ( ROL.SIGN_OBJECT in ("
													+ rolesLine
													+ ") and LINKSYSGROUP.UP_SYSTEMS=SYS.ID_SRV ) "
													: " ") +

											"group by GR.ID_SRV " + ") "
											+ "where gr_id=gr_full.ID_SRV "
											+ "order by gr_full.FULL_ ")
							.setParameter("idIS", idIS)
							.setParameter(
									"onlyISUsers",
									(CUDConstants.categorySYS.equals(category) ? 1
											: -1)).getSingleResult())
							.intValue();

				} else {
					// группы текущего пользователя

					result_count = ((java.math.BigDecimal) em
							.createNativeQuery(

									"select count(*)  "
											+ "from GROUP_USERS_KNL_T gr_full, "
											+ "( "
											+ "select GR.ID_SRV gr_id "
											+ "from GROUP_USERS_KNL_T gr, "
											+ "LINK_GROUP_USERS_ROLES_KNL_T lgr, "
											+ "AC_ROLES_BSS_T rol, "
											+ "AC_IS_BSS_T sys, "
											+ "LINK_GROUP_USERS_USERS_KNL_T guu, "
											+ "GROUP_SYSTEMS_KNL_T sysgroup,  LINK_GROUP_SYS_SYS_KNL_T linksysgroup "
											+ "where GR.ID_SRV = LGR.UP_GROUP_USERS(+) "
											+ "and ROL.ID_SRV(+) = LGR.UP_ROLES "
											+ "and SYS.ID_SRV(+)=ROL.UP_IS "
											+

											"and guu.UP_GROUP_USERS = GR.ID_SRV "
											+ "and guu.UP_USERS = :idUser "
											+

											"and SYSGROUP.ID_SRV = LINKSYSGROUP.UP_GROUP_SYSTEMS "
											+ "and ( "
											+ "LINKSYSGROUP.UP_SYSTEMS=SYS.ID_SRV  "
											+ "and SYSGROUP.GROUP_CODE = :idIS "
											+ "or -1 = :onlyISUsers  "
											+ " ) "
											+

											(uidsLine != null ? "and GR.SIGN_OBJECT in ("
													+ uidsLine + ") "
													: " ") +

											"group by GR.ID_SRV " + ") "
											+ "where gr_id=gr_full.ID_SRV "
											+ "order by gr_full.FULL_ ")

							.setParameter("idIS", idIS)
							.setParameter("onlyISUsers", 1)
							.setParameter("idUser", idUserAuth)
							.getSingleResult()).intValue();

				}
			}

			// isu.setUserAttributesRoles(new
			// ArrayList<UserAttributesRoles>(result_ids.values()));
			isu.setGroups(result);
			isu.setCount(result_count);

			sys_audit(72L, /* "token:"+subject.getId()+ */"idIS:" + idIS
					+ "result_count:" + result_count, "true; ", IPAddress,
					idUserAuth);

		} catch (Exception e) {
			sys_audit(72L, /* "token:"+subject.getId()+ */"; idIS:" + idIS,
					"error", IPAddress, idUserAuth);

			logger.error("groups_data:Error:" + e);
			throw new GeneralFailure(e.getMessage());
		}
		return isu;
	}

	/**
	 * данные по ресурсам
	 */
	public ResourcesData resources_data(String idIS,
			List<String> resourcesCodes, String category,
			List<String> rolesCodes, Integer start, Integer count,
			Map<String, String> settings, Long idUserAuth, String IPAddress)
			throws GeneralFailure {

		logger.info("resources_data:01");

		// onlyISUsers[category:SYS] условие сильнее, чем rolesCodes
		// то есть, если стоит onlyISUsers = false [все пользователи],
		// то rolesCodes уже не учитывается

		// category:
		// USER (используем idUserAuth)
		// SYS

		Integer result_count = 0;
		List<ResourceNU> result = new ArrayList<ResourceNU>();
		Map<String, ResourceNU> result_ids = new HashMap<String, ResourceNU>();
		ResourceNU uat = null;

		List<Object[]> lo = null;
		String id_rec = null;

		ResourcesData isu = new ResourcesData();

		String rolesLine = null;
		String usersIdsLine = null;
	
		try {

			if (idIS == null) {
				logger.info("resources_data:return:1");
				throw new GeneralFailure("idIS is null!");
			}

			if (count == null) {
				count = MAX_CONT_RES;
			}
			if (start == null) {
				start = 0;
			}

			if (MAX_CONT_RES < count) {
				logger.info("resources_data:return:2");
				throw new GeneralFailure("'count' should be less than "
						+ MAX_CONT_RES);
			}

			logger.info("resources_data:idIS1:" + idIS);

			String uidsLine = null;

			if (resourcesCodes != null && !resourcesCodes.isEmpty()) {

				for (String uid_user : resourcesCodes) {
					if (uidsLine == null) {
						uidsLine = "'" + uid_user + "'";
					} else {
						uidsLine = uidsLine + ", '" + uid_user + "'";
					}
				}
			}

			if (CUDConstants.categorySYS.equals(category) && rolesCodes != null
					&& !rolesCodes.isEmpty()) {

				for (String role : rolesCodes) {
					if (rolesLine == null) {
						rolesLine = "'" + role + "'";
					} else {
						rolesLine = rolesLine + ", '" + role + "'";
					}
				}

			}

			if (idIS.startsWith(CUDConstants.armPrefix)
					|| idIS.startsWith(CUDConstants.subArmPrefix)) {

				// !!!
				idIS = get_code_is(idIS);

				logger.info("resources_data:idIS2:" + idIS);

				// системы и подсистемы
				// 1.группы

				lo = em.createNativeQuery(
						" SELECT res_full.ID_SRV, "
								+ "         res_full.SIGN_OBJECT, "
								+ "         res_full.FULL_, "
								+ "         res_full.DESCRIPTION "
								+ "    FROM AC_RESOURCES_BSS_T res_full, "
								+ "         (  SELECT res.ID_SRV res_id "
								+ "              FROM AC_RESOURCES_BSS_T res, "
								+ "                   AC_LINK_ROLE_RESOURCE_KNL_T lgr, "
								+ "                   AC_ROLES_BSS_T rol, "
								+ "                   AC_IS_BSS_T sys "
								+ "             WHERE     res.ID_SRV = LGR.UP_RESOURCE(+) "
								+ "                   AND ROL.ID_SRV(+) = LGR.UP_ROLE "
								+ "                   AND SYS.ID_SRV(+) = ROL.UP_IS "
								+ "                   AND res.up_is = SYS.ID_SRV "
								+ "                   AND (SYS.SIGN_OBJECT = :idIS OR -1 = :onlyISUsers) "
								+ (uidsLine != null ? "   and res.SIGN_OBJECT in ("
										+ uidsLine + ") "
										: "")
								+

								(rolesLine != null ? "    and (ROL.SIGN_OBJECT in ("
										+ rolesLine
										+ ") and SYS.SIGN_OBJECT = :idIS ) "
										: "") +

								"          GROUP BY res.ID_SRV) "
								+ "   WHERE res_id = res_full.ID_SRV "
								+ "ORDER BY res_full.FULL_")
						.setParameter("idIS", idIS)
						.setParameter(
								"onlyISUsers",
								(CUDConstants.categorySYS.equals(category) ? 1
										: -1))
						.setFirstResult(start != null ? start.intValue() : 0)
						.setMaxResults(
								count != null ? count.intValue() : 1000000)
						.getResultList();

				for (Object[] objectArray : lo) {

					id_rec = objectArray[0].toString();

					uat = new ResourceNU();

					uat.setCode((objectArray[1] != null ? objectArray[1]
							.toString() : ""));
					uat.setName((objectArray[2] != null ? objectArray[2]
							.toString() : ""));
					uat.setDescription((objectArray[3] != null ? objectArray[3]
							.toString() : ""));

					result.add(uat); // для сохранения сортировки из запроса
					result_ids.put(id_rec, uat);

					if (usersIdsLine == null) {
						usersIdsLine = "'" + id_rec + "'";
					} else {
						usersIdsLine = usersIdsLine + ", '" + id_rec + "'";
					}
				}

				logger.info("resources_data:02");

				// 2.роли

				lo = em.createNativeQuery(
						"  SELECT gr.ID_SRV, ROL.SIGN_OBJECT "
								+ "    FROM AC_IS_BSS_T sys, "
								+ "         AC_ROLES_BSS_T rol, "
								+ "         GROUP_USERS_KNL_T gr, "
								+ "         AC_SUBSYSTEM_CERT_BSS_T subsys, "
								+ "         LINK_GROUP_USERS_ROLES_KNL_T lugr "
								+ "   WHERE (SYS.SIGN_OBJECT = :idIS "
								+ "          OR SUBSYS.SUBSYSTEM_CODE = :idIS ) "
								+ "         AND ROL.ID_SRV = LUGR.UP_ROLES "
								+ "         AND LUGR.UP_GROUP_USERS = gr.ID_SRV "
								+ "         AND ROL.UP_IS = sys.ID_SRV "
								+ "         AND gr.ID_SRV IN (" + usersIdsLine
								+ ") "
								+ "         AND SUBSYS.UP_IS(+) = SYS.ID_SRV "
								+ "GROUP BY gr.ID_SRV, ROL.SIGN_OBJECT ")
						.setParameter("idIS", idIS).getResultList();

				for (Object[] objectArray : lo) {

					if (result_ids.get(objectArray[0].toString())
							.getCodesRoles() == null) {
						result_ids.get(objectArray[0].toString())
								.setCodesRoles(new ArrayList<String>());
					}

					result_ids.get(objectArray[0].toString()).getCodesRoles()
							.add(objectArray[1].toString());
				}

				// 3.количество

				logger.info("resources_data:03");

				result_count = ((java.math.BigDecimal) em
						.createNativeQuery(
								"select count(*) from  GROUP_USERS_KNL_T gr_full, "
										+ "( "
										+ "select GR.ID_SRV gr_id "
										+ "from GROUP_USERS_KNL_T gr, "
										+ "LINK_GROUP_USERS_ROLES_KNL_T lgr, "
										+ "AC_ROLES_BSS_T rol, "
										+ "AC_IS_BSS_T sys "
										+ "where GR.ID_SRV = LGR.UP_GROUP_USERS(+) "
										+ "and ROL.ID_SRV(+) = LGR.UP_ROLES "
										+ "and SYS.ID_SRV(+)=ROL.UP_IS "
										+ " "
										+ "and( SYS.SIGN_OBJECT = :idIS or -1 = :onlyISUsers ) "
										+

										(uidsLine != null ? "and GR.SIGN_OBJECT in ("
												+ uidsLine + ") "
												: " ")
										+

										(rolesLine != null ? "and ( ROL.SIGN_OBJECT in ("
												+ rolesLine
												+ ") and SYS.SIGN_OBJECT = :idIS ) "
												: " ") +

										"group by GR.ID_SRV " + ") "
										+ "where gr_id=gr_full.ID_SRV "
										+ "order by gr_full.FULL_ ")
						.setParameter("idIS", idIS)
						.setParameter(
								"onlyISUsers",
								(CUDConstants.categorySYS.equals(category) ? 1
										: -1)).getSingleResult()).intValue();

			} else if (idIS.startsWith(CUDConstants.groupArmPrefix)) {

				// группы
				// 1. пользователи

				lo = em.createNativeQuery(

						"select gr_full.ID_SRV, gr_full.SIGN_OBJECT, gr_full.FULL_, gr_full.DESCRIPTION  "
								+ "from GROUP_USERS_KNL_T gr_full, "
								+ "( "
								+ "select GR.ID_SRV gr_id "
								+ "from GROUP_USERS_KNL_T gr, "
								+ "LINK_GROUP_USERS_ROLES_KNL_T lgr, "
								+ "AC_ROLES_BSS_T rol, "
								+ "AC_IS_BSS_T sys, "
								+ "GROUP_SYSTEMS_KNL_T sysgroup,  LINK_GROUP_SYS_SYS_KNL_T linksysgroup "
								+ "where GR.ID_SRV = LGR.UP_GROUP_USERS(+) "
								+ "and ROL.ID_SRV(+) = LGR.UP_ROLES "
								+ "and SYS.ID_SRV(+)=ROL.UP_IS "
								+

								"and SYSGROUP.ID_SRV = LINKSYSGROUP.UP_GROUP_SYSTEMS "
								+ "and ( "
								+ "LINKSYSGROUP.UP_SYSTEMS=SYS.ID_SRV  "
								+ "and SYSGROUP.GROUP_CODE = :idIS "
								+ "or -1 = :onlyISUsers  "
								+ " ) "
								+

								(uidsLine != null ? "and GR.SIGN_OBJECT in ("
										+ uidsLine + ") " : " ")
								+

								(rolesLine != null ? "and ( ROL.SIGN_OBJECT in ("
										+ rolesLine
										+ ") and LINKSYSGROUP.UP_SYSTEMS=SYS.ID_SRV ) "
										: " ")
								+

								"group by GR.ID_SRV "
								+ ") "
								+ "where gr_id=gr_full.ID_SRV "
								+ "order by gr_full.FULL_ ")

						.setParameter("idIS", idIS)
						.setParameter(
								"onlyISUsers",
								(CUDConstants.categorySYS.equals(category) ? 1
										: -1))
						.setFirstResult(start != null ? start.intValue() : 0)
						.setMaxResults(
								count != null ? count.intValue() : 1000000)
						.getResultList();

				for (Object[] objectArray : lo) {

					id_rec = objectArray[0].toString();

					uat = new ResourceNU();

					uat.setCode((objectArray[1] != null ? objectArray[1]
							.toString() : ""));
					uat.setName((objectArray[2] != null ? objectArray[2]
							.toString() : ""));
					uat.setDescription((objectArray[3] != null ? objectArray[3]
							.toString() : ""));

					result.add(uat); // для сохранения сортировки из запроса
					result_ids.put(id_rec, uat);

					if (usersIdsLine == null) {
						usersIdsLine = "'" + id_rec + "'";
					} else {
						usersIdsLine = usersIdsLine + ", '" + id_rec + "'";
					}
				}

				logger.info("resources_data:04");

				// 2. роли

				lo = em.createNativeQuery(
						" SELECT gr_id, '[' || sys_code || ']' || role_code role_full_code "
								+ " FROM ( "
								+ " SELECT gr.ID_SRV gr_id, SYS.SIGN_OBJECT sys_code, ROL.SIGN_OBJECT role_code "
								+ "    FROM AC_IS_BSS_T sys, "
								+ "         AC_ROLES_BSS_T rol, "
								+ "         GROUP_USERS_KNL_T gr, "
								+ "         LINK_GROUP_USERS_ROLES_KNL_T lugr, "
								+ "         GROUP_SYSTEMS_KNL_T gsys, "
								+ "         LINK_GROUP_SYS_SYS_KNL_T lgr "
								+ "   WHERE  ROL.ID_SRV = LUGR.UP_ROLES "
								+ "         AND LUGR.UP_GROUP_USERS = gr.ID_SRV "
								+ "         AND ROL.UP_IS = sys.ID_SRV "
								+ "         AND gr.ID_SRV IN ("
								+ usersIdsLine
								+ ") "
								+ "   AND GSYS.GROUP_CODE = :idIS "
								+ "   AND GSYS.ID_SRV = LGR.UP_GROUP_SYSTEMS "
								+ "   AND LGR.UP_SYSTEMS = SYS.ID_SRV "
								+ "GROUP BY gr.ID_SRV, SYS.SIGN_OBJECT, ROL.SIGN_OBJECT ) "
								+ "ORDER BY gr_id, sys_code ")
						.setParameter("idIS", idIS).getResultList();

				for (Object[] objectArray : lo) {

					if (result_ids.get(objectArray[0].toString())
							.getCodesRoles() == null) {
						result_ids.get(objectArray[0].toString())
								.setCodesRoles(new ArrayList<String>());
					}

					result_ids.get(objectArray[0].toString()).getCodesRoles()
							.add(objectArray[1].toString());
				}

				// 3. количество

				logger.info("resources_data:05");

				result_count = ((java.math.BigDecimal) em
						.createNativeQuery(
								"select count(*) "
										+ "from GROUP_USERS_KNL_T gr_full, "
										+ "( "
										+ "select GR.ID_SRV gr_id "
										+ "from GROUP_USERS_KNL_T gr, "
										+ "LINK_GROUP_USERS_ROLES_KNL_T lgr, "
										+ "AC_ROLES_BSS_T rol, "
										+ "AC_IS_BSS_T sys, "
										+ "GROUP_SYSTEMS_KNL_T sysgroup,  LINK_GROUP_SYS_SYS_KNL_T linksysgroup "
										+ "where GR.ID_SRV = LGR.UP_GROUP_USERS(+) "
										+ "and ROL.ID_SRV(+) = LGR.UP_ROLES "
										+ "and SYS.ID_SRV(+)=ROL.UP_IS "
										+

										"and SYSGROUP.ID_SRV = LINKSYSGROUP.UP_GROUP_SYSTEMS "
										+ "and ( "
										+ "LINKSYSGROUP.UP_SYSTEMS=SYS.ID_SRV  "
										+ "and SYSGROUP.GROUP_CODE = :idIS "
										+ "or -1 = :onlyISUsers  "
										+ " ) "
										+

										(uidsLine != null ? "and GR.SIGN_OBJECT in ("
												+ uidsLine + ") "
												: " ")
										+

										(rolesLine != null ? "and ( ROL.SIGN_OBJECT in ("
												+ rolesLine
												+ ") and LINKSYSGROUP.UP_SYSTEMS=SYS.ID_SRV ) "
												: " ") +

										"group by GR.ID_SRV " + ") "
										+ "where gr_id=gr_full.ID_SRV "
										+ "order by gr_full.FULL_ ")
						.setParameter("idIS", idIS)
						.setParameter(
								"onlyISUsers",
								(CUDConstants.categorySYS.equals(category) ? 1
										: -1)).getSingleResult()).intValue();

			}

			// isu.setUserAttributesRoles(new
			// ArrayList<UserAttributesRoles>(result_ids.values()));
			isu.setResources(result);
			isu.setCount(result_count);

			sys_audit(72L, "idIS:" + idIS + "result_count:" + result_count,
					"true; ", IPAddress, idUserAuth);

		} catch (Exception e) {
			sys_audit(72L, "; idIS:" + idIS, "error", IPAddress, idUserAuth);

			logger.error("resources_data:Error:" + e);
			throw new GeneralFailure(e.getMessage());
		}
		return isu;

	}

	/**
	 * данные по подсистемам
	 */
	public List<Resource> resources_data_subsys(String idIS,
	
	String category, Long idUserAuth, String IPAddress) throws GeneralFailure {

		logger.info("resources_data:01");

		// onlyISUsers[category:SYS] условие сильнее, чем rolesCodes
		// то есть, если стоит onlyISUsers = false [все пользователи],
		// то rolesCodes уже не учитывается

		// category:
		// USER (используем idUserAuth)
		// SYS

		Integer result_count = 0;
		List<Resource> result = new ArrayList<Resource>();
		Map<String, Resource> result_ids = new HashMap<String, Resource>();
		Resource uat = null;

		List<Object[]> lo = null;
		String id_rec = null;

		try {

			if (idIS == null) {
				logger.info("resources_data:return:1");
				throw new GeneralFailure("idIS is null!");
			}

			logger.info("resources_data:idIS1:" + idIS);

			if (idIS.startsWith(CUDConstants.armPrefix)
					|| idIS.startsWith(CUDConstants.subArmPrefix)) {

				// !!!
				idIS = get_code_is(idIS);

				logger.info("resources_data:idIS2:" + idIS);

				// системы и подсистемы

				// информация по системе
				if (CUDConstants.categorySYS.equals(category)) {

					lo = em.createNativeQuery(
							"select arm.ID_SRV, ARM.SIGN_OBJECT, ARM.FULL_,  ARM.DESCRIPTION , arm.LINKS from AC_IS_BSS_T arm "
									+ "where ARM.SIGN_OBJECT= :idIS ")
							.setParameter("idIS", idIS).getResultList();

				} else {
					// доступная система для пользователя
					lo = em.createNativeQuery(
							"select sys_full.ID_SRV, sys_full.SIGN_OBJECT, sys_full.FULL_, SYS_FULL.DESCRIPTION, sys_full.LINKS from AC_IS_BSS_T sys_full,"
									+ " (select   SYS.ID_SRV sys_id"
									+ "                     from  AC_IS_BSS_T sys,     "
									+ "                              AC_ROLES_BSS_T rol,     "
									+ "                              AC_USERS_LINK_KNL_T url,     "
									+ "                              AC_USERS_KNL_T AU,     "
									+ "                              LINK_GROUP_USERS_ROLES_KNL_T lugr,   "
									+ "                              LINK_GROUP_USERS_USERS_KNL_T lugu     "
									+ "                     where SYS.SIGN_OBJECT= :idIS      "
									+ "                           and (ROL.ID_SRV = URL.UP_ROLES or ROL.ID_SRV = LUGR.UP_ROLES )   "
									+ "                           and LUGU.UP_GROUP_USERS= LUGR.UP_GROUP_USERS(+)   "
									+ "                           and LUGU.UP_USERS(+)  = AU.ID_SRV   "
									+ "                           and URL.UP_USERS(+)  = AU.ID_SRV   "
									+ "                           and ROL.UP_IS=sys.ID_SRV    "
									+ "                           and AU.ID_SRV = :idUser       "
									+ "                     group by SYS.ID_SRV  ) t1 "
									+ "                     where t1.sys_id = SYS_FULL.ID_SRV ")
							.setParameter("idIS", idIS)
							.setParameter("idUser", idUserAuth).getResultList();

				}

				for (Object[] objectArray : lo) {

					id_rec = objectArray[0].toString();

					uat = new Resource();

					uat.setCode((objectArray[1] != null ? objectArray[1]
							.toString() : ""));
					uat.setName((objectArray[2] != null ? objectArray[2]
							.toString() : ""));
					uat.setDescription((objectArray[3] != null ? objectArray[3]
							.toString() : ""));

					if (objectArray[4] != null
							&& !objectArray[4].toString().trim().equals("")) {
						uat.setLinks(Arrays.asList(objectArray[4].toString()
								.replaceAll(" ", "").split(LINKS_SEPARATOR)));
					}

					result.add(uat); // для сохранения сортировки из запроса
					result_ids.put(id_rec, uat);

				}

				logger.info("resources_data:02");

			} else if (idIS.startsWith(CUDConstants.groupArmPrefix)) {

				// группы
				// 1. пользователи

				if (CUDConstants.categorySYS.equals(category)) {

					lo = em.createNativeQuery(
							" select SYS.ID_SRV, SYS.SIGN_OBJECT, SYS.FULL_, SYS.DESCRIPTION, sys.LINKS from "
									+ " GROUP_SYSTEMS_KNL_T gsys, "
									+ " AC_IS_BSS_T sys, "
									+ " LINK_GROUP_SYS_SYS_KNL_T lgr "
									+ " where GSYS.ID_SRV=LGR.UP_GROUP_SYSTEMS "
									+ " and LGR.UP_SYSTEMS= SYS.ID_SRV "
									+ " and GSYS.GROUP_CODE=:idIS ")
							.setParameter("idIS", idIS).getResultList();

				} else {

					lo = em.createNativeQuery(
							"select sys_full.ID_SRV,  sys_full.SIGN_OBJECT, sys_full.FULL_, SYS_FULL.DESCRIPTION , sys_full.LINKS "
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
									+ "                         where GSYS.GROUP_CODE=:idIS   "
									+ "                         and GSYS.ID_SRV=LGR.UP_GROUP_SYSTEMS   "
									+ "                         and LGR.UP_SYSTEMS=SYS.ID_SRV   "
									+ "                         and ROL.UP_IS= SYS.ID_SRV   "
									+ "                         and (ROL.ID_SRV = URL.UP_ROLES or ROL.ID_SRV = LUGR.UP_ROLES )   "
									+ "                         and LUGU.UP_GROUP_USERS = LUGR.UP_GROUP_USERS(+)   "
									+ "                         and LUGU.UP_USERS(+)  = AU.ID_SRV   "
									+ "                         and URL.UP_USERS(+)  = AU.ID_SRV   "
									+ "                        and AU.ID_SRV = :idUser   "
									+ "                         group by SYS.ID_SRV )   t1 "
									+ "                          where t1.sys_id = SYS_FULL.ID_SRV "
									+ "                         order by sys_full.SIGN_OBJECT ")
							.setParameter("idIS", idIS)
							.setParameter("idUser", idUserAuth).getResultList();

				}

				for (Object[] objectArray : lo) {

				// d_rec=((java.math.BigDecimal)objectArray[0]).longValue();
					id_rec = objectArray[0].toString();

					uat = new Resource();

					uat.setCode((objectArray[1] != null ? objectArray[1]
							.toString() : ""));
					uat.setName((objectArray[2] != null ? objectArray[2]
							.toString() : ""));
					uat.setDescription((objectArray[3] != null ? objectArray[3]
							.toString() : ""));

					if (objectArray[4] != null
							&& !objectArray[4].toString().trim().equals("")) {
						// linksList =
						// Arrays.asList(objectArray[4].toString().split(LINKS_SEPARATOR));
						uat.setLinks(Arrays.asList(objectArray[4].toString()
								.replaceAll(" ", "").split(LINKS_SEPARATOR)));
					}

					result.add(uat); // для сохранения сортировки из запроса
					result_ids.put(id_rec, uat);

				}

				logger.info("resources_data:04");

			}

			sys_audit(72L, "idIS:" + idIS + "result_count:" + result_count,
					"true; ", IPAddress, idUserAuth);

		} catch (Exception e) {
			sys_audit(72L, "; idIS:" + idIS, "error", IPAddress, idUserAuth);

			logger.error("resources_data:Error:" + e);

			e.printStackTrace(System.out);

			throw new GeneralFailure(e.getMessage());
		}
		return result;

	}

	
	public List<Role> roles_data(String idIS, String category,
			Long idUserAuth, String IPAddress) throws GeneralFailure{
		     
		 //расширенная версия SyncManager.is_roles()
		
		// для групп систем, систем и подсистем

		       // category:
				// USER (используем idUserAuth)
				// SYS

		
				logger.info("roles_data:01");

				HashMap<String, Long> roles_cl = new HashMap<String, Long>();

				List<Role> result = new ArrayList<Role>();
				List<String> keyList = new ArrayList<String>();

				List<Object[]> lo = null;
				
				try {

					if (idIS == null || idIS.trim().isEmpty()) {
						logger.info("roles_data:01");
						throw new GeneralFailure("idIS is null!");
					}

					if (idIS.startsWith(CUDConstants.groupArmPrefix)) {
						// группа ИС

						logger.info("roles_data:02");

						// информация по системе
						if (CUDConstants.categorySYS.equals(category)) {
						 lo = em
								.createNativeQuery(
										"  SELECT '[' || sys_code || ']' || role_full.SIGN_OBJECT role_is_code, "
												+ "         role_full.FULL_, "
												+ "         role_full.DESCRIPTION "
												+ "    FROM (  SELECT SYS.SIGN_OBJECT sys_code, ROL.ID_SRV role_id "
												+ "              FROM GROUP_SYSTEMS_KNL_T gsys, "
												+ "                   AC_IS_BSS_T sys, "
												+ "                   AC_ROLES_BSS_T rol, "
												+ "                   LINK_GROUP_SYS_SYS_KNL_T lgr "
												+ "             WHERE     GSYS.GROUP_CODE = ? "
												+ "                   AND GSYS.ID_SRV = LGR.UP_GROUP_SYSTEMS "
												+ "                   AND LGR.UP_SYSTEMS = SYS.ID_SRV "
												+ "                   AND ROL.UP_IS = SYS.ID_SRV "
												+ "          GROUP BY SYS.SIGN_OBJECT, ROL.ID_SRV), "
												+ "         AC_ROLES_BSS_T role_full "
												+ "   WHERE role_full.ID_SRV = role_id "
												+ "ORDER BY sys_code ")
								.setParameter(1, idIS).getResultList();
						}else{
							lo = em
									.createNativeQuery(
											"select '['||sys_code||']' || role_full.SIGN_OBJECT role_full_code, "
													+ "role_full.FULL_, "
													+ "role_full.DESCRIPTION "
													+ "from( "
													+ "select SYS.SIGN_OBJECT sys_code,  ROL.ID_SRV role_id "
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
													+ "and AU.ID_SRV= :idUser "
													+ "group by SYS.SIGN_OBJECT, ROL.ID_SRV), "
													+ "AC_ROLES_BSS_T role_full "
													+ "WHERE role_full.ID_SRV = role_id "
													+ "order by sys_code ")
									.setParameter("idIs", idIS)
									.setParameter("idUser", idUserAuth).getResultList();
						}
						for (Object[] objectArray : lo) {
							logger.info("IdRole:" + objectArray[0].toString());

							Role role = new Role();

							role.setCode(objectArray[0].toString());
							role.setName(objectArray[1].toString());
							role.setDescription((objectArray[2] != null ? objectArray[2]
									.toString() : null));

							result.add(role);

							keyList.add(objectArray[0].toString());

						}

						logger.info("roles_data:03");

					} else if (idIS.startsWith(CUDConstants.armPrefix)
							|| idIS.startsWith(CUDConstants.subArmPrefix)) {
						// система или подсистема

						// !!!
						idIS = get_code_is(idIS);

						
						// информация по системе
						if (CUDConstants.categorySYS.equals(category)) {
						
						 lo = em
								.createNativeQuery(
										"SELECT ROL.SIGN_OBJECT, ROL.FULL_, ROL.DESCRIPTION "
												+ "  FROM AC_IS_BSS_T app, AC_ROLES_BSS_T rol "
												+ " WHERE APP.SIGN_OBJECT = ? AND ROL.UP_IS = APP.ID_SRV ")
								.setParameter(1, idIS).getResultList();
						}else{
							// доступные роли для пользователя
							
							lo =  em
									.createNativeQuery(
											"select ROL.SIGN_OBJECT, ROL.FULL_, ROL.DESCRIPTION "
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
													+ "      and AU.ID_SRV= :idUser "
													+ "      and  SUBSYS.UP_IS(+) =SYS.ID_SRV "
													+ "group by ROL.SIGN_OBJECT, ROL.FULL_, ROL.DESCRIPTION ")
									.setParameter("idIs", idIS)
									.setParameter("idUser", idUserAuth).getResultList();
						}
						for (Object[] objectArray : lo) {

							Role role = new Role();

							role.setCode(objectArray[0].toString());
							role.setName(objectArray[1].toString());
							role.setDescription((objectArray[2] != null ? objectArray[2]
									.toString() : null));

							result.add(role);

							keyList.add(objectArray[0].toString());

						}

						logger.info("roles_data:04");

					}

					sys_audit(80L, "idIS:" + idIS, "true", IPAddress, null);

					return result;

				} catch (Exception e) {
					sys_audit(80L, "idIS:" + idIS, "error", IPAddress, null);
					throw new GeneralFailure(e.getMessage());
				}
		
	}
	
	/**
	 * данные по организациям
	 */
	public ISOrganisations is_organisations(
	    String idIS, Integer start, Integer count, List<String> rolesCodes,
	    Long idUserAuth, String IPAddress) throws GeneralFailure {

		logger.info("is_organisations:STUB");

		return null;
	}

	/**
	 * проверки наличия системы
	 */
	public void is_exist(String idIS) throws GeneralFailure {

		logger.info("is_exist:idIS:" + idIS);

		try {

			em.createNativeQuery(
					"select APP.ID_SRV " + "from AC_IS_BSS_T app "
							+ "where APP.SIGN_OBJECT=?").setParameter(1, idIS)
					.getSingleResult();

		} catch (NoResultException ex) {
			logger.error("is_exist:NoResultException");
			throw new GeneralFailure("Информационная система не определёна!");
		} catch (Exception e) {
			logger.error("is_exist:Error:" + e);
			throw new GeneralFailure(e.getMessage());
		}
	}

	/**
	 * протоколирование действий
	 */
	private void sys_audit(Long idServ, String inp_param, String result,
			String ip_adr, Long idUser) {
		logger.info("sys_audit");
		try {

			if (idUser != null && !idUser.equals(-1L)) {
				em.createNativeQuery(
						"insert into SERVICES_LOG_KNL_T( "
								+ "ID_SRV,  UP_SERVICES, DATE_ACTION, CREATED, "
								+ "input_param, result_value, ip_address, UP_USERS ) "
								+ "values(SERVICES_LOG_KNL_SEQ.nextval , ?, sysdate, sysdate, "
								+ "?, ?, ?, ? ) ").setParameter(1, idServ)
						.setParameter(2, inp_param).setParameter(3, result)
						.setParameter(4, ip_adr).setParameter(5, idUser)
						.executeUpdate();
			} else {
				em.createNativeQuery(
						"insert into SERVICES_LOG_KNL_T( "
								+ "ID_SRV,  UP_SERVICES, DATE_ACTION, CREATED, "
								+ "input_param, result_value, ip_address ) "
								+ "values(SERVICES_LOG_KNL_SEQ.nextval , ?, sysdate, sysdate, "
								+ "?, ?, ? ) ").setParameter(1, idServ)
						.setParameter(2, inp_param).setParameter(3, result)
						.setParameter(4, ip_adr).executeUpdate();
			}
		} catch (Exception e) {
			logger.error("sys_audit:Error:" + e);
			
		}

	}

	/**
	 * получение ИД системы по коду системы
	 */
	private String get_code_is(String codeSys) throws GeneralFailure {

		String result = null;

		try {

			// сюда заходим только в случае систем и подсистем
			// вообще нужно толко для подсистем - взять код их ИС.
			// при группе систем своя ветка - там код группы
			// используется прямо в запросе.

			// if(codeSys.startsWith(CUDConstants.armPrefix)||codeSys.startsWith(CUDConstants.subArmPrefix)){
			// системы и подсистемы

			result = (String) em
					.createNativeQuery(
							"select SYS.SIGN_OBJECT "
									+ "from  AC_IS_BSS_T sys, "
									+ "AC_SUBSYSTEM_CERT_BSS_T subsys "
									+ "where (SYS.SIGN_OBJECT= :codeSys or  SUBSYS.SUBSYSTEM_CODE= :codeSys) "
									+ "and  SUBSYS.UP_IS(+) =SYS.ID_SRV "
									+ "group by SYS.SIGN_OBJECT ")
					.setParameter("codeSys", codeSys).getSingleResult();

			// }

		} catch (NoResultException ex) {
			logger.error("get_code_is:NoResultException");
			throw new GeneralFailure("System is not defined");
		} catch (Exception e) {
			throw new GeneralFailure(e.getMessage());
		}
		return result;
	}

}
