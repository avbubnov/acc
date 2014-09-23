package ru.spb.iac.cud.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import ru.spb.iac.cud.core.util.CUDConstants;
import ru.spb.iac.cud.core.util.HashPassword;
import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.InvalidCredentials;
import ru.spb.iac.cud.exceptions.RevokedCertificate;
import ru.spb.iac.cud.items.AuditFunction;
import ru.spb.iac.cud.items.AuthMode;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import ch.qos.logback.classic.LoggerContext;
//import ch.qos.logback.core.util.StatusPrinter;

/**
 * EJB для аутентификации и аудита
 * 
 * @author bubnov
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class AccessManager implements AccessManagerLocal, AccessManagerRemote {

	@PersistenceContext(unitName = "AuthServices")
	EntityManager em;

	static {
		// System.setProperty("logback.configurationFile",
		// "/Development/test/log/logback.xml");
	}

	Logger logger = LoggerFactory.getLogger(AccessManager.class);

	private static String reestr_path = System
			.getProperty("jboss.server.config.dir")
			+ "/"
			+ "crl_reestr.properties";

	public AccessManager() {
	}

	/**
	 * аутентификация пользователя по логин/паролю
	 */
	public String authenticate_login(String login, String password,
			AuthMode authMode, String IPAddress, String codeSys)
			throws GeneralFailure, InvalidCredentials {

		logger.info("authenticate_login:login:" + login);

		// LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		// StatusPrinter.print(lc);

		Object[] dataUser = null;

		Long idUser = null;
		String loginUser = null;

		Long authModeValue = null;

		if (authMode.equals(AuthMode.WEB_SERVICES)) {
			authModeValue = 2L;
		} else if (authMode.equals(AuthMode.HTTP_REDIRECT)) {
			authModeValue = 97L;
		} else if (authMode.equals(AuthMode.HTTP_REDIRECT_EXT_AUTH_OPEN)) {
			authModeValue = 99L;
		} else if (authMode.equals(AuthMode.HTTP_REDIRECT_EXT_AUTH_ENCRYPT)) {
			authModeValue = 79L;
		}

		try {

			if (authModeValue.equals(78L)) {
				logger.info("authenticate:01");

				dataUser = (Object[]) em
						.createNativeQuery(
								"select AU.ID_SRV, AU.login, AU.PASSWORD_ "
										+ "from "
										+ "AC_USERS_KNL_T au "
										+ "where AU.LOGIN=? "
										+ "and (AU.START_ACCOUNT is null or au.START_ACCOUNT <= sysdate) "
										+ "and (AU.START_ACCOUNT is null or au.START_ACCOUNT > sysdate) "
										+ "and AU.STATUS = 1 ")
						.setParameter(1, login).getSingleResult();

				boolean matched = HashPassword.validatePassword(
						(dataUser[2] != null ? dataUser[2].toString() : ""),
						password);

				if (!matched) {
					throw new NoResultException();
				}
				idUser = ((java.math.BigDecimal) dataUser[0]).longValue();
				loginUser = dataUser[1].toString();
				logger.info("authenticate:02");

			} else {

				logger.info("authenticate:03");

				dataUser = (Object[]) em
						.createNativeQuery(
								"select AU.ID_SRV, AU.login "
										+ "from "
										+ "AC_USERS_KNL_T au "
										+ "where AU.LOGIN=? "
										+ "and AU.PASSWORD_=? "
										+ "and (AU.START_ACCOUNT is null or au.START_ACCOUNT <= sysdate) "
										+ "and (AU.START_ACCOUNT is null or au.START_ACCOUNT > sysdate) "
										+ "and AU.STATUS = 1 ")
						.setParameter(1, login).setParameter(2, password)
						.getSingleResult();

				idUser = ((java.math.BigDecimal) dataUser[0]).longValue();
				loginUser = dataUser[1].toString();

				logger.info("authenticate:04");
			}

			sys_audit(authModeValue, "login:" + login + "; passw:***", "true",
					IPAddress, idUser, codeSys);

			return loginUser;

		} catch (NoResultException ex) {

			sys_audit(authModeValue, "login:" + login + "; passw:" + password,
					"false", IPAddress, null, codeSys);

			logger.error("authenticate:NoResultException");
			throw new InvalidCredentials("Учетной записи нет в системе!");
		} catch (Exception e) {

			sys_audit(authModeValue, "login:" + login + "; passw:***", "error",
					IPAddress, idUser, codeSys);

			logger.error("authenticate:Error:" + e);
			throw new GeneralFailure(e.getMessage());
		}
	}

	/**
	 * аутентификация пользователя по токену от другой системы
	 * @param login логин пользователя
	 */
	public Long authenticate_login_obo(String login, AuthMode authMode,
			String IPAddress, String codeSys) throws GeneralFailure {

		// !!!
		// пока метод не используется
		// !!!
		// отказались от передачи токена целиком
		// передаём token_id
		// а в нём указан uid пользователя

		logger.info("aauthenticate_login:login_obo:" + login);

		Long idUser = null;
		Long authModeValue = null;

		// authMode.equals(AuthMode.WEB_SERVICES)
		authModeValue = 82L;

		try {

			logger.info("authenticate_login_obo:01");
			idUser = ((java.math.BigDecimal) em
					.createNativeQuery(
							"select AU.ID_SRV "
									+ "from "
									+ "AC_USERS_KNL_T au "
									+ "where AU.LOGIN=? "
									+ "and (AU.START_ACCOUNT is null or au.START_ACCOUNT <= sysdate) "
									+ "and (AU.START_ACCOUNT is null or au.START_ACCOUNT > sysdate) "
									+ "and AU.STATUS = 1 ")
					.setParameter(1, login).getSingleResult()).longValue();

			logger.info("authenticate_login_obo:idUser:"
					+ idUser);

			sys_audit(authModeValue, "login:" + login + "; passw:***", "true",
					IPAddress, idUser, codeSys);

			return idUser;

		} catch (NoResultException ex) {

			sys_audit(authModeValue, "login:" + login + "; ", "false",
					IPAddress, null, codeSys);

			logger.error("authenticate_login_obo:NoResultException");
			throw new GeneralFailure("Учетной записи нет в системе!");
		} catch (Exception e) {

			sys_audit(authModeValue, "login:" + login + "; passw:***", "error",
					IPAddress, idUser, codeSys);

			logger.error("authenticate_login_obo:Error:" + e);
			throw new GeneralFailure(e.getMessage());
		}
	}

	/**
	 * аутентификация пользователя по токену от другой системы
	 * @param uid уид пользователя
	 */
	public String authenticate_uid_obo(String uid, AuthMode authMode,
			String IPAddress, String codeSys) throws GeneralFailure {

		logger.info("authenticate_uid_obo:" + uid);

		String loginUser = null;
		Long authModeValue = null;

		// authMode.equals(AuthMode.WEB_SERVICES)
		authModeValue = 82L;

		if (uid == null || uid.trim().isEmpty()) {
			throw new GeneralFailure("Учетной записи нет в системе!");
		}

		try {

			logger.info("authenticate_login_obo:01");

			loginUser = ((String) em
					.createNativeQuery(
							"select AU.LOGIN "
									+ "from "
									+ "AC_USERS_KNL_T au "
									+ "where AU.ID_SRV=? "
									+ "and (AU.START_ACCOUNT is null or au.START_ACCOUNT <= sysdate) "
									+ "and (AU.START_ACCOUNT is null or au.START_ACCOUNT > sysdate) "
									+ "and AU.STATUS = 1 ")
					.setParameter(1, new Long(uid)).getSingleResult());

			logger.info("authenticate_login_obo:loginUser:"
					+ loginUser);

			sys_audit(authModeValue, "uid:" + uid + "; passw:***", "true",
					IPAddress, new Long(uid), codeSys);

			return loginUser;

		} catch (NoResultException ex) {

			sys_audit(authModeValue, "uid:" + uid + "; ", "false", IPAddress,
					null, codeSys);

			logger.error("authenticate_uid_obo:NoResultException");
			throw new GeneralFailure("Учетной записи нет в системе!");
		} catch (Exception e) {

			sys_audit(authModeValue, "uid:" + uid + "; passw:***", "error",
					IPAddress, new Long(uid), codeSys);

			logger.error("authenticate_uid_obo:Error:" + e);
			throw new GeneralFailure(e.getMessage());

		}
	}
	/**
	 * аутентификация пользователя по сертификату
	 * @param sn серийный номер сертификата пользователя
	 */
	public String authenticate_cert_sn(String sn, AuthMode authMode,
			String IPAddress, String codeSys) throws GeneralFailure,
			InvalidCredentials, RevokedCertificate {

		logger.info("authenticate_cert_sn:01");

		Object[] dataUser = null;

		Long idUser = null;
		String loginUser = null;
		Long authModeValue = null;

		if (authMode.equals(AuthMode.WEB_SERVICES)) {
			authModeValue = 62L;
		} else if (authMode.equals(AuthMode.HTTP_REDIRECT)) {
			authModeValue = 98L;
		}

		if (sn == null) {
			sys_audit(2L, "sn:" + sn, "error:Serial Number is Empty!",
					IPAddress, null, codeSys);
			throw new GeneralFailure("Serial Number is Empty!");
		}

		sn = sn.replaceAll(" ", "").toUpperCase();

		try {

			logger.info("authenticate_cert_sn:02+:" + sn);

			// с учётом множественности сертификатов
			dataUser = ((Object[]) em
					.createNativeQuery(
							"select AU.ID_SRV, AU.login "
									+ "from "
									+ "AC_USERS_KNL_T au, "
									+ "AC_USERS_CERT_BSS_T auc "
									+ "where (UPPER(AU.CERTIFICATE)= UPPER(:certNum) or UPPER(AUC.CERT_NUM) = UPPER(:certNum)  ) "
									+ "and (AU.START_ACCOUNT is null or au.START_ACCOUNT <= sysdate) "
									+ "and (AU.START_ACCOUNT is null or au.START_ACCOUNT > sysdate) "
									+ "and AU.STATUS = 1 "
									+ "and AU.ID_SRV = AUC.UP_USER(+) ")
					.setParameter("certNum", sn).getSingleResult());

			idUser = ((java.math.BigDecimal) dataUser[0]).longValue();
			loginUser = dataUser[1].toString();

			logger.info("authenticate_cert_sn:03+:" + loginUser);

			if (isRevoked(get_reestr("curr_crl"), sn)) {
				throw new RevokedCertificate("Сертификат отозван!");
			}

			logger.info("authenticate_cert_sn:04");

			logger.info("authenticate_cert_sn:06");

			sys_audit(authModeValue, "sn:" + sn, "true", IPAddress, idUser,
					codeSys);

			logger.info("authenticate_cert_sn:07");

			return loginUser;

		} catch (NoResultException ex) {

			sys_audit(authModeValue, "sn:" + sn, "false", IPAddress, null,
					codeSys);

			logger.error("authenticate_cert_sn:NoResultException");
			throw new InvalidCredentials("Учетной записи нет в системе!");
		} catch (RevokedCertificate er) {

			sys_audit(authModeValue, "sn:" + sn, "false, RevokedCertificate",
					IPAddress, null, codeSys);

			logger.error("authenticate_cert_sn:RevokedCertificate");
			throw er;

		} catch (Exception e) {

			sys_audit(authModeValue, "sn:" + sn, "error", IPAddress, idUser,
					codeSys);

			logger.error("authenticate_cert_sn:Error+:" + e);

			throw new GeneralFailure(e.getMessage());
		}
	}

	/**
	 * аудит действий пользователя
	 */
	public void audit_pro(String codeSys, String login,
			List<AuditFunction> userFunctions, Long idUserAuth, String IPAddress)
			throws GeneralFailure {

		logger.info("audit:01");

		HashMap<String, Long> act_cl = new HashMap<String, Long>();
		Long idFunc = null;
		Long idIS = null;
		Long idUserSubject = null;

		if (userFunctions == null || userFunctions.isEmpty()) {
			logger.info("audit:return");
			return;
		}

		try {

			if (codeSys == null) {
				logger.info("audit:System Code is null");
				throw new GeneralFailure("System ID is null");
			}
			if (login == null) {
				logger.info("audit:User login is nul");
				throw new GeneralFailure("User login is null");
			}

			idIS = get_id_is(codeSys);
			// решили определять пользователей извне ЦУД по их ИД, а не логинам
			// idUser = get_id_user(login);
			idUserSubject = new Long(login);

			logger.info("audit:02:idIS:" + idIS);
			logger.info("audit:03:idUserSubject:" + idUserSubject);

			List<Object[]> lo = em
					.createNativeQuery(
							"select ACT.SIGN_OBJECT, ACT.ID_SRV "
									+ "from ACTIONS_BSS_T act, "
									+ "AC_IS_BSS_T app "
									+ "where APP.ID_SRV = ACT.UP_IS "
									+ "and APP.ID_SRV=?").setParameter(1, idIS)
					.getResultList();
			logger.info("audit:04");

			for (Object[] objectArray : lo) {
				act_cl.put(
						objectArray[0] != null ? objectArray[0].toString() : "",
						objectArray[1] != null ? new Long(objectArray[1]
								.toString()) : -1L);
			}

			for (AuditFunction func : userFunctions) {
				idFunc = act_cl.get(func.getCodeFunction());
				logger.info("audit:05:func.getIdFunction():"
						+ func.getCodeFunction());
				logger.info("audit:06:idFunc:" + idFunc);

				if (func.getDetailsFunction() != null
						&& func.getDetailsFunction().length() > 500) {
					throw new GeneralFailure(
							"Field length 'DetailsFunction' is more than 500 characters! ");
				}

				if (idFunc != null && func.getDateFunction() != null) {

					// насчёт func.getDateFunction подумать:
					// выдавать исключение или просто эту запись не вставлять

					/*
					 * if(func.getDateFunction()==null){ throw new
					 * GeneralFailure("Отсутствует код роли!"); }
					 */

					em.createNativeQuery(
							"insert into ACTIONS_LOG_KNL_T(ID_SRV,  UP_ACTIONS, UP_USERS, DATE_ACTION, DETAILS_ACTION, created) "
									+ "values(ACTIONS_LOG_KNL_SEQ.nextval, ?, ?, ?, ?, sysdate) ")
							.setParameter(1, idFunc)
							.setParameter(2, idUserSubject)
							.setParameter(3, func.getDateFunction(),
									TemporalType.TIMESTAMP)
							.setParameter(4, func.getDetailsFunction())
							.executeUpdate();
				}
			}

			sys_audit(6L, "idIS:" + idIS + "; size of funct_list: "
					+ userFunctions.size(), "true", IPAddress, idUserAuth,
					codeSys);

		} catch (Exception e) {
			sys_audit(6L, "idIS:" + idIS + "; size of funct_list: "
					+ userFunctions.size(), "error", IPAddress, idUserAuth,
					codeSys);

			logger.error("audit:Error:" + e);
			throw new GeneralFailure(e.getMessage());
		}
	}

	/**
	 * изменение пароля пользователя
	 */
	public void change_password(String login, String password,
			String new_password, String IPAddress) throws GeneralFailure,
			InvalidCredentials {

		// !!!
		// пока метод не используется

		logger.info("change_password:login:" + login);

		Long idUser = null;
		try {
			logger.info("change_password:01");
			idUser = ((java.math.BigDecimal) em
					.createNativeQuery(
							"select AU.ID_SRV "
									+ "from "
									+ "AC_USERS_KNL_T au "
									+ "where AU.LOGIN=? "
									+ "and AU.PASSWORD_=? "
									+ "and (AU.START_ACCOUNT is null or au.START_ACCOUNT <= sysdate) "
									+ "and (AU.START_ACCOUNT is null or au.START_ACCOUNT > sysdate) ")
					.setParameter(1, login).setParameter(2, password)
					.getSingleResult()).longValue();

			logger.info("change_password:idUser:" + idUser);

			if (new_password == null || new_password.trim().equals("")) {
				throw new GeneralFailure("Некорректный новый пароль!");
			}

			em.createNativeQuery(
					"UPDATE AC_USERS_KNL_T au " + "SET au.PASSWORD_=? "
							+ "WHERE au.ID_SRV=? ")
					.setParameter(1, new_password).setParameter(2, idUser)
					.executeUpdate();

			sys_audit(22L, "login:" + login + "; passw:***", "true", IPAddress,
					idUser, null);

		} catch (NoResultException ex) {

			sys_audit(22L, "login:" + login + "; passw:***", "false",
					IPAddress, null, null);

			logger.error("change_password:NoResultException");
			throw new InvalidCredentials("Учетной записи нет в системе!");

		} catch (GeneralFailure e) {

			sys_audit(22L, "login:" + login + "; passw:***",
					"error: empty new password", IPAddress, idUser, null);

			logger.error("change_password:Error:" + e);
			throw e;

		} catch (Exception e) {

			// sys_audit(22L, "login:"+login+"; passw:***", "error", IPAddress,
			// idUser );

			logger.error("change_password:Error:" + e);
			throw new GeneralFailure(e.getMessage());
		}
	}

	/**
	 * проверка корректности ид системы
	 */
	public void is_exist(String idIS) throws GeneralFailure {

		logger.info("AuthManager:is_exist:idIS:" + idIS);

		try {

			em.createNativeQuery(
					"select APP.ID_SRV " + "from AC_IS_BSS_T app "
							+ "where APP.SIGN_OBJECT=?").setParameter(1, idIS)
					.getSingleResult();

		} catch (NoResultException ex) {
			logger.error("createAuth:NoResultException");
			throw new GeneralFailure("Информационная система не определёна!");
		} catch (Exception e) {
			logger.error("is_exist:Error:" + e);
			throw new GeneralFailure(e.getMessage());
		}
	}

	/**
	 * аудит вызовов сервисов (кроме аутентификации систем и logout)
	 */
	private void sys_audit(Long idServ, String inp_param, String result,
			String ip_adr, Long idUser, String codeSys) {
		logger.info("sys_audit");

		Long idSys = null;
		Long idGrSys = null;
		try {

			if (codeSys != null) {

				// ищем код в системах и подсистемах
				if (codeSys.startsWith(CUDConstants.armPrefix)
						|| codeSys.startsWith(CUDConstants.subArmPrefix)) {

					idSys = get_id_is(codeSys);

				} else if (codeSys.startsWith(CUDConstants.groupArmPrefix)) {
					// группы систем

					idGrSys = ((java.math.BigDecimal) em
							.createNativeQuery(
									"select T1.ID_SRV "
											+ "from GROUP_SYSTEMS_KNL_T t1 "
											+ "where T1.GROUP_CODE=? ")
							.setParameter(1, codeSys).getSingleResult())
							.longValue();

				}
			}

			if (idUser != null && !idUser.equals(-1L)) {
				em.createNativeQuery(
						"insert into SERVICES_LOG_KNL_T( "
								+ "ID_SRV,  UP_SERVICES, DATE_ACTION, CREATED, "
								+ "input_param, result_value, ip_address, UP_USERS, "
								+ "UP_SYS, UP_GR_SYS ) "
								+ "values(SERVICES_LOG_KNL_SEQ.nextval , ?, sysdate, sysdate, "
								+ "?, ?, ?, ?, ?, ? ) ")
						.setParameter(1, idServ).setParameter(2, inp_param)
						.setParameter(3, result).setParameter(4, ip_adr)
						.setParameter(5, idUser)
						.setParameter(6, idSys != null ? idSys : "")
						.setParameter(7, idGrSys != null ? idGrSys : "")
						.executeUpdate();
			} else {
				em.createNativeQuery(
						"insert into SERVICES_LOG_KNL_T( "
								+ "ID_SRV,  UP_SERVICES, DATE_ACTION, CREATED, "
								+ "input_param, result_value, ip_address, "
								+ "UP_SYS, UP_GR_SYS ) "
								+ "values(SERVICES_LOG_KNL_SEQ.nextval , ?, sysdate, sysdate, "
								+ "?, ?, ?, ?, ? ) ").setParameter(1, idServ)
						.setParameter(2, inp_param).setParameter(3, result)
						.setParameter(4, ip_adr)
						.setParameter(5, idSys != null ? idSys : "")
						.setParameter(6, idGrSys != null ? idGrSys : "")
						.executeUpdate();
			}
		} catch (Exception e) {
			logger.error("sys_audit:Error:" + e);
		}

	}

	/**
	 * аудит вызовов сервисов аутентификации систем и logout
	 */
	public void sys_audit_public(Long idServ, String inp_param, String result,
			String ip_adr, Long idUser, String loginUser, String codeSys) {

		// используется при аутентификации системы и logout

		logger.info("sys_audit_public:01");
		try {
			// при аудите logout
			// передаётся логин, а не ид пользователя

			if (idUser == null && loginUser != null) {

				try {
					// если по логину не найдём, то get_id_user выдаст ошибку
					// и мы до sys_audit не дойдём
					// поэтому берём get_id_user в try{}catch(){}

					idUser = get_id_user(loginUser);

				} catch (Exception e) {
					logger.error("sys_audit_public:02");
				}
			}
			sys_audit(idServ, inp_param, result, ip_adr, idUser, codeSys);

		} catch (Exception e) {
			logger.error("sys_audit_public:Error:" + e);
		}

	}

	/**
	 * определение файла с реестром отозванных сертификатов
	 */
	public String get_reestr(String prop_name) {
		Properties properties = new Properties();
		InputStream is = null;
		String result = null;

		logger.info("get_reestr");

		try {

			File f = new File(reestr_path);

			if (f.exists()) {

				properties.load(is = new FileInputStream(f));

				result = properties.getProperty(prop_name);

				logger.info("get_reestr:result:" + result);
			}

		} catch (Exception e) {
			logger.error("initTask:error:" + e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
				logger.error("get_reestr:finally:is:error:" + e);
			}
		}
		return result;
	}

	/**
	 * проверка сертификата на наличие в реестре отозванных сертификатов
	 */
	private synchronized boolean isRevoked(String curr_crl, String hex) {

		boolean result = false;

		CertificateFactory cf = null;
		InputStream is = null;
		Collection<? extends X509CRL> x509Crls = null;
		File crlFile = null;

		try {

			crlFile = new File(curr_crl);

			logger.info("isRevoked:01");

			cf = CertificateFactory.getInstance("X.509");

			x509Crls = (Collection<? extends X509CRL>) cf
					.generateCRLs(is = new FileInputStream(crlFile));

			logger.info("isRevoked:02");

			// List<CRL> cpCrls = new ArrayList<CRL>(x509Crls.size());

			for (X509CRL crl : x509Crls) {

			
				X509CRLEntry xce = crl.getRevokedCertificate(hex_to_dec(hex));

				if (xce != null) {
					return true;
				}
				logger.info("isRevoked:revoked:" + (xce == null));
				// cpCrls.add(crl);
			}

			logger.info("isRevoked:03");

		} catch (Exception e) {
			logger.error("isRevoked:error:" + e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
				logger.error("isRevoked:finally:is:error:" + e);
			}
		}

		return result;
	}

	/**
	 * конвертирование серийного номера сертификата в десятичный формат
	 */
	private BigInteger hex_to_dec(String hex) {

		BigInteger bi = null;

		try {

			bi = new BigInteger(hex, 16);

			logger.info("hex_to_dec:num:" + bi);
		} catch (NumberFormatException e) {
			logger.error("hex_to_dec:Error:" + e);
		}
		return bi;

	}

	/**
	 * определение ид системы по её коду
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
	 * определение уид пользователя по его логину
	 */
	private Long get_id_user(String login) throws GeneralFailure {

		logger.info("get_id_user:login:" + login);

		Long result = null;
		try {
			result = ((java.math.BigDecimal) em
					.createNativeQuery(
							"select AU.ID_SRV "
									+ "from "
									+ "AC_USERS_KNL_T au "
									+ "where AU.LOGIN=? "
									+ "and (AU.START_ACCOUNT is null or au.START_ACCOUNT <= sysdate) "
									+ "and (AU.START_ACCOUNT is null or au.START_ACCOUNT > sysdate) ")
					.setParameter(1, login).getSingleResult()).longValue();

		} catch (NoResultException ex) {

			logger.error("get_id_user:NoResultException");
			throw new GeneralFailure("User is not defined!");

		} catch (Exception e) {

			logger.error("get_id_user:Error:" + e);
			throw new GeneralFailure(e.getMessage());
		}

		return result;
	}

}