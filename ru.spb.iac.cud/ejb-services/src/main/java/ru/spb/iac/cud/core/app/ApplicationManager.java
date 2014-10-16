package ru.spb.iac.cud.core.app;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap; import java.util.Map;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import ru.spb.iac.cud.core.util.CUDConstants;
import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.items.app.AppAttribute;
import ru.spb.iac.cud.items.app.AppAccept;
import ru.spb.iac.cud.items.app.AppSystemAttributesClassif;
import ru.spb.iac.cud.items.app.AppUserAttributesClassif;
import ru.spb.iac.cud.util.TIDEncode;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.apache.xml.security.utils.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EJB для обработки заявок
 * 
 * @author bubnov
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@TransactionManagement(TransactionManagementType.BEAN)
public class ApplicationManager implements ApplicationManagerLocal,
		ApplicationManagerRemote {

	@PersistenceContext(unitName = "AuthServices")
	EntityManager em;

	@Resource
	UserTransaction utx;

	private static int max_size_attributes = 50;

	private static int max_size_roles = 500;

	final static Logger LOGGER = LoggerFactory.getLogger(ApplicationManager.class);

	public ApplicationManager() {
	}

	/**
	 * регистрация инф системы
	 */
	private AppAccept system_registration(String fullNameSystem,
			String shortNameSystem, String descriptionSystem, String principal,
			Long idUser, String IPAddress) throws GeneralFailure {

		LOGGER.debug("system_registration:01:" + principal);

		AppAccept result = new AppAccept();

		try {

			if (fullNameSystem == null || fullNameSystem.trim().isEmpty()
					|| fullNameSystem.length() > 100 || shortNameSystem == null
					|| shortNameSystem.trim().isEmpty()
					|| shortNameSystem.length() > 50
					|| descriptionSystem != null
					&& descriptionSystem.length() > 500) {
				throw new GeneralFailure("Некорректные данные в заявке!");
			}

			utx.begin();

			List results = em.createNativeQuery(
					"select JOURN_APP_SYSTEM_SEQ.nextval from dual ")
					.getResultList();
			Long number = ((BigDecimal) results.get(0)).longValue();

			String secret = TIDEncode.getSecret();

			result.setDate(new Date());
			result.setNumber(number.toString());
			result.setSecret(secret);

			LOGGER.debug("system_registration:number:" + number);

			em.createNativeQuery(
					"insert into JOURN_APP_SYSTEM_BSS_T (ID_SRV, FULL_NAME, SHORT_NAME, "
							+ "DESCRIPTION, UP_USER, SECRET) "
							+ " values ( ?, ?, ?, ?, ?, ? ) ")
					.setParameter(1, number).setParameter(2, fullNameSystem)
					.setParameter(3, shortNameSystem)
					.setParameter(4, descriptionSystem).setParameter(5, idUser)
					.setParameter(6, secret).executeUpdate();

			LOGGER.debug("system_registration:02");

			utx.commit();

			// sys_audit
			
			
		} catch (GeneralFailure eg) {
			// sys_audit
			try {
				utx.rollback();
			} catch (Exception er) {
				LOGGER.error("rollback:Error:", er);
			}
			LOGGER.error("system_registration:ErrorEG:" + eg);
			throw eg;
		} catch (Exception e) {
			// sys_audit
			try {
				utx.rollback();
			} catch (Exception er) {
				LOGGER.error("rollback:Error:", er);
			}
			LOGGER.error("system_registration:Error:", e);
			throw new GeneralFailure(e.getMessage());
		}
		return result;
	}

	/**
	 * регистрация пользоавтеля
	 */
	private AppAccept user_registration(String surnameUser, String nameUser,
			String patronymicUser, String iogvCodeUser, String positionUser,
			String emailUser, String phoneUser, String certificateUser,
			String nameDepartament, String nameOrg, String iogvCodeOrg,
			String principal, Long idUser, String IPAddress)
			throws GeneralFailure {

		// idUser может = -1L
		// это значит заявка от самого пользователя на себя

		LOGGER.debug("user_registration:01:" + idUser);

		AppAccept result = new AppAccept();

		try {

			if (surnameUser == null || surnameUser.trim().isEmpty()
					|| surnameUser.length() > 45 || nameUser == null
					|| nameUser.trim().isEmpty() || nameUser.length() > 45
					|| patronymicUser == null
					|| patronymicUser.trim().isEmpty()
					|| patronymicUser.length() > 45 || nameOrg == null
					|| nameOrg.trim().isEmpty() || nameOrg.length() > 500
					|| iogvCodeUser != null && iogvCodeUser.length() > 50
					|| positionUser != null && positionUser.length() > 255
					|| emailUser != null && emailUser.length() > 50
					|| phoneUser != null && phoneUser.length() > 50
					|| certificateUser != null && certificateUser.length() > 50
					|| nameDepartament != null
					&& nameDepartament.length() > 255 || iogvCodeOrg != null
					&& iogvCodeOrg.length() > 50) {
				throw new GeneralFailure("Некорректные данные в заявке!");
			}

			utx.begin();

			List results = em.createNativeQuery(
					"select JOURN_APP_USER_SEQ.nextval from dual ")
					.getResultList();

			Long number = ((BigDecimal) results.get(0)).longValue();

			String secret = TIDEncode.getSecret();

			result.setDate(new Date());
			result.setNumber(number.toString());
			result.setSecret(secret);

			LOGGER.debug("user_registration:number:" + number);

			em.createNativeQuery(
					"insert into JOURN_APP_USER_BSS_T (ID_SRV, SURNAME_USER, NAME_USER, PATRONYMIC_USER, "
							+ "SIGN_USER, POSITION_USER, EMAIL_USER, PHONE_USER, "
							+ "CERTIFICATE_USER, NAME_DEPARTAMENT, NAME_ORG, SIGN_ORG, "
							+ "UP_USER, SECRET ) "
							+ " values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ")
					.setParameter(1, number).setParameter(2, surnameUser)
					.setParameter(3, nameUser).setParameter(4, patronymicUser)
					.setParameter(5, iogvCodeUser)
					.setParameter(6, positionUser).setParameter(7, emailUser)
					.setParameter(8, phoneUser)
					.setParameter(9, certificateUser)
					.setParameter(10, nameDepartament)
					.setParameter(11, nameOrg).setParameter(12, iogvCodeOrg)
					.setParameter(13, (!idUser.equals(-1L) ? idUser : ""))
					.setParameter(14, secret).executeUpdate();

			LOGGER.debug("user_registration:02");

			utx.commit();

			// sys_audit

		} catch (GeneralFailure eg) {
			// sys_audit
			try {
				utx.rollback();
			} catch (Exception er) {
				LOGGER.error("rollback:Error:", er);
			}
			LOGGER.error("user_registration:ErrorEG:" + eg);
			throw eg;
		} catch (Exception e) {
			// sys_audit
			try {
				utx.rollback();
			} catch (Exception er) {
				LOGGER.error("rollback:Error:", er);
			}
			LOGGER.error("user_registration:Error:", e);
			throw new GeneralFailure(e.getMessage());
		}
		return result;
	}

	/**
	 * регистрация инф системы
	 */
	public AppAccept system_registration(List<AppAttribute> attributes,
			String principal, Long idUser, String IPAddress)
			throws GeneralFailure {

		LOGGER.debug("system_registration:");

		// principal - принудительно = null
		// idUser - это действующий id user

		// добавить REG_NUM_SYS

		try {

			if (attributes == null || attributes.isEmpty()
					|| attributes.size() > max_size_attributes) {
				throw new GeneralFailure("Некорректные данные в заявке!");
			}

			HashMap<String, String> atMap = new HashMap<String, String>();

			for (AppAttribute attr : attributes) {
				 
				 
				if (attr.getName() != null) {
					atMap.put(attr.getName(), attr.getValue());
				}
			}
			return system_registration(
					atMap.get(AppSystemAttributesClassif.FULL_NAME_SYS.name()),
					atMap.get(AppSystemAttributesClassif.SHORT_NAME_SYS.name()),
					atMap.get(AppSystemAttributesClassif.DESCRIPTION_SYS.name()),
					principal, idUser, IPAddress);

		} catch (Exception e) {
			// sys_audit

			LOGGER.error("system_registration:Error:", e);
			throw new GeneralFailure(e.getMessage());
		}
	}

	/**
	 * регистрация пользователя
	 */
	public AppAccept user_registration(List<AppAttribute> attributes,
			String principal, Long idUser, String IPAddress)
			throws GeneralFailure {

		// principal - принудительно = null
		// idUser - это действующий id user

		LOGGER.debug("user_registration:");

		try {

			if (attributes == null || attributes.isEmpty()
					|| attributes.size() > max_size_attributes) {
				throw new GeneralFailure("Некорректные данные в заявке!");
			}

			HashMap<String, String> atMap = new HashMap<String, String>();

			for (AppAttribute attr : attributes) {
				 
				 
				if (attr.getName() != null) {
					atMap.put(attr.getName(), attr.getValue());
				}
			}

			return user_registration(
					atMap.get(AppUserAttributesClassif.SURNAME_USER.name()),
					atMap.get(AppUserAttributesClassif.NAME_USER.name()),
					atMap.get(AppUserAttributesClassif.PATRONYMIC_USER.name()),
					atMap.get(AppUserAttributesClassif.IOGV_CODE_USER.name()),
					atMap.get(AppUserAttributesClassif.POSITION_USER.name()),
					atMap.get(AppUserAttributesClassif.EMAIL_USER.name()),
					atMap.get(AppUserAttributesClassif.PHONE_USER.name()),
					atMap.get(AppUserAttributesClassif.CERTIFICATE_USER.name()),
					atMap.get(AppUserAttributesClassif.NAME_DEPARTAMENT.name()),
					atMap.get(AppUserAttributesClassif.NAME_ORG.name()),
					atMap.get(AppUserAttributesClassif.IOGV_CODE_ORG.name()),
					principal, idUser, IPAddress);

		} catch (Exception e) {
			// sys_audit

			LOGGER.error("user_registration:Error:", e);
			throw new GeneralFailure(e.getMessage());
		}
	}

	/**
	 * доступ по ролям
	 */
	public AppAccept access_roles(String modeExec, String loginUser,
			String codeSystem, List<String> codesRoles, String principal,
			Long idUser, String IPAddress) throws GeneralFailure {

		// principal - принудительно = null
		// idUser - это заявитель

		// modeExec:
		// 0 - REPLACE
		// 1 - ADD
		// 2 - REMOVE

		LOGGER.debug("access_roles:" + loginUser);

		AppAccept result = new AppAccept();

		Long idRoleApp = null;
		try {

			if (modeExec == null
					|| modeExec.trim().isEmpty()
					|| (!modeExec.equals("REPLACE") && !modeExec.equals("ADD") && !modeExec
							.equals("REMOVE")) || loginUser == null
					|| loginUser.trim().isEmpty() || codeSystem == null
					|| codeSystem.trim().isEmpty() || codeSystem.length() > 240
					|| codesRoles == null || codesRoles.isEmpty()
					|| codesRoles.size() > max_size_roles) {
				throw new GeneralFailure("Некорректные данные в заявке!");
			}

			utx.begin();

			// решили определять пользователей извне ЦУД по их ИД, а не логинам
			// Long idUserApp = user_exist(loginUser);
			Long idUserApp = new Long(loginUser);
			// idUserApp - это кому назначаются роли
			Long idSystemApp = system_exist(codeSystem);
			int mode = 1;

			if (modeExec.equals("REPLACE")) {
				mode = 0;
			} else if (modeExec.equals("ADD")) {
				mode = 1;
			} else if (modeExec.equals("REMOVE")) {
				mode = 2;
			}

			List results = em.createNativeQuery(
					"select JOURN_APP_ACCESS_SEQ.nextval from dual ")
					.getResultList();
			Long number = ((BigDecimal) results.get(0)).longValue();

			String secret = TIDEncode.getSecret();

			result.setDate(new Date());
			result.setNumber(number.toString());
			result.setSecret(secret);

			em.createNativeQuery(
					"insert into JOURN_APP_ACCESS_BSS_T (ID_SRV,  LOGIN_USER, CODE_SYSTEM, "
							+ "UP_USER_APP, UP_IS_APP, "
							+ "UP_USER, SECRET, MODE_EXEC) "
							+ " values ( ?, ?, ?, ?, ?, ?, ?, ? ) ")
					.setParameter(1, number)
					.setParameter(2, "-"/* loginUser */)
					.setParameter(3, codeSystem).setParameter(4, idUserApp)
					.setParameter(5, idSystemApp).setParameter(6, idUser)
					.setParameter(7, secret).setParameter(8, mode)
					.executeUpdate();

			for (String role : codesRoles) {

				idRoleApp = role_exist(idSystemApp, role);

				em.createNativeQuery(
						"insert into ROLES_APP_ACCESS_BSS_T (ID_SRV, UP_APP_ACCESS, CODE_ROLE, UP_ROLE ) "
								+ " values (ROLES_APP_ACCESS_SEQ.nextval, ?, ?, ? ) ")
						.setParameter(1, number).setParameter(2, role)
						.setParameter(3, idRoleApp).executeUpdate();

			}

			utx.commit();

		} catch (GeneralFailure ge) {
			// sys_audit
			try {
				utx.rollback();
			} catch (Exception er) {
				LOGGER.error("rollback:Error:", er);
			}
			LOGGER.error("access_roles:Error:" + ge);
			throw ge;
		} catch (Exception e) {
			// sys_audit
			try {
				utx.rollback();
			} catch (Exception er) {
				LOGGER.error("rollback:Error:", er);
			}
			LOGGER.error("access_roles:Error:", e);
			throw new GeneralFailure(e.getMessage());
		}
		return result;
	}

	/**
	 * доступ по группам
	 */
	public AppAccept access_groups(String modeExec, String loginUser,
			String codeSystem, List<String> codesGroups, String principal,
			Long idUser, String IPAddress) throws GeneralFailure {

		// principal - принудительно = null
		// idUser - это заявитель

		// modeExec:
		// 0 - REPLACE
		// 1 - ADD
		// 2 - REMOVE

		LOGGER.debug("access_groups:" + loginUser);

		AppAccept result = new AppAccept();

		Long idGroupApp = null;
		try {

			if (modeExec == null
					|| modeExec.trim().isEmpty()
					|| (!modeExec.equals("REPLACE") && !modeExec.equals("ADD") && !modeExec
							.equals("REMOVE")) || loginUser == null
					|| loginUser.trim().isEmpty() || codeSystem == null
					|| codeSystem.trim().isEmpty() || codeSystem.length() > 240
					|| codesGroups == null || codesGroups.isEmpty()
					|| codesGroups.size() > max_size_roles) {
				throw new GeneralFailure("Некорректные данные в заявке!");
			}

			utx.begin();

			// решили определять пользователей извне ЦУД по их ИД, а не логинам
			// Long idUserApp = user_exist(loginUser);
			Long idUserApp = new Long(loginUser);
			// idUserApp - это кому назначаются роли
			Long idSystemApp = system_exist(codeSystem);
			int mode = 1;

			if (modeExec.equals("REPLACE")) {
				mode = 0;
			} else if (modeExec.equals("ADD")) {
				mode = 1;
			} else if (modeExec.equals("REMOVE")) {
				mode = 2;
			}

			List results = em.createNativeQuery(
					"select JOURN_APP_ACCESS_GR_SEQ.nextval from dual ")
					.getResultList();
			Long number = ((BigDecimal) results.get(0)).longValue();

			String secret = TIDEncode.getSecret();

			result.setDate(new Date());
			result.setNumber(number.toString());
			result.setSecret(secret);

			em.createNativeQuery(
					"insert into JOURN_APP_ACCESS_GROUPS_BSS_T (ID_SRV, "
							+ "UP_USER_APP, UP_IS_APP, "
							+ "UP_USER, SECRET, MODE_EXEC) "
							+ " values ( ?, ?, ?, ?, ?, ? ) ")
					.setParameter(1, number).setParameter(2, idUserApp)
					.setParameter(3, idSystemApp).setParameter(4, idUser)
					.setParameter(5, secret).setParameter(6, mode)
					.executeUpdate();

			for (String group : codesGroups) {

				idGroupApp = group_exist(idSystemApp, group);

				em.createNativeQuery(
						"insert into GROUPS_APP_ACCESS_GR_BSS_T (ID_SRV, UP_APP_ACC_GR, UP_GROUP ) "
								+ " values (GROUPS_APP_ACCESS_GR_SEQ.nextval, ?, ? ) ")
						.setParameter(1, number).setParameter(2, idGroupApp)
						.executeUpdate();

			}

			utx.commit();

		} catch (GeneralFailure ge) {
			// sys_audit
			try {
				utx.rollback();
			} catch (Exception er) {
				LOGGER.error("rollback:Error:", er);
			}
			LOGGER.error("access_groups:Error:" + ge);
			throw ge;
		} catch (Exception e) {
			// sys_audit
			try {
				utx.rollback();
			} catch (Exception er) {
				LOGGER.error("rollback:Error:", er);
			}
			LOGGER.error("access_groups:Error:", e);
			throw new GeneralFailure(e.getMessage());
		}
		return result;
	}

	/**
	 * блокировка пользователя
	 */
	public AppAccept block(String modeExec, String loginUser,
			String blockReason, String principal, Long idUser, String IPAddress)
			throws GeneralFailure {

		// principal - принудительно = null
		// idUser - это заявитель

		// modeExec:
		// 0 - BLOCK
		// 1 - UNBLOCK

		LOGGER.debug("block:01");

		AppAccept result = new AppAccept();

		try {

			if (modeExec == null
					|| modeExec.trim().isEmpty()
					|| (!modeExec.equals("BLOCK") && !modeExec
							.equals("UNBLOCK")) || loginUser == null
					|| loginUser.trim().isEmpty()
					|| (blockReason != null && blockReason.length() > 1000)) {
				throw new GeneralFailure("Некорректные данные в заявке!");
			}

			utx.begin();

			List results = em.createNativeQuery(
					"select JOURN_APP_BLOCK_SEQ.nextval from dual ")
					.getResultList();
			Long number = ((BigDecimal) results.get(0)).longValue();

			String secret = TIDEncode.getSecret();

			result.setDate(new Date());
			result.setNumber(number.toString());
			result.setSecret(secret);

			LOGGER.debug("block:number:" + number);

			// решили определять пользователей извне ЦУД по их ИД, а не логинам
			// Long idUserApp = user_exist(loginUser);
			Long idUserApp = new Long(loginUser);

			int mode = 0;

			if (modeExec.equals("BLOCK")) {
				mode = 0;
			} else if (modeExec.equals("UNBLOCK")) {
				mode = 1;
			}

			em.createNativeQuery(
					"insert into JOURN_APP_BLOCK_BSS_T (ID_SRV, LOGIN_USER, UP_USER_APP, "
							+ "BLOCK_REASON, MODE_EXEC, UP_USER, SECRET) "
							+ "values ( ?, ?, ?, ?, ?, ?, ? ) ")
					.setParameter(1, number)
					.setParameter(2, "-"/* loginUser */)
					.setParameter(3, idUserApp).setParameter(4, blockReason)
					.setParameter(5, mode).setParameter(6, idUser)
					.setParameter(7, secret).executeUpdate();

			LOGGER.debug("block:02");

			utx.commit();

			// sys_audit(70L, "" ,
			// "true; loginsLine:"+(loginsLine.length()>450?loginsLine.substring(0,
			// 450):loginsLine), IPAddress, idUser );
		} catch (GeneralFailure eg) {
			// sys_audit
			try {
				utx.rollback();
			} catch (Exception er) {
				LOGGER.error("rollback:Error:", er);
			}
			LOGGER.error("block:ErrorEG:" + eg);
			throw eg;
		} catch (Exception e) {
			// sys_audit
			try {
				utx.rollback();
			} catch (Exception er) {
				LOGGER.error("rollback:Error:", er);
			}
			LOGGER.error("block:Error:", e);
			throw new GeneralFailure(e.getMessage());
		}
		return result;

	}

	/**
	 * изменение данных инф системы
	 */
	public AppAccept system_modification(String codeSystem,
			List<AppAttribute> attributes, String principal, Long idUser,
			String IPAddress) throws GeneralFailure {

		// principal - принудительно = null
		// idUser - это действующий id user

		LOGGER.debug("system_modification");

		AppAccept result = new AppAccept();

		try {

			if (codeSystem == null || codeSystem.trim().isEmpty()
					|| codeSystem.length() > 240 || attributes == null
					|| attributes.isEmpty()
					|| attributes.size() > max_size_attributes) {
				throw new GeneralFailure("Некорректные данные в заявке!");
			}

			utx.begin();

			HashMap<String, String> atMap = new HashMap<String, String>();

			for (AppAttribute attr : attributes) {
				 
				 
				if (attr.getName() != null) {
					// сейчас берём себе за правило, что переданные аттрибуты
					// без значений игнорируются
					// в дальнейшем надо продумать механизм сброса значений
					if (attr.getValue() != null
							&& !attr.getValue().trim().isEmpty()) {
						atMap.put(attr.getName(), attr.getValue());
					}
				}
			}

			Long idSystemApp = system_exist(codeSystem);

			List results = em.createNativeQuery(
					"select JOURN_APP_SYSTEM_MODIFY_SEQ.nextval from dual ")
					.getResultList();
			Long number = ((BigDecimal) results.get(0)).longValue();

			String secret = TIDEncode.getSecret();

			result.setDate(new Date());
			result.setNumber(number.toString());
			result.setSecret(secret);

			em.createNativeQuery(
					"insert into JOURN_APP_SYSTEM_MODIFY_BSS_T (ID_SRV, CODE_SYSTEM, "
							+ "FULL_NAME, SHORT_NAME, DESCRIPTION, REG_NUMBER, "
							+ "UP_IS_APP, UP_USER, SECRET ) "
							+ " values ( ?, ?, ?, ?, ?, ?, ?, ?, ? ) ")
					.setParameter(1, number)
					.setParameter(2, codeSystem)
					.setParameter(
							3,
							atMap.get(AppSystemAttributesClassif.FULL_NAME_SYS
									.name()))
					.setParameter(
							4,
							atMap.get(AppSystemAttributesClassif.SHORT_NAME_SYS
									.name()))
					.setParameter(
							5,
							atMap.get(AppSystemAttributesClassif.DESCRIPTION_SYS
									.name()))
					.setParameter(
							6,
							atMap.get(AppSystemAttributesClassif.REG_NUM_SYS
									.name())).setParameter(7, idSystemApp)
					.setParameter(8, idUser).setParameter(9, secret)
					.executeUpdate();

			utx.commit();

		} catch (GeneralFailure ge) {
			// sys_audit
			try {
				utx.rollback();
			} catch (Exception er) {
				LOGGER.error("rollback:Error:", er);
			}
			LOGGER.error("system_modification:Error:" + ge);
			throw ge;
		} catch (Exception e) {
			// sys_audit
			try {
				utx.rollback();
			} catch (Exception er) {
				LOGGER.error("rollback:Error:", er);
			}
			LOGGER.error("system_modification:Error:", e);
			throw new GeneralFailure(e.getMessage());
		}
		return result;
	}

	/**
	 * изменение данных пользователя
	 */
	public AppAccept user_modification(String loginUser,
			List<AppAttribute> attributes, String principal, Long idUser,
			String IPAddress) throws GeneralFailure {

		// principal - принудительно = null
		// idUser - это заявитель

		LOGGER.debug("user_modification:01");

		AppAccept result = new AppAccept();

		// разобраться с изменением сертификата -
		// почему его всавка закомментирована -
		// setParameter(10,
		// null/*atMap.get(AppUserClassif.CERTIFICATE_USER.name())*/)

		try {

			if (loginUser == null || loginUser.trim().isEmpty()
					|| attributes == null || attributes.isEmpty()
					|| attributes.size() > max_size_attributes) {
				throw new GeneralFailure("Некорректные данные в заявке!");
			}

			utx.begin();

			HashMap<String, String> atMap = new HashMap<String, String>();

			for (AppAttribute attr : attributes) {
				 
				 
				if (attr.getName() != null) {
					// сейчас берём себе за правило, что переданные аттрибуты
					// без значений игнорируются
					// в дальнейшем надо продумать механизм сброса значений
					if (attr.getValue() != null
							&& !attr.getValue().trim().isEmpty()) {
						atMap.put(attr.getName(), attr.getValue());
					}
				}
			}

			if (atMap.get(AppUserAttributesClassif.SURNAME_USER.name()) == null
					&& atMap.get(AppUserAttributesClassif.NAME_USER.name()) == null
					&& atMap.get(AppUserAttributesClassif.PATRONYMIC_USER
							.name()) == null
					&& atMap.get(AppUserAttributesClassif.POSITION_USER.name()) == null
					&& atMap.get(AppUserAttributesClassif.EMAIL_USER.name()) == null
					&& atMap.get(AppUserAttributesClassif.PHONE_USER.name()) == null
					&& atMap.get(AppUserAttributesClassif.NAME_DEPARTAMENT
							.name()) == null
					&& atMap.get(AppUserAttributesClassif.IOGV_CODE_USER.name()) == null) {

				throw new GeneralFailure("Некорректные данные в заявке!");

			}

			// решили определять пользователей извне ЦУД по их ИД, а не логинам
			// здесь получилось избыточный запрос - потом переделать
			// из запроса не нужен ИД пользователя, он теперь new
			// Long(loginUser)
			Object[] user_info = (Object[]) em
					.createNativeQuery(
							"select to_char(usr.ID_SRV), usr.UP_SIGN_user "
									+ "from  AC_USERS_KNL_T usr " +
									/*
									 * "where usr.login=? ") .setParameter(1,
									 * loginUser)
									 */
									"where usr.ID_SRV=? ")
					.setParameter(1, new Long(loginUser)).getSingleResult();

			Long idUserApp = new Long(user_info[0].toString());// user_exist(loginUser);

			List results = em.createNativeQuery(
					"select JOURN_APP_USER_MODIFY_SEQ.nextval from dual ")
					.getResultList();
			Long number = ((BigDecimal) results.get(0)).longValue();

			String secret = TIDEncode.getSecret();

			result.setDate(new Date());
			result.setNumber(number.toString());
			result.setSecret(secret);

			String userIOGV = atMap.get(AppUserAttributesClassif.IOGV_CODE_USER
					.name());

			String rejectReason = null;

			if (user_info[1] == null) { // пользователь без привязки ИОГВ

			} else {// с привязкой ИОГВ

				if ((userIOGV != null && !userIOGV.trim().isEmpty())) {
					// с привязкой ИОГВ, но в заявке передан код ИОГВ

					if (userIOGV.trim().equals(
							CUDConstants.appAttributeEmptyValue)) {
						// отвязка
						// должны быть аттрибуты ФИО

						if (atMap.get(AppUserAttributesClassif.SURNAME_USER
								.name()) != null
								&& !atMap
										.get(AppUserAttributesClassif.SURNAME_USER
												.name()).trim().isEmpty()
								&& atMap.get(AppUserAttributesClassif.NAME_USER
										.name()) != null
								&& !atMap
										.get(AppUserAttributesClassif.NAME_USER
												.name()).trim().isEmpty()
								&& atMap.get(AppUserAttributesClassif.PATRONYMIC_USER
										.name()) != null
								&& !atMap
										.get(AppUserAttributesClassif.PATRONYMIC_USER
												.name()).trim().isEmpty()) {
							// есть в заявке аттрибуты ФИО
							// нормально, записываем в базу
						} else {
							// нет аттрибутов ФИО

							rejectReason = "При отвязке кода ОГК ИОГВ у пользователя в заявке должны быть "
									+ "указаны значения Фамилии, Имени и Отчества!";
						}

					} else {
						// привязка или замена привязки
						// код должен быть той же организации

						if (userIOGV.trim().length() != 8) {
							throw new GeneralFailure(
									"Некорректные данные в заявке!");
						}

						if (!((String) user_info[1]).substring(0, 3).equals(
								userIOGV.trim().substring(0, 3))) {
							// не совпадают организации

							rejectReason = "Не совпадают организации учётной записи пользователя и "
									+ "передаваемого кода ОГК ИОГВ !";

						} else {
							// совпадают организации
							// нормально, записываем в базу

						}

					}

				} else {
					// с привязкой ИОГВ, и в заявке нет кода ИОГВ

					rejectReason = "Учётная запись пользователя привязана к ОГК ИОГВ. "
							+ "Обновление данных по пользователю производится при обновлении ОГК ИОГВ";

				}

				if (rejectReason == null) {

					em.createNativeQuery(
							"insert into JOURN_APP_USER_MODIFY_BSS_T (ID_SRV, LOGIN_USER, "
									+ "SURNAME_USER, NAME_USER, PATRONYMIC_USER, "
									+ "SIGN_USER, POSITION_USER, EMAIL_USER, PHONE_USER, "
									+ "CERTIFICATE_USER, NAME_DEPARTAMENT, "
									+ "UP_USER_APP, UP_USER, SECRET ) "
									+ " values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ")
							.setParameter(1, number)
							.setParameter(2, "-"/* loginUser */)
							.setParameter(
									3,
									atMap.get(AppUserAttributesClassif.SURNAME_USER
											.name()))
							.setParameter(
									4,
									atMap.get(AppUserAttributesClassif.NAME_USER
											.name()))
							.setParameter(
									5,
									atMap.get(AppUserAttributesClassif.PATRONYMIC_USER
											.name()))
							.setParameter(
									6, /* null */
									atMap.get(AppUserAttributesClassif.IOGV_CODE_USER
											.name()))
							.setParameter(
									7,
									atMap.get(AppUserAttributesClassif.POSITION_USER
											.name()))
							.setParameter(
									8,
									atMap.get(AppUserAttributesClassif.EMAIL_USER
											.name()))
							.setParameter(
									9,
									atMap.get(AppUserAttributesClassif.PHONE_USER
											.name()))
							.setParameter(10, null/*
												 * atMap.get(AppUserClassif.
												 * CERTIFICATE_USER.name())
												 */)
							.setParameter(
									11,
									atMap.get(AppUserAttributesClassif.NAME_DEPARTAMENT
											.name()))

							.setParameter(12, idUserApp)
							.setParameter(13, idUser).setParameter(14, secret)
							.executeUpdate();

				} else {
					em.createNativeQuery(
							"insert into JOURN_APP_USER_MODIFY_BSS_T (ID_SRV, LOGIN_USER, "
									+ "SURNAME_USER, NAME_USER, PATRONYMIC_USER, "
									+ "SIGN_USER, POSITION_USER, EMAIL_USER, PHONE_USER, "
									+ "CERTIFICATE_USER, NAME_DEPARTAMENT, "
									+ "UP_USER_APP, UP_USER, SECRET, STATUS, REJECT_REASON ) "
									+ " values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ")
							.setParameter(1, number)
							.setParameter(2, "-"/* loginUser */)
							.setParameter(
									3,
									atMap.get(AppUserAttributesClassif.SURNAME_USER
											.name()))
							.setParameter(
									4,
									atMap.get(AppUserAttributesClassif.NAME_USER
											.name()))
							.setParameter(
									5,
									atMap.get(AppUserAttributesClassif.PATRONYMIC_USER
											.name()))
							.setParameter(
									6, /* null */
									atMap.get(AppUserAttributesClassif.IOGV_CODE_USER
											.name()))
							.setParameter(
									7,
									atMap.get(AppUserAttributesClassif.POSITION_USER
											.name()))
							.setParameter(
									8,
									atMap.get(AppUserAttributesClassif.EMAIL_USER
											.name()))
							.setParameter(
									9,
									atMap.get(AppUserAttributesClassif.PHONE_USER
											.name()))
							.setParameter(10, null/*
												 * atMap.get(AppUserClassif.
												 * CERTIFICATE_USER.name())
												 */)
							.setParameter(
									11,
									atMap.get(AppUserAttributesClassif.NAME_DEPARTAMENT
											.name()))

							.setParameter(12, idUserApp)
							.setParameter(13, idUser).setParameter(14, secret)

							.setParameter(15, 2).setParameter(16, rejectReason)
							.executeUpdate();
				}

			}

			utx.commit();

		} catch (GeneralFailure ge) {
			// sys_audit
			try {
				utx.rollback();
			} catch (Exception er) {
				LOGGER.error("rollback:Error:", er);
			}
			LOGGER.error("user_modification:Error:" + ge);
			throw ge;
		} catch (Exception e) {
			// sys_audit
			try {
				utx.rollback();
			} catch (Exception er) {
				LOGGER.error("rollback:Error:", er);
			}
			LOGGER.error("user_modification:Error:", e);
			throw new GeneralFailure(e.getMessage());
		}
		return result;
	}

	/**
	 * изменение учетных данных пользователя
	 */
	public AppAccept user_identity_modification(String loginUser, String login,
			String password, Long idUser, String IPAddress)
			throws GeneralFailure {

		// loginUser - субъект заявки [ид пользователя]
		// login, password - новые заменяющие значения

		// principal - принудительно = null
		// idUser - это действующий id user

		LOGGER.debug("user_identity_modification:01");

		AppAccept result = new AppAccept();

		try {

			if (loginUser == null
					|| loginUser.trim().isEmpty()
					|| ((login == null || login.isEmpty()) && (password == null || password
							.isEmpty()))) {
				throw new GeneralFailure("Некорректные данные в заявке!");
			}

			utx.begin();

			Long idUserApp = user_exist(loginUser);

			List results = em.createNativeQuery(
					"select JOURN_APP_USER_ACCMODIFY_SEQ.nextval from dual ")
					.getResultList();
			Long number = ((BigDecimal) results.get(0)).longValue();

			String secret = TIDEncode.getSecret();

			result.setDate(new Date());
			result.setNumber(number.toString());
			result.setSecret(secret);

			em.createNativeQuery(
					"insert into JOURN_APP_USER_ACCMODIFY_BSS_T (ID_SRV, LOGIN_USER, PASS_USER, "
							+ "UP_USER_APP, UP_USER, SECRET ) "
							+ " values ( ?, ?, ?, ?, ?, ? ) ")
					.setParameter(1, number).setParameter(2, login)
					.setParameter(3, password).setParameter(4, idUserApp)
					.setParameter(5, idUser).setParameter(6, secret)
					.executeUpdate();

			utx.commit();

		} catch (GeneralFailure ge) {
			// sys_audit
			try {
				utx.rollback();
			} catch (Exception er) {
				LOGGER.error("rollback:Error:", er);
			}
			LOGGER.error("user_identity_modification:Error:" + ge);
			throw ge;
		} catch (Exception e) {
			// sys_audit
			try {
				utx.rollback();
			} catch (Exception er) {
				LOGGER.error("rollback:Error:", er);
			}
			LOGGER.error("user_identity_modification:Error:", e);
			throw new GeneralFailure(e.getMessage());
		}
		return result;
	}

	/**
	 * изменение сертификата пользователя
	 */
	public AppAccept user_cert_modification(String modeExec, String loginUser,
			String certBase64, Long idUser, String IPAddress)
			throws GeneralFailure {

		// loginUser - субъект заявки [ид пользователя]
		// login, password - новые заменяющие значения

		// principal - принудительно = null
		// idUser - это действующий id user

		// modeExec:
		// 0 - REMOVE
		// 1 - ADD

		LOGGER.debug("user_cert_modification:01");

		AppAccept result = new AppAccept();

		try {

			if (loginUser == null || loginUser.trim().isEmpty()
					|| modeExec == null || modeExec.trim().isEmpty()
					|| (!modeExec.equals("ADD") && !modeExec.equals("REMOVE"))
					|| (certBase64 == null || certBase64.isEmpty())) {
				throw new GeneralFailure("Некорректные данные в заявке!");
			}

			utx.begin();

			int mode = 1;

			if (modeExec.equals("ADD")) {
				mode = 1;
			} else if (modeExec.equals("REMOVE")) {
				mode = 0;
			}

			byte[] certByteX = Base64.decode(certBase64);

			CertificateFactory userCf = CertificateFactory
					.getInstance("X.509");
			X509Certificate userCertX = (X509Certificate) userCf
					.generateCertificate(new ByteArrayInputStream(certByteX));

			String x509Cert = Base64.encode(userCertX.getEncoded());

			// !!!
			// нужно брать байты именно от кодированного сертификата
			// (x509Cert.getBytes("utf-8")),
			// а не от самого сертификата (userCertX.getEncoded())

			Long idUserApp = user_exist(loginUser);

			List results = em.createNativeQuery(
					"select JOURN_APP_USER_CERTADD_SEQ.nextval from dual ")
					.getResultList();
			Long number = ((BigDecimal) results.get(0)).longValue();

			String secret = TIDEncode.getSecret();

			result.setDate(new Date());
			result.setNumber(number.toString());
			result.setSecret(secret);

			em.createNativeQuery(
					"insert into JOURN_APP_USER_CERTADD_BSS_T (ID_SRV, CERT_VALUE, MODE_EXEC, "
							+ "UP_USER_APP, UP_USER, SECRET ) "
							+ " values ( ?, ?, ?, ?, ?, ? ) ")
					.setParameter(1, number)
					.setParameter(2, x509Cert.getBytes("utf-8"))
					.setParameter(3, mode).setParameter(4, idUserApp)
					.setParameter(5, idUser).setParameter(6, secret)
					.executeUpdate();

			utx.commit();

		} catch (GeneralFailure ge) {
			// sys_audit
			try {
				utx.rollback();
			} catch (Exception er) {
				LOGGER.error("rollback:Error:", er);
			}
			LOGGER.error("user_cert_modification:Error:" + ge);
			throw ge;
		} catch (Exception e) {
			// sys_audit
			try {
				utx.rollback();
			} catch (Exception er) {
				LOGGER.error("rollback:Error:", er);
			}
			LOGGER.error("user_cert_modification:Error:", e);
			throw new GeneralFailure(e.getMessage());
		}
		return result;
	}

	/**
	 * изменение данных подразделения
	 */
	public AppAccept user_dep_modification(String loginUser,
			List<AppAttribute> attributes, String principal, Long idUser,
			String IPAddress) throws GeneralFailure {

		// метод не должен по идее использоваться

		// principal - принудительно = null
		// idUser - это действующий id user

		LOGGER.debug("user_dep_modification:01");

		AppAccept result = new AppAccept();

		try {

			if (loginUser == null || loginUser.trim().isEmpty()
					|| attributes == null || attributes.isEmpty()
					|| attributes.size() > max_size_attributes) {
				throw new GeneralFailure("Некорректные данные в заявке!");
			}

			utx.begin();

			HashMap<String, String> atMap = new HashMap<String, String>();

			for (AppAttribute attr : attributes) {
				 
				 
				if (attr.getName() != null) {
					// сейчас берём себе за правило, что переданные аттрибуты
					// без значений игнорируются
					// в дальнейшем надо продумать механизм сброса значений
					if (attr.getValue() != null
							&& !attr.getValue().trim().isEmpty()) {
						atMap.put(attr.getName(), attr.getValue());
					}
				}
			}

			if (atMap.get(AppUserAttributesClassif.NAME_DEPARTAMENT.name()) == null) {
				throw new GeneralFailure("Некорректные данные в заявке!");
			}

			Long idUserApp = user_exist(loginUser);

			List results = em.createNativeQuery(
					"select JOURN_APP_USERDEP_MODIFY_SEQ.nextval from dual ")
					.getResultList();
			Long number = ((BigDecimal) results.get(0)).longValue();

			String secret = TIDEncode.getSecret();

			result.setDate(new Date());
			result.setNumber(number.toString());
			result.setSecret(secret);

			em.createNativeQuery(
					"insert into JOURN_APP_USERDEP_MODIFY_BSS_T (ID_SRV, LOGIN_USER, NAME_DEPARTAMENT, "
							+ "UP_USER_APP, UP_USER, SECRET ) "
							+ " values ( ?, ?, ?, ?, ?, ? ) ")
					.setParameter(1, number)
					.setParameter(2, loginUser)
					.setParameter(
							3,
							atMap.get(AppUserAttributesClassif.NAME_DEPARTAMENT
									.name())).setParameter(4, idUserApp)
					.setParameter(5, idUser).setParameter(6, secret)
					.executeUpdate();

			utx.commit();

		} catch (GeneralFailure ge) {
			// sys_audit
			try {
				utx.rollback();
			} catch (Exception er) {
				LOGGER.error("rollback:Error:", er);
			}
			LOGGER.error("user_dep_modification:Error:" + ge);
			throw ge;
		} catch (Exception e) {
			// sys_audit
			try {
				utx.rollback();
			} catch (Exception er) {
				LOGGER.error("rollback:Error:", er);
			}
			LOGGER.error("user_dep_modification:Error:", e);
			throw new GeneralFailure(e.getMessage());
		}
		return result;
	}

	/**
	 * проверка наличия пользователя по номеру сертификата
	 */
	public Long principal_exist(String principal) throws GeneralFailure {

		Long idUser = null;

		try {
			principal = principal.replaceAll(" ", "").toUpperCase();

			LOGGER.debug("principal_exist:principal:" + principal);

			idUser = ((java.math.BigDecimal) em
					.createNativeQuery(
							"select AU.ID_SRV "
									+ "from "
									+ "AC_USERS_KNL_T au "
									+ "where AU.CERTIFICATE=? "
									+ "and (AU.START_ACCOUNT is null or au.START_ACCOUNT <= sysdate) "
									+ "and (AU.START_ACCOUNT is null or au.START_ACCOUNT > sysdate) "
									+
									// "and AU.STATUS != 2 ")
									"and AU.STATUS = 1 ")
					.setParameter(1, principal).getSingleResult()).longValue();

			LOGGER.debug("principal_exist:idUser:" + idUser);

			return idUser;

		} catch (NoResultException ex) {
			LOGGER.error("principal_exist:NoResultException");
			throw new GeneralFailure("Пользователь не идентифицирован! ");
		} catch (Exception e) {
			LOGGER.error("principal_exist:Error:", e);
			throw new GeneralFailure(e.getMessage());
		}
	}

	/**
	 * проверка наличия системы
	 */
	private Long system_exist(String codeSystem) throws GeneralFailure {

		LOGGER.debug("system_exist:codeSystem:" + codeSystem);

		Long result = null;

		try {

			result = ((java.math.BigDecimal) em
					.createNativeQuery(
							"select APP.ID_SRV " + "from AC_IS_BSS_T app "
									+ "where APP.SIGN_OBJECT=?")
					.setParameter(1, codeSystem).getSingleResult()).longValue();

			return result;

		} catch (NoResultException ex) {
			LOGGER.error("system_exist:NoResultException");
			throw new GeneralFailure("Информационная система не определёна!");
		} catch (Exception e) {
			LOGGER.error("system_exist:Error:", e);
			throw new GeneralFailure(e.getMessage());
		}
	}

	/**
	 * проверка наличия пользователя по его ИД
	 */
	private Long user_exist(String idUser) throws GeneralFailure {

		LOGGER.debug("user_exist:idUser:" + idUser);

		Long result = null;

		try {

			result = ((java.math.BigDecimal) em
					.createNativeQuery(
							"select usr.ID_SRV " + "from  AC_USERS_KNL_T usr "
									+ "where usr.ID_SRV=?")
					.setParameter(1, idUser).getSingleResult()).longValue();

			return result;

		} catch (NoResultException ex) {
			LOGGER.error("user_exist:NoResultException");
			throw new GeneralFailure("Пользователь не определён!");
		} catch (Exception e) {
			LOGGER.error("system_exist:Error:", e);
			throw new GeneralFailure(e.getMessage());
		}
	}

	/**
	 * проверка наличия роли по её коду
	 */
	private Long role_exist(Long idSystem, String codeRole)
			throws GeneralFailure {

		LOGGER.debug("role_exist:idSystem:" + idSystem);
		LOGGER.debug("role_exist:codeRole:" + codeRole);

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
			LOGGER.error("system_exist:NoResultException");
			throw new GeneralFailure("Роль " + codeRole + " не определёна!");
		} catch (Exception e) {
			LOGGER.error("role_exist:Error:", e);
			throw new GeneralFailure(e.getMessage());
		}

		return result;
	}

	/**
	 * проверка наличия группы по её коду
	 */
	private Long group_exist(Long idSystem, String codeGroup)
			throws GeneralFailure {

		LOGGER.debug("group_exist:idSystem:" + idSystem);
		LOGGER.debug("group_exist:codeRole:" + codeGroup);

		Long result = null;

		try {

			result = ((java.math.BigDecimal) em
					.createNativeQuery(
							"select GR.ID_SRV " + "from GROUP_USERS_KNL_T gr "
									+ "where GR.SIGN_OBJECT= ? ")
					.setParameter(1, codeGroup).getSingleResult()).longValue();

		} catch (NoResultException ex) {
			LOGGER.error("group_exist:NoResultException");
			throw new GeneralFailure("Группа " + codeGroup + " не определёна!");
		} catch (Exception e) {
			LOGGER.error("group_exist:Error:", e);
			throw new GeneralFailure(e.getMessage());
		}

		return result;
	}

	/**
	 * протоколирование действий
	 */
	
}
