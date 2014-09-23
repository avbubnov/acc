package ru.spb.iac.cud.services.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.cert.X509Certificate;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.ws.handler.MessageContext;

import org.apache.catalina.Session;
import org.jboss.as.web.security.SecurityContextAssociationValve;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spb.iac.cud.context.ContextAccessWebManager;
import ru.spb.iac.cud.context.ContextIDPAccessManager;
import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.InvalidCredentials;
import ru.spb.iac.cud.exceptions.RevokedCertificate;
import ru.spb.iac.cud.exceptions.TokenExpired;
import ru.spb.iac.cud.items.Token;

/**
 * Servlet implementation class AccessServicesWeb
 */
public class AccessServicesWeb extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String X509CERTIFICATE_REQUEST_ATTRIBUTE = "javax.servlet.request.X509Certificate";

	final static Logger logger = LoggerFactory
			.getLogger(AccessServicesWeb.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AccessServicesWeb() {
		super();
	}

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		Token token = null;
		String success = "false";
		int revokedCertificate = 0;
		String login_user = null;

		HttpSession hs = request.getSession();

		try {

			/*
			 * String tokenID_attr = (String) hs.getAttribute("tokenID");
			 * 
			 * logger.info("service:tokenID_attr:"+tokenID_attr);
			 * 
			 * 
			 * //не дойдёт до выполнения //перехватывается в фильтре //if
			 * requestURI.endsWith(cert_to_form_ie) //if(principal2!=null){
			 * //response.sendRedirect(request.getContextPath()+main);
			 * if(tokenID_attr!=null&&isTokenActive(tokenID_attr)){ //sso && не
			 * было logout
			 * 
			 * sendPost(backUrl, response, "true", tokenID_attr, 0); }else{
			 */

			String sn = getSN(request);

			if (sn != null) {

				login_user = (new ContextAccessWebManager())
						.authenticate_cert_sn(sn, getIPAddress(request),
								getCodeSystem(request));

				success = "true";

				hs.setAttribute("tokenID", token.getId());

				hs.setAttribute("login_user", login_user);
			}

			// }

		} catch (InvalidCredentials e1) {
			logger.error("service:error1:" + e1.getMessage());
		} catch (GeneralFailure e2) {
			logger.error("service:error2:" + e2.getMessage());
		} catch (RevokedCertificate e3) {
			revokedCertificate = 1;
			logger.error("service:error3:" + e3.getMessage());
		} catch (Exception e4) {
			logger.error("service:error4:" + e4.getMessage());
		}
		if (success.equals("true")) {

			request.getSession().setAttribute("cud_auth_type",
					"urn:oasis:names:tc:SAML:2.0:ac:classes:X509");

			request.getSession().setAttribute("authenticate", "success");
			response.sendRedirect(request.getContextPath());

		} else {

			failure(response);

			// hs.invalidate();

			// eraseCookie(request, response);

		}

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}

	private String getSN(HttpServletRequest hsr) {

		// logger.info("getSN:01");

		String result = null;

		X509Certificate[] certs = (X509Certificate[]) hsr
				.getAttribute(X509CERTIFICATE_REQUEST_ATTRIBUTE);

		X509Certificate clientCert = null;
		if (null != certs) {
			// logger.info("verify:length:"+certs.length);
		}

		if (null != certs && certs.length > 0) {
			clientCert = certs[0];

			result = clientCert.getSerialNumber().toString(16).toUpperCase();

			// logger.info("getSN:SubjectDN:"+clientCert.getSubjectDN().getName());
			// logger.info("getSN:SerialNumber:"+clientCert.getSerialNumber());

		}

		return result;
	}

	private String getIPAddress(HttpServletRequest request) {

		String ipAddress = request.getRemoteAddr();

		return ipAddress;
	}

	public void failure(HttpServletResponse response) throws IOException {

		// logger.info("failure:01");

		try {

			common(response);

			response.setContentType("text/html; charset=windows-1251");
			// response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

			PrintWriter pw = response.getWriter();

			pw.print("<html>");

			pw.print("<body>");
			pw.print("<div>");
			pw.print("Учётной записи нет в системе!");
			pw.print("<br/>");
			pw.print("<a href='javascript:location.reload(true);'>");
			pw.print("Войти</a>");
			/*
			 * pw.print("<a href='javascript:history.go(-1);'>");
			 * pw.print("Вернуться назад</a>");
			 */
			/*
			 * pw.print("<a href='"); pw.print(redirect_url);
			 * pw.print("'>Войти заново</a>");
			 */
			pw.print("</div>");

			pw.print("</body>");
			pw.print("</html>");

			pw.print("<script type='text/javascript'>");
			pw.print("document.execCommand('ClearAuthenticationCache');");
			pw.print("</script>");

			pw.close();

		} catch (Exception e) {
			logger.error("failure:error:" + e);
		}

		return;
	}

	private void eraseCookie(HttpServletRequest req, HttpServletResponse resp) {

		// logger.info("eraseCookie:01");

		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {

				// logger.info("eraseCookie:02:"+cookies[i].getName());

				cookies[i].setValue("");
				cookies[i].setPath("/");
				cookies[i].setMaxAge(0);
				resp.addCookie(cookies[i]);
			}
		}
	}

	private static void common(HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache, no-store");
	}

	private String getCodeSystem(HttpServletRequest request) {

		// !!!
		// для метода важно, что в IDPValve сохраняется SAMLRequest,
		// а в ExtFilter - incoming_http_method

		// 1.
		// 1)вызывается IDPValve(сохраняется SAMLRequest)
		// 2)ExtFilter (сохраняется incoming_http_method) -
		// redirect(cert_to_form_ie)
		// 2.
		// 1)вызывается IDPValve
		// 2)ExtFilter - просто пропускает запрос ниже - на сервлет
		// 3)AccessServicesWeb

		logger.info("getCodeSystem:031");
		String result = null;

		try {
			org.apache.catalina.connector.Request request2 = null;
			request2 = SecurityContextAssociationValve.getActiveRequest();
			Session session = request2.getSessionInternal(false);

			// "SAMLRequest"
			String samlRequestMessage = (String) session
					.getNote(GeneralConstants.SAML_REQUEST_KEY);

			if (samlRequestMessage != null) {

				// IDPWebRequestUtil webRequestUtil = new
				// IDPWebRequestUtil(request, null, null);
				// SAMLDocumentHolder samlDocumentHolder =
				// webRequestUtil.getSAMLDocumentHolder(samlRequestMessage);

				boolean begin_req_method = "GET".equals((String) request
						.getSession().getAttribute("incoming_http_method"));

				SAMLDocumentHolder samlDocumentHolder = getSAMLDocumentHolder(
						samlRequestMessage, begin_req_method);

				if (samlDocumentHolder != null) {

					if (samlDocumentHolder.getSamlObject() != null) {
						// RequestAbstractType requestAbstractType =
						// (RequestAbstractType)samlDocumentHolder.getSamlObject();
						AuthnRequestType requestAbstractType = (AuthnRequestType) samlDocumentHolder
								.getSamlObject();
						result = requestAbstractType.getIssuer().getValue();

						logger.info("getCodeSystem:032:" + result);

					}
				}

			}

		} catch (Exception e) {
			logger.error("getCodeSystem:error:" + e);
			// e.printStackTrace(System.out);
		}

		return result;
	}

	public SAMLDocumentHolder getSAMLDocumentHolder(String samlMessage,
			boolean redirectProfile) throws Exception {
		logger.info("getSAMLDocumentHolder:01:" + redirectProfile);

		InputStream is = null;
		SAML2Request saml2Request = new SAML2Request();
		try {
			if (redirectProfile) {
				is = RedirectBindingUtil.base64DeflateDecode(samlMessage);
			} else {
				byte[] samlBytes = PostBindingUtil.base64Decode(samlMessage);
				is = new ByteArrayInputStream(samlBytes);
			}
		} catch (Exception rte) {
			logger.error("getSAMLDocumentHolder:error:" + rte);
			throw rte;
		}

		saml2Request.getSAML2ObjectFromStream(is);

		return saml2Request.getSamlDocumentHolder();
	}

}
