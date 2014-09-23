package iac.cud.infosweb.ws;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import org.apache.xml.security.transforms.Transforms;
import org.picketlink.identity.federation.core.saml.v2.factories.SAMLAssertionFactory;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class TestClientCryptoSOAPHandler implements SOAPHandler<SOAPMessageContext>{


	  private static PublicKey publicKey = null;
	    
	    private static PrivateKey privateKey = null;
	    
 //@Override
 public Set<QName> getHeaders() {
    return null;
 }

 //@Override
 public void close(MessageContext mc) {
 }

 //@Override
 public boolean handleFault(SOAPMessageContext mc) {
    return true;
 }

 //@Override
 public boolean handleMessage(SOAPMessageContext mc) {
	 
	 System.out.println("TestClientCryptoSOAPHandler:handleMessage:01:"+mc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));
	
	 
	 
	 try{
			SOAPMessage sm = mc.getMessage();
			//sm.writeTo(System.out);
	    
		    SOAPHeader header = sm.getSOAPHeader();
		    SOAPBody body = sm.getSOAPBody();
		 
		    char[] signingKeyPass="Access_Control".toCharArray();
			String signingAlias="cudvm_export";
		
		 if(publicKey==null) {
			KeyStore ks  = KeyStore.getInstance("HDImageStore", "JCP");
			ks.load(null, null);
			
			privateKey = (PrivateKey)ks.getKey(signingAlias, signingKeyPass);
			
			Certificate cert = ks.getCertificate(signingAlias);
		 	publicKey = cert.getPublicKey() ;
		 }
		//запрос 
	  if (Boolean.TRUE.equals(mc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))) {
    	
    	
		
		

		
	 	org.apache.xml.security.Init.init();
	    
	    Provider xmlDSigProvider = new ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI();
	  
	    XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM", xmlDSigProvider);

	     
	    Node SecuritySOAP = header.getFirstChild();
	    
	   // System.out.println("TestClientCryptoSOAPHandler:handleMessage:02:"+SecuritySOAP.getLocalName());
	    
	    
	    body.addNamespaceDeclaration("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
	    body.setAttributeNS("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "wsu:Id","Body");
	  
	    
	    header.addNamespaceDeclaration("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
	    header.setAttributeNS("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "wsu:Id","Header");
	
	    
	 	
	    List<Transform> transformList = new ArrayList<Transform>();
		Transform transform = fac.newTransform(Transform.ENVELOPED, (XMLStructure) null);
		Transform transformC14N = fac.newTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS, (XMLStructure) null);
		transformList.add(transform);
		transformList.add(transformC14N);
		
		
		 Reference ref1 = fac.newReference("#Header", fac.newDigestMethod("http://www.w3.org/2001/04/xmldsig-more#gostr3411", null) 
	        		,transformList, null, null);
	        Reference ref2 = fac.newReference("#Body", fac.newDigestMethod("http://www.w3.org/2001/04/xmldsig-more#gostr3411", null) 
	        		,transformList, null, null);
	        List<Reference> referenceList = new ArrayList<Reference>();
	        
	        referenceList.add(ref1);
	        referenceList.add(ref2);
	        
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

			//куда вставлять подпись
			//DOMSignContext signContext = new DOMSignContext(privateKey, newDoc.getDocumentElement()); 
			DOMSignContext signContext = new DOMSignContext(privateKey, SecuritySOAP); 
			
			signContext.putNamespacePrefix(XMLSignature.XMLNS, "dsig");
			
			//фиксация аттрибута id в подписываемом элементе
			//место ответственное за факт появления Pre-digested input в логе
		
			signContext.setIdAttributeNS(body, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
			signContext.setIdAttributeNS(header, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
		
		
			
			// System.out.println("TestClientCryptoSOAPHandler:handleMessage:03");
			    
	    sig.sign(signContext);
	  	    
	  	    
    	/*
    	sm.getSOAPPart().getEnvelope().addNamespaceDeclaration("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		
    	WSSecHeader header = new WSSecHeader();
	    header.setActor("http://smev.gosuslugi.ru/actors/smev");
	    header.setMustUnderstand(false);
	    
	    Element sec = header.insertSecurityHeader(sm.getSOAPPart());
	    
	    Element saml_assertion = createSAML();
	    
	    
	    Element token = (Element) sec.appendChild(saml_assertion);
    	*/
	    
	   // System.out.println("TestClientCryptoSOAPHandler:handleMessage:04");
	    
    }else{
    //ответ
    	
    	//System.out.println("TestClientCryptoSOAPHandler:handleMessage:05");
    	
    	 Provider xmlDSigProvider = new ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI();
   	  
    	 XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM", xmlDSigProvider);
        	
    	 Node securityNode = header.getFirstChild(); 
    	 
    	 if(securityNode==null){
    		 System.out.println("TestClientCryptoSOAPHandler:handleMessage:06");
    	        throw new Exception("This service requires <wsse:Security>, which is missing!!!");
    	 }
    	 
    	 NodeList securityNodeChilds = securityNode.getChildNodes();

    	 Node signatureNode1 = null;
    	 
    	 for (int i = 0; i<securityNodeChilds.getLength(); i++) {
    		 
    		// System.out.println("TestClientCryptoSOAPHandler:handleMessage:07:"+securityNodeChilds.item(i).getLocalName());
    		 
    		 if(securityNodeChilds.item(i).getLocalName()!=null&&securityNodeChilds.item(i).getLocalName().equals("Signature")){
    			 signatureNode1 = securityNodeChilds.item(i);
    		 }
    	 }
    	 
    	// System.out.println("TestClientCryptoSOAPHandler:handleMessage:08");
    	 
         //signature
    	 
    	 if(signatureNode1==null){
    		 System.out.println("TestClientCryptoSOAPHandler:handleMessage:09");
    	        throw new Exception("This service requires <dsig:Signature>, which is missing!!!");
    	 }

    	
         
       //  System.out.println("TestClientCryptoSOAPHandler:handleMessage:010:"+signatureNode1.getNodeName());
     
         
        DOMValidateContext valContext1 = new DOMValidateContext(publicKey, signatureNode1);
        
        valContext1.putNamespacePrefix(XMLSignature.XMLNS, "dsig");
       	 
    	valContext1.setIdAttributeNS(header, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
    	valContext1.setIdAttributeNS(body, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
      
    		    
    	       javax.xml.crypto.dsig.XMLSignature signature1 = fac.unmarshalXMLSignature(valContext1);
         
    	    	boolean result1 = signature1.validate(valContext1);
    			 
    	    	 System.out.println("TestClientCryptoSOAPHandler:handleMessage:010+:"+result1);

    	
    }

	 } catch (Exception e) {
		 System.out.println("TestClientCryptoSOAPHandler:handleMessage:error:"+e);
         throw new ProtocolException(e);
     }
    return true;
 }
 
  

}
