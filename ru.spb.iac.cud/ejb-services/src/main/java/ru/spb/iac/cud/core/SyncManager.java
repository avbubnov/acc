package ru.spb.iac.cud.core;

import java.math.BigDecimal;
import java.util.HashMap; import java.util.Map;
import java.util.List;
 
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import java.util.ArrayList;
import ru.spb.iac.cud.core.util.CUDConstants;
import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.items.Function;
import ru.spb.iac.cud.items.Group;
import ru.spb.iac.cud.items.Resource;
import ru.spb.iac.cud.items.Role;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EJB ��� ����� ������������� � ������� �� ��������
 * 
 * @author bubnov
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@TransactionManagement(TransactionManagementType.BEAN)
public class SyncManager implements SyncManagerLocal, SyncManagerRemote {

	@PersistenceContext(unitName = "AuthServices")
	EntityManager em;

	@javax.annotation.Resource
	UserTransaction utx;

	final static Logger LOGGER = LoggerFactory.getLogger(SyncManager.class);

	public SyncManager() {
	}

	/**
	 * ������������� ������
	 */
	public void sync_roles(String idIS, List<Role> roles, String modeExec,
			Long idUserAuth, String IPAddress) throws GeneralFailure {

		// ��� ������ � ���������

		// modeExec:
		// 0 - REPLACE
		// 1 - ADD
		// 2 - REMOVE

		// ��� REPLACE � REMOVE, ���� � ���� ���� ������������ ��� ������,
		// �� ����� ����������

		// ADD - ��� ADD ��� UPDATE

		LOGGER.debug("sync_roles:01");

		Map<String, Long> roles_cl = new HashMap<String, Long>();

		try {

			utx.begin();

			if (roles == null || roles.isEmpty()) {
				LOGGER.debug("sync_roles:return");
				throw new GeneralFailure("����������� ������ �����!");
			}

			if (idIS == null) {
				LOGGER.debug("sync_roles:return");
				throw new GeneralFailure("idIS is null");
			}

			if (modeExec == null
					|| modeExec.trim().isEmpty()
					|| (!modeExec.equals("REPLACE") && !modeExec.equals("ADD") && !modeExec
							.equals("REMOVE"))) {
				throw new GeneralFailure("������������ ������ [modeExec]!");
			}

			int mode = 1;

			if (modeExec.equals("REPLACE")) {
				mode = 0;
			} else if (modeExec.equals("ADD")) {
				mode = 1;
			} else if (modeExec.equals("REMOVE")) {
				mode = 2;
			}

			// !!!
			idIS = get_code_is(idIS);

			if (mode == 1) { // //ADD

				// ��������� ����
				List<Object[]> lo = em
						.createNativeQuery(
								"select rls.SIGN_OBJECT, rls.ID_SRV "
										+ "from AC_ROLES_BSS_T rls, "
										+ "AC_IS_BSS_T app "
										+ "where APP.ID_SRV = rls.UP_IS "
										+ "and APP.SIGN_OBJECT=?")
						.setParameter(1, idIS).getResultList();
				LOGGER.debug("sync_roles:02");

				for (Object[] objectArray : lo) {
					roles_cl.put(
							objectArray[0] != null ? objectArray[0].toString()
									: "", objectArray[1] != null ? new Long(
									objectArray[1].toString()) : -1L);
				}

				for (Role role : roles) {

					if (role.getCode() == null
							|| role.getCode().trim().equals("")) {
						throw new GeneralFailure("����������� ��� ����!");
					} else if (role.getName() == null
							|| role.getName().trim().equals("")) {
						throw new GeneralFailure("����������� �������� ����!");
					}

					if (roles_cl.containsKey(role.getCode())) {

						em.createNativeQuery(
								"UPDATE AC_ROLES_BSS_T rls "
										+ "set rls.FULL_=?, rls.DESCRIPTION=? "
										+ "where rls.SIGN_OBJECT = ? "
										+ "and rls.UP_IS=( "
										+ "SELECT APP.ID_SRV "
										+ "FROM AC_IS_BSS_T app "
										+ "WHERE APP.SIGN_OBJECT=? ) ")
								.setParameter(1, role.getName())
								.setParameter(2, role.getDescription())
								.setParameter(3, role.getCode())
								.setParameter(4, idIS)

								.executeUpdate();

					} else {
						em.createNativeQuery(
								"insert into AC_ROLES_BSS_T(ID_SRV, UP_IS, SIGN_OBJECT, FULL_ , DESCRIPTION, CREATOR,  created) "
										+ "values(AC_ROLES_BSS_SEQ.nextval, (select APP.ID_SRV FROM AC_IS_BSS_T app WHERE APP.SIGN_OBJECT=?), "
										+ "?, ?, ?, 1, sysdate) ")
								.setParameter(1, idIS)
								.setParameter(2, role.getCode())
								.setParameter(3, role.getName())
								.setParameter(4, role.getDescription())
								.executeUpdate();
					}
				}

			} else if (mode == 2) { // REMOVE

				LOGGER.debug("sync_roles:03");

				for (Role role : roles) {

					LOGGER.debug("sync_roles:04:" + role.getCode());

					if (role.getCode() == null
							|| role.getCode().trim().equals("")) {
						throw new GeneralFailure("����������� ��� ����!");
					} else if (role.getName() == null
							|| role.getName().trim().equals("")) {
						throw new GeneralFailure("����������� �������� ����!");
					}

					// roles_cl ��������������� ������ � ADD
					// if(roles_cl.containsKey(role.getIdRole())){

					// ���� ���� � ����

					try {
						LOGGER.debug("sync_roles:05");

						em.createNativeQuery(
								"DELETE FROM AC_ROLES_BSS_T rls "
										+ "where rls.SIGN_OBJECT = ? "
										+ "and rls.UP_IS=( "
										+ "SELECT APP.ID_SRV "
										+ "FROM AC_IS_BSS_T app "
										+ "WHERE APP.SIGN_OBJECT=? ) ")
								.setParameter(1, role.getCode())
								.setParameter(2, idIS).executeUpdate();

					} catch (Exception e) {
						LOGGER.error("sync_roles:error:", e);
						// sys_audit ��� �� ����� ��������, �.�. ����������
						// ����������� ��������,
						// � �� ������� ����� ����������
						// ���� �������� - ����� ������ ������ ����������
						// �����������,
						// ����� �������� � ����� ��� ������

						// sys_audit(12L, "idIS:"+idIS,
						// "error:dependent records found", IPAddress, null );
						throw new GeneralFailure(
								"Removal role ['"
										+ role.getCode()
										+ "'] is not possible: dependent records found ! ");
					}
					// }
				}
			} else if (mode == 0) { // REPLACE

				// REPLACE = ALL REMOVE + ADD
				LOGGER.debug("sync_roles:07");

				// 1. REMOVE ALL

				try {
					LOGGER.debug("sync_roles:09");

					em.createNativeQuery(
							"DELETE FROM AC_ROLES_BSS_T rls "
									+ "where rls.UP_IS=( "
									+ "SELECT APP.ID_SRV "
									+ "FROM AC_IS_BSS_T app "
									+ "WHERE APP.SIGN_OBJECT=? ) ")
							.setParameter(1, idIS).executeUpdate();

				} catch (Exception e) {
					LOGGER.error("sync_roles:010");
					// sys_audit ��� �� ����� ��������, �.�. ����������
					// ����������� ��������,
					// � �� ������� ����� ����������
					// ���� �������� - ����� ������ ������ ����������
					// �����������,
					// ����� �������� � ����� ��� ������
					throw new GeneralFailure(
							"Replacement roles is not possible: dependent records found ! ");
				}

				// 2.ADD

				for (Role role : roles) {

					LOGGER.debug("sync_roles:011:" + role.getCode());

					if (role.getCode() == null
							|| role.getCode().trim().equals("")) {
						throw new GeneralFailure("����������� ��� ����!");
					} else if (role.getName() == null
							|| role.getName().trim().equals("")) {
						throw new GeneralFailure("����������� �������� ����!");
					}

					em.createNativeQuery(
							"insert into AC_ROLES_BSS_T(ID_SRV, UP_IS, SIGN_OBJECT, FULL_ , DESCRIPTION, CREATOR,  created) "
									+ "values(AC_ROLES_BSS_SEQ.nextval, (select APP.ID_SRV FROM AC_IS_BSS_T app WHERE APP.SIGN_OBJECT=?), "
									+ "?, ?, ?, 1, sysdate) ")
							.setParameter(1, idIS)
							.setParameter(2, role.getCode())
							.setParameter(3, role.getName())
							.setParameter(4, role.getDescription())
							.executeUpdate();
				}

			}

			sys_audit(12L, "idIS:" + idIS, "true", IPAddress, null);

			utx.commit();

		} catch (Exception e) {
			try {

				utx.rollback();

				utx.begin();
				sys_audit(12L, "idIS:" + idIS, "error", IPAddress, null);
				utx.commit();

			} catch (Exception er) {
				try {
					utx.rollback();
				} catch (Exception er1) {
					LOGGER.error("rollback:Error1:", er1);
				}
				LOGGER.error("rollback:Error:", er);
			}
			throw new GeneralFailure(e.getMessage());
		}

	}

	/**
	 * ������������� ������� ������
	 */
	public void sync_functions(String idIS, List<Function> functions,
			String modeExec, Long idUserAuth, String IPAddress)
			throws GeneralFailure {

		// ��� ������ � ���������

		// modeExec:
		// 0 - REPLACE
		// 1 - ADD
		// 2 - REMOVE

		// ��� REPLACE � REMOVE, ���� � ������� ���� ��� �����,
		// �� ����� ����������

		// ADD - ��� ADD ��� UPDATE

		LOGGER.debug("sync_functions:01");

		Map<String, Long> act_cl = new HashMap<String, Long>();

		try {

			utx.begin();

			if (idIS == null || idIS.trim().equals("")) {
				throw new GeneralFailure("����������� ��� �������!");
			}

			if (functions == null || functions.isEmpty()) {
				LOGGER.debug("sync_functions:return");
				throw new GeneralFailure("����������� ������ �������!");
				// return;
			}

			if (idIS == null) {
				LOGGER.debug("sync_functions:return");
				throw new GeneralFailure("idIS is null!");
			}

			if (modeExec == null
					|| modeExec.trim().isEmpty()
					|| (!modeExec.equals("REPLACE") && !modeExec.equals("ADD") && !modeExec
							.equals("REMOVE"))) {
				throw new GeneralFailure("������������ ������ [modeExec]!");
			}

			int mode = 1;

			if (modeExec.equals("REPLACE")) {
				mode = 0;
			} else if (modeExec.equals("ADD")) {
				mode = 1;
			} else if (modeExec.equals("REMOVE")) {
				mode = 2;
			}

			// !!!
			idIS = get_code_is(idIS);

			/*
			 * em.createNativeQuery( "delete from ACTIONS_BSS_T act "+
			 * "where ACT.UP_IS=? ") .setParameter(1, new Long(idIS))
			 * .executeUpdate();
			 */

			if (mode == 1) { // //ADD

				// ��������� ����
				List<Object[]> lo = em
						.createNativeQuery(
								"select ACT.SIGN_OBJECT, ACT.ID_SRV "
										+ "from ACTIONS_BSS_T act, "
										+ "AC_IS_BSS_T app "
										+ "where APP.ID_SRV = ACT.UP_IS "
										+ "and APP.SIGN_OBJECT=?")
						.setParameter(1, idIS).getResultList();
				LOGGER.debug("sync_functions:03");

				for (Object[] objectArray : lo) {
					act_cl.put(
							objectArray[0] != null ? objectArray[0].toString()
									: "", objectArray[1] != null ? new Long(
									objectArray[1].toString()) : -1L);
				}

				for (Function func : functions) {

					if (func.getCode() == null
							|| func.getCode().trim().equals("")) {
						throw new GeneralFailure("����������� ��� �������!");
					} else if (func.getName() == null
							|| func.getName().trim().equals("")) {
						throw new GeneralFailure(
								"����������� �������� �������!");
					}

					if (act_cl.containsKey(func.getCode())) {

						em.createNativeQuery(
								"UPDATE ACTIONS_BSS_T act "
										+ "set act.FULL_=?, act.DESCRIPTIONS=? "
										+ "where act.SIGN_OBJECT = ? "
										+ "and act.UP_IS=( "
										+ "SELECT APP.ID_SRV "
										+ "FROM AC_IS_BSS_T app "
										+ "WHERE APP.SIGN_OBJECT=? ) ")
								.setParameter(1, func.getName())
								.setParameter(2, func.getDescription())
								.setParameter(3, func.getCode())
								.setParameter(4, idIS)

								.executeUpdate();

					} else {
						em.createNativeQuery(
								"insert into ACTIONS_BSS_T(ID_SRV, UP_IS, SIGN_OBJECT, FULL_ , DESCRIPTIONS, CREATOR,  created) "
										+ "values(ACTIONS_BSS_SEQ.nextval, (select APP.ID_SRV FROM AC_IS_BSS_T app WHERE APP.SIGN_OBJECT=?), "
										+ "?, ?, ?, 1, sysdate) ")
								.setParameter(1, idIS)
								.setParameter(2, func.getCode())
								.setParameter(3, func.getName())
								.setParameter(4, func.getDescription())
								.executeUpdate();
					}
				}

			} else if (mode == 2) { // REMOVE

				LOGGER.debug("sync_functions:04");

				for (Function func : functions) {

					if (func.getCode() == null
							|| func.getCode().trim().equals("")) {
						throw new GeneralFailure("����������� ��� �������!");
					} else if (func.getName() == null
							|| func.getName().trim().equals("")) {
						throw new GeneralFailure(
								"����������� �������� �������!");
					}

					// roles_cl ��������������� ������ � ADD
					// if(act_cl.containsKey(func.getIdFunction())){

					// ������� ���� � ����

					try {
						LOGGER.debug("sync_functions:05");

						em.createNativeQuery(
								"DELETE FROM ACTIONS_BSS_T act "
										+ "where act.SIGN_OBJECT = ? "
										+ "and act.UP_IS=( "
										+ "SELECT APP.ID_SRV "
										+ "FROM AC_IS_BSS_T app "
										+ "WHERE APP.SIGN_OBJECT=? ) ")
								.setParameter(1, func.getCode())
								.setParameter(2, idIS).executeUpdate();

					} catch (Exception e) {
						LOGGER.error("sync_functions:06");
						// sys_audit ��� �� ����� ��������, �.�. ����������
						// ����������� ��������,
						// � �� ������� ����� ����������
						// ���� �������� - ����� ������ ������ ����������
						// �����������,
						// ����� �������� � ����� ��� ������

						throw new GeneralFailure(
								"Removal role ['"
										+ func.getCode()
										+ "'] is not possible: dependent records found ! ");
					}

					// }
				}

			} else if (mode == 0) { // REPLACE

				// REPLACE = ALL REMOVE + ADD

				LOGGER.debug("sync_functions:07");

				// 1. REMOVE ALL

				try {
					LOGGER.debug("sync_functions:09");

					em.createNativeQuery(
							"DELETE FROM ACTIONS_BSS_T act "
									+ "where act.UP_IS=( "
									+ "SELECT APP.ID_SRV "
									+ "FROM AC_IS_BSS_T app "
									+ "WHERE APP.SIGN_OBJECT=? ) ")
							.setParameter(1, idIS).executeUpdate();

				} catch (Exception e) {
					LOGGER.error("sync_functions:010");
					// sys_audit ��� �� ����� ��������, �.�. ����������
					// ����������� ��������,
					// � �� ������� ����� ����������
					// ���� �������� - ����� ������ ������ ����������
					// �����������,
					// ����� �������� � ����� ��� ������
					throw new GeneralFailure(
							"Replacement roles is not possible: dependent records found ! ");
				}

				// 2.ADD

				for (Function func : functions) {

					if (func.getCode() == null
							|| func.getCode().trim().equals("")) {
						throw new GeneralFailure("����������� ��� �������!");
					} else if (func.getName() == null
							|| func.getName().trim().equals("")) {
						throw new GeneralFailure(
								"����������� �������� �������!");
					}

					em.createNativeQuery(
							"insert into ACTIONS_BSS_T(ID_SRV, UP_IS, SIGN_OBJECT, FULL_ , DESCRIPTIONS, CREATOR,  created) "
									+ "values(ACTIONS_BSS_SEQ.nextval, (select APP.ID_SRV FROM AC_IS_BSS_T app WHERE APP.SIGN_OBJECT=?), "
									+ "?, ?, ?, 1, sysdate) ")
							.setParameter(1, idIS)
							.setParameter(2, func.getCode())
							.setParameter(3, func.getName())
							.setParameter(4, func.getDescription())
							.executeUpdate();
				}

			}

			sys_audit(13L, "idIS:" + idIS, "true", IPAddress, null);

			utx.commit();

		} catch (Exception e) {

			try {

				utx.rollback();

				utx.begin();
				sys_audit(13L, "idIS:" + idIS, "error", IPAddress, null);
				utx.commit();

			} catch (Exception er) {
				try {
					utx.rollback();
				} catch (Exception er1) {
					LOGGER.error("rollback:Error1:", er1);
				}
				LOGGER.error("rollback:Error:", er);
			}

			throw new GeneralFailure(e.getMessage());
		}
	}

	/**
	 * ������ ����� �������
	 */
	public List<Role> is_roles(String idIS, Long idUserAuth, String IPAddress)
			throws GeneralFailure {

		// ��� ����� ������, ������ � ���������

		LOGGER.debug("is_roles:01");

		List<Role> result = new ArrayList<Role>();
		List<String> keyList = new ArrayList<String>();

		try {

			if (idIS == null || idIS.trim().isEmpty()) {
				LOGGER.debug("is_roles:01");
				throw new GeneralFailure("idIS is null!");
			}

			if (idIS.startsWith(CUDConstants.groupArmPrefix)) {
				// ������ ��

				LOGGER.debug("is_roles:02");

				List<Object[]> lo = em
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

				for (Object[] objectArray : lo) {
					LOGGER.debug("IdRole:" + objectArray[0].toString());

					Role role = new Role();

					role.setCode(objectArray[0].toString());
					role.setName(objectArray[1].toString());
					role.setDescription (objectArray[2] != null ? objectArray[2]
							.toString() : null);

					result.add(role);

					keyList.add(objectArray[0].toString());

				}

				LOGGER.debug("is_roles:03");

			} else if (idIS.startsWith(CUDConstants.armPrefix)
					|| idIS.startsWith(CUDConstants.subArmPrefix)) {
				// ������� ��� ����������

				// !!!
				idIS = get_code_is(idIS);

				List<Object[]> lo = em
						.createNativeQuery(
								"SELECT ROL.SIGN_OBJECT, ROL.FULL_, ROL.DESCRIPTION "
										+ "  FROM AC_IS_BSS_T app, AC_ROLES_BSS_T rol "
										+ " WHERE APP.SIGN_OBJECT = ? AND ROL.UP_IS = APP.ID_SRV ")
						.setParameter(1, idIS).getResultList();

				for (Object[] objectArray : lo) {

					Role role = new Role();

					role.setCode(objectArray[0].toString());
					role.setName(objectArray[1].toString());
					role.setDescription (objectArray[2] != null ? objectArray[2]
							.toString() : null);

					result.add(role);

					keyList.add(objectArray[0].toString());

				}

				LOGGER.debug("is_roles:04");

			}

			sys_audit(80L, "idIS:" + idIS, "true", IPAddress, null);

			return result;

		} catch (Exception e) {
			sys_audit(80L, "idIS:" + idIS, "error", IPAddress, null);
			throw new GeneralFailure(e.getMessage());
		}
	}

	/**
	 * ������ ������� ������
	 */
	public List<Function> is_functions(String idIS, Long idUserAuth,
			String IPAddress) throws GeneralFailure {

		// ��� ����� ������, ������ � ���������

		LOGGER.debug("is_functions:01");

			List<Function> result = new ArrayList<Function>();
		List<String> keyList = new ArrayList<String>();

		try {

			if (idIS == null || idIS.trim().isEmpty()) {
				LOGGER.debug("is_functions:01");
				throw new GeneralFailure("idIS is null!");
			}

			if (idIS.startsWith(CUDConstants.groupArmPrefix)) {
				// ������ ��

				LOGGER.debug("is_functions:02");

				List<Object[]> lo = em
						.createNativeQuery(
								" SELECT '[' || sys_code || ']' || act_full.SIGN_OBJECT act_is_code, "
										+ "         act_full.FULL_, "
										+ "         act_full.DESCRIPTIONS "
										+ "    FROM (  SELECT SYS.SIGN_OBJECT sys_code, act.ID_SRV act_id "
										+ "              FROM GROUP_SYSTEMS_KNL_T gsys, "
										+ "                   AC_IS_BSS_T sys, "
										+ "                   ACTIONS_BSS_T act, "
										+ "                   LINK_GROUP_SYS_SYS_KNL_T lgr "
										+ "             WHERE     GSYS.GROUP_CODE = ? "
										+ "                   AND GSYS.ID_SRV = LGR.UP_GROUP_SYSTEMS "
										+ "                   AND LGR.UP_SYSTEMS = SYS.ID_SRV "
										+ "                   AND act.UP_IS = SYS.ID_SRV "
										+ "          GROUP BY SYS.SIGN_OBJECT, act.ID_SRV), "
										+ "         ACTIONS_BSS_T act_full "
										+ "   WHERE act_full.ID_SRV = act_id "
										+ "ORDER BY sys_code")
						.setParameter(1, idIS).getResultList();

				for (Object[] objectArray : lo) {
					LOGGER.debug("IdRole:" + objectArray[0].toString());

					Function func = new Function();

					func.setCode(objectArray[0].toString());
					func.setName(objectArray[1].toString());
					func.setDescription (objectArray[2] != null ? objectArray[2]
							.toString() : null);

					result.add(func);

					keyList.add(objectArray[0].toString());

				}

				LOGGER.debug("is_functions:03");

			} else if (idIS.startsWith(CUDConstants.armPrefix)
					|| idIS.startsWith(CUDConstants.subArmPrefix)) {
				// ������� ��� ����������

				// !!!
				idIS = get_code_is(idIS);

				List<Object[]> lo = em
						.createNativeQuery(
								"SELECT act.SIGN_OBJECT,act.FULL_, ACT.DESCRIPTIONS "
										+ "FROM AC_IS_BSS_T app, ACTIONS_BSS_T act "
										+ "WHERE APP.SIGN_OBJECT = ? AND act.UP_IS = APP.ID_SRV ")
						.setParameter(1, idIS).getResultList();

				for (Object[] objectArray : lo) {

					Function func = new Function();

					func.setCode(objectArray[0].toString());
					func.setName(objectArray[1].toString());
					func.setDescription (objectArray[2] != null ? objectArray[2]
							.toString() : null);

					result.add(func);

					keyList.add(objectArray[0].toString());

				}

				LOGGER.debug("is_functions:04");

			}

			sys_audit(81L, "idIS:" + idIS, "true", IPAddress, null);

			return result;

		} catch (Exception e) {
			sys_audit(81L, "idIS:" + idIS, "error", IPAddress, null);
			throw new GeneralFailure(e.getMessage());
		}

	}

	/**
	 * ������������� ����� �������������
	 */
	public void sync_groups(String idIS, List<Group> groups, String modeExec,
			Long idUserAuth, String IPAddress) throws GeneralFailure {

		// ��� ������ � ���������

		// !!!
		// REPLACE ���!!!

		// !!!
		// �������� ����� ���������� ���������� ADD

		// modeExec:
		// 1 - ADD
		// 2 - REMOVE

		// ��� REMOVE, ���� � ���� ���� ������������ ��� ������,
		// �� ����� ����������

		// ADD - ��� ADD ��� UPDATE

		LOGGER.debug("sync_groups:01");

		Map<String, Long> group_cl = new HashMap<String, Long>();

		List<String> role_cl = new ArrayList<String>();

		try {

			utx.begin();

			/*
			 * �������� �� ������ ���� - is_exist()
		    */

			if (groups == null || groups.isEmpty()) {
				LOGGER.debug("sync_groups:return");
				throw new GeneralFailure("����������� ������ �����!");
			}

			if (idIS == null) {
				LOGGER.debug("sync_groups:return");
				throw new GeneralFailure("idIS is null!");
			}

			if (modeExec == null || modeExec.trim().isEmpty()
					|| (!modeExec.equals("ADD") && !modeExec.equals("REMOVE"))) {
				throw new GeneralFailure("������������ ������ [modeExec]!");
			}

			int mode = 1;

			if (modeExec.equals("ADD")) {
				mode = 1;
			} else if (modeExec.equals("REMOVE")) {
				mode = 2;
			}

			// !!!
			idIS = get_code_is(idIS);

			if (mode == 1) { // //ADD

				// ��������� ������
				List<Object[]> lo = em.createNativeQuery(
						"select gr.SIGN_OBJECT, GR.ID_SRV "
								+ "from GROUP_USERS_KNL_T gr ").getResultList();

				LOGGER.debug("sync_groups:02");

				for (Object[] objectArray : lo) {
					group_cl.put(
							objectArray[0] != null ? objectArray[0].toString()
									: "", objectArray[1] != null ? new Long(
									objectArray[1].toString()) : -1L);
				}

				for (Group group : groups) {

					if (group.getCode() == null
							|| group.getCode().trim().equals("")) {
						throw new GeneralFailure("����������� ��� ������!");
					} else if (group.getName() == null
							|| group.getName().trim().equals("")) {
						throw new GeneralFailure("����������� �������� ������!");
					}

					role_cl = new ArrayList<String>();

					if (group_cl.containsKey(group.getCode())) {

						em.createNativeQuery(
								"UPDATE GROUP_USERS_KNL_T rls "
										+ "set rls.FULL_=?, rls.DESCRIPTION=? "
										+ "where rls.SIGN_OBJECT = ? ")
								.setParameter(1, group.getName())
								.setParameter(2, group.getDescription())
								.setParameter(3, group.getCode())
								.executeUpdate();

						// �������� ����� � ������
						if (group.getCodesRoles() != null) {

							LOGGER.debug("sync_groups:03");

							// ��������� ���� � ������ �� ������ �������

							List<Object[]> lo_roles = em
									.createNativeQuery(
											"select ROL.SIGN_OBJECT, ROL.ID_SRV "
													+ "from GROUP_USERS_KNL_T gr, "
													+ "LINK_GROUP_USERS_ROLES_KNL_T lgr, "
													+ "AC_ROLES_BSS_T rol, "
													+ "AC_IS_BSS_T sys "
													+ "where GR.ID_SRV=LGR.UP_GROUP_USERS "
													+ "and LGR.UP_ROLES=ROL.ID_SRV "
													+ "and SYS.SIGN_OBJECT = ? "
													+ "and SYS.ID_SRV= ROL.UP_IS "
													+ "and GR.SIGN_OBJECT = ? "
													+ "group by  ROL.SIGN_OBJECT, ROL.ID_SRV ")
									.setParameter(1, idIS)
									.setParameter(2, group.getCode())
									.getResultList();

							for (Object[] objectArray : lo_roles) {
								role_cl.add(objectArray[0] != null ? objectArray[0]
										.toString() : "");

								LOGGER.debug("sync_groups:04:" + objectArray[0]);

							}

							for (String role_code : group.getCodesRoles()) {

								LOGGER.debug("sync_groups:05:" + role_code);

								if (!role_cl.contains(role_code)) {

									LOGGER.debug("sync_groups:06");

									em.createNativeQuery(
											"insert into LINK_GROUP_USERS_ROLES_KNL_T(UP_ROLES, UP_GROUP_USERS, CREATOR,  created) "
													+ "values ("
													+ "(select role.ID_SRV from AC_ROLES_BSS_T role, AC_IS_BSS_T sys  "
													+ "where SYS.ID_SRV= ROLE.UP_IS and  SYS.SIGN_OBJECT = ? "
													+ "and role.SIGN_OBJECT = ? ), ?, ?, sysdate) ")
											.setParameter(1, idIS)
											.setParameter(2, role_code)
											.setParameter(
													3,
													group_cl.get(group
															.getCode()))
											.setParameter(4, 1L)
											.executeUpdate();
								}
							}
						}

					} else {

						List results = em
								.createNativeQuery(
										"select GROUP_USERS_KNL_SEQ.nextval from dual ")
								.getResultList();

						Long newIdGroup = ((BigDecimal) results.get(0))
								.longValue();

						em.createNativeQuery(
								"insert into GROUP_USERS_KNL_T(ID_SRV, SIGN_OBJECT, FULL_ , DESCRIPTION, CREATOR,  created) "
										+ "values(?, "
										+ "?, ?, ?, ?, sysdate) ")
								.setParameter(1, newIdGroup)
								.setParameter(2, group.getCode())
								.setParameter(3, group.getName())
								.setParameter(4, group.getDescription())
								.setParameter(5, 1L).executeUpdate();

						// �������� ����� � ������
						if (group.getCodesRoles() != null) {

							// ��������� ���� � ������ �� ������ �������

							List<Object[]> lo_roles = em
									.createNativeQuery(
											"select ROL.SIGN_OBJECT, ROL.ID_SRV "
													+ "from GROUP_USERS_KNL_T gr, "
													+ "LINK_GROUP_USERS_ROLES_KNL_T lgr, "
													+ "AC_ROLES_BSS_T rol, "
													+ "AC_IS_BSS_T sys "
													+ "where GR.ID_SRV=LGR.UP_GROUP_USERS "
													+ "and LGR.UP_ROLES=ROL.ID_SRV "
													+ "and SYS.SIGN_OBJECT = ? "
													+ "and SYS.ID_SRV= ROL.UP_IS "
													+ "and GR.SIGN_OBJECT = ? "
													+ "group by  ROL.SIGN_OBJECT, ROL.ID_SRV ")
									.setParameter(1, idIS)
									.setParameter(2, group.getCode())
									.getResultList();

							for (Object[] objectArray : lo_roles) {
								role_cl.add(objectArray[0] != null ? objectArray[0]
										.toString() : "");
							}

							for (String role_code : group.getCodesRoles()) {

								if (!role_cl.contains(role_code)) {

									em.createNativeQuery(
											"insert into LINK_GROUP_USERS_ROLES_KNL_T(UP_ROLES, UP_GROUP_USERS, CREATOR,  created) "
													+ "values ("
													+ "(select role.ID_SRV from AC_ROLES_BSS_T role, AC_IS_BSS_T sys "
													+ "where SYS.ID_SRV= ROLE.UP_IS and  SYS.SIGN_OBJECT = ? "
													+ "and role.SIGN_OBJECT = ? ), ?, ?, sysdate) ")
											.setParameter(1, idIS)
											.setParameter(2, role_code)
											.setParameter(3, newIdGroup)
											.setParameter(4, 1L)
											.executeUpdate();
								}
							}
						}

					}
				}

			} else if (mode == 2) { // REMOVE

				LOGGER.debug("sync_groups:03");

				for (Group group : groups) {

					LOGGER.debug("sync_groups:04:" + group.getCode());

					if (group.getCode() == null
							|| group.getCode().trim().equals("")) {
						throw new GeneralFailure("����������� ��� ������!");
					}

					// roles_cl ��������������� ������ � ADD
					// if(roles_cl.containsKey(role.getIdRole())){

					// ���� ���� � ����

					try {
						LOGGER.debug("sync_group:05");

						em.createNativeQuery(
								"DELETE FROM GROUP_USERS_KNL_T rls "
										+ "where rls.SIGN_OBJECT = ? ")
								.setParameter(1, group.getCode())
								.executeUpdate();

					} catch (Exception e) {
						LOGGER.error("sync_group:06");
						// sys_audit ��� �� ����� ��������, �.�. ����������
						// ����������� ��������,
						// � �� ������� ����� ����������
						// ���� �������� - ����� ������ ������ ����������
						// �����������,
						// ����� �������� � ����� ��� ������

						// sys_audit(12L, "idIS:"+idIS,
						// "error:dependent records found", IPAddress, null );
						throw new GeneralFailure(
								"Removal group ['"
										+ group.getCode()
										+ "'] is not possible: dependent records found ! ");
					}
					// }
				}
			}

			sys_audit(12L, "idIS:" + idIS, "true", IPAddress, null);

			utx.commit();

		} catch (Exception e) {
			try {

				utx.rollback();

				utx.begin();
				sys_audit(12L, "idIS:" + idIS, "error", IPAddress, null);
				utx.commit();

			} catch (Exception er) {
				try {
					utx.rollback();
				} catch (Exception er1) {
					LOGGER.error("rollback:Error1:", er1);
				}
				LOGGER.error("rollback:Error:", er);
			}
			throw new GeneralFailure(e.getMessage());
		}

	}

	/**
	 * ������������� ����� �����
	 */
	public void sync_groups_roles(String idIS, List<String> codesGroups,
			List<String> codesRoles, String modeExec, Long idUserAuth,
			String IPAddress) throws GeneralFailure {

		// ��� ������ � ���������

		// !!!
		// �������� ����� ���������� ���������� ADD

		// modeExec:
		// 0 - REPLACE
		// 1 - ADD
		// 2 - REMOVE

		// ��� REMOVE, ���� � ���� ���� ������������ ��� ������,
		// �� ����� ����������

		// ADD - ��� ADD ��� UPDATE

		LOGGER.debug("sync_groups_roles:01");

		List<String> role_cl = new ArrayList<String>();

		try {

			utx.begin();

			/*
			 * �������� �� ������ ���� - is_exist()
			 * if(idIS==null||idIS.trim().equals("")){ throw new
			 * GeneralFailure("����������� ��� �������!"); }
			 */

			if (codesGroups == null || codesGroups.isEmpty()) {
				LOGGER.debug("sync_groups_roles:return");
				throw new GeneralFailure("����������� ������ �����!");
			}
			if (codesRoles == null || codesRoles.isEmpty()) {
				LOGGER.debug("sync_groups_roles:return2");
				throw new GeneralFailure("����������� ������ �����!");
			}

			if (idIS == null) {
				LOGGER.debug("sync_groups_roles:return3");
				throw new GeneralFailure("idIS is null!");
			}

			if (modeExec == null
					|| modeExec.trim().isEmpty()
					|| (!modeExec.equals("REPLACE") && !modeExec.equals("ADD") && !modeExec
							.equals("REMOVE"))) {
				throw new GeneralFailure("������������ ������ [modeExec]!");
			}

			int mode = 1;

			if (modeExec.equals("REPLACE")) {
				mode = 0;
			} else if (modeExec.equals("ADD")) {
				mode = 1;
			} else if (modeExec.equals("REMOVE")) {
				mode = 2;
			}

			// !!!
			idIS = get_code_is(idIS);

			if (mode == 1) { // //ADD

				for (String group : codesGroups) {

					if (group == null || group.trim().equals("")) {
						throw new GeneralFailure("����������� ��� ������!");
					}

					role_cl = new ArrayList<String>();

					// �������� ����� � ������
					LOGGER.debug("sync_groups_roles:03");

					// ��������� ���� � ������ �� ������ �������

					List<Object[]> lo_roles = em
							.createNativeQuery(
									"select ROL.SIGN_OBJECT, ROL.ID_SRV "
											+ "from GROUP_USERS_KNL_T gr, "
											+ "LINK_GROUP_USERS_ROLES_KNL_T lgr, "
											+ "AC_ROLES_BSS_T rol, "
											+ "AC_IS_BSS_T sys "
											+ "where GR.ID_SRV=LGR.UP_GROUP_USERS "
											+ "and LGR.UP_ROLES=ROL.ID_SRV "
											+ "and SYS.SIGN_OBJECT = ? "
											+ "and SYS.ID_SRV= ROL.UP_IS "
											+ "and GR.SIGN_OBJECT = ? "
											+ "group by  ROL.SIGN_OBJECT, ROL.ID_SRV ")
							.setParameter(1, idIS).setParameter(2, group)
							.getResultList();

					for (Object[] objectArray : lo_roles) {
						role_cl.add(objectArray[0] != null ? objectArray[0]
								.toString() : "");

						LOGGER.debug("sync_groups_roles:04:" + objectArray[0]);

					}

					for (String role_code : codesRoles) {

						LOGGER.debug("sync_groups_roles:05:" + role_code);

						if (!role_cl.contains(role_code)) {

							LOGGER.debug("sync_groups_roles:06");

							em.createNativeQuery(
									"insert into LINK_GROUP_USERS_ROLES_KNL_T(UP_ROLES, UP_GROUP_USERS, CREATOR,  created) "
											+ "values ("
											+ "(select role.ID_SRV from AC_ROLES_BSS_T role, AC_IS_BSS_T sys  "
											+ "where SYS.ID_SRV= ROLE.UP_IS and  SYS.SIGN_OBJECT = ? "
											+ "and role.SIGN_OBJECT = ? ), "
											+ "(select lgu.ID_SRV from GROUP_USERS_KNL_T lgu "
											+ "where lgu.SIGN_OBJECT = ? ), ?, sysdate) ")
									.setParameter(1, idIS)
									.setParameter(2, role_code)
									.setParameter(3, group).setParameter(4, 1L)
									.executeUpdate();
						}
					}
				}

			} else if (mode == 2) { // REMOVE

				LOGGER.debug("sync_groups_roles:03");

				for (String group : codesGroups) {

					LOGGER.debug("sync_groups_roles:04:" + group);

					if (group == null || group.trim().equals("")) {
						throw new GeneralFailure("����������� ��� ������!");
					}

					try {
						LOGGER.debug("sync_group_roles:05");

						for (String role_code : codesRoles) {

							em.createNativeQuery(
									"DELETE FROM LINK_GROUP_USERS_ROLES_KNL_T rls "
											+ "where rls.UP_ROLES = "
											+ "(select role.ID_SRV from AC_ROLES_BSS_T role, AC_IS_BSS_T sys  "
											+ "where SYS.ID_SRV= ROLE.UP_IS and  SYS.SIGN_OBJECT = ? "
											+ "and role.SIGN_OBJECT = ? ) "
											+ "and rls.UP_GROUP_USERS = (select lgu.ID_SRV from GROUP_USERS_KNL_T lgu "
											+ "where lgu.SIGN_OBJECT = ? )")
									.setParameter(1, idIS)
									.setParameter(2, role_code)
									.setParameter(3, group).executeUpdate();

						}

					} catch (Exception e) {
						LOGGER.debug("sync_group_roles:06");
					}
					// }
				}

			} else if (mode == 0) { // REPLACE

				// REPLACE = ALL REMOVE + ADD
				LOGGER.debug("sync_groups_roles:07");

				for (String group : codesGroups) {

					if (group == null || group.trim().equals("")) {
						throw new GeneralFailure("����������� ��� ������!");
					}

					// 1. REMOVE ALL

					try {
						LOGGER.debug("sync_groups_roles:09");

						em.createNativeQuery(
								"DELETE FROM LINK_GROUP_USERS_ROLES_KNL_T rls "
										+ "where rls.UP_GROUP_USERS = "
										+ "(select lgu.ID_SRV from GROUP_USERS_KNL_T lgu "
										+ "where lgu.SIGN_OBJECT = ? )")
								.setParameter(1, group).executeUpdate();

					} catch (Exception e) {
						LOGGER.debug("sync_groups_roles:010");
					}

					// 2.ADD

					for (String role_code : codesRoles) {

						LOGGER.debug("sync_groups_roles:05:" + role_code);

						if (role_code == null || role_code.trim().equals("")) {
							throw new GeneralFailure("����������� ��� ����!");
						}

						em.createNativeQuery(
								"insert into LINK_GROUP_USERS_ROLES_KNL_T(UP_ROLES, UP_GROUP_USERS, CREATOR,  created) "
										+ "values ("
										+ "(select role.ID_SRV from AC_ROLES_BSS_T role, AC_IS_BSS_T sys  "
										+ "where SYS.ID_SRV= ROLE.UP_IS and  SYS.SIGN_OBJECT = ? "
										+ "and role.SIGN_OBJECT = ? ), "
										+ "(select lgu.ID_SRV from GROUP_USERS_KNL_T lgu "
										+ "where lgu.SIGN_OBJECT = ? ), ?, sysdate) ")
								.setParameter(1, idIS)
								.setParameter(2, role_code)
								.setParameter(3, group).setParameter(4, 1L)
								.executeUpdate();
					}
				}

			}

			sys_audit(12L, "idIS:" + idIS, "true", IPAddress, null);

			utx.commit();

		} catch (Exception e) {
			try {

				utx.rollback();

				utx.begin();
				sys_audit(12L, "idIS:" + idIS, "error", IPAddress, null);
				utx.commit();

			} catch (Exception er) {
				try {
					utx.rollback();
				} catch (Exception er1) {
					LOGGER.error("rollback:Error1:", er1);
				}
				LOGGER.error("rollback:Error:", er);
			}
			throw new GeneralFailure(e.getMessage());
		}

	}

		
	/**
	 * ������������� ��������
	 * 
	 */
	public void sync_resources(
			 String idIS, 
			 List<Resource> resources,
			 String modeExec,
			 Long idUserAuth,
			 String IPAddress) throws GeneralFailure{
		 
		 LOGGER.debug("sync_resources:01");
		 
		 //ADD - ��� ADD ��� UPDATE
		 
		 //��� ������ - ��� UPDATE
		 //��� ������ ������ ��� ���������� ������� �� resources.get(0)
		 //��� ������ ��� ������ ������ idIS, � �� resources.get(0).resource.getCode()
		 
		
		 String linksLine=null;
		 
		 try{
			 utx.begin();
			 
			 if(idIS==null||idIS.trim().isEmpty()){
				 throw new GeneralFailure("idIS is null!");
			 }
			 
			 if(modeExec==null || modeExec.trim().isEmpty()||
					 (!modeExec.equals("ADD"))){
	    		 throw new GeneralFailure("������������ ������ [modeExec]!");
	    	 }
			 
			 Map<String, Long> res_cl=new HashMap<String, Long>();
			 
			 int mode=1;
				
			  if(modeExec.equals("ADD")){
				mode=1;
			 }
			 if(idIS.startsWith(CUDConstants.armPrefix)||idIS.startsWith(CUDConstants.subArmPrefix)){
					
				//!!!
				idIS =  get_code_is(idIS);
					
				LOGGER.debug("sync_resources:idIS2:"+idIS);
					
				if(mode==1){ //ADD [UPDATE]
					
					
					if (resources!=null && resources.get(0)!=null){
						
						Resource resource = resources.get(0);
							 
						
						      // ����������������, �.�. ��� ������ ��� ������ ������ idIS
							  /* if(re/source.getCode()==nul/l||reso/urce.getCode().trim().equals("")){
								   throw/ new GeneralFa/ilure("����������� ��� �������!");
							   }els/e */if(resource.getName()==null||resource.getName().trim().equals("")){
								   throw new GeneralFailure("����������� �������� �������!");
							   }
								 
							 	 
							   linksLine=null;
							   
							   if(resource.getLinks()!=null&&!resource.getLinks().isEmpty()){
					            	
						            for(String link :resource.getLinks()){
						    		 if(linksLine==null){
						    			 linksLine=link;
						    		 }else{
						    			 linksLine=linksLine+","+link;
						    		 }
						    		}
							   }
							   
							 	
								   em.createNativeQuery(
										"UPDATE AC_IS_BSS_T sys "+
	                                      "set sys.FULL_=?, sys.DESCRIPTION=?, sys.LINKS=?  "+
	                                      "where sys.SIGN_OBJECT = ?  ")
							         .setParameter(1, resource.getName())
						             .setParameter(2, resource.getDescription())
						             .setParameter(3, linksLine)
						             .setParameter(4, idIS)
						            //!!! .setPara/meter(/4, resourc/e.getCode())
						            
						            .executeUpdate();	
					}
				}
				
				
			 }else if(idIS.startsWith(CUDConstants.groupArmPrefix)){
					//������ ��
				 
				  
				  if(mode==1){ //ADD
				       
			    	   //��������� ������� (����������) � ������ ������
			    	   List<Object[]> lo=
				    			em.createNativeQuery(
				    					" select  SYS.SIGN_OBJECT, SYS.ID_SRV, SYS.FULL_, SYS.DESCRIPTION, sys.LINKS from " + 
				    							" GROUP_SYSTEMS_KNL_T gsys, " + 
				    							" AC_IS_BSS_T sys, " + 
				    							" LINK_GROUP_SYS_SYS_KNL_T lgr " + 
				    							" where GSYS.ID_SRV=LGR.UP_GROUP_SYSTEMS " + 
				    							" and LGR.UP_SYSTEMS= SYS.ID_SRV " + 
				    							" and GSYS.GROUP_CODE=:idIS ")
				                .setParameter("idIS", idIS)
				       	      	.getResultList();
					   LOGGER.debug("sync_resources:02");
				       
				       for(Object[] objectArray :lo){
				    	   res_cl.put(objectArray[0]!=null?objectArray[0].toString():"", 
				        			   objectArray[1]!=null? new Long(objectArray[1].toString()):-1L);
				       }
				       
			    	   
						 for(Resource resource :resources){
							 
						   if(resource.getCode()==null||resource.getCode().trim().equals("")){
							   throw new GeneralFailure("����������� ��� �������!");
						   }else if(resource.getName()==null||resource.getName().trim().equals("")){
							   throw new GeneralFailure("����������� �������� �������!");
						   }
							 
							 
						   linksLine=null;
						   
						   if(resource.getLinks()!=null&&!resource.getLinks().isEmpty()){
				            	
					            for(String link :resource.getLinks()){
					    		 if(linksLine==null){
					    			 linksLine=link;
					    		 }else{
					    			 linksLine=linksLine+","+link;
					    		 }
					    		}
						   }
						   
						   if(res_cl.containsKey(resource.getCode())){
							
							   em.createNativeQuery(
									"UPDATE AC_IS_BSS_T sys "+
                                     "set sys.FULL_=?, sys.DESCRIPTION=?, sys.LINKS=?  "+
                                     "where sys.SIGN_OBJECT = ?  ")
						         .setParameter(1, resource.getName())
					             .setParameter(2, resource.getDescription())
					             .setParameter(3, linksLine)
					             .setParameter(4, resource.getCode())
					            
					            .executeUpdate();	 
							   
													   
						   }else{
							   
							 List results = em.createNativeQuery("select AC_IS_BSS_SEQ.nextval from dual ").getResultList();
					            
					         Long newIdRes = ((BigDecimal)results.get(0)).longValue();
					            
							 em.createNativeQuery(
								     "insert into AC_IS_BSS_T(ID_SRV, SIGN_OBJECT, FULL_ , DESCRIPTION, LINKS, CREATOR,  created ) "+
				                     "values(?, ?, ?, ?, ?, 1, sysdate) ")
					         .setParameter(1, newIdRes)
				             .setParameter(2, resource.getCode())
					         .setParameter(3, resource.getName())
				             .setParameter(4, resource.getDescription())
				             .setParameter(5, linksLine)
				             .executeUpdate();
							 
							 
							 em.createNativeQuery(
								     "insert into LINK_GROUP_SYS_SYS_KNL_T(UP_SYSTEMS, UP_GROUP_SYSTEMS, CREATOR, CREATED  ) "+
				                     "values(?, (select gr_sys.ID_SRV from GROUP_SYSTEMS_KNL_T gr_sys where gr_sys.GROUP_CODE = ? ), 1, sysdate) ")
					         .setParameter(1, newIdRes)
				             .setParameter(2, idIS)
					         .executeUpdate();
							 
						  }
						 }
			       }
		 		}
		 
		  sys_audit(101L, "idIS:"+idIS, "true", IPAddress, null ); 
		 
		  utx.commit();
		  
		 }catch(Exception e){
			 
		  try{ 
			 utx.rollback();
				
			 utx.begin();
			 sys_audit(101L, "idIS:"+idIS, "error", IPAddress, null );
			 utx.commit();
			 
		    }catch (Exception er) {
				try{utx.rollback();}catch (Exception er1) {LOGGER.error("rollback:Error1:"+er1);} 
				LOGGER.error("rollback:Error:"+er);
			} 
		 
			 throw new GeneralFailure(e.getMessage());
			 
			 
		 }
	 }
	
	

	/**
	 * ������������� ����� ��������
	 */
	public void sync_resources_roles(String idIS, List<String> codesResources,
			List<String> codesRoles, String modeExec, Long idUserAuth,
			String IPAddress) throws GeneralFailure {

		// ��� ������ � ���������

		// !!!
		// �������� ����� ���������� ���������� ADD

		// modeExec:
		// 0 - REPLACE
		// 1 - ADD
		// 2 - REMOVE

		// ��� REMOVE, ���� � ���� ���� ������������ ��� ������,
		// �� ����� ����������

		// ADD - ��� ADD ��� UPDATE

		LOGGER.debug("sync_groups_roles:01");

		List<String> role_cl = new ArrayList<String>();

		try {

			utx.begin();

			/*
			 * �������� �� ������ ���� - is_exist()
			 */

			if (codesResources == null || codesResources.isEmpty()) {
				LOGGER.debug("sync_resources_roles:return");
				throw new GeneralFailure("����������� ������ ��������!");
			}
			if (codesRoles == null || codesRoles.isEmpty()) {
				LOGGER.debug("sync_resources_roles:return2");
				throw new GeneralFailure("����������� ������ �����!");
			}

			if (idIS == null) {
				LOGGER.debug("sync_resources_roles:return3");
				throw new GeneralFailure("idIS is null!");
			}

			if (modeExec == null
					|| modeExec.trim().isEmpty()
					|| (!modeExec.equals("REPLACE") && !modeExec.equals("ADD") && !modeExec
							.equals("REMOVE"))) {
				throw new GeneralFailure("������������ ������ [modeExec]!");
			}

			int mode = 1;

			if (modeExec.equals("REPLACE")) {
				mode = 0;
			} else if (modeExec.equals("ADD")) {
				mode = 1;
			} else if (modeExec.equals("REMOVE")) {
				mode = 2;
			}

			// !!!
			idIS = get_code_is(idIS);

			if (mode == 1) { // //ADD

				for (String resource : codesResources) {

					if (resource == null || resource.trim().equals("")) {
						throw new GeneralFailure("����������� ��� �������!");
					}

					role_cl = new ArrayList<String>();

					// �������� ����� � �������
					LOGGER.debug("sync_resources_roles:03");

					// ��������� ���� � ������� �� ������ �������

					List<Object[]> lo_roles = em
							.createNativeQuery(
									"  SELECT ROL.SIGN_OBJECT, ROL.ID_SRV "
											+ "FROM AC_RESOURCES_BSS_T res, "
											+ "AC_LINK_ROLE_RESOURCE_KNL_T lgr, "
											+ "AC_ROLES_BSS_T rol, "
											+ "AC_IS_BSS_T sys "
											+ "WHERE  res.ID_SRV = LGR.UP_RESOURCE "
											+ "AND LGR.UP_ROLE = ROL.ID_SRV "
											+ "AND SYS.SIGN_OBJECT = ? "
											+ "AND SYS.ID_SRV = ROL.UP_IS "
											+ "AND res.SIGN_OBJECT = ? "
											+ "GROUP BY ROL.SIGN_OBJECT, ROL.ID_SRV")
							.setParameter(1, idIS).setParameter(2, resource)
							.getResultList();

					for (Object[] objectArray : lo_roles) {
						role_cl.add(objectArray[0] != null ? objectArray[0]
								.toString() : "");

						LOGGER.debug("sync_resources_roles:04:" + objectArray[0]);

					}

					for (String role_code : codesRoles) {

						LOGGER.debug("sync_resources_roles:05:" + role_code);

						if (!role_cl.contains(role_code)) {

							LOGGER.debug("sync_resources_roles:06");

							em.createNativeQuery(
									"insert into AC_LINK_ROLE_RESOURCE_KNL_T(UP_ROLE, UP_RESOURCE, CREATOR,  created) "
											+ "values ("
											+ "(select role.ID_SRV from AC_ROLES_BSS_T role, AC_IS_BSS_T sys  "
											+ "where SYS.ID_SRV= ROLE.UP_IS and  SYS.SIGN_OBJECT = ? "
											+ "and role.SIGN_OBJECT = ? ), "
											+ "(select ROL.ID_SRV from AC_ROLES_BSS_T rol "
											+ "where rol.SIGN_OBJECT = ? ), ?, sysdate) ")
									.setParameter(1, idIS)
									.setParameter(2, role_code)
									.setParameter(3, resource)
									.setParameter(4, 1L).executeUpdate();
						}
					}
				}

			} else if (mode == 2) { // REMOVE

				LOGGER.debug("sync_resources_roles:03");

				for (String resource : codesResources) {

					LOGGER.debug("sync_resources_roles:04:" + resource);

					if (resource == null || resource.trim().equals("")) {
						throw new GeneralFailure("����������� ��� ������!");
					}

					try {
						LOGGER.debug("sync_resources_roles:05");

						for (String role_code : codesRoles) {

							em.createNativeQuery(

									"DELETE FROM AC_LINK_ROLE_RESOURCE_KNL_T lrr "
											+ "where LRR.UP_RESOURCE = "
											+ "(select RES.ID_SRV "
											+ "from AC_RESOURCES_BSS_T res "
											+ "AC_IS_BSS_T app "
											+ "where res.UP_IS=APP.ID_SRV "
											+ "and APP.SIGN_OBJECT = :sysCode "
											+ "and RES.SIGN_OBJECT = :resCode ) "
											+ "and lrr.UP_ROLE = "
											+ "(select role.ID_SRV from AC_ROLES_BSS_T role, AC_IS_BSS_T sys  "
											+ "where SYS.ID_SRV= ROLE.UP_IS and  SYS.SIGN_OBJECT = :sysCode "
											+ "and role.SIGN_OBJECT = :roleCode ) ")
									.setParameter("sysCode", idIS)
									.setParameter("roleCode", role_code)
									.setParameter("resCode", resource)
									.executeUpdate();

						}

					} catch (Exception e) {
						LOGGER.error("sync_resources_roles:06");
					}
					// }
				}

			} else if (mode == 0) { // REPLACE

				// REPLACE = ALL REMOVE + ADD
				LOGGER.debug("sync_resources_roles:07");

				for (String resource : codesResources) {

					if (resource == null || resource.trim().equals("")) {
						throw new GeneralFailure("����������� ��� ������!");
					}

					// 1. REMOVE ALL

					try {
						LOGGER.debug("sync_resources_roles:09");

						em.createNativeQuery(
								"DELETE FROM AC_LINK_ROLE_RESOURCE_KNL_T lrr "
										+ "where LRR.UP_RESOURCE = ( "
										+ "select RES.ID_SRV "
										+ "from AC_RESOURCES_BSS_T res, "
										+ "AC_IS_BSS_T app "
										+ "where res.UP_IS=APP.ID_SRV "
										+ "and APP.SIGN_OBJECT = ? "
										+ "and  RES.SIGN_OBJECT = ? ) ")
								.setParameter(1, resource)
								.setParameter(2, idIS).executeUpdate();

					} catch (Exception e) {
						LOGGER.error("sync_resources_roles:010");
					}

					// 2.ADD

					for (String role_code : codesRoles) {

						LOGGER.debug("sync_resources_roles:05:" + role_code);

						if (role_code == null || role_code.trim().equals("")) {
							throw new GeneralFailure("����������� ��� ����!");
						}

						em.createNativeQuery(
								"insert into AC_LINK_ROLE_RESOURCE_KNL_T(UP_ROLE, UP_RESOURCE, CREATOR,  created) "
										+ "values ("
										+ "(select role.ID_SRV from AC_ROLES_BSS_T role, AC_IS_BSS_T sys  "
										+ "where SYS.ID_SRV= ROLE.UP_IS and  SYS.SIGN_OBJECT = ? "
										+ "and role.SIGN_OBJECT = ? ), "
										+ "(select ROL.ID_SRV from AC_ROLES_BSS_T rol "
										+ "where rol.SIGN_OBJECT = ? ), ?, sysdate) ")
								.setParameter(1, idIS)
								.setParameter(2, role_code)
								.setParameter(3, resource).setParameter(4, 1L)
								.executeUpdate();

					}
				}

			}

			sys_audit(12L, "idIS:" + idIS, "true", IPAddress, null);

			utx.commit();

		} catch (Exception e) {
			try {

				utx.rollback();

				utx.begin();
				sys_audit(12L, "idIS:" + idIS, "error", IPAddress, null);
				utx.commit();

			} catch (Exception er) {
				try {
					utx.rollback();
				} catch (Exception er1) {
					LOGGER.error("rollback:Error1:", er1);
				}
				LOGGER.error("rollback:Error:", er);
			}
			throw new GeneralFailure(e.getMessage());
		}
	}

	/**
	 * �������� ������� �������
	 */
	public void is_exist(String idIS) throws GeneralFailure {

		LOGGER.debug(":is_exist:01");

		try {

			em.createNativeQuery(
					"select APP.ID_SRV " + "from AC_IS_BSS_T app "
							+ "where APP.SIGN_OBJECT=?").setParameter(1, idIS)
					.getSingleResult();

		} catch (NoResultException ex) {
			LOGGER.error("is_exist:NoResultException");
			throw new GeneralFailure("�������������� ������� �� ���������!");
		} catch (Exception e) {
			LOGGER.error("is_exist:Error:", e);
			throw new GeneralFailure(e.getMessage());
		}
	}

	/**
	 * ��������� ���� ������� �� ���� ����������
	 */
	private String get_code_is(String codeSys) throws GeneralFailure {

		// ��������� ������ ��������, ����� ����� ��� ����������,
		// �� ������ �������� ��� �������

		String result = null;

		try {
			result = (String) em
					.createNativeQuery(
							"select SYS.SIGN_OBJECT "
									+ "from  AC_IS_BSS_T sys, "
									+ "AC_SUBSYSTEM_CERT_BSS_T subsys "
									+ "where (SYS.SIGN_OBJECT= :codeSys or  SUBSYS.SUBSYSTEM_CODE= :codeSys) "
									+ "and  SUBSYS.UP_IS(+) =SYS.ID_SRV "
									+ "group by SYS.SIGN_OBJECT ")
					.setParameter("codeSys", codeSys).getSingleResult();

		} catch (NoResultException ex) {
			LOGGER.error("get_id_is:NoResultException");
			throw new GeneralFailure("System is not defined");
		} catch (Exception e) {
			throw new GeneralFailure(e.getMessage());
		}
		return result;

	}

	/**
	 * ���������������� ��������
	 */
	private void sys_audit(Long idServ, String inp_param, String result,
			String ip_adr, Long idUser) {
		LOGGER.debug("sys_audit:01");
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
			LOGGER.error("is_exist:Error:", e);
		}

	}

}
