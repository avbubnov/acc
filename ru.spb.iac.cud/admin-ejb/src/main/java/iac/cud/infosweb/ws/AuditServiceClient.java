package iac.cud.infosweb.ws;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

import mypackage.Configuration;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLAssertionWriter;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.KeyInfoConfirmationDataType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationDataType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationType;
import org.picketlink.identity.xmlsec.w3.xmldsig.KeyInfoType;
import org.picketlink.identity.xmlsec.w3.xmldsig.X509CertificateType;
import org.picketlink.identity.xmlsec.w3.xmldsig.X509DataType;
import org.picketlink.trust.jbossws.SAML2Constants;
import org.picketlink.trust.jbossws.handler.SAML2Handler;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.InvalidCredentials;
import ru.spb.iac.cud.exceptions.TokenExpired;
import ru.spb.iac.cud.items.Attribute;
import ru.spb.iac.cud.items.AuditFunction;
import ru.spb.iac.cud.items.Function;
import ru.spb.iac.cud.items.Role;
import ru.spb.iac.cud.items.Token;
import ru.spb.iac.cud.items.UserToken;
import ru.spb.iac.cud.services.audit.AuditService;

@Name("auditServicesClient")
public class AuditServiceClient {

	final static org.slf4j.Logger LOGGER = LoggerFactory
			.getLogger(AuditServiceClient.class);

	// test-contur
	// "https://localhost:7443/CudServicesPro/AuditService?wsdl";
	// + в jboss-deployment-structure.xml

	// "https://acc.lan.iac.spb.ru:18443/CudServicesPro/AuditService?wsdl";
	static String endpointURI = Configuration.getAuditService();

	private AuditService accessServices = null;

	public void audit(String login, List<AuditFunction> funccodes)
			throws Exception {
		try {

			LOGGER.debug("auditServicesClient:audit:01");

			System.setProperty("javax.net.debug", "all");

			System.setProperty("javax.net.ssl.trustStore",
					Configuration.getStorePath());
			System.setProperty("javax.net.ssl.trustStoreType", "HDImageStore");
			System.setProperty("javax.net.ssl.trustStorePassword",
					"Access_Control");

			System.setProperty("javax.net.ssl.keyStore", "@");
			System.setProperty("javax.net.ssl.keyStoreType", "HDImageStore");
			System.setProperty("javax.net.ssl.keyStorePassword",
					"Access_Control");

			/*
			 * //test-contur HostnameVerifier /hv = new /HostnameVerifier() {
			 * public/ boolean verify/(String urlHostName, /SSLSession session) {
			 * HttpsURLConnection/.setDefaultHostnameVerifier/(hv)
			 */

			TokenInstall ti = new TokenInstall();

			String samlAssertion = ti.get_assertion();

		
			getPort(endpointURI, samlAssertion).audit(login, funccodes);

		
		} catch (GeneralFailure e1) {
			LOGGER.error("auditServicesClient:audit:error1:" + e1.getMessage());
		} catch (Exception e2) {
			LOGGER.error("auditServicesClient:audit:error2:" + e2.getMessage());
		}
	}

	public void sync_functions(List<Function> functions) throws Exception {

		try {

			String samlAssertion = null;

			LOGGER.debug("auditServicesClient:sync_functions:01");

			getPort(endpointURI, samlAssertion)
					.sync_functions(functions, "ADD");

		} catch (GeneralFailure e1) {
			LOGGER.error("auditServicesClient:sync_functions:error1:" + e1);
		} catch (Exception e2) {
			LOGGER.error("auditServicesClient:sync_functions:error2:" + e2);
		}
	}

	private AuditService getPort(String endpointURI, String samlAssertion)
			throws MalformedURLException {

		if (accessServices == null) {

			LOGGER.debug("getPort:02");

			QName serviceName = new QName(
					"http://audit.services.cud.iac.spb.ru/",
					"AuditServiceImplService");
			URL wsdlURL = new URL(endpointURI);

			Service service = Service.create(wsdlURL, serviceName);

			QName portName = new QName("http://audit.services.cud.iac.spb.ru/",
					"AuditServiceImplPort");

			accessServices = service.getPort(portName, AuditService.class);

			/*
			 * //test-contur // HTTPConduit httpConduit = (HTTPConduit)
			 * ((org.apache
			 * .cxf.jaxws.DispatchImpl)dispatch).getClient().getConduit() ;
			 * HTTPConduit httpConduit = (HTTPConduit)
			 * ClientProxy.getClient(accessServices).getConduit();
			 * TLSClientParameters tlsCP = new TLSClientParameters(); final
			 * SSLSocketFactoryImpl sslFact = new SSLSocketFactoryImpl();
			 * tlsCP.setSSLSocketFactory(sslFact);
			 * tlsCP.setDisableCNCheck(true);
			 * httpConduit.setTlsClientParameters(tlsCP);
			 */

			// Element assertion = get_saml_assertion_from_xml(samlAssertion);
			Element assertion = get_saml_assertion_from_xml_simple(samlAssertion);

			BindingProvider bp = (BindingProvider) accessServices;
			bp.getRequestContext().put(SAML2Constants.SAML2_ASSERTION_PROPERTY,
					assertion);
			List<Handler> handlers = bp.getBinding().getHandlerChain();
			handlers.add(new SAML2Handler());
			handlers.add(new TestClientCryptoSOAPHandler());
			bp.getBinding().setHandlerChain(handlers);
		}

	
		return accessServices;
	}

	private static Element get_saml_assertion_from_xml_simple(
			String samlAssertion) {

		LOGGER.debug("AuditServiceClient:get_saml_assertion_from_xml_simple:01");

		Element result = null;
		try {
			// !!!нельзя так просто
			// нужно именно setNamespaceAware (true)
			// Document Builder db =
			// Document Builder Factory . newInstance .newDocumentBuilder

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			// !!!
			dbf.setNamespaceAware(true);

			DocumentBuilder db = dbf.newDocumentBuilder();

			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(samlAssertion));

			Document assDoc = db.parse(is);

			result = assDoc.getDocumentElement();

			LOGGER.debug("AuditServiceClient:get_saml_assertion_from_xml_simple:02:"
					+ DocumentUtil.asString(assDoc));

		} catch (Exception e) {
			LOGGER.error("AuditServiceClient:get_saml_assertion_from_xml_simple:error:"
					+ e);
		}
		return result;
	}

	
}
