package ru.spb.iac.cud.services.audit;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.TokenExpired;
import ru.spb.iac.cud.items.AuditFunction;
import ru.spb.iac.cud.items.Function;
import ru.spb.iac.cud.items.ISUsers;
import ru.spb.iac.cud.items.Role;
import ru.spb.iac.cud.items.Token;
import ru.spb.iac.cud.items.UserAttributes;

@WebService(targetNamespace = AuditService.NS)
public interface AuditService {
	
	 public static final String NS = "http://audit.services.cud.iac.spb.ru/";
	   
	 @WebMethod
	 public void audit(
			  @WebParam(name = "uidUser", targetNamespace = NS) String uidUser,
			  @WebParam(name = "userFunctions", targetNamespace = NS)  List<AuditFunction> userFunctions) throws GeneralFailure;

	 @WebMethod
	 public void sync_functions(
			 @WebParam(name = "functions", targetNamespace = NS) List<Function> functions,
			 @WebParam(name = "modeExec", targetNamespace = NS) String modeExec) throws GeneralFailure;
}
