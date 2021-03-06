package iac.grn.infosweb.context.proc.bindunbind;

import iac.cud.infosweb.dataitems.BaseParamItem;
import iac.cud.infosweb.local.service.IHLocal;
import iac.cud.infosweb.local.service.ServiceReestr;
import iac.cud.infosweb.local.service.ServiceReestrAction;
import iac.cud.infosweb.remote.frontage.IRemoteFrontageLocal;
import iac.grn.infosweb.session.audit.export.ActionsMap;
import iac.grn.infosweb.session.audit.export.AuditExportData;
import iac.grn.infosweb.session.audit.export.ResourcesMap;
import iac.grn.serviceitems.ProcBUBInfoItem;
import iac.grn.serviceitems.ProcBUBItem;
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

@Name("procBindUnBindManager")
public class ProcBindUnBindManager {

	@Logger private Log log;
	
	//private static final String proc_binding_unbind_exec_file=System.getProperty("jboss.server.config.url")+"proc_binding_unbind_exec.properties";
	private static final String proc_binding_unbind_exec_file=System.getProperty("jboss.server.config.dir")+"/"+"proc_binding_unbind_exec.properties";

	//private static final String proc_binding_unbind_info_file=System.getProperty("jboss.server.config.url")+"proc_binding_unbind_info.properties";
	private static final String proc_binding_unbind_info_file=System.getProperty("jboss.server.config.dir")+"/"+"proc_binding_unbind_info.properties";
		
	private Date startDate;
	
	private Long period=1L;
	
	//private Long periodUpd=1L;
	
	private ProcBUBItem procBUBBean;
	
	private ProcBUBInfoItem procBUBInfoBean;
	
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
	public ProcBUBItem getProcBUBBean(){
		if(this.procBUBBean==null){
			initProcBUBBean();
		}
		return this.procBUBBean;
	}
	public void setProcBUBBean(ProcBUBItem procBUBBean){
		this.procBUBBean=procBUBBean;
	}
	
	@Factory
	public ProcBUBInfoItem getProcBUBInfoBean(){
		 log.info("procBindUnBindManager:getProcBUBInfoBean");
		if(this.procBUBInfoBean==null){
			initProcBUBInfoBean();
		}
		return this.procBUBInfoBean;
	}
	public void setProcBUBInfoBean(ProcBUBInfoItem procBUBInfoBean){
		this.procBUBInfoBean=procBUBInfoBean;
	}
	
	public void initProcBUBBean(){
		 log.info("procBindUnBindManager:initProcBUBBean:01");
		 
		 String start_date=null, period=null, status=null;
		 Properties properties = new Properties();
		 String path = proc_binding_unbind_exec_file;
		 InputStream is = null;
		 
		 String  remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("remoteAudit");
		 log.info("procBindUnBindManager:initProcBUBBean:remoteAudit:"+remoteAudit);
		
		 procBUBBean= new ProcBUBItem();
		 
		 if(remoteAudit!=null && remoteAudit.equals("procInfo")){
			 // ������ "��������" ������������� ��� ���������� � Center, � Bottom ������� 
		     // ����� ���������� ���������, ���� ��� ������ ��������/��������� �������
			 //	return;
		
		 }
		 
		 if(remoteAudit!=null && remoteAudit.equals("procCrt")){
			// this.confLCBean.setActive(false);
			 procBUBBean.setStatus("passive");
			 return;
		 }
		 if(remoteAudit!=null && remoteAudit.equals("procDel")){
			// this.confLCBean.setActive(true);
			 procBUBBean.setStatus("active");
			 return;
		 }
		 if(remoteAudit!=null && remoteAudit.equals("procPause")){
			 procBUBBean.setStatus("active");
			 return;
		 }
		 if(remoteAudit!=null && remoteAudit.equals("procRun")){
			 procBUBBean.setStatus("pause");
			 return;
		 }
		 
		 try {
			 DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm");
		    // URL url = new URL(path);
		    // File f=new File(url.toURI());
		     
		     File f=new File(path); 
		     
		     if(f.exists()) { 
		    	 
		    	 properties.load(is=new FileInputStream(f));
		    	 
		    	 start_date=properties.getProperty("start_date");
		    	 period=properties.getProperty("period");
		    	 status=properties.getProperty("status");
		    	 
		    	log.info("procBindUnBindManager:initProcBUBBean:start_date:"+start_date);
		    	 log.info("procBindUnBindManager:initProcBUBBean:period:"+period);
		    	 log.info("procBindUnBindManager:initProcBUBBean:status:"+status);
		    	 
		    	 if(start_date!=null&&period!=null&&status!=null){
		    		 if(status.equals("active")||status.equals("pause")){
		    			 procBUBBean.setStartDate(df.parse(start_date));
		    			 procBUBBean.setPeriod(new Long(period));
		    		   }
		    		 procBUBBean.setStatus(status);
		    		 }
		      }else{
		    	  procBUBBean.setStatus("passive");
		      }
		 }catch (Exception e) {
				log.error("procBindUnBindManager:initProcBUBBean:error:"+e);
		 }finally{
			try {
			  if(is!=null){
			    is.close();
			   }
			} catch (Exception e) {
				log.error("procBindUnBindManager:initProcBUBBean:finally:is:error:"+e);
			}
	   }    
	}
	
	public void initProcBUBInfoBean(){
		 log.info("procBindUnBindManager:initProcBUBInfoBean:01");
		 
		 String exec_date=null, exec_hit=null, conf_date=null, conf_period=null;
		 Properties properties = new Properties();
		 String path = proc_binding_unbind_info_file;
		 InputStream is = null;
		 
		 try {
			 DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm:ss");
		    // URL url = new URL(path);
		    // File f=new File(url.toURI());
		     
		     File f=new File(path); 
		     
		     if(f.exists()) { 
		    	 
		    	 procBUBInfoBean= new ProcBUBInfoItem();
		    	 
		    	 properties.load(is=new FileInputStream(f));
		    	 
		    	 exec_date=properties.getProperty("exec_date");
		    	 exec_hit=properties.getProperty("exec_hit");
		    	/* conf_date=properties.getProperty("conf_date");
		    	 conf_period=properties.getProperty("conf_period");*/
		    	 
		    	 log.info("procBindUnBindManager:initProcBUBInfoBean:exec_date:"+exec_date);
		    	 log.info("procBindUnBindManager:initProcBUBInfoBean:exec_hit:"+exec_hit);
		    	 /*log.info("procBindUnBindManager:initProcBUBInfoBean:conf_date:"+conf_date);
		    	 log.info("procBindUnBindManager:initProcBUBInfoBean:conf_period:"+conf_period);*/
		    	 
		    	 procBUBInfoBean.setExecDate(exec_date != null ? df.parse(exec_date) : null);
		    	 procBUBInfoBean.setExecHit(exec_hit != null ? (exec_hit.equals("true") ? "�������" : "����") : null);
		    	/* procBUBInfoBean.setConfDate(conf_date != null ? df.parse(conf_date) : null);
		    	 procBUBInfoBean.setConfPeriod(conf_period != null ? new Long(conf_period) : null);*/
		    	 
		     }
		  }catch (Exception e) {
				log.error("procBindUnBindManager:initProcBUBInfoBean:error:"+e);
		 }finally{
			try {
			  if(is!=null){
			    is.close();
			   }
			} catch (Exception e) {
				log.error("procBindUnBindManager:initProcBUBInfoBean:finally:is:error:"+e);
			}
	   }    
	}
	public synchronized void procCrt(){
		  log.info("procBindUnBindManager:procCrt:01");
		/*  log.info("procBindUnBindManager:procCrt:startDate:"+startDate);
		  log.info("procBindUnBindManager:procCrt:period:"+period);*/
		  
		  Properties properties = new Properties();
		  String path = proc_binding_unbind_exec_file;
		  OutputStream os = null;
		  
		if(this.period==null || this.startDate==null){
			  log.info("procBindUnBindManager:procCrt:02");
			  return;
		  }
		   
		  try {
			 DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm");
		    // URL url = new URL(path);
		    // File f=new File(url.toURI());
		 	
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
	    	   
	    	   ((IHLocal)ctx.lookup(ServiceReestr.BindingUnBind)).run(bpi);
    		   
    		   log.info("procBindUnBindManager:procCrt:03");
    	  
    		   forView("procCrt");
    		   
    		   audit(ResourcesMap.PROC_BIND_UNBIND, ActionsMap.START); 
    		   
		  }catch (Exception e) {
				log.error("procBindUnBindManager:procCrt:error:"+e);
		  }finally{
			 try {
				if(os!=null){
					 os.close();
				}
			 } catch (Exception e) {
				log.error("procBindUnBindManager:procCrt:os:error:"+e);
			 }
		 }
	}
	public synchronized void procDel(){
		log.info("procBindUnBindManager:procDel:01");
		
		InputStream is = null;
 		OutputStream os = null;
 		
		try {
		   Context ctx = new InitialContext();
		   /*IHProcArchASysLocal obj = (IHProcArchASysLocal)ctx.lookup("procarchasys.IHProcArchASys.local");
   		   obj.stopTask();*/
   		   
   		   BaseParamItem bpi = new BaseParamItem();
   		   
   		   bpi.put("gactiontype", ServiceReestrAction.PROCESS_STOP.name());
 	       
 	       ((IHLocal)ctx.lookup(ServiceReestr.BindingUnBind)).run(bpi);
   		   
   		   Properties properties = new Properties();
   		   String path = proc_binding_unbind_exec_file;
   		  
   		   //URL url = new URL(path);
	      // File f=new File(url.toURI());
	       
   		    File f=new File(path); 
   		
	      // boolean bfd = f.delete();
	      // log.info("procBindUnBindManager:procDel:bfd:"+bfd);
	       
	       properties.load(is=new FileInputStream(f));
	       properties.setProperty("status", "passive");
	       properties.store(os=new FileOutputStream(f), null);
	       
	       forView("procDel");
	       
	       audit(ResourcesMap.PROC_BIND_UNBIND, ActionsMap.STOP);
	       
        }catch (Exception e) {
				log.error("procBindUnBindManager:procDel:error:"+e);
	   }finally{
		 try {
			if(os!=null){
				 os.close();
			}
		 } catch (Exception e) {
			log.error("procBindUnBindManager:procDel:os:error:"+e);
		 }
		 try {
			  if(is!=null){
			    is.close();
			  }
		} catch (Exception e) {
			log.error("procBindUnBindManager:procDel:finally:is:error:"+e);
		}
	 }
	}
	public synchronized void procPause(){
		log.info("procBindUnBindManager:procPause:01");
		
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
	       ((IHLocal)ctx.lookup(ServiceReestr.BindingUnBind)).run(bpi);
   		   
   		   log.info("procBindUnBindManager:procPause:02");
   		   
   		   Properties properties = new Properties();
   		   String path = proc_binding_unbind_exec_file;
   		  
   		  // URL url = new URL(path);
	     //  File f=new File(url.toURI());
	       
	       File f=new File(path); 
	       
	       if(f.exists()) {
	      // boolean bfd = f.delete();
	      // log.info("procBindUnBindManager:procDel:bfd:"+bfd);
	       
	          properties.load(is=new FileInputStream(f));
	       
	          period=properties.getProperty("period");
	          start_date=properties.getProperty("start_date");
	      
	         if(period==null || start_date==null){
			    log.info("procBindUnBindManager:procRun:02");
			    return;
		      }
	 	
	          this.startDate=df.parse(start_date);
	          this.period=new Long(period);
	     
	          properties.setProperty("status", "pause");
	          properties.store(os=new FileOutputStream(f), null);
	       
	          forView("procPause");
	       }
	       
	       audit(ResourcesMap.PROC_BIND_UNBIND, ActionsMap.PAUSE);
	       
        }catch (Exception e) {
				log.error("procBindUnBindManager:procPause:error:"+e);
	   }finally{
		 try {
			if(os!=null){
				 os.close();
			}
		 } catch (Exception e) {
			log.error("procBindUnBindManager:procPause:os:error:"+e);
		 }
		 try {
			  if(is!=null){
			    is.close();
			  }
		} catch (Exception e) {
			log.error("procBindUnBindManager:procDel:finally:is:error:"+e);
		}
	 }
	}
	public synchronized void procRun(){
		  log.info("procBindUnBindManager:procRun:01");
		  
		  Properties properties = new Properties();
		  String path = proc_binding_unbind_exec_file;
		  OutputStream os = null;
		  InputStream is = null;
		  String start_date=null, period=null;
		/*  if(this.period==null || this.startDate==null){
			  log.info("procBindUnBindManager:procCrt:02");
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
				  log.info("procBindUnBindManager:procRun:02");
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
     	       
     	       ((IHLocal)ctx.lookup(ServiceReestr.BindingUnBind)).run(bpi);
    		   
    		   log.info("procBindUnBindManager:procRun:03");
    	  
    		   forView("procRun");
    		   
    		   audit(ResourcesMap.PROC_BIND_UNBIND, ActionsMap.START);
    		   
    		 }
		  }catch (Exception e) {
				log.error("procBindUnBindManager:procRun:error:"+e);
		  }finally{
			 try {
				if(os!=null){
					 os.close();
				}
			 } catch (Exception e) {
				log.error("procBindUnBindManager:procRun:os:error:"+e);
			 }
		 }
	}
	/*
	public synchronized void procUpd(){
		  log.info("procBindUnBindManager:procUpd:01");
		  log.info("procBindUnBindManager:procUpd:period:"+period);
		  
		  Properties properties = new Properties();
		  String path = System.getProperty("jboss.server.config.url")+"conf_logcontr_exec.properties";
		  OutputStream os = null;
		  InputStream is = null;
		  DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm");
		  
		  if(this.periodUpd==null){
			  log.info("procBindUnBindManager:procUpd:02");
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
		       
		     log.info("procBindUnBindManager:procUpd:03");
  	  
  	  	     forView("procUpd");
  		   //  }
		  }catch (Exception e) {
				log.error("procBindUnBindManager:procUpd:error:"+e);
		  }finally{
			 try {
				if(os!=null){
					 os.close();
				}
			 } catch (Exception e) {
				log.error("procBindUnBindManager:procUpd:os:error:"+e);
			 }
			 try {
				  if(is!=null){
				    is.close();
				   }
			} catch (Exception e) {
				log.error("procBindUnBindManager:procUpd:finally:is:error:"+e);
			}
		 }
	}*/
	
	private void forView(String type){
	   try {
		   
		   procBUBBean= new ProcBUBItem();
		   
		  if(type.equals("procCrt")){
		    this.procBUBBean.setPeriod(this.period);
		    this.procBUBBean.setStartDate(this.startDate);
		 //   this.confLCBean.setActive(true);
		    procBUBBean.setStatus("active");
		  }else if (type.equals("procDel")){ 
		//	this.confLCBean.setActive(false);
			  procBUBBean.setStatus("passive");
		  }else if (type.equals("procPause")){
			this.procBUBBean.setPeriod(this.period);
			this.procBUBBean.setStartDate(this.startDate);
			procBUBBean.setStatus("pause");
		  }else if (type.equals("procRun")){
			this.procBUBBean.setPeriod(this.period);
			this.procBUBBean.setStartDate(this.startDate);
			procBUBBean.setStatus("active");
		  }
		  /*else if (type.equals("procUpd")){ 
			this.confLCBean.setPeriod(this.periodUpd);
			this.confLCBean.setStartDate(this.startDate);
			this.confLCBean.setActive(true);
		  }*/
		  
		  Contexts.getEventContext().set("procBUBBean", this.procBUBBean);
		  
	   }catch (Exception e) {
		  log.error("procBindUnBindManager:forView:error:"+e);
	   }
	}
	
	 public void audit(ResourcesMap resourcesMap, ActionsMap actionsMap){
		   try{
			   AuditExportData auditExportData = (AuditExportData)Component.getInstance("auditExportData",ScopeType.SESSION);
			   auditExportData.addFunc(resourcesMap.getCode()+":"+actionsMap.getCode());
			   
			   log.info("procBindUnBindManager:audit:"+(resourcesMap.getCode()+":"+actionsMap.getCode()));
			   
		   }catch(Exception e){
			   log.error("procBindUnBindManager:audit:error:"+e);
		   }
	  }
}
