package iac.grn.infosweb.context.proc.archasys;

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

@Name("procArchASysSettingsManager")
public class ProcArchASysSettingsManager {

	@Logger private Log log;
	
	@In
	EntityManager entityManager;
	 
	//private static final String proc_aasys_settings_file=System.getProperty("jboss.server.config.url")+"proc_aasys_settings.properties";
	//private static final String proc_aasys_settings_file=System.getProperty("jboss.server.config.dir")+"/"+"proc_aasys_settings.properties";

	private static String param_code="to_archive_audit_sys";
	
	public void init(){
		
		String monthInterval = null;
		
		try{
		   log.info("ConfLoadDataSettingsManager:init:01");
		   
		    String  remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("remoteAudit");
		    log.info("confLoadDataManager:init:remoteAudit:"+remoteAudit);
	
		     if(remoteAudit!=null /*&& !remoteAudit.equals("procSetting")*/){ 
		    	 //при сохранении настроек
		    	 //procArchASysSettingsBean устанавливать не нужно
			     //он автоматически продолжается в EventContext
			     //от оправки формы до её отображения
			   return;
			 }
		   
		    // ProcAASSettingsItem bean = new ProcAASSettingsItem();
		     ProcArchASysSettingsBean beanSettings = new ProcArchASysSettingsBean();
		    	
		     
			 String directory=null, url=null, interrupt=null, directory_output=null, 
					yesterday_only=null, current_today=null, run_eas=null, run_meta=null;
			 Properties properties = new Properties();
			 
			// String path = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("conf_loaddata_settings");
			// String path = proc_aasys_settings_file;
			 
			 log.info("ConfLoadDataSettingsManager:getCLDSBeanView:01");
			 
			 InputStream is = null;
			 
			 try {
				 
				/* 
				URL url_path = new URL(path);
			    File f=new File(url_path.toURI());
			     
			     if(f.exists()) { 
			    	 
			    	 properties.load(is=new FileInputStream(f));
			    	 
			    	 run_eas=properties.getProperty("run_eas");
			    	 run_meta=properties.getProperty("run_meta");
			    	 
			    	 bean.setRun_eas(run_eas != null ? (run_eas.equals("true") ? "Да" : "Нет") : "");
			    	 bean.setRun_meta(run_meta != null ? (run_meta.equals("true") ? "Да" : "Нет") : "");
			     
			     	 beanSettings.setRun_eas((run_eas!=null&&run_eas.equals("true") ? true : false));
			    	 beanSettings.setRun_meta((run_meta!=null&&run_meta.equals("true") ? true : false));
			  
			    }else{
			    	 bean.setRun_eas("Нет");
			    	 bean.setRun_meta("Нет");

			    	 beanSettings.setRun_eas(false);
			    	 beanSettings.setRun_meta(false);

			     }
			     
			     Contexts.getEventContext().set("cLDSBeanView", bean);*/
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
		    	  
			      Contexts.getEventContext().set("procArchASysSettingsBean", beanSettings);
			     
			      
			      
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
		   Properties properties = new Properties();
		 //  String path = proc_aasys_settings_file;
		   OutputStream os = null;
			 
		   ProcArchASysSettingsBean beanSettings = (ProcArchASysSettingsBean) 
				   Contexts.getEventContext().get("procArchASysSettingsBean");
		   
		  // log.info("confLoadDataSettingsManager:save:ParamActualData:"+beanSettings.getParamActualData());
		   
			  if(beanSettings.getParamActualData()==null){
				  log.info("confLoadDataSettingsManager:save:02");
				  return;
			  }
			   
			  try {
				/*  
				 DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm");
			     URL url = new URL(path);
			     File f=new File(url.toURI());
			     properties.setProperty("run_eas", (beanSettings.getRun_eas().booleanValue()==true?"true":"false"));
			     properties.setProperty("run_meta", (beanSettings.getRun_meta().booleanValue()==true?"true":"false"));
			       
			     properties.store(os=new FileOutputStream(f), null);
			      */
				  entityManager.createNativeQuery(
			              "update SETTINGS_KNL_T st " +
			              "set ST.VALUE_PARAM=? "+
	                      "where ST.SIGN_OBJECT=? ")
	                      .setParameter(1, beanSettings.getParamActualData())
	                      .setParameter(2, param_code)
	                      .executeUpdate();
	                      
			     log.info("confLoadDataSettingsManager:save:03");
	    	  
			     //procArchASysSettingsBean устанавливать не нужно
			     //он автоматически продолжается в EventContext
			     //от оправки формы до её отображения
			     
			     audit(ResourcesMap.PROC_ARCH_AUDIT_SYS, ActionsMap.SET_PARAM);
			     
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
	
/*	@Factory
	public ConfLoadDataSettingsItem getCLDSBeanView(){
		 log.info("ConfLoadDataSettingsManager:getCLDSBeanView:01");
		 
		 ConfLoadDataSettingsItem bean = new ConfLoadDataSettingsItem();
		 
		 String directory=null, url=null, interrupt=null, directory_output=null, 
				yesterday_only=null, current_today=null, run_eas=null, run_meta=null;
		 Properties properties = new Properties();
		 
		// String path = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("conf_loaddata_settings");
		 String path = System.getProperty("jboss.server.config.url")+"conf_loaddata_exec.settings";
		 
		 log.info("ConfLoadDataSettingsManager:getCLDSBeanView:path:"+path);
		 
		 InputStream is = null;
		 
		 try {
			// DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm");
		    URL url_path = new URL(path);
		    File f=new File(url_path.toURI());
		    // File f=new File(path);
		     
		     if(f.exists()) { 
		    	 
		    	 properties.load(is=new FileInputStream(f));
		    	 
		    	 directory=properties.getProperty("directory");
		    	 url=properties.getProperty("url");
		    	 interrupt=properties.getProperty("interrupt");
		    	 directory_output=properties.getProperty("directory_output");
		    	 yesterday_only=properties.getProperty("yesterday_only");
		    	 current_today=properties.getProperty("current_today");
		    	 run_eas=properties.getProperty("run_eas");
		    	 run_meta=properties.getProperty("run_meta");
		    	 
		    	 bean.setDirectory (directory != null ? directory : "");
		    	 bean.setUrl(url != null ? url : "");
		    	 bean.setInterrupt(interrupt!= null ? (interrupt.equals("true") ? "Да" : "Нет") : "");
		    	 bean.setDirectory_output(directory_output != null ? directory_output : "");
		    	 bean.setYesterday_only(yesterday_only != null ? (yesterday_only.equals("true") ? "Да" : "Нет") : "");
		    	 bean.setCurrent_today(current_today != null ? (current_today.equals("true") ? "Да" : "Нет") : "");
		    	 bean.setRun_eas(run_eas != null ? (run_eas.equals("true") ? "Да" : "Нет") : "");
		    	 bean.setRun_meta(run_meta != null ? (run_meta.equals("true") ? "Да" : "Нет") : "");
		     }
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
		return bean; 
	}*/
	
}
