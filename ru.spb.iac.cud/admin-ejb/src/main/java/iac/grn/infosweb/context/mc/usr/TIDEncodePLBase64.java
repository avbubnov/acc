package iac.grn.infosweb.context.mc.usr;

import java.util.Random;

import org.picketlink.common.util.Base64;

public class TIDEncodePLBase64 {

	public static String getNewKey_not_used() {

		
		Random random = new Random();
	    
		byte[] keyRandom = new byte[12];
        random.nextBytes(keyRandom);

	       String key = Base64.encodeBytes(keyRandom);
        try {
     	   //System.out.println("main:1:"+key);
          // key += Base64.encode((InetAddress.getLocalHost()).getAddress());
        //   key += Base64.encode(Long.toString(System.currentTimeMillis()).getBytes());
       //    System.out.println("main:2:"+Long.toString(System.currentTimeMillis()).getBytes());
        } catch (Exception e) {
	           e.printStackTrace();
	       }
	       key += Long.toString(System.currentTimeMillis());
	       
	       String result =  Base64.encodeBytes(key.getBytes());
	       System.out.println("main:result:"+result);
	       
	       return result;
	   }
	
    public static String getSecret() {

		
		Random random = new Random();
	    
		byte[] keyRandom = new byte[3];
        random.nextBytes(keyRandom);

	    String key = Base64.encodeBytes(keyRandom);
        try {
     	   //System.out.println("main:1:"+key);
          // key += Base64.encode((InetAddress.getLocalHost()).getAddress());
        //   key += Base64.encode(Long.toString(System.currentTimeMillis()).getBytes());
       //    System.out.println("main:2:"+Long.toString(System.currentTimeMillis()).getBytes());
        } catch (Exception e) {
	           e.printStackTrace();
	       }
	       key += Long.toString(System.currentTimeMillis());
	       
	       String result =  Base64.encodeBytes(key.getBytes());
	       System.out.println("main:result:"+result);
	       
	       return result;
	   }
    
    public static void main(String[] args) {
		
		 System.out.println("main:01");
		 
		 try{
			 String key = TIDEncodePLBase64.getNewKey_not_used();
			 
			 String secret = TIDEncodePLBase64.getSecret();
			 
			 System.out.println("main:02");
		 }catch(Exception e){
			 e.printStackTrace(System.out);
			 
			System.out.println("error:"+e);	  
		 }

	}
    
}
