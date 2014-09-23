package iac.grn.infosweb.session;

import iac.grn.infosweb.session.audit.export.AuditExportData;
import iac.grn.infosweb.session.audit.export.AuditExportManager;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.log.Log;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;

//@Scope(ScopeType.EVENT)
//@Name("sessionObserver")
public class SessionObserver {

	@Logger private Log log;
	 
	//@Observer("org.jboss.seam.preDestroyContext.SESSION")
	//@Transactional
	public void onSessionDestroyed()
	{
		 System.out.println("---SessionObserver:onSessionDestroyed:01");
		 log.info("SessionObserver:onSessionDestroyed");
		 
		/* new Thread(
					new Runnable(){
					      public void run() {
					    	  
		                      auditExport();
					     }
					   }
					).start();*/
		// System.out.println("___SessionObserver:onSessionDestroyed:02");
	}
	
	private void auditExport(){
		 try{
			 System.out.println("SessionObserver:auditExport:01");
		
			// Thread.sleep(5000);
			 
			 Lifecycle.beginCall();
			 
			 System.out.println("SessionObserver:auditExport:02");
			 
			 /*	 AuditExportManager auditExportManager = (AuditExportManager)Component.getInstance("auditExportManager",ScopeType.EVENT);
			 
			 System.out.println("SessionObserver:auditExport:03");
			 
			 auditExportManager.export();*/
			   
			 System.out.println("SessionObserver:auditExport:04");
			 
			
			 
		   }catch(Exception e){
			   log.error("SessionObserver:audit:error:"+e);
		   }finally{
			   System.out.println("SessionObserver:auditExport:05");
			   Lifecycle.endCall();
			   System.out.println("SessionObserver:auditExport:06");
		   }
		 System.out.println("SessionObserver:auditExport:07");
	}
}
