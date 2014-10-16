package iac.grn.infosweb.context.proc.archasys;

import iac.cud.infosweb.dataitems.BaseParamItem;
import iac.cud.infosweb.local.service.IHLocal;
import iac.cud.infosweb.local.service.ServiceReestr;
import iac.cud.infosweb.local.service.ServiceReestrAction;
import iac.cud.infosweb.remote.frontage.IRemoteFrontageLocal;
import iac.grn.infosweb.session.audit.export.ActionsMap;
import iac.grn.infosweb.session.audit.export.AuditExportData;
import iac.grn.infosweb.session.audit.export.ResourcesMap;
import iac.grn.serviceitems.ProcInfoItem;
import iac.grn.serviceitems.ProcItem;
import iac.grn.serviceitems.ProcAASInfoItem;
import iac.grn.serviceitems.ProcAASItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;

@Name("procArchASysManager")
public class ProcArchASysManager {

	@Logger private Log log;
	
	private static final String proc_aasys_exec_file=System.getProperty("jboss.server.config.dir")+"/"+"proc_aasys_exec.properties";
	
	private static final String proc_aasys_info_file=System.getProperty("jboss.server.config.dir")+"/"+"proc_aasys_info.properties";
	
	private Date startDate;
	
	private Long period=1L;
	
	
	private ProcAASItem procAASBean;
	
	private ProcAASInfoItem procAASInfoBean;
	
	public Date getStartDate(){
		return this.startDate;
	}
	public void setStartDate(Date startDate){
		this.startDate=startDate;
	}
	
	public Long getPeriod(){
		return this.period;
	}
	public void setPeriod(Long period){
		this.period=period;
	}
	
	
	
	@Factory
	public ProcAASItem getProcAASBean(){
		if(this.procAASBean==null){
			initProcAASBean();
		}
		return this.procAASBean;
	}
	public void setProcAASBean(ProcAASItem procAASBean){
		this.procAASBean=procAASBean;
	}
	
	@Factory
	public ProcAASInfoItem getProcAASInfoBean(){
		 log.info("__confLogContrManager:getProcAASInfoBean");
		if(this.procAASInfoBean==null){
			initProcAASInfoBean();
		}
		return this.procAASInfoBean;
	}
	public void setProcAASInfoBean(ProcAASInfoItem procAASInfoBean){
		this.procAASInfoBean=procAASInfoBean;
	}
	
	public void initProcAASBean(){
		 log.info("confLogContrManager:initProcAASBean:01");
		 
		 String status=null;
		 Properties properties = new Properties();
		 String path = proc_aasys_exec_file;
		 InputStream is = null;
		 
		 String  remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("remoteAudit");
		 log.info("confLogContrManager:initProcAASBean:remoteAudit:"+remoteAudit);
		
		 procAASBean= new ProcAASItem();
		 
		 if(remoteAudit!=null && "procInfo".equals(remoteAudit)){
			 // кнопка "обновить" задействована для обновления и Center, и Bottom панелей 
		     // чтобы отобразить изменения, если кто другой запустил/остановил процесс
			 
		
		 }
		 
		 if(remoteAudit!=null && "procCrt".equals(remoteAudit)){
			 procAASBean.setStatus("passive");
			 return;
		 }
		 if(remoteAudit!=null && "procDel".equals(remoteAudit)){
			 procAASBean.setStatus("active");
			 return;
		 }
		 if(remoteAudit!=null && "procPause".equals(remoteAudit)){
			 procAASBean.setStatus("active");
			 return;
		 }
		 if(remoteAudit!=null && "procRun".equals(remoteAudit)){
			 procAASBean.setStatus("pause");
			 return;
		 }
		 
		 try {
			    
		     File f=new File(path); 
		     
		     if(f.exists()) { 
		    	 
		    	 properties.load(is=new FileInputStream(f));
		    	 
		    	 status=properties.getProperty("status");
		    	 
		    	 log.info("confLogContrManager:initProcAASBean:status:"+status);
		    	 
		    	 if(status!=null){
		    		 if(status.equals("active")||status.equals("pause")){
		    		   }
		    		 procAASBean.setStatus(status);
		    		 }
		      }else{
		    	  procAASBean.setStatus("passive");
		      }
		 }catch (Exception e) {
				log.error("confLogContrManager:initProcAASBean:error:"+e);
		 }finally{
			try {
			  if(is!=null){
			    is.close();
			   }
			} catch (Exception e) {
				log.error("confLogContrManager:initProcAASBean:finally:is:error:"+e);
			}
	   }    
	}
	
	public void initProcAASInfoBean(){
		 log.info("confLogContrManager:initProcAASInfoBean:01");
		 
		 String execDate=null, execHit=null;
		 Properties properties = new Properties();
		 String path = proc_aasys_info_file;
		 InputStream is = null;
		 
		 try {
			 DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm:ss");
		    
		    
		     
		     File f=new File(path); 
		     
		     if(f.exists()) { 
		    	 
		    	 procAASInfoBean= new ProcAASInfoItem();
		    	 
		    	 properties.load(is=new FileInputStream(f));
		    	 
		    	 execDate=properties.getProperty("exec_date");
		    	 execHit=properties.getProperty("exec_hit");
		    		 
		    	 log.info("confLogContrManager:initProcAASInfoBean:exec_date:"+execDate);
		    	 log.info("confLogContrManager:initProcAASInfoBean:exec_hit:"+execHit);
		    	 
		    	 procAASInfoBean.setExecDate(execDate != null ? df.parse(execDate) : null);
		    	 procAASInfoBean.setExecHit(execHit != null ? ("true".equals(execHit) ? "Запущен" : "Сбой") : null);
		      	 
		     }
		  }catch (Exception e) {
				log.error("confLogContrManager:initProcAASInfoBean:error:"+e);
		 }finally{
			try {
			  if(is!=null){
			    is.close();
			   }
			} catch (Exception e) {
				log.error("confLogContrManager:initProcAASInfoBean:finally:is:error:"+e);
			}
	   }    
	}
	public synchronized void procCrt(){
		  log.info("confLogContrManager:procCrt:01");
			  
		  Properties properties = new Properties();
		  String path = proc_aasys_exec_file;
		  OutputStream os = null;
		  
			   
		  try {
		 	
		     File f=new File(path); 
		     
		         properties.setProperty("status", "active");
		       
		       properties.store(os=new FileOutputStream(f), null);
		       
		     
    		  
    		 
		       Context ctx = new InitialContext(); 
	 	    	 
 	           BaseParamItem bpi = new BaseParamItem();
	    	   bpi.put("gactiontype", ServiceReestrAction.PROCESS_START.name());
	    	   ((IHLocal)ctx.lookup(ServiceReestr.ArchiveAuditSys)).run(bpi);
    		   
    		   log.info("confLogContrManager:procCrt:03");
    	  
    		   forView("procCrt");
    		   
    		   audit(ResourcesMap.PROC_ARCH_AUDIT_SYS, ActionsMap.START);
    		   
		  }catch (Exception e) {
				log.error("confLogContrManager:procCrt:error:"+e);
		  }finally{
			 try {
				if(os!=null){
					 os.close();
				}
			 } catch (Exception e) {
				log.error("confLogContrManager:procCrt:os:error:"+e);
			 }
		 }
	}
	public synchronized void procDel(){
		log.info("confLogContrManager:procDel:01");
		
		InputStream is = null;
 		OutputStream os = null;
 		
		try {
		   Context ctx = new InitialContext();
		   
   		   BaseParamItem bpi = new BaseParamItem();
   		   
   		   bpi.put("gactiontype", ServiceReestrAction.PROCESS_STOP.name());
 	       
 	       ((IHLocal)ctx.lookup(ServiceReestr.ArchiveAuditSys)).run(bpi);
   		   
   		   Properties properties = new Properties();
   		   String path = proc_aasys_exec_file;
   		  
   		  
	     
	       
	       File f=new File(path); 
	       
	      
	       
	       
	       properties.load(is=new FileInputStream(f));
	       properties.setProperty("status", "passive");
	       properties.store(os=new FileOutputStream(f), null);
	       
	       forView("procDel");
	       
	       audit(ResourcesMap.PROC_ARCH_AUDIT_SYS, ActionsMap.STOP);
	       
        }catch (Exception e) {
				log.error("confLogContrManager:procDel:error:"+e);
	   }finally{
		 try {
			if(os!=null){
				 os.close();
			}
		 } catch (Exception e) {
			log.error("confLogContrManager:procDel:os:error:"+e);
		 }
		 try {
			  if(is!=null){
			    is.close();
			  }
		} catch (Exception e) {
			log.error("confLogContrManager:procDel:finally:is:error:"+e);
		}
	 }
	}
	public synchronized void procPause(){
		log.info("confLogContrManager:procPause:01");
		
		InputStream is = null;
 		OutputStream os = null;
 		try {
		   Context ctx = new InitialContext();
		   
   		   BaseParamItem bpi = new BaseParamItem();
	       bpi.put("gactiontype", ServiceReestrAction.PROCESS_STOP.name());
	       ((IHLocal)ctx.lookup(ServiceReestr.ArchiveAuditSys)).run(bpi);
   		   
   		   log.info("confLogContrManager:procPause:02");
   		   
   		   Properties properties = new Properties();
   		   String path = proc_aasys_exec_file;
   		  
   		 
	     
	       
	       File f=new File(path); 
	       
	       if(f.exists()) {
	      
	       
	       
	          properties.load(is=new FileInputStream(f));
	       
	      
	          properties.setProperty("status", "pause");
	          properties.store(os=new FileOutputStream(f), null);
	       
	          forView("procPause");
	       }
	       
	       audit(ResourcesMap.PROC_ARCH_AUDIT_SYS, ActionsMap.PAUSE);
	       
        }catch (Exception e) {
				log.error("confLogContrManager:procPause:error:"+e);
	   }finally{
		 try {
			if(os!=null){
				 os.close();
			}
		 } catch (Exception e) {
			log.error("confLogContrManager:procPause:os:error:"+e);
		 }
		 try {
			  if(is!=null){
			    is.close();
			  }
		} catch (Exception e) {
			log.error("confLogContrManager:procDel:finally:is:error:"+e);
		}
	 }
	}
	public synchronized void procRun(){
		  log.info("confLogContrManager:procRun:01");
		  
		  Properties properties = new Properties();
		  String path = proc_aasys_exec_file;
		  OutputStream os = null;
			   
		  try {
		     
		     File f=new File(path); 
		     
		     if(f.exists()) {
		    	 
		       properties.load(new FileInputStream(f));
		       
		         
		       properties.setProperty("status", "active");
		       properties.store(os=new FileOutputStream(f), null);
		       
		       Context ctx = new InitialContext();
		          
    		   BaseParamItem bpi = new BaseParamItem();
     	       bpi.put("gactiontype", ServiceReestrAction.PROCESS_START.name());
     	       ((IHLocal)ctx.lookup(ServiceReestr.ArchiveAuditSys)).run(bpi);
    		   
    		   log.info("confLogContrManager:procRun:03");
    	  
    		   forView("procRun");
    		   
    		   audit(ResourcesMap.PROC_ARCH_AUDIT_SYS, ActionsMap.START);
    		   
    		 }
		  }catch (Exception e) {
				log.error("confLogContrManager:procRun:error:"+e);
		  }finally{
			 try {
				if(os!=null){
					 os.close();
				}
			 } catch (Exception e) {
				log.error("confLogContrManager:procRun:os:error:"+e);
			 }
		 }
	}
	
	
	private void forView(String type){
	   try {
		   
		   procAASBean= new ProcAASItem();
		   
		  if(type.equals("procCrt")){
			 
		    procAASBean.setStatus("active");
		  }else if (type.equals("procDel")){ 
			  procAASBean.setStatus("passive");
		  }else if (type.equals("procPause")){
			procAASBean.setStatus("pause");
		  }else if (type.equals("procRun")){
			procAASBean.setStatus("active");
		  }
		  
		  Contexts.getEventContext().set("procAASBean", this.procAASBean);
		  
	   }catch (Exception e) {
		  log.error("confLogContrManager:forView:error:"+e);
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
