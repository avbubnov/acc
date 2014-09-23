package ru.spb.iac.cud.uarm.web.context.user;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.xml.security.utils.Base64;

import com.objsys.asn1j.runtime.Asn1BerDecodeBuffer;


import com.objsys.asn1j.runtime.Asn1BerEncodeBuffer;

import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.ContentInfo;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.DigestAlgorithmIdentifier;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignedData;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.params.OID;
import ru.CryptoPro.JCP.tools.Decoder;
import ru.spb.iac.cud.uarm.ejb.context.user.UserManagerEJB;
import ru.spb.iac.cud.uarm.ejb.entity.AcUsersCertBssT;
import ru.spb.iac.cud.uarm.util.CUDUserConsoleConstants;

@WebServlet("/WebCertAction")
@ManagedBean(name="webCertActionBean")
@RequestScoped
public class WebCertAction extends HttpServlet {
	
	
	@EJB(beanName = "CUDUserConsole-ejb.jar#UserManagerEJB")
	private UserManagerEJB userManagerEJB;
	
	private static final long serialVersionUID = 1L;
   /*  
	public static final String STR_CMS_OID_DATA = "1.2.840.113549.1.7.1";
	public static final String STR_CMS_OID_SIGNED = "1.2.840.113549.1.7.2";
	public static final String STR_CMS_OID_ENVELOPED = "1.2.840.113549.1.7.3";

	public static final String STR_CMS_OID_CONT_TYP_ATTR = "1.2.840.113549.1.9.3";
	public static final String STR_CMS_OID_DIGEST_ATTR = "1.2.840.113549.1.9.4";
	public static final String STR_CMS_OID_SIGN_TYM_ATTR = "1.2.840.113549.1.9.5";

	public static final String STR_CMS_OID_TS = "1.2.840.113549.1.9.16.1.4";

	public static final String DIGEST_OID = JCP.GOST_DIGEST_OID;
	public static final String SIGN_OID = JCP.GOST_EL_KEY_OID;
	*/
	
	public static final String STR_CMS_OID_SIGNED = "1.2.840.113549.1.7.2";
	public static final String DIGEST_OID = JCP.GOST_DIGEST_OID;
	
	private static StringBuffer out = new StringBuffer("");
	private static StringBuffer out1 = new StringBuffer("");
	
	private static String alias="certificate";
	private static String alias_root="уцспбгуп«спбиац».crt";
	
	private String root_sn = null;
    
	private static String cert_store_url;
	
    public WebCertAction() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void init(ServletConfig config) throws ServletException {
		try{
			
			if(cert_store_url==null){
				cert_store_url = config.getServletContext().getInitParameter("cert_store_url");
			}
			
			if(cert_store_url==null){
				throw new Exception("cert_store_url is not set!!!");
			}
			
			//System.out.println("WebCertAction:init:cert_store_url:"+cert_store_url);
			
		}catch(Exception e){
			 System.out.println("WebCertAction:init:error:"+e);
		} 
	}
    
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		//Token token = null;
		String backUrl = null, forceBack=null;
		String success = "false";
		String signatureValue=null;
		String repeatLoginUrl=null;
		int revokedCertificate = 0;
		
		String login_user = null;
		
		String destination = null;
		
		try{
			
		
			//String successUrl = request.getParameter("successUrl");
			//String failsUrl = request.getParameter("failsUrl");
			backUrl = request.getParameter("backUrl");
			signatureValue = request.getParameter("signatureValue");
			
			forceBack = request.getParameter("forceBack");
			
		//	System.out.println("WebCertAction:service:backUrl:"+backUrl);
			//System.out.println("WebLoginAction:service:signatureValue:"+signatureValue);
			
			if(signatureValue!=null){
			
		     // String certSN =validate(signatureValue);
		      X509Certificate user_cert =validate(signatureValue);
		      
		      
		      System.out.println("WebCertAction:service:user_cert:"+(user_cert!=null));
		      
		      if(user_cert!=null){
		    	 
		    	  System.out.println("WebCertAction:service:userManagerEJB:"+(userManagerEJB==null));
		    	  
			    // login_user = (new ContextAccessWebManager())
			   // 		.authenticate_cert_sn(certSN, getIPAddress(request), getCodeSystem(request));
		    	
		    	if (addUserCert(user_cert, request)==1){
		  	       success="true"; 
		       	}
			
					    
			  }
			}
		}catch(Exception e4){
		    System.out.println("WebCertAction:error4:"+e4.getMessage());
		}
		
		
		 if(success.equals("true")){
			// response.sendRedirect(request.getContextPath()+"/"+(request.getParameter("overauth")!=null?"?overauth":""));
			 
			// response.sendRedirect(request.getContextPath()+
			//		 "/context/profile/info/cert/test3.xhtml?success=true");
			 
			 destination = request.getContextPath()+"/context/profile/cert/list.xhtml?pageItem=profile_cert";
			 
		 }else{
			 
			 destination = request.getContextPath()+"/context/profile/cert/create/list.xhtml?pageItem=profile_cert&error=cert_exist";
			
		 }
		 
		 location_href(response, destination);
		 
	}
   /* public static void sendPost(String destination, HttpServletResponse response,
            String success, String tokenID)throws IOException{

System.out.println("WebCertAction:sendPost:01");


if(destination==null){
try{
	
	
	PrintWriter pw = response.getWriter();
		
		
      pw.print("<html>");
    
      pw.print("<body>");
    
      pw.print("<body>");
      pw.print("success:"+success);
      pw.print("<br/>");
      pw.print("tokenID:"+tokenID);
   
   
      pw.print("</body>");
      pw.print("</html>");
      pw.close();
	}catch(Exception e){
		   System.out.println("main:error:"+e);
	}
return;
}

response.setContentType("text/html");
PrintWriter out = response.getWriter();
common(destination, response);
StringBuilder builder = new StringBuilder();

builder.append("<HTML>");
builder.append("<HEAD>");
builder.append("<TITLE>HTTP Post Binding Response (Response)</TITLE>");
builder.append("</HEAD>");
builder.append("<BODY Onload=\"document.forms[0].submit()\">");

builder.append("<FORM METHOD=\"POST\" ACTION=\"" + destination + "\">");
builder.append("<INPUT TYPE=\"HIDDEN\" NAME=\"success\" VALUE=\"" + success + "\"/>");

if(tokenID!=null){
builder.append("<INPUT TYPE=\"HIDDEN\" NAME=\"tokenID\" VALUE=\"" + tokenID + "\"/>");
}  

builder.append("</FORM></BODY></HTML>");

String str = builder.toString();

System.out.println("AccessServicesWeb:sendPost:"+str);

out.println(str);
out.close();
}
*/
    private void location_href(HttpServletResponse response, String destination) {
    	
		try {

			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			common(response);
			StringBuilder builder = new StringBuilder();

			builder.append("<HTML>");
			builder.append("<HEAD>");
			builder.append("<TITLE>HTTP Response</TITLE>");
			builder.append("</HEAD>");
			builder.append("<BODY>");

			builder.append("</BODY>");
			
			builder.append("<script>");
			builder.append("//<![CDATA[ \n");
			//builder.append("alert(1);");
			builder.append("window.top.location.href=");
			builder.append("\""+destination+"\";");
			builder.append("\n //]]>");
			builder.append("</script>");
			
			builder.append("</HTML>");

			String str = builder.toString();

			System.out.println("WebCertAction:location_href:" + str);

			out.println(str);
			out.close();

		} catch (Exception e) {
			System.out.println("WebCertAction:location_href:error:" + e);
		}
	}
    
    
	private static void common( HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache, no-store");
	}

  private String getIPAddress(HttpServletRequest request){
    
    String ipAddress = request.getRemoteAddr(); 
    
  /*  String ipAddress  = request.getHeader("X-FORWARDED-FOR");  
    if(ipAddress == null)  
    { 
      ipAddress = request.getRemoteAddr();  
    }  */
    
return ipAddress;
}

private X509Certificate validate(String message){
	
	try{
	
	final Decoder decoder = new Decoder();
    final byte[] enc =
            decoder.decodeBuffer(new ByteArrayInputStream(message.getBytes()));
  
  System.out.println("main:04");
  if(enc!=null){
   //System.out.println("main:02:enc:"+new String(enc, "utf-8")); 
   }
  
    return CMSVerify(enc,null, "12345".getBytes());
	}catch(Exception e){
		System.out.println("decode:error:"+e);
	}
	return null;
		
}

public X509Certificate CMSVerify(byte[] buffer, Certificate[] certs, byte[] data)
        throws Exception {
    //clear buffers fo logs
	
	//System.out.println("CMSVerify:001");
	
    out = new StringBuffer("");
    out1 = new StringBuffer("");
    final Asn1BerDecodeBuffer asnBuf = new Asn1BerDecodeBuffer(buffer);
    
   // System.out.println("CMSVerify:002");
    final ContentInfo all = new ContentInfo();
    
  //  System.out.println("CMSVerify:003");
    all.decode(asnBuf);
    
  //  System.out.println("CMSVerify:004");
    
    if (!new OID(STR_CMS_OID_SIGNED).eq(all.contentType.value))
        throw new Exception("Not supported");
    final SignedData cms = (SignedData) all.content;
    final byte[] text;
    if (cms.encapContentInfo.eContent != null)
        text = cms.encapContentInfo.eContent.value;
    else if (data != null) text = data;
    else throw new Exception("No content for verify");
    OID digestOid = null;
    final DigestAlgorithmIdentifier digestAlgorithmIdentifier =
            new DigestAlgorithmIdentifier(new OID(DIGEST_OID).value);
    for (int i = 0; i < cms.digestAlgorithms.elements.length; i++) {
        if (cms.digestAlgorithms.elements[i].algorithm
                .equals(digestAlgorithmIdentifier.algorithm)) {
            digestOid =
                    new OID(cms.digestAlgorithms.elements[i].algorithm.value);
            break;
        }
    }
    if (digestOid == null)
        throw new Exception("Unknown digest");
    final OID eContTypeOID = new OID(cms.encapContentInfo.eContentType.value);
    
  //  System.out.println("CMSVerify:01");
    
    if (cms.certificates != null) {
    	
    //	System.out.println("CMSVerify:02");
    	
        //Проверка на вложенных сертификатах
        for (int i = 0; i < cms.certificates.elements.length; i++) {
            final Asn1BerEncodeBuffer encBuf = new Asn1BerEncodeBuffer();
            cms.certificates.elements[i].encode(encBuf);

            final CertificateFactory cf =
                    CertificateFactory.getInstance("X.509");
            final X509Certificate cert =
                    (X509Certificate) cf
                            .generateCertificate(encBuf.getInputStream());

            
            // System.out.println("CMSVerify:03:"+cert.toString());
         //   System.out.println("CMSVerify:03:SubjectDN:"+cert.getSubjectDN());
          //  System.out.println("CMSVerify:03:"+cert.getSerialNumber());
         //   System.out.println("CMSVerify:03:cert_sn:"+dec_to_hex(cert.getSerialNumber()));
       //     System.out.println("CMSVerify:03:root_sn:"+root_sn());
            
         if(root_sn()!=null&&!root_sn().equals(dec_to_hex(cert.getSerialNumber()))){
        	
        	 
        	/* 
        	// String cert_base64_1 = DatatypeConverter.printBase64Binary(cert.getEncoded());
        	 
        	// System.out.println("cert_base64_1:"+cert_base64_1);
        	 
        	 String cert_base64_2 = new BASE64Encoder().encode(cert.getEncoded());
        	 
        	 System.out.println("cert_base64_2:"+cert_base64_2);
        	 
        	// byte[] encoded = org.apache.commons.codec.binary.Base64.encodeBase64(cert.getEncoded());
        	// String cert_base64_3 = Arrays.toString(encoded);
        		
        	 String cert_base64_3 = Base64.encodeBytes(cert.getEncoded());
        	 
        	 System.out.println("cert_base64_3:"+cert_base64_3);
        	 
        	 getX509CertificateFromKeyInfoString(cert_base64_3);
        	 */
        	 /* byte[] certData = DatatypeConverter.parseBase64Binary(cert_base64);
 			
 			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
 			InputStream in = new ByteArrayInputStream(certData);
 			X509Certificate cert_obj = (X509Certificate)certFactory.generateCertificate(in);
 		    if(cert_obj==null){
 		    	 System.out.println("Certificate not found!");
 		    }else{
 		  		 System.out.println("AccessServicesImpl:authenticate_cert_base64:SerialNumber:"+cert_obj.getSerialNumber().toString(16)); 
 		    }
        	 in.close();*/
 		   
            if(chain_check(cert)){
            	
              return cert;
        	//  return dec_to_hex(cert.getSerialNumber());
           }
         }  
            

        }
    }

    return null;
}

public static boolean chain_check(Certificate pcert) {
	// TODO Auto-generated method stub
	boolean result = false;
	
	try{
		
		KeyStore keyStore = KeyStore.getInstance("CertStore", "JCP");
	//	keyStore.load(new FileInputStream("/Development/cert/gost/cudvm/cudvm.store"), "Access_Control".toCharArray());
		//keyStore.load(new FileInputStream("/distr/jboss/jboss-5.1.0.GA/cert/cudvm.store"), "Access_Control".toCharArray());
		
		keyStore.load(new FileInputStream(cert_store_url), "Access_Control".toCharArray());
		
		
		
	 // final KeyStore keyStore  = KeyStore.getInstance("HDImageStore", "JCP");
	//  keyStore.load(null, null);
	 
//	  PrivateKey key = (PrivateKey)keyStore.getKey(alias, password);
//	  System.out.println("key:"+key.toString());
	  
	//  System.out.println("Current alias_01 ");
	  
	  Enumeration aliases = keyStore.aliases();
	 while (aliases.hasMoreElements()) {
	  String alias = (String) aliases.nextElement();
	//  System.out.println("Current alias: " + alias);
	  if (keyStore.isCertificateEntry(alias)) {
	//  System.out.println( ((X509Certificate)keyStore.getCertificate(alias)).getSubjectDN() );
	  }
	  }
	  
	  Certificate tr = keyStore.getCertificate(alias_root);
	  Certificate crt=pcert;
	//  Certificate crt = keyStore.getCertificate(alias);
	  
	//   System.out.println("cert:"+tr.toString());
	  
	   
	   final Certificate[] certs = new Certificate[2];
	    certs[0] = crt;
	    certs[1] = tr;  //root

	    final Set trust = new HashSet(0);
	    trust.add(new TrustAnchor((X509Certificate) tr, null));

	    final List cert = new ArrayList(0);
	    for (int i = 0; i < certs.length; i++)
	        cert.add(certs[i]);

	    //Параметры
	    final PKIXBuilderParameters cpp = new PKIXBuilderParameters(trust, null);
	    cpp.setSigProvider(null);
	    final CollectionCertStoreParameters par =
	            new CollectionCertStoreParameters(cert);
	    final CertStore store = CertStore.getInstance("Collection", par);
	    cpp.addCertStore(store);
	    final X509CertSelector selector = new X509CertSelector();
	    selector.setCertificate((X509Certificate) crt);
	    cpp.setTargetCertConstraints(selector);

	    //Сертификаты (CertPath)
	    //1)просто из списка сертификатов (в правильном порядке)
	    //final CertificateFactory cf = CertificateFactory.getInstance("X509");
	    //final CertPath cp = cf.generateCertPath(cert);

	    //2) построение цепочки
	    //а) с проверкой crl
	    //cpp.setRevocationEnabled(true);
	    //для использования расширения сертификата CRL Distribution Points
	    //установить System.setProperty("com.sun.security.enableCRLDP", "true");
	    //или System.setProperty("com.ibm.security.enableCRLDP", "true");

	    //б) без проверки crl
	    cpp.setRevocationEnabled(false);
	    final PKIXCertPathBuilderResult res =
	            (PKIXCertPathBuilderResult) CertPathBuilder.
	                    getInstance("PKIX").build(cpp);
	    final CertPath cp = res.getCertPath();

	    //Проверка
	    final CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
	    cpp.setRevocationEnabled(false);
	    cpv.validate(cp, cpp);
	    
	    result = true;
	  
//	  final File file = new File(STORE_PATH);
	 
//	  keyStore.store(new FileOutputStream(file), STORE_PASS);
	  
	}catch(Exception e){
		System.out.println("error:"+e);
	}
	
	return result;
}

private static String dec_to_hex(BigInteger bi) {
	
	String result = null;
	
	try
	{
	 result = bi.toString(16);
    // System.out.println("num_convert:num:"+result);
	}
	catch (NumberFormatException e)
	{
	     System.out.println("Error! tried to parse an invalid number format");
	}
	 return result;
}

public String root_sn() {
	// TODO Auto-generated method stub
	
	if(root_sn==null){
	
		//  System.out.println("WebCertAction:root_sn:01");
		  
	try{
		
		KeyStore keyStore = KeyStore.getInstance("CertStore", "JCP");
		//keyStore.load(new FileInputStream("/Development/cert/gost/cudvm/cudvm.store"), "Access_Control".toCharArray());
		//keyStore.load(new FileInputStream("/distr/jboss/jboss-5.1.0.GA/cert/cudvm.store"), "Access_Control".toCharArray());
		
		keyStore.load(new FileInputStream(cert_store_url), "Access_Control".toCharArray());
		
		
	 // final KeyStore keyStore  = KeyStore.getInstance("HDImageStore", "JCP");
	//  keyStore.load(null, null);
	 
//	  PrivateKey key = (PrivateKey)keyStore.getKey(alias, password);
//	  System.out.println("key:"+key.toString());
	  
	
	  
	  
	  X509Certificate tr = (X509Certificate)keyStore.getCertificate(alias_root);

	  root_sn = dec_to_hex(tr.getSerialNumber());
	  
	 /* 
	  Enumeration<String> enumeration =keyStore.aliases();
	  
 	  while(enumeration.hasMoreElements()) {
          String alias = (String)enumeration.nextElement();
          System.out.println("alias name: " + alias);
          Certificate certificate = keyStore.getCertificate(alias);
          System.out.println(certificate.toString());

      }*/
	  
	  }catch(Exception e){
		  System.out.println("WebCertAction:root_sn:error:"+e);
		 // e.printStackTrace(System.out);
	  }
	}
	
	return root_sn;
}

 public int addUserCert(X509Certificate user_cert, HttpServletRequest request) throws Exception { 
	
	InputStream inputStream = null;
	
	DateFormat df = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
	 
	int result = 0;
	try{
	
	   System.out.println("WebCertAction:addUserCert:01");
	
	   HttpSession hs = request.getSession();
	   Long authUserID = (Long) hs.getAttribute(CUDUserConsoleConstants.authUserID);
		
	   /*
	   HttpSession hs = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false); 
	   Long authUserID = (Long) hs.getAttribute(CUDUserConsoleConstants.authUserID);
		
	  
	   inputStream = certFile.getInputStream();   
	   
	   CertificateFactory user_cf = CertificateFactory.getInstance("X.509");
       X509Certificate user_cert = (X509Certificate)
    		   user_cf.generateCertificate(inputStream);
      */
       String x509Cert = Base64.encode(user_cert.getEncoded());
       
       String serial = dec_to_hex(user_cert.getSerialNumber());
       
       if(userManagerEJB.certNumExistCrt(serial)){
    	   System.out.println("WebCertAction:addUserCert:01_1:return;");
    	 //  FacesContext.getCurrentInstance().addMessage(null, 
       	//		new FacesMessage("Сертификат уже используется!"));
    	 //  FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
    	   return result;
       }
       
       AcUsersCertBssT userCert = new AcUsersCertBssT();
	   
	   userCert.setCertNum(serial);
	   userCert.setCertDate(df.format(user_cert.getNotAfter()));
	   
	   //!!!
	   //сохраняем именно не user_cert.getEncoded(),
	   //а x509Cert.getBytes
	   userCert.setCertValue(x509Cert.getBytes("UTF-8"));
	   
	   String subject = user_cert.getSubjectDN().getName();
	   
	   System.out.println("WebCertAction:addUserCert:02:"+subject);
	   
	   LdapName ldapDN = new LdapName(subject);
	   
	   for(Rdn rdn: ldapDN.getRdns()) {
		    System.out.println(rdn.getType() + " -> " + rdn.getValue());
		    
		    if("CN".equals(rdn.getType())){
		    	userCert.setUserFio((String)rdn.getValue());
		    }else if("OU".equals(rdn.getType())){
		    	userCert.setDepName((String)rdn.getValue());
		    }else if("O".equals(rdn.getType())){
		    	userCert.setOrgName((String)rdn.getValue());
		    }
		    
		}
	   
	   userCert.setUpUserRaw(authUserID);
	   
	   userCert.setCreator(authUserID);
	   userCert.setCreated(new Date());
	   
	   
	   userManagerEJB.uploadCertFile(userCert);
	  
       
	   result=1;
     //  FacesContext.getCurrentInstance().addMessage(null, 
   		//	new FacesMessage("Сертификат добавлен!"));

       
	} catch(Exception e){
		 System.out.println("WebCertAction:addUserCert:error:"+e);
		 
		 result=-1;
		//FacesContext.getCurrentInstance().addMessage(null, 
    	//		new FacesMessage("Ошибка при добавлении сертификата!"));
   }finally{
	   try{
		   if(inputStream!=null){
			   inputStream.close();
		   }
	   }catch(Exception e){
		   
	   }
   }
	 return result;
}  


}
