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
	 
	//private static final String proc_binding_unbind_settings_file=System.getProperty("jboss.server.config.url")+"proc_binding_unbind_settings.properties";
	//private static final String proc_binding_unbind_settings_file=System.getProperty("jboss.server.config.dir")+"/"+"proc_binding_unbind_settings.properties";
	
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
		    	 //��� ���������� ��������
		    	 //procArchAFuncSettingsBean ������������� �� �����
			     //�� ������������� ������������ � EventContext
			     //�� ������� ����� �� � �����������
			   return;
			 }
		   
		    // ProcAASSettingsItem bean = new ProcAASSettingsItem();
		     ProcBindUnBindSettingsBean beanSettings = new ProcBindUnBindSettingsBean();
		    	
		     
			 String directory=null, url=null, interrupt=null, directory_output=null, 
					yesterday_only=null, current_today=null, run_eas=null, run_meta=null;
			 Properties properties = new Properties();
			 
			// String path = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("conf_loaddata_settings");
			// String path = proc_binding_unbind_settings_file;
			 
			 log.info("procBindUnBindSettingsManager:getCLDSBeanView:01");
			 
			 InputStream is = null;
			 
			 try {
				 
				/* 
				URL url_path = new URL(path);
			    File f=new File(url_path.toURI());
			     
			     if(f.exists()) { 
			    	 
			    	 properties.load(is=new FileInputStream(f));
			    	 
			    	 run_eas=properties.getProperty("run_eas");
			    	 run_meta=properties.getProperty("run_meta");
			    	 
			    	 bean.setRun_eas(run_eas != null ? (run_eas.equals("true") ? "��" : "���") : "");
			    	 bean.setRun_meta(run_meta != null ? (run_meta.equals("true") ? "��" : "���") : "");
			     
			     	 beanSettings.setRun_eas((run_eas!=null&&run_eas.equals("true") ? true : false));
			    	 beanSettings.setRun_meta((run_meta!=null&&run_meta.equals("true") ? true : false));
			  
			    }else{
			    	 bean.setRun_eas("���");
			    	 bean.setRun_meta("���");

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
		   Properties properties = new Properties();
		  // String path = proc_binding_unbind_settings_file;
		   OutputStream os = null;
			 
		   ProcBindUnBindSettingsBean beanSettings = (ProcBindUnBindSettingsBean) 
				   Contexts.getEventContext().get("procBindUnBindSettingsBean");
		   
		  // log.info("procBindUnBindSettingsManager:save:ParamActualData:"+beanSettings.getParamActualData());
		   
			  if(beanSettings.getParamActualData()==null){
				  log.info("procBindUnBindSettingsManager:save:02");
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
	                      
			     log.info("procBindUnBindSettingsManager:save:03");
	    	  
			     //procArchAFuncSettingsBean ������������� �� �����
			     //�� ������������� ������������ � EventContext
			     //�� ������� ����� �� � �����������
			     
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
	
/*	@Factory
	public ConfLoadDataSettingsItem getCLDSBeanView(){
		 log.info("procBindUnBindSettingsManager:getCLDSBeanView:01");
		 
		 ConfLoadDataSettingsItem bean = new ConfLoadDataSettingsItem();
		 
		 String directory=null, url=null, interrupt=null, directory_output=null, 
				yesterday_only=null, current_today=null, run_eas=null, run_meta=null;
		 Properties properties = new Properties();
		 
		// String path = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("conf_loaddata_settings");
		 String path = System.getProperty("jboss.server.config.url")+"conf_loaddata_exec.settings";
		 
		 log.info("procBindUnBindSettingsManager:getCLDSBeanView:path:"+path);
		 
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
		    	 bean.setInterrupt(interrupt!= null ? (interrupt.equals("true") ? "��" : "���") : "");
		    	 bean.setDirectory_output(directory_output != null ? directory_output : "");
		    	 bean.setYesterday_only(yesterday_only != null ? (yesterday_only.equals("true") ? "��" : "���") : "");
		    	 bean.setCurrent_today(current_today != null ? (current_today.equals("true") ? "��" : "���") : "");
		    	 bean.setRun_eas(run_eas != null ? (run_eas.equals("true") ? "��" : "���") : "");
		    	 bean.setRun_meta(run_meta != null ? (run_meta.equals("true") ? "��" : "���") : "");
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
