package iac.grn.infosweb.session.audit.export;

import iac.cud.authmodule.dataitem.AuthItem;
import iac.cud.infosweb.dataitems.NavigItem;
import iac.cud.infosweb.entity.AcAppPage;
import iac.cud.infosweb.entity.AcUser;
import iac.cud.infosweb.ws.AccessServiceClient;
import iac.cud.infosweb.ws.AuditServiceClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import iac.grn.infosweb.session.navig.LinksMap;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;

import ru.spb.iac.cud.items.AuditFunction;


/**
 * ”правл€ющий бин, осуществл€ющий реализацию навигации 
 * по используемым ресурсам приложени€
 * @author bubnov
 *
 */
@Name("auditExportManager")
public class AuditExportManager {

	 @Logger private Log log;
	 
	// static Logger logger = Logger.getLogger(GRuNProFile.class);
	 
	 @In 
	 EntityManager entityManager;
	
	 @In
	 LinksMap linksMap;
	
	/* 
	 public void export(List<AuditFunction> funcList, String tokenID) throws Exception{
		 log.info("auditExportManager:export:01");
		 
		    try {
		    	
		    	//AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
		    	
		    	AccessServiceClient asc = (AccessServiceClient)Component.getInstance("asClient",ScopeType.EVENT);
		    	
		    //	AuditExportData auditExportData = (AuditExportData)Component.getInstance("auditExportData",ScopeType.SESSION);

		    //	List<AuditFunction> funcList = auditExportData.getFuncList();
		    	
		    	log.info("auditExportManager:export:funcList:"+(funcList!=null?funcList.size():"null"));
		    	log.info("auditExportManager:export:tokenID:"+tokenID);
		    	
		    	if(funcList!=null&&!funcList.isEmpty()){
		    		asc.audit(tokenID, funcList); 
		    		//asc.audit(au.getTokenID(), funcList); 
		    	}
		    	
		      } catch (Exception e) {
		    	 log.error("auditExportManager:export:ERROR:"+e);
		         throw e;
		   }
		    
		    log.info("auditExportManager:export:02");
	 }
	 */
	 
	 public void export(List<AuditFunction> funcList, String uid) throws Exception{
		 log.info("auditExportManager:export:01");
		 
		 //tokenID теперь[11.12.13] не используетс€
		 
		    try {
		    	
		    	//AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
		    	
		    	AuditServiceClient asc = (AuditServiceClient)Component.getInstance("auditServicesClient",ScopeType.EVENT);
		    	
		    //	AuditExportData auditExportData = (AuditExportData)Component.getInstance("auditExportData",ScopeType.SESSION);

		    //	List<AuditFunction> funcList = auditExportData.getFuncList();
		    	
		    	log.info("auditExportManager:export:funcList:"+(funcList!=null?funcList.size():"null"));
		    	log.info("auditExportManager:export:uid:"+uid);
		    	
		    	if(funcList!=null&&!funcList.isEmpty()){
		    		asc.audit(uid, funcList); 
		     	}
		    	
		      } catch (Exception e) {
		    	 log.error("auditExportManager:export:ERROR:"+e);
		    	 log.trace(e);
		    	 
		    	 e.printStackTrace(System.out);
		    	
		         throw e;
		   }
		    
		    log.info("auditExportManager:export:02");
	 }
	 
}

