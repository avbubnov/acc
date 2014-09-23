package ru.spb.iac.cud.services.util;

import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.security.annotation.SecurityDomain;
import org.w3c.dom.Element;

import ru.spb.iac.cud.context.ContextSyncManager;
import ru.spb.iac.cud.context.eis.ContextUtilManager;
import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.TokenExpired;
import ru.spb.iac.cud.items.Function;
import ru.spb.iac.cud.items.GroupsData;
import ru.spb.iac.cud.items.ISUsers;
import ru.spb.iac.cud.items.Resource;
import ru.spb.iac.cud.items.ResourcesData;
import ru.spb.iac.cud.items.Role;
import ru.spb.iac.cud.items.Token;
import ru.spb.iac.cud.items.UserAttributes;
import ru.spb.iac.cud.items.UsersData;

@WebService(targetNamespace = UtilServiceImpl.NS)
@HandlerChain(file = "/handlers.xml")
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class UtilServiceImpl implements UtilService {

	public static final String NS = "http://util.services.cud.iac.spb.ru/";

	@javax.annotation.Resource(name = "wsContext")
	private WebServiceContext wsContext;

	@WebMethod
	@WebResult(targetNamespace = NS)
	public UsersData users_data(
			@WebParam(name = "uidsUsers", targetNamespace = NS) List<String> uidsUsers,
			@WebParam(name = "category", targetNamespace = NS) String category,
			@WebParam(name = "rolesCodes", targetNamespace = NS) List<String> rolesCodes,
			@WebParam(name = "groupsCodes", targetNamespace = NS) List<String> groupsCodes,
			@WebParam(name = "startRow", targetNamespace = NS) Integer startRow,
			@WebParam(name = "countRow", targetNamespace = NS) Integer countRow,
			@WebParam(name = "settings", targetNamespace = NS) List<String> settings)
			throws GeneralFailure {

		return (new ContextUtilManager()).users_data(getIDSystem(), uidsUsers,
				category, rolesCodes, groupsCodes, startRow, countRow, null,
				getIDUser(), getIPAddress());

	}

	@WebMethod
	@WebResult(targetNamespace = NS)
	public List<Role> sys_roles() throws GeneralFailure {
		return (new ContextSyncManager()).is_roles(getIDSystem(), getIDUser(),
				getIPAddress());
	}

	@WebResult(targetNamespace = NS)
	public List<Function> sys_functions() throws GeneralFailure {
		System.out.println("AuditServiceImpl:is_functions");
		return (new ContextSyncManager()).is_functions(getIDSystem(),
				getIDUser(), getIPAddress());

	}

	@WebMethod
	@WebResult(targetNamespace = NS)
	public GroupsData groups_data(
			@WebParam(name = "groupsCodes", targetNamespace = NS) List<String> groupsCodes,
			@WebParam(name = "category", targetNamespace = NS) String category,
			@WebParam(name = "rolesCodes", targetNamespace = NS) List<String> rolesCodes,
			@WebParam(name = "startRow", targetNamespace = NS) Integer startRow,
			@WebParam(name = "countRow", targetNamespace = NS) Integer countRow,
			@WebParam(name = "settings", targetNamespace = NS) List<String> settings)
			throws GeneralFailure {

		return (new ContextUtilManager()).groups_data(getIDSystem(),
				groupsCodes, category, rolesCodes, startRow, countRow, null,
				getIDUser(), getIPAddress());
	}

	/*
	 * @WebResult(targetNamespace = NS) public ResourcesData resources_data(
	 * 
	 * @WebParam(name = "resourcesCodes", targetNamespace = NS) List<String>
	 * resourcesCodes,
	 * 
	 * @WebParam(name = "category", targetNamespace = NS) String category,
	 * 
	 * @WebParam(name = "rolesCodes", targetNamespace = NS) List<String>
	 * rolesCodes,
	 * 
	 * @WebParam(name = "startRow", targetNamespace = NS) Integer startRow,
	 * 
	 * @WebParam(name = "countRow", targetNamespace = NS) Integer countRow,
	 * 
	 * @WebParam(name = "settings", targetNamespace = NS) Map<String, String>
	 * settings) throws GeneralFailure{
	 * 
	 * return (new ContextUtilManager()).resources_data( getIDSystem(),
	 * resourcesCodes, category, rolesCodes, startRow, countRow, settings,
	 * getIDUser(), getIPAddress());
	 * 
	 * }
	 */

	@WebResult(targetNamespace = NS)
	public List<Resource> resources_data(
	/*
	 * @WebParam(name = "resourcesCodes", targetNamespace = NS) List<String>
	 * resourcesCodes,
	 */
	@WebParam(name = "category", targetNamespace = NS) String category)
			throws GeneralFailure {

		return (new ContextUtilManager()).resources_data_subsys(getIDSystem(), /*
																				 * resourcesCodes
																				 * ,
																				 */
				category, getIDUser(), getIPAddress());

	}

	private String getIPAddress() {
		MessageContext context = wsContext.getMessageContext();
		HttpServletRequest request = (HttpServletRequest) context
				.get(MessageContext.SERVLET_REQUEST);

		String ipAddress = request.getRemoteAddr();
		return ipAddress;
	}

	private String getIDSystem() {
		MessageContext context = wsContext.getMessageContext();
		HttpServletRequest request = (HttpServletRequest) context
				.get(MessageContext.SERVLET_REQUEST);

		String idSystem = (String) request.getSession().getAttribute(
				"cud_sts_principal");

		System.out.println("UtilServicesImpl:getIDSystem:" + idSystem);

		return idSystem;
	}

	private Long getIDUser() throws GeneralFailure {
		MessageContext context = wsContext.getMessageContext();
		HttpServletRequest request = (HttpServletRequest) context
				.get(MessageContext.SERVLET_REQUEST);

		Long idUser = null;
		try {
			// user из ApplicantToken_1
			// это заявитель

			// когда пользователя определяли по логину, то сначала в
			// AppSOAPHandler
			// вычисляли его ИД через authenticate_login_obo
			// и в сессию клали уже Long idUser,
			// поэтому при извлечении из сессии можно было делать привидение к
			// Long
			// сейчас же мы кладём в сессиию ид пользователя из текстового поля
			// запроса
			// Long idUser =
			// (Long)request.getSession().getAttribute("user_id_principal");

			if (request.getSession().getAttribute("user_id_principal") != null
					&& !request.getSession().getAttribute("user_id_principal")
							.toString().isEmpty()) {

				// это заявитель
				idUser = new Long((String) request.getSession().getAttribute(
						"user_id_principal"));

				// System.out.println("ApplicationServicesImpl:getIDUser:"+idUser);

			} else {
				// аноним
				idUser = -1L;
			}
			return idUser;

		} catch (Exception e) {
			throw new GeneralFailure("USER UID IS NOT CORRECT");
		}
	}
}
