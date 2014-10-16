package iac.grn.infosweb.context.proc.bindunbind;

import iac.grn.infosweb.session.audit.export.ActionsMap;
import iac.grn.infosweb.session.audit.export.AuditExportData;
import iac.grn.infosweb.session.audit.export.ResourcesMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;

@Name("procBindUnBindSettingsManager")
public class ProcBindUnBindSettingsManager {

	@Logger private Log log;
	
	@In
	EntityManager entityManager;
	 
		
	private static String param_code="to_archive_audit_func";
	
	public void init(){
		
		String monthInterval = null;
		
		try{
		   log.info("procBindUnBindSettingsManager:init:01");
		   
		    String  remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("remoteAudit");
		    log.info("procBindUnBindSettingsManager:init:remoteAudit:"+remoteAudit);
	
		     if(remoteAudit!=null /*&& !remoteAudit.equals("procSetting")*/){ 
		    	 //при сохранении настроек
		    	 //procArchAFuncSettingsBean устанавливать не нужно
			     //он автоматически продолжается в EventContext
			     //от оправки формы до её отображения
			   return;
			 }
		   
		    
		     ProcBindUnBindSettingsBean beanSettings = new ProcBindUnBindSettingsBean();
		    	
		   	 
			 log.info("procBindUnBindSettingsManager:getCLDSBeanView:01");
			 
			 InputStream is = null;
			 
			 try {
				 
					 List<String> los = entityManager.createNativeQuery(
			              "select ST.VALUE_PARAM "+
	                      "from SETTINGS_KNL_T st "+
	                      "where ST.SIGN_OBJECT=? ")
	                      .setParameter(1, param_code)
	                      .getResultList();
		    	  
		    	  if(los!=null&&!los.isEmpty()){
		    	    monthInterval=los.get(0);
		    	  }
		    	  
		    	  if(monthInterval==null){ 
		    		  monthInterval="6";
		    	  }	    	 
			     
		    	  beanSettings.setParamActualData(new Long(monthInterval));
		    	  
			      Contexts.getEventContext().set("procBindUnBindSettingsBean", beanSettings);
			     
			      
			      
			  }catch (Exception e) {
					log.error("confLoadDataManager:initConfLDInfoBean:error:"+e);
			 }finally{
				try {
				  if(is!=null){
				    is.close();
				   }
				} catch (Exception e) {
					log.error("confLoadDataManager:initConfLDInfoBean:finally:is:error:"+e);
				}
		   }    
		  
		   
		}catch(Exception e){
		   log.error("procBindUnBindSettingsManager:init:ERROR:"+e);
		} 
	}     
	
	public void save(){
		try{
		   log.info("procBindUnBindSettingsManager:save:01");
		  
		   OutputStream os = null;
			 
		   ProcBindUnBindSettingsBean beanSettings = (ProcBindUnBindSettingsBean) 
				   Contexts.getEventContext().get("procBindUnBindSettingsBean");
		   
		   
		   
			  if(beanSettings.getParamActualData()==null){
				  log.info("procBindUnBindSettingsManager:save:02");
				  return;
			  }
			   
			  try {
					  entityManager.createNativeQuery(
			              "update SETTINGS_KNL_T st " +
			              "set ST.VALUE_PARAM=? "+
	                      "where ST.SIGN_OBJECT=? ")
	                      .setParameter(1, beanSettings.getParamActualData())
	                      .setParameter(2, param_code)
	                      .executeUpdate();
	                      
			     log.info("procBindUnBindSettingsManager:save:03");
	    	  
			     //procArchAFuncSettingsBean устанавливать не нужно
			     //он автоматически продолжается в EventContext
			     //от оправки формы до её отображения
			     
			     audit(ResourcesMap.PROC_ARCH_AUDIT_USER, ActionsMap.SET_PARAM);
			     
		  	  }catch (Exception e) {
					log.error("procBindUnBindSettingsManager:save:"+e);
			  }finally{
				 try {
					if(os!=null){
						 os.close();
					}
				 } catch (Exception e) {
					log.error("procBindUnBindSettingsManager:save:os:error:"+e);
				 }
			 }
			   
		}catch(Exception e){
		   log.error("procBindUnBindSettingsManager:save:ERROR:"+e);
		} 
	}     
	 public void audit(ResourcesMap resourcesMap, ActionsMap actionsMap){
		   try{
			   AuditExportData auditExportData = (AuditExportData)Component.getInstance("auditExportData",ScopeType.SESSION);
			   auditExportData.addFunc(resourcesMap.getCode()+":"+actionsMap.getCode());
			   
		   }catch(Exception e){
			   log.error("GroupManager:audit:error:"+e);
		   }
	  }
	

	
}
