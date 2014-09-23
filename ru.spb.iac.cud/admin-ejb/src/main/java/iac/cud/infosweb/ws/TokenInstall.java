package iac.cud.infosweb.ws;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import mypackage.Configuration;

import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.KeyInfoConfirmationDataType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationDataType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationType;

import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.items.AuditFunction;

public class TokenInstall {

	//private static final String assertion_file_path = "/home/jboss/jboss/data/saml/saml_asserion.xml";
	private static final String assertion_file_path = Configuration.getSamlAssertion();
	
	 public synchronized String get_assertion() throws Exception{
		 
		 String result = null;
		 try{ 	 
		   
		   System.out.println("TokenInstall:install:01");
			 
		   if(!valid()){
			   System.out.println("TokenInstall:install:02");
			   setup();
		   }
		   
		   result = load();
		   
		   System.out.println("TokenInstall:install:0100");
		   
		  }catch(Exception e){
			 System.out.println("TokenInstall:install:error:"+e);
			 
	     }
		 
		 return result;
		}
	 
	 private boolean valid() {
		 
		 boolean result = false;
		 InputStream samlAssertionInputStream = null;
				 
		 try{
			 System.out.println("TokenInstall:valid:01");
			 
			 File file = new File(assertion_file_path);
			 
			 if(file.exists()){
				
				System.out.println("TokenInstall:valid:02");
				 
				samlAssertionInputStream = new FileInputStream(file);
			    SAMLParser samlParser = new SAMLParser();
                Object parsedObject = samlParser.parse(samlAssertionInputStream);
                AssertionType assertionType = (AssertionType) parsedObject;

                if (!AssertionUtil.hasExpired(assertionType)){
                	System.out.println("TokenInstall:valid:03");
                	 result = true;
                }
                
			 }
			 
			 System.out.println("TokenInstall:valid:0100");
			 
		 }catch(Exception e){
			 System.out.println("TokenInstall:valid:error:"+e);
		 }finally{
			 try{
				 if(samlAssertionInputStream!=null){
					 samlAssertionInputStream.close();
				 }
				 
			 }catch(Exception e1){
				 System.out.println("TokenInstall:valid:finally:error:"+e1);
			 }
		 }
		 
		 return result;
	 }
	 
   private void setup() {
		 
		 InputStream in = null;
		 OutputStream output = null;
		 byte[] buffer = new byte[4096];
         int n = - 1;
         
		 try{
			 System.out.println("TokenInstall:setup:01");
			 
			 File file = new File(assertion_file_path);
			
			 STSServiceClient scl = new STSServiceClient();
			   
			 String samlAssertion = scl.sign_verify_soap_transform_2sign();
			   
			// System.out.println("TokenInstall:setup:02:"+samlAssertion);
			  
			 in = new ByteArrayInputStream(samlAssertion.getBytes("UTF-8"));
			 
			 output = new FileOutputStream(file);
			 
			 while ( (n = in.read(buffer)) != -1){
		              if (n > 0){
		                 output.write(buffer, 0, n);
		              }
		      }
		      output.close();
		      
			 System.out.println("TokenInstall:setup:0100");
			 
		 }catch(Exception e){
			 System.out.println("TokenInstall:setup:error:"+e);
		 }finally{
			 try{
				 if(in!=null){
					 in.close();
				 }
			 }catch(Exception e1){
				 System.out.println("TokenInstall:valid:finally:error:1"+e1);
			 }
			 try{
				 if(output!=null){
					 output.close();
				 }
			 }catch(Exception e2){
				 System.out.println("TokenInstall:valid:finally:error2:"+e2);
			 }
		 }
		 
	 }
   
   private String load() {
		 
		 String result = null;
		 InputStream samlAssertionInputStream = null;
				 
		 try{
			 System.out.println("TokenInstall:load:01");
			 
			 File file = new File(assertion_file_path);
			 
			 if(file.exists()){
				
				System.out.println("TokenInstall:load:02");
				 
				samlAssertionInputStream = new FileInputStream(file);
			    SAMLParser samlParser = new SAMLParser();
              Object parsedObject = samlParser.parse(samlAssertionInputStream);
              AssertionType assertionType = (AssertionType) parsedObject;

              // ¬ажно!!!
              // вынужденна€ мера дл€ picketlink 
              // иначе становитс€ пустой <saml:SubjectConfirmationData/>
              SubjectConfirmationType sct = assertionType.getSubject().getConfirmation().get(0);
  	          SubjectConfirmationDataType  scdt = sct.getSubjectConfirmationData();
  	          KeyInfoConfirmationDataType type = new KeyInfoConfirmationDataType();
  	          type.setAnyType(scdt.getAnyType());		
  	          sct.setSubjectConfirmationData(type);
               
  	          
              if (!AssertionUtil.hasExpired(assertionType)){
              	 result = AssertionUtil.asString(assertionType);
              }
              
			 }
			 
			 System.out.println("TokenInstall:load:0100");
			 
		 }catch(Exception e){
			 System.out.println("TokenInstall:load:error:"+e);
		 }finally{
			 try{
				 if(samlAssertionInputStream!=null){
					 samlAssertionInputStream.close();
				 }
				 
			 }catch(Exception e1){
				 System.out.println("TokenInstall:load:finally:error:"+e1);
			 }
		 }
		 
		 return result;
	 }
	 
}
