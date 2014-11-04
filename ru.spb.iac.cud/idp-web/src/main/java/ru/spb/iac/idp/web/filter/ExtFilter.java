package ru.spb.iac.idp.web.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.connector.Request;



 
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.naming.NamingException;
import javax.servlet.http.Cookie;

import org.apache.catalina.Container;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Session;
import org.apache.catalina.Valve;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.authenticator.SavedRequest;
import org.apache.catalina.authenticator.SingleSignOn;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.tomcat.util.buf.ByteChunk;
import org.jboss.as.web.security.SecurityContextAssociationValve;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.util.Base64;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.LogoutRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.RequestAbstractType;
import org.picketlink.identity.federation.saml.v2.protocol.RequestedAuthnContextType;
import org.picketlink.identity.federation.web.util.IDPWebRequestUtil;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spb.iac.cud.context.ContextAccessWebManager;
import ru.spb.iac.cud.core.util.WebUtil;
import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.InvalidCredentials;
import ru.spb.iac.cud.items.AuthMode;
import ru.spb.iac.pl.sp.key.KeyStoreKeyManager;

public class ExtFilter implements Filter {

	final static Logger LOGGER = LoggerFactory.getLogger(ExtFilter.class);

	private static final String main = "/main.jsp";

	private static final String cert_to_form = "/services/access_cert.jsp";
	private static final String cert_to_auth = "WebCertAction";

	private static final String login_to_form = "/AccessServicesWebLogin";
	private static final String login_to_auth = "WebLoginAction";

	private static final String cert_to_form_ie = "/AccessServicesWeb";

	private static final boolean cache = true;

	private static final String auth_type_password = "urn:oasis:names:tc:SAML:2.0:ac:classes:password";
	private static final String auth_type_x509 = "urn:oasis:names:tc:SAML:2.0:ac:classes:X509";

	private static final String info_page = "index.jsp";

	private static final String overauth = "overauth";

	private static final String SAMLMessageKey = "CUD_SAML_MESSAGE";

	private static final String HTTPMethodKey = "CUD_HTTP_METHOD";

	private static final String RequestQueryStringKey = "CUD_REQUEST_QUERY_STRING";

	private static final String RequestRequestURIKey = "CUD_REQUEST_REQUEST_URI";

	private String redirect_url = null;

	private String context_path = null;

	private static final String CIPHER_ALG_TRANSPORT = "GostTransport";

	private static final String CIPHER_ALG = "GOST28147/CFB/NoPadding";

	private static final String loginEncrypt = "loginEncrypt";

	
	public void destroy() {
	}

	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {

		LOGGER.debug("doFilter:01");

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		String requestURI = request.getRequestURI();

		LOGGER.debug("doFilter:02_1:" + requestURI);

		
		if(requestURI.endsWith(loginEncrypt)){
		//������������� �������� � ������������� �������	
		 request.getSession().setAttribute(
				"login_encrypt", "true");
		}
		
		context_path = request.getContextPath();

		LOGGER.debug("doFilter:02_2:" + context_path);

		int returnFlag = 0;
		try {

			setRedirectUrl(request);

			 

		

			Principal principal1 = request.getUserPrincipal();

			LOGGER.debug("doFilter:03:" + (principal1 == null));

			org.apache.catalina.connector.Request request2 = null;

			request2 = SecurityContextAssociationValve.getActiveRequest();

			Principal principal2 = null;

			if (cache) {
				LOGGER.debug("doFilter:04");
				principal2 = request2.getUserPrincipal();
				if (principal2 == null) {
					LOGGER.debug("doFilter:05");
					Session session2 = request2.getSessionInternal(false);
					if (session2 != null) {
						principal2 = session2.getPrincipal();

						if (principal2 != null) {
							LOGGER.debug("doFilter:06:" + session2.getAuthType());

						
							 

							request2.setAuthType(session2.getAuthType());
							request2.setUserPrincipal(principal2);
							/*
							 * }el/se{ //������� ����� ��������������, ���� ���
							 * ���� �������� ������
							 * 
							 * //�����!!! resetSe/ssionData`
							 * 
							 * requ/est.getSessionetAttribute(
							 * "incoming_http_method", requ/est.getMethod
							 * 
							 * //������ ����� ������ ������ principal2 ������
							 * ���� = null //���������� �������� ��������
							 * principal2, �.�. ��� ����������� ������, //�
							 * ����� ������ ������ ��� ��������� �����
							 * ������������� ��������. //���������� principal2 =
							 */
						}

					}

				}

			}
			LOGGER.debug("doFilter:07:" + principal2);

			// ������� �������� - cert.jsp
			// ������� �������� - WebCertAction
			// ������� �������� - /idp - �������� � WebCertAction

			if (resource_detect(requestURI)
					|| requestURI.endsWith("/error.jsp")) {
				LOGGER.debug("doFilter:08");
				return;
			}

			String authenticate = (String) request.getSession().getAttribute(
					"authenticate");

			if (requestURI.endsWith(cert_to_form)
					|| requestURI.endsWith(cert_to_auth)
					|| requestURI.endsWith(cert_to_form_ie)
					|| requestURI.endsWith(login_to_form)
					|| requestURI.endsWith(login_to_auth)) {

				LOGGER.debug("doFilter:08_1");

				if (request.getParameter(SAMLMessageKey) != null
						&& !request.getParameter(SAMLMessageKey).isEmpty()) {

					LOGGER.debug("doFilter:08_1_1");

					request2.getSessionInternal().setNote(
							GeneralConstants.SAML_REQUEST_KEY,
							request.getParameter(SAMLMessageKey));

					request.getSession().setAttribute("incoming_http_method",
							request.getParameter(HTTPMethodKey));

					request.getSession().setAttribute(RequestQueryStringKey,
							request.getParameter(RequestQueryStringKey));

					request.getSession().setAttribute(RequestRequestURIKey,
							request.getParameter(RequestRequestURIKey));

					// 2-� �������:
					// ������� ��������� ������ � ������������������� ������
					/*
					 * resetSessionData
					 * 
					 * //������ ����� ������ ������ principal2 ������ ���� =
					 * null //���������� �������� �������� principal2, �.�. ���
					 * ����������� ������, //� ����� ������ ������ ��� ���������
					 * ����� ������������� ��������. //���������� principal2 =
					 */
				}

				if (principal2 == null
						|| request.getParameter(overauth) != null) {

					LOGGER.debug("doFilter:08_2");
					if (requestURI.endsWith(login_to_form)
							&& request.getSession().getAttribute(
									"password_cud_redirect") == null) {
						// ������������ ��� ������� �� �������� �����/������

						LOGGER.debug("doFilter:08_2_1");

						response.sendRedirect(redirect_url);
						returnFlag = 1;
						return;

					} else {

						if (request.getParameter(overauth) != null) {
							// ������ �� �������� ������ � CUDAbstractIDPValve
							request2.setUserPrincipal(null);
						}

						LOGGER.debug("doFilter:08_3");
					}

				} else {
					LOGGER.debug("doFilter:08_4");

					/*
					 * //1-� �������: //��������� ��������� ������ //������� ��
					 * ��������� ������� ����� � valve
					 */

					// 3-� �������:
					// ��������� ��������� ������
					// � ������� ���������������������
					// ������� ������ �� ������� - ������ ����������
					// Web...Action
					// ���� ����� ������ ��� � ������������������
					if (request.getParameter(SAMLMessageKey) != null
							&& !request.getParameter(SAMLMessageKey).isEmpty()) {
						LOGGER.debug("doFilter:08_6");
						// ������ �� �������� ������ � CUDAbstractIDPValve
						request2.setUserPrincipal(null);
						return;
					}

					if ("false".equals(request.getParameter("success"))) {
						LOGGER.debug("doFilter:08_7");

						// �� ������ ��������������
						// ���������� ����� ����� � ���������� �� ������
						// ������ �� �������� ������ � CUDAbstractIDPValve
						request2.setUserPrincipal(null);

					} else {
						// ����� ������������ ������ ������ �������������� �
						// idp,
						// ��������� ���� � �������������
						// �� ��� ���������� ��������� �������������� �� ������
						// �����/�������
						// ����� ������������ ���������� ����� �� WebCertAction,
						// � ����� �� ���� 2 ���������
						// ���� �� ������� ������ �� WebCertAction
						// ����, ���� �������� ��� ������������ ������ ��
						// �������� ��������� ������
						// � ����� ���������, ���� -
						// �� ��� ���� ���� �������� ����� cert_to_form ��
						// cert_to_auth
						response.sendRedirect(request.getContextPath() + main);
						returnFlag = 1;
						return;

					}
				}

			} else if ((requestURI.endsWith(context_path) || requestURI
					.endsWith(context_path + "/"))
					&& authenticate != null
					&& authenticate.equals("success")) {

				LOGGER.debug("doFilter:09:" + authenticate);

				if (authenticate != null && authenticate.equals("success")) {
					// if(request_getParameter("form_login")!=null) {

					// ����� ���� ������ ������ ��� ������� - � �������������
					// ����� Principal ���������� -
					// ���� �� ������� �������� ������.
				
					LOGGER.debug("doFilter:09_1");

					if (request.getParameter(overauth) != null) {

						LOGGER.debug("doFilter:09_2");

						if (request2.getSessionInternal().getNote("overauth") == null) {

							LOGGER.debug("doFilter:09_3:"
									+ request.getSession().getAttribute(
											"incoming_http_method"
													+ "_overauth"));

							request2.getSessionInternal().setNote(
									GeneralConstants.SAML_REQUEST_KEY,
									request2.getSessionInternal().getNote(
											GeneralConstants.SAML_REQUEST_KEY
													+ "_overauth"));

							request2.getSessionInternal().removeNote(
									GeneralConstants.SAML_REQUEST_KEY
											+ "_overauth");

							request.getSession().setAttribute(
									"incoming_http_method",
									request.getSession().getAttribute(
											"incoming_http_method"
													+ "_overauth"));

							request.getSession().removeAttribute(
									"incoming_http_method" + "_overauth");

						}
						request2.getSessionInternal().removeNote("overauth");

					}

					String loginUser = (String) request.getSession()
							.getAttribute("login_user");

					LOGGER.debug("doFilter:010_+1:" + loginUser);

					authenticate(request, loginUser);

					principal1 = request.getUserPrincipal();

					LOGGER.debug("doFilter:011:" + principal1);

					LOGGER.debug("doFilter:012:" + request.getMethod());

				
					LOGGER.debug("doFilter:013:" + request2.getMethod());

					LOGGER.debug("doFilter:014:"
							+ request.getSession().getAttribute(
									"incoming_http_method"));

				
					// !!!
					// ����� ���������� ����� ������� ����� - ����� �� ��� ���
					// ������ ��������� �� � IDP
					// IDPWebRequestUtil ��������� ��� ��� ����������� SAML
					request2.getCoyoteRequest()
							.method()
							.setString(
									(String) request.getSession().getAttribute(
											"incoming_http_method"));

					LOGGER.debug("doFilter:015:" + request2.getMethod());

					SavedRequest saved = (SavedRequest) request2
							.getSessionInternal(false)
							.getNote(
									"org.apache.catalina.authenticator.REQUEST");

					// saved = null ��� ������ ��������� ������������ ���
					// SAMLRequest -
					// ��� �� �� �� ��������� saved.
					if (saved != null) {
						// method - GET/POST
					
						// ?
					
						// ?
					
						request2.getCoyoteRequest()
								.queryString()
								.setString(
										(String) request.getSession()
												.getAttribute(
														RequestQueryStringKey));
						request2.getCoyoteRequest()
								.requestURI()
								.setString(
										(String) request.getSession()
												.getAttribute(
														RequestRequestURIKey));

						LOGGER.debug("doFilter:016:" + saved.getMethod());
						 
						 

						LOGGER.debug("doFilter:017:"
								+ request.getSession().getAttribute(
										RequestQueryStringKey));
						LOGGER.debug("doFilter:018:"
								+ request.getSession().getAttribute(
										RequestRequestURIKey));
					}

					// !!!
					request.getSession().removeAttribute("authenticate");

					// }
				} else {

				}
			} else if (requestURI.endsWith(info_page)) {
				// ���������� �� ������� ������
				LOGGER.debug("doFilter:018_2");
			} else {
				// ������ ������ �� ������� ������� ������� ��� ������������
				// � �.�. /login

				LOGGER.debug("doFilter:019_002");

				if (request.getParameter(GeneralConstants.SAML_REQUEST_KEY) != null) {
					// ������������� ������ ������ �� ������� ������

					// !!!����� �� ����� ����� ������� - ��������!!!
					// ����� ���������� - ���������� ������� � ������
					// �����������!!!
					// ���� ����� ��������� ��� ������ ��� /login, � �
					// ������ incoming_http_method ������������
				
					if (isLogout(request2)) {
						// /logout
						// ������ �� ������
						LOGGER.debug("doFilter:019_001");

						// getCodeSystem ������ ������������, �.�. �� ��
						// ��������� ����� � ������
						(new ContextAccessWebManager()).sys_audit_public(
								100L,
								"inp_param",
								"true",
								getIPAddress(request),
								null,
								getLoginLogout(request2),
								getCodeSystem(request,
										(String) request.getMethod()));

					} else {
						// /login

						// !!!
						// ����� ���������� - ���������� ������� � ������
						// �����������!!!
						request.getSession().setAttribute(
								"incoming_http_method", request.getMethod());
						saveRequest(request2,
								request2.getSessionInternal(false));

						if (!getForceAuthn(request)) {

						} else {
							// ������� ����� ��������������, ���� ��� ����
							// �������� ������

							LOGGER.debug("doFilter:019_01");

							// �����!!!
							resetSessionData(request, request2);

							request.getSession()
									.setAttribute("incoming_http_method",
											request.getMethod());

							// ������ ����� ������ ������ principal2 ������ ����
							// = null
							// ���������� �������� �������� principal2, �.�. ���
							// ����������� ������,
							// � ����� ������ ������ ��� ��������� �����
							// ������������� ��������.
							// ���������� principal2 = null;
							principal2 = request2.getSessionInternal(false)
									.getPrincipal();
						}

						if (getIsPassive(request)) {
							// ��������� ��������������

							LOGGER.debug("doFilter:019_1");

							if (principal2 == null) {
								// ��� �������� ������
								// ������������� ���� ��� ��������� �
								// CUDAbstractIDPValve

								LOGGER.debug("doFilter:019_2");

								AuthMode am = AuthMode.HTTP_REDIRECT_EXT_AUTH_OPEN;
								
								String loginUser = null;
								String success = "false";
								
								String[] data = getExtPassiveAuthData(request);

								String login = data[0];
								String password = data[1];
								String login_encrypt_key = data[2];
								String password_encrypt_key = data[3];
								String secret_key_key = data[4];
								String initialization_vector_key = data[5];
								String tokenId = data[6];
								
								LOGGER.debug("doFilter:019_2_1:" + login);
								LOGGER.debug("doFilter:019_2_2:" + password);
								LOGGER.debug("doFilter:019_2_3:"
										+ login_encrypt_key);
								LOGGER.debug("doFilter:019_2_4:"
										+ password_encrypt_key);
								LOGGER.debug("doFilter:019_2_5:"
										+ secret_key_key);
								LOGGER.debug("doFilter:019_2_6:"
										+ initialization_vector_key);
								LOGGER.debug("doFilter:019_2_7:"
										+ tokenId);
								
								if ((login != null || (login_encrypt_key != null
										&& secret_key_key != null && initialization_vector_key != null))
										&& (password != null|| (password_encrypt_key != null
												&& secret_key_key != null && initialization_vector_key != null))) {
									// ����� �������� �����/������ ������������

									LOGGER.debug("doFilter:019_3");

									
									try {
										

										if (login_encrypt_key != null
												|| password_encrypt_key != null) {

											LOGGER.debug("doFilter:019_4");

											Cipher cipher = getCipherDecrypt(
													secret_key_key,
													initialization_vector_key);
											am = AuthMode.HTTP_REDIRECT_EXT_AUTH_ENCRYPT;

											if (login_encrypt_key != null) {
												login = getTextDecrypt(
														login_encrypt_key,
														cipher);
											}
											if (password_encrypt_key != null) {
												password = getTextDecrypt(
														password_encrypt_key,
														cipher);
											}
										}

										LOGGER.debug("doFilter:019_5");

										if (password != null && login != null) {
											loginUser = (new ContextAccessWebManager())
													.authenticate_login(
															login,
															password,
															am,
															getIPAddress(request),
															getCodeSystem(
																	request,
																	null));
											success = "true";
										}

									} catch (InvalidCredentials e1) {
										LOGGER.error("doFilter:019_3:error:"
												+ e1.getMessage());
									} catch (GeneralFailure e2) {
										LOGGER.error("doFilter:019_4:error:"
												+ e2.getMessage());
									} catch (Exception e3) {
										LOGGER.error("doFilter:019_5:error:"
												+ e3.getMessage());
									}

									if (success.equals("true")) {

										// !!!
										// �� ������ ������ �� ������� �������
										// ������� - ���������� ��������������
										// ?!
										// ?! 
									
										// !!!
										// �� WebLoginAction
										request.getSession()
												.setAttribute("cud_auth_type",
														"urn:oasis:names:tc:SAML:2.0:ac:classes:password");
										request.getSession().setAttribute(
												"authenticate", "success");
										request.getSession().setAttribute(
												"login_user", loginUser);
										response.sendRedirect(request
												.getContextPath() + "/");

									} else {
										request.getSession().setAttribute(
												"is_passive_failed", "true");
									}

								} else if(tokenId!=null){
									
									String[] userAuthInfo = getUserAuthInfo(tokenId);
									
									if(userAuthInfo!=null && userAuthInfo.length==2) {
									
										if(userAuthInfo[0]!=null) {
									
											try {
											
									          loginUser = (new ContextAccessWebManager())
											     .authenticate_uid_obo(userAuthInfo[0],
													am, getIPAddress(request),
													getCodeSystem(
															request,
															null));
									
									           success = "true";
									           
											} catch (GeneralFailure e2) {
												LOGGER.error("doFilter:019_4_2:error:"
														+ e2.getMessage());
											} catch (Exception e3) {
												LOGGER.error("doFilter:019_5_2:error:"
														+ e3.getMessage());
											}
									  }
									}
									
									if (success.equals("true")) {

										request.getSession()
												.setAttribute("cud_auth_type",
														auth_type_x509.equals(userAuthInfo[1])?
														auth_type_x509:auth_type_password);
										
										request.getSession().setAttribute(
												"authenticate", "success");
										request.getSession().setAttribute(
												"login_user", loginUser);
										response.sendRedirect(request
												.getContextPath() + "/");

									} else {
										request.getSession().setAttribute(
												"is_passive_failed", "true");
									}

									
									
								}else{

									request.getSession().setAttribute(
											"is_passive_failed", "true");

								}

							} else {
								// ������������ ��� ����������������
							}

						} else {
							// ���������� ��������������

							LOGGER.debug("doFilter:019_3");

							if (principal2 == null) {

								// �������� �� �������� ������

								// ������������ - ���� �� ����������
								
								LOGGER.debug("doFilter:020:"
										+ request.getMethod());

								// ?!
								// ?! 
								
								String auth_type = getAuthType(request);

								LOGGER.debug("doFilter:021:auth_type:"
										+ auth_type);

								// ���������� � ��������������� �������� -
								// WebCertAction � WebLoginAction
								// ����� �������� ��������������
								
								if (auth_type_x509.equals(auth_type)) {

									response.sendRedirect(redirect_url);
								
								} else if (auth_type_password.equals(auth_type)) {

									// ��������, ��� ��� ��� ������������ ��
									// �������� �����/������
									request.getSession().setAttribute(
											"password_cud_redirect", true);
									response.sendRedirect(request
											.getContextPath() + login_to_form);

								} else {
									// !!!
									// ������-�� ���� �� ������
									// ������ ����� ���� ����� ������
									// ������������ � ���������
									// � SAMLRequest � ���, ������ �� �����
									// ������ � SAMLRequest
									// � � ���� ������ ��-��������� auth_type =
									// auth_type_password

									// ������������ ������ �� �� �������,
									// � �������� ���������� � ���
									// ������� ����� ������������������,
									// � ����� ���������� � ��������
									// ������������� ��� ����� �������������� ��
									// ��������� -
									// -����������

									response.sendRedirect(redirect_url);

									}

							} else {
								// ������ �� ����������� ����� ������������ ���
								// ����������������

								// ��������� ��� ��������������
								String reqAuthType = getAuthType(request);

								LOGGER.debug("doFilter:022:req_auth_type:"
										+ reqAuthType);

								// ��������� ��� ��������������
								String mainAuthType = (String) request
										.getSession().getAttribute(
												"cud_auth_type");

								LOGGER.debug("doFilter:023:mainAuthType:"
										+ mainAuthType);

								if (auth_type_x509.equals(reqAuthType)
										&& auth_type_password
												.equals(mainAuthType)) {

									// ����� �� ��� ������� ����������,
									// � � ��� ���� �����/������

									LOGGER.debug("doFilter:024");
									// ���������� � ��������������� �������� -
									// WebCertAction � WebLoginAction
									// ����� �������� ��������������
								
									// �����!!!
								
									// ������ ��������� 2-� ���������
									// ��������������
									// ��� ������� ������ � ��������������� ��
									// ������
									// ��� ������ �������� � ������� �����������
									// ������ �� ������ �����������
									// � ����������� ���� ��� ��������
									// �������������� �� �����������
									// !���� ��� �������� ������, �����
									// ������������ �� ����������� ��
									// ���������������

									// ������ �� �������� ������ �
									// CUDAbstractIDPValve
									request2.setUserPrincipal(null);

									request2.getSessionInternal().setNote(
											"overauth", "1");

									response.sendRedirect(redirect_url
											+ "?overauth");

								} else {

									LOGGER.debug("doFilter:024_1");

									// ��������� � ��� � ��� �������������������
									// �������������
									// 1) �������: ���� ���� ������ �� ������, �
									// ��������� �� ������
									// 2) �������:���� ���� ������ ��
									// �����������, � ��������� �� ������
									// 3) �������:���� ���� ������ ��
									// �����������, � ��������� �� �����������
								}
							}

						}

					}

				} else {
					// ��������� �������� � IDP ��� SAMLRequest

					LOGGER.debug("doFilter:024_1_1");

					if (principal2 == null) {

						// �������� �� �������� ������

						LOGGER.debug("doFilter:024_1_2");

						// ������������ ������ �� �� �������,
						// � �������� ���������� � ���
						// ������� ����� ������������������,
						// � ����� ���������� � ��������
						// ������������� ��� ����� �������������� �� ��������� -
						// -����������

						response.sendRedirect(redirect_url);

					
					} else {
						// ������ �� ����������� ����� ������������ ���
						// ����������������

						LOGGER.debug("doFilter:024_1_3");

						// ��������� � ��� � ��� �������������������
						// �������������
						// 4) ������ ��������� : ��� ���� ��������������
						// [hosted]
					}

				}

			}

		} catch (Exception e) {
			LOGGER.error("doFilter:025:error:", e);
		} finally {
			LOGGER.debug("doFilter:026");
			if (returnFlag == 0) {
				filterChain.doFilter(servletRequest, servletResponse);
			}
		}
	}

	private void authenticate(
			HttpServletRequest servletRequest,
			String loginUser) {
		LOGGER.debug("authenticate:027");
		try {

			Principal principal = null;
			String credential = "9753560";
			org.apache.catalina.connector.Request request2 = null;

			request2 = SecurityContextAssociationValve.getActiveRequest();
			if (request2 == null)
				throw new IllegalStateException("request is null");

			 
			 
			 

			LOGGER.debug("authenticate:028");

		
			principal = new GenericPrincipal(null, loginUser, credential);

			register(request2, principal, loginUser, credential,
					servletRequest);

			LOGGER.debug("authenticate:029");

		} catch (Exception e) {
			LOGGER.error("authenticate:030:error:", e);
		}
	}

	private String getCodeSystem(HttpServletRequest request,
			String incomingHttpMethod) {

		// !!!
		// incomingHttpMethod ��������� ��� logout
		// !!!
		// ��� �����, ��� ��� logout,
		// getSamlObject ����� ��� LogoutRequestType,
		// � �� AuthnRequestType

		LOGGER.debug("getCodeSystem:031");
		String result = null;

		try {
			org.apache.catalina.connector.Request request2 = null;
			request2 = SecurityContextAssociationValve.getActiveRequest();
			Session session = request2.getSessionInternal(false);

			String samlRequestMessage = (String) session.getNote("SAMLRequest");

			if (samlRequestMessage != null) {

			
				// !!!
				boolean beginReqMethod = "GET"
						.equals(incomingHttpMethod != null ? incomingHttpMethod
								: (String) request.getSession().getAttribute(
										"incoming_http_method"));

				SAMLDocumentHolder samlDocumentHolder = getSAMLDocumentHolder(
						samlRequestMessage, beginReqMethod);

				if (samlDocumentHolder != null) {

					if (samlDocumentHolder.getSamlObject() != null) {

						if (samlDocumentHolder.getSamlObject() instanceof AuthnRequestType == true) {

							AuthnRequestType requestAbstractType = (AuthnRequestType) samlDocumentHolder
									.getSamlObject();
							result = requestAbstractType.getIssuer().getValue();

						} else if (samlDocumentHolder.getSamlObject() instanceof LogoutRequestType == true) {

							LogoutRequestType requestAbstractType = (LogoutRequestType) samlDocumentHolder
									.getSamlObject();
							result = requestAbstractType.getIssuer().getValue();

						}

						LOGGER.debug("getCodeSystem:032:" + result);

					}
				}

			}

		} catch (Exception e) {
			LOGGER.error("getCodeSystem:error:", e);
		}

		return result;
	}

	private boolean isLogout(Request request) {
		boolean result = false;

		try {
			LOGGER.debug("isLogout:01");

			String samlRequestMessage = (String) request.getSessionInternal()
					.getNote(GeneralConstants.SAML_REQUEST_KEY);

			// !!!
			// ��� ������� �� � ������ ��������� ��� ��� incoming_http_method -
			// �������� ������
			boolean beginReqMethod = "GET".equals((String) request
					.getMethod());

			SAMLDocumentHolder samlDocumentHolder = getSAMLDocumentHolder(
					samlRequestMessage, beginReqMethod);
			if (samlDocumentHolder != null) {
				LOGGER.debug("isLogout:02");

				if (samlDocumentHolder.getSamlObject() != null) {
				
					if (samlDocumentHolder.getSamlObject() instanceof LogoutRequestType == true) {
						result = true;
					}
				}
			}
			LOGGER.debug("isLogout:03:" + result);

		} catch (Exception e) {
			LOGGER.error("isLogout:error:", e);
		}

		return result;
	}

	private String getLoginLogout(Request request) {

		String result = null;

		try {
			LOGGER.debug("getLoginLogout:01");

			String samlRequestMessage = (String) request.getSessionInternal()
					.getNote(GeneralConstants.SAML_REQUEST_KEY);

			// !!!
			// ��� ������� �� � ������ ��������� ��� ��� incoming_http_method -
			// �������� ������
			boolean beginReqMethod = "GET".equals((String) request
					.getMethod());

			SAMLDocumentHolder samlDocumentHolder = getSAMLDocumentHolder(
					samlRequestMessage, beginReqMethod);
			if (samlDocumentHolder != null) {
				LOGGER.debug("getLoginLogout:02:"
						+ (samlDocumentHolder.getSamlObject() == null));

				if (samlDocumentHolder.getSamlObject() != null) {
				
					if (samlDocumentHolder.getSamlObject() instanceof LogoutRequestType == true) {

						LogoutRequestType lrt = (LogoutRequestType) samlDocumentHolder
								.getSamlObject();

						result = lrt.getNameID().getValue();
					}
				}
			}
			LOGGER.debug("getLoginLogout:03:" + result);

		} catch (Exception e) {
			LOGGER.error("getLoginLogout:error:", e);
		}

		return result;
	}

	private String getAuthType(HttpServletRequest request) {

		LOGGER.debug("getAuthType:041");
		List<String> classes_list = null;

		// !!!
		// ����� ������ ������ �� idp ��� SAMLRequest, �� result ������ =null
		// ����� ������ �� �� (� SAMLRequest), �� ������ �� ���������
		// �������������� �� ������
		String result = null;

		try {
			org.apache.catalina.connector.Request request2 = null;
			request2 = SecurityContextAssociationValve.getActiveRequest();
			Session session = request2.getSessionInternal(false);

			String samlRequestMessage = (String) session.getNote("SAMLRequest");

			if (samlRequestMessage != null) {

				// !!! ����� �� ��������� �������������� �� ������
				result = auth_type_password;

			
				boolean beginReqMethod = "GET".equals((String) request
						.getSession().getAttribute("incoming_http_method"));

				SAMLDocumentHolder samlDocumentHolder = getSAMLDocumentHolder(
						samlRequestMessage, beginReqMethod);

				LOGGER.debug("getAuthType:045:" + (samlDocumentHolder == null));

				if (samlDocumentHolder != null) {
					LOGGER.debug("getAuthType:046:"
							+ (samlDocumentHolder.getSamlObject() == null));

					if (samlDocumentHolder.getSamlObject() != null) {
							AuthnRequestType requestAbstractType = (AuthnRequestType) samlDocumentHolder
								.getSamlObject();
						
					
						RequestedAuthnContextType ract = requestAbstractType
								.getRequestedAuthnContext();
						LOGGER.debug("getAuthType:047:" + ract);

						if (ract != null) {
							classes_list = ract.getAuthnContextClassRef();

							LOGGER.debug("getAuthType:048:" + classes_list);

							if (classes_list != null) {
								LOGGER.debug("getAuthType:049:"
										+ classes_list.size());

								if (classes_list.size() > 0) {
									result = classes_list.get(0);
									LOGGER.debug("getAuthType:050:" + result);
								}
							}
						}
					}
				}

			}

			LOGGER.debug("getAuthType:051");
		} catch (Exception e) {
			LOGGER.error("getAuthType:052:error:", e);
			// !!!
			result = null;
		}

		return result;
	}

	private boolean getForceAuthn(HttpServletRequest request) {

		LOGGER.debug("getForceAuthn:01");

		// !!!
		// ����������� ��� Boolean
		// � AuthnRequestType ������������ Boolean
		// � ���� � ������� ��� ���������, �� �� = null
		Boolean result = false;
		try {
			org.apache.catalina.connector.Request request2 = null;
			request2 = SecurityContextAssociationValve.getActiveRequest();
			Session session = request2.getSessionInternal(false);

			String samlRequestMessage = (String) session.getNote("SAMLRequest");

			 

			if (samlRequestMessage != null) {

			
				boolean beginReqMethod = "GET".equals((String) request
						.getSession().getAttribute("incoming_http_method"));

				SAMLDocumentHolder samlDocumentHolder = getSAMLDocumentHolder(
						samlRequestMessage, beginReqMethod);

				if (samlDocumentHolder != null) {

					if (samlDocumentHolder.getSamlObject() != null) {
						LOGGER.debug("getForceAuthn:02");

						AuthnRequestType requestAbstractType = (AuthnRequestType) samlDocumentHolder
								.getSamlObject();

						LOGGER.debug("getForceAuthn:03");

						

						LOGGER.debug("getForceAuthn:04:"
								+ (requestAbstractType == null));

						result = requestAbstractType.isForceAuthn();

						LOGGER.debug("getForceAuthn:02:" + result);

					}
				}

			}

		} catch (Exception e) {
			LOGGER.error("getForceAuthn:error:", e);
		}

		return result != null ? result : false;
	}

	private boolean getIsPassive(HttpServletRequest request) {

		LOGGER.debug("getIsPassive:01");

		// !!!
		// ����������� ��� Boolean
		// � AuthnRequestType ������������ Boolean
		// � ���� � ������� ��� ���������, �� �� = null
		Boolean result = false;
		try {
			org.apache.catalina.connector.Request request2 = null;
			request2 = SecurityContextAssociationValve.getActiveRequest();
			Session session = request2.getSessionInternal(false);

			String samlRequestMessage = (String) session.getNote("SAMLRequest");

			if (samlRequestMessage != null) {

			
				boolean beginReqMethod = "GET".equals((String) request
						.getSession().getAttribute("incoming_http_method"));

				SAMLDocumentHolder samlDocumentHolder = getSAMLDocumentHolder(
						samlRequestMessage, beginReqMethod);

				if (samlDocumentHolder != null) {

					if (samlDocumentHolder.getSamlObject() != null) {
						AuthnRequestType requestAbstractType = (AuthnRequestType) samlDocumentHolder
								.getSamlObject();
						

						result = requestAbstractType.isIsPassive();
						LOGGER.debug("getIsPassive:02:" + result);

					}
				}

			}

		} catch (Exception e) {
			LOGGER.error("getAIsPassive:error:", e);
		}

		return result != null ? result : false;
	}

	private String[] getExtPassiveAuthData(HttpServletRequest request) {

		LOGGER.debug("getExtPassiveAuthData:01");

		// !!!
		// ����������� ��� Boolean
		// � AuthnRequestType ������������ Boolean
		// � ���� � ������� ��� ���������, �� �� = null
		String[] result = new String[7];
		URI result_uri = null;
		try {
			org.apache.catalina.connector.Request request2 = null;
			request2 = SecurityContextAssociationValve.getActiveRequest();
			Session session = request2.getSessionInternal(false);

			String samlRequestMessage = (String) session.getNote("SAMLRequest");

			if (samlRequestMessage != null) {

				boolean beginReqMethod = "GET".equals((String) request
						.getSession().getAttribute("incoming_http_method"));

				SAMLDocumentHolder samlDocumentHolder = getSAMLDocumentHolder(
						samlRequestMessage, beginReqMethod);

				if (samlDocumentHolder != null) {

					if (samlDocumentHolder.getSamlObject() != null) {
						AuthnRequestType requestAbstractType = (AuthnRequestType) samlDocumentHolder
								.getSamlObject();
						
						result_uri = requestAbstractType.getDestination();

					}
				}

			}

			if (result_uri != null) {

				String result_st = result_uri.toString();

				LOGGER.debug("getExtPassiveAuthData:02:" + result_st);

				result[0] = WebUtil.getTokenValue(result_st, "login");
				result[1] = WebUtil.getTokenValue(result_st, "password");
				result[2] = WebUtil.getTokenValue(result_st, "elogin");
				result[3] = WebUtil.getTokenValue(result_st, "epassword");
				result[4] = WebUtil.getTokenValue(result_st, "skey");
				result[5] = WebUtil.getTokenValue(result_st, "ivector");

				result[6] = WebUtil.getTokenValue(result_st, "tokenId");
				
				result[0] = result[0] != null ? URLDecoder.decode(result[0],
						"UTF-8") : null;
				result[1] = result[1] != null ? URLDecoder.decode(result[1],
						"UTF-8") : null;
				result[2] = result[2] != null ? URLDecoder.decode(result[2],
						"UTF-8") : null;
				result[3] = result[3] != null ? URLDecoder.decode(result[3],
						"UTF-8") : null;
				result[4] = result[4] != null ? URLDecoder.decode(result[4],
						"UTF-8") : null;
				result[5] = result[5] != null ? URLDecoder.decode(result[5],
						"UTF-8") : null;
				result[6] = result[6] != null ? URLDecoder.decode(result[6],
						"UTF-8") : null;
			}

		} catch (Exception e) {
			LOGGER.error("getExtPassiveAuthData:error:", e);
		}

		return result;
	}

	private void resetSessionData(HttpServletRequest request,
			org.apache.catalina.connector.Request request2) throws Exception {

		LOGGER.debug("resetSessionData:01");

		try {

			// ������� ���� ��������� ������������� ����������
			request.getSession().removeAttribute("password_cud_redirect");
			request.getSession().removeAttribute("authenticate");
			request.getSession().removeAttribute("login_user");
			request.getSession().removeAttribute("tokenID");

			Session session2 = request2.getSessionInternal(false);
			session2.setPrincipal(null);
			session2.setAuthType(null);

			request2.setUserPrincipal(null);
			request2.setAuthType(null);

		} catch (Exception e) {
			LOGGER.error("resetSessionData:error:", e);
			throw e;
		}

	}

	/**
	 * Register the principal with the request, session etc just the way
	 * AuthenticatorBase does
	 * 
	 * @param request
	 *            Catalina Request
	 * @param principal
	 *            User Principal generated via authentication
	 * @param username
	 *            username passed by the user (null for client-cert)
	 * @param credential
	 *            Password (null for client-cert and digest)
	 */
	protected void register(Request request, Principal principal,
			String username, Object password, HttpServletRequest servletRequest) {
		LOGGER.debug("register:01");

		String cud_auth_type = (String) servletRequest.getSession()
				.getAttribute("cud_auth_type");

		LOGGER.debug("register:01+:" + cud_auth_type);

		request.setAuthType(cud_auth_type);
		request.setUserPrincipal(principal);

		// Cache the authentication principal in the session
		Session session = request.getSessionInternal(false);

		LOGGER.debug("register:01_1:" + session.getId());
		LOGGER.debug("register:01_2:" + session.getAuthType());
		LOGGER.debug("register:01_3:" + session.getSession().getId());
		LOGGER.debug("register:01_4:" + (session.getPrincipal() == null));

		if (session.getPrincipal() != null) {
			LOGGER.debug("register:01_4+:" + session.getPrincipal().getName());
		}

		
		if (session != null) {

			LOGGER.debug("register:02");

			session.setAuthType(cud_auth_type);
			session.setPrincipal(principal);
			session.setNote("org.apache.catalina.authenticator.PRINCIPAL",
					principal);

			if (username != null) {
				LOGGER.debug("register:03");
			} else {
				LOGGER.debug("register:04");
				session.removeNote(Constants.SESS_USERNAME_NOTE);
			}
			if (password != null) {
				LOGGER.debug("register:05");
			} else {
				LOGGER.debug("register:06");
				session.removeNote(Constants.SESS_PASSWORD_NOTE);
			}
		}

		LOGGER.debug("register:07");
	}

	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

	protected void saveRequest(org.apache.catalina.connector.Request request,
			Session session) throws IOException {
		LOGGER.debug("saveRequest:01");

		SavedRequest saved = new SavedRequest();
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++)
				saved.addCookie(cookies[i]);
		}
		Enumeration names = request.getHeaderNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			Enumeration values = request.getHeaders(name);
			while (values.hasMoreElements()) {
				String value = (String) values.nextElement();
				saved.addHeader(name, value);
			}
		}
		Enumeration locales = request.getLocales();
		while (locales.hasMoreElements()) {
			Locale locale = (Locale) locales.nextElement();
			saved.addLocale(locale);
		}

		if ("POST".equalsIgnoreCase(request.getMethod())) {
			ByteChunk body = new ByteChunk();
			body.setLimit(request.getConnector().getMaxSavePostSize());

			byte[] buffer = new byte[4096];

			InputStream is = request.getInputStream();
			int bytesRead;
			while ((bytesRead = is.read(buffer)) >= 0) {
				body.append(buffer, 0, bytesRead);
			}
			saved.setContentType(request.getContentType());
			saved.setBody(body);

			if (body.getLength() == 0) {
				Enumeration e = request.getParameterNames();
				while (e.hasMoreElements()) {
					String name = (String) e.nextElement();
					String[] val = request.getParameterValues(name);
					saved.addParameter(name, val);
				}
			}
		}

		saved.setMethod(request.getMethod());
		saved.setQueryString(request.getQueryString());
		saved.setRequestURI(request.getRequestURI());

		session.setNote("org.apache.catalina.authenticator.REQUEST", saved);

		request.getSession().setAttribute(RequestQueryStringKey,
				request.getQueryString());
		request.getSession().setAttribute(RequestRequestURIKey,
				request.getRequestURI());

		 
		 
		 
	}

	private boolean resource_detect(String requestURI) throws ServletException {

		boolean result = false;
		try {
			if (requestURI.endsWith(".js") || requestURI.endsWith(".css")
					|| requestURI.endsWith(".png")
					|| requestURI.endsWith(".img")) {
				result = true;
			}

		} catch (Exception e) {
			LOGGER.error("resource_detect:error:", e);
		}
		return result;
	}

	private void setRedirectUrl(HttpServletRequest request)
			throws ServletException {

		try {
			String userAgent = request.getHeader("user-agent");
			LOGGER.debug("doFilter:userAgent:" + userAgent);

			redirect_url = request.getScheme() + "://"
					+ request.getServerName() + ":" + request.getServerPort()
					+ request.getContextPath() + cert_to_form;

			LOGGER.debug("doFilter:redirect_url:" + redirect_url);

		} catch (Exception e) {
			LOGGER.error("resource_detect:error:", e);
		}
	}

	private String getIPAddress(HttpServletRequest request) {

		String ipAddress = request.getRemoteAddr();

		return ipAddress;
	}

	public SAMLDocumentHolder getSAMLDocumentHolder(String samlMessage,
			boolean redirectProfile) throws Exception {
		LOGGER.debug("getSAMLDocumentHolder:01:" + redirectProfile);

		InputStream is = null;
		SAML2Request saml2Request = new SAML2Request();
		try {
			if (redirectProfile) {
				LOGGER.debug("getSAMLDocumentHolder:02");

				is = RedirectBindingUtil.base64DeflateDecode(samlMessage);
				LOGGER.debug("getSAMLDocumentHolder:03");

			} else {
				byte[] samlBytes = PostBindingUtil.base64Decode(samlMessage);
				is = new ByteArrayInputStream(samlBytes);
			}
		} catch (Exception rte) {
			LOGGER.error("getSAMLDocumentHolder:error:" + rte);
			throw rte;
		}

		LOGGER.debug("getSAMLDocumentHolder:04");

		saml2Request.getSAML2ObjectFromStream(is);

		LOGGER.debug("getSAMLDocumentHolder:05");

		return saml2Request.getSamlDocumentHolder();
	}

	private Cipher getCipherDecrypt(String secretKeyBase64, String ivBase64) {

		Cipher result = null;
		try {

			// ����������
			
			LOGGER.debug("getCipherDecrypt:01:" + secretKeyBase64);
			LOGGER.debug("getCipherDecrypt:02:" + ivBase64);

			final byte[] wrappedSecretKey = Base64.decode(secretKeyBase64);
			final byte[] iv = Base64.decode(ivBase64);

			// �����������

			KeyStoreKeyManager kskm = new KeyStoreKeyManager();
			// � KeyStoreKeyManager KeyStore ks - static
			// ������� ks ��� ��������������� ������� �����������
			// � ����� �����, ��� static:
			
			KeyPair keyPair = kskm.getSigningKeyPair();
			
			PrivateKey responderPrivateKey = keyPair.getPrivate();

			LOGGER.debug("getCipherDecrypt:03:" + responderPrivateKey);

			Cipher unwrapCipher = Cipher.getInstance(CIPHER_ALG_TRANSPORT);
			unwrapCipher.init(Cipher.UNWRAP_MODE, responderPrivateKey);
			SecretKey clientSecretKey = (SecretKey) unwrapCipher.unwrap(
					wrappedSecretKey, null, Cipher.SECRET_KEY);

			Cipher cipher = Cipher.getInstance(CIPHER_ALG);
			cipher.init(Cipher.DECRYPT_MODE, clientSecretKey,
					new IvParameterSpec(iv), null);

			result = cipher;

			LOGGER.debug("getCipherDecrypt:04");

		} catch (Exception e) {
			LOGGER.error("getCipherDecrypt:error:", e);
		}
		return result;
	}

	private String getTextDecrypt(String textEncryptBase64, Cipher cipher) {

		String result = null;
		try {

			// ����������
			
			LOGGER.debug("getTextDecrypt:01:" + textEncryptBase64);

			byte[] encryptedtext = Base64.decode(textEncryptBase64);

			byte[] decryptedtext = cipher.doFinal(encryptedtext, 0,
					encryptedtext.length);

			result = new String(decryptedtext, "utf-8");

			LOGGER.debug("getText:02:" + result);

		} catch (Exception e) {
			LOGGER.error("getText:error:", e);
		}
		return result;
	}

	private String[] getUserAuthInfo(String base64TokenId){
		
		String[] result = new String[2];
		
		try{
			
				 

				byte[] byteTokenID = Base64.decode(base64TokenId);

				String tokenID = new String(byteTokenID, "UTF-8");

				 

				String[] arrTokenID = tokenID.split("_");

				if (arrTokenID == null || arrTokenID.length != 4) {
					return null;
				}

                
				result[0] = arrTokenID[0].toString();
				
				LOGGER.debug("getUserAuthInfo:userId" + result[0]);

				/*
				Date expired = new Date(Long.parseLong(arrTokenID[1]));
            	LOGGER.debug("handleMessage:" + expired);

				if (!tokenIDValidateResult) {
					throw new Exception(
							"TokenId of OnBehalfOf is not valid!!!");
				}

				//�� ����� ���� ������� ������
				//���� ����� ���������, �� ����� �� �� ��� ������ �������� 
				//������ �����?
				//���� ����������� ������� ������ �� ����� � ������
				// � ���� ��� �������� ����������, ��� ����� ��������� -
				//�� ��� � ������?
				//��� ��� ������ ���� ������� �������� �� ���� ��� 
				//��������� ������ �� 1 �����
				if (new Date(System.currentTimeMillis()).after(expired)) {
					//throw new TokenExpired("TokenId of OnBehalfOf is expired!!!");
					//throw new Exception(
					//		"TokenId of OnBehalfOf is expired!!!");
				}
                */
				result[1] =  arrTokenID[2].toString();
				 
				LOGGER.debug("getUserAuthInfo:authType" + result[1]);
				
		}catch(Exception e){
			LOGGER.debug("getUserAuthInfo:userId:error:", e);
		}
		
		return result;
	}
}
