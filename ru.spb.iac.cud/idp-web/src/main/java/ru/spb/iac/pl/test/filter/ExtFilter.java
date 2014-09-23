package ru.spb.iac.pl.test.filter;

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

//import org.jboss.web.tomcat.security.login.WebAuthentication;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
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

	final static Logger logger = LoggerFactory.getLogger(ExtFilter.class);

	private static final String AUTH_TYPE = "PROGRAMMATIC_WEB_LOGIN";
	// private static final String AUTH_TYPE = "FORM";

	private static final String main = "/main.jsp";

	private static final String default_to_form = "/services/access_cert.jsp";

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

	public void destroy() {
	}

	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {

		logger.info("doFilter:01");

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		String requestURI = request.getRequestURI();

		logger.info("doFilter:02_1:" + requestURI);

		context_path = request.getContextPath();

		logger.info("doFilter:02_2:" + context_path);

		int return_flag = 0;
		try {

			setRedirectUrl(request);

			// logger.info("doFilter:01_2_2:"+request.getSession().getId());

			/*
			 * logger.info("doFilter:01_1:"+request.getLocalAddr());
			 * logger.info("doFilter:01_2:"+request.getContextPath());
			 * logger.info("doFilter:01_3:"+request.getLocalName());
			 * logger.info("doFilter:01_4:"+request.getPathInfo());
			 * logger.info("doFilter:01_5:"+request.getRemoteAddr());
			 * logger.info("doFilter:01_6:"+request.getRemoteHost());
			 * logger.info("doFilter:01_7:"+request.getRequestURI());
			 * logger.info("doFilter:01_8:"+request.getServerName());
			 */

			String samlRequest = request.getParameter("SAMLRequest");

			Principal principal1 = request.getUserPrincipal();

			logger.info("doFilter:03:" + (principal1 == null));

			org.apache.catalina.connector.Request request2 = null;

			request2 = SecurityContextAssociationValve.getActiveRequest();

			Principal principal2 = null;

			if (cache) {
				logger.info("doFilter:04");
				principal2 = request2.getUserPrincipal();
				if (principal2 == null) {
					logger.info("doFilter:05");
					Session session2 = request2.getSessionInternal(false);
					if (session2 != null) {
						principal2 = session2.getPrincipal();

						if (principal2 != null) {
							logger.info("doFilter:06:" + session2.getAuthType());

							// if(!getForceAuthn(request)){

							// logger.info("doFilter:06_2");

							request2.setAuthType(session2.getAuthType());
							request2.setUserPrincipal(principal2);
							/*
							 * }else{ //требуют новую аутентификацию, хотя уже
							 * есть активная сессия
							 * 
							 * //важно!!! resetSessionData(request, request2);
							 * 
							 * request.getSession().setAttribute(
							 * "incoming_http_method", request.getMethod());
							 * 
							 * //теперь после сброса сессии principal2 должен
							 * быть = null //необходимо обновить значение
							 * principal2, т.к. оно исользается дальше, //а
							 * после сброса сессии оно сохранило ранее
							 * установленное значение. //эквивалент principal2 =
							 * null; principal2 = session2.getPrincipal(); }
							 */
						}

					}

				}

			}
			logger.info("doFilter:07:" + principal2);

			// текущая страница - cert.jsp
			// текущая страница - WebCertAction
			// текущая страница - /idp - редирект с WebCertAction

			if (resource_detect(requestURI)
					|| requestURI.endsWith("/error.jsp")) {
				logger.info("doFilter:08");
				return;
			}

			String authenticate = (String) request.getSession().getAttribute(
					"authenticate");

			if (requestURI.endsWith(cert_to_form)
					|| requestURI.endsWith(cert_to_auth)
					|| requestURI.endsWith(cert_to_form_ie)
					|| requestURI.endsWith(login_to_form)
					|| requestURI.endsWith(login_to_auth)) {

				logger.info("doFilter:08_1");

				if (request.getParameter(SAMLMessageKey) != null
						&& !request.getParameter(SAMLMessageKey).isEmpty()) {

					logger.info("doFilter:08_1_1");

					request2.getSessionInternal().setNote(
							GeneralConstants.SAML_REQUEST_KEY,
							request.getParameter(SAMLMessageKey));

					request.getSession().setAttribute("incoming_http_method",
							request.getParameter(HTTPMethodKey));

					request.getSession().setAttribute(RequestQueryStringKey,
							request.getParameter(RequestQueryStringKey));

					request.getSession().setAttribute(RequestRequestURIKey,
							request.getParameter(RequestRequestURIKey));

					// 2-й вариант:
					// сбиваем имеющуюся сессию и переаутентифицируем заново
					/*
					 * resetSessionData(request, request2);
					 * 
					 * //теперь после сброса сессии principal2 должен быть =
					 * null //необходимо обновить значение principal2, т.к. оно
					 * исользается дальше, //а после сброса сессии оно сохранило
					 * ранее установленное значение. //эквивалент principal2 =
					 * null; principal2 =
					 * request2.getSessionInternal(false).getPrincipal();
					 */
				}

				if (principal2 == null
						|| request.getParameter(overauth) != null) {

					logger.info("doFilter:08_2");
					if (requestURI.endsWith(login_to_form)
							&& request.getSession().getAttribute(
									"password_cud_redirect") == null) {
						// пользователь сам перешёл на страницу логин/пароля

						logger.info("doFilter:08_2_1");

						response.sendRedirect(redirect_url);
						return_flag = 1;
						return;

						// response.sendRedirect(request.getContextPath()+default_to_form);
					} else {

						if (request.getParameter(overauth) != null) {
							// защита от отправки ответа в CUDAbstractIDPValve
							request2.setUserPrincipal(null);
						}

						logger.info("doFilter:08_3");
					}

				} else {
					logger.info("doFilter:08_4");

					/*
					 * //1-й вариант: //сохраняем имеющуюся сессию //выходим из
					 * обработки запроса прямо в valve
					 * if(request.getParameter(SAMLMessageKey
					 * )!=null&&!request.getParameter
					 * (SAMLMessageKey).isEmpty()){
					 * logger.info("doFilter:08_5"); return_flag=1; return; }
					 */

					// 3-й вариант:
					// сохраняем имеющуюся сессию
					// и пробуем переаутентифицировать
					// выходим только из фильтра - дальше вызывается
					// Web...Action
					// Этот метод связан ещё с закомментированием
					// if(principal2==null ||
					// request.getParameter(overauth)!=null){
					if (request.getParameter(SAMLMessageKey) != null
							&& !request.getParameter(SAMLMessageKey).isEmpty()) {
						logger.info("doFilter:08_6");
						// защита от отправки ответа в CUDAbstractIDPValve
						request2.setUserPrincipal(null);
						return;
					}

					if ("false".equals(request.getParameter("success"))) {
						logger.info("doFilter:08_7");

						// не прошла аутентификация
						// отображаем форму снова с сообщением об ошибке
						// защита от отправки ответа в CUDAbstractIDPValve
						request2.setUserPrincipal(null);

					} else {
						// когда пользователь решает начать аутентификацию с
						// idp,
						// открывает окно с сертификатами
						// но его перебивает пассивная аутентификация со своими
						// логин/паролем
						// потом пользователь отправляет форму на WebCertAction,
						// и чтобы не было 2 редиректа
						// надо не пустить запрос на WebCertAction
						// хотя, надо признать его отправленные данные не
						// подменят имеющуюся сессию
						// а чтобы подменили, надо -
						// request2.setUserPrincipal(null); return;
						// но при этом надо отделять ветки cert_to_form от
						// cert_to_auth
						response.sendRedirect(request.getContextPath() + main);
						return_flag = 1;
						return;

					}
				}

				// }else if (requestURI.endsWith("/AccessServlet")){
			} else if ((requestURI.endsWith(context_path) || requestURI
					.endsWith(context_path + "/"))
					&& authenticate != null
					&& authenticate.equals("success")) {

				logger.info("doFilter:09:" + authenticate);

				if (authenticate != null && authenticate.equals("success")) {
					// if(request.getParameter("form_login")!=null) {

					// может быть вообще убрать это условие - и устанавливать
					// новый Principal независимо -
					// есть ли текущая активная сессия.
					// if(principal2==null ||
					// request.getParameter(overauth)!=null){

					logger.info("doFilter:09_1");

					if (request.getParameter(overauth) != null) {

						logger.info("doFilter:09_2");

						if (request2.getSessionInternal().getNote("overauth") == null) {

							logger.info("doFilter:09_3:"
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

					/*
					 * не используется String codeSystem =
					 * getCodeSystem(request);
					 * 
					 * logger.info("doFilter:010:"+codeSystem);
					 */

					String login_user = (String) request.getSession()
							.getAttribute("login_user");

					logger.info("doFilter:010_+1:" + login_user);

					authenticate(/* codeSystem, */request, login_user);

					principal1 = request.getUserPrincipal();

					logger.info("doFilter:011:" + principal1);

					logger.info("doFilter:012:" + request.getMethod());

					// org.apache.catalina.connector.Request request2=null;

					// request2 = SecurityAssociationValve.activeRequest.get();

					logger.info("doFilter:013:" + request2.getMethod());

					logger.info("doFilter:014:"
							+ request.getSession().getAttribute(
									"incoming_http_method"));

					// FormAuthenticator.restoreRequest()

					// !!!
					// нужно установить метод запроса токой - какой он был при
					// первом обращении ИС к IDP
					// IDPWebRequestUtil учитывает это при расшифровке SAML
					// Request- "GET".equals(request.getMethod())
					request2.getCoyoteRequest()
							.method()
							.setString(
									(String) request.getSession().getAttribute(
											"incoming_http_method"));

					logger.info("doFilter:015:" + request2.getMethod());

					SavedRequest saved = (SavedRequest) request2
							.getSessionInternal(false)
							.getNote(
									"org.apache.catalina.authenticator.REQUEST");

					// saved = null при прямом обращении пользователя без
					// SAMLRequest -
					// при нём мы не сохраняем saved.
					if (saved != null) {
						// method - GET/POST
						// request2.getCoyoteRequest().method().setString(saved.getMethod());

						// ?
						// request2.getCoyoteRequest().queryString().setString(saved.getQueryString());

						// ?
						// request2.getCoyoteRequest().requestURI().setString(saved.getRequestURI());

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

						logger.info("doFilter:016:" + saved.getMethod());
						// logger.info("doFilter:017:"+saved.getQueryString());
						// logger.info("doFilter:018:"+saved.getRequestURI());

						logger.info("doFilter:017:"
								+ request.getSession().getAttribute(
										RequestQueryStringKey));
						logger.info("doFilter:018:"
								+ request.getSession().getAttribute(
										RequestRequestURIKey));
					}

					// !!!
					request.getSession().removeAttribute("authenticate");

					// }
				} else {

				}
			} else if (requestURI.endsWith(info_page)) {
				// информация по текущей сессии
				logger.info("doFilter:018_2");
			} else {
				// первый запрос со стороны внешней системы или пользователя
				// в т.ч. /login

				logger.info("doFilter:019_002");

				if (request.getParameter(GeneralConstants.SAML_REQUEST_KEY) != null) {
					// действительно первый запрос со стороны систем

					// !!!может их лучше здесь держать - подумать!!!
					// перед обработкой - сохранение запроса и метода
					// обязательно!!!
					// request.getSession().setAttribute("incoming_http_method",
					// request.getMethod());
					// saveRequest(request2,
					// request2.getSessionInternal(false));
					// пока можно выполнять это только для /login, а в
					// isLogout()
					// вместо incoming_http_method использовать
					// request.getMethod()

					if (isLogout(request2)) {
						// /logout
						// ничего не делаем
						logger.info("doFilter:019_001");

						// getCodeSystem нельзя использовать, т.к. мы не
						// сохраняем метод в сессии
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
						// перед обработкой - сохранение запроса и метода
						// обязательно!!!
						request.getSession().setAttribute(
								"incoming_http_method", request.getMethod());
						saveRequest(request2,
								request2.getSessionInternal(false));

						if (!getForceAuthn(request)) {

						} else {
							// требуют новую аутентификацию, хотя уже есть
							// активная сессия

							logger.info("doFilter:019_01");

							// важно!!!
							resetSessionData(request, request2);

							request.getSession()
									.setAttribute("incoming_http_method",
											request.getMethod());

							// теперь после сброса сессии principal2 должен быть
							// = null
							// необходимо обновить значение principal2, т.к. оно
							// исользается дальше,
							// а после сброса сессии оно сохранило ранее
							// установленное значение.
							// эквивалент principal2 = null;
							principal2 = request2.getSessionInternal(false)
									.getPrincipal();
						}

						if (getIsPassive(request)) {
							// пассивная аутентификация

							logger.info("doFilter:019_1");

							if (principal2 == null) {
								// нет активной сессии
								// устанавливаем флаг для обработки в
								// CUDAbstractIDPValve

								logger.info("doFilter:019_2");

								String[] data = getExtPassiveAuthData(request);

								String login = data[0];
								String password = data[1];
								String login_encrypt_key = data[2];
								String password_encrypt_key = data[3];
								String secret_key_key = data[4];
								String initialization_vector_key = data[5];

								logger.info("doFilter:019_2_1:" + login);
								logger.info("doFilter:019_2_2:" + password);
								logger.info("doFilter:019_2_3:"
										+ login_encrypt_key);
								logger.info("doFilter:019_2_4:"
										+ password_encrypt_key);
								logger.info("doFilter:019_2_5:"
										+ secret_key_key);
								logger.info("doFilter:019_2_6:"
										+ initialization_vector_key);

								if ((login != null || (login_encrypt_key != null
										&& secret_key_key != null && initialization_vector_key != null))
										&& (password != null/*
															 * ||password_hash!=null
															 */|| (password_encrypt_key != null
												&& secret_key_key != null && initialization_vector_key != null))) {
									// режим передачи логин/пароля пользователя

									logger.info("doFilter:019_3");

									boolean auth_result = true;
									String login_user = null;
									String success = "false";
									AuthMode am = AuthMode.HTTP_REDIRECT_EXT_AUTH_OPEN;

									try {
										/*
										 * if(password_hash!=null){ login_user =
										 * (new ContextAccessWebManager()).
										 * authenticate_login(login,
										 * password_hash,
										 * AuthMode.HTTP_REDIRECT_EXT_AUTH_HASH,
										 * getIPAddress(request));
										 * success="true"; }
										 */

										if (login_encrypt_key != null
												|| password_encrypt_key != null) {

											logger.info("doFilter:019_4");

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

										logger.info("doFilter:019_5");

										if (password != null && login != null) {
											login_user = (new ContextAccessWebManager())
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
										logger.error("doFilter:019_3:error:"
												+ e1.getMessage());
									} catch (GeneralFailure e2) {
										logger.error("doFilter:019_4:error:"
												+ e2.getMessage());
									} catch (Exception e3) {
										logger.error("doFilter:019_5:error:"
												+ e3.getMessage());
									}

									if (success.equals("true")) {

										// !!!
										// из первый запрос со стороны внешней
										// системы - нормальная аутентификация
										// ?!
										// request.getSession().setAttribute("incoming_http_method",
										// request.getMethod());
										// ?! saveRequest(request2,
										// request2.getSessionInternal(false));

										// !!!
										// из WebLoginAction
										request.getSession()
												.setAttribute("cud_auth_type",
														"urn:oasis:names:tc:SAML:2.0:ac:classes:password");
										request.getSession().setAttribute(
												"authenticate", "success");
										request.getSession().setAttribute(
												"login_user", login_user);
										response.sendRedirect(request
												.getContextPath() + "/");

									} else {
										request.getSession().setAttribute(
												"is_passive_failed", "true");
									}

								} else {

									request.getSession().setAttribute(
											"is_passive_failed", "true");

								}

							} else {
								// пользователь уже аутентифицирован
							}

						} else {
							// нормальная аутентификация

							logger.info("doFilter:019_3");

							if (principal2 == null) {

								// редирект на страницу логина

								// подстраховка - пока не используем
								// request.getSession().removeAttribute("is_passive_failed");

								logger.info("doFilter:020:"
										+ request.getMethod());

								// ?!
								// request.getSession().setAttribute("incoming_http_method",
								// request.getMethod());
								// ?! saveRequest(request2,
								// request2.getSessionInternal(false));

								String auth_type = getAuthType(request);

								logger.info("doFilter:021:auth_type:"
										+ auth_type);

								// перемещаем в соответствующие сервлеты -
								// WebCertAction и WebLoginAction
								// после успешной аутентификации
								// request.getSession().setAttribute("cud_auth_type",
								// auth_type);

								if (auth_type_x509.equals(auth_type)) {

									response.sendRedirect(redirect_url);
									// response.sendRedirect(request.getContextPath()+redirect_url);

								} else if (auth_type_password.equals(auth_type)) {

									// отмечаем, что это цуд перенаправил на
									// страницу логин/пароля
									request.getSession().setAttribute(
											"password_cud_redirect", true);
									response.sendRedirect(request
											.getContextPath() + login_to_form);

								} else {
									// !!!
									// вообще-то сюда не попадём
									// раньше здесь были когда вместе
									// обрабатывали и обращения
									// с SAMLRequest и без, сейчас же здесь
									// только с SAMLRequest
									// а в этом случае по-умолчанию auth_type =
									// auth_type_password

									// пользователь пришёл не из системы,
									// а напрямую обращается в ЦУД
									// сначала хочет зарегистрироваться,
									// а потом обращаться к системам
									// устанавливаем ему метод аутентификации по
									// умолчанию -
									// -сертификат

									response.sendRedirect(redirect_url);

									// response.sendRedirect(request.getContex
									// tPath()+default_to_form);
									// response.sendRedirect(request.getContextPath()+"/error.jsp");
								}

							} else {
								// запрос на регистрацию когда пользователь уже
								// аутентифицирован

								// требуемый тип аутентификации
								String req_auth_type = getAuthType(request);

								logger.info("doFilter:022:req_auth_type:"
										+ req_auth_type);

								// имеющийся тип аутентификации
								String main_auth_type = (String) request
										.getSession().getAttribute(
												"cud_auth_type");

								logger.info("doFilter:023:main_auth_type:"
										+ main_auth_type);

								if (auth_type_x509.equals(req_auth_type)
										&& auth_type_password
												.equals(main_auth_type)) {

									// когда от нас требуют сертификат,
									// а у нас есть логин/пароль

									logger.info("doFilter:024");
									// перемещаем в соответствующие сервлеты -
									// WebCertAction и WebLoginAction
									// после успешной аутентификации
									// request.getSession().setAttribute("cud_auth_type",
									// req_auth_type);

									// важно!!!
									// ?resetSessionData(request, request2);
									// ?request.getSession().setAttribute("incoming_http_method",
									// request.getMethod());

									// запуск механизма 2-х уровневой
									// аутентификации
									// при наличии сессии с аутентификацией по
									// паролю
									// при выводе страницы с выбором сертификата
									// сессия по паролю сохраняется
									// и подменяется лишь при успешной
									// аутентификации по сертификату
									// !надо ещё доделать момент, когда
									// пользователь по сертификату не
									// зарегистрирован

									// ?response.sendRedirect(redirect_url);

									// ?!
									// request.getSession().setAttribute("incoming_http_method",
									// request.getMethod());
									// ?! saveRequest(request2,
									// request2.getSessionInternal(false));
									// защита от отправки ответа в
									// CUDAbstractIDPValve
									request2.setUserPrincipal(null);

									request2.getSessionInternal().setNote(
											"overauth", "1");

									response.sendRedirect(redirect_url
											+ "?overauth");

								} else {

									logger.info("doFilter:024_1");

									// обращение к ЦУД с уже аутентифицированным
									// пользователем
									// 1) система: если есть аутент по паролю, и
									// требуется по паролю
									// 2) система:если есть аутент по
									// сертификату, и требуется по паролю
									// 3) система:если есть аутент по
									// сертификату, и требуется по сертификату
								}
							}

						}

					}

				} else {
					// обращение напрямую к IDP без SAMLRequest

					logger.info("doFilter:024_1_1");

					if (principal2 == null) {

						// редирект на страницу логина

						logger.info("doFilter:024_1_2");

						// пользователь пришёл не из системы,
						// а напрямую обращается в ЦУД
						// сначала хочет зарегистрироваться,
						// а потом обращаться к системам
						// устанавливаем ему метод аутентификации по умолчанию -
						// -сертификат

						response.sendRedirect(redirect_url);

						// response.sendRedirect(request.getContex
						// tPath()+default_to_form);
						// response.sendRedirect(request.getContextPath()+"/error.jsp");

					} else {
						// запрос на регистрацию когда пользователь уже
						// аутентифицирован

						logger.info("doFilter:024_1_3");

						// обращение к ЦУД с уже аутентифицированным
						// пользователем
						// 4) прямое обращение : нет типа аутентификации
						// [hosted]
					}

				}

			}

		} catch (Exception e) {
			logger.error("doFilter:025:error:" + e);
		} finally {
			logger.info("doFilter:026");
			if (return_flag == 0) {
				filterChain.doFilter(servletRequest, servletResponse);
			}
		}
	}

	private void authenticate(
			/* String codeSystem, */HttpServletRequest servletRequest,
			String login_user) {
		logger.info("authenticate:027");
		try {

			Principal principal = null;
			String credential = "9753560";
			// String username= "bubnov";
			org.apache.catalina.connector.Request request2 = null;

			request2 = SecurityContextAssociationValve.getActiveRequest();
			if (request2 == null)
				throw new IllegalStateException("request is null");

			// Session session = request2.getSessionInternal(false);
			// logger.info("doFilter:04_:"+session.getClass());
			// logger.info("doFilter:04_+:"+(session.getPrincipal()==null));
			// logger.info("doFilter:04_++:"+session.getNote(Constants.SESS_USERNAME_NOTE));
			// logger.info("doFilter:04_+++:"+session.getNote(Constants.FORM_PRINCIPAL_NOTE));

			logger.info("authenticate:028");

			/*
			 * List<String> roles = new ArrayList<String>();
			 * 
			 * roles.add("manager"); roles.add("employee"); roles.add("sales");
			 */

			principal = new GenericPrincipal(null, login_user, credential/*
																		 * ,
																		 * roles
																		 */);

			register(request2, principal, login_user, credential,
					servletRequest);

			logger.info("authenticate:029");

		} catch (Exception e) {
			logger.error("authenticate:030:error:" + e);
		}
	}

	private String getCodeSystem(HttpServletRequest request,
			String incomingHttpMethod) {

		// !!!
		// incomingHttpMethod передаётся при logout
		// !!!
		// ещё важно, что при logout,
		// getSamlObject имеет тип LogoutRequestType,
		// а не AuthnRequestType

		logger.info("getCodeSystem:031");
		String result = null;

		try {
			org.apache.catalina.connector.Request request2 = null;
			request2 = SecurityContextAssociationValve.getActiveRequest();
			Session session = request2.getSessionInternal(false);

			String samlRequestMessage = (String) session.getNote("SAMLRequest");

			if (samlRequestMessage != null) {

				// IDPWebRequestUtil webRequestUtil = new
				// IDPWebRequestUtil(request, null, null);
				// SAMLDocumentHolder samlDocumentHolder =
				// webRequestUtil.getSAMLDocumentHolder(samlRequestMessage);

				// !!!
				boolean begin_req_method = "GET"
						.equals(incomingHttpMethod != null ? incomingHttpMethod
								: (String) request.getSession().getAttribute(
										"incoming_http_method"));

				SAMLDocumentHolder samlDocumentHolder = getSAMLDocumentHolder(
						samlRequestMessage, begin_req_method);

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

	private boolean isLogout(Request request) {
		boolean result = false;

		try {
			logger.info("isLogout:01");

			String samlRequestMessage = (String) request.getSessionInternal()
					.getNote(GeneralConstants.SAML_REQUEST_KEY);

			// !!!
			// при логауте мы в сессии сохранять или нет incoming_http_method -
			// открытый вопрос
			// boolean begin_req_method =
			// "GET".equals((String)request.getSession().getAttribute("incoming_http_method"));
			boolean begin_req_method = "GET".equals((String) request
					.getMethod());

			SAMLDocumentHolder samlDocumentHolder = getSAMLDocumentHolder(
					samlRequestMessage, begin_req_method);
			if (samlDocumentHolder != null) {
				logger.info("isLogout:02");

				if (samlDocumentHolder.getSamlObject() != null) {
					// RequestAbstractType requestAbstractType =
					// (RequestAbstractType)samlDocumentHolder.getSamlObject();
					// AuthnRequestType requestAbstractType =
					// (AuthnRequestType)samlDocumentHolder.getSamlObject();

					if (samlDocumentHolder.getSamlObject() instanceof LogoutRequestType == true) {
						result = true;
					}
				}
			}
			logger.info("isLogout:03:" + result);

		} catch (Exception e) {
			logger.error("isLogout:error:" + e);
		}

		return result;
	}

	private String getLoginLogout(Request request) {

		String result = null;

		try {
			logger.info("getLoginLogout:01");

			String samlRequestMessage = (String) request.getSessionInternal()
					.getNote(GeneralConstants.SAML_REQUEST_KEY);

			// !!!
			// при логауте мы в сессии сохранять или нет incoming_http_method -
			// открытый вопрос
			// boolean begin_req_method =
			// "GET".equals((String)request.getSession().getAttribute("incoming_http_method"));
			boolean begin_req_method = "GET".equals((String) request
					.getMethod());

			SAMLDocumentHolder samlDocumentHolder = getSAMLDocumentHolder(
					samlRequestMessage, begin_req_method);
			if (samlDocumentHolder != null) {
				logger.info("getLoginLogout:02:"
						+ (samlDocumentHolder.getSamlObject() == null));

				if (samlDocumentHolder.getSamlObject() != null) {
					// RequestAbstractType requestAbstractType =
					// (RequestAbstractType)samlDocumentHolder.getSamlObject();
					// AuthnRequestType requestAbstractType =
					// (AuthnRequestType)samlDocumentHolder.getSamlObject();

					if (samlDocumentHolder.getSamlObject() instanceof LogoutRequestType == true) {

						LogoutRequestType lrt = (LogoutRequestType) samlDocumentHolder
								.getSamlObject();

						result = lrt.getNameID().getValue();
					}
				}
			}
			logger.info("getLoginLogout:03:" + result);

		} catch (Exception e) {
			logger.error("getLoginLogout:error:" + e);
		}

		return result;
	}

	private String getAuthType(HttpServletRequest request) {

		logger.info("getAuthType:041");
		List<String> classes_list = null;

		// !!!
		// когда запрос просто на idp без SAMLRequest, то result должен =null
		// когда запрос от ИС (с SAMLRequest), то ставим по умолчанию
		// аутентификацию по паролю
		String result = null;

		try {
			org.apache.catalina.connector.Request request2 = null;
			request2 = SecurityContextAssociationValve.getActiveRequest();
			Session session = request2.getSessionInternal(false);

			String samlRequestMessage = (String) session.getNote("SAMLRequest");

			if (samlRequestMessage != null) {

				// !!! Задаём по умолчанию аутентификация по паролю
				result = auth_type_password;

				// IDPWebRequestUtil webRequestUtil = new
				// IDPWebRequestUtil(request, null, null);
				// SAMLDocumentHolder samlDocumentHolder =
				// webRequestUtil.getSAMLDocumentHolder(samlRequestMessage);

				boolean begin_req_method = "GET".equals((String) request
						.getSession().getAttribute("incoming_http_method"));

				SAMLDocumentHolder samlDocumentHolder = getSAMLDocumentHolder(
						samlRequestMessage, begin_req_method);

				logger.info("getAuthType:045:" + (samlDocumentHolder == null));

				if (samlDocumentHolder != null) {
					logger.info("getAuthType:046:"
							+ (samlDocumentHolder.getSamlObject() == null));

					if (samlDocumentHolder.getSamlObject() != null) {
						// RequestAbstractType requestAbstractType =
						// (RequestAbstractType)samlDocumentHolder.getSamlObject();
						AuthnRequestType requestAbstractType = (AuthnRequestType) samlDocumentHolder
								.getSamlObject();
						String issuer = requestAbstractType.getIssuer()
								.getValue();

						// requestAbstractType

						RequestedAuthnContextType ract = requestAbstractType
								.getRequestedAuthnContext();
						logger.info("getAuthType:047:" + ract);

						if (ract != null) {
							classes_list = ract.getAuthnContextClassRef();

							logger.info("getAuthType:048:" + classes_list);

							if (classes_list != null) {
								logger.info("getAuthType:049:"
										+ classes_list.size());

								if (classes_list.size() > 0) {
									result = classes_list.get(0);
									logger.info("getAuthType:050:" + result);
								}
							}
						}
					}
				}

			}

			logger.info("getAuthType:051");
		} catch (Exception e) {
			logger.error("getAuthType:052:error:" + e);
			// !!!
			result = null;
		}

		return result;
	}

	private boolean getForceAuthn(HttpServletRequest request) {

		logger.info("getForceAuthn:01");

		// !!!
		// Обязательно тип Boolean
		// в AuthnRequestType используется Boolean
		// и если в запросе нет параметра, то он = null
		// и return result!=null?result:false;
		Boolean result = false;
		try {
			org.apache.catalina.connector.Request request2 = null;
			request2 = SecurityContextAssociationValve.getActiveRequest();
			Session session = request2.getSessionInternal(false);

			String samlRequestMessage = (String) session.getNote("SAMLRequest");

			// logger.info("getForceAuthn:02:"+samlRequestMessage);

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
						logger.info("getForceAuthn:02");

						// RequestAbstractType requestAbstractType =
						// (RequestAbstractType)samlDocumentHolder.getSamlObject();
						AuthnRequestType requestAbstractType = (AuthnRequestType) samlDocumentHolder
								.getSamlObject();

						logger.info("getForceAuthn:03");

						String issuer = requestAbstractType.getIssuer()
								.getValue();

						logger.info("getForceAuthn:04:"
								+ (requestAbstractType == null));

						result = requestAbstractType.isForceAuthn();

						logger.info("getForceAuthn:02:" + result);

					}
				}

			}

		} catch (Exception e) {
			logger.error("getForceAuthn:error:" + e);
			e.printStackTrace(System.out);
		}

		return result != null ? result : false;
	}

	private boolean getIsPassive(HttpServletRequest request) {

		logger.info("getIsPassive:01");

		// !!!
		// Обязательно тип Boolean
		// в AuthnRequestType используется Boolean
		// и если в запросе нет параметра, то он = null
		// и return result!=null?result:false;
		Boolean result = false;
		try {
			org.apache.catalina.connector.Request request2 = null;
			request2 = SecurityContextAssociationValve.getActiveRequest();
			Session session = request2.getSessionInternal(false);

			String samlRequestMessage = (String) session.getNote("SAMLRequest");

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
						String issuer = requestAbstractType.getIssuer()
								.getValue();

						result = requestAbstractType.isIsPassive();
						logger.info("getIsPassive:02:" + result);

					}
				}

			}

		} catch (Exception e) {
			logger.error("getAIsPassive:error:" + e);
			// e.printStackTrace(System.out);
		}

		return result != null ? result : false;
	}

	private String[] getExtPassiveAuthData(HttpServletRequest request) {

		logger.info("getExtPassiveAuthData:01");

		// !!!
		// Обязательно тип Boolean
		// в AuthnRequestType используется Boolean
		// и если в запросе нет параметра, то он = null
		// и return result!=null?result:false;
		String[] result = new String[6];
		URI result_uri = null;
		try {
			org.apache.catalina.connector.Request request2 = null;
			request2 = SecurityContextAssociationValve.getActiveRequest();
			Session session = request2.getSessionInternal(false);

			String samlRequestMessage = (String) session.getNote("SAMLRequest");

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
						String issuer = requestAbstractType.getIssuer()
								.getValue();

						result_uri = requestAbstractType.getDestination();

					}
				}

			}

			if (result_uri != null) {

				String result_st = result_uri.toString();

				logger.info("getExtPassiveAuthData:02:" + result_st);

				// URLDecoder.decode(result[0] , "UTF-8");\
				result[0] = WebUtil.getTokenValue(result_st, "login");
				result[1] = WebUtil.getTokenValue(result_st, "password");
				/*
				 * result[2]=WebUtil.getTokenValue(result_st, "login_encrypt");
				 * result[3]=WebUtil.getTokenValue(result_st,
				 * "password_encrypt");
				 * result[4]=WebUtil.getTokenValue(result_st, "secret_key");
				 * result[5]=WebUtil.getTokenValue(result_st,
				 * "initialization_vector");
				 */
				result[2] = WebUtil.getTokenValue(result_st, "elogin");
				result[3] = WebUtil.getTokenValue(result_st, "epassword");
				result[4] = WebUtil.getTokenValue(result_st, "skey");
				result[5] = WebUtil.getTokenValue(result_st, "ivector");

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
			}

		} catch (Exception e) {
			logger.error("getExtPassiveAuthData:error:" + e);
			// e.printStackTrace(System.out);
		}

		return result;
	}

	private void resetSessionData(HttpServletRequest request,
			org.apache.catalina.connector.Request request2) throws Exception {

		logger.info("resetSessionData:01");

		try {

			// очистка всех возможных установленных аттрибутов
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
			logger.error("resetSessionData:error:" + e);
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
		logger.info("register:01");

		String cud_auth_type = (String) servletRequest.getSession()
				.getAttribute("cud_auth_type");

		logger.info("register:01+:" + cud_auth_type);

		// request.setAuthType(AUTH_TYPE);
		request.setAuthType(cud_auth_type);
		request.setUserPrincipal(principal);

		// Cache the authentication principal in the session
		Session session = request.getSessionInternal(false);

		logger.info("register:01_1:" + session.getId());
		logger.info("register:01_2:" + session.getAuthType());
		logger.info("register:01_3:" + session.getSession().getId());
		logger.info("register:01_4:" + (session.getPrincipal() == null));

		if (session.getPrincipal() != null) {
			logger.info("register:01_4+:" + session.getPrincipal().getName());
		}

		String ssoId_ = (String) request
				.getNote("org.apache.catalina.request.SSOID");

		logger.info("register:01_5:" + ssoId_);

		if (session != null) {

			logger.info("register:02");

			// session.setAuthType(AUTH_TYPE);
			session.setAuthType(cud_auth_type);
			session.setPrincipal(principal);
			session.setNote("org.apache.catalina.authenticator.PRINCIPAL",
					principal);

			if (username != null) {
				logger.info("register:03");
				// session.setNote(Constants.SESS_USERNAME_NOTE, username);
			} else {
				logger.info("register:04");
				session.removeNote(Constants.SESS_USERNAME_NOTE);
			}
			if (password != null) {
				logger.info("register:05");
				// session.setNote(Constants.SESS_PASSWORD_NOTE,
				// getPasswordAsString(password));
			} else {
				logger.info("register:06");
				session.removeNote(Constants.SESS_PASSWORD_NOTE);
			}
		}

		logger.info("register:07");
	}

	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

	protected void saveRequest(org.apache.catalina.connector.Request request,
			Session session) throws IOException {
		logger.info("saveRequest:01");

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

		// logger.info("saveRequest:02_1:"+request.getMethod());
		// logger.info("saveRequest:02_2:"+request.getQueryString());
		// logger.info("saveRequest:02_3:"+request.getRequestURI());
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
			logger.error("resource_detect:error:" + e);
		}
		return result;
	}

	private void setRedirectUrl(HttpServletRequest request)
			throws ServletException {

		try {
			String userAgent = request.getHeader("user-agent");
			logger.info("doFilter:userAgent:" + userAgent);

			redirect_url = request.getScheme() + "://"
					+ request.getServerName() + ":" + request.getServerPort()
					+ request.getContextPath() + cert_to_form;

			logger.info("doFilter:redirect_url:" + redirect_url);

		} catch (Exception e) {
			logger.error("resource_detect:error:" + e);
		}
	}

	private String getIPAddress(HttpServletRequest request) {

		String ipAddress = request.getRemoteAddr();

		return ipAddress;
	}

	public SAMLDocumentHolder getSAMLDocumentHolder(String samlMessage,
			boolean redirectProfile) throws Exception {
		logger.info("getSAMLDocumentHolder:01:" + redirectProfile);

		InputStream is = null;
		SAML2Request saml2Request = new SAML2Request();
		try {
			if (redirectProfile) {
				logger.info("getSAMLDocumentHolder:02");

				is = RedirectBindingUtil.base64DeflateDecode(samlMessage);
				logger.info("getSAMLDocumentHolder:03");

			} else {
				byte[] samlBytes = PostBindingUtil.base64Decode(samlMessage);
				is = new ByteArrayInputStream(samlBytes);
			}
		} catch (Exception rte) {
			logger.error("getSAMLDocumentHolder:error:" + rte);
			throw rte;
		}

		logger.info("getSAMLDocumentHolder:04");

		saml2Request.getSAML2ObjectFromStream(is);

		logger.info("getSAMLDocumentHolder:05");

		return saml2Request.getSamlDocumentHolder();
	}

	private Cipher getCipherDecrypt(String secretKeyBase64, String ivBase64) {

		Cipher result = null;
		try {

			// шифрование
			/*
			 * final byte[] data = password.getBytes("utf-8");
			 * 
			 * SecretKey secretKey =
			 * KeyGenerator.getInstance("GOST28147").generateKey();
			 * 
			 * Cipher cipher = Cipher.getInstance(CIPHER_ALG);
			 * cipher.init(Cipher.ENCRYPT_MODE, secretKey); final byte[] iv =
			 * cipher.getIV();
			 * 
			 * final byte[] encryptedtext = cipher.doFinal(data, 0,
			 * data.length);
			 * 
			 * Cipher wrapCipher = Cipher.getInstance(CIPHER_ALG_TRANSPORT);
			 * wrapCipher.init(Cipher.WRAP_MODE, publicKeyCUD); byte[]
			 * wrappedSecretKey = wrapCipher.wrap(secretKey);
			 * 
			 * 
			 * result = new String[3];
			 * 
			 * String passwordEncryptBase64 = Base64.encodeBytes(encryptedtext,
			 * Base64.DONT_BREAK_LINES); String secretKeyBase64 =
			 * Base64.encodeBytes(wrappedSecretKey, Base64.DONT_BREAK_LINES);
			 * String ivBase64 = Base64.encodeBytes(iv,
			 * Base64.DONT_BREAK_LINES);
			 * 
			 * result[0]=passwordEncryptBase64; result[1]=secretKeyBase64;
			 * result[2]=ivBase64;
			 */
			logger.info("getCipherDecrypt:01:" + secretKeyBase64);
			logger.info("getCipherDecrypt:02:" + ivBase64);

			final byte[] wrappedSecretKey = Base64.decode(secretKeyBase64);
			final byte[] iv = Base64.decode(ivBase64);

			// расшифровка

			KeyStoreKeyManager kskm = new KeyStoreKeyManager();
			// в KeyStoreKeyManager KeyStore ks - static
			// поэтому ks уже инициализирован нужными параметрами
			// а также важно, что static:
			// private static char[] signingKeyPass;
			// private static String signingAlias;

			KeyPair keyPair = kskm.getSigningKeyPair();
			PublicKey publicKey = keyPair.getPublic();
			PrivateKey responderPrivateKey = keyPair.getPrivate();

			logger.info("getCipherDecrypt:03:" + responderPrivateKey);

			Cipher unwrapCipher = Cipher.getInstance(CIPHER_ALG_TRANSPORT);
			unwrapCipher.init(Cipher.UNWRAP_MODE, responderPrivateKey);
			SecretKey clientSecretKey = (SecretKey) unwrapCipher.unwrap(
					wrappedSecretKey, null, Cipher.SECRET_KEY);

			Cipher cipher = Cipher.getInstance(CIPHER_ALG);
			cipher.init(Cipher.DECRYPT_MODE, clientSecretKey,
					new IvParameterSpec(iv), null);

			result = cipher;

			logger.info("getCipherDecrypt:04");

		} catch (Exception e) {
			logger.error("getCipherDecrypt:error:" + e);
		}
		return result;
	}

	private String getTextDecrypt(String textEncryptBase64, Cipher cipher) {

		String result = null;
		try {

			// шифрование
			/*
			 * final byte[] data = password.getBytes("utf-8");
			 * 
			 * SecretKey secretKey =
			 * KeyGenerator.getInstance("GOST28147").generateKey();
			 * 
			 * Cipher cipher = Cipher.getInstance(CIPHER_ALG);
			 * cipher.init(Cipher.ENCRYPT_MODE, secretKey); final byte[] iv =
			 * cipher.getIV();
			 * 
			 * final byte[] encryptedtext = cipher.doFinal(data, 0,
			 * data.length);
			 * 
			 * Cipher wrapCipher = Cipher.getInstance(CIPHER_ALG_TRANSPORT);
			 * wrapCipher.init(Cipher.WRAP_MODE, publicKeyCUD); byte[]
			 * wrappedSecretKey = wrapCipher.wrap(secretKey);
			 * 
			 * 
			 * result = new String[3];
			 * 
			 * String passwordEncryptBase64 = Base64.encodeBytes(encryptedtext,
			 * Base64.DONT_BREAK_LINES); String secretKeyBase64 =
			 * Base64.encodeBytes(wrappedSecretKey, Base64.DONT_BREAK_LINES);
			 * String ivBase64 = Base64.encodeBytes(iv,
			 * Base64.DONT_BREAK_LINES);
			 * 
			 * result[0]=passwordEncryptBase64; result[1]=secretKeyBase64;
			 * result[2]=ivBase64;
			 */
			logger.info("getTextDecrypt:01:" + textEncryptBase64);

			byte[] encryptedtext = Base64.decode(textEncryptBase64);

			byte[] decryptedtext = cipher.doFinal(encryptedtext, 0,
					encryptedtext.length);

			result = new String(decryptedtext, "utf-8");

			logger.info("getText:02:" + result);

		} catch (Exception e) {
			logger.error("getText:error:" + e);
		}
		return result;
	}

}
