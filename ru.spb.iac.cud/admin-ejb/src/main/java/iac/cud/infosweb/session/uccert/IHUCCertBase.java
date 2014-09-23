package iac.cud.infosweb.session.uccert;

import iac.cud.infosweb.dataitems.BaseParamItem;
import iac.cud.infosweb.local.service.ServiceReestrAction;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import iac.cud.infosweb.local.service.IHLocal;
import iac.cud.infosweb.local.service.ServiceReestr;


public class IHUCCertBase{
  
	
	public BaseParamItem run(BaseParamItem paramMap)throws Exception{
		
		BaseParamItem jpi = new BaseParamItem();
		
		String gactiontype = (String)paramMap.get("gactiontype");
		
		System.out.println("IHUCCertBase:run:gactiontype:"+gactiontype);
		
    	/*if(gactiontype.equals(ServiceReestrAction.TO_ARCHIVE.name())){
			jpi=to_archive(paramMap);
        }*/
		if(gactiontype.equals(ServiceReestrAction.PROCESS_START.name())){
			jpi=process_start(paramMap);
        }else if(gactiontype.equals(ServiceReestrAction.PROCESS_STOP.name())){
        	jpi=process_stop(paramMap);
        }else if(gactiontype.equals(ServiceReestrAction.TASK_RUN.name())){
        	jpi=task_run(paramMap);
        }
		return jpi;
	}

  /* public BaseParamItem to_archive(BaseParamItem paramMap)throws Exception{
		
		BaseParamItem jpi = new BaseParamItem();
		
		return jpi;
   }*/
   
   public BaseParamItem process_start(BaseParamItem paramMap)throws Exception{
		
		BaseParamItem jpi = new BaseParamItem();
		
		System.out.println("IHUCCertBase:process_start");
		
		return jpi;
   }

   public BaseParamItem process_stop(BaseParamItem paramMap)throws Exception{
	
	BaseParamItem jpi = new BaseParamItem();
	
	return jpi;
   }
   
   public BaseParamItem task_run(BaseParamItem paramMap)throws Exception{
		
		BaseParamItem jpi = new BaseParamItem();
		
		return jpi;
  }
   /* 
   public BaseParamItem clean(BaseParamItem paramMap, Date clearDate1, Date clearDate2)throws Exception{
	   Context ctx=new InitialContext(); ;
	   return ((IHLocal)ctx.lookup(ServiceReestr.Archive)).run(paramMap);
   }
   
   protected boolean isExecuted(String startStr, String endStr) {
       try {
         return false;
        }catch (Exception e) {
         System.out.println("isExecuted:error:"+e);
         return true;
       }finally {
        try{ }catch (Exception e) {}
       }
     }
   */
  
}
