package ru.spb.iac.cud.core.eis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Remote;

import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.InvalidCredentials;
import ru.spb.iac.cud.exceptions.TokenExpired;
import ru.spb.iac.cud.items.Attribute;
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
public interface AdminManagerRemote {

	public void access(String codeSystem, List<String> uidsUsers,
			String modeExec, List<String> codesRoles, Long idUserAuth,
			String IPAddress) throws GeneralFailure;

	public void access_groups(String codeSystem, List<String> uidsUsers,
			String modeExec, List<String> codesGroups, Long idUserAuth,
			String IPAddress) throws GeneralFailure;

	public void cert_change(String codeSystem, String newCert, Long idUserAuth,
			String IPAddress) throws GeneralFailure;

}
