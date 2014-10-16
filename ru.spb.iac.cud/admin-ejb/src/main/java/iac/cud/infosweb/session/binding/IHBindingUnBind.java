package iac.cud.infosweb.session.binding;

import iac.cud.infosweb.dataitems.BaseParamItem;
import iac.cud.infosweb.local.service.IHLocal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap; import java.util.Map;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import javax.transaction.UserTransaction;
import javax.ejb.TransactionManagementType;
import org.jboss.seam.contexts.Contexts;
import iac.cud.infosweb.local.service.ServiceReestr;
import iac.grn.infosweb.context.proc.TaskProcessor;

 
 
import org.apache.log4j.Logger;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)  
public class IHBindingUnBind extends IHBindingBase implements IHLocal {
  
   @PersistenceContext(unitName="InfoSCUD-web")
   EntityManager em;

   @Resource UserTransaction utx;
   
   private Logger log = Logger.getLogger(IHBindingUnBind.class);

   private static final String proc_binding_unbind_info_file=System.getProperty("jboss.server.config.dir")+"/"+"proc_binding_unbind_info.properties";
	
   
   private static final ScheduledExecutorService scheduler =  
       Executors.newScheduledThreadPool(1);
   
   public BaseParamItem process_start(BaseParamItem paramMap)throws Exception{
		
	BaseParamItem jpi = new BaseParamItem();
	
	log.info("IHBindingUnBind:process_start");
	  Date startDate =(Date) paramMap.get("startDate");
	  Long period = (Long) paramMap.get("period");
	  
	  
	  if(startDate==null||period==null){
		log.info("IHBindingUnBind:process_start:return");
		return jpi;
	  }
	  
	  Long currentTime=System.currentTimeMillis();
		Long trans=startDate.getTime();
		
		Long start=trans-currentTime;
		
		log.info("IHBindingUnBind:process_start:start:1:"+start);
		
		while(start<0){
			
			log.info("IHBindingUnBind:process_start:01:2");
			
			int batch=0;
			while(trans<currentTime){
				batch++;
				trans+=period*24*60*60*1000;
				if(batch % 100 == 0){
					log.info("IHBindingUnBind:process_start:batch:"+batch);
				}
			}
			start=trans-System.currentTimeMillis();
			currentTime=System.currentTimeMillis();
			
			log.info("IHBindingUnBind:process_start:start:2:"+start);
		}
		
	ScheduledFuture shf =  scheduler.scheduleAtFixedRate(new Runnable() {
	      public void run() {
	    	 
	    	
		     try {
		   
		       log.info("IHBindingUnBind:process_start:run");
		        
		     	Calendar cln = Calendar.getInstance();   
		    	
		    	int day = cln.get(Calendar.DAY_OF_MONTH);
		    		
		    	log.info("IHBindingUnBind:process_start:run:day:"+day);		
		    	
		    	  
		    		process_start_content();
		
		     }catch (Exception e) {
		        log.error("IHBindingUnBind:process_start:run:error:"+e);
		     }finally{
				try {
				
				}catch (Exception e) {
					log.error("IHBindingUnBind:process_start:run:finally:error:"+e);
				}
			 }    
	 }
	},	start, period*24*60*60*1000, TimeUnit.MILLISECONDS);  
	
	
	if(TaskProcessor.getControls().containsKey("bindingUnBindScheduled")){
		try{
			TaskProcessor.getControls().get("bindingUnBindScheduled").cancel(false);
		}catch(Exception e){
			log.info("IHBindingUnBind:process_start:error:"+e);
		}
	}
	TaskProcessor.getControls().put("bindingUnBindScheduled", shf);
	
	return jpi;
   }

   public BaseParamItem process_stop(BaseParamItem paramMap)throws Exception{
	
	BaseParamItem jpi = new BaseParamItem();
	
	log.info("IHConfLoadData:stopTask:01");
	
	 try{
    	
    	 ScheduledFuture shf =TaskProcessor.getControls().get("bindingUnBindScheduled");
    	 
    	 log.info("IHConfLoadData:stopTask:02");
         if(shf!=null){       //может быть = null, когда приостановили, а потом отключаем
    	   shf.cancel(false);
    	 }
   }catch(Exception e){
    	log.error("IHConfLoadData:stopTask:error:"+e);
  		throw e;
	 }

	
	return jpi;
   }
   
   public BaseParamItem task_run(BaseParamItem paramMap)throws Exception{
	   
	    BaseParamItem jpi = new BaseParamItem();
		
		log.info("IHBindingUnBind:task_run:01");
		
		try{
			
			process_start_content();
			 
		 }catch(Exception e){
		    	log.error("IHBindingUnBind:task_run:error:"+e);
		  		throw e;
		 }
	   return jpi;
  }
   
   private synchronized void process_start_content() throws Exception{
		
		boolean hit = true;
		OutputStream os = null;

		
        log.info("IHBindingUnBind:process_start_content:01");
	     
	    try{
	    	
	    	if(BindingProcessor.getControls().containsKey("binding_un_bind")){
	    		log.info("IHBindingUnBind:process_start_content:return");
	    		return;
	    	}
	    	
	    	 BindingProcessor.getControls().put("binding_un_bind", "");
	    	
	    	 
	    	  utx.begin();

	    	  em.createNativeQuery(
		              "delete from BINDING_AUTO_LINK_BSS_T tt " +
		              "WHERE TT.TYPE_BINDING = 1 ")
                      .executeUpdate();
	    	 
	    	  em.createNativeQuery(
		              "INSERT INTO BINDING_AUTO_LINK_BSS_T (UP_USERS, UP_ISP_SIGN_USER, TYPE_BINDING) "+
                      "select AU.ID_SRV, CL_USER.SIGN_OBJECT, 1 "+
                      "from AC_USERS_KNL_T au, "+
                      "ISP_BSS_T cl_user "+
                      "where AU.UP_SIGN_USER is null "+
                      "and CL_USER.STATUS='A' "+
                      "and CL_USER.FIO like AU.SURNAME ||'% '||AU.NAME_ || '%'||AU.PATRONYMIC "+
                      "and AU.UP_SIGN= substr(CL_USER.SIGN_OBJECT,1,3)||'00000' " +
	    			  "group by AU.ID_SRV, CL_USER.SIGN_OBJECT ")
                    .executeUpdate();
	    	  
	 	    
	    	  utx.commit();
	       }catch(Exception e){
	    	  log.error("IHBindingUnBind:process_start_content:error"+e);
	    	  
	    	  utx.rollback();
	    	  
	    	  hit=false;
	          throw e;
	          
      	  }finally{
			  
			 try{
				 BindingProcessor.getControls().remove("binding_un_bind");
				 
				 log.info("IHBindingUnBind:process_start_content:finally:hit:"+hit);
						 
					   DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm:ss");
						   
					   File f=new File(proc_binding_unbind_info_file); 
					   
					   Properties properties = new Properties();

					   properties.setProperty("exec_date", df.format(System.currentTimeMillis()));
					   properties.setProperty("exec_hit", hit ? "true" : "false");
					   
					   properties.store(os=new FileOutputStream(f), null);
					   
			}catch(Exception e){
				log.error("IHBindingUnBind:process_start_content:error:2:"+e);
			 }finally{
				 try {
					if(os!=null){
						os.close();
					 }
				} catch (Exception e) {}
			}
		 }
	}
  	
   
}
