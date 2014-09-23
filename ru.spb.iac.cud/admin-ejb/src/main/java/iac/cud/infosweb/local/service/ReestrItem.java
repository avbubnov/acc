package iac.cud.infosweb.local.service;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class ReestrItem {

	private static final Map<String, String> lookup = new HashMap<String,String>();

    static {
      for(ServiceReestrPro s : EnumSet.allOf(ServiceReestrPro.class)){
    	  try{
           lookup.put(s.name(), (String)ServiceReestr.class.getDeclaredField(s.name()).get(null));
          }catch(Exception e){
        	  System.out.println("ReestrItem:Error:"+e);
          }
         }
    }
    
    public static String getUrl(String s){
    	return lookup.get(s);
    }
}
