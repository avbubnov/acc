package probation;

import iac.cud.infosweb.session.binding.BindingProcessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
//import com.sun.identity.shared.encode.Base64;

import org.apache.xml.security.utils.Base64;


public class Test1 {

	protected static Random random = new Random();
	
	private static volatile ConcurrentHashMap<String, String> controls = new ConcurrentHashMap<String, String>();
	
	
	public static void main(String[] args) {
		SecureRandom secureRandom = null;
		
		System.out.println("main:01");
        try {
        	 
        	// dec_to_hex();
        	 //hex_to_dec();
        	 //trim();
        	// reestr();
        	
        	//getNewKey2();
        	
        	//split();
        	
        	//delete_file();
        	
        	//map();
        	
        	replace();
        	
         } catch (Exception e) {
        	System.out.println("main:error:"+e);
          //  secureRandom = SecureRandom.getInstance("SHA1PRNG");
        }
        
  	}
	
	public static void _main(String[] args) {
		SecureRandom secureRandom = null;
		 String ssoTokenID = null;
		System.out.println("main:01");
        try {
          secureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
          ssoTokenID = getNewKey();
        } catch (Exception e) {
        	System.out.println("main:error:"+e);
          //  secureRandom = SecureRandom.getInstance("SHA1PRNG");
        }
        String amCtxId = Long.toHexString(secureRandom.nextLong());
        System.out.println("main:amCtxId:"+amCtxId);
        System.out.println("main:ssoTokenID:"+ssoTokenID);

	}
	protected static String getNewKey() {

		
		
	       byte[] keyRandom = new byte[12];
           random.nextBytes(keyRandom);

	       String key = Base64.encode(keyRandom);
           try {
        	   System.out.println("main:1:"+key);
             // key += Base64.encode((InetAddress.getLocalHost()).getAddress());
           //   key += Base64.encode(Long.toString(System.currentTimeMillis()).getBytes());
          //    System.out.println("main:2:"+Long.toString(System.currentTimeMillis()).getBytes());
           } catch (Exception e) {
	           e.printStackTrace();
	       }
	       key += Long.toString(System.currentTimeMillis());
	       return (Base64.encode(key.getBytes()));
	   }
	
	public static String getNewKey2() {
		
		String result =  null;
		try {
		Random random = new Random();
	    
		byte[] keyRandom = new byte[12];
        random.nextBytes(keyRandom);

	    String key = Base64.encode(keyRandom);
	    System.out.println("getNewKey2:key:"+key);
       
     	   //System.out.println("main:1:"+key);
          // key += Base64.encode((InetAddress.getLocalHost()).getAddress());
        //   key += Base64.encode(Long.toString(System.currentTimeMillis()).getBytes());
       //    System.out.println("main:2:"+Long.toString(System.currentTimeMillis()).getBytes());
        
	       //key +=Long.toString(System.currentTimeMillis());
	       
	      
	    /*  Calendar cln = Calendar.getInstance();
	      Long token_issue = cln.getTimeInMillis();
		  cln.add(Calendar.HOUR_OF_DAY, 2);
		  Long token_expire = cln.getTimeInMillis();
	    
		  
	       key=key+";token_issue:"+Long.toString(token_issue);
	       key=key+";token_expire:"+Long.toString(token_expire);
	       key=key+";type:"+"cert";*/
	       
	       key=xml_bind("12345"+"_"+key);
	       System.out.println("getNewKey2:key2:"+key);
	       
	       result =  Base64.encode(key.getBytes());
	       System.out.println("getNewKey2:result:"+result);
	       
	       //System.out.println("main:result:"+new String(Base64.decode(result)));
		 } catch (Exception e) {
	           e.printStackTrace();
	       }
		 
	       return result;
	   }
	
	private static void dec_to_hex() {
	try
	{
	 BigInteger bi = new BigInteger("80281257310973985818053");
     System.out.println("num_convert:num:"+bi.toString(16));
	}
	catch (NumberFormatException e)
	{
	     System.out.println("Error! tried to parse an invalid number format");
	   }
	}
	
	private static void hex_to_dec() {
		try
		{
		 String st = "11000e410000000005c5";
		 
		 BigInteger bi = new BigInteger(st, 16);
		 
	     System.out.println("num_convert:num:"+bi);
		}
		catch (NumberFormatException e)
		{
		     System.out.println("Error! tried to parse an invalid number format");
		   }
		}
	
	private static void trim() {
		try
		{
		 String st = "7bb4 108a 0000 0000 05ab";
	     System.out.println("num_convert:num:"+st.replaceAll(" ", "").toUpperCase());
		}
		catch (Exception e)
		{
		     System.out.println("Error! tried to parse an invalid number format");
		   }
		}
	
	private static void reestr(){
		System.out.println("reestr:01");
		try{
			//Preferences localPreferences=Preferences.systemRoot().node("/ru/spb/iac/35");
			Preferences localPreferences=Preferences.userRoot().node("/ru/spb/iac/35");
			localPreferences.remove("user");
			//.put("admin", "avbubnov");
		}catch(Exception e){
			 System.out.println("reestr:Error:"+e);
		}
	}
	
	private static String xml_bind(String id){
		
		String result = null;
				
		try{
			
		System.out.println("xml_bind:01");
		
		StringWriter sw = new StringWriter();
		
		JAXBContext contextObj = JAXBContext.newInstance(Token.class);
	
	    Marshaller marshallerObj = contextObj.createMarshaller();
	  //  marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
	    
	    marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
/*
	    Student myStudent = new Student();
	    myStudent.setGender("M");
	    myStudent.setName("Amar");
	    myStudent.setAge(20);
	    Book book = new Book();
	    book.setNameBook("12345");
	    myStudent.setBook(book);
	  */  
	    Calendar cln = Calendar.getInstance();
	    Long token_issue = cln.getTimeInMillis();
		cln.add(Calendar.HOUR_OF_DAY, 2);
		Long token_expire = cln.getTimeInMillis();
		  
	    Token tk = new Token();
	    tk.setId(id);
	    tk.setAuthType("cert");
	    tk.setDateIssue(token_issue.toString());
	    tk.setDateExpire(token_expire.toString());
	    
	   // marshallerObj.marshal(tk, new FileOutputStream("token.xml")); 
	    marshallerObj.marshal(tk, sw); 
	    
	    result= sw.toString();
	    System.out.println("xml_bind:result:"+result);
	   
	    /*
	    StringReader reader = new StringReader(sw.toString());
	    
	    Unmarshaller u = contextObj.createUnmarshaller();

	    Token tk_out = 
	    (Token)u.unmarshal(reader); 

	    System.out.println("main:DateIssue:"+tk_out.getDateIssue());
	    
	    */
	
		}catch(Exception e){
			 System.out.println("xml_bind:Error:"+e);
		}
		
		return result;
	}
	
	private static void split() {
		
		System.out.println("split:01");
		
		try{
		 String st = " Иванов иван  ";
		 
		 String[] result={"", "", ""};
		 
		 String[] fioArray=st.trim().split("\\s+");
		 
		 System.out.println("split:length:"+fioArray.length);
		 
		 for(int i=0; i<3; i++ ){
			 
			 if(i<fioArray.length){
				 result[i]=fioArray[i];
			 }
			 
		 }
		
		 
		 System.out.println("split:0:"+result[0]);
		 System.out.println("split:1:"+result[1]);
		 System.out.println("split:2:"+result[2]);
		 
		}catch (Exception e){
		     System.out.println("split:Error:"+e);
		   }
		}
   private static void delete_file() {
		
	System.out.println("delete_file:01");
	File file=null;
	//String file_path="/distr/jboss/data/audit/token/";
	String file_path="/home/jboss/jboss/data/audit/token/";
	BufferedWriter bw=null;
	int BUFF_SIZE = 1000*1024;
	 
	try{
	   file=new File(file_path+"audit_token_test.txt");
	   bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"Cp1251"), BUFF_SIZE);

	  bw.append("ID_SRV"+"\t"+
			    "UP_USERS"+"\t"+
			    "SIGN_OBJECT"+"\t"+
			    "CREATED"+"\t"+
			    "UP_SERVICE"+"\n");
	  
	  System.out.println("delete_file:02");
	}catch (Exception e){
	     System.out.println("delete_file:Error:"+e);
	 }finally{
		 try{
			 if(bw!=null){
				bw.close  ();
			  }
			 Thread.sleep(3000);
			 
			 file.delete();
			 
		 }catch (Exception e) {} 
		}
	}
   
   private static void map() {
		
		System.out.println("map:01");
		
		try{
			
	//	 ConcurrentHashMap<String, String> controls = new ConcurrentHashMap<String, String>();
		
		 boolean result = BindingProcessor.getControls().containsKey("binding_no_act");
			 
		 System.out.println("map:result:0:"+result);	
			
		 BindingProcessor.getControls().put("binding_no_act", "");
		 
		 result = BindingProcessor.getControls().containsKey("binding_no_act");
		 
		 System.out.println("map:result:1:"+result);
		 
		 BindingProcessor.getControls().remove("binding_no_act");
		 
         result = BindingProcessor.getControls().containsKey("binding_no_act");
		 
		 System.out.println("map:result:2:"+result);
		 
		}catch (Exception e){
		     System.out.println("map:Error:"+e);
		   }
		}
   
     private static void replace() {
		try
		{
		   System.out.println("replace:01");
		   
		   String st = null+"qwqee,  erer";
		   
		   //st = st.replaceAll(" ", "7");
		   
		   System.out.println("replace:02:"+st);
		   
		}
		catch (Exception e)
		{
		     System.out.println("replace:Error:"+e);
		   }
		}
}
