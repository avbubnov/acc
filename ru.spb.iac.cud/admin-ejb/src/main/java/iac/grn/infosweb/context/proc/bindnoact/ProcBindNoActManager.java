package iac.grn.infosweb.context.proc.bindnoact;

import iac.cud.infosweb.dataitems.BaseParamItem;
import iac.cud.infosweb.local.service.IHLocal;
import iac.cud.infosweb.local.service.ServiceReestr;
import iac.cud.infosweb.local.service.ServiceReestrAction;
import iac.cud.infosweb.remote.frontage.IRemoteFrontageLocal;
import iac.grn.infosweb.session.audit.export.ActionsMap;
import iac.grn.infosweb.session.audit.export.AuditExportData;
import iac.grn.infosweb.session.audit.export.ResourcesMap;
import iac.grn.serviceitems.ProcBNAInfoItem;
import iac.grn.serviceitems.ProcBNAItem;
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

@Name("procBindNoActManager")
public class ProcBindNoActManager {

	@Logger private Log log;
	
	//private static final String proc_binding_noact_exec_file=System.getProperty("jboss.server.config.url")+"proc_binding_noact_exec.properties";
	private static final String proc_binding_noact_exec_file=System.getProperty("jboss.server.config.dir")+"/"+"proc_binding_noact_exec.properties";
	
	//private static final String proc_binding_noact_info_file=System.getProperty("jboss.server.config.url")+"proc_binding_noact_info.properties";
	private static final String proc_binding_noact_info_file=System.getProperty("jboss.server.config.dir")+"/"+"proc_binding_noact_info.properties";
		
	private Date startDate;
	
	private Long period=1L;
	
	//private Long periodUpd=1L;
	
	private ProcBNAItem procBNABean;
	
	private ProcBNAInfoItem procBNAInfoBean;
	
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
	public ProcBNAItem getProcBNABean(){
		if(this.procBNABean==null){
			initProcBNABean();
		}
		return this.procBNABean;
	}
	public void setProcBNABean(ProcBNAItem procBNABean){
		this.procBNABean=procBNABean;
	}
	
	@Factory
	public ProcBNAInfoItem getProcBNAInfoBean(){
		 log.info("procBindNoActManager:getProcBNAInfoBean");
		if(this.procBNAInfoBean==null){
			initProcBNAInfoBean();
		}
		return this.procBNAInfoBean;
	}
	public void setProcBNAInfoBean(ProcBNAInfoItem procBNAInfoBean){
		this.procBNAInfoBean=procBNAInfoBean;
	}
	
	public void initProcBNABean(){
		 log.info("procBindNoActManager:initProcBNABean:01");
		 
		 String start_date=null, period=null, status=null;
		 Properties properties = new Properties();
		 String path = proc_binding_noact_exec_file;
		 InputStream is = null;
		 
		 String  remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("remoteAudit");
		 log.info("procBindNoActManager:initProcBNABean:remoteAudit:"+remoteAudit);
		
		 procBNABean= new ProcBNAItem();
		 
		 if(remoteAudit!=null && remoteAudit.equals("procInfo")){
			 // кнопка "обновить" задействована для обновления и Center, и Bottom панелей 
		     // чтобы отобразить изменения, если кто другой запустил/остановил процесс
			 //	return;
		
		 }
		 
		 if(remoteAudit!=null && remoteAudit.equals("procCrt")){
			// this.confLCBean.setActive(false);
			 procBNABean.setStatus("passive");
			 return;
		 }
		 if(remoteAudit!=null && remoteAudit.equals("procDel")){
			// this.confLCBean.setActive(true);
			 procBNABean.setStatus("active");
			 return;
		 }
		 if(remoteAudit!=null && remoteAudit.equals("procPause")){
			 procBNABean.setStatus("active");
			 return;
		 }
		 if(remoteAudit!=null && remoteAudit.equals("procRun")){
			 procBNABean.setStatus("pause");
			 return;
		 }
		 
		 try {
			 DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm");
		    // URL url = new URL(path);
		   //  File f=new File(url.toURI());
		     
		     File f=new File(path); 
		     
		     if(f.exists()) { 
		    	 
		    	 properties.load(is=new FileInputStream(f));
		    	 
		    	 start_date=properties.getProperty("start_date");
		    	 period=properties.getProperty("period");
		    	 status=properties.getProperty("status");
		    	 
		    	log.info("procBindNoActManager:initProcBNABean:start_date:"+start_date);
		    	 log.info("procBindNoActManager:initProcBNABean:period:"+period);
		    	 log.info("procBindNoActManager:initProcBNABean:status:"+status);
		    	 
		    	 if(start_date!=null&&period!=null&&status!=null){
		    		 if(status.equals("active")||status.equals("pause")){
		    			 procBNABean.setStartDate(df.parse(start_date));
		    			 procBNABean.setPeriod(new Long(period));
		    		   }
		    		 procBNABean.setStatus(status);
		    		 }
		      }else{
		    	  procBNABean.setStatus("passive");
		      }
		 }catch (Exception e) {
				log.error("procBindNoActManager:initProcBNABean:error:"+e);
		 }finally{
			try {
			  if(is!=null){
			    is.close();
			   }
			} catch (Exception e) {
				log.error("procBindNoActManager:initProcBNABean:finally:is:error:"+e);
			}
	   }    
	}
	
	public void initProcBNAInfoBean(){
		 log.info("procBindNoActManager:initProcBNAInfoBean:01");
		 
		 String exec_date=null, exec_hit=null, conf_date=null, conf_period=null;
		 Properties properties = new Properties();
		 String path = proc_binding_noact_info_file;
		 InputStream is = null;
		 
		 try {
			 DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm:ss");
		    // URL url = new URL(path);
		   //  File f=new File(url.toURI());
		     
		     File f=new File(path); 
		     
		     if(f.exists()) { 
		    	 
		    	 procBNAInfoBean= new ProcBNAInfoItem();
		    	 
		    	 properties.load(is=new FileInputStream(f));
		    	 
		    	 exec_date=properties.getProperty("exec_date");
		    	 exec_hit=properties.getProperty("exec_hit");
		    	/* conf_date=properties.getProperty("conf_date");
		    	 conf_period=properties.getProperty("conf_period");*/
		    	 
		    	 log.info("procBindNoActManager:initProcBNAInfoBean:exec_date:"+exec_date);
		    	 log.info("procBindNoActManager:initProcBNAInfoBean:exec_hit:"+exec_hit);
		    	 /*log.info("procBindNoActManager:initProcBNAInfoBean:conf_date:"+conf_date);
		    	 log.info("procBindNoActManager:initProcBNAInfoBean:conf_period:"+conf_period);*/
		    	 
		    	 procBNAInfoBean.setExecDate(exec_date != null ? df.parse(exec_date) : null);
		    	 procBNAInfoBean.setExecHit(exec_hit != null ? (exec_hit.equals("true") ? "Запущен" : "Сбой") : null);
		    	/* procBNAInfoBean.setConfDate(conf_date != null ? df.parse(conf_date) : null);
		    	 procBNAInfoBean.setConfPeriod(conf_period != null ? new Long(conf_period) : null);*/
		    	 
		     }
		  }catch (Exception e) {
				log.error("procBindNoActManager:initProcBNAInfoBean:error:"+e);
		 }finally{
			try {
			  if(is!=null){
			    is.close();
			   }
			} catch (Exception e) {
				log.error("procBindNoActManager:initProcBNAInfoBean:finally:is:error:"+e);
			}
	   }    
	}
	public synchronized void procCrt(){
		  log.info("procBindNoActManager:procCrt:01");
		  log.info("procBindNoActManager:procCrt:startDate:"+startDate);
		  log.info("procBindNoActManager:procCrt:period:"+period);
		  
		  Properties properties = new Properties();
		  String path = proc_binding_noact_exec_file;
		  OutputStream os = null;
		  
		  if(this.period==null || this.startDate==null){
			  log.info("procBindNoActManager:procCrt:02");
			  return;
		  }
		   
		  try {
			 DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm");
		   //  URL url = new URL(path);
		   //  File f=new File(url.toURI());
		 	
		     File f=new File(path); 
		     
		       properties.setProperty("start_date", df.format(this.startDate));
		       properties.setProperty("period", this.period.toString());
		       properties.setProperty("status", "active");
		       
		       properties.store(os=new FileOutputStream(f), null);
		       
		     //  IHProcArchASysLocal obj = (IHProcArchASysLocal)ctx.lookup("procarchasys.IHProcArchASys.local");
    		  // obj.startTask(this.startDate,this.period);
    		 
		       Context ctx = new InitialContext(); 
	 	    	 
 	           BaseParamItem bpi = new BaseParamItem();
	    	   bpi.put("gactiontype", ServiceReestrAction.PROCESS_START.name());
	    	   
	    	   bpi.put("startDate", this.startDate);
	    	   bpi.put("period", this.period);
	    	   
	    	   ((IHLocal)ctx.lookup(ServiceReestr.BindingNoAct)).run(bpi);
    		   
    		   log.info("procBindNoActManager:procCrt:03");
    	  
    		   forView("procCrt");
    		   
    		   audit(ResourcesMap.PROC_BIND_NOACT, ActionsMap.START); 
    		   
		  }catch (Exception e) {
				log.error("procBindNoActManager:procCrt:error:"+e);
		  }finally{
			 try {
				if(os!=null){
					 os.close();
				}
			 } catch (Exception e) {
				log.error("procBindNoActManager:procCrt:os:error:"+e);
			 }
		 }
	}
	public synchronized void procDel(){
		log.info("procBindNoActManager:procDel:01");
		
		InputStream is = null;
 		OutputStream os = null;
 		
		try {
		   Context ctx = new InitialContext();
		   /*IHProcArchASysLocal obj = (IHProcArchASysLocal)ctx.lookup("procarchasys.IHProcArchASys.local");
   		   obj.stopTask();*/
   		   
   		   BaseParamItem bpi = new BaseParamItem();
   		   
   		   bpi.put("gactiontype", ServiceReestrAction.PROCESS_STOP.name());
 	       
 	       ((IHLocal)ctx.lookup(ServiceReestr.BindingNoAct)).run(bpi);
   		   
   		   Properties properties = new Properties();
   		   String path = proc_binding_noact_exec_file;
   		  
   		  // URL url = new URL(path);
	     //  File f=new File(url.toURI());
	       
	       File f=new File(path); 
	       
	      // boolean bfd = f.delete();
	      // log.info("procBindNoActManager:procDel:bfd:"+bfd);
	       
	       properties.load(is=new FileInputStream(f));
	       properties.setProperty("status", "passive");
	       properties.store(os=new FileOutputStream(f), null);
	       
	       forView("procDel");
	       
	       audit(ResourcesMap.PROC_BIND_NOACT, ActionsMap.STOP);
	       
        }catch (Exception e) {
				log.error("procBindNoActManager:procDel:error:"+e);
	   }finally{
		 try {
			if(os!=null){
				 os.close();
			}
		 } catch (Exception e) {
			log.error("procBindNoActManager:procDel:os:error:"+e);
		 }
		 try {
			  if(is!=null){
			    is.close();
			  }
		} catch (Exception e) {
			log.error("procBindNoActManager:procDel:finally:is:error:"+e);
		}
	 }
	}
	public synchronized void procPause(){
		log.info("procBindNoActManager:procPause:01");
		
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
	       ((IHLocal)ctx.lookup(ServiceReestr.BindingNoAct)).run(bpi);
   		   
   		   log.info("procBindNoActManager:procPause:02");
   		   
   		   Properties properties = new Properties();
   		   String path = proc_binding_noact_exec_file;
   		  
   		   //URL url = new URL(path);
	      // File f=new File(url.toURI());
	       
	       File f=new File(path); 
	       
	       if(f.exists()) {
	      // boolean bfd = f.delete();
	      // log.info("procBindNoActManager:procDel:bfd:"+bfd);
	       
	          properties.load(is=new FileInputStream(f));
	       
	          period=properties.getProperty("period");
	          start_date=properties.getProperty("start_date");
	      
	          if(period==null || start_date==null){
			    log.info("procBindNoActManager:procRun:02");
			    return;
		      }
	 	
	          this.startDate=df.parse(start_date);
	          this.period=new Long(period);
	      
	          properties.setProperty("status", "pause");
	          properties.store(os=new FileOutputStream(f), null);
	       
	          forView("procPause");
	       }
	       
	       audit(ResourcesMap.PROC_BIND_NOACT, ActionsMap.PAUSE);
	       
        }catch (Exception e) {
				log.error("procBindNoActManager:procPause:error:"+e);
	   }finally{
		 try {
			if(os!=null){
				 os.close();
			}
		 } catch (Exception e) {
			log.error("procBindNoActManager:procPause:os:error:"+e);
		 }
		 try {
			  if(is!=null){
			    is.close();
			  }
		} catch (Exception e) {
			log.error("procBindNoActManager:procDel:finally:is:error:"+e);
		}
	 }
	}
	public synchronized void procRun(){
		  log.info("procBindNoActManager:procRun:01");
		  
		  Properties properties = new Properties();
		  String path = proc_binding_noact_exec_file;
		  OutputStream os = null;
		  InputStream is = null;
		  String start_date=null, period=null;
		/*  if(this.period==null || this.startDate==null){
			  log.info("procBindNoActManager:procCrt:02");
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
		     
		       if(period==null || start_date==null){
				  log.info("procBindNoActManager:procRun:02");
				  return;
			   }
		 	 
		       this.startDate=df.parse(start_date);
		       this.period=new Long(period);
		      
		       
		       properties.setProperty("status", "active");
		       properties.store(os=new FileOutputStream(f), null);
		       
		       Context ctx = new InitialContext();
		      /* IHProcArchASysLocal obj = (IHProcArchASysLocal)ctx.lookup("procarchasys.IHProcArchASys.local");
    		   obj.startTask(df.parse(start_date), new Long(period));
    		   */
		       
    		   BaseParamItem bpi = new BaseParamItem();
     	       bpi.put("gactiontype", ServiceReestrAction.PROCESS_START.name());
     	       
     	       bpi.put("startDate", this.startDate);
	    	   bpi.put("period", this.period);
     	       
     	       ((IHLocal)ctx.lookup(ServiceReestr.BindingNoAct)).run(bpi);
    		   
    		   log.info("procBindNoActManager:procRun:03");
    	  
    		   forView("procRun");
    		   
    		   audit(ResourcesMap.PROC_BIND_NOACT, ActionsMap.START);
    		   
    		 }
		  }catch (Exception e) {
				log.error("procBindNoActManager:procRun:error:"+e);
		  }finally{
			 try {
				if(os!=null){
					 os.close();
				}
			 } catch (Exception e) {
				log.error("procBindNoActManager:procRun:os:error:"+e);
			 }
		 }
	}
	/*
	public synchronized void procUpd(){
		  log.info("procBindNoActManager:procUpd:01");
		  log.info("procBindNoActManager:procUpd:period:"+period);
		  
		  Properties properties = new Properties();
		  String path = System.getProperty("jboss.server.config.url")+"conf_logcontr_exec.properties";
		  OutputStream os = null;
		  InputStream is = null;
		  DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm");
		  
		  if(this.periodUpd==null){
			  log.info("procBindNoActManager:procUpd:02");
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
		       
		     log.info("procBindNoActManager:procUpd:03");
  	  
  	  	     forView("procUpd");
  		   //  }
		  }catch (Exception e) {
				log.error("procBindNoActManager:procUpd:error:"+e);
		  }finally{
			 try {
				if(os!=null){
					 os.close();
				}
			 } catch (Exception e) {
				log.error("procBindNoActManager:procUpd:os:error:"+e);
			 }
			 try {
				  if(is!=null){
				    is.close();
				   }
			} catch (Exception e) {
				log.error("procBindNoActManager:procUpd:finally:is:error:"+e);
			}
		 }
	}*/
	
	private void forView(String type){
	   try {
		   
		   procBNABean= new ProcBNAItem();
		   
		  if(type.equals("procCrt")){
		    this.procBNABean.setPeriod(this.period);
		    this.procBNABean.setStartDate(this.startDate);
		 //   this.confLCBean.setActive(true);
		    procBNABean.setStatus("active");
		  }else if (type.equals("procDel")){ 
		//	this.confLCBean.setActive(false);
			  procBNABean.setStatus("passive");
		  }else if (type.equals("procPause")){
			this.procBNABean.setPeriod(this.period);
			this.procBNABean.setStartDate(this.startDate);
			procBNABean.setStatus("pause");
		  }else if (type.equals("procRun")){
			this.procBNABean.setPeriod(this.period);
			this.procBNABean.setStartDate(this.startDate);
			procBNABean.setStatus("active");
		  }
		  /*else if (type.equals("procUpd")){ 
			this.confLCBean.setPeriod(this.periodUpd);
			this.confLCBean.setStartDate(this.startDate);
			this.confLCBean.setActive(true);
		  }*/
		  
		  Contexts.getEventContext().set("procBNABean", this.procBNABean);
		  
	   }catch (Exception e) {
		  log.error("procBindNoActManager:forView:error:"+e);
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
