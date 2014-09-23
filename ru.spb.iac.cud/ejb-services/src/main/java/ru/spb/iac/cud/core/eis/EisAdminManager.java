package ru.spb.iac.cud.core.eis;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import ru.spb.iac.cud.exceptions.GeneralFailure;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EJB для сервиса управления
 * 
 * @author bubnov
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class EisAdminManager implements AdminManagerLocal, AdminManagerRemote {

	@PersistenceContext(unitName = "AuthServices")
	EntityManager em;

	private static int max_size_roles = 500;

	Logger logger = LoggerFactory.getLogger(EisAdminManager.class);

	public EisAdminManager() {
	}

	/**
	 * предоставление доступа пользователя по ролям
	 */
	public void access(String codeSystem, List<String> uidsUsers,
			String modeExec, List<String> codesRoles, Long idUserAuth,
			String IPAddress) throws GeneralFailure {

		// для систем и подсистем

		// modeExec:
		// 0 - REPLACE
		// 1 - ADD
		// 2 - REMOVE
		logger.info("access:01");

		// List<String> result = new ArrayList<String>();

		try {
			logger.info("access:codeSystem:" + codeSystem);
			logger.info("access:modeExec:" + modeExec);

			if (modeExec == null
					|| modeExec.trim().isEmpty()
					|| (!modeExec.equals("REPLACE") && !modeExec.equals("ADD") && !modeExec
							.equals("REMOVE")) || uidsUsers == null
					|| uidsUsers.isEmpty() || codeSystem == null
					|| codeSystem.trim().isEmpty() || codeSystem.length() > 240
					|| codesRoles == null || codesRoles.isEmpty()
					|| codesRoles.size() > max_size_roles) {
				throw new GeneralFailure("Некорректные данные!");
			}

			logger.info("access:02");

			for (String uidUser : uidsUsers) {

				logger.info("access:uidUser:" + uidUser);

				if (uidUser == null || uidUser.trim().isEmpty()) {
					throw new GeneralFailure("Некорректные данные!");
				}

				Long idRoleApp = null;
				String rolesLineExist = null;
				String rolesLineApp = null;
				List<String> roles_app = new ArrayList<String>();

				// решили определять пользователей извне ЦУД по их ИД, а не
				// логинам
				// Long idUser = user_exist(loginUser);
				Long idUser = new Long(uidUser);
				Long idArm = get_id_is(codeSystem);
				int mode = 1;

				if (modeExec.equals("REPLACE")) {
					mode = 0;
				} else if (modeExec.equals("ADD")) {
					mode = 1;
				} else if (modeExec.equals("REMOVE")) {
					mode = 2;
				}

				for (String role : codesRoles) {

					idRoleApp = role_exist(idArm, role);

					roles_app.add(idRoleApp.toString());

					logger.info("createAccess:role:" + role);
					logger.info("createAccess:idRoleApp:" + idRoleApp);
				}

				// список ролей, которые уже есть у пользователя в системе
				List<String> roles_user = (List<String>) em
						.createNativeQuery(
								"select to_char(URL.UP_ROLES) "
										+ "from AC_ROLES_BSS_T rl, "
										+ "AC_USERS_LINK_KNL_T url "
										+ "where RL.ID_SRV=URL.UP_ROLES "
										+ "and RL.UP_IS=? "
										+ "and URL.UP_USERS=? ")
						.setParameter(1, idArm).setParameter(2, idUser)
						.getResultList();

				logger.info("createAccess:rolesLine:" + rolesLineExist);

				if (mode == 0) { // REPLACE

					// удаляем имеющиеся у пользователя роли в системе
					for (String role : roles_user) {
						if (rolesLineExist == null) {
							rolesLineExist = role;
						} else {
							rolesLineExist += ", " + role;
						}

					}

					logger.info("createAccess:rolesLine:" + rolesLineExist);

					em.createNativeQuery(
							"DELETE FROM AC_USERS_LINK_KNL_T url "
									+ "WHERE URL.UP_ROLES in ("
									+ rolesLineExist + ") "
									+ "and URL.UP_USERS= ? ")
							.setParameter(1, idUser).executeUpdate();

					// назначаем роли из заявки
					for (String role : roles_app) {

						em.createNativeQuery(
								"insert into AC_USERS_LINK_KNL_T (UP_ROLES, UP_USERS, CREATOR, CREATED) "
										+ "values(?, ?, ?, sysdate) ")
								.setParameter(1, new Long(role))
								.setParameter(2, idUser).setParameter(3, 0)
								.executeUpdate();
					}

				} else if (mode == 1) { // ADD

					for (String role : roles_app) {

						logger.info("createAccess:role2:" + role);

						if (!roles_user.contains(role)) {

							em.createNativeQuery(
									"insert into AC_USERS_LINK_KNL_T (UP_ROLES, UP_USERS, CREATOR, CREATED) "
											+ "values(?, ?, ?, sysdate) ")
									.setParameter(1, new Long(role))
									.setParameter(2, idUser).setParameter(3, 0)
									.executeUpdate();
						}
					}

				} else if (mode == 2) { // REMOVE
					// удаляем роли из заявки
					for (String role : roles_app) {

						if (rolesLineApp == null) {
							rolesLineApp = role;
						} else {
							rolesLineApp += ", " + role;
						}
					}

					em.createNativeQuery(
							"DELETE FROM AC_USERS_LINK_KNL_T url "
									+ "WHERE URL.UP_ROLES in (" + rolesLineApp
									+ ") " + "and URL.UP_USERS= ? ")
							.setParameter(1, idUser).executeUpdate();
				} else {
					// return;
				}

			}

			// текущие роли пользователя на возврат результата
			// решили не передавать в ответ
			// есть отдельный сервис для возврата данных (в том числе ролей) для
			// пользователей
			/*
			 * result=(List<String>)em.createNativeQuery(
			 * "select rl.SIGN_OBJECT "+ "from AC_ROLES_BSS_T rl, "+
			 * "AC_USERS_LINK_KNL_T url "+ "where RL.ID_SRV=URL.UP_ROLES "+
			 * "and RL.UP_IS=? "+ "and URL.UP_USERS=? ") .setParameter(1, idArm)
			 * .setParameter(2, idUser) .getResultList();
			 */
			// logger.info("access:result.size:"+result.size());

		} catch (GeneralFailure ge) {
			// sys_audit(70L, "" , "error", IPAddress, idUser );
			logger.error("access:Error:" + ge);
			throw ge;
		} catch (Exception e) {
			// sys_audit(70L, "" , "error", IPAddress, idUser );
			logger.error("access:Error:" + e);
			throw new GeneralFailure(e.getMessage());
		}

		// return result;
	}

	/**
	 * предоставление доступа пользователя по группам
	 */
	public void access_groups(String codeSystem, List<String> uidsUsers,
			String modeExec, List<String> codesGroups, Long idUserAuth,
			String IPAddress) throws GeneralFailure {

		// для систем и подсистем

		// modeExec:
		// 0 - REPLACE
		// 1 - ADD
		// 2 - REMOVE
		logger.info("access_groups:01");

		try {
			logger.info("access_groups:codeSystem:" + codeSystem);
			logger.info("access_groups:modeExec:" + modeExec);

			if (modeExec == null
					|| modeExec.trim().isEmpty()
					|| (!modeExec.equals("REPLACE") && !modeExec.equals("ADD") && !modeExec
							.equals("REMOVE")) || uidsUsers == null
					|| uidsUsers.isEmpty() || codeSystem == null
					|| codeSystem.trim().isEmpty() || codeSystem.length() > 240
					|| codesGroups == null || codesGroups.isEmpty()
					|| codesGroups.size() > max_size_roles) {
				throw new GeneralFailure("Некорректные данные!");
			}

			logger.info("access_groups:02");

			for (String uidUser : uidsUsers) {

				logger.info("access_groups:uidUser:" + uidUser);

				if (uidUser == null || uidUser.trim().isEmpty()) {
					throw new GeneralFailure("Некорректные данные!");
				}

				Long idGroupApp = null;
				String groupsLineExist = null;
				String groupsLineApp = null;
				List<String> groups_app = new ArrayList<String>();

				// решили определять пользователей извне ЦУД по их ИД, а не
				// логинам
				// Long idUser = user_exist(loginUser);
				Long idUser = new Long(uidUser);
				Long idArm = get_id_is(codeSystem);
				int mode = 1;

				if (modeExec.equals("REPLACE")) {
					mode = 0;
				} else if (modeExec.equals("ADD")) {
					mode = 1;
				} else if (modeExec.equals("REMOVE")) {
					mode = 2;
				}

				for (String group : codesGroups) {

					idGroupApp = group_exist(group);

					groups_app.add(idGroupApp.toString());

					logger.info("createAccess_groups:group:" + group);
					logger.info("createAccess_groups:idRoleApp:" + idGroupApp);
				}

				// список групп, которые уже есть у пользователя
				List<String> groups_user = (List<String>) em
						.createNativeQuery(
								"select to_char(rl.UP_GROUP_USERS) "
										+ "from LINK_GROUP_USERS_USERS_KNL_T rl "
										+ "where UP_USERS=? ")
						.setParameter(1, idUser).getResultList();

				if (mode == 0) { // REPLACE

					// удаляем имеющиеся у пользователя роли в системе
					for (String group : groups_user) {
						if (groupsLineExist == null) {
							groupsLineExist = group;
						} else {
							groupsLineExist += ", " + group;
						}

					}

					logger.info("createAccess_groups:rolesLine:"
							+ groupsLineExist);

					em.createNativeQuery(
							"DELETE FROM LINK_GROUP_USERS_USERS_KNL_T url "
									+ "WHERE URL.UP_GROUP_USERS in ("
									+ groupsLineExist + ") "
									+ "and URL.UP_USERS= ? ")
							.setParameter(1, idUser).executeUpdate();

					// назначаем роли из заявки
					for (String group : groups_app) {

						em.createNativeQuery(
								"insert into LINK_GROUP_USERS_USERS_KNL_T (UP_GROUP_USERS, UP_USERS, CREATOR, CREATED) "
										+ "values(?, ?, ?, sysdate) ")
								.setParameter(1, new Long(group))
								.setParameter(2, idUser).setParameter(3, 0)
								.executeUpdate();
					}

				} else if (mode == 1) { // ADD

					for (String group : groups_app) {

						logger.info("createAccess_groups:role2:" + group);

						if (!groups_user.contains(group)) {

							logger.info("createAccess_groups:role2_2");

							em.createNativeQuery(
									"insert into LINK_GROUP_USERS_USERS_KNL_T (UP_GROUP_USERS, UP_USERS, CREATOR, CREATED) "
											+ "values(?, ?, ?, sysdate) ")
									.setParameter(1, new Long(group))
									.setParameter(2, idUser).setParameter(3, 0)
									.executeUpdate();
						}
					}

				} else if (mode == 2) { // REMOVE
					// удаляем роли из заявки
					for (String group : groups_app) {

						if (groupsLineApp == null) {
							groupsLineApp = group;
						} else {
							groupsLineApp += ", " + group;
						}
					}

					em.createNativeQuery(
							"DELETE FROM LINK_GROUP_USERS_USERS_KNL_T url "
									+ "WHERE URL.UP_GROUP_USERS in ("
									+ groupsLineApp + ") "
									+ "and URL.UP_USERS= ? ")
							.setParameter(1, idUser).executeUpdate();
				} else {
					// return;
				}

			}

		} catch (GeneralFailure ge) {
			// sys_audit(70L, "" , "error", IPAddress, idUser );
			logger.error("access_groups:Error:" + ge);
			throw ge;
		} catch (Exception e) {
			// sys_audit(70L, "" , "error", IPAddress, idUser );
			logger.error("access_groups:Error:" + e);
			throw new GeneralFailure(e.getMessage());
		}
	}

	/**
	 * замена сертификата системы
	 */
	public void cert_change(String codeSystem, String newCert, Long idUserAuth,
			String IPAddress) throws GeneralFailure {

		// группа систем, система, подсистема

		X509Certificate cert_obj = null;
		try {
			logger.info("cert_change:01");

			if (newCert == null || newCert.isEmpty()) {
				throw new GeneralFailure("New Certificate is null or empty!");
			}

			try {

				StringBuilder builder = new StringBuilder();
				builder.append("-----BEGIN CERTIFICATE-----\n").append(newCert)
						.append("\n-----END CERTIFICATE-----");
				String derFormattedString = builder.toString();

				CertificateFactory certFactory = CertificateFactory
						.getInstance("X.509");
				// InputStream in = new ByteArrayInputStream(certData);
				InputStream in = new ByteArrayInputStream(
						derFormattedString.getBytes());
				cert_obj = (X509Certificate) certFactory
						.generateCertificate(in);
				// cert_obj создаём только для проверки переданного текста - что
				// из него можно получить сертификат
				// в базе храним текст base64 сертификата

			} catch (Exception e) {
				logger.error("cert_change:ErrorCertValid:" + e);
				throw new GeneralFailure("New Certificate is not valid!");
			}
			logger.info("cert_change:02:" + cert_obj);

			// сначала предполагаем, что имеем дело с системой
			int result_flag = em
					.createNativeQuery(
							"update AC_IS_BSS_T t1 " + "set T1.CERT_DATE=? "
									+ "where t1.SIGN_OBJECT=? ")
					.setParameter(1, newCert).setParameter(2, codeSystem)
					.executeUpdate();

			logger.info("cert_change:03:" + result_flag);

			if (result_flag == 0) {
				// не было изменённых записей, значит не нашли в системах такой
				// код, значит у нас подсистема
				result_flag = em
						.createNativeQuery(
								"update AC_SUBSYSTEM_CERT_BSS_T t1 "
										+ "set T1.CERT_DATE=? "
										+ "where t1.SUBSYSTEM_CODE=? ")
						.setParameter(1, newCert).setParameter(2, codeSystem)
						.executeUpdate();
			}

			if (result_flag == 0) {
				// не было изменённых записей, значит не нашли в системах и
				// подсистемах такой код, значит у нас подсистема
				result_flag = em
						.createNativeQuery(
								"update GROUP_SYSTEMS_KNL_T t1 "
										+ "set T1.CERT_DATA=? "
										+ "where t1.GROUP_CODE=? ")
						.setParameter(1, newCert).setParameter(2, codeSystem)
						.executeUpdate();
			}

			if (result_flag == 0) {
				throw new GeneralFailure("CodeSystem ['" + codeSystem
						+ "'] not found ! ");
			}

		} catch (GeneralFailure ge) {
			logger.error("cert_change:Error1:" + ge);
			throw ge;
		} catch (Exception e) {
			logger.error("cert_change:Error2:" + e);
			throw new GeneralFailure(e.getMessage());
		}

	}

	/**
	 * проверка наличия системы
	 */
	private Long system_exist(String codeSystem) throws GeneralFailure {

		logger.info("system_exist:codeSystem:" + codeSystem);

		Long result = null;

		try {

			result = ((java.math.BigDecimal) em
					.createNativeQuery(
							"select APP.ID_SRV " + "from AC_IS_BSS_T app "
									+ "where APP.SIGN_OBJECT=?")
					.setParameter(1, codeSystem).getSingleResult()).longValue();

			return result;

		} catch (NoResultException ex) {
			logger.error("system_exist:NoResultException");
			throw new GeneralFailure("Информационная система не определена!");
		} catch (Exception e) {
			logger.error("system_exist:Error:" + e);
			throw new GeneralFailure(e.getMessage());
		}
	}

	/**
	 * получение ИД системы по коду системы
	 */
	private Long get_id_is(String codeSys) throws GeneralFailure {

		Long result = null;

		try {
			result = ((java.math.BigDecimal) em
					.createNativeQuery(
							"select SYS.ID_SRV "
									+ "from  AC_IS_BSS_T sys, "
									+ "AC_SUBSYSTEM_CERT_BSS_T subsys "
									+ "where (SYS.SIGN_OBJECT= :codeSys or  SUBSYS.SUBSYSTEM_CODE= :codeSys) "
									+ "and  SUBSYS.UP_IS(+) =SYS.ID_SRV "
									+ "group by SYS.ID_SRV ")
					.setParameter("codeSys", codeSys).getSingleResult())
					.longValue();

		} catch (NoResultException ex) {
			logger.error("get_id_is:NoResultException");
			throw new GeneralFailure("System is not defined");
		} catch (Exception e) {
			throw new GeneralFailure(e.getMessage());
		}
		return result;

	}

	/**
	 * проверка наличия пользователя по его ИД
	 */
	private Long user_exist(String loginUser) throws GeneralFailure {

		logger.info("user_exist:loginUser:" + loginUser);

		Long result = null;

		try {

			result = ((java.math.BigDecimal) em
					.createNativeQuery(
							"select usr.ID_SRV " + "from  AC_USERS_KNL_T usr "
									+ "where usr.login=?")
					.setParameter(1, loginUser).getSingleResult()).longValue();

			return result;

		} catch (NoResultException ex) {
			logger.error("user_exist:NoResultException");
			throw new GeneralFailure("Пользователь не определён!");
		} catch (Exception e) {
			logger.error("system_exist:Error:" + e);
			throw new GeneralFailure(e.getMessage());
		}
	}

	/**
	 * проверка наличия роли по её коду
	 */
	private Long role_exist(Long idSystem, String codeRole)
			throws GeneralFailure {

		logger.info("role_exist:idSystem:" + idSystem);
		logger.info("role_exist:codeRole:" + codeRole);

		Long result = null;

		try {

			result = ((java.math.BigDecimal) em
					.createNativeQuery(
							"select RL.ID_SRV " + "from AC_ROLES_BSS_T rl "
									+ "where RL.UP_IS=? "
									+ "and rl.SIGN_OBJECT= ? ")
					.setParameter(1, idSystem).setParameter(2, codeRole)
					.getSingleResult()).longValue();

		} catch (NoResultException ex) {
			logger.error("role_exist:NoResultException");
			throw new GeneralFailure("Роль " + codeRole + " не определёна!");
		} catch (Exception e) {
			logger.info("role_error:Error:" + e);
			throw new GeneralFailure(e.getMessage());
		}

		return result;
	}

	/**
	 * проверка наличия группы по её коду
	 */
	private Long group_exist(String codeGroup) throws GeneralFailure {

		logger.info("role_exist:codeGroup:" + codeGroup);

		Long result = null;

		try {

			result = ((java.math.BigDecimal) em
					.createNativeQuery(
							"select RL.ID_SRV " + "from GROUP_USERS_KNL_T rl "
									+ "where rl.SIGN_OBJECT= ? ")
					.setParameter(1, codeGroup).getSingleResult()).longValue();

		} catch (NoResultException ex) {
			logger.error("group_exist:NoResultException");
			throw new GeneralFailure("Группа " + codeGroup + " не определёна!");
		} catch (Exception e) {
			logger.error("group_exist:Error:" + e);
			throw new GeneralFailure(e.getMessage());
		}

		return result;
	}

	/**
	 * протоколирование действий
	 */
	private void sys_audit(Long idServ, String inp_param, String result,
			String ip_adr, Long idUser) {
		logger.info("sys_audit:01");
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

}
