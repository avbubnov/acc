package iac.grn.infosweb.context.proc;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public class TaskProcessor {

	private static volatile ConcurrentHashMap<String, ScheduledFuture> controls = new ConcurrentHashMap();
	
	public static ConcurrentHashMap<String, ScheduledFuture> getControls(){
		
		Set<Map.Entry<String, ScheduledFuture>> set = controls.entrySet();
        for (Map.Entry<String, ScheduledFuture> me : set) {
        	System.out.println("TaskProcessor:key:"+me.getKey());
        	if(me.getValue()!=null) {
        	  System.out.println("TaskProcessor:isCancelled:"+me.getValue().isCancelled());
        	}else{
        	  System.out.println("TaskProcessor:value:is null");	
        	}
		 }
		
		return controls;
	}
	
	public static void setControls(ConcurrentHashMap<String, ScheduledFuture> pcontrols){
		controls=pcontrols;
	}
}
