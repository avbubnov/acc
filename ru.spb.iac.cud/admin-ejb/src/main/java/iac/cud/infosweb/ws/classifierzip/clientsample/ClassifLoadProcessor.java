package iac.cud.infosweb.ws.classifierzip.clientsample;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public class ClassifLoadProcessor {
	
    private static volatile ConcurrentHashMap<String, String> controls = new ConcurrentHashMap<String, String>();
	
	public static ConcurrentHashMap<String, String> getControls(){
			return controls;
	}
	
	public static void setControls(ConcurrentHashMap<String, String> pcontrols){
		controls=pcontrols;
	}
}
