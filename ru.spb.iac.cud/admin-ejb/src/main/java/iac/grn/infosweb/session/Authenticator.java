package iac.grn.infosweb.session;


import static org.picketlink.common.util.StringUtil.isNotNull;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;

import iac.cud.authmodule.dataitem.*; 

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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

import org.jboss.seam.faces.FacesMessages;
import iac.grn.infosweb.session.navig.LinksMap;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.xb.binding.SimpleTypeBindings;

import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.apache.xml.security.transforms.Transforms;

import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.util.Base64;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.common.util.StringUtil;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.holders.DestinationInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLRequestWriter;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType.STSubType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.LogoutRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.RequestAbstractType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.CryptoPro.JCPxml.xmldsig.JCPXMLDSigInit;
import ru.spb.iac.crypto.export.Crypto15Init;
import ru.spb.iac.cud.core.util.HashPassword;
import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.InvalidCredentials;
import ru.spb.iac.cud.items.Attribute;
import ru.spb.iac.cud.items.Role;


/*import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.seam.federation.SeamSamlPrincipal;*/

import iac.cud.authmodule.session.AutzManagerLocal;
import iac.cud.authmodule.session.CUDAuthManagerLocal;
import iac.cud.infosweb.entity.*;
import iac.cud.infosweb.ws.AccessServiceClient;
/**
 * ����������� ���, ������������� �� �������������� ������������� � 
 * �������� ���� ������� � ��������� ��������
 * @author bubnov
 *
 */
@Name("authenticator")
public class Authenticator
{
    @Logger private Log log;

    //@In Identity identity;
    /**
     * ������������� �������� � ������� ������������ ��� ����������� 
     * �������������� 
     */
    @In Credentials credentials;

    /**
     * �������� ���������, �������������� �������������� � ��
     */
    @In EntityManager entityManager;

    /**
     * ������������� �������� ������������ 
     * ��� �������� ���� �������
     */
    @In(required = false,scope=ScopeType.SESSION)
	private AcUser currentUser;

    /**
     * ������������� �������� ������������ �������� 
     * ��� �������� ���� �������
     */
    @In
	LinksMap linksMap;
    
    public static final String ROLE_SYS_ADMIN_CUD = "role:urn:sys_admin_cud";
    
    public static final String CIPHER_DATA_LOCALNAME = "CipherData";

    public static final String ENCRYPTED_KEY_LOCALNAME = "EncryptedKey";

    public static final String DS_KEY_INFO = "ds:KeyInfo";

    public static final String XMLNS = "http://www.w3.org/2000/xmlns/";

    public static String XMLSIG_NS = "http://www.w3.org/2000/09/xmldsig#";

    public static String XMLENC_NS = "http://www.w3.org/2001/04/xmlenc#";
    
    private static final String CIPHER_ALG = "GOST28147/CFB/NoPadding";
    
    private static final String CIPHER_ALG_TRANSPORT = "GostTransport";
    
   // public boolean access(){
    public boolean access(String pageCode) throws Exception{
    	try{
    		AuthItem ai=(AuthItem)Component.getInstance("authItem", ScopeType.SESSION);
    		return ai.getPageList().containsKey(pageCode);
    	}catch(Exception e){
    	 log.error("authenticator:access:error:"+e);
    		// e.printStackTrace(System.out);
    	  throw e;
    	}
     }
    /**
     * �������� ���� ������� � �������������� ������� 
     * �� ������ �������
     * @param pageCode ������������� ������
     * @return ������������ ������/��������
     * @throws Exception
     */
    public boolean access_(String pageCode) throws Exception{
    	
    	try{
  //  	 log.info("authenticator:access:pageCode:"+pageCode);
  //  	 log.info("authenticator:access:acApplication:"+linksMap.getAppCode());
  //  	 log.info("authenticator:access:idUser:"+currentUser.getIdUser());
    	 List<AcRole> alist = entityManager.createQuery(
                 "select ar " +
                 "from AcUser au, AcRole ar, AcLinkUserToRoleToRaion aur, " +
                 "AcLinkRoleAppPagePrmssn arp, AcAppPage ap where " +
                 "aur.acRole is not null and aur.acRole.idRol=ar.idRol and " +
                 "aur.acUser is not null and aur.acUser.idUser=au.idUser and " +
                 "arp.acRole is not null and arp.acRole.idRol=ar.idRol and " +
                 "arp.acAppPage is not null and arp.acAppPage.idRes=ap.idRes and " +
                 "ap.pageCode=:pageCode and " +
                 "ap.acApplication=:acApplication and " +
                 "au.idUser=:idUser " )
                 .setParameter("pageCode", pageCode)
                 .setParameter("acApplication", linksMap.getAppCode())
                 .setParameter("idUser", currentUser.getIdUser())
                 .getResultList();
  
    //	 log.info("authenticator:access:isEmpty:"+alist.isEmpty());
     	 if(alist.isEmpty()){
     		return false;
     	 }
    	}catch(Exception e){
    		 log.error("authenticator:access:error"+e);
    		// e.printStackTrace(System.out);
    		 throw e;
    	}
    	return true;
    }
   public boolean accessPerm(String pageCode, String permCode) throws Exception{
	//   log.info("authenticator:accessPerm:01:pageCode:"+pageCode);
	//   log.info("authenticator:accessPerm:01:permCode:"+permCode);
	  try{
   		AuthItem ai=(AuthItem)Component.getInstance("authItem",ScopeType.SESSION);
   		PageItem pi = ai.getPageList().get(pageCode);
   		if(pi==null){
   			return false;
   		}
   	 /*   for(String st: pi.getPermList()){
   	       log.info("authenticator:accessPerm:st"+st);
   	    }*/
   		return pi.getPermList().contains(permCode);
   	  }catch(Exception e){
   	  log.error("authenticator:accessPerm:error"+e);
   		// e.printStackTrace(System.out);
   	  throw e;
   	 }
    }
    /**
     * �������� ���� ������� � �������������� ������� 
     * �� ������ ������������� ����������
     * @param pageCode ������������� ������
     * @param permCode ������������� ����������
     * @return  ������������ ������/��������
     * @throws Exception
     */
   public boolean accessPerm_(String pageCode, String permCode) throws Exception{
    	
    	try{
    //	 log.info("authenticator:accessPerm:pageCode:"+pageCode);
    //	 log.info("authenticator:accessPerm:permCode:"+permCode);
    // 	 log.info("authenticator:accessPerm:acApplication:"+linksMap.getAppCode());
    //	 log.info("authenticator:accessPerm:idUser:"+currentUser.getIdUser());
    	 List<AcRole> alist = entityManager.createQuery(
                 "select ar " +
                 "from AcUser au, AcRole ar, AcLinkUserToRoleToRaion aur, " +
                 "AcLinkRoleAppPagePrmssn arp, AcAppPage ap, AcPermissionsList aperm where " +
                 "aur.acRole is not null and aur.acRole.idRol=ar.idRol and " +
                 "aur.acUser is not null and aur.acUser.idUser=au.idUser and " +
                 "arp.acRole is not null and arp.acRole.idRol=ar.idRol and " +
                 "arp.acAppPage is not null and arp.acAppPage.idRes=ap.idRes and " +
                 "arp.acPermissionsList is not null and arp.acPermissionsList.idPerm=aperm.idPerm and " +
                 "ap.pageCode=:pageCode and " +
                 "aperm.idPerm=:idPerm and " +
                 "ap.acApplication=:acApplication and " +
                 "au.idUser=:idUser " )
                 .setParameter("pageCode", pageCode)
                 .setParameter("idPerm", new Long(permCode))
                 .setParameter("acApplication", linksMap.getAppCode())
                 .setParameter("idUser", currentUser.getIdUser())
                 .getResultList();
  
  //  	 log.info("authenticator:accessPerm:isEmpty:"+alist.isEmpty());
     	 if(alist.isEmpty()){
     		return false;
     	 }
    	}catch(Exception e){
    		 log.error("authenticator:access:error"+e);
    		// e.printStackTrace(System.out);
    		 throw e;
    	}
    	return true;
    }
   public boolean authenticate()
   {
	   
	   log.info("authenticator:authenticate:01");
	   
	   char[] signingKeyPass="Access_Control".toCharArray();
		 String signingAlias="cudvm_export";
		 
	   try{
		/* Context ctx=new InitialContext();
		  CUDAuthManagerLocal aml=(CUDAuthManagerLocal)ctx.lookup("cudAuthModule.CUDAuthManager.local");
		  AuthItem ai =aml.authCompleteItem(linksMap.getAppCode(), credentials.getUsername(), credentials.getPassword());
		 */ 
		   
		   
		  String pSAMLResponse = FacesContext.getCurrentInstance().getExternalContext()
           .getRequestParameterMap()
           .get("SAMLResponse"); 
		  
		 // System.out.println("authenticator:authenticate:01_1:"+pSAMLResponse);
		  
		    KeyStore ks  = KeyStore.getInstance("HDImageStore", "JCP");
			ks.load(null, null);
			
			PrivateKey privateKey = (PrivateKey)ks.getKey(signingAlias, signingKeyPass);
		
			PublicKey publicKey = ks.getCertificate("cudvm_export").getPublicKey();
		  
			Document  samlDocument= get_saml_assertion_from_response(pSAMLResponse); 	
			
			//������� ����� ��������� ���������
			//�.�. idp ������� �������, ����� �����������
			//���������� ������ assertion
			//���������� ���� ��� ���������� assertion
			AssertionType ass = decrypt(samlDocument, privateKey);
			
			System.out.println("authenticator:authenticate:01_2");
			
			//�������� ��������� Assertion
			boolean ass_valid = ass_valid(samlDocument, publicKey);
			
			System.out.println("authenticator:authenticate:01_3:"+ass_valid);
			
			if(ass_valid==false){
	    		  throw new Exception("Signature Assertion is not valid!!!");
	    	 }
			
			//�������� ���������
			
			 Provider xmlDSigProvider = new ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI();
		   	  
	    	 XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM", xmlDSigProvider);
	        	
	    	 System.out.println("test1:01_1");
	    	 
	    	 configureIdAttribute(samlDocument);
	    	 
	    	 
	    	// propagateIDAttributeSetup(samlDocument.getDocumentElement(), samlDocument.getDocumentElement());

	         NodeList nl = samlDocument.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
	         if (nl == null || nl.getLength() == 0) {
	        	 throw new Exception("Cannot find Signature element");
	         }
	    	 
	    		 
	    	 Node signatureNode1 = nl.item(0);
	    		 
	    	 System.out.println("SignValidateSOAPHandler:handleMessage:07_3");
	    	 
	         //signature
	    	 
	    	 if(signatureNode1==null){
	    		 System.out.println("SignValidateSOAPHandler:handleMessage:08");
	    	        throw new Exception("This service requires <dsig:Signature>, which is missing!!!");
	    	 }

	    	 System.out.println("SignValidateSOAPHandler:handleMessage:09:"+signatureNode1.getNodeName());
	     
	         DOMValidateContext valContext1 = new DOMValidateContext(publicKey, signatureNode1);
	        
	     //   valContext1.putNamespacePrefix(XMLSignature.XMLNS, "dsig");
	       	 
	    	//valContext1.setIdAttributeNS(header, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
	    	//valContext1.setIdAttributeNS(body, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
	      
	    	 javax.xml.crypto.dsig.XMLSignature signature1 = fac.unmarshalXMLSignature(valContext1);
	         
	    	  boolean result1 = signature1.validate(valContext1);
	    			 
	    	  System.out.println("SignValidateSOAPHandler:010+:"+result1);
			
	    	  if(result1==false){
	    		  throw new Exception("Signature is not valid!!!");
	    	  }
	    	  
			//������ ���������� ������
	    	boolean  expiredAssertion = AssertionUtil.hasExpired(ass);
			
	    	 System.out.println("expiredAssertion:"+expiredAssertion);
	    	/* 
	    	 System.out.println("Authenticator:authenticate:01:"+(SimpleTypeBindings.marshalDateTime(new GregorianCalendar(TimeZone.getTimeZone("UTC")))));
			 System.out.println("Authenticator:authenticate:02:"+(SimpleTypeBindings.marshalDateTime(new GregorianCalendar())));
			 System.out.println("Authenticator:authenticate:03:"+(new Date()));
			 System.out.println("Authenticator:authenticate:04:"+(TimeZone.getDefault()));
	    	*/
	    	if(expiredAssertion){
	    		  throw new Exception("expiredAssertion!!!");
	    	  }
	    	
	    	
	    	 SubjectType subject = ass.getSubject();
	            /*
	             * JAXBElement<NameIDType> jnameID = (JAXBElement<NameIDType>) subject.getContent().get(0); NameIDType nameID =
	             * jnameID.getValue();
	             */
	            if (subject == null)
	                throw new Exception("Subject in the assertion");

	            STSubType subType = subject.getSubType();
	            if (subType == null)
	                throw new Exception("Unable to find subtype via subject");
	            NameIDType nameID = (NameIDType) subType.getBaseID();

	            if (nameID == null)
	                throw  new Exception("Unable to find username via subject");

	            final String userName = nameID.getValue();
	            
	            System.out.println("userName:"+userName);
	            
	            
	            List<String> roles = new ArrayList<String>();
	            Map<String, String> userAttrib = new HashMap<String, String>(); 
	            
	            
	            Set<StatementAbstractType> statements = ass.getStatements();
	            for (StatementAbstractType statement : statements) {
	                if (statement instanceof AttributeStatementType) {
	                    AttributeStatementType attributeStatement = (AttributeStatementType) statement;
	                    roles.addAll(getRoles(attributeStatement, userAttrib));
	                }
	            }

	            System.out.println("Authenticator:70");
	    	
	          
	            
	    /*	
		  String pTokenID = FacesContext.getCurrentInstance().getExternalContext()
		             .getRequestParameterMap()
		             .get("tokenID"); 
		  
		  String success = FacesContext.getCurrentInstance().getExternalContext()
		             .getRequestParameterMap()
		             .get("success"); 
		  
		  String welcome_form = FacesContext.getCurrentInstance().getExternalContext()
		             .getRequestParameterMap()
		             .get("welcome_form");
		  
		  log.info("authenticator:authenticate:pTokenID:"+pTokenID); 
		  log.info("authenticator:authenticate:success:"+success);
		  log.info("authenticator:authenticate:welcome_form:"+welcome_form);
		  
		  
		  if(welcome_form==null && (success==null||success.equals("false"))){
			  FacesMessages.instance().add("������������ �� ���������������!");
			  return false;
		  }
		  
		  AccessServiceClient asc = (AccessServiceClient)Component.getInstance("asClient",ScopeType.EVENT);
		  
		  //if(pTokenID==null){
		  if(welcome_form!=null){ //����� �����-������
		    try{
		    	
		    	if((credentials.getUsername()==null||credentials.getUsername().trim().equals(""))||
		    		(credentials.getPassword()==null||credentials.getPassword().trim().equals(""))){
		    		 FacesMessages.instance().add("���� ����� � ������ �����������!");
					  return false;
		    	}
		    	
			   tokenID = asc.authenticate_login(credentials.getUsername(), credentials.getPassword());
		    }catch(InvalidCredentials e1){
			   log.info("authenticator:authenticate:02");
			   FacesMessages.instance().add("������������ �� ���������������!");
			   return false;
	         }
		  }else{//����� ���������� 
			   //pTokenID ������ ����, ��� ����� �� ������ ��� ������� success=true
			  tokenID = pTokenID;
		  }
		  */
		  //List<Role> roleList = asc.authorize(tokenID);
		  
		  if(roles.isEmpty()){
		 // if(roleList.isEmpty()){
			  log.info("authenticator:authenticate:03");
			  FacesMessages.instance().add("��� ���� ������� � �������!");
			  return false;
		  }
		 /*
		  List<String> roleStList = new ArrayList<String>();
		  List<String> rolesInfoList = new ArrayList<String>();
		  for(Role rl :roleList){
			  roleStList.add(rl.getIdRole());
			  rolesInfoList.add(rl.getName());
		  }*/
		  List<String> roleStList = roles;
		  List<String> rolesInfoList = roles;
				  
		  Context ctx=new InitialContext();
		 // AutzManagerLocal aml=(AutzManagerLocal)ctx.lookup("cudAuthModule.AutzManager.local");
		  AutzManagerLocal aml=(AutzManagerLocal)ctx.lookup("java:global/CUDAuthModule/AutzManager!iac.cud.authmodule.session.AutzManagerLocal");
		  
		  AuthItem ai =aml.getAccessComplete(linksMap.getAppCode(), roleStList);
		 
		  if(ai==null){
			  log.info("authenticator:authenticate:04");
			  FacesMessages.instance().add("������ �������!");
			  return false;
		  }
		  Contexts.getSessionContext().set("authItem", ai);
		  
		 
		  
		  String fio = null, org =null, tel = null, email = null, 
				 login = null, orgIOGVCode=null, uid = null;
		  String base64TokenID = null;
		  /*
		  List<Attribute> attribList = asc.attrib(tokenID);
		  
		  for(Attribute at :attribList){
			  if(at.getName().equals("fio")){
				  fio=at.getValue();
			  }else if(at.getName().equals("org")){
				  org=at.getValue();
			  }else if(at.getName().equals("tel")){
				  tel=at.getValue();
			  }else if(at.getName().equals("email")){
				  email=at.getValue();
			  }else if(at.getName().equals("login")){
				  login=at.getValue();
			  }else if(at.getName().equals("orgIOGVCode")){
				  orgIOGVCode=at.getValue();
			  }
		  }*/
		  
		  fio = userAttrib.get("USER_FIO");
		  org = userAttrib.get("ORG_NAME");
		  tel = userAttrib.get("USER_PHONE");
		  email = userAttrib.get("USER_EMAIL");
		  login = userAttrib.get("USER_LOGIN");
		  orgIOGVCode = userAttrib.get("ORG_CODE_IOGV");	
		  uid = userAttrib.get("USER_UID");
		  base64TokenID = userAttrib.get("TOKEN_ID");
		  
		  System.out.println("Authenticator:71:"+fio);
		  
		  if(base64TokenID!=null){
		  
			  System.out.println("Authenticator:72:"+base64TokenID);
			  
			  byte[] byteTokenID = Base64.decode(base64TokenID);
	          
			  String tokenID = new String (byteTokenID, "UTF-8");
			  
			  System.out.println("Authenticator:73:"+tokenID);
			  
			  String[] arrTokenID = tokenID.split("_");
			  
			  if(arrTokenID==null||arrTokenID.length!=3){
				 //UserAuthToken is not valid!!!
				  
			  }else{
			  
			  
				  StringBuilder sb = new StringBuilder();
				  
				  sb.append(arrTokenID[0]+"_"+arrTokenID[1]);
				  
				  byte[] sigValue = Base64.decode(arrTokenID[2]);
				  
				  boolean tokenIDValidateResult = GOSTSignatureUtil.validate(sb.toString().getBytes("UTF-8"), sigValue, publicKey);
				  
				  System.out.println("Authenticator:74:"+tokenIDValidateResult);
				  
				  System.out.println("Authenticator:75:"+new Date(Long.parseLong(arrTokenID[1])));
			  
			  }
		  }
		  //--------------------------------------------------
		  
		  AcUser currentUser = new AcUser();
		  
		 // currentUser.setTokenID(tokenID);
		  
		  currentUser.setFio(fio);
		  currentUser.setOrgName(org);
		  currentUser.setPhone(tel);
		  currentUser.setEmail(email);
		  currentUser.setLogin(login);
		 
		  currentUser.setUpSign(orgIOGVCode);
		  
		  currentUser.setRolesInfoList(rolesInfoList);
		  
		  /*if(accessPerm("0052","1")){
			  currentUser.setIsSysAdmin(1L);
		  }*/
		  //����������
		  //�.�. ��� ������� ������� ����� 0052(��������� �������)
		  //������ ������ �������� ���� ��� ������ � �������
		  //� �� ��� ���������� ���� ���� ������� ������ �� ����������� � ����
		  
		  if(roles.contains(ROLE_SYS_ADMIN_CUD)){
			  currentUser.setIsSysAdmin(1L);
		  }
		  
		  //��������� ����
		  //� ���������� ������� �� �������� ������� � ������������ ����������� ����� ����
		  /*Long idUser = (Long) entityManager
				  .createQuery("select au.idUser from AcUser au " +
				  		        "where au.login= :login")
				  .setParameter("login", login)		        
                  .getSingleResult();
				  ;
		  currentUser.setIdUser(idUser);
		  */
		  currentUser.setIdUser(new Long(uid));
		  
		  //currentUser.setTokenID(pTokenID)
		  
		  //����������� ����������������� �� ��
		  //������ ������ ������������ ��� ����� ��
		  //� ������ ������� - ����������� ���������� 
		  //��� ������� �������� ������������ � �� 
		  //� ������� �����������������
		  if(!roles.contains(ROLE_SYS_ADMIN_CUD)){
			  
			  List<Long> allowedSys = allowedSys(new Long(uid));
			  
			  if(allowedSys!=null){
				  
			    log.info("authenticator:authenticate:05");
				
			    currentUser.setAllowedSys(allowedSys);
			    
			    List<String> allowedReestr = allowedReestr(new Long(uid));
				  
				  if(allowedReestr!=null&&!allowedReestr.isEmpty()){
					  
				    log.info("authenticator:authenticate:05_1");
					
				    currentUser.setAllowedReestr(allowedReestr);
				    
				    }
			  }else{
			  }
			  
			  //�������� ��� - ����� �� ������������ ������������ ���� 
			  //� ����� ��, � ���������� ������� �������� �����������
			  //������ - ��� ����������� �����������, ������ - �����.
			  
			  if(isAccOrgManager(new Long(uid))){
				  currentUser.setIsAccOrgManager(1L);
				  
			  }
			  
			  
		  }else{	  
		  }
		  
		  
		  
		  
		  
		  Contexts.getSessionContext().set("currentUser", currentUser);
		  
		  //��� �������� ������ �� ��������� ������
		  try{
		    HttpSession session = (HttpSession)  FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		    //session.setAttribute("auditExportToken", tokenID);
		    //session.setAttribute("auditExportLogin", login);
		    session.setAttribute("auditExportUid", uid);
		    }catch(Exception e){
		    	 log.error("authenticator:authenticate:session.setAttribute:Error:"+e);
		   	}
	
		  return true;
	   }catch(Exception e){
		 log.error("authenticator:authenticate:Error+:"+e);
		 
		 e.printStackTrace(System.out);
		 
      	 FacesMessages.instance().add("������ �������!");
         return false;
	   }
	}
   
   public void test(){
	   log.info("authenticator:test:!!!");
   }
   
   public void cudAuth() throws Exception{
	   
	  char[] signingKeyPass="Access_Control".toCharArray();
		String signingAlias="cudvm_export";
		 
   	try{
   	 log.info("authenticator:cudAuth:01");
  	 
   	 HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
   	 HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    	
   	 
   	 //String destinat ion = "http://10.128.66.140:8080/cudidp/";
   	 String destination = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/cudidp/login";
   
   	 String assertionConsumerServiceURL = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/infoscud/public.seam";
   	 
   	 log.info("authenticator:cudAuth:01+:"+request.getContextPath());
   	 
   	 /*
   //  String backUrl = "https://cudvm/infoscud/back.jsp";
     String backUrl = "http://cudvm/infoscud/public.seam";
	 String destination = "http://cudvm/CudServicesWeb/AccessServicesWebGeneral";
	// String destination = "http://cudvm/CudServicesWeb/AccessServicesWebLogin";
	 String pswitch = "false";
	 
     response.sendRedirect(destination+"?switch="+pswitch+"&backUrl="+backUrl);
  */
   
   	
   	KeyStore ks  = KeyStore.getInstance("HDImageStore", "JCP");
	ks.load(null, null);
	
	PrivateKey privateKey = (PrivateKey)ks.getKey(signingAlias, signingKeyPass);

	PublicKey publicKey = ks.getCertificate("cudvm_export").getPublicKey();
	
	// System.out.println("test1:02:"+privateKey.getAlgorithm());
	  
   	Document samlDocument = get_saml_assertion_from_xml(assertionConsumerServiceURL, privateKey, publicKey);
   	 
    Node nextSibling = getNextSiblingOfIssuer(samlDocument);
	 
	 String samlMessage = PostBindingUtil.base64Encode(DocumentUtil.getDocumentAsString(samlDocument));
	 String samlMessage2 = Base64.encodeBytes(DocumentUtil.getDocumentAsString(samlDocument).getBytes("UTF-8"), Base64.DONT_BREAK_LINES);
	
	 
	// System.out.println("test1:03_1:"+samlMessage);
	// System.out.println("test1:03_2:"+samlMessage2);
	 
	  Provider xmlDSigProvider = new ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI();
	  
	//  System.out.println("test1:03_3");
	  
	   XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM", xmlDSigProvider);

	//   System.out.println("test1:03_4");
	 //������������ ����� ������������� �����-�� ���� � ���������
	  //����������� ���� ����, �������� ������ ��������, � ���� ���������� ���� ����
	  // � ����� propagateIDAttributeSetup() �� ����, ������� ���� ��������� � ����� ����������� id  
	 //   propagateIDAttributeSetup(samlDocument, samlDocument.getDocumentElement());
	 	
	    List<Transform> transformList = new ArrayList<Transform>();
		Transform transform = fac.newTransform(Transform.ENVELOPED, (XMLStructure) null);
		Transform transformC14N = fac.newTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS, (XMLStructure) null);
		transformList.add(transform);
		transformList.add(transformC14N);
		
		 String id = samlDocument.getDocumentElement().getAttribute("ID");
		 String referenceURI = "#" + id;
		 
		 Reference ref1 = fac.newReference(referenceURI, fac.newDigestMethod("http://www.w3.org/2001/04/xmldsig-more#gostr3411", null) 
	        		,transformList, null, null);
	      List<Reference> referenceList = new ArrayList<Reference>();
	        
	        referenceList.add(ref1);
	        
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
//!!!
	  	  
	  	  configureIdAttribute(samlDocument);
	  	  
	  	
			//���� ��������� �������
	  	    //nextSibling - ����� ��� ����� ��������� �������
	  	    //nextSibling ��� <saml:Issuer> - ��� <samlp:NameIDPolicy>
	  	    //� Signature ����� ����� <saml:Issuer> � <samlp:NameIDPolicy>
			//DOMSignContext signContext = new DOMSignContext(privateKey, newDoc.getDocumentElement()); 
			DOMSignContext signContext = new DOMSignContext(privateKey, samlDocument.getDocumentElement(), nextSibling); 
			
			signContext.putNamespacePrefix(XMLSignature.XMLNS, "dsig");
			
			//�������� ��������� id � ������������� ��������
			//����� ������������� �� ���� ��������� Pre-digested input � ����
		
		// ������ ����� ������������ configureIdAttribute(samlDocument);	
		//	signContext.setIdAttributeNS(body, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
		//	signContext.setIdAttributeNS(header, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
		
		
			 
		//	 System.out.println("test1:04");
			    
	    sig.sign(signContext);
	
	 
	 //   System.out.println("test1:05:"+DocumentUtil.asString(samlDocument));
	    
	      
	 byte[] responseBytes = DocumentUtil.getDocumentAsString(samlDocument).getBytes("UTF-8");

	// String samlResponse = PostBindingUtil.base64Encode(new String(responseBytes));

	 String samlRequest=Base64.encodeBytes(new String(responseBytes).getBytes("UTF-8"), Base64.DONT_BREAK_LINES);
	 
	// System.out.println("test1:06:"+samlRequest);
   
   	 
	 sendPost(samlRequest, destination, response);
	 
	// System.out.println("test1:07");
	 
   	}catch(Exception e){
   	 log.error("authenticator:cudAuth:error:"+e);
   		// e.printStackTrace(System.out);
   	  throw e;
   	}
    }
 
   public void cudLogout(String login) throws Exception{
	   
		  char[] signingKeyPass="Access_Control".toCharArray();
			String signingAlias="cudvm_export";
			 
	   	try{
	   	 log.info("authenticator:cudLogout:01");
	  	 
	   	 HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
	   	 HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
	    	
	   	 
	   	 //String destinat ion = "http://10.128.66.140:8080/cudidp/";
	   	 String destination = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/cudidp/logout";
	   
	   	 String logoutBackUrl = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/infoscud/";
	   	 
	   	 log.info("authenticator:cudLogout:02:"+destination);
	   	 log.info("authenticator:cudLogout:03:"+logoutBackUrl);
	   	
	   	 /*
	   //  String backUrl = "https://cudvm/infoscud/back.jsp";
	     String backUrl = "http://cudvm/infoscud/public.seam";
		 String destination = "http://cudvm/CudServicesWeb/AccessServicesWebGeneral";
		// String destination = "http://cudvm/CudServicesWeb/AccessServicesWebLogin";
		 String pswitch = "false";
		 
	     response.sendRedirect(destination+"?switch="+pswitch+"&backUrl="+backUrl);
	  */
	   
	   	
	   	KeyStore ks  = KeyStore.getInstance("HDImageStore", "JCP");
		ks.load(null, null);
		
		PrivateKey privateKey = (PrivateKey)ks.getKey(signingAlias, signingKeyPass);

		PublicKey publicKey = ks.getCertificate("cudvm_export").getPublicKey();
		
		// System.out.println("test1:02:"+privateKey.getAlgorithm());
		  
	   	Document samlDocument = get_saml_logout_from_xml(logoutBackUrl, login);
	   	
	   	log.info("authenticator:cudLogout:04:"+DocumentUtil.asString(samlDocument));
	   	
	    Node nextSibling = getNextSiblingOfIssuer(samlDocument);
		 
		 String samlMessage = PostBindingUtil.base64Encode(DocumentUtil.getDocumentAsString(samlDocument));
		 String samlMessage2 = Base64.encodeBytes(DocumentUtil.getDocumentAsString(samlDocument).getBytes("UTF-8"), Base64.DONT_BREAK_LINES);
		
		 
		// System.out.println("test1:03_1:"+samlMessage);
		// System.out.println("test1:03_2:"+samlMessage2);
		 
		  Provider xmlDSigProvider = new ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI();
		  
		//  System.out.println("test1:03_3");
		  
		   XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM", xmlDSigProvider);

		//   System.out.println("test1:03_4");
		 //������������ ����� ������������� �����-�� ���� � ���������
		  //����������� ���� ����, �������� ������ ��������, � ���� ���������� ���� ����
		  // � ����� propagateIDAttributeSetup() �� ����, ������� ���� ��������� � ����� ����������� id  
		 //   propagateIDAttributeSetup(samlDocument, samlDocument.getDocumentElement());
		 	
		    List<Transform> transformList = new ArrayList<Transform>();
			Transform transform = fac.newTransform(Transform.ENVELOPED, (XMLStructure) null);
			Transform transformC14N = fac.newTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS, (XMLStructure) null);
			transformList.add(transform);
			transformList.add(transformC14N);
			
			 String id = samlDocument.getDocumentElement().getAttribute("ID");
			 String referenceURI = "#" + id;
			 
			 Reference ref1 = fac.newReference(referenceURI, fac.newDigestMethod("http://www.w3.org/2001/04/xmldsig-more#gostr3411", null) 
		        		,transformList, null, null);
		      List<Reference> referenceList = new ArrayList<Reference>();
		        
		        referenceList.add(ref1);
		        
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
	//!!!
		  	  
		  	  configureIdAttribute(samlDocument);
		  	  
		  	
				//���� ��������� �������
		  	    //nextSibling - ����� ��� ����� ��������� �������
		  	    //nextSibling ��� <saml:Issuer> - ��� <samlp:NameIDPolicy>
		  	    //� Signature ����� ����� <saml:Issuer> � <samlp:NameIDPolicy>
				//DOMSignContext signContext = new DOMSignContext(privateKey, newDoc.getDocumentElement()); 
				DOMSignContext signContext = new DOMSignContext(privateKey, samlDocument.getDocumentElement(), nextSibling); 
				
				signContext.putNamespacePrefix(XMLSignature.XMLNS, "dsig");
				
				//�������� ��������� id � ������������� ��������
				//����� ������������� �� ���� ��������� Pre-digested input � ����
			
			// ������ ����� ������������ configureIdAttribute(samlDocument);	
			//	signContext.setIdAttributeNS(body, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
			//	signContext.setIdAttributeNS(header, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
			
			
				 
			//	 System.out.println("test1:04");
				    
		    sig.sign(signContext);
		
		    log.info("authenticator:cudLogout:05:"+DocumentUtil.asString(samlDocument));
		 //   System.out.println("test1:05:"+DocumentUtil.asString(samlDocument));
		    
		      
		 byte[] responseBytes = DocumentUtil.getDocumentAsString(samlDocument).getBytes("UTF-8");

		// String samlResponse = PostBindingUtil.base64Encode(new String(responseBytes));

		 String samlRequest=Base64.encodeBytes(new String(responseBytes).getBytes("UTF-8"), Base64.DONT_BREAK_LINES);
		 
		// System.out.println("test1:06:"+samlRequest);
	   
		 log.info("authenticator:cudLogout:06");
		 
		 sendPost(samlRequest, destination, response);
		 
		 log.info("authenticator:cudLogout:07");
		 
		// System.out.println("test1:07");
		 
	   	}catch(Exception e){
	   	 log.error("authenticator:cudLogout:error:"+e);
	   		// e.printStackTrace(System.out);
	   	  throw e;
	   	}
	    }
   
   private static Document get_saml_assertion_from_xml(String assertionConsumerServiceURL, PrivateKey privateKeyClient, PublicKey publicKeyCUD){
		
		 Document result = null;
		 InputStream samlAssertionInputStream = null;	
			try{
			
			 System.out.println("get_saml_assertion_from_xml:01");
			 
			   samlAssertionInputStream = new FileInputStream("/home/jboss/jboss/data/saml/saml_request_post.xml");

			 //  System.out.println("get_saml_assertion_from_xml:01_2+");
			   
		        SAMLParser samlParser = new SAMLParser();
		        
		       // System.out.println("get_saml_assertion_from_xml:01_3");
		        
		        Object parsedObject = samlParser.parse(samlAssertionInputStream);

		      //  System.out.println("get_saml_assertion_from_xml:01_4");
		        
		        // cast the parsed object to the expected type, in this case AssertionType
		        AuthnRequestType authn = (AuthnRequestType) parsedObject;
		        
		      //  System.out.println("get_saml_assertion_from_xml:01_5+:"+new URI(assertionConsumerServiceURL));
		        
		        //!!! ������ ���������
		        authn.setAssertionConsumerServiceURL(new URI(assertionConsumerServiceURL));
		        
		        
		        //external passive auth
		        String SAMLdestination = getSAMLDestination(privateKeyClient, publicKeyCUD);
		        
		        authn.setDestination(URI.create(SAMLdestination));		        
		        
		        SAML2Request  sr = new SAML2Request();
		        
		       Document doc_authn = sr.convert(authn);
		     
		      
		       result = doc_authn;
		        
		     //  System.out.println("get_saml_assertion_from_xml:01_2:"+DocumentUtil.asString(doc_authn));
			  		      
		  }catch(Exception e3){
				 System.out.println("get_saml_assertion_from_xml:error:"+e3);
				 e3.printStackTrace(System.out);
		  }finally{
			  try{
				  if(samlAssertionInputStream!=null){
					  samlAssertionInputStream.close();
				  }
			  }catch(Exception e){
				  
			  }
		  }
			return result;
		}
   
   private static Document get_saml_logout_from_xml(String logoutBackUrl, String login){
		
		 Document result = null;
		 InputStream samlAssertionInputStream = null;	
		
			try{
			
			 System.out.println("get_saml_logout_from_xml:01");
			 
			   samlAssertionInputStream = new FileInputStream("/home/jboss/jboss/data/saml/saml_request_logout_post.xml");

			 //  System.out.println("get_saml_assertion_from_xml:01_2+");
			   
		        SAMLParser samlParser = new SAMLParser();
		        
		       // System.out.println("get_saml_assertion_from_xml:01_3");
		        
		        Object parsedObject = samlParser.parse(samlAssertionInputStream);

		      //  System.out.println("get_saml_assertion_from_xml:01_4");
		        
		        // cast the parsed object to the expected type, in this case AssertionType
		          
		        LogoutRequestType logout = (LogoutRequestType) parsedObject;
		        
		      //  System.out.println("get_saml_assertion_from_xml:01_5+:"+new URI(assertionConsumerServiceURL));
		        
		        
		        //external passive auth
		        String SAMLdestination = logout.getDestination().toString()+
		        		                 "?logoutBackUrl="+URLEncoder.encode(logoutBackUrl, "utf-8");
		        
		        logout.setDestination(URI.create(SAMLdestination));		        
		        
		        NameIDType nameID = new NameIDType();
		        	
		        
		        nameID.setValue(login);
		        
		        logout.setNameID(nameID);
		        
		        SAML2Request  sr = new SAML2Request();
		        
		       Document doc_authn = sr.convert(logout);
		     
		      
		       result = doc_authn;
		        
		     //  System.out.println("get_saml_assertion_from_xml:01_2:"+DocumentUtil.asString(doc_authn));
			  		      
		  }catch(Exception e3){
				 System.out.println("get_saml_logout_from_xml:error:"+e3);
				 e3.printStackTrace(System.out);
		  }finally{
			  try{
				  if(samlAssertionInputStream!=null){
					  samlAssertionInputStream.close();
				  }
			  }catch(Exception e){
				  
			  }
		  }
			return result;
		}
   
   public static Node getNextSiblingOfIssuer(Document doc) {
       
		//  System.out.println("getNextSiblingOfIssuer:01");
				  
		    NodeList nl = doc.getElementsByTagNameNS(JBossSAMLURIConstants.ASSERTION_NSURI.get(), JBossSAMLConstants.ISSUER.get());
		   // "urn:oasis:names:tc:SAML:2.0:assertion", "Issuer"
	        if (nl.getLength() > 0) {
	            Node issuer = nl.item(0);

	          //  System.out.println("getNextSiblingOfIssuer:02:"+issuer.getNextSibling().getLocalName());
	            return issuer.getNextSibling();
	        }
	        return null;
	    }

   private static void configureIdAttribute(Document document) {
      // Estabilish the IDness of the ID attribute.
       document.getDocumentElement().setIdAttribute("ID", true);

       NodeList nodes = document.getElementsByTagNameNS(JBossSAMLURIConstants.ASSERTION_NSURI.get(),
               JBossSAMLConstants.ASSERTION.get());

       for (int i = 0; i < nodes.getLength(); i++) {
           Node n = nodes.item(i);
           if (n instanceof Element) {
               ((Element) n).setIdAttribute("ID", true);
           }
       }
   }
    
   public boolean authenticate_work()
    {
	   log.info("authenticator:authenticate_work:01");
	   try{
		  Context ctx=new InitialContext();
		  CUDAuthManagerLocal aml=(CUDAuthManagerLocal)ctx.lookup("cudAuthModule.CUDAuthManager.local");
		  AuthItem ai =aml.authCompleteItem(linksMap.getAppCode(), credentials.getUsername(), credentials.getPassword());
		  if(ai==null){
			  log.info("authenticator:authenticate_work:02");
			  FacesMessages.instance().add("������ �������!");
			  return false;
		  }
		  AcUser user = entityManager.find(AcUser.class, ai.getIdUser());
		 
		  Contexts.getSessionContext().set("authItem", ai);
		  
		  if(accessPerm("0052","1")){
			  user.setIsSysAdmin(1L);
		  }
		 /* 
		  entityManager.createQuery(
	               "select o from AcRole o where " +
	               "o.sign ")
	               .setParameter("login", credentials.getUsername())
	               .setParameter("password", credentials.getPassword())
	               .getSingleResult();*/
		  
		  Contexts.getSessionContext().set("currentUser", user);
		  return true;
	   }catch(Exception e){
		 log.error("authenticator:authenticate_work:Error:"+e);
       	 FacesMessages.instance().add("������ �������!");
         return false;
	   }
	}
 /*  
   public boolean authenticate_after_sso(String login, String password)
   {
	   log.info("authenticator:authenticate:01");
	   try{
		  Context ctx=new InitialContext();
		  AuthManagerLocal aml=(AuthManagerLocal)ctx.lookup("authModule.AuthManager.local");
		  AuthItem ai =aml.authCompleteItem(linksMap.getAppCode(), login, password);
		  if(ai==null){
			  log.info("authenticator:authenticate:02");
			  FacesMessages.instance().add("������ �������!");
			  return false;
		  }
		  AcUser user = entityManager.find(AcUser.class, ai.getIdUser());
		  Contexts.getSessionContext().set("authItem", ai);
		  Contexts.getSessionContext().set("currentUser", user);
		  return true;
	   }catch(Exception e){
		 log.error("authenticator:authenticate:Error:"+e);
      	 FacesMessages.instance().add("������ �������!");
        return false;
	   }
	}*/
 /**
   * ���������� �������������� ������������ ��� ����� � �������
   * @return ������������ ������/��������
   */
    public boolean authenticate_()
    {
      /*  log.info("authenticating {0}", credentials.getUsername());
        if ("admin".equals(credentials.getUsername()))
        {
            identity.addRole("admin");
            return true;
        }
        return false;*/
    	 try
         {
     	   log.info("auth_01");
     	   AcUser user = (AcUser) entityManager.createQuery(
               "select au from AcUser au where " +
               "au.login = :login and au.password = :password and " +
               "(au.start is null or au.start <= sysdate) and (au.finish is null or au.finish > sysdate) ")
               .setParameter("login", credentials.getUsername())
               .setParameter("password", credentials.getPassword())
               .getSingleResult();
     	//   user.setShortOrgName(user.getIdOrg().getFullName());
     
     	//  identity.

      Contexts.getSessionContext().set("currentUser", user);
       
   /*   if(user.getLinksUserToRolesCollection2()!=null && user.getLinksUserToRolesCollection2().size()!=0){
     	
     	 Contexts.getSessionContext().set("roleUser", user.getLinksUserToRolesCollection2().get(0).getIdRole().getRoleTitle());
       }else{
     	
      	 FacesMessages.instance().add("� ������������ ��� �� ����� ����!");
       	 return false;
      }*/
            return true;
         }
         catch (NoResultException ex)
         {
         	log.error("auth_NoResultException");
        	FacesMessages.instance().add("�� ���������� ����� ��� ������!!!");
            return false;
         }
         catch (Exception e)
         {
         	log.error("auth_Exception:"+e);
         	 FacesMessages.instance().add("������ �������!");
            return false;
         }

    }
    
 /*   @In Identity identity;
  
    public boolean internalAuthenticate(Principal principal, List<String> roles)
    {
    	System.out.println("!!!authenticate!!!");
    	
    	
    	
    	
        if ((principal instanceof SeamSamlPrincipal))
        {
          SeamSamlPrincipal samlPrincipal = (SeamSamlPrincipal)principal;
          
          
          
          
          System.out.println("!!!authenticate:login:"+samlPrincipal.getName());
             
          
          authenticate_after_sso(samlPrincipal.getName(), "111");
          
          if (samlPrincipal.getName().equals("employee"))
          {
            return Boolean.valueOf(false);
          }

          for (AttributeType attribute : samlPrincipal.getAttributes())
          {
            if (attribute.getName().equals("role"))
            {
              List value = attribute.getAttributeValue();
              if ((value != null) && (value.size() > 0))
              {
                roles.add((String)value.get(0));
              }
            }
          }

          return Boolean.valueOf(true);
        }

        return Boolean.valueOf(true);
     }*/
    
    public String localLogout(){
    	
       log.info("localLogout:01");
       
       
      try{
    	   
    	   if(Identity.instance().isLoggedIn()){
    	   
		    	   Identity.instance().logout();
		  	 
		 	       log.info("localLogout:02_2:"+currentUser.getLogin());
		 	   
		    	   cudLogout(currentUser.getLogin());
		    	   
		    	  // HttpSession hs = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		    	   //hs.invalidate();
		    	   
		    	  /* HttpServletResponse hresp = (HttpServletResponse) 
		    			   FacesContext.getCurrentInstance().getExternalContext().getResponse();
		    	   hresp.sendRedirect("http://localhost:8080/infoscud/test2.seam");
		    	   */
		    	   
		    	 
		    	  
		    	  // ���� ���������������, ��� �� �����, ����� �� �����  logout.
		    	 //  AccessServiceClient asc = (AccessServiceClient)Component.getInstance("asClient",ScopeType.EVENT);
		     	 //  asc.logout(currentUser.getTokenID());
		     	  
		    	   log.info("localLogout:03");
    	   
    	   }
    	   
       }catch(Exception e){
    	   log.error("localLogoutError:"+e);
       }
       
       return "loggedOut";
    }
    
    public void sendPost(String samlMessage, String destination, HttpServletResponse response) throws Exception {
       
    	try{
    	log.info("Authenticator:sendPost:01");
    	// System.out.println("sendPost:01");
    	 
    	String key = GeneralConstants.SAML_REQUEST_KEY ;
    	/*String login_key = "login";
        String password_key = "password";
        String password_hash_key = "password_hash";
        String login_encrypt_key = "login_encrypt";
        String password_encrypt_key = "password_encrypt";
        String secret_key_key = "secret_key";
        String initialization_vector_key = "initialization_vector";
        String login = "les";
        String password = "les";
    	
        String passwordEncryptBase64 = null;
        String loginEncryptBase64 = null;
        String secretKeyBase64 = null;
		String ivBase64 = null;
		
		final int encrypt_data_length = 4;
		  
        //String password_hash = HashPassword.generateStorngPasswordHash(password);
        String[] encrypt_data = login_password_encrypt(login, password, privateKeyClient, publicKeyCUD);
        
        if(encrypt_data!=null&&encrypt_data.length==encrypt_data_length){
            passwordEncryptBase64 = encrypt_data[0];
            loginEncryptBase64 = encrypt_data[1];
            secretKeyBase64 = encrypt_data[2];
            ivBase64 = encrypt_data[3];
        }
        */
        if (destination == null) {
        	log.error("Authenticator:sendPost:Destination is null");
            throw new Exception("Authenticator:sendPost:Destination is null");
        }
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        common(response);
        StringBuilder builder = new StringBuilder();

        builder.append("<HTML>");
        builder.append("<HEAD>");
        
        builder.append("<TITLE>HTTP Post Binding (Request)</TITLE>");
       
        builder.append("</HEAD>");
        builder.append("<BODY Onload=\"document.forms[0].submit()\">");

        builder.append("<FORM METHOD=\"POST\" ACTION=\"" + destination + "\">");
        builder.append("<INPUT TYPE=\"HIDDEN\" NAME=\"" + key + "\"" + " VALUE=\"" + samlMessage + "\"/>");
       
       /* if(encrypt_data!=null&&encrypt_data.length==encrypt_data_length){
        
         // builder.append("<INPUT TYPE=\"HIDDEN\" NAME=\"" + login_key + "\"" + " VALUE=\"" + login + "\"/>");
         // builder.append("<INPUT TYPE=\"HIDDEN\" NAME=\"" + password_key + "\"" + " VALUE=\"" + password + "\"/>");
       
          builder.append("<INPUT TYPE=\"HIDDEN\" NAME=\"" + login_encrypt_key + "\"" + " VALUE=\"" + loginEncryptBase64 + "\"/>");
          builder.append("<INPUT TYPE=\"HIDDEN\" NAME=\"" + password_encrypt_key + "\"" + " VALUE=\"" + passwordEncryptBase64 + "\"/>");
          builder.append("<INPUT TYPE=\"HIDDEN\" NAME=\"" + secret_key_key + "\"" + " VALUE=\"" + secretKeyBase64 + "\"/>");
          builder.append("<INPUT TYPE=\"HIDDEN\" NAME=\"" + initialization_vector_key + "\"" + " VALUE=\"" + ivBase64 + "\"/>");
        }*/
     
        
        builder.append("<NOSCRIPT>");
        builder.append("<P>JavaScript is disabled. We strongly recommend to enable it. Click the button below to continue.</P>");
        builder.append("<INPUT TYPE=\"SUBMIT\" VALUE=\"CONTINUE\" />");
        builder.append("</NOSCRIPT>");

        builder.append("</FORM></BODY></HTML>");
        
        String str = builder.toString();
       
        log.info("Authenticator:sendPost:02:"+str);
        
      //  System.out.println("test1:02:"+str);
        
        /*
        ServletOutputStream outputStream = response.getOutputStream();

         // we need to re-configure the content length, because Tomcat may have written some content.
        response.resetBuffer();
        response.setContentLength(str.length());
        
        outputStream.println(str);
        outputStream.close();
        */
        
        out.println(str);
	    out.close();
	    
	    log.info("Authenticator:sendPost:03");
	    
      //  System.out.println("sendPost:05");
        
    	}catch(Exception e){
    		log.error("Authenticator:sendPost:error:"+e);
    		System.out.println("sendPost:error:"+e);
    		e.printStackTrace(System.out);
    	}
    }

    private static void common(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, no-store");
    } 
    
    private static String[] login_password_encrypt(String login, String password, PrivateKey privateKeyClient, PublicKey publicKeyCUD) {
		
        String[] result = null;   
		try{
		
		  //����������
		  final byte[] data_pass = password.getBytes("utf-8");
		  byte[] data_login = null;
			
		  if(login!=null){
			  data_login = login.getBytes("utf-8");
		  }
		  
		  SecretKey secretKey = KeyGenerator.getInstance("GOST28147").generateKey();
		 
		  Cipher cipher = Cipher.getInstance(CIPHER_ALG);
	      cipher.init(Cipher.ENCRYPT_MODE, secretKey);
	      final byte[] iv = cipher.getIV();
	    
	      final byte[] encryptedtext_pass = cipher.doFinal(data_pass, 0, data_pass.length);
	      byte[] encryptedtext_login = null;
	      
	      if(login!=null){
	        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
	        encryptedtext_login = cipher.doFinal(data_login, 0, data_login.length);
	      }
	      
	 	  Cipher wrapCipher = Cipher.getInstance(CIPHER_ALG_TRANSPORT);
		  wrapCipher.init(Cipher.WRAP_MODE, publicKeyCUD);
		  byte[] wrappedSecretKey = wrapCipher.wrap(secretKey);
		 
		  
		  result = new String[4]; 
		  
		 // org.apache.commons.codec.binary.Base64
		  String passwordEncryptBase64 = Base64.encodeBytes(encryptedtext_pass, Base64.DONT_BREAK_LINES);
		  String loginEncryptBase64 = null;
		  if(login!=null){
		    loginEncryptBase64 = Base64.encodeBytes(encryptedtext_login, Base64.DONT_BREAK_LINES);
		  }	 
		  
		  String secretKeyBase64 = Base64.encodeBytes(wrappedSecretKey, Base64.DONT_BREAK_LINES);
		  String ivBase64 = Base64.encodeBytes(iv, Base64.DONT_BREAK_LINES);
		  		
		  result[0]=passwordEncryptBase64;
		  result[1]=loginEncryptBase64;
		  result[2]=secretKeyBase64;
		  result[3]=ivBase64;
		  
		  System.out.println("passwordEncryptBase64:"+passwordEncryptBase64);
		  System.out.println("loginEncryptBase64:"+loginEncryptBase64);
		  System.out.println("secretKeyBase64:"+secretKeyBase64);
		  System.out.println("ivBase64:"+ivBase64);
	       
		  
		//�����������
		/*  
		  PrivateKey responderPrivateKey = privateKeyClient;
		  Cipher unwrapCipher = Cipher.getInstance(CIPHER_ALG_TRANSPORT);
		  unwrapCipher.init(Cipher.UNWRAP_MODE, responderPrivateKey);
		  SecretKey clientSecretKey = (SecretKey) unwrapCipher.unwrap(wrappedSecretKey, null, Cipher.SECRET_KEY);
		  
		  cipher = Cipher.getInstance(CIPHER_ALG);
		  cipher.init(Cipher.DECRYPT_MODE, clientSecretKey, new IvParameterSpec(iv), null);
		  final byte[] decryptedtext = cipher
		            .doFinal(encryptedtext, 0, encryptedtext.length);
		  
		  System.out.println("decryptedtext:"+new String(decryptedtext, "utf-8"));
		  */
		  
		}catch(Exception e){
			System.out.println("error:"+e);
		}
		return result;
	}
   private static String getSAMLDestination(PrivateKey privateKeyClient, PublicKey publicKeyCUD) {
		
        String result = null;   
		try{
			System.out.println("getSAMLDestination:01");
 
			String login_key = "login";
	        String password_key = "password";
	       /* String login_encrypt_key = "login_encrypt";
	        String password_encrypt_key = "password_encrypt";
	        String secret_key_key = "secret_key";
	        String initialization_vector_key = "initialization_vector";*/
	       String login_encrypt_key = "elogin";
	        String password_encrypt_key = "epassword";
	        String secret_key_key = "skey";
	        String initialization_vector_key = "ivector";
	        String login = "les";
	        String password = "les";
	    	
	        String passwordEncryptBase64 = null;
	        String loginEncryptBase64 = null;
	        String secretKeyBase64 = null;
			String ivBase64 = null;
			
			final int encrypt_data_length = 4;
			  
	        //String password_hash = HashPassword.generateStorngPasswordHash(password);
	        String[] encrypt_data = login_password_encrypt(null, password, privateKeyClient, publicKeyCUD);
	        
	        if(encrypt_data!=null&&encrypt_data.length==encrypt_data_length){
	            passwordEncryptBase64 = encrypt_data[0];
	           // loginEncryptBase64 = encrypt_data[1];
	            secretKeyBase64 = encrypt_data[2];
	            ivBase64 = encrypt_data[3];
	        }
	        
	       // result="?"+login_encrypt_key+"="+URLEncoder.encode(loginEncryptBase64, "UTF-8")+
	     	result="?"+login_key+"="+URLEncoder.encode(login, "UTF-8")+
	        	   "&"+password_encrypt_key+"="+URLEncoder.encode(passwordEncryptBase64, "UTF-8")+
	        	   "&"+secret_key_key+"="+URLEncoder.encode(secretKeyBase64, "UTF-8")+
	        	   "&"+initialization_vector_key+"="+URLEncoder.encode(ivBase64, "UTF-8");
	        		
	   }catch(Exception e){
			System.out.println("getSAMLDestination:error:"+e);
		}
		return result;
	}
    
	 private AssertionType/*Document*/ decrypt(Document doc, PrivateKey privateKey){
		// Document result = null;
		 AssertionType result = null;
		 try{
			 
			 System.out.println("decrypt:01");
			 
			 Element enc = DocumentUtil.getElement(doc, new QName(JBossSAMLConstants.ENCRYPTED_ASSERTION.get()));
		        if (enc == null){
		        	 throw new Exception("Cannot find ENCRYPTED_ASSERTION element");
		        }
		        String oldID = enc.getAttribute(JBossSAMLConstants.ID.get());
		        Document newDoc = DocumentUtil.createDocument();
		        Node importedNode = newDoc.importNode(enc, true);
		        newDoc.appendChild(importedNode);

		      //  Element decryptedDocumentElement = GOSTXMLEncryptionUtil.decryptElementInDocument(newDoc, privateKey);
		        Element decryptedDocumentElement = decryptElementInDocument(newDoc, privateKey);
			        
		       //System.out.println("decrypt:02:"+DocumentUtil.asString(decryptedDocumentElement.getOwnerDocument()));
		        
		        SAMLParser parser = new SAMLParser();

		        JAXPValidationUtil.checkSchemaValidation(decryptedDocumentElement);
		        AssertionType assertion = (AssertionType)parser.parse(StaxParserUtil.getXMLEventReader(DocumentUtil.getNodeAsStream(decryptedDocumentElement)));

		        result=assertion;
		        
		        Document ass_doc = AssertionUtil.asDocument(assertion);
		        
		       System.out.println("Authenticator:decrypt:03:"+DocumentUtil.asString(ass_doc));
		        
		  	 
			// System.out.println("decrypt:0100");
		 }catch(Exception e3){
			 System.out.println("decrypt:error:"+e3);
			 e3.printStackTrace(System.out);
	     }
		return result;
	 }
	 
	 
	 public static Element decryptElementInDocument(Document documentWithEncryptedElement, PrivateKey privateKey)
	            throws Exception {
		  
		  System.out.println("decryptElementInDocument:01");
			
		  
		  org.apache.xml.security.Init.init();
		  
		  if(!JCPXMLDSigInit.isInitialized()) {
			System.out.println("GostXMLEncryptionUtil:static:02+");
		  //  JCPXMLDSigInit.init();
			 Crypto15Init.fileInit();
		    System.out.println("GostXMLEncryptionUtil:static:03");
		  }
		  
	        if (documentWithEncryptedElement == null)
	        	throw new Exception("Input document is null");

	        // Look for encrypted data element
	        Element documentRoot = documentWithEncryptedElement.getDocumentElement();
	      //  Element encDataElement = getNextElementNode(documentRoot.getFirstChild());
	        Element encDataElement =  (Element) documentRoot.getFirstChild();
	        if (encDataElement == null)
	            throw new Exception("No element representing the encrypted data found");

	      //  System.out.println("decryptElementInDocument:01:"+encDataElement.getLocalName());
	        
	        // Look at siblings for the key
	        Element encKeyElement = getNextElementNode(encDataElement.getNextSibling());
	        if (encKeyElement == null) {
	        	
	        	//System.out.println("decryptElementInDocument:01+");
	            // Search the enc data element for enc key
	            NodeList nodeList = encDataElement.getElementsByTagNameNS(XMLENC_NS, ENCRYPTED_KEY_LOCALNAME);

	            if (nodeList == null || nodeList.getLength() == 0)
	            	throw new Exception("Encrypted Key not found in the enc data");

	            encKeyElement = (Element) nodeList.item(0);
	        }

	       // System.out.println("decryptElementInDocument:02:"+encKeyElement.getLocalName());
	        
	        XMLCipher cipher;
	        EncryptedData encryptedData;
	        EncryptedKey encryptedKey;
	        try {
	        //	System.out.println("decryptElementInDocument:03");
	        	
	            cipher = XMLCipher.getInstance();
	            cipher.init(XMLCipher.DECRYPT_MODE, null);
	            encryptedData = cipher.loadEncryptedData(documentWithEncryptedElement, encDataElement);
	            encryptedKey = cipher.loadEncryptedKey(documentWithEncryptedElement, encKeyElement);
	      
	           // System.out.println("decryptElementInDocument:04");
	            
	        } catch (XMLEncryptionException e1) {
	        	throw new Exception(e1);
	        }

	        Document decryptedDoc = null;

	        if (encryptedData != null && encryptedKey != null) {
	        	
	        //	System.out.println("decryptElementInDocument:05");
	        	
	            try {
	                String encAlgoURL = encryptedData.getEncryptionMethod().getAlgorithm();
	                XMLCipher keyCipher = XMLCipher.getInstance();
	                keyCipher.init(XMLCipher.UNWRAP_MODE, privateKey);
	                Key encryptionKey = keyCipher.decryptKey(encryptedKey, encAlgoURL);
	                cipher = XMLCipher.getInstance();
	                cipher.init(XMLCipher.DECRYPT_MODE, encryptionKey);

	                decryptedDoc = cipher.doFinal(documentWithEncryptedElement, encDataElement);
	            } catch (Exception e) {
	            	throw new Exception(e);
	            }
	        }
	      //  System.out.println("decryptElementInDocument:06");
	        
	        Element decryptedRoot = decryptedDoc.getDocumentElement();
	        //<saml:EncryptedAssertion xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"><saml:Assertion... 
	        
	       // System.out.println("decryptElementInDocument:07:"+DocumentUtil.asString(decryptedRoot.getOwnerDocument()));
	        
	        //Element dataElement = getNextElementNode(decryptedRoot.getFirstChild());
	        Element dataElement = (Element)decryptedRoot.getFirstChild();
	        //<saml:Assertion...
	        
	      //  System.out.println("decryptElementInDocument:08:"+dataElement.getLocalName());
	        
	        if (dataElement == null)
	        	throw new Exception("Data Element after encryption is null");

	        decryptedRoot.removeChild(dataElement);
	        decryptedDoc.replaceChild(dataElement, decryptedRoot);
         // �������� <saml:EncryptedAssertion (decryptedRoot) �� <saml:Assertion (dataElement)
	    	        
	        return decryptedDoc.getDocumentElement();
	    }
	 
	 private static Element getNextElementNode(Node node) {
		//  System.out.println("getNextElementNode:01");
	        while (node != null) {
	        	// System.out.println("getNextElementNode:02");
	            if (Node.ELEMENT_NODE == node.getNodeType()){
	            	//System.out.println("getNextElementNode:03");
	                return (Element) node;}
	            node = node.getNextSibling();
	        }
	        return null;
	    }
	
	 private boolean ass_valid(Document signedDoc, PublicKey publicKey){
		 
		 boolean result = false;
		 try{
			    SAML2Signature samlSignature = new GOSTSAML2Signature();
		        samlSignature.setSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#gostr34102001-gostr3411");
				samlSignature.setDigestMethod("http://www.w3.org/2001/04/xmldsig-more#gostr3411");
			
				
			    Document doc = DocumentUtil.createDocument();
		        Node n = doc.importNode( signedDoc.getDocumentElement(), true);
		        doc.appendChild(n);

		        result = samlSignature.validate(doc, publicKey);
		        
		 }catch(Exception e){
			 System.out.println("Authenticator:ass_valid:error:"+e);
		 }
		 return result;
	 }
	 
	 private static Document get_saml_assertion_from_response (String pSAMLResponse){
			
		 Document result = null;
		 InputStream dataStream = null;	
		 
			try{
	
				 System.out.println("get_saml_assertion_from_xml:01");
			 
			 
			 dataStream = PostBindingUtil.base64DecodeAsStream(pSAMLResponse);
			
			 
			 //  InputStream samlAssertionInputStream = new FileInputStream("saml_response.out");

			 //  System.out.println("get_saml_assertion_from_xml:01_2");
			   
		        SAMLParser samlParser = new SAMLParser();

		        Object parsedObject = samlParser.parse(dataStream);

		        // cast the parsed object to the expected type, in this case AssertionType
		        ResponseType authn = (ResponseType) parsedObject;

		       // SAML2Request  sr = new SAML2Request();
		        SAML2Response sr = new SAML2Response();
		        
		       Document doc_authn = sr.convert(authn);
		        
		       result = doc_authn;
		        
		      // System.out.println("get_saml_assertion_from_xml:01_3:"+DocumentUtil.asString(doc_authn));
			  		      
		  }catch(Exception e3){
				 System.out.println("get_saml_assertion_from_xml:error:"+e3);
				 e3.printStackTrace(System.out);
		  }finally{
			  try{
				  if(dataStream!=null){
					  dataStream.close();
				  }
			  }catch(Exception e){
				  
			  }
		  }
			return result;
		}
	 
	 private List<String> getRoles(AttributeStatementType attributeStatement, Map<String, String> userAttrib) throws Exception {
        
		 System.out.println("Authenticator:getRoles:01");
		 
		 List<String> roles = new ArrayList<String>();

        
         // PLFED-140: which of the attribute statements represent roles?
         List<String> roleKeys = new ArrayList<String>();

         //String roleKey = "Role";
         String roleKey = "USER_ROLES";
         roleKeys.addAll(StringUtil.tokenize(roleKey));
          
         List<ASTChoiceType> attList = attributeStatement.getAttributes();
         for (ASTChoiceType obj : attList) {
             AttributeType attr = obj.getAttribute();
             
            // System.out.println("Authenticator:getRoles:02:"+attr.getName());
             
             if (!roleKeys.contains(attr.getName())){
                //��������
            	 List<Object> attributeValues = attr.getAttributeValue();
                 if (attributeValues != null) {
                     for (Object attrValue : attributeValues) {
                    	 
                    	 if (attrValue instanceof String) {
                        	// System.out.println("Authenticator:getRoles:03:"+(String) attrValue);
                             userAttrib.put(attr.getName(), (String) attrValue);
                         } else
                             throw new Exception(attrValue.toString());
                     }
                 }
             }else{
            	 //����
             List<Object> attributeValues = attr.getAttributeValue();
             if (attributeValues != null) {
                 for (Object attrValue : attributeValues) {
                	 
                	// System.out.println("Authenticator:getRoles:04");
                	 
                     if (attrValue instanceof String) {
                    	// System.out.println("Authenticator:getRoles:05:"+(String) attrValue);
                         roles.add((String) attrValue);
                     } else
                         throw new Exception(attrValue.toString());
                 }
             }
           }
         }
         return roles;
     }
	 
	 
	 public List<Long> allowedSys(Long idUser) throws Exception{
	    	
		    log.info("authenticator:allowedSys:01");
		 
		    List<Long> result = null;
		    
	    	try{
	    	  List<Object> slist = entityManager.createNativeQuery(
	                 "select to_char(lus.UP_SYS) up_sys " +
	                 "from LINK_ADMIN_USER_SYS lus " +
	                 "where " +
	                 "lus.UP_USER =:idUser " )
	                 .setParameter("idUser", idUser)
	                 .getResultList();
	  
	     	  for(Object sys: slist){
	     		if(result==null){
	     			result = new ArrayList<Long>();
	     		}
	     		
	     		result.add(Long.parseLong((String)sys));
	      	 		 
	     	  }
	    	}catch(Exception e){
	    		 log.error("authenticator:allowedSys:error"+e);
	    		// e.printStackTrace(System.out);
	    		// throw e;
	    	}
	    	return result;
	    }
	 
	 public List<String> allowedReestr(Long idUser) throws Exception{
	    	
		    log.info("authenticator:allowedReestr:01");
		 
		    List<String> result = null;
		    
	    	try{
	    		result = entityManager.createNativeQuery(
						  "select DOM.PAGE_CODE||':'|| PERM.ID_SRV code  " + 
						  "from LINK_ADMIN_USER_DOM_PRM udp,  " + 
						  "AC_APP_DOMAINS_BSS_T dom,  " + 
						  "AC_PERMISSIONS_LIST_BSS_T perm  " + 
						  "where DOM.ID_SRV=UDP.UP_DOM  " + 
						  "and PERM.ID_SRV=UDP.UP_PRM  " + 
						  "and UDP.UP_USER = :idUser")
			           .setParameter("idUser", idUser)
			           .getResultList();
	  	}catch(Exception e){
	    		 log.error("authenticator:allowedReestr:error"+e);
	    		// e.printStackTrace(System.out);
	    		// throw e;
	    	}
	    	return result;
	    }
	 
	  public boolean isAccOrgManager(Long idUser) throws Exception{
	    	
		    log.info("authenticator:isAccOrgManager:01");
		 
		    boolean result = false;
		    
	    	try{
	    		Long isAccOrgManager = (Long) entityManager
	  				  .createQuery("select au.isAccOrgManager from AcUser au " +
	  				  		        "where au.idUser= :idUser")
	  				  .setParameter("idUser", idUser)		        
	                    .getSingleResult();
	    		
	    		if(isAccOrgManager!=null&&isAccOrgManager.equals(1L)){
	    			result = true;
	    		}
	    		
	    		log.info("authenticator:isAccOrgManager:02:"+result);
	    		 
	  	}catch(Exception e){
	    		 log.error("authenticator:isAccOrgManager:error"+e);
	    		// e.printStackTrace(System.out);
	    		// throw e;
	    	}
	    	return result;
	    }
	 
/*
 select AR.ROLE_TITLE, AUR.ID_RAION, ARD.ID_PERM   
from ac_users au, ac_roles ar, 
       AC_LINK_USER_TO_ROLE_TO_RAIONS aur, AC_LINK_ROLE_APP_DOMEN_PRMSSNS ard,
       AC_APP_DOMAINS ad
where AU.ID_USER=4 and AUR.ID_ROLE= AR.ID_ROLE 
    and AUR.ID_USER=AU.ID_USER and ARD.ID_ROLE=AR.ID_ROLE
     and ARD.ID_DOMEN=AD.ID_DOMEN and AD.PAGE_CODE='003'
     and AD.APP_CODE=24
 */
}
