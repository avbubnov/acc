package ru.spb.iac.cud.services.application;

import java.security.Principal;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceContext;

import ru.spb.iac.cud.context.application.ContextApplicationManager;
import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.TokenExpired;
import ru.spb.iac.cud.items.app.AppAttribute;
import ru.spb.iac.cud.items.app.AppAccept;
import ru.spb.iac.cud.items.app.AppSystemResult;
import ru.spb.iac.cud.items.app.AppTypeClassif;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.security.SimplePrincipal;
import org.slf4j.Logger;
//import org.jboss.ws.annotation.EndpointConfig;
import org.slf4j.LoggerFactory;

@WebService(targetNamespace = ApplicationServiceImpl.NS)
@HandlerChain(file = "/handlers.xml")
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class ApplicationServiceImpl implements ApplicationService {

	public static final String NS = "http://application.services.cud.iac.spb.ru/";

	Logger logger = LoggerFactory.getLogger(ApplicationServiceImpl.class);

	@Resource(name = "wsContext")
	private WebServiceContext wsContext;

	@WebMethod
	@WebResult(targetNamespace = NS)
	public AppAccept system_registration(
			@WebParam(name = "attributes", targetNamespace = NS) List<AppAttribute> attributes)
			throws GeneralFailure {

		logger.info("system_registration");
		return (new ContextApplicationManager()).system_registration(
				attributes, getIDUser(), getIPAddress());
	}

	@WebMethod
	@WebResult(targetNamespace = NS)
	public AppAccept user_registration(
			@WebParam(name = "attributes", targetNamespace = NS) List<AppAttribute> attributes)
			throws GeneralFailure {

		logger.info("user_registration");

		return (new ContextApplicationManager()).user_registration(attributes,
				getIDUser(), getIPAddress());
	}

	@WebMethod
	@WebResult(targetNamespace = NS)
	public AppAccept access_roles(
			@WebParam(name = "modeExec", targetNamespace = NS) String modeExec,
			@WebParam(name = "uidUser", targetNamespace = NS) String uidUser,
			@WebParam(name = "codesRoles", targetNamespace = NS) List<String> codesRoles)
			throws GeneralFailure {

		// mode:
		// 1) REPLACE
		// 2) ADD
		// 3) REMOVE

		logger.info("access_roles");

		return (new ContextApplicationManager())
				.access_roles(modeExec, uidUser, getIDSystem(), codesRoles,
						getIDUser(), getIPAddress());
	}

	@WebMethod
	@WebResult(targetNamespace = NS)
	public AppAccept access_groups(
			@WebParam(name = "modeExec", targetNamespace = NS) String modeExec,
			@WebParam(name = "uidUser", targetNamespace = NS) String uidUser,
			@WebParam(name = "codesGroups", targetNamespace = NS) List<String> codesGroups)
			throws GeneralFailure {

		// mode:
		// 1) REPLACE
		// 2) ADD
		// 3) REMOVE

		logger.info("access_groups");

		return (new ContextApplicationManager()).access_groups(modeExec,
				uidUser, getIDSystem(), codesGroups, getIDUser(),
				getIPAddress());
	}

	@WebMethod
	@WebResult(targetNamespace = NS)
	public AppAccept block(
			@WebParam(name = "modeExec", targetNamespace = NS) String modeExec,
			/*
			 * @WebParam(name = "loginUser", targetNamespace = NS) String
			 * loginUser,
			 */
			@WebParam(name = "uidUser", targetNamespace = NS) String uidUser,
			@WebParam(name = "blockUser", targetNamespace = NS) String blockReason)
			throws GeneralFailure {

		// mode:
		// 1) BLOCK
		// 2) UNBLOCK

		logger.info("block");

		return (new ContextApplicationManager()).block(modeExec, uidUser,
				blockReason, getIDUser(), getIPAddress());
	}

	@WebMethod
	@WebResult(targetNamespace = NS)
	public AppAccept system_modification(
			@WebParam(name = "attributes", targetNamespace = NS) List<AppAttribute> attributes)
			throws GeneralFailure {

		logger.info("system_modification");

		return (new ContextApplicationManager()).system_modification(
				getIDSystem(), attributes, getIDUser(), getIPAddress());
	}

	@WebMethod
	@WebResult(targetNamespace = NS)
	public AppAccept user_modification(
			@WebParam(name = "uidUser", targetNamespace = NS) String uidUser,
			@WebParam(name = "attributes", targetNamespace = NS) List<AppAttribute> attributes)
			throws GeneralFailure {

		logger.info("user_modification");

		return (new ContextApplicationManager()).user_modification(uidUser,
				attributes, getIDUser(), getIPAddress());

	}

	@WebMethod
	@WebResult(targetNamespace = NS)
	public AppAccept user_identity_modification(
			@WebParam(name = "uidUser", targetNamespace = NS) String uidUser,
			@WebParam(name = "login", targetNamespace = NS) String login,
			@WebParam(name = "password", targetNamespace = NS) String password)
			throws GeneralFailure {

		return (new ContextApplicationManager()).user_identity_modification(
				uidUser, login, password, getIDUser(), getIPAddress());
	}

	@WebMethod
	@WebResult(targetNamespace = NS)
	public AppAccept user_cert_modification(
			@WebParam(name = "modeExec", targetNamespace = NS) String modeExec,
			@WebParam(name = "uidUser", targetNamespace = NS) String uidUser,
			@WebParam(name = "certBase64", targetNamespace = NS) String certBase64)
			throws GeneralFailure {

		return (new ContextApplicationManager()).user_cert_modification(
				modeExec, uidUser, certBase64, getIDUser(), getIPAddress());
	}

	private String getIPAddress() {
		MessageContext context = wsContext.getMessageContext();
		HttpServletRequest request = (HttpServletRequest) context
				.get(MessageContext.SERVLET_REQUEST);

		String ipAddress = request.getRemoteAddr();

		return ipAddress;
	}

	private Long getIDUser() throws GeneralFailure {
		MessageContext context = wsContext.getMessageContext();
		HttpServletRequest request = (HttpServletRequest) context
				.get(MessageContext.SERVLET_REQUEST);

		Long idUser = null;
		try {
			// user �� ApplicantToken_1
			// ��� ���������

			// ����� ������������ ���������� �� ������, �� ������� �
			// AppSOAPHandler
			// ��������� ��� �� ����� authenticate_login_obo
			// � � ������ ����� ��� Long idUser,
			// ������� ��� ���������� �� ������ ����� ���� ������ ���������� �
			// Long
			// ������ �� �� ����� � ������� �� ������������ �� ���������� ����
			// �������
			// Long idUser =
			// (Long)request.getSession().getAttribute("user_id_principal");

			if (request.getSession().getAttribute("user_id_principal") != null
					&& !request.getSession().getAttribute("user_id_principal")
							.toString().isEmpty()) {

				// ��� ���������
				idUser = new Long((String) request.getSession().getAttribute(
						"user_id_principal"));

				// logger.info("getIDUser:"+idUser);

			} else {
				// �������
				idUser = -1L;
			}
			return idUser;

		} catch (Exception e) {
			throw new GeneralFailure("USER UID IS NOT CORRECT");
		}
	}

	private String getIDSystem() {
		MessageContext context = wsContext.getMessageContext();
		HttpServletRequest request = (HttpServletRequest) context
				.get(MessageContext.SERVLET_REQUEST);

		String idSystem = (String) request.getSession().getAttribute(
				"cud_sts_principal");

		logger.info("getIDSystem:" + idSystem);

		return idSystem;
	}
}
