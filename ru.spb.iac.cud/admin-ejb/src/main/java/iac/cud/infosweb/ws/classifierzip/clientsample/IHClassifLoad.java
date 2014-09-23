package iac.cud.infosweb.ws.classifierzip.clientsample;

import iac.cud.infosweb.dataitems.BaseParamItem;
import iac.cud.infosweb.local.service.IHLocal;
import iac.cud.infosweb.local.service.ServiceReestrAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
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

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.jboss.seam.contexts.Contexts;

import iac.cud.infosweb.local.service.ServiceReestr;
import iac.grn.infosweb.context.proc.TaskProcessor;




//import org.jboss.seam.annotations.Logger;
//import org.jboss.seam.log.Log;
import org.apache.log4j.Logger;

@Stateless
//@LocalBinding(jndiBinding=ServiceReestr.BindingNoAct)
@TransactionManagement(TransactionManagementType.BEAN)  
public class IHClassifLoad implements IHLocal {
  
   @PersistenceContext(unitName="InfoSCUD-web")
   EntityManager em;

   @Resource UserTransaction utx;
   
   //@Logger private Log log;
   private Logger log = Logger.getLogger(IHClassifLoad.class);

  // private static final String proc_binding_noact_info_file=System.getProperty("jboss.server.config.url")+"proc_binding_noact_info.properties";
//   private static final String proc_binding_noact_info_file=System.getProperty("jboss.server.config.dir")+"/"+"proc_binding_noact_info.properties";

   private static final ScheduledExecutorService scheduler =  
       Executors.newScheduledThreadPool(1);
   
   
   public BaseParamItem run(BaseParamItem paramMap)throws Exception{
		
		BaseParamItem jpi = new BaseParamItem();
		
		String gactiontype = (String)paramMap.get("gactiontype");
		
		System.out.println("IHClassifLoad:run:gactiontype:"+gactiontype);
		
    	if(gactiontype.equals(ServiceReestrAction.TASK_RUN.name())){
         	jpi=task_run(paramMap);
        }else if(gactiontype.equals(ServiceReestrAction.TO_WORK.name())){
         	jpi=to_work(paramMap);
        }
		return jpi;
	}


  
   public BaseParamItem task_run(BaseParamItem paramMap)throws Exception{
		
	  BaseParamItem jpi = new BaseParamItem();
	
	  log.info("IHClassifLoad:process_start:01");
	
	  final Long seancact =(Long) paramMap.get("seancact");
	  final Integer clVersion =(Integer) paramMap.get("clVersion");
	  
      ScheduledFuture shf =  scheduler.schedule(new Runnable() {
		  
	      public void run() {
	    	 
	    	// String path = System.getProperty("jboss.server.config.url")+"conf_loaddata_exec.properties";
			
		     try {
		   
		       log.info("IHClassifLoad:process_start:run");
		        
		     //  synchronized(this){
		      
		    	Calendar cln = Calendar.getInstance();   
		    	
		    	int day = cln.get(Calendar.DAY_OF_MONTH);
		    		
		    	log.info("IHClassifLoad:process_start:run:day:"+day);		
		    	
		    	process_start_content(seancact, clVersion);
		    
		     }catch (Exception e) {
		        log.error("IHClassifLoad:process_start:run:error:"+e);
		     }finally{
				try {
				
				}catch (Exception e) {
					log.error("IHClassifLoad:process_start:run:finally:error:"+e);
				}
			 }    
	 }
	},	0, TimeUnit.MILLISECONDS);  
	
	  /*
	if(TaskProcessor.getControls().containsKey("bindingNoActScheduled")){
		try{
			TaskProcessor.getControls().get("bindingNoActScheduled").cancel(false);
		}catch(Exception e){
			log.info("IHClassifLoad:process_start:error:"+e);
		}
	}
	TaskProcessor.getControls().put("bindingNoActScheduled", shf);
	*/
	return jpi;
   }

   
   
   private synchronized  void process_start_content(Long seancact, Integer clVersion) throws Exception{
		
		boolean hit = true;
		OutputStream os = null;

		
       log.info("IHClassifLoad:process_start_content:01");
	     
	    try{
	    	 if(ClassifLoadProcessor.getControls().containsKey("classif_load")){
	    		log.info("IHClassifLoad:process_start_content:return");
	    		return;
	    	 }
	    	
	    	 ClassifLoadProcessor.getControls().put("classif_load", "");
	    	
	    	 utx.begin();

	    /*	  em.createNativeQuery(
		              "delete from BINDING_AUTO_LINK_BSS_T tt " +
		              "WHERE TT.TYPE_BINDING = 2 ")
                     .executeUpdate();
	   */
	    	 
	    	 ClientSample.run2(em, utx, seancact, clVersion);
	    	 
	    	/* 
	    	 Session session = (Session) em.getDelegate();
	    	 final int[] result=new int[]{-1};
	  
			 session.doWork(new Work(){
			     public void execute(Connection conn)throws SQLException{
				    System.out.println("IHLogContrList:logContrList:execute:conn:"+conn.isClosed());
				    try{
				    	//conn.setAutoCommit(false);
				    	
				    	// PojoRunProcess pp = new PojoRunProcess(conn);
				    	// pp.startProcess();
				    	  
				    	ClientSample.run2(conn, utx);
				    	 
				    	 System.out.println("IHLogContrList:logContrList:execute:01");
				    	 
				    }catch(Exception e){
				     System.out.println("IHLogContrList:logContrList:execute:error:1:"+e);
					// throw e;
				    }finally{
				    	try{
				    	//!!!
				    	//обязательно здесь НЕ закрывать коннект!!!
				    		
				  		//  if(conn!=null&&!conn.isClosed()){
				  		//	 conn.close();
				  		//  }
				  		}catch(Exception e){}
				    }
			   }
			  }); */
	 	    
	    	  utx.commit();
	       }catch(Exception e){
	    	  log.error("IHClassifLoad:process_start_content:error"+e);
	    	  
	    	  utx.rollback();
	    	  
	    	  hit=false;
	          throw e;
	          
     	  }finally{
			  
			 try{
					
				ClassifLoadProcessor.getControls().remove("classif_load");
				 
				 log.info("IHClassifLoad:process_start_content:finally:hit:"+hit);
								   
			}catch(Exception e){
				log.error("IHClassifLoad:process_start_content:error:2:"+e);
			 }finally{
				 try {
					if(os!=null){
						os.close();
					 }
				} catch (Exception e) {}
			}
		 }
	}
  
   
   public BaseParamItem to_work(BaseParamItem paramMap)throws Exception{
		
		  BaseParamItem jpi = new BaseParamItem();
		
		  log.info("IHClassifLoad:to_work:01");
		
		 // final Long seancact =(Long) paramMap.get("seancact");
		 // final Integer clVersion =(Integer) paramMap.get("clVersion");
		  
		  try{
			  
		    utx.begin();
		    
		  
		    //чистим архив
			  //clear ISP_ARH_BSS_T
			   em.createNativeQuery(
					   "TRUNCATE TABLE ISP_ARH_BSS_T")
				.executeUpdate();
			 
			   //копируем текущее в архив на случай отката
			   //ISP_BSS_T -> ISP_ARH_BSS_T
			   em.createNativeQuery(
					   "insert /*+ APPEND */ into ISP_ARH_BSS_T( "+
					     "ID_SRV, "+
	                     "SIGN_OBJECT, "+
						  "SIGN_OKOGY, "+
						  "FULL_, "+
						  "POSITION, "+
						  "FIO, "+
						  "POSTAL_CODE, "+
						  "COUNTRY, "+
						  "REGION, "+
						  "PLACE, "+
						  "PREFIX, "+
						  "HOUSE, "+
						  "HOUSING, "+
						  "FLAT, "+
						  "PHONE, "+
						  "FAX, "+
						  "EMAIL, "+
						  "STATUS, "+
						  "DATE_IN_SRV, "+
						  "DATE_DEL_SRV, "+
						  "DOC_IN_SRV, "+
						  "DOC_DEL_SRV, "+
						  "UNI_ID, "+
						  "IS_EXTERNAL, "+
						  "SIGN_OKATO, "+
						  "UP_ISP_LOAD, "+
						  "CREATED "+
					 ") select "+
						 "ID_SRV, "+
	                     "SIGN_OBJECT, "+
						  "SIGN_OKOGY, "+
						  "FULL_, "+
						  "POSITION, "+
						  "FIO, "+
						  "POSTAL_CODE, "+
						  "COUNTRY, "+
						  "REGION, "+
						  "PLACE, "+
						  "PREFIX, "+
						  "HOUSE, "+
						  "HOUSING, "+
						  "FLAT, "+
						  "PHONE, "+
						  "FAX, "+
						  "EMAIL, "+
						  "STATUS, "+
						  "DATE_IN_SRV, "+
						  "DATE_DEL_SRV, "+
						  "DOC_IN_SRV, "+
						  "DOC_DEL_SRV, "+
						  "UNI_ID, "+
						  "IS_EXTERNAL, "+
						  "SIGN_OKATO, "+
						  "UP_ISP_LOAD, "+
						  "sysdate "+
					 //  " from ISP_TEST_BSS_T  ")
					   " from ISP_BSS_T  ")
						.executeUpdate();
			
			  //чистим текущее
			  //clear ISP_BSS_T
			   em.createNativeQuery(
					 //  "TRUNCATE TABLE ISP_TEST_BSS_T")
					    "TRUNCATE TABLE ISP_BSS_T")
				.executeUpdate();
			
			   
			   //копируем из технологич. т. в текущее
			   //ISP_TEMP_BSS_T -> ISP_BSS_T	   
			   em.createNativeQuery(
					  // "insert /*+ APPEND */ into ISP_TEST_BSS_T( "+
					   "insert /*+ APPEND */ into ISP_BSS_T( "+
					     "ID_SRV, "+
	                     "SIGN_OBJECT, "+
						  "SIGN_OKOGY, "+
						  "FULL_, "+
						  "POSITION, "+
						  "FIO, "+
						  "POSTAL_CODE, "+
						  "COUNTRY, "+
						  "REGION, "+
						  "PLACE, "+
						  "PREFIX, "+
						  "HOUSE, "+
						  "HOUSING, "+
						  "FLAT, "+
						  "PHONE, "+
						  "FAX, "+
						  "EMAIL, "+
						  "STATUS, "+
						  "DATE_IN_SRV, "+
						  "DATE_DEL_SRV, "+
						  "DOC_IN_SRV, "+
						  "DOC_DEL_SRV, "+
						  "UNI_ID, "+
						  "IS_EXTERNAL, "+
						  "SIGN_OKATO, "+
						  "UP_ISP_LOAD, "+
						  "CREATED "+
					 ") select "+
						 "ID_SRV, "+
	                     "SIGN_OBJECT, "+
						  "SIGN_OKOGY, "+
						  "FULL_, "+
						  "POSITION, "+
						  "FIO, "+
						  "POSTAL_CODE, "+
						  "COUNTRY, "+
						  "REGION, "+
						  "PLACE, "+
						  "PREFIX, "+
						  "HOUSE, "+
						  "HOUSING, "+
						  "FLAT, "+
						  "PHONE, "+
						  "FAX, "+
						  "EMAIL, "+
						  "STATUS, "+
						  "DATE_IN_SRV, "+
						  "DATE_DEL_SRV, "+
						  "DOC_IN_SRV, "+
						  "DOC_DEL_SRV, "+
						  "UNI_ID, "+
						  "IS_EXTERNAL, "+
						  "SIGN_OKATO, "+
						  "UP_ISP_LOAD, "+
						  "sysdate "+
					   " from ISP_TEMP_BSS_T  ")
						.executeUpdate();
			
			   em.createNativeQuery(
					   "TRUNCATE TABLE ISP_TEMP_BSS_T")
				.executeUpdate();
			   
		    utx.commit();
		    
		  }catch(Exception e){
	    	  log.error("IHClassifLoad:to_work:error"+e);
	    	  
	    	  utx.rollback();
	    	  
	    	  throw e;
	          
     	  }
		  
		return jpi;
	   }
   
 
}
