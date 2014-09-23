package ru.spb.iac.cud.core.app;

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
import ru.spb.iac.cud.items.app.AppResult;
import ru.spb.iac.cud.items.app.AppResultRequest;
import ru.spb.iac.cud.items.app.AppSystemResult;
import ru.spb.iac.cud.items.app.AppTypeClassif;

/**
 * @author bubnov
 */
@Remote
public interface ApplicationResultManagerRemote {

	public List<AppResult> result(List<AppResultRequest> request_list,
			Long idUserAuth, String IPAddress) throws GeneralFailure;

	public void number_secret_valid(String number, String secret, String type)
			throws GeneralFailure;

	public Long principal_exist(String principal) throws GeneralFailure;
}
