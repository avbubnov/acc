package iac.cud.infosweb.session.uccert;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public class UCCertProcessor {
	
    private static volatile ConcurrentHashMap<String, String> controls = new ConcurrentHashMap<String, String>();
	
	public static ConcurrentHashMap<String, String> getControls(){
		/*
		Set<Map.Entry<String, ScheduledFuture>> set = controls.entrySet();
        for (Map.Entry<String, ScheduledFuture> me : set) {
        	System.out.println("TaskProcessor:key:"+me.getKey());
        	if(me.getValue()!=null) {
        	  System.out.println("TaskProcessor:isCancelled:"+me.getValue().isCancelled());
        	}else{
        	  System.out.println("TaskProcessor:value:is null");	
        	}
		 }*/
		
		return controls;
	}
	
	public static void setControls(ConcurrentHashMap<String, String> pcontrols){
		controls=pcontrols;
	}
}
