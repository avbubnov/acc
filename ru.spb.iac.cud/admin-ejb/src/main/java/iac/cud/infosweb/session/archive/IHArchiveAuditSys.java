package iac.cud.infosweb.session.archive;

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
import java.util.HashMap;
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

//import org.jboss.seam.annotations.Logger;
//import org.jboss.seam.log.Log;
import org.apache.log4j.Logger;

@Stateless
//@LocalBinding(jndiBinding=ServiceReestr.ArchiveAuditSys)
@TransactionManagement(TransactionManagementType.BEAN)  
public class IHArchiveAuditSys extends IHArchiveBase implements IHLocal {
  
   @PersistenceContext(unitName="InfoSCUD-web")
   EntityManager em;

   @Resource UserTransaction utx;
   
   //@Logger private Log log;
   private Logger log = Logger.getLogger(IHArchiveAuditSys.class);

   //private static String file_path="/distr/jboss/data/audit/sys/";
   private static String file_path="/home/jboss/jboss/data/audit/sys/";
   
   private static String param_code="to_archive_audit_sys";
	
  // private static final String proc_aasys_info_file=System.getProperty("jboss.server.config.url")+"proc_aasys_info.properties";
   private static final String proc_aasys_info_file=System.getProperty("jboss.server.config.dir")+"/"+"proc_aasys_info.properties";
	
   private static final ScheduledExecutorService scheduler =  
       Executors.newScheduledThreadPool(1);
   
   public BaseParamItem process_start(BaseParamItem paramMap)throws Exception{
		
	BaseParamItem jpi = new BaseParamItem();
	
	log.info("IHArchiveAuditSys:process_start");
	
	ScheduledFuture shf =  scheduler.scheduleAtFixedRate(new Runnable() {
	      public void run() {
	    	 
	    	// String path = System.getProperty("jboss.server.config.url")+"conf_loaddata_exec.properties";
			
		     try {
		   
		       log.info("IHArchiveAuditSys:process_start:run");
		        
		     //  synchronized(this){
		      
		    	Calendar cln = Calendar.getInstance();   
		    	
		    	int day = cln.get(Calendar.DAY_OF_MONTH);
		    		
		    	log.info("IHArchiveAuditSys:process_start:run:day:"+day);		
		    	
		    	if(day==1){
		    	  //_LaunchAuditSysArchiveRepeatTask lct = new _LaunchAuditSysArchiveRepeatTask();
		      	  //lct.content();
		    		process_start_content(null);
		    	}
			
		  //    }
		     }catch (Exception e) {
		        log.error("IHArchiveAuditSys:process_start:run:error:"+e);
		     }finally{
				try {
				
				}catch (Exception e) {
					log.error("IHArchiveAuditSys:process_start:run:finally:error:"+e);
				}
			 }    
	 }
	},	calendar(), 24*60*60*1000, TimeUnit.MILLISECONDS);  
	
	//Contexts.getApplicationContext().set("archiveAuditSysScheduled", shf);

	if(TaskProcessor.getControls().containsKey("archiveAuditSysScheduled")){
		try{
			TaskProcessor.getControls().get("archiveAuditSysScheduled").cancel(false);
		}catch(Exception e){
			log.info("IHArchiveAuditSys:process_start:error:"+e);
		}
	}
	TaskProcessor.getControls().put("archiveAuditSysScheduled", shf);
	
	return jpi;
   }

   public BaseParamItem process_stop(BaseParamItem paramMap)throws Exception{
	
	BaseParamItem jpi = new BaseParamItem();
	
	log.info("IHArchiveAuditSysa:process_stop:01");
	
	 try{
    	// ScheduledFuture shf = (ScheduledFuture) Contexts.getApplicationContext().get("archiveAuditSysScheduled");
    	
    	 ScheduledFuture shf =TaskProcessor.getControls().get("archiveAuditSysScheduled");
    	 
    	 log.info("IHArchiveAuditSys:process_stop:02");
         if(shf!=null){       //может быть = null, когда приостановили, а потом отключаем
    	   shf.cancel(false);
    	 }
   }catch(Exception e){
    	log.error("IHArchiveAuditSys:process_stop:error:"+e);
  		throw e;
	 }

	
	return jpi;
   }
   
   public BaseParamItem task_run(BaseParamItem paramMap)throws Exception{
	   
	    BaseParamItem jpi = new BaseParamItem();
		
		log.info("IHArchiveAuditSys:task_run:01");
		
		try{
			Long archiveParamValue=(Long)paramMap.get("archiveParamValue");
			
			process_start_content(archiveParamValue);
			 
		 }catch(Exception e){
		    	log.error("IHArchiveAuditSys:task_run:error:"+e);
		  		throw e;
		 }
	   return jpi;
   }
   
   private synchronized void process_start_content(Long archiveParamValue) throws Exception{
		
		BaseParamItem bpi = new BaseParamItem();
		String prev_date=null;
		BufferedWriter bw=null;
		File file=null; 
		int i=1;
		String monthInterval = null;   
		boolean hit = true;
		OutputStream os = null;

		
		log.info("IHArchiveAuditSys:process_start_content:archiveParamValue:"+archiveParamValue);
	     
	    try{
	    	 utx.begin();

	    	 File dir = new File(file_path);
	    	 if(!dir.exists()){
	    		 dir.mkdirs();
	    	 }
	    	
	    	 if(archiveParamValue==null){
	    	 
	    	    List<String> los = em.createNativeQuery(
		              "select ST.VALUE_PARAM "+
                      "from SETTINGS_KNL_T st "+
                      "where ST.SIGN_OBJECT=? ")
                      .setParameter(1, param_code)
                      .getResultList();
	    	    
	    	    if(los!=null&&!los.isEmpty()){
	    	      monthInterval=los.get(0);
	    	    }
	    	  
	    	  }else{
	    		  monthInterval=archiveParamValue.toString() ; 
	    	  }
	    	  
	    	  if(monthInterval==null){ 
	    		  monthInterval="6";
	    	  }	    	  
	    	  
	    	  log.info("IHArchiveAuditSys:process_start_content:monthInterval:"+monthInterval);
	    	  
	    	  List<Object[]> lo = em.createNativeQuery(
	    			              "select to_char(SL.CREATED , 'YYYY_MM') vdate, SL.ID_SRV, SL.UP_SERVICES, SL.UP_USERS, " +
	    			              "to_char(SL.CREATED,'DD.MM.YYYY HH24:MI:SS') CREATED, SL.INPUT_PARAM, SL.RESULT_VALUE, SL.IP_ADDRESS "+
                                  "from SERVICES_LOG_KNL_T SL "+
                                  "where SL.CREATED < to_date('01.'||to_char(SYSDATE - INTERVAL '"+monthInterval+"' month, 'MM.YYYY'),'DD.MM.YYYY') " +
                                  "order by SL.CREATED ")
                               // .setParameter(1, monthInterval.toString())
                                .getResultList();
	    	  log.info("IHArchiveAuditSys:process_start_content:02");
	    	  
	    	 
			  int BUFF_SIZE = 1000*1024;
	    	  
	    	  for(Object[] objectArray :lo){
	    		  if(prev_date==null||!prev_date.equals(objectArray[0].toString())){
	    			  
	    			  if(bw!=null){
	    				  bw.flush();
	    				  bw.close();
	    			  }
	    			  
	    			  file=new File(file_path+"audit_sys_"+objectArray[0].toString()+".txt");
					  bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"Cp1251"), BUFF_SIZE);
				 
					  bw.append("ID_SRV"+"\t"+
							    "UP_SERVICES"+"\t"+
							    "UP_USERS"+"\t"+
							    "CREATED"+"\t"+
							    "INPUT_PARAM"+"\t"+
							    "RESULT_VALUE"+"\t"+
							    "IP_ADDRESS"+"\n");
	    		  }
				   
				   bw.append((objectArray[1]!=null?objectArray[1].toString():"null")+"\t"+
						     (objectArray[2]!=null?objectArray[2].toString():"null")+"\t"+
						     (objectArray[3]!=null?objectArray[3].toString():"null")+"\t"+
						     (objectArray[4]!=null?objectArray[4].toString():"null")+"\t"+
						     (objectArray[5]!=null?objectArray[5].toString():"null")+"\t"+
						     (objectArray[6]!=null?objectArray[6].toString():"null")+"\t"+
						     (objectArray[7]!=null?objectArray[7].toString():"null")+"\n");
				   
				  i++;

				  if((i % 100)==0){
		    		  bw.flush();
			      }
				  
				  prev_date = objectArray[0].toString();
	    	  }
	    	  if(bw!=null){
	    	    bw.flush();
	    	  }
	    	  
	    	  
	    	  em.createNativeQuery(
		              "delete from SERVICES_LOG_KNL_T SL "+
                      "where SL.CREATED < to_date('01.'||to_char(SYSDATE - INTERVAL '"+monthInterval+"' month, 'MM.YYYY'),'DD.MM.YYYY') ")
	    	  .executeUpdate();
	    
	    	  utx.commit();
	    	//  jpi.put("list", JiList) ;
	      }catch(Exception e){
	    	  log.error("IHArchiveAuditSys:process_start_content:error"+e);
	    	  
	    	  utx.rollback();
	    	  
	    	  /* можно в принципе файл и оставить.
	    	     если в базе сведения не удалились, 
	    	     то при следующем запуске файл будет перезаписан
	    	  if(bw!=null){
				 bw.close();
			  }
		      file.delete();*/
		    	  
	    	  hit=false;
              throw e;
      	  }finally{
			  
			 try{
						
				 log.info("IHArchiveAuditSys:process_start_content:finally:hit:"+hit);
						 
					   DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm:ss");
					   //URL url = new URL(proc_aasys_info_file);
					  // File f=new File(url.toURI()); 
					   
					   File f=new File(proc_aasys_info_file); 
					   
					   Properties properties = new Properties();

					   properties.setProperty("exec_date", df.format(System.currentTimeMillis()));
					   properties.setProperty("exec_hit", hit ? "true" : "false");
					   
					   properties.store(os=new FileOutputStream(f), null);
					   
			}catch(Exception e){
				log.error("ConfLoadDataTask:run:error:2:"+e);
			 }finally{
				 try {
					if(os!=null){
						os.close();
					 }
				} catch (Exception e) {}
				 try{
					  if(bw!=null){
						bw.close  ();
					  }
				}catch (Exception e) {} 
			}
		 }
	}
   
   private static Long calendar() {
		// System.out.println("calendar:01");
		 
		 Long currentTime=System.currentTimeMillis();
		 
		 // 4.40 в jboss - это в реальном времени 5.40
		 
		 Calendar cln = Calendar.getInstance();
		 cln.set(Calendar.HOUR_OF_DAY, 4);
		 cln.set(Calendar.MINUTE, 40);
		 cln.set(Calendar.SECOND, 0);
		 cln.set(Calendar.MILLISECOND, 0);
		 
		 Long trans = cln.getTimeInMillis();
		 
		 Long start = trans-currentTime;
		 
		 if(start<=0){
			 start=start+24*60*60*1000;
		 }
		 
		 System.out.println("IHArchiveAuditSys:calendar:start:"+start);
			
		 return start;
		 //return 5000L;
		}
   
}
