package ru.spb.iac.cud.core.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Remote;

import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.InvalidCredentials;
import ru.spb.iac.cud.exceptions.TokenExpired;
import ru.spb.iac.cud.items.app.AppAttribute;
import ru.spb.iac.cud.items.AuditFunction;
import ru.spb.iac.cud.items.Function;
import ru.spb.iac.cud.items.ISUsers;
import ru.spb.iac.cud.items.Role;
import ru.spb.iac.cud.items.Token;
import ru.spb.iac.cud.items.UserAttributes;
import ru.spb.iac.cud.items.app.AppAccept;
import ru.spb.iac.cud.items.app.AppTypeClassif;

/**
 * @author bubnov
 */
@Remote
public interface ApplicationManagerRemote {

	public AppAccept system_registration(List<AppAttribute> attributes,
			String principal, Long idUser, String IPAddress)
			throws GeneralFailure;

	public AppAccept user_registration(List<AppAttribute> attributes,
			String principal, Long idUser, String IPAddress)
			throws GeneralFailure;

	public AppAccept access_roles(String modeExec, String loginUser,
			String codeSystem, List<String> codesRoles, String principal,
			Long idUser, String IPAddress) throws GeneralFailure;

	public AppAccept access_groups(String modeExec, String loginUser,
			String codeSystem, List<String> codesGroups, String principal,
			Long idUser, String IPAddress) throws GeneralFailure;

	public AppAccept block(String modeExec, String loginUser,
			String blockReason, String principal, Long idUser, String IPAddress)
			throws GeneralFailure;

	public AppAccept system_modification(String codeSystem,
			List<AppAttribute> attributes, String principal, Long idUser,
			String IPAddress) throws GeneralFailure;

	public AppAccept user_modification(String loginUser,
			List<AppAttribute> attributes, String principal, Long idUser,
			String IPAddress) throws GeneralFailure;

	public AppAccept user_identity_modification(String loginUser, String login,
			String password, Long idUser, String IPAddress)
			throws GeneralFailure;

	public AppAccept user_cert_modification(String modeExec, String loginUser,
			String certBase64, Long idUser, String IPAddress)
			throws GeneralFailure;

	public AppAccept user_dep_modification(String loginUser,
			List<AppAttribute> attributes, String principal, Long idUser,
			String IPAddress) throws GeneralFailure;

	public Long principal_exist(String principal) throws GeneralFailure;
}
