package ru.spb.iac.cud.uarm.ws;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.bind.JAXBElement;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

//import ru.CryptoPro.ssl.SSLSocketFactoryImpl;

//import org.apache.cxf.configuration.jsse.TLSClientParameters;
//import org.apache.cxf.transport.http.HTTPConduit;





import mypackage.Configuration;




//import org.apache.catalina.realm.GenericPrincipal;
//import org.apache.catalina.realm.GenericPrincipal;
//import org.apache.ws.security.WSConstants;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.IdResolver;
//import org.jboss.ws.core.StubExt;







import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.InvalidCredentials;
import ru.spb.iac.cud.items.Attribute;
import ru.spb.iac.cud.items.AuditFunction;
import ru.spb.iac.cud.items.Function;
import ru.spb.iac.cud.items.ISUsers;
import ru.spb.iac.cud.items.Role;
import ru.spb.iac.cud.items.Token;
import ru.spb.iac.cud.items.UserAttributes;
import ru.spb.iac.cud.items.UserAttributesRoles;
//import ru.spb.iac.cud.services.eis.util.UtilServices;
//import ru.spb.iac.cud.services.saml.SAMLServices;






import org.jboss.security.Base64Encoder;
import org.jboss.security.Util;
//import org.jboss.ws.core.soap.SOAPElementImpl;
//import org.jboss.ws.extensions.security.exception.InvalidSecurityHeaderException;
//import org.jboss.xb.binding.SimpleTypeBindings;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.constants.WSTrustConstants;
import org.picketlink.common.exceptions.fed.WSTrustException;
import org.picketlink.common.util.Base64;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.api.wstrust.WSTrustClient;
import org.picketlink.identity.federation.api.wstrust.WSTrustClient.SecurityInfo;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.factories.SAMLAssertionFactory;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLAssertionWriter;
import org.picketlink.identity.federation.core.wstrust.WSTrustUtil;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.writers.WSTrustRequestWriter;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.KeyInfoConfirmationDataType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationDataType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.ws.trust.CancelTargetType;
import org.picketlink.identity.federation.ws.trust.ClaimsType;
import org.picketlink.identity.federation.ws.trust.RenewTargetType;
import org.picketlink.identity.federation.ws.trust.UseKeyType;
import org.picketlink.identity.federation.ws.trust.ValidateTargetType;
import org.picketlink.identity.xmlsec.w3.xmldsig.KeyInfoType;
import org.picketlink.identity.xmlsec.w3.xmldsig.X509CertificateType;
import org.picketlink.identity.xmlsec.w3.xmldsig.X509DataType;
import org.picketlink.trust.jbossws.SAML2Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class STSOBOServiceClient {

	final static Logger logger = LoggerFactory.getLogger(STSOBOServiceClient.class);
	
	// ������������ ������ ��� setAppliesTo
	static String endpointURI = "https://acc.lan.iac.spb.ru:18443/CudServicesSTS/CUDSTS";
	
	//test-contur
	//static String wsdlLocationURI = "https://localhost:7443/CudServicesSTS/CUDSTS?wsdl";
	//+ � jboss-deployment-structure.xml
	
	//static String wsdlLocationURI = "https://acc.lan.iac.spb.ru:18443/CudServicesSTS/CUDSTS?wsdl";
	static String wsdlLocationURI =Configuration.getStsOboService();
	
	//static String wsdlLocationURI = "https://cudvm:18443/CudServicesSTS/CUDSTS?wsdl";
	//static String wsdlLocationURI = "https://192.168.12.16:8443/CudServicesSTS/CUDSTS?wsdl";
	

	  private static PublicKey publicKey = null;
	    
	    private static PrivateKey privateKey = null;
	    
	    private static  Certificate cert = null;
	    
	public  Document  sign_verify_soap_transform_2sign(String onBehalfOfToken)  throws Exception{
	
	
	   // �������� ������� usernametoken � header � soap
	   // � ������������� transform.
	   // ���� transform ����������� � ���������:
	/*   
	   <dsig:Signature xmlns:dsig='http://www.w3.org/2000/09/xmldsig#'>
	    <dsig:SignedInfo>
		     <dsig:Reference URI='#UsernameToken-1' xmlns:dsig='http://www.w3.org/2000/09/xmldsig#'>
				<dsig:Transforms xmlns:dsig='http://www.w3.org/2000/09/xmldsig#'>
					<dsig:Transform Algorithm='http://www.w3.org/2000/09/xmldsig#enveloped-signature' xmlns:dsig='http://www.w3.org/2000/09/xmldsig#'/>
	 				<dsig:Transform Algorithm='http://www.w3.org/2001/10/xml-exc-c14n#' xmlns:dsig='http://www.w3.org/2000/09/xmldsig#'/>
				</dsig:Transforms>
	*/
	   // ��� �������� ������� � ���� ����� Pre-digested input ���������� 
	   // �������������� ���������� ��� transform:
	/*   
	   [ru.CryptoPro.JCPxml.dsig.internal.dom.DOMReference] (http-0.0.0.0-80-1) Data class name: ru.CryptoPro.JCPxml.dsig.internal.dom.ApacheNodeSetData
	   [ru.CryptoPro.JCPxml.dsig.internal.dom.ApacheTransform] (http-0.0.0.0-80-1) Created transform for algorithm: http://www.w3.org/2000/09/xmldsig#enveloped-signature
	   [ru.CryptoPro.JCPxml.dsig.internal.dom.ApacheTransform] (http-0.0.0.0-80-1) Created transform for algorithm: http://www.w3.org/2000/09/xmldsig#enveloped-signature
	   [ru.CryptoPro.JCPxml.dsig.internal.dom.ApacheTransform] (http-0.0.0.0-80-1) ApacheData = true
	   [ru.CryptoPro.JCPxml.dsig.internal.dom.ApacheTransform] (http-0.0.0.0-80-1) ApacheData = true
	   [ru.CryptoPro.JCPxml.dsig.internal.dom.ApacheCanonicalizer] (http-0.0.0.0-80-1) Created transform for algorithm: http://www.w3.org/2001/10/xml-exc-c14n#
	   [ru.CryptoPro.JCPxml.dsig.internal.dom.ApacheCanonicalizer] (http-0.0.0.0-80-1) Created transform for algorithm: http://www.w3.org/2001/10/xml-exc-c14n#
	   [ru.CryptoPro.JCPxml.dsig.internal.dom.ApacheCanonicalizer] (http-0.0.0.0-80-1) ApacheData = true
	   [ru.CryptoPro.JCPxml.dsig.internal.dom.ApacheCanonicalizer] (http-0.0.0.0-80-1) ApacheData = true
	   [ru.CryptoPro.JCPxml.dsig.internal.DigesterOutputStream] (http-0.0.0.0-80-1) Pre-digested input:
	   [ru.CryptoPro.JCPxml.dsig.internal.DigesterOutputStream] (http-0.0.0.0-80-1) Pre-digested input:  
	  */  
	
	/*	
	   ���������� ����� ��������� ����������: 
       ���������� "������������� �����" �������� � ���� "���������� ������" ��� "������������ ������"; 
       ���������� "���������� ����" ����� �������� "�������� ����������� �������".
     */
	
		logger.info("STSServiceClient:sign_verify_soap_transform_2sign:01+");	  
		
		final ThreadLocal<Dispatch<SOAPMessage>> dispatchLocal = new InheritableThreadLocal(); 
		try{
			
			
            System.setProperty("javax.net.debug", "all"); 
		/*	
			System.setProperty("javax.net.ssl.trustStore", "/Development/cert/gost/client/bubnov2014.store");
			System.setProperty("javax.net.ssl.trustStoreType", "OCFStore");
			System.setProperty("javax.net.ssl.trustStorePassword", "1234567890");
			
		    System.setProperty("javax.net.ssl.keyStore", "@");
			System.setProperty("javax.net.ssl.keyStoreType", "OCFStore");
			System.setProperty("javax.net.ssl.keyStorePassword", "1234567890");
          */
			
          //System.setProperty("javax.net.ssl.trustStore", "/home/jboss/jboss/certstore/cudvm.store");
			System.setProperty("javax.net.ssl.trustStore", Configuration.getStorePath());
			System.setProperty("javax.net.ssl.trustStoreType", "HDImageStore");
			System.setProperty("javax.net.ssl.trustStorePassword", "Access_Control");
			
		    System.setProperty("javax.net.ssl.keyStore", "@");
			System.setProperty("javax.net.ssl.keyStoreType", "HDImageStore");
			System.setProperty("javax.net.ssl.keyStorePassword", "Access_Control");
			
		   //cxf2.7    
           //  System.setProperty("org.apache.cxf.stax.allowInsecureParser", "true");
         
			/*
			//test-contur
			HostnameVerifier hv = new HostnameVerifier() {
				public boolean verify(String urlHostName, SSLSession session) {
								 logger.info("Warning: URL Host: "+ urlHostName+ " vs. " + session.getPeerHost());
								 return true;
				  }
			};
			HttpsURLConnection.setDefaultHostnameVerifier(hv); 
           */
			
			String ServiceName = "CUDSTS";
			String PortName = "CUDSTSPort";
			
			
			//String soapBinding = "http://schemas.xmlsoap.org/wsdl/soap/http";
			String soapBinding = SOAPBinding.SOAP12HTTP_BINDING; 
			char[] signingKeyPass="Access_Control".toCharArray();
			String signingAlias="cudvm_export";
			
		 if(publicKey==null) {	
			KeyStore ks  = KeyStore.getInstance("HDImageStore", "JCP");
			ks.load(null, null);
			
			privateKey = (PrivateKey)ks.getKey(signingAlias, signingKeyPass);
			
			
			cert = ks.getCertificate(signingAlias);
		 	publicKey = cert.getPublicKey() ;
		 }
		 
		/* 	
			QName service = new QName("http://org.picketlink.trust/sts/", ServiceName);
			QName portName = new QName("http://org.picketlink.trust/sts/", PortName);
        	Service jaxwsService = Service.create(service);
		 */	
			//QName service = new QName("urn:picketlink:identity-federation:sts", ServiceName);
			//QName portName = new QName("urn:picketlink:identity-federation:sts", PortName);
			
			QName service = new QName("http://sts.services.cud.iac.spb.ru/", ServiceName);
			QName portName = new QName("http://sts.services.cud.iac.spb.ru/", PortName);
			
			
			URL wsdlLocation = new URL(wsdlLocationURI);
			
			Service jaxwsService = Service.create(wsdlLocation, service);
			
			//�� �������� � https - java7 - cxf
			//Service jaxwsService = Service.create(service);
			//jaxwsService.addPort(portName, soapBinding, endpointURI);
		
			//�� ����� :
			//���� - Service.create(wsdlLocation, service) � ��� addPort
			//���� - Service.create(service) � addPort
			//jaxwsService.addPort(portName2, soapBinding, endpointURI);
			
		    Dispatch dispatch = jaxwsService.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);
		    
		 //   dispatch.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://cudcudcud") ; 
		   
		  /*
		    //test-contur
		    HTTPConduit httpConduit = (HTTPConduit)  ((org.apache.cxf.jaxws.DispatchImpl)dispatch).getClient().getConduit() ; 
			//HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(proxy).getConduit();
           	TLSClientParameters tlsCP = new TLSClientParameters();
			final SSLSocketFactoryImpl sslFact = new SSLSocketFactoryImpl();
			tlsCP.setSSLSocketFactory(sslFact);
			tlsCP.setDisableCNCheck(true);
			httpConduit.setTlsClientParameters(tlsCP);
			*/
		    
		    dispatchLocal.set(dispatch);
		    
		//    logger.info("dispatch:02");
		    
		   // MessageFactory mf = MessageFactory.newInstance();
		    MessageFactory mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		 
		    SOAPMessage sm = mf.createMessage();
		    SOAPPart soapPart = sm.getSOAPPart();
		    SOAPHeader header = sm.getSOAPHeader();
		    SOAPBody body = sm.getSOAPBody();
		   
		    
		    
		    soapPart.getEnvelope().addNamespaceDeclaration("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		    soapPart.getEnvelope().addNamespaceDeclaration("ds", "http://www.w3.org/2000/09/xmldsig#");
		
		   // soapPart.getEnvelope().addNamespaceDeclaration("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");

		    body.addNamespaceDeclaration("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
		    body.setAttributeNS("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "wsu:Id","Body");
		  
		    
		    header.addNamespaceDeclaration("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
		    header.setAttributeNS("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "wsu:Id","Header");
		  
		    String system = "urn:sub-eis:employee:get";
		    String username = "tut";
		    String password = "tut";
		    String x509Cert = null;
		    
		    Document doc = sm.getSOAPPart();
		
		  //  logger.info("dispatch:03");
		    
		
		    // ��� ������������ Pre-digested input ���������� ������ <wsse:Username/>,
		    // �� ���� ������������� ������ <wsse:Username/>
		    // � ����� �� ����� ��������� �������� parentNode.replaceChild, �� ������� ���������� 
		    // <wsse:Username xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"/><wsse:Password xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"/>
		    // ����� �� � ������������. 
		    // � �� ������� ������� ��� �������� �������  Pre-digested input ������ ���������� 
		    //  <wsse:Username xmlns:wsse="http://..."/>, �� ���� ����������� ������ <wsse:Username xmlns:wsse="http://..."/>
		    // � ����� ����� �������� ����� �� SOAPElement ������������ UsernameSOAP.addNamespaceDeclaration.
		    
		    
		    QName SecurityQN = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security", "wsse");
			QName SystemTokenQN = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "UsernameToken", "wsse");
			QName UsernameTokenQN = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "UsernameToken", "wsse");
			QName UsernameCertQN = new QName("http://www.w3.org/2000/09/xmldsig#", "X509Certificate", "ds");
			
			
			QName SystemQN = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Username", "wsse");
			QName UsernameQN = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Username", "wsse");
			QName PasswordQN = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Password", "wsse");
			QName timestampQN = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Timestamp", "wsu");
			QName CreatedQN = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Created", "wsu");
			
			
			SOAPElement SecuritySOAP = header.addChildElement(SecurityQN);
			
			SecuritySOAP.addNamespaceDeclaration("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
			SecuritySOAP.setAttributeNS("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "wsu:Id","_id_sec");
			  
			
			//�������������� �������
			SOAPElement SystemTokenSOAP = SecuritySOAP.addChildElement(SystemTokenQN);
			SOAPElement SystemSOAP = SystemTokenSOAP.addChildElement(SystemQN);
			//�����������
			SystemSOAP.addNamespaceDeclaration("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
			//����������� ��������� id ��������� � ��������, ������� ����� �����������
			SystemTokenSOAP.setAttributeNS("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "wsu:Id", "SystemToken_1");
			SystemSOAP.addTextNode(system);
		
			
			//timestamp
            SOAPElement timestampSOAP = SecuritySOAP.addChildElement(timestampQN);
			SOAPElement CreatedSOAP = timestampSOAP.addChildElement(CreatedQN);
			//CreatedSOAP.addTextNode(SimpleTypeBindings.marshalDateTime(new GregorianCalendar(TimeZone.getTimeZone("UTC"))));
			//CreatedSOAP.addTextNode(SimpleTypeBindings.marshalDateTime(new GregorianCalendar()));
			CreatedSOAP.addTextNode(DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(new GregorianCalendar()).toXMLFormat());
			
			
			/*
			//�������������� ������������ �� �����/������
			SOAPElement UsernameTokenSOAP = SecuritySOAP.addChildElement(UsernameTokenQN);
			
			UsernameTokenSOAP.setAttributeNS("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "wsu:Id", "UsernameToken_1");
			
			SOAPElement UsernameSOAP = UsernameTokenSOAP.addChildElement(UsernameQN);
			SOAPElement PasswordSOAP = UsernameTokenSOAP.addChildElement(PasswordQN);
			
			UsernameSOAP.addNamespaceDeclaration("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
			PasswordSOAP.addNamespaceDeclaration("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
			PasswordSOAP.setAttribute("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
			UsernameSOAP.addTextNode(username);
			PasswordSOAP.addTextNode(password);
			*/
			
			/*
			//�������������� ������������ �� ����������
			SOAPElement UsernameCertSOAP = SecuritySOAP.addChildElement(UsernameCertQN);
			UsernameCertSOAP.setAttributeNS("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "wsu:Id", "x509CertToken_1");
			String user_cert_file = "/�������/��� ���������� 4.0/�����������/etoken/bubnovNewEtoken.cer";
		    InputStream user_inStream = new FileInputStream(user_cert_file);
            CertificateFactory user_cf = CertificateFactory.getInstance("X.509");
            X509Certificate user_cert = (X509Certificate)user_cf.generateCertificate(user_inStream);
            x509Cert = Base64.encodeBytes(user_cert.getEncoded());
		    UsernameCertSOAP.addTextNode(x509Cert);
			*/
		    
				
			
			
			
			
			//<env:Envelope xmlns:env="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ds="http://www.w3.org/2000/09/xmldsig#" xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"><env:Header wsu:Id="Header" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"><wsse:Security><wsse:UsernameToken wsu:Id="UsernameToken-1"><wsse:Username xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"/><wsse:Password xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"/></wsse:UsernameToken></wsse:Security></env:Header><env:Body wsu:Id="Body" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"/></env:Envelope>

			    
		  // logger.info("dispatch:05:"+DocumentUtil.asString(doc));
		   
		    org.apache.xml.security.Init.init();
		    
		     Provider xmlDSigProvider = new ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI();
		  
		     XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM", xmlDSigProvider);

	      /*  
		  // �������������� ��� ����������
			final Transforms transforms = new Transforms(doc);
			transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
			transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
			
			logger.info("dispatch:06:"+DocumentUtil.asString(doc));
		*/
			
			// �������������� ��� ������ SignedInfo
			List<Transform> transformList = new ArrayList<Transform>();
			Transform transform = fac.newTransform(Transform.ENVELOPED, (XMLStructure) null);
			Transform transformC14N = fac.newTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS, (XMLStructure) null);
			transformList.add(transform);
			transformList.add(transformC14N);
		
		// logger.info("dispatch:07");
			
		 
			//	 logger.info("dispatch:08");
			/*
			Node nodeToBeSigned=UsernameTokenSOAP;
			
			// ������������ ��� ���������� soap � �������� - ������ �������������� �������� �����������
			 Node parentNode = nodeToBeSigned.getParentNode();
			 
			 //������
			 Document newDoc = DocumentUtil.createDocument();
		     Node signingNode = newDoc.importNode(UsernameTokenSOAP, true);
			 newDoc.appendChild(signingNode);
			
			 
			 logger.info("dispatch:08_2:"+DocumentUtil.asString(newDoc));
			*/ 
			
				 
		/*		 
		    QName payloadName =
		    		   new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512", "RequestSecurityToken", "wst");

		    SOAPBodyElement payload = body.addBodyElement(payloadName); 

		    QName child1 =
		    		   new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512", "RequestType", "wst");

		    SOAPElement message1 = payload.addChildElement(child1);

		    message1.addTextNode("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue");

		    QName child2 =
		    		   new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512", "TokenType", "wst");

		    SOAPElement message2 = payload.addChildElement(child2);

		    message2.addTextNode("http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0");
*/
			
			 RequestSecurityToken request = new RequestSecurityToken();
			    
			    request.setContext("default-context");
			
			     //issue
			   request.setRequestType(URI.create("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue"));
			
			   
			     request.setTokenType(URI.create("http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0"));
			    
			    //add cert to RST for HOK
			 try{
			    	
			   //   logger.info("cud_sts:02_1");	
			    
			       
			      /*
			      String root_cert_file = "/�������/��� ���������� 4.0/�����������/������/cudvm_der.cer";
			  
			      InputStream inStream = new FileInputStream(root_cert_file);

			      CertificateFactory cf = CertificateFactory.getInstance("X.509");

			      X509Certificate cert_hok = (X509Certificate)cf.generateCertificate(inStream);

                  inStream.close();
			      */
			      
				 URI uri = new URI(WSTrustConstants.KEY_TYPE_PUBLIC); 
					
			      //!!!
			      Certificate cert_hok = cert;
			      
			    //  logger.info("cud_sts:03:"+cert_hok);	
			      
			         
			      byte[] value = Base64.encodeBytes(cert_hok.getEncoded()).getBytes();
			      
			     		  
	              UseKeyType useKeyType = new UseKeyType();

	              useKeyType.add(value);
	              
			     // request.setUseKey(useKeyType);
   		       //   request.setKeyType(uri);
			  
			      
			    }catch(Exception e){
			    	 logger.error("cud_sts:04:"+e);	
			    }
		
			    //!!! setAppliesTo
			   // request.setAppliesTo(WSTrustUtil.createAppliesTo(endpointURI));
			    
			  //  ClaimsType ct = null;
			 //   request.setClaims(ct);
			    
			 //!!! OnBehalfOf
				request.setOnBehalfOf(WSTrustUtil.createOnBehalfOfWithUsername(onBehalfOfToken, "ID_1"));
			    
			    DOMSource requestSource = createSourceFromRequest(request);
			    body.addDocument((Document)requestSource.getNode());
			    
			    
/*			    
try{

 Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    // ����������� ����������� XML-��������� � �����
    transformer.transform(new DOMSource(requestSource.getNode()), new StreamResult(System.out));
}catch(Exception e){
}*/
			    
		  
		   
			    //��� �����������
		   
		      //  Reference ref1 = fac.newReference("#UsernameToken-1", fac.newDigestMethod("http://www.w3.org/2001/04/xmldsig-more#gostr3411", null) 
		       // 		,transformList, null, null);

		        Reference ref1 = fac.newReference("#Header", fac.newDigestMethod("http://www.w3.org/2001/04/xmldsig-more#gostr3411", null) 
		        		,transformList, null, null);
		        Reference ref2 = fac.newReference("#Body", fac.newDigestMethod("http://www.w3.org/2001/04/xmldsig-more#gostr3411", null) 
		        		,transformList, null, null);
		        List<Reference> referenceList = new ArrayList<Reference>();
		        
		        referenceList.add(ref1);
		        referenceList.add(ref2);
		        
		    //    logger.info("dispatch:06");
		        
				SignedInfo si = fac.newSignedInfo( fac.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE,
						 								(C14NMethodParameterSpec) null),
						 						   fac.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#gostr34102001-gostr3411", null),
						 						  referenceList);

				KeyInfoFactory kif = fac.getKeyInfoFactory();
				
				KeyValue kv = kif.newKeyValue(publicKey);
				KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));
				
				//X509Data x509d = kif.newX509Data(Collections.singletonList((X509Certificate) cert));
				//KeyInfo ki = kif.newKeyInfo(Collections.singletonList(x509d));
				
		
				
			javax.xml.crypto.dsig.XMLSignature sig = fac.newXMLSignature(si, ki);
			
				//���� ��������� �������
				//DOMSignContext signContext = new DOMSignContext(privateKey, newDoc.getDocumentElement()); 
				DOMSignContext signContext = new DOMSignContext(privateKey, SecuritySOAP); 
				
				signContext.putNamespacePrefix(XMLSignature.XMLNS, "dsig");
				
				//�������� ��������� id � ������������� ��������
				//����� ������������� �� ���� ��������� Pre-digested input � ����
			
				signContext.setIdAttributeNS(body, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
				signContext.setIdAttributeNS(header, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
			
			
				
			//	 logger.info("dispatch:011");
				    
		    sig.sign(signContext);
		    
		  //  Pre-digested input: <wsse:UsernameToken wsu:Id="UsernameToken-1"><wsse:Username xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"></wsse:Username><wsse:Password xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"></wsse:Password></wsse:UsernameToken>
		    
		   // logger.info("dispatch:011+:"+DocumentUtil.asString(newDoc));
		  //  logger.info("dispatch:011+:"+DocumentUtil.asString(SecuritySOAP.getOwnerDocument()));
		    
		    // <wsse:UsernameToken xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" wsu:Id="UsernameToken-1"><wsse:Username xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"/><wsse:Password xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"/><dsig:Signature xmlns:dsig="http://www.w3.org/2000/09/xmldsig#"><dsig:SignedInfo><dsig:CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/><dsig:SignatureMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#gostr34102001-gostr3411"/><dsig:Reference URI="#UsernameToken-1"><dsig:Transforms><dsig:Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/><dsig:Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/></dsig:Transforms><dsig:DigestMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#gostr3411"/><dsig:DigestValue>CjgZhMuDCKWfzsPLhwI6m72TuatCNeoaZP90a5etMEA=</dsig:DigestValue></dsig:Reference></dsig:SignedInfo><dsig:SignatureValue>pwGfBx3dXoaFC0hnnaCH4ZAfBuPI8aAYXZq6vQEdY8AUoHeiELjuuim7RaBp8thnkJ5LtMjNm714l8A+CIp7Cg==</dsig:SignatureValue><dsig:KeyInfo><dsig:KeyValue><dsig:GOSTKeyValue><dsig:PublicKey>MGMwHAYGKoUDAgITMBIGByqFAwICJAAGByqFAwICHgEDQwAEQCunDBpRlzBS7ROt4BrFULCojjzROAkng89j1UOqRoIzpIdrgbHb5F5IxyxprcTPu2gDuCLhSvNCk5nEP5jdx80=</dsig:PublicKey></dsig:GOSTKeyValue></dsig:KeyValue></dsig:KeyInfo></dsig:Signature></wsse:UsernameToken>

		   
		    
		//	 logger.info("dispatch_verify_1!");
			 
			 //----------------------------------------------------------------
			 // �������� �������
			 
         // Node signatureNode1 = newDoc.getFirstChild().getLastChild();
          Node signatureNode1 = SecuritySOAP.getLastChild();	 
          
		//	 logger.info("dispatch:011_4:"+signatureNode1.getNodeName());
			 
			 DOMValidateContext valContext1 = new DOMValidateContext(publicKey, signatureNode1);
			 valContext1.putNamespacePrefix(XMLSignature.XMLNS, "dsig");
			 
				 
			// valContext1.setIdAttributeNS((Element)newDoc.getFirstChild(), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
			    
			 valContext1.setIdAttributeNS(body, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
			 valContext1.setIdAttributeNS(header, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
				
			 
		    javax.xml.crypto.dsig.XMLSignature signature1 = fac.unmarshalXMLSignature(valContext1);
	        
		    	boolean result1 = signature1.validate(valContext1);
				 
		    	 logger.info("dispatch:011_5:"+result1);
		    	 
				 
		    //	 logger.info("dispatch:012:"+DocumentUtil.asString(doc));
		    	 
		    	// Pre-digested input: <wsse:UsernameToken wsu:Id="UsernameToken-1"><wsse:Username xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"></wsse:Username><wsse:Password xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"></wsse:Password></wsse:UsernameToken>

		    	 
		    	
		    	//----------------------------------------------------------------
				// ���������� soap - ������ �������������� �������� �����������
		    /*	 
		    	 Node signedNode_soap = doc.importNode(newDoc.getFirstChild(), true);
		    	 parentNode.replaceChild(signedNode_soap, nodeToBeSigned);	 
		    	*/ 
		    	// logger.info("dispatch:013:"+DocumentUtil.asString(doc));
		    	
		    	 //<env:Envelope xmlns:env="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ds="http://www.w3.org/2000/09/xmldsig#" xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"><env:Header wsu:Id="Header" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"><wsse:Security><wsse:UsernameToken wsu:Id="UsernameToken-1"><wsse:Username xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"/><wsse:Password xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"/><dsig:Signature xmlns:dsig="http://www.w3.org/2000/09/xmldsig#"><dsig:SignedInfo><dsig:CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#" xmlns:dsig="http://www.w3.org/2000/09/xmldsig#"/><dsig:SignatureMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#gostr34102001-gostr3411" xmlns:dsig="http://www.w3.org/2000/09/xmldsig#"/><dsig:Reference URI="#UsernameToken-1" xmlns:dsig="http://www.w3.org/2000/09/xmldsig#"><dsig:Transforms xmlns:dsig="http://www.w3.org/2000/09/xmldsig#"><dsig:Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature" xmlns:dsig="http://www.w3.org/2000/09/xmldsig#"/><dsig:Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#" xmlns:dsig="http://www.w3.org/2000/09/xmldsig#"/></dsig:Transforms><dsig:DigestMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#gostr3411" xmlns:dsig="http://www.w3.org/2000/09/xmldsig#"/><dsig:DigestValue xmlns:dsig="http://www.w3.org/2000/09/xmldsig#">CjgZhMuDCKWfzsPLhwI6m72TuatCNeoaZP90a5etMEA=</dsig:DigestValue></dsig:Reference></dsig:SignedInfo><dsig:SignatureValue>pwGfBx3dXoaFC0hnnaCH4ZAfBuPI8aAYXZq6vQEdY8AUoHeiELjuuim7RaBp8thnkJ5LtMjNm714l8A+CIp7Cg==</dsig:SignatureValue><dsig:KeyInfo><dsig:KeyValue xmlns:dsig="http://www.w3.org/2000/09/xmldsig#"><dsig:GOSTKeyValue xmlns:dsig="http://www.w3.org/2000/09/xmldsig#"><dsig:PublicKey xmlns:dsig="http://www.w3.org/2000/09/xmldsig#">MGMwHAYGKoUDAgITMBIGByqFAwICJAAGByqFAwICHgEDQwAEQCunDBpRlzBS7ROt4BrFULCojjzROAkng89j1UOqRoIzpIdrgbHb5F5IxyxprcTPu2gDuCLhSvNCk5nEP5jdx80=</dsig:PublicKey></dsig:GOSTKeyValue></dsig:KeyValue></dsig:KeyInfo></dsig:Signature></wsse:UsernameToken></wsse:Security></env:Header><env:Body wsu:Id="Body" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"><wst:RequestSecurityToken xmlns:wst="http://docs.oasis-open.org/ws-sx/ws-trust/200512"><wst:RequestType>http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue</wst:RequestType><wst:TokenType>http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0</wst:TokenType></wst:RequestSecurityToken></env:Body></env:Envelope>
  //  sm.writeTo(System.out);
		    	
		    SOAPMessage reply = (SOAPMessage) ((Dispatch)dispatchLocal.get()).invoke(sm);
		    
		  //  logger.info("dispatch:014");
		    
		    //�������� ��������� ������
		    
		    
		    SOAPHeader soapHeader_reply =  reply.getSOAPHeader();
		    SOAPBody soapBody_reply =  reply.getSOAPBody();
		    
		  //signature
	    	 NodeList signatureList_reply = soapHeader_reply.getElementsByTagNameNS("*", "Signature");
	    	 
	    	 if(signatureList_reply==null||signatureList_reply.getLength()==0){
	    		 logger.info("TestServerCryptoSOAPHandler:handleMessage:02_1");
			        throw new Exception("This service requires <dsig:Signature>, which is missing!!!");
	    	 }
	    	 
	    	  PublicKey publicKey_reply = null;
	    	  
	    	 //X509Certificate
	    	 NodeList x509CertificateList_reply = ((Element)signatureList_reply.item(0)).getElementsByTagNameNS("*", "X509Certificate");
	    	 
	    	 if(x509CertificateList_reply!=null&&x509CertificateList_reply.getLength()>0){
	    		 logger.info("TestServerCryptoSOAPHandler:handleMessage:02_2");
			      //  throw new InvalidSecurityHeaderException("This service requires <dsig:X509Certificate>, which is missing!!!");
	    	 
	    	 
            // SOAPElementImpl sei_x509Cert = (SOAPElementImpl)x509CertificateList_reply.item(0);
	    	// String x509Cert_Value = sei_x509Cert.getValue();
	    	 
	    	 String x509Cert_Value = x509CertificateList_reply.item(0).getTextContent();
	    	 
             byte [] byteX509Certificate = Base64.decode(x509Cert_Value);
	    	 
	    	 X509Certificate cert_reply = null;
	    	 
	    	 CertificateFactory cf = CertificateFactory.getInstance("X.509");
	         ByteArrayInputStream bais = new ByteArrayInputStream(byteX509Certificate);

	         while (bais.available() > 0)
	           cert_reply = (X509Certificate)cf.generateCertificate(bais);
	         
	        // logger.info("GostKeyHandler:handleRequestType:04:"+cert_reply);
	         
	       
	         if(cert_reply!=null){
	        	 publicKey_reply=cert_reply.getPublicKey();
	         }

	       //  logger.info("publicKey_reply:"+publicKey_reply);
	         
	    	 }
	    	 
	    	// 2-� ������� - GOSTKeyValue
	    	 if(publicKey_reply==null){
	    		 
		    	 NodeList GOSTKeyList_reply = ((Element)signatureList_reply.item(0)).getElementsByTagNameNS("*", "GOSTKeyValue");
		    	 
		    	 if(GOSTKeyList_reply==null||GOSTKeyList_reply.getLength()==0){
		    		 logger.info("TestServerCryptoSOAPHandler:handleMessage:02_3");
				        throw new Exception("This service requires <dsig:X509Certificate> or <dsig:GOSTKeyValue>, which is missing!!!");
		    	 }
		    	 
                 NodeList publicKeyList_reply = ((Element)signatureList_reply.item(0)).getElementsByTagNameNS("*", "PublicKey");
		    	 
		    	 if(publicKeyList_reply==null||publicKeyList_reply.getLength()==0){
		    		 logger.info("TestServerCryptoSOAPHandler:handleMessage:02_4");
				        throw new Exception("This service requires <dsig:PublicKey>, which is missing!!!");
		    	 }
		    	 
		    	// SOAPElementImpl sei_PublKey_reply = (SOAPElementImpl)publicKeyList_reply.item(0);
    	    	// String base64PublKey_reply = sei_PublKey_reply.getValue();
    	    	
    	    	 String base64PublKey_reply = publicKeyList_reply.item(0).getTextContent();
    	    	 
    	    	 
		       //  logger.info("GostKeyHandler:handleRequestType:05_reply:"+base64PublKey_reply);
		    
		         byte [] bytePublKey_reply = Base64.decode(base64PublKey_reply);
		    
		         KeyFactory keyFactory_reply = KeyFactory.getInstance("GOST3410");
			     EncodedKeySpec publicKeySpec_reply = new X509EncodedKeySpec(bytePublKey_reply);
			     publicKey_reply = keyFactory_reply.generatePublic(publicKeySpec_reply);
	    	 }
	    	 
	    	 if(publicKey_reply==null){
		    	 
	    		    throw new Exception("Public key is null!!!");
	    	}
	    	 
		      Node signatureNode1_reply =signatureList_reply.item(0);
	         
	        // logger.info("TestServerCryptoSOAPHandlerTransform:handleMessage:09_2:"+signatureNode1_reply.getNodeName());
	     
	         
	        DOMValidateContext valContext1_reply = new DOMValidateContext(publicKey_reply, signatureNode1_reply);
	        
	        valContext1_reply.putNamespacePrefix(XMLSignature.XMLNS, "dsig");
	        
	         
	      
			   // valContext1.setIdAttributeNS(newDoc.getDocumentElement(), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
		    
			   
			valContext1_reply.setIdAttributeNS(soapHeader_reply, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
			valContext1_reply.setIdAttributeNS(soapBody_reply, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
	  
			    
		       javax.xml.crypto.dsig.XMLSignature signature1_reply = fac.unmarshalXMLSignature(valContext1_reply);
	        
		    	boolean result1_reply = signature1_reply.validate(valContext1_reply);
				 
		    	 logger.info("STSServiceClient:sign_verify_soap_transform_2sign:70:validate:"+result1_reply);
	    	 
               NodeList assertionList = soapBody_reply.getElementsByTagNameNS(JBossSAMLURIConstants.ASSERTION_NSURI.get(), "Assertion");
		    	 
               if(assertionList==null||assertionList.getLength()==0){
		    		 logger.info("STSServiceClient:sign_verify_soap_transform_2sign:71");
				        throw new Exception("This service requires <Assertion>, which is missing!!!");
		    	 }
               
               Document newDocAssertion = DocumentUtil.createDocument();
               Node nodeAssertion = newDocAssertion.importNode(assertionList.item(0), true);
               newDocAssertion.appendChild(nodeAssertion);
               
               String stAssertion = DocumentUtil.asString(newDocAssertion);
		    	
              // return stAssertion;
               return newDocAssertion;
		    	 
		}catch(Exception e){
			logger.error("STSServiceClient:sign_verify_soap_transform_2sign:error:"+e);	  
			e.printStackTrace(System.out);
			throw e;
		}
	}



	 private static DOMSource createSourceFromRequest(RequestSecurityToken request) throws WSTrustException {
		    try {
		      DOMResult result = new DOMResult(DocumentUtil.createDocument());
		      WSTrustRequestWriter writer = new WSTrustRequestWriter(result);
		      writer.write(request);
		      return new DOMSource(result.getNode());
		     } catch (Exception e) {
		    	 throw new WSTrustException(e);
		    }
		  
		  }
	 public static void propagateIDAttributeSetup(Node sourceNode, Element destElement)
	  {
	    NamedNodeMap nnm = sourceNode.getAttributes();
	    for (int i = 0; i < nnm.getLength(); i++) {
	      Attr attr = (Attr)nnm.item(i);
	      if (attr.isId()) {
	        destElement.setIdAttribute(attr.getName(), true);
	        break;
	      }
	    }
	  }
  
}
