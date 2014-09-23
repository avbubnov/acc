package ru.spb.iac.cud.uarm.ws;

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
import org.slf4j.Logger;
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


public class AuditServiceClient {

	final static Logger logger = LoggerFactory.getLogger(AuditServiceClient.class);
	
	//@Logger private Log log;
	
	//test-contur
	//static String endpointURI ="https://localhost:7443/CudServicesPro/AuditService?wsdl";
	//+ в jboss-deployment-structure.xml
	
	//static String endpointURI ="https://acc.lan.iac.spb.ru:18443/CudServicesPro/AuditService?wsdl";
	static String endpointURI =Configuration.getAuditService();
	
	//static String endpointURI ="https://cudvm:18443/CudServicesPro/AuditService?wsdl";
	//static String endpointURI ="https://192.168.12.16:8443/CudServicesPro/AuditService?wsdl";
	
	private AuditService accessServices= null;
	


   public void audit(String login, List<AuditFunction> funccodes) throws Exception{
	 try{ 	 
	   
	   logger.info("auditServicesClient:audit:01");
		
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
				
	   TokenInstall ti = new TokenInstall();
	   
	   String samlAssertion = ti.get_assertion() ;
	   
	//   logger.info("auditServicesClient:audit:02:"+samlAssertion);
	   
	   getPort(endpointURI, samlAssertion).audit(login, funccodes);
	 
	//   logger.info("auditServicesClient:audit:03");
	   
	 }catch(GeneralFailure e1){
		 logger.error("auditServicesClient:audit:error1:"+e1.getMessage());
     }catch(Exception e2){
		 logger.error("auditServicesClient:audit:error2:"+e2.getMessage());
		 e2.printStackTrace(System.out);
     }
	}
   
   public void sync_functions(List<Function> functions) throws Exception{
		
   	try{
   	
   	  String samlAssertion = null;
   		
   	  logger.info("auditServicesClient:sync_functions:01");
   	  
   	  getPort(endpointURI, samlAssertion).sync_functions(functions, "ADD");
   	  
   	}catch(GeneralFailure e1){
      	  logger.error("auditServicesClient:sync_functions:error1:"+e1);
    }catch(Exception e2){
		  logger.error("auditServicesClient:sync_functions:error2:"+e2);
	}
   }
   
 
	private  AuditService getPort(String endpointURI, String samlAssertion) throws MalformedURLException   {
		
		//logger.info("auditServicesClient:getPort:01");
		if(accessServices==null){
			 
		   logger.info("getPort:02");
		   
		   QName serviceName = new QName("http://audit.services.cud.iac.spb.ru/", "AuditServiceImplService");
		   URL wsdlURL = new URL(endpointURI);

		   Service service = Service.create(wsdlURL, serviceName);
		   
		   QName portName = new QName("http://audit.services.cud.iac.spb.ru/", "AuditServiceImplPort");
		 
	       accessServices=service.getPort(portName, AuditService.class);
	       
	         
	      /*//test-contur
	      // HTTPConduit httpConduit = (HTTPConduit)  ((org.apache.cxf.jaxws.DispatchImpl)dispatch).getClient().getConduit() ; 
			HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(accessServices).getConduit();
			TLSClientParameters tlsCP = new TLSClientParameters();
		    final SSLSocketFactoryImpl sslFact = new SSLSocketFactoryImpl();
			tlsCP.setSSLSocketFactory(sslFact);
			tlsCP.setDisableCNCheck(true);
			httpConduit.setTlsClientParameters(tlsCP);
	   */
			
	      //  Element assertion = get_saml_assertion_from_xml(samlAssertion);
	        Element assertion = get_saml_assertion_from_xml_simple(samlAssertion);
	        
		   BindingProvider bp = (BindingProvider) accessServices;
		    bp.getRequestContext().put(SAML2Constants.SAML2_ASSERTION_PROPERTY, assertion);
		    List<Handler> handlers = bp.getBinding().getHandlerChain();
		    handlers.add(new SAML2Handler());
		    handlers.add(new TestClientCryptoSOAPHandler());
		    bp.getBinding().setHandlerChain(handlers);
		  }
		
		//logger.info("auditServicesClient:getPort:03");
		
		  return accessServices;
		// return service.getPort(portName, AccessServices.class);
		 }
	
	private static Element get_saml_assertion_from_xml_simple(String samlAssertion){
		
		logger.info("AuditServiceClient:get_saml_assertion_from_xml_simple:01");
		 
		Element result = null;
		try{
			//!!!нельзя так просто
			//нужно именно setNamespaceAware(true)
			//DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        	//!!!
			dbf.setNamespaceAware(true);
			
			DocumentBuilder db = dbf.newDocumentBuilder();
		

			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(samlAssertion));

			Document ass_doc = db.parse(is);
			
			result=ass_doc.getDocumentElement();
			
			logger.info("AuditServiceClient:get_saml_assertion_from_xml_simple:02:"+DocumentUtil.asString(ass_doc));
		       
			
		}catch(Exception e){
			logger.error("AuditServiceClient:get_saml_assertion_from_xml_simple:error:"+e);
		}
		return result;
	}	
	
private static Element get_saml_assertion_from_xml(String samlAssertion){
		
		Element result = null;
		InputStream samlAssertionInputStream = null;
				
		try{
		
		 logger.info("AuditServiceClient:get_saml_assertion_from_xml:01");
		 
		 if(samlAssertion!=null){
			 
		   samlAssertionInputStream = new ByteArrayInputStream(samlAssertion.getBytes("UTF-8"));
		 //  logger.info("AuditServiceClient:get_saml_assertion_from_xml:01_1");
		   
		 }else{
			 
		  // samlAssertionInputStream = new FileInputStream("/home/jboss/jboss/data/saml/saml_asserion.xml");
		   samlAssertionInputStream = new FileInputStream(Configuration.getSamlAssertion());
			
		   
		 }
		   
		   
		 //  logger.info("AuditServiceClient:get_saml_assertion_from_xml:01_3");
		   
	        SAMLParser samlParser = new SAMLParser();

	        Object parsedObject = samlParser.parse(samlAssertionInputStream);

	       // logger.info("AuditServiceClient:get_saml_assertion_from_xml:02:"+(parsedObject==null));
	      
	     //   logger.info("AuditServiceClient:get_saml_assertion_from_xml:03:"+parsedObject.getClass());
	       
	       // cast the parsed object to the expected type, in this case AssertionType
	        AssertionType assertionType = (AssertionType) parsedObject;

	        //Важно!!!
	        SubjectConfirmationType sct = assertionType.getSubject().getConfirmation().get(0);
	        SubjectConfirmationDataType  scdt = sct.getSubjectConfirmationData();
	        KeyInfoConfirmationDataType type = new KeyInfoConfirmationDataType();
	        type.setAnyType(scdt.getAnyType());		
	        sct.setSubjectConfirmationData(type);
	        
	        logger.info("AuditServiceClient:get_saml_assertion_from_xml:04+:"+AssertionUtil.hasExpired(assertionType));
	        
	        
	      // тут может возникнуть NullPointerException
	      // при парсинге строки в AssertionType через picketlink api
	      // и без ручной установки sct.setSubjectConfirmationData(type) 
	      // в TokenInstall.load();  
	       
	         byte[] bb = (( X509CertificateType) ( (X509DataType )((KeyInfoType)assertionType.getSubject().getConfirmation().get(0).getSubjectConfirmationData().getAnyType()).getContent().get(0)).getDataObjects().get(0)).getEncodedCertificate();
            
          //  logger.info("AuditServiceClient:get_saml_assertion_from_xml:04_1:"+new String(bb));
     
	        
	        Document ass_doc = AssertionUtil.asDocument(assertionType);
	     
	     //   logger.info("AuditServiceClient:get_saml_assertion_from_xml:04_1");
	        		
	       // result = ass_doc.getDocumentElement();
	        
	       result = SAMLUtil.toElement(assertionType);
	        
	       logger.info("AuditServiceClient:get_saml_assertion_from_xml:05:"+DocumentUtil.asString(ass_doc));
	        
	        // let's write the parsed assertion to the sysout
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();

	        SAMLAssertionWriter writer = new SAMLAssertionWriter(StaxUtil.getXMLStreamWriter(baos));

	        writer.write(assertionType);

	        
	    //    logger.info(new String(baos.toByteArray()));
		  			      
	  }catch(Exception e3){
			 logger.error("get_saml_assertion_from_xml:error:"+e3);
			 e3.printStackTrace(System.out);
	  }
		return result;
	}
}
