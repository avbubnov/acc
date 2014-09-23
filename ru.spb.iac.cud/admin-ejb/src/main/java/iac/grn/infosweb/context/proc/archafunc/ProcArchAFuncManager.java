package iac.grn.infosweb.context.proc.archafunc;

import iac.cud.infosweb.dataitems.BaseParamItem;
import iac.cud.infosweb.local.service.IHLocal;
import iac.cud.infosweb.local.service.ServiceReestr;
import iac.cud.infosweb.local.service.ServiceReestrAction;
import iac.cud.infosweb.remote.frontage.IRemoteFrontageLocal;
import iac.grn.infosweb.session.audit.export.ActionsMap;
import iac.grn.infosweb.session.audit.export.AuditExportData;
import iac.grn.infosweb.session.audit.export.ResourcesMap;
import iac.grn.serviceitems.ProcAAFInfoItem;
import iac.grn.serviceitems.ProcAAFItem;
import iac.grn.serviceitems.ProcInfoItem;
import iac.grn.serviceitems.ProcItem;

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

@Name("procArchAFuncManager")
public class ProcArchAFuncManager {

	@Logger private Log log;
	
	//private static final String proc_aafunc_exec_file=System.getProperty("jboss.server.config.url")+"proc_aafunc_exec.properties";
	private static final String proc_aafunc_exec_file=System.getProperty("jboss.server.config.dir")+"/"+"proc_aafunc_exec.properties";
	
	//private static final String proc_aafunc_info_file=System.getProperty("jboss.server.config.url")+"proc_aafunc_info.properties";
	private static final String proc_aafunc_info_file=System.getProperty("jboss.server.config.dir")+"/"+"proc_aafunc_info.properties";
	
	private Date startDate;
	
	private Long period=1L;
	
	//private Long periodUpd=1L;
	
	private ProcAAFItem procAAFBean;
	
	private ProcAAFInfoItem procAAFInfoBean;
	
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
	
	/*public Long getPeriodUpd(){
		return this.periodUpd;
	}
	public void setPeriodUpd(Long periodUpd){
		this.periodUpd=periodUpd;
	}*/
	
	@Factory
	public ProcAAFItem getProcAAFBean(){
		if(this.procAAFBean==null){
			initProcAAFBean();
		}
		return this.procAAFBean;
	}
	public void setProcAAFBean(ProcAAFItem procAAFBean){
		this.procAAFBean=procAAFBean;
	}
	
	@Factory
	public ProcAAFInfoItem getProcAAFInfoBean(){
		 log.info("__confLogContrManager:getProcAAFInfoBean");
		if(this.procAAFInfoBean==null){
			initProcAAFInfoBean();
		}
		return this.procAAFInfoBean;
	}
	public void setProcAAFInfoBean(ProcAAFInfoItem procAAFInfoBean){
		this.procAAFInfoBean=procAAFInfoBean;
	}
	
	public void initProcAAFBean(){
		 log.info("confLogContrManager:initProcAAFBean:01");
		 
		 String start_date=null, period=null, status=null;
		 Properties properties = new Properties();
		 String path = proc_aafunc_exec_file;
		 InputStream is = null;
		 
		 String  remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("remoteAudit");
		 log.info("confLogContrManager:initProcAAFBean:remoteAudit:"+remoteAudit);
		
		 procAAFBean= new ProcAAFItem();
		 
		 if(remoteAudit!=null && remoteAudit.equals("procInfo")){
			 // кнопка "обновить" задействована для обновления и Center, и Bottom панелей 
		     // чтобы отобразить изменения, если кто другой запустил/остановил процесс
			 //	return;
		
		 }
		 
		 if(remoteAudit!=null && remoteAudit.equals("procCrt")){
			// this.confLCBean.setActive(false);
			 procAAFBean.setStatus("passive");
			 return;
		 }
		 if(remoteAudit!=null && remoteAudit.equals("procDel")){
			// this.confLCBean.setActive(true);
			 procAAFBean.setStatus("active");
			 return;
		 }
		 if(remoteAudit!=null && remoteAudit.equals("procPause")){
			 procAAFBean.setStatus("active");
			 return;
		 }
		 if(remoteAudit!=null && remoteAudit.equals("procRun")){
			 procAAFBean.setStatus("pause");
			 return;
		 }
		 
		 try {
			 DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm");
		    // URL url = new URL(path);
		     //File f=new File(url.toURI());
		     
			 File f=new File(path); 
			 
		     if(f.exists()) { 
		    	 
		    	 properties.load(is=new FileInputStream(f));
		    	 
		    	/* start_date=properties.getProperty("start_date");
		    	 period=properties.getProperty("period");*/
		    	 status=properties.getProperty("status");
		    	 
		    	/* log.info("confLogContrManager:initProcAAFBean:start_date:"+start_date);
		    	 log.info("confLogContrManager:initProcAAFBean:period:"+period);*/
		    	 log.info("confLogContrManager:initProcAAFBean:status:"+status);
		    	 
		    	 if(/*start_date!=null&&period!=null&&*/status!=null){
		    		 if(status.equals("active")||status.equals("pause")){
		    		  /* confLCBean.setStartDate(df.parse(start_date));
		    		   confLCBean.setPeriod(new Long(period));*/
		    		   }
		    		 procAAFBean.setStatus(status);
		    		 }
		      }else{
		    	  procAAFBean.setStatus("passive");
		      }
		 }catch (Exception e) {
				log.error("confLogContrManager:initProcAAFBean:error:"+e);
		 }finally{
			try {
			  if(is!=null){
			    is.close();
			   }
			} catch (Exception e) {
				log.error("confLogContrManager:initProcAAFBean:finally:is:error:"+e);
			}
	   }    
	}
	
	public void initProcAAFInfoBean(){
		 log.info("confLogContrManager:initProcAAFInfoBean:01");
		 
		 String exec_date=null, exec_hit=null, conf_date=null, conf_period=null;
		 Properties properties = new Properties();
		 String path = proc_aafunc_info_file;
		 InputStream is = null;
		 
		 try {
			 DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm:ss");
		    // URL url = new URL(path);
		    // File f=new File(url.toURI());
		     
			 File f=new File(path); 
			 
		     if(f.exists()) { 
		    	 
		    	 procAAFInfoBean= new ProcAAFInfoItem();
		    	 
		    	 properties.load(is=new FileInputStream(f));
		    	 
		    	 exec_date=properties.getProperty("exec_date");
		    	 exec_hit=properties.getProperty("exec_hit");
		    	/* conf_date=properties.getProperty("conf_date");
		    	 conf_period=properties.getProperty("conf_period");*/
		    	 
		    	 log.info("confLogContrManager:initProcAAFInfoBean:exec_date:"+exec_date);
		    	 log.info("confLogContrManager:initProcAAFInfoBean:exec_hit:"+exec_hit);
		    	 /*log.info("confLogContrManager:initProcAAFInfoBean:conf_date:"+conf_date);
		    	 log.info("confLogContrManager:initProcAAFInfoBean:conf_period:"+conf_period);*/
		    	 
		    	 procAAFInfoBean.setExecDate(exec_date != null ? df.parse(exec_date) : null);
		    	 procAAFInfoBean.setExecHit(exec_hit != null ? (exec_hit.equals("true") ? "Запущен" : "Сбой") : null);
		    	/* procAAFInfoBean.setConfDate(conf_date != null ? df.parse(conf_date) : null);
		    	 procAAFInfoBean.setConfPeriod(conf_period != null ? new Long(conf_period) : null);*/
		    	 
		     }
		  }catch (Exception e) {
				log.error("confLogContrManager:initProcAAFInfoBean:error:"+e);
		 }finally{
			try {
			  if(is!=null){
			    is.close();
			   }
			} catch (Exception e) {
				log.error("confLogContrManager:initProcAAFInfoBean:finally:is:error:"+e);
			}
	   }    
	}
	public synchronized void procCrt(){
		  log.info("confLogContrManager:procCrt:01");
		/*  log.info("confLogContrManager:procCrt:startDate:"+startDate);
		  log.info("confLogContrManager:procCrt:period:"+period);*/
		  
		  Properties properties = new Properties();
		  String path = proc_aafunc_exec_file;
		  OutputStream os = null;
		  
		 /* if(this.period==null || this.startDate==null){
			  log.info("confLogContrManager:procCrt:02");
			  return;
		  }*/
		   
		  try {
			 DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm");
		     //URL url = new URL(path);
		    // File f=new File(url.toURI());
		 	
		     File f=new File(path); 
		     
		      /* properties.setProperty("start_date", df.format(this.startDate));
		       properties.setProperty("period", this.period.toString());*/
		       properties.setProperty("status", "active");
		       
		       properties.store(os=new FileOutputStream(f), null);
		       
		     //  IHProcArchASysLocal obj = (IHProcArchASysLocal)ctx.lookup("procarchasys.IHProcArchASys.local");
    		  // obj.startTask(this.startDate,this.period);
    		 
		       Context ctx = new InitialContext(); 
	 	    	 
 	           BaseParamItem bpi = new BaseParamItem();
	    	   bpi.put("gactiontype", ServiceReestrAction.PROCESS_START.name());
	    	   ((IHLocal)ctx.lookup(ServiceReestr.ArchiveAuditFunc)).run(bpi);
    		   
    		   log.info("confLogContrManager:procCrt:03");
    	  
    		   forView("procCrt");
    		   
    		   audit(ResourcesMap.PROC_ARCH_AUDIT_USER, ActionsMap.START); 
    		   
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
		   /*IHProcArchASysLocal obj = (IHProcArchASysLocal)ctx.lookup("procarchasys.IHProcArchASys.local");
   		   obj.stopTask();*/
   		   
   		   BaseParamItem bpi = new BaseParamItem();
   		   
   		   bpi.put("gactiontype", ServiceReestrAction.PROCESS_STOP.name());
 	       
 	       ((IHLocal)ctx.lookup(ServiceReestr.ArchiveAuditFunc)).run(bpi);
   		   
   		   Properties properties = new Properties();
   		   String path = proc_aafunc_exec_file;
   		  
   		  // URL url = new URL(path);
	      // File f=new File(url.toURI());
	       
   		   File f=new File(path); 
   		
	      // boolean bfd = f.delete();
	      // log.info("confLogContrManager:procDel:bfd:"+bfd);
	       
	       properties.load(is=new FileInputStream(f));
	       properties.setProperty("status", "passive");
	       properties.store(os=new FileOutputStream(f), null);
	       
	       forView("procDel");
	       
	       audit(ResourcesMap.PROC_ARCH_AUDIT_USER, ActionsMap.STOP);
	       
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
 		DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm");
 		String start_date=null, period=null;
		try {
		   Context ctx = new InitialContext();
		 /*  IHProcArchASysLocal obj = (IHProcArchASysLocal)ctx.lookup("procarchasys.IHProcArchASys.local");
   		   obj.stopTask();*/
   		   
   		   BaseParamItem bpi = new BaseParamItem();
	       bpi.put("gactiontype", ServiceReestrAction.PROCESS_STOP.name());
	       ((IHLocal)ctx.lookup(ServiceReestr.ArchiveAuditFunc)).run(bpi);
   		   
   		   log.info("confLogContrManager:procPause:02");
   		   
   		   Properties properties = new Properties();
   		   String path = proc_aafunc_exec_file;
   		  
   		  // URL url = new URL(path);
	     //  File f=new File(url.toURI());
	       
	       File f=new File(path); 
	       
	       if(f.exists()) {
	      // boolean bfd = f.delete();
	      // log.info("confLogContrManager:procDel:bfd:"+bfd);
	       
	          properties.load(is=new FileInputStream(f));
	       
	          period=properties.getProperty("period");
	          start_date=properties.getProperty("start_date");
	      
	          /*if(period==null || start_date==null){
			    log.info("confLogContrManager:procRun:02");
			    return;
		      }
	 	
	          this.startDate=df.parse(start_date);
	          this.period=new Long(period);
	       */
	          properties.setProperty("status", "pause");
	          properties.store(os=new FileOutputStream(f), null);
	       
	          forView("procPause");
	       }
	       
	       audit(ResourcesMap.PROC_ARCH_AUDIT_USER, ActionsMap.PAUSE);
	       
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
		  String path = proc_aafunc_exec_file;
		  OutputStream os = null;
		  InputStream is = null;
		  String start_date=null, period=null;
		/*  if(this.period==null || this.startDate==null){
			  log.info("confLogContrManager:procCrt:02");
			  return;
		  }*/
		   
		  try {
			 DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm");
		    // URL url = new URL(path);
		    // File f=new File(url.toURI());
		     
		     File f=new File(path); 
		     
		     if(f.exists()) {
		    	 
		       properties.load(is=new FileInputStream(f));
		       
		       period=properties.getProperty("period");
		       start_date=properties.getProperty("start_date");
		     
		      /* if(period==null || start_date==null){
				  log.info("confLogContrManager:procRun:02");
				  return;
			   }
		 	 
		       this.startDate=df.parse(start_date);
		       this.period=new Long(period);
		       */
		       
		       properties.setProperty("status", "active");
		       properties.store(os=new FileOutputStream(f), null);
		       
		       Context ctx = new InitialContext();
		      /* IHProcArchASysLocal obj = (IHProcArchASysLocal)ctx.lookup("procarchasys.IHProcArchASys.local");
    		   obj.startTask(df.parse(start_date), new Long(period));
    		   */
		       
    		   BaseParamItem bpi = new BaseParamItem();
     	       bpi.put("gactiontype", ServiceReestrAction.PROCESS_START.name());
     	       ((IHLocal)ctx.lookup(ServiceReestr.ArchiveAuditFunc)).run(bpi);
    		   
    		   log.info("confLogContrManager:procRun:03");
    	  
    		   forView("procRun");
    		   
    		   audit(ResourcesMap.PROC_ARCH_AUDIT_USER, ActionsMap.START);
    		   
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
	/*
	public synchronized void procUpd(){
		  log.info("confLogContrManager:procUpd:01");
		  log.info("confLogContrManager:procUpd:period:"+period);
		  
		  Properties properties = new Properties();
		  String path = System.getProperty("jboss.server.config.url")+"conf_logcontr_exec.properties";
		  OutputStream os = null;
		  InputStream is = null;
		  DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm");
		  
		  if(this.periodUpd==null){
			  log.info("confLogContrManager:procUpd:02");
			  return;
		  }
		   
		  try {
			 URL url = new URL(path);
		     File f=new File(url.toURI());
		 	// if(f.exists()) { 
		     properties.load(is=new FileInputStream(f));
		     
		     this.startDate=df.parse(properties.getProperty("start_date"));
		     
		     properties.setProperty("period", this.periodUpd.toString());
		     properties.store(os=new FileOutputStream(f), null);
		       
		     log.info("confLogContrManager:procUpd:03");
  	  
  	  	     forView("procUpd");
  		   //  }
		  }catch (Exception e) {
				log.error("confLogContrManager:procUpd:error:"+e);
		  }finally{
			 try {
				if(os!=null){
					 os.close();
				}
			 } catch (Exception e) {
				log.error("confLogContrManager:procUpd:os:error:"+e);
			 }
			 try {
				  if(is!=null){
				    is.close();
				   }
			} catch (Exception e) {
				log.error("confLogContrManager:procUpd:finally:is:error:"+e);
			}
		 }
	}*/
	
	private void forView(String type){
	   try {
		   
		   procAAFBean= new ProcAAFItem();
		   
		  if(type.equals("procCrt")){
		   /* this.procAAFBean.setPeriod(this.period);
		    this.procAAFBean.setStartDate(this.startDate);*/
		 //   this.confLCBean.setActive(true);
		    procAAFBean.setStatus("active");
		  }else if (type.equals("procDel")){ 
		//	this.confLCBean.setActive(false);
			  procAAFBean.setStatus("passive");
		  }else if (type.equals("procPause")){
			/*this.procAAFBean.setPeriod(this.period);
			this.procAAFBean.setStartDate(this.startDate);*/
			procAAFBean.setStatus("pause");
		  }else if (type.equals("procRun")){
			/*this.procAAFBean.setPeriod(this.period);
			this.procAAFBean.setStartDate(this.startDate);*/
			procAAFBean.setStatus("active");
		  }
		  /*else if (type.equals("procUpd")){ 
			this.confLCBean.setPeriod(this.periodUpd);
			this.confLCBean.setStartDate(this.startDate);
			this.confLCBean.setActive(true);
		  }*/
		  
		  Contexts.getEventContext().set("procAAFBean", this.procAAFBean);
		  
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
