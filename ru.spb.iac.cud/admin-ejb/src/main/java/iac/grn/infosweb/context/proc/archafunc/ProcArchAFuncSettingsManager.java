package iac.grn.infosweb.context.proc.archafunc;

import iac.grn.infosweb.session.audit.actions.ActionsMap;
import iac.grn.infosweb.session.audit.actions.ResourcesMap;
import iac.grn.infosweb.session.audit.export.AuditExportData;

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

@Name("procArchAFuncSettingsManager")
public class ProcArchAFuncSettingsManager {

	@Logger private Log log;
	
	@In
	EntityManager entityManager;
	 
	
	private static String param_code="to_archive_audit_func";
	
	public void init(){
		
		String monthInterval = null;
		
		try{
		   log.info("ConfLoadDataSettingsManager:init:01");
		   
		    String  remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("remoteAudit");
		    log.info("confLoadDataManager:init:remoteAudit:"+remoteAudit);
	
		     if(remoteAudit!=null ){ 
		    	 //��� ���������� ��������
		    	 //procArchAFuncSettingsBean ������������� �� �����
			     //�� ������������� ������������ � EventContext
			     //�� ������� ����� �� � �����������
			   return;
			 }
		   
		    
		     ProcArchAFuncSettingsBean beanSettings = new ProcArchAFuncSettingsBean();
		    	
		     
				 
			 log.info("ConfLoadDataSettingsManager:getCLDSBeanView:01");
			 
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
		    	  
			      Contexts.getEventContext().set("procArchAFuncSettingsBean", beanSettings);
			     
			      
			      
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
		   log.error("confLoadDataSettingsManager:init:ERROR:"+e);
		} 
	}     
	
	public void save(){
		try{
		   log.info("confLoadDataSettingsManager:save:01");
		  
		   OutputStream os = null;
			 
		   ProcArchAFuncSettingsBean beanSettings = (ProcArchAFuncSettingsBean) 
				   Contexts.getEventContext().get("procArchAFuncSettingsBean");
		   
		   
		   
			  if(beanSettings.getParamActualData()==null){
				  log.info("confLoadDataSettingsManager:save:02");
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
	                      
			     log.info("confLoadDataSettingsManager:save:03");
	    	  
			     //procArchAFuncSettingsBean ������������� �� �����
			     //�� ������������� ������������ � EventContext
			     //�� ������� ����� �� � �����������
			     
			     audit(ResourcesMap.PROC_ARCH_AUDIT_USER, ActionsMap.SET_PARAM);
			     
		  	  }catch (Exception e) {
					log.error("confLoadDataSettingsManager:save:"+e);
			  }finally{
				 try {
					if(os!=null){
						 os.close();
					}
				 } catch (Exception e) {
					log.error("confLoadDataSettingsManager:save:os:error:"+e);
				 }
			 }
			   
		}catch(Exception e){
		   log.error("ConfLoadDataSettingsManager:save:ERROR:"+e);
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
