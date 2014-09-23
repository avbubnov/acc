package ru.spb.iac.cud.services.web;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathValidator;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

import com.objsys.asn1j.runtime.Asn1BerDecodeBuffer;

import ru.spb.iac.cud.context.ContextAccessWebManager;
import ru.spb.iac.cud.context.ContextIDPAccessManager;
import ru.spb.iac.cud.context.ContextIDPUtilManager;
import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.InvalidCredentials;
import ru.spb.iac.cud.exceptions.RevokedCertificate;
import ru.spb.iac.cud.items.Token;

import com.objsys.asn1j.runtime.Asn1BerEncodeBuffer;

import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.ContentInfo;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.DigestAlgorithmIdentifier;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignedData;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.params.OID;
import ru.CryptoPro.JCP.tools.Decoder;

public class WebCertAction extends HttpServlet {

	final static Logger logger = LoggerFactory.getLogger(WebCertAction.class);

	private static final long serialVersionUID = 1L;

	public static final String STR_CMS_OID_SIGNED = "1.2.840.113549.1.7.2";
	public static final String DIGEST_OID = JCP.GOST_DIGEST_OID;

	private static StringBuffer out = new StringBuffer("");
	private static StringBuffer out1 = new StringBuffer("");

	private static String alias = "certificate";
	private static String alias_root = "уцспбгуп«спбиац».crt";

	private String root_sn = null;

	private static String cert_store_url;

	public WebCertAction() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init(ServletConfig config) throws ServletException {
		try {

			if (cert_store_url == null) {
				cert_store_url = config.getServletContext().getInitParameter(
						"cert_store_url");
			}

			if (cert_store_url == null) {
				throw new Exception("cert_store_url is not set!!!");
			}

			// logger.info("cert_store_url:"+cert_store_url);

		} catch (Exception e) {
			logger.error("error:" + e);
		}
	}

	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		String backUrl = null, forceBack = null;
		String success = "false";
		String signatureValue = null;
		String repeatLoginUrl = null;
		int revokedCertificate = 0;

		String login_user = null;

		try {

			backUrl = request.getParameter("backUrl");
			signatureValue = request.getParameter("signatureValue");

			forceBack = request.getParameter("forceBack");

			if (signatureValue != null) {

				String certSN = validate(signatureValue);

				logger.info("service:certSN:" + certSN);

				if (certSN != null) {

					login_user = (new ContextAccessWebManager())
							.authenticate_cert_sn(certSN,
									getIPAddress(request),
									getCodeSystem(request));

					success = "true";

					HttpSession hs = request.getSession();

					hs.setAttribute("login_user", login_user);

				}
			}
		} catch (InvalidCredentials e1) {
			logger.error("error1:" + e1.getMessage());
		} catch (GeneralFailure e2) {
			logger.error("error2:" + e2.getMessage());
		} catch (RevokedCertificate e3) {
			revokedCertificate = 1;
			logger.error("error3:" + e3.getMessage());
		} catch (Exception e4) {
			logger.error("error4:" + e4.getMessage());
		}

		if (success.equals("true")) {

			request.getSession().setAttribute("cud_auth_type",
					"urn:oasis:names:tc:SAML:2.0:ac:classes:X509");

			request.getSession().setAttribute("authenticate", "success");
			response.sendRedirect(request.getContextPath()
					+ "/"
					+ (request.getParameter("overauth") != null ? "?overauth"
							: ""));

		} else {

			if (forceBack == null) {// всегда здесь

				repeatLoginUrl = request.getContextPath()
						+ "/services/access_cert.jsp?success=false"
						+ (backUrl != null ? "&backUrl=" + backUrl : "")
						+ (revokedCertificate == 1 ? "&revokedCertificate=1"
								: "")
						+ (request.getParameter("overauth") != null ? "&overauth"
								: "");

				response.sendRedirect(repeatLoginUrl);

			}
		}
	}

	private static void common(String destination, HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache, no-store");
	}

	private String getIPAddress(HttpServletRequest request) {

		String ipAddress = request.getRemoteAddr();

		return ipAddress;
	}

	private String validate(String message) {

		try {

			final Decoder decoder = new Decoder();
			final byte[] enc = decoder.decodeBuffer(new ByteArrayInputStream(
					message.getBytes()));

			logger.info("validate:04");

			return CMSVerify(enc, null, "12345".getBytes());
		} catch (Exception e) {
			logger.error("validate:error:" + e);
		}
		return null;

	}

	public String CMSVerify(byte[] buffer, Certificate[] certs, byte[] data)
			throws Exception {
		// clear buffers fo logs

		// logger.info("CMSVerify:001");

		out = new StringBuffer("");
		out1 = new StringBuffer("");
		final Asn1BerDecodeBuffer asnBuf = new Asn1BerDecodeBuffer(buffer);

		final ContentInfo all = new ContentInfo();

		all.decode(asnBuf);

		if (!new OID(STR_CMS_OID_SIGNED).eq(all.contentType.value))
			throw new Exception("Not supported");
		final SignedData cms = (SignedData) all.content;
		final byte[] text;
		if (cms.encapContentInfo.eContent != null)
			text = cms.encapContentInfo.eContent.value;
		else if (data != null)
			text = data;
		else
			throw new Exception("No content for verify");
		OID digestOid = null;
		final DigestAlgorithmIdentifier digestAlgorithmIdentifier = new DigestAlgorithmIdentifier(
				new OID(DIGEST_OID).value);
		for (int i = 0; i < cms.digestAlgorithms.elements.length; i++) {
			if (cms.digestAlgorithms.elements[i].algorithm
					.equals(digestAlgorithmIdentifier.algorithm)) {
				digestOid = new OID(
						cms.digestAlgorithms.elements[i].algorithm.value);
				break;
			}
		}
		if (digestOid == null)
			throw new Exception("Unknown digest");
		final OID eContTypeOID = new OID(
				cms.encapContentInfo.eContentType.value);

		if (cms.certificates != null) {

			// logger.info("CMSVerify:02");

			// Проверка на вложенных сертификатах
			for (int i = 0; i < cms.certificates.elements.length; i++) {
				final Asn1BerEncodeBuffer encBuf = new Asn1BerEncodeBuffer();
				cms.certificates.elements[i].encode(encBuf);

				final CertificateFactory cf = CertificateFactory
						.getInstance("X.509");
				final X509Certificate cert = (X509Certificate) cf
						.generateCertificate(encBuf.getInputStream());

				// logger.info("CMSVerify:03:"+cert.toString());
				// logger.info("CMSVerify:03:SubjectDN:"+cert.getSubjectDN());
				// logger.info("CMSVerify:03:"+cert.getSerialNumber());
				// logger.info("CMSVerify:03:cert_sn:"+dec_to_hex(cert.getSerialNumber()));
				// logger.info("CMSVerify:03:root_sn:"+root_sn());

				if (root_sn() != null
						&& !root_sn()
								.equals(dec_to_hex(cert.getSerialNumber()))) {

					if (chain_check(cert)) {
						return dec_to_hex(cert.getSerialNumber());
					}
				}

			}
		}

		return null;
	}

	public static boolean chain_check(Certificate pcert) {
		// TODO Auto-generated method stub
		boolean result = false;

		try {

			KeyStore keyStore = KeyStore.getInstance("CertStore", "JCP");

			keyStore.load(new FileInputStream(cert_store_url),
					"Access_Control".toCharArray());

			Enumeration aliases = keyStore.aliases();
			while (aliases.hasMoreElements()) {
				String alias = (String) aliases.nextElement();
				// logger.info("Current alias: " + alias);
				if (keyStore.isCertificateEntry(alias)) {
					// logger.info(
					// ((X509Certificate)keyStore.getCertificate(alias)).getSubjectDN()
					// );
				}
			}

			Certificate tr = keyStore.getCertificate(alias_root);
			Certificate crt = pcert;
			// Certificate crt = keyStore.getCertificate(alias);

			// logger.info("cert:"+tr.toString());

			final Certificate[] certs = new Certificate[2];
			certs[0] = crt;
			certs[1] = tr; // root

			final Set trust = new HashSet(0);
			trust.add(new TrustAnchor((X509Certificate) tr, null));

			final List cert = new ArrayList(0);
			for (int i = 0; i < certs.length; i++)
				cert.add(certs[i]);

			// Параметры
			final PKIXBuilderParameters cpp = new PKIXBuilderParameters(trust,
					null);
			cpp.setSigProvider(null);
			final CollectionCertStoreParameters par = new CollectionCertStoreParameters(
					cert);
			final CertStore store = CertStore.getInstance("Collection", par);
			cpp.addCertStore(store);
			final X509CertSelector selector = new X509CertSelector();
			selector.setCertificate((X509Certificate) crt);
			cpp.setTargetCertConstraints(selector);

			// Сертификаты (CertPath)
			// 1)просто из списка сертификатов (в правильном порядке)
			// final CertificateFactory cf =
			// CertificateFactory.getInstance("X509");
			// final CertPath cp = cf.generateCertPath(cert);

			// 2) построение цепочки
			// а) с проверкой crl
			// cpp.setRevocationEnabled(true);
			// для использования расширения сертификата CRL Distribution Points
			// установить System.setProperty("com.sun.security.enableCRLDP",
			// "true");
			// или System.setProperty("com.ibm.security.enableCRLDP", "true");

			// б) без проверки crl
			cpp.setRevocationEnabled(false);
			final PKIXCertPathBuilderResult res = (PKIXCertPathBuilderResult) CertPathBuilder
					.getInstance("PKIX").build(cpp);
			final CertPath cp = res.getCertPath();

			// Проверка
			final CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
			cpp.setRevocationEnabled(false);
			cpv.validate(cp, cpp);

			result = true;

			// final File file = new File(STORE_PATH);

			// keyStore.store(new FileOutputStream(file), STORE_PASS);

		} catch (Exception e) {
			logger.error("error:" + e);
		}

		return result;
	}

	private static String dec_to_hex(BigInteger bi) {

		String result = null;

		try {
			result = bi.toString(16);
		} catch (NumberFormatException e) {
			logger.error("Error! tried to parse an invalid number format");
		}
		return result;
	}

	public String root_sn() {
		// TODO Auto-generated method stub

		if (root_sn == null) {

			// logger.info("root_sn:01");

			try {

				KeyStore keyStore = KeyStore.getInstance("CertStore", "JCP");

				keyStore.load(new FileInputStream(cert_store_url),
						"Access_Control".toCharArray());

				X509Certificate tr = (X509Certificate) keyStore
						.getCertificate(alias_root);

				root_sn = dec_to_hex(tr.getSerialNumber());

			} catch (Exception e) {
				logger.error("root_sn:error:" + e);
				// e.printStackTrace(System.out);
			}
		}

		return root_sn;
	}

	private String getCodeSystem(HttpServletRequest request) {

		// !!!
		// для метода важно, что в ExtFilter при
		// if(...||requestURI.endsWith(cert_to_auth)...)
		// идёт установка
		// request2.getSessionInternal().setNote(GeneralConstants.SAML_REQUEST_KEY,
		// request.getParameter(SAMLMessageKey))
		// и request.getSession().setAttribute("incoming_http_method",
		// request.getParameter(HTTPMethodKey))

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
