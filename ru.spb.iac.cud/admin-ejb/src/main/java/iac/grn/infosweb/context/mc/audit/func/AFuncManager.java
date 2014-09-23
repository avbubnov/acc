package iac.grn.infosweb.context.mc.audit.func;

//import org.jboss.ejb3.common.proxy.plugins.async.AsyncUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import java.io.File;

import iac.cud.infosweb.dataitems.AuditFuncItem;
import iac.cud.infosweb.dataitems.BaseItem;
import iac.cud.infosweb.dataitems.BaseParamItem;
import iac.cud.infosweb.entity.AcPermissionsList;
import iac.cud.infosweb.entity.AcUser;
import iac.cud.infosweb.entity.ActionsLogKnlT;
import iac.cud.infosweb.local.service.ServiceReestrAction;
import iac.cud.infosweb.local.service.ServiceReestrPro;
import iac.cud.infosweb.remote.frontage.IRemoteFrontageLocal;
import iac.cud.infosweb.ws.AccessServiceClient;
import iac.grn.infosweb.session.audit.export.ActionsMap;
import iac.grn.infosweb.session.audit.export.AuditExportData;
import iac.grn.infosweb.session.audit.export.ResourcesMap;
import iac.grn.infosweb.session.cache.CacheManager;
import iac.grn.infosweb.session.navig.LinksMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.jboss.seam.Component;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.OneToMany;
import javax.persistence.TemporalType;

import iac.grn.ramodule.entity.VAuditReport;
import iac.grn.serviceitems.BaseTableItem;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletResponse;

/**
 * Управляющий Бин
 * @author bubnov
 *
 */
@Name("aFuncManager")
public class AFuncManager {
	
	 @Logger private Log log;
	
	 @In 
	 EntityManager entityManager;
	 
	/**
     * Экспортируемая сущности 
     * для отображения
     */
	//private BaseItem usrBean;             !!! Проверить !!!
	
	private String dellMessage;
	 
	private List<BaseItem> auditList;//= new ArrayList<VAuditReport>();
	
	private List<BaseItem> auditReportList;
	
	private List<AuditFuncItem> auditReportCubeList;
	
	private Long auditReportCubeCount;
	
	private Long auditCount;
	
	private List <BaseTableItem> auditItemsListSelect;
	
	private List <BaseTableItem> auditItemsListContext;
	
	private int connectError=0;
	private Boolean evaluteForList;
	private Boolean evaluteForListFooter;  
	private Boolean evaluteForBean;
	
	private Date clearDate1;
	private Date clearDate2;
	
    private Date reportDate1;
	
	private Date reportDate2;
	
//	private List<AcApplication> listArm = null;
	
	private static String param_code="to_archive_audit_func";
	
	//private static String jndiBinding = "infoscud.IRemoteFrontage.local";
	//private static String jndiBinding = "java:global/InfoSCUD-ear/InfoS-ejb/IRemoteFrontage!iac.cud.infosweb.remote.frontage.IRemoteFrontageLocal";
	private static String jndiBinding = "java:global/InfoS-ear/InfoS-ejb/IRemoteFrontage!iac.cud.infosweb.remote.frontage.IRemoteFrontageLocal";
	
	
	private Long archiveParamValue=null;
	
	public List<BaseItem> getAuditList(int firstRow, int numberOfRows){
	  String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
	             .getRequestParameterMap()
	             .get("remoteAudit");
	   log.info("auditManager:getAuditList:remoteAudit:"+remoteAudit);
	  
	  
	  log.info("getAuditList:firstRow:"+firstRow);
	  log.info("getAuditList:numberOfRows:"+numberOfRows);
	  
	  List<BaseItem> aFuncListCached = (List<BaseItem>)
			  Component.getInstance("aFuncListCached",ScopeType.SESSION);
	  if(auditList==null){
		  log.info("getAuditList:01");
		 	if((remoteAudit.equals("rowSelectFact")||
			    remoteAudit.equals("selRecAllFact")||
			    remoteAudit.equals("clRecAllFact")||
			    remoteAudit.equals("clSelOneFact")||
			    remoteAudit.equals("onSelColSaveFact"))&&
			    aFuncListCached!=null){
		 	//	log.info("getAuditList:02:"+orgListCached.size());
			    	this.auditList=aFuncListCached;
			}else{
				log.info("getAuditList:03");
		    	invokeLocal("list", firstRow, numberOfRows, null);
			    Contexts.getSessionContext().set("aFuncListCached", this.auditList);
			    log.info("getAuditList:03:"+this.auditList.size());
			}
		 	
		 	ArrayList<String> selRecArm = (ArrayList<String>)
					  Component.getInstance("selRecaFunc",ScopeType.SESSION);
		 	if(this.auditList!=null && selRecArm!=null) {
		 		 for(BaseItem it:this.auditList){
				   if(selRecArm.contains(it.getBaseId().toString())){
					// log.info("invoke:Selected!!!");
					 it.setSelected(true);
				   }else{
					  it.setSelected(false);
				   }
				 }
		    }
		}
		return this.auditList;
	}
	public void setAuditList(List<BaseItem> auditList){
		this.auditList=auditList;
	}
	
	public List<BaseItem> getAuditReportList(){
		  String date1 = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("date1");
		  String date2 = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("date2");
		 
		  
		  DateFormat df = new SimpleDateFormat ("dd.MM.yy");
		 // DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm");
		  
		  try{
		    this.reportDate1 = df.parse(date1);
		    this.reportDate2 = df.parse(date2);
		  }catch(Exception e){
			  log.info("aFuncManager:getAuditReportList:parseError:"+e);
		  }
		  
		  if(this.reportDate1!=null && this.reportDate2!=null){
		  
		    if(auditReportList==null){
			  invokeLocal("listReport", 0, 0, null);
	  	    }
		  
		  }
		  return this.auditReportList;
	}
	
	public List<AuditFuncItem> getAuditReportCubeList(){
		try{
			if(this.auditReportCubeList==null){
				 invokeLocal("listReportCube", 0, 0, null);
			}
		}catch(Exception e){
			 log.error("aFuncManager:getAuditReportCubeList:Error:"+e);
		}
		return this.auditReportCubeList;
	}
	
	public void invokeLocal(String type, int firstRow, int numberOfRows,
	           String sessionId) {
		try{
			 String orderQuery=null;
			 log.info("hostsManager:invokeLocal");
			 
			 AFuncStateHolder aFuncStateHolder = (AFuncStateHolder)
					  Component.getInstance("aFuncStateHolder",ScopeType.SESSION);
			 HashMap<String, String> filterMap = aFuncStateHolder.getColumnFilterValues();
			 String st=null;
			 
			 if(type.equals("list")){
				 log.info("invokeLocal:list:01");
				 
				
				 Set<Map.Entry<String, String>> set = aFuncStateHolder.getSortOrders().entrySet();
                 for (Map.Entry<String, String> me : set) {
      		       log.info("me.getKey+:"+me.getKey());
      		       log.info("me.getValue:"+me.getValue());
      		       
      		       if(orderQuery==null){
      		    	 orderQuery="order by "+me.getKey()+" "+me.getValue();
      		       }else{
      		    	 orderQuery=orderQuery+", "+me.getKey()+" "+me.getValue();  
      		       }
      		     }
                 log.info("invokeLocal:list:orderQuery:"+orderQuery);
                 
                 if(filterMap!=null){
    	    		 Set<Map.Entry<String, String>> set_filter = filterMap.entrySet();
    	              for (Map.Entry<String, String> me : set_filter) {
    	            	  log.info("me.getKey+:"+me.getKey());
    	            	  log.info("me.getValue:"+me.getValue());
    	   		   
    	           //у нас act_dat_value переведена в строку уже в запросе
    	   		   /*  if(me.getKey().equals("act_dat_value")){  
    	        	   //  st=(st!=null?st+" and " :"")+" lower(to_char("+me.getKey()+",'DD.MM.YY HH24:MI:SS')) like lower('%"+me.getValue()+"%') ";
    	        	   //делаем фильтр на начало  
    	        	     st=(st!=null?st+" and " :"")+" lower(to_char("+me.getKey()+",'DD.MM.YY HH24:MI:SS')) like lower('"+me.getValue()+"%') ";
    	    	     */
    	             if(me.getKey().equals("arm_id")){ 
      	        		  st=(st!=null?st+" and " :" ")+me.getKey()+" = "+me.getValue();
      	        	 }else{
    	        		// st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('%"+me.getValue()+"%') ";
    	        		//делаем фильтр на начало
    	            	  st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('"+me.getValue()+"%') ";
    	        	  }
    	            }
    	    	  }
                 log.info("invokeLocal:list:filterQuery:"+st);
                 
				/*
				 auditList = entityManager.createQuery("select o from ActionsLogKnlT o "+(orderQuery!=null ? orderQuery+", o.idSrv desc" : " order by o.idSrv desc "))
                       .setFirstResult(firstRow)
                       .setMaxResults(numberOfRows)
                       .getResultList();
				 */
               /*  List<Object[]> lo = entityManager.createNativeQuery(
                		 "select t1.act_id, t1.act_dat_value, t1.usr_fio, t1.arm_name, t1.act_name "+
                		 "from "+
                		 "(select AL.ID_SRV act_id, " +
                		 "AL.DATE_ACTION act_dat, to_char(AL.DATE_ACTION, 'DD.MM.YY HH24:MI:SS') act_dat_value, "+
                         "decode(USR.UP_ISP_USER, null, USR.SURNAME||' '||USR.NAME_ ||' '|| USR.PATRONYMIC,  IBT.FIO ) usr_fio, "+
                         "ARM.ID_SRV arm_id, ARM.FULL_ arm_name, ACT.FULL_ act_name "+
                         "from ACTIONS_LOG_KNL_T al, "+
                         "AC_IS_BSS_T arm, "+
                         "ACTIONS_BSS_T act, "+
                         "AC_USERS_KNL_T usr, "+
                         "ISP_BSS_T ibt, "+
                         "TOKEN_KNL_T tkn "+
                         "where ACT.ID_SRV=AL.UP_ACTIONS "+
                         "and ACT.UP_IS=ARM.ID_SRV "+
                         "and TKN.ID_SRV=AL.UP_TOKEN "+
                         "and TKN.UP_USERS=USR.ID_SRV "+
                         "and USR.UP_ISP_USER=IBT.ID_SRV(+) "+
                		 ") t1 "+
                         (st!=null ? " where "+st :" ")+
                         (orderQuery!=null ? orderQuery+", act_id desc " : " order by act_id desc "))
                          .setFirstResult(firstRow)
                          .setMaxResults(numberOfRows)
        		          .getResultList();*/
                 
                /* List<Object[]> lo = entityManager.createNativeQuery(
                		 "select t1.act_id, t1.act_dat_value, t1.usr_fio, t1.arm_name, t1.act_name "+
                		 "from "+
                		 "(select AL.ID_SRV act_id, " +
                		 "AL.DATE_ACTION act_dat, to_char(AL.DATE_ACTION, 'DD.MM.YY HH24:MI:SS') act_dat_value, "+
                         "decode(USR.UP_ISP_USER, null, USR.SURNAME||' '||USR.NAME_ ||' '|| USR.PATRONYMIC,  IBT.FIO ) usr_fio, "+
                         "ARM.ID_SRV arm_id, ARM.FULL_ arm_name, ACT.FULL_ act_name "+
                         "from ACTIONS_LOG_KNL_T al, "+
                         "AC_IS_BSS_T arm, "+
                         "ACTIONS_BSS_T act, "+
                         "AC_USERS_KNL_T usr, "+
                         "ISP_BSS_T ibt "+
                         "where ACT.ID_SRV=AL.UP_ACTIONS "+
                         "and ACT.UP_IS=ARM.ID_SRV "+
                         "and USR.ID_SRV=AL.UP_USERS "+
                         "and USR.UP_ISP_USER=IBT.ID_SRV(+) "+
                		 ") t1 "+
                         (st!=null ? " where "+st :" ")+
                         (orderQuery!=null ? orderQuery+", act_id desc " : " order by act_id desc "))
                          .setFirstResult(firstRow)
                          .setMaxResults(numberOfRows)
        		          .getResultList();*/
                 
                /* "select t1.act_id, t1.act_dat_value, t1.usr_fio, t1.arm_name, t1.act_name "+
                 "from "+ 
                 "(select AL.ID_SRV act_id, "+  
                 "AL.DATE_ACTION act_dat, to_char(AL.DATE_ACTION, 'DD.MM.YY HH24:MI:SS') act_dat_value, "+ 
              */
                List<Object[]> lo = null;  
                 
                AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
	    		 
 	    		if(au.getAllowedSys()!=null){
                 
                  lo = entityManager.createNativeQuery(
                		 "select t1.act_id, t1.act_dat_value, t1.usr_fio, t1.arm_name, t1.act_name "+
                         "from "+ 
                         "(select AL.ID_SRV act_id, "+  
                         "AL.DATE_ACTION act_dat, to_char(AL.DATE_ACTION, 'DD.MM.YY HH24:MI:SS') act_dat_value, "+ 
                         "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.SURNAME||' '||AU_FULL.NAME_ ||' '|| AU_FULL.PATRONYMIC,  CL_USR_FULL.FIO) usr_fio, "+ 
                         "ARM.ID_SRV arm_id, ARM.FULL_ arm_name, ACT.FULL_ act_name "+ 
                         "from ACTIONS_LOG_KNL_T al, "+ 
                         "AC_IS_BSS_T arm, "+ 
                         "ACTIONS_BSS_T act, "+ 
                         "AC_USERS_KNL_T AU_FULL, "+ 
                         "ISP_BSS_T cl_usr_full, "+
                         "(select max(CL_usr.ID_SRV) CL_USR_ID,  CL_USR.SIGN_OBJECT  CL_USR_CODE "+ 
                         "from ISP_BSS_T cl_usr, "+ 
                         "AC_USERS_KNL_T au "+ 
                         "where AU.UP_SIGN_USER  = CL_usr.SIGN_OBJECT "+ 
                         "group by CL_usr.SIGN_OBJECT) t2 "+ 
                         "where ACT.ID_SRV=AL.UP_ACTIONS "+ 
                         "and ACT.UP_IS=ARM.ID_SRV "+ 
                         "and AU_FULL.UP_SIGN_USER=t2.CL_USR_CODE(+) "+ 
                         "and AU_FULL.ID_SRV=AL.UP_USERS "+ 
                         "and CL_USR_FULL.ID_SRV(+)=t2.CL_USR_ID "+
                         ") t1 "+
                         "where arm_id in (:idsArm) "+
                         (st!=null ? " and "+st :" ")+
                         (orderQuery!=null ? orderQuery+", act_id desc " : " order by act_id desc "))
                          .setFirstResult(firstRow)
                          .setMaxResults(numberOfRows)
                          .setParameter("idsArm", au.getAllowedSys())
        		          .getResultList();
 	    		}else{
 	    			
 	                 lo = entityManager.createNativeQuery(
 	                		 "select t1.act_id, t1.act_dat_value, t1.usr_fio, t1.arm_name, t1.act_name "+
 	                         "from "+ 
 	                         "(select AL.ID_SRV act_id, "+  
 	                         "AL.DATE_ACTION act_dat, to_char(AL.DATE_ACTION, 'DD.MM.YY HH24:MI:SS') act_dat_value, "+ 
 	                         "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.SURNAME||' '||AU_FULL.NAME_ ||' '|| AU_FULL.PATRONYMIC,  CL_USR_FULL.FIO) usr_fio, "+ 
 	                         "ARM.ID_SRV arm_id, ARM.FULL_ arm_name, ACT.FULL_ act_name "+ 
 	                         "from ACTIONS_LOG_KNL_T al, "+ 
 	                         "AC_IS_BSS_T arm, "+ 
 	                         "ACTIONS_BSS_T act, "+ 
 	                         "AC_USERS_KNL_T AU_FULL, "+ 
 	                         "ISP_BSS_T cl_usr_full, "+
 	                         "(select max(CL_usr.ID_SRV) CL_USR_ID,  CL_USR.SIGN_OBJECT  CL_USR_CODE "+ 
 	                         "from ISP_BSS_T cl_usr, "+ 
 	                         "AC_USERS_KNL_T au "+ 
 	                         "where AU.UP_SIGN_USER  = CL_usr.SIGN_OBJECT "+ 
 	                         "group by CL_usr.SIGN_OBJECT) t2 "+ 
 	                         "where ACT.ID_SRV=AL.UP_ACTIONS "+ 
 	                         "and ACT.UP_IS=ARM.ID_SRV "+ 
 	                         "and AU_FULL.UP_SIGN_USER=t2.CL_USR_CODE(+) "+ 
 	                         "and AU_FULL.ID_SRV=AL.UP_USERS "+ 
 	                         "and CL_USR_FULL.ID_SRV(+)=t2.CL_USR_ID "+
 	                         ") t1 "+
 	                         (st!=null ? " where "+st :" ")+
 	                         (orderQuery!=null ? orderQuery+", act_id desc " : " order by act_id desc "))
 	                          .setFirstResult(firstRow)
 	                          .setMaxResults(numberOfRows)
 	        		          .getResultList();
 	    			
 	    		}
                 
                 auditList = new ArrayList<BaseItem>();
                 
                 for(Object[] objectArray :lo){
                	 
                	 try{ 
                		 
                      ActionsLogKnlT al = new ActionsLogKnlT();
                      
                      al.setIdSrv(new Long(objectArray[0].toString()));
                      al.setDateActionValue(objectArray[1].toString());
                      al.setUserName(objectArray[2]!=null?objectArray[2].toString():"");
                      al.setIsName(objectArray[3]!=null?objectArray[3].toString():"");
                      al.setActName(objectArray[4]!=null?objectArray[4].toString():"");

                      auditList.add(al);
                      
              	   }catch(Exception e1){
              		   log.error("invokeLocal:for:error:"+e1);
              	   }
                 }
                 
             log.info("invokeLocal:list:02");
  
			 } else if(type.equals("count")){
				 log.info("IHReposList:count:01");
				/* auditCount = (Long)entityManager.createQuery(
						 "select count(o) " +
				         "from ActionsLogKnlT o ")
		                .getSingleResult();*/
				 
                 if(filterMap!=null){
    	    		 Set<Map.Entry<String, String>> set_filter = filterMap.entrySet();
    	              for (Map.Entry<String, String> me : set_filter) {
    	            	  log.info("me.getKey+:"+me.getKey());
    	            	  log.info("me.getValue:"+me.getValue());
    	           //у нас act_dat_value переведена в строку уже в запросе
    	   		    /* if(me.getKey().equals("act_dat_value")){  
    	        	   //  st=(st!=null?st+" and " :"")+" lower(to_char("+me.getKey()+",'DD.MM.YY HH24:MI:SS')) like lower('%"+me.getValue()+"%') ";
    	        	   //делаем фильтр на начало  
    	        	     st=(st!=null?st+" and " :"")+" lower(to_char("+me.getKey()+",'DD.MM.YY HH24:MI:SS')) like lower('"+me.getValue()+"%') ";
    	    	     */
    	             if(me.getKey().equals("arm_id")){ 
          	        	 st=(st!=null?st+" and " :" ")+me.getKey()+" = "+me.getValue();
          	         }else{
    	        		// st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('%"+me.getValue()+"%') ";
    	        		//делаем фильтр на начало
    	            	  st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('"+me.getValue()+"%') ";
    	        	  }
    	              }
    	    	  }
                 log.info("invokeLocal:count:filterQuery:"+st);
               /*  auditCount = ((java.math.BigDecimal)entityManager.createNativeQuery(
                		 "select count(*) "+
                		 "from "+
                		 "(select AL.ID_SRV act_id, " +
                		 "AL.DATE_ACTION act_dat, to_char(AL.DATE_ACTION , 'DD.MM.YY HH24:MI:SS') act_dat_value, "+
                         "decode(USR.UP_ISP_USER, null, USR.SURNAME||' '||USR.NAME_ ||' '|| USR.PATRONYMIC,  IBT.FIO ) usr_fio, "+
                         "ARM.ID_SRV arm_id, ARM.FULL_ arm_name, ACT.FULL_ act_name "+
                         "from ACTIONS_LOG_KNL_T al, "+
                         "AC_IS_BSS_T arm, "+
                         "ACTIONS_BSS_T act, "+
                         "AC_USERS_KNL_T usr, "+
                         "ISP_BSS_T ibt, "+
                         "TOKEN_KNL_T tkn "+
                         "where ACT.ID_SRV=AL.UP_ACTIONS "+
                         "and ACT.UP_IS=ARM.ID_SRV "+
                         "and TKN.ID_SRV=AL.UP_TOKEN "+
                         "and TKN.UP_USERS=USR.ID_SRV "+
                         "and USR.UP_ISP_USER=IBT.ID_SRV(+) "+
                		 ") t1 "+
                         (st!=null ? " where "+st :" "))
                         .getSingleResult()).longValue();*/
				 
               /*  auditCount = ((java.math.BigDecimal)entityManager.createNativeQuery(
                		 "select count(*) "+
                		 "from "+
                		 "(select AL.ID_SRV act_id, " +
                		 "AL.DATE_ACTION act_dat, to_char(AL.DATE_ACTION , 'DD.MM.YY HH24:MI:SS') act_dat_value, "+
                         "decode(USR.UP_ISP_USER, null, USR.SURNAME||' '||USR.NAME_ ||' '|| USR.PATRONYMIC,  IBT.FIO ) usr_fio, "+
                         "ARM.ID_SRV arm_id, ARM.FULL_ arm_name, ACT.FULL_ act_name "+
                         "from ACTIONS_LOG_KNL_T al, "+
                         "AC_IS_BSS_T arm, "+
                         "ACTIONS_BSS_T act, "+
                         "AC_USERS_KNL_T usr, "+
                         "ISP_BSS_T ibt "+
                         "where ACT.ID_SRV=AL.UP_ACTIONS "+
                         "and ACT.UP_IS=ARM.ID_SRV "+
                         "and USR.ID_SRV=AL.UP_USERS "+
                         "and USR.UP_ISP_USER=IBT.ID_SRV(+) "+
                		 ") t1 "+
                         (st!=null ? " where "+st :" "))
                         .getSingleResult()).longValue();*/
                 
                 AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
	    		 
  	    		if(au.getAllowedSys()!=null){
  	    			
  	    		  auditCount = ((java.math.BigDecimal)entityManager.createNativeQuery(
                		         "select count(*) "+
                				 "from "+ 
                                 "(select AL.ID_SRV act_id, "+  
                                 "AL.DATE_ACTION act_dat, to_char(AL.DATE_ACTION, 'DD.MM.YY HH24:MI:SS') act_dat_value, "+ 
                                 "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.SURNAME||' '||AU_FULL.NAME_ ||' '|| AU_FULL.PATRONYMIC,  CL_USR_FULL.FIO) usr_fio, "+ 
                                 "ARM.ID_SRV arm_id, ARM.FULL_ arm_name, ACT.FULL_ act_name "+ 
                                 "from ACTIONS_LOG_KNL_T al, "+ 
                                 "AC_IS_BSS_T arm, "+ 
                                 "ACTIONS_BSS_T act, "+ 
                                 "AC_USERS_KNL_T AU_FULL, "+ 
                                 "ISP_BSS_T cl_usr_full, "+
                                 "(select max(CL_usr.ID_SRV) CL_USR_ID,  CL_USR.SIGN_OBJECT  CL_USR_CODE "+ 
                                 "from ISP_BSS_T cl_usr, "+ 
                                 "AC_USERS_KNL_T au "+ 
                                 "where AU.UP_SIGN_USER  = CL_usr.SIGN_OBJECT "+ 
                                 "group by CL_usr.SIGN_OBJECT) t2 "+ 
                                 "where ACT.ID_SRV=AL.UP_ACTIONS "+ 
                                 "and ACT.UP_IS=ARM.ID_SRV "+ 
                                 "and AU_FULL.UP_SIGN_USER=t2.CL_USR_CODE(+) "+ 
                                 "and AU_FULL.ID_SRV=AL.UP_USERS "+ 
                                 "and CL_USR_FULL.ID_SRV(+)=t2.CL_USR_ID "+
                                 ") t1 "+
                                 "where arm_id in (:idsArm) "+
                         (st!=null ? " and "+st :" "))
                         .setParameter("idsArm", au.getAllowedSys())
                         .getSingleResult()).longValue();
  	    		}else{
  	    			 auditCount = ((java.math.BigDecimal)entityManager.createNativeQuery(
            		         "select count(*) "+
            				 "from "+ 
                             "(select AL.ID_SRV act_id, "+  
                             "AL.DATE_ACTION act_dat, to_char(AL.DATE_ACTION, 'DD.MM.YY HH24:MI:SS') act_dat_value, "+ 
                             "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.SURNAME||' '||AU_FULL.NAME_ ||' '|| AU_FULL.PATRONYMIC,  CL_USR_FULL.FIO) usr_fio, "+ 
                             "ARM.ID_SRV arm_id, ARM.FULL_ arm_name, ACT.FULL_ act_name "+ 
                             "from ACTIONS_LOG_KNL_T al, "+ 
                             "AC_IS_BSS_T arm, "+ 
                             "ACTIONS_BSS_T act, "+ 
                             "AC_USERS_KNL_T AU_FULL, "+ 
                             "ISP_BSS_T cl_usr_full, "+
                             "(select max(CL_usr.ID_SRV) CL_USR_ID,  CL_USR.SIGN_OBJECT  CL_USR_CODE "+ 
                             "from ISP_BSS_T cl_usr, "+ 
                             "AC_USERS_KNL_T au "+ 
                             "where AU.UP_SIGN_USER  = CL_usr.SIGN_OBJECT "+ 
                             "group by CL_usr.SIGN_OBJECT) t2 "+ 
                             "where ACT.ID_SRV=AL.UP_ACTIONS "+ 
                             "and ACT.UP_IS=ARM.ID_SRV "+ 
                             "and AU_FULL.UP_SIGN_USER=t2.CL_USR_CODE(+) "+ 
                             "and AU_FULL.ID_SRV=AL.UP_USERS "+ 
                             "and CL_USR_FULL.ID_SRV(+)=t2.CL_USR_ID "+
                             ") t1 "+
                     (st!=null ? " where "+st :" "))
                     .getSingleResult()).longValue();
  	    		} 
                 
                 
               log.info("invokeLocal:count:02:"+auditCount);
           	 } else if(type.equals("bean")){
				 
			 } else if(type.equals("listReport")){
				 log.info("invokeLocal:listReport:01");
                 
					// auditList = new ArrayList<BaseItem>();
				 auditReportList = entityManager.createQuery(
						 "select o from ActionsLogKnlT o  " +
						 "where o.dateAction >= :date1 " +
						 "and o.dateAction <= :date2 " +
						 "order by o.idSrv desc ")
						 .setParameter("date1", this.reportDate1)
    	                 .setParameter("date2", this.reportDate2)
	                     .getResultList();
					 
	             log.info("invokeLocal:listReport:02:size:"+auditReportList.size());
				 
			 }else if(type.equals("listReportCube")){
				 log.info("invokeLocal:listReportCube:01");
                 
				 this.auditReportCubeList=new ArrayList<AuditFuncItem>();
				 int count=0;
				 
					// auditList = new ArrayList<BaseItem>();
				 List<Object[]> lo  = entityManager.createNativeQuery(
					    "нет запроса"
						/* "select decode(USR.UP_ISP_USER,null,  USR.SURNAME|| ' '||USR.NAME_ || ' '|| USR.PATRONYMIC , IBT.FIO) fio,  ACT.FULL_ , num from ( "+
								 "select TKN.UP_USERS ,  ALOG.UP_ACTIONS  , count(*) num "+
								 "from ACTIONS_LOG_KNL_T alog, TOKEN_KNL_T tkn "+
								 "where alog.UP_TOKEN=TKN.ID_SRV "+
								 "group by TKN.UP_USERS ,  ALOG.UP_ACTIONS "+
								 ") t1, AC_USERS_KNL_T usr, ACTIONS_BSS_T act, ISP_BSS_T ibt "+
								 "where t1.UP_USERS=USR.ID_SRV "+
								 "and t1.UP_ACTIONS=ACT.ID_SRV "+
								 "and USR.UP_ISP_USER=IBT.ID_SRV(+) "+
								 "order by USR.SURNAME,  ACT.FULL_"*/)
						 .getResultList();
				 
				 	/*	"select t1.fio, t1.action, count(*) from ( "+
                        "select decode(USR.UP_ISP_USER,null,  USR.SURNAME|| ' '||USR.NAME_ || ' '|| USR.PATRONYMIC , IBT.FIO) fio,  ACT.FULL_ action  from ACTIONS_LOG_KNL_T alog,  "+
                        "TOKEN_KNL_T tkn, AC_USERS_KNL_T usr, ISP_BSS_T ibt, ACTIONS_BSS_T act "+
                        "where alog.UP_TOKEN=TKN.ID_SRV "+
                        "and TKN.UP_USERS=USR.ID_SRV "+
                        "and USR.UP_ISP_USER=IBT.ID_SRV(+) "+
                        "and ALOG.UP_ACTIONS=ACT.ID_SRV "+
                        ") t1 "+
                        "group by fio, action")*/
                        
	                    
				 for(Object[] objectArray :lo){
					 AuditFuncItem afi = new AuditFuncItem(
							 (objectArray[0]!=null?(String)objectArray[0]:""),
							 (objectArray[1]!=null?(String)objectArray[1]:""),
							 (objectArray[2]!=null?((java.math.BigDecimal)objectArray[2]).longValue():0L)
							 );
					 this.auditReportCubeList.add(afi);
					 count+=(objectArray[2]!=null?((java.math.BigDecimal)objectArray[2]).intValue():0);
				 }
				 
				 this.auditReportCubeCount = new Long(count);
	             log.info("invokeLocal:listReportCube:02:size:"+auditReportList.size());
			 }
		}catch(Exception e){
			  log.error("invokeLocal:error:"+e);
			  evaluteForList=false;
			  FacesMessages.instance().add("Ошибка!");
		}
	}
	
  public Long getAuditReportCubeCount(){
	  return this.auditReportCubeCount;
  }
	  /**
	  * Подготовка сущности Аудит УФМС 
	  * для последующих операций просмотра
	  */
   public void forView(String modelType) {
	   String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	  log.info("forView:sessionId:"+sessionId);
	  log.info("forView:modelType:"+modelType);
	  if(sessionId!=null /*&& usrBean==null*/){
		  
		    String service="";
			if(modelType==null){
		    	return ;
		    }
			if(modelType.equals("aFuncDataModel")){
				//service=ServiceReestr.Repos;
			}  
		//  invoke("bean", 0, 0, sessionId, service);
		//  Contexts.getEventContext().set("logContrBean", logContrBean);
	
		 /* 
	 	 List<AcUser> usrListCached = (List<AcUser>)
				  Component.getInstance("usrListCached",ScopeType.SESSION);
		  if(usrListCached!=null){
			 for(AcUser it : usrListCached){
				 
				 log.info("forView_inside_for");
				 
				 if(it.getBaseId().toString().equals(sessionId)){
					 log.info("forView_Achtung!!!");
					// this.usrBean=it;
					// Contexts.getEventContext().set("usrBean", usrBean);
					 Contexts.getEventContext().set("usrBean", it);
					 return;
				 }
			 }
		 }*/
		 ActionsLogKnlT ar = searchBean(sessionId);
		 Contexts.getEventContext().set("aFuncBean", ar);
	  }
   }
   
   private  ActionsLogKnlT searchBean(String sessionId){
    	
      if(sessionId!=null){
    	 List<ActionsLogKnlT> aFuncListCached = (List<ActionsLogKnlT>)
				  Component.getInstance("aFuncListCached",ScopeType.SESSION);
    	 
		if(aFuncListCached!=null){
			for(ActionsLogKnlT it : aFuncListCached){
				 
			// log.info("searchBean_inside_for");
			  if(it.getBaseId().toString().equals(sessionId)){
					 log.info("searchBean_Achtung!!!");
					 return it;
			  }
			}
		 }
	   }
	   return null;
    }
    public Long getAuditCount(){
	   log.info("getAuditCount");
	 
	   invokeLocal("count",0,0,null);
	  
	   return auditCount;
	  // FacesMessages.instance().add("Ошибка доступа к серверу xxx.xxx.x.xxx!");
   }
   
   public void addaFunc(){
	/*   log.info("aFuncManager:addaFunc:01");
	   
	   ActionsLogKnlT aFuncBeanCrt = (ActionsLogKnlT)
				  Component.getInstance("aFuncBeanCrt",ScopeType.CONVERSATION);
	   
	   if(aFuncBeanCrt==null){
		   return;
	   }
	 
	   try {
		//  AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
		   
		//  permBeanCrt.setCreator(au.getIdUser());
		//  permBeanCrt.setCreated(new Date());
	      entityManager.persist(aFuncBeanCrt);
	 
	     //  entityManager.flush();
	    //  entityManager.refresh(permBeanCrt);
	    
	    }catch (Exception e) {
	       log.error("aFuncManager:addaFunc:ERROR:"+e);
	    }
	   */
   }
   
   public void updaFunc(){
	/*   
	   log.info("aFuncManager:updaFunc:01");
	   
	   ActionsLogKnlT aFuncBean = (ActionsLogKnlT)
				  Component.getInstance("aFuncBean",ScopeType.CONVERSATION);
	   
	   String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	   log.info("aFuncManager:updaFunc:sessionId:"+sessionId);
	
	   if(aFuncBean==null || sessionId==null){
		   return;
	   }
	
	   try {
		  AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
		   
		  ActionsLogKnlT aam = entityManager.find(ActionsLogKnlT.class, new Long(sessionId));
		  
		  aam.setaFuncName(aFuncBean.getaFuncName());
		  aam.setaFuncDescr(aFuncBean.getaFuncDescr());
		  aam.setOrderNum(aFuncBean.getOrderNum());
		  
		 // aam.setModificator(au.getIdUser());
		//  aam.setModified(new Date());
		  
		  entityManager.flush();
	      entityManager.refresh(aam);
	    	  
	    	//  usrBean = entityManager.find(AcUser.class, new Long(sessionId));
	      Contexts.getEventContext().set("aFuncBean", aam);
	    	  
	     }catch (Exception e) {
           log.error("armManager:updSrm:ERROR:"+e);
         }*/
   }
   
   public void delaFunc(){
	/* try{
		log.info("aFuncManager:delaFunc:01");  
		
		ActionsLogKnlT armBean = (ActionsLogKnlT)
				  Component.getInstance("aFuncBean",ScopeType.CONVERSATION);
		// <h:inputHidden value="#{armBean.idArm}"/>
		
		if(armBean==null){
			return;
		}
		 
		log.info("aFuncManager:delaFunc:IdaFunc:"+armBean.getIdaFunc());
		
		ActionsLogKnlT aom = entityManager.find(ActionsLogKnlT.class, armBean.getIdaFunc());
		  
		entityManager.remove(aom);
		
	 }catch(Exception e){
		 log.error("armManager:delArm:error:"+e); 
	 }*/
    }
 
    public void forViewUpdDel() {
	 /*  try{
	     String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("sessionId");
	     log.info("forViewUpdDel:sessionId:"+sessionId);
	     if(sessionId!=null){
	    	 ActionsLogKnlT ao = entityManager.find(ActionsLogKnlT.class, new Long(sessionId));
	    	 Contexts.getEventContext().set("aFuncBean", ao);
	   	 }
	   }catch(Exception e){
		   log.error("forViewUpdDel:Error:"+e);
	   }*/
    } 
   
    public void forViewDelMessage() {
	/*	  String sessionId = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap()
				.get("sessionId");
		  log.info("forViewDel:sessionId:"+sessionId);
		  if(sessionId!=null){
			 ActionsLogKnlT aa = entityManager.find(ActionsLogKnlT.class, new Long(sessionId));
			 if((aa.getAcLinkRoleAppPagePrmssns()!=null&&!aa.getAcLinkRoleAppPagePrmssns().isEmpty())){
				dellMessage="У разрешения есть порождённые записи! При удалении они будут удалены!";
			 }
			 Contexts.getEventContext().set("aFuncBean", aa);
		 }	*/
    }
  
    public void forViewWord(String fileName){
		log.info("JournManager:forViewWord:01");
		try{
		 
			if(fileName==null || fileName.equals("")){
				fileName="user_actions";
			}
			
		  HttpServletResponse response = (HttpServletResponse)
				  FacesContext.getCurrentInstance().getExternalContext().getResponse();
		  response.setHeader("Content-disposition", "attachment; filename="+fileName+".doc");
	      response.setContentType("application/msword");

		}catch(Exception e){
			log.error("auditReportsManager:forViewWord:error:"+e);
		}
		log.info("auditReportsManager:forViewWord:02");
	}
    
   public int getConnectError(){
	   return connectError;
   }
  /* 
   public List<AcApplication> getListArm() throws Exception{
	    log.info("armManager::getListArm:01");
	    try {
	    	if(listArm==null){
	       		listArm=entityManager.createQuery("select o from AcApplication o").getResultList();
	    	}
	     } catch (Exception e) {
	    	 log.error("armManager::getListArm:ERROR:"+e);
	         throw e;
	     }
	    return listArm;
  }*/
   
   public List <BaseTableItem> getAuditItemsListSelect() {
		 log.info("getAuditItemsListSelect:01");
	
	    AFuncContext ac= new AFuncContext();
		   if( auditItemsListSelect==null){
			   log.info("getAuditItemsListSelect:02");
			   auditItemsListSelect = new ArrayList<BaseTableItem>();
			   
			 /* String reposType = FacesContext.getCurrentInstance().getExternalContext()
			      .getRequestParameterMap()
			      .get("reposType");
	            log.info("getAuditItemsListSelect:reposType:"+reposType);
			    if(reposType!=null){
					 if(reposType.equals("1")){
					 }else if(reposType.equals("2")){
					 }else if(reposType.equals("3")){
					 }else if(reposType.equals("4")){
				     }else{
				     }
			    }else{
			    }*/
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("dateAction"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("userName"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("isName"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("actName"));
		   }
	       return this.auditItemsListSelect;
   }
   
   public void setAuditItemsListSelect(List <BaseTableItem> auditItemsListSelect) {
		    this.auditItemsListSelect=auditItemsListSelect;
   }
   
   public List <BaseTableItem> getAuditItemsListContext() {
	   log.info("orgManager:getAuditItemsListContext");
	   if(auditItemsListContext==null){
		   AFuncContext ac= new AFuncContext();
		   auditItemsListContext = new ArrayList<BaseTableItem>();
		   //auditItemsListContext.addAll(ac.getAuditItemsMap().values());
		   //auditItemsListContext.addAll(ac.getAuditItemsCollection());
		   auditItemsListContext=ac.getAuditItemsCollection();
	   }
	   return this.auditItemsListContext;
   }
      
    
   public void selectRecord(){
	    String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	    log.info("selectRecord:sessionId="+sessionId);
	    
	   //  forView(); //!!!
	    ArrayList<String> selRecArm = (ArrayList<String>)
				  Component.getInstance("selRecaFunc",ScopeType.SESSION);
	    
	    if(selRecArm==null){
	       selRecArm = new ArrayList<String>();
	       log.info("selectRecord:01");
	    }
	    
	    // AcApplication aa = searchBean(sessionId);
	    ActionsLogKnlT aa = new ActionsLogKnlT();
  	    // в getAuditList : else{it.setSelected(false);}
	    
	    if(aa!=null){
	     if(selRecArm.contains(sessionId)){
	    	selRecArm.remove(sessionId);
	    	aa.setSelected(false);
	    	log.info("selectRecord:02");
	     }else{
	    	selRecArm.add(sessionId);
	    	aa.setSelected(true);
	    	log.info("selectRecord:03");
	     }
	    Contexts.getSessionContext().set("selRecaFunc", selRecArm);	
	    
	    Contexts.getEventContext().set("aFuncBean", aa);
	   }
	}
   
public void clearDate(){
	   
	   log.info("aFuncManager:clearDate:archiveParamValue:"+this.archiveParamValue);
	   
	   List<Object[]> lo=null;
	   BufferedWriter bw=null;
	   File file=null; 
	   int i=1;

	   try{

		   Context ctx = new InitialContext(); 
	    	 
	       BaseParamItem bpi = new BaseParamItem(ServiceReestrPro.ArchiveAuditFunc.name());
	      // bpi.put("gactiontype", ServiceReestrAction.TO_ARCHIVE.name());
	       bpi.put("gactiontype", ServiceReestrAction.TASK_RUN.name());
	       
	       bpi.put("archiveParamValue", this.archiveParamValue);
	       
	       IRemoteFrontageLocal obj = (IRemoteFrontageLocal)ctx.lookup(jndiBinding);
        		   
         //  IRemoteFrontageLocal asyncObj = AsyncUtils.mixinAsync(obj);

        //   asyncObj.run(bpi);
         
	       obj.run(bpi);
	       
           audit(ResourcesMap.AUDIT_USER, ActionsMap.START);
           
		/*    entityManager.createNativeQuery(
				   "delete from ACTIONS_LOG_KNL_T al "+
                   "where al.DATE_ACTION>=? "+
                   "and al.DATE_ACTION<=? ")
    	  	 .setParameter(1, clearDate1, TemporalType.TIMESTAMP)
    	     .setParameter(2, clearDate2, TemporalType.TIMESTAMP)
           .executeUpdate();*/

		   
	   }catch (Exception e) {
	   	 log.error("aFuncManager:clearDate:ERROR:"+e);
	   }finally{
		   try{
		    if(bw!=null){
			    bw.close  ();
			}
		   }catch (Exception e) {}
	   }
   }
   
   public String getDellMessage() {
	   return dellMessage;
   }
   public void setDellMessage(String dellMessage) {
	   this.dellMessage = dellMessage;
   } 
   
   public Date getClearDate1(){
	   return this.clearDate1;
   }
   public void setClearDate1(Date clearDate1){
	   this.clearDate1=clearDate1;
   }
  
   public Date getClearDate2(){
	   return this.clearDate2;
   }
   public void setClearDate2(Date clearDate2){
	   this.clearDate2=clearDate2;
   }

public Long getArchiveParamValue() {
	   
	   log.info("aSysManager:getArchiveParamValue:01");
	   
	   if(this.archiveParamValue==null){
		   
		   //!!!
		   this.archiveParamValue=6L;
		   
		   try{
			   List<String> los = entityManager.createNativeQuery(
			              "select ST.VALUE_PARAM "+
	                      "from SETTINGS_KNL_T st "+
	                      "where ST.SIGN_OBJECT=? ")
	                      .setParameter(1, param_code)
	                      .getResultList();
		    	  
		       if(los!=null&&!los.isEmpty()){
		    	   this.archiveParamValue= Long.parseLong(los.get(0));
		       }
		     
		       log.info("aSysManager:getArchiveParamValue:archiveParamValue:"+this.archiveParamValue);
		    	  
		   }catch(Exception e){
			   log.error("aSysManager:getArchiveParamValue:ERROR:"+e);
		   }
	   }
	   return this.archiveParamValue;
   }
   public void setArchiveParamValue(Long archiveParamValue) {
		this.archiveParamValue = archiveParamValue;
   }
   
   public void audit(ResourcesMap resourcesMap, ActionsMap actionsMap){
	   try{
		   AuditExportData auditExportData = (AuditExportData)Component.getInstance("auditExportData",ScopeType.SESSION);
		   auditExportData.addFunc(resourcesMap.getCode()+":"+actionsMap.getCode());
		   
	   }catch(Exception e){
		   log.error("GroupManager:audit:error:"+e);
	   }
  }
   
   public Boolean getEvaluteForList() {
	
   	log.info("armManager:evaluteForList:01");
   	if(evaluteForList==null){
   		evaluteForList=false;
    	String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
	             .getRequestParameterMap()
	             .get("remoteAudit");
	   log.info("aFuncManager:evaluteForList:remoteAudit:"+remoteAudit);
     	
    	if(remoteAudit!=null&&
    	 
    	   !remoteAudit.equals("OpenCrtFact")&&	
    	   !remoteAudit.equals("OpenUpdFact")&&
    	   !remoteAudit.equals("OpenDelFact")&&
   	       !remoteAudit.equals("onSelColFact")&&
   	       !remoteAudit.equals("refreshPdFact")){
    		log.info("aFuncManager:evaluteForList!!!");
   		    evaluteForList=true;
    	}
   	 }
       return evaluteForList;
   }
   public Boolean getEvaluteForListFooter() {
		
	  // 	log.info("reposManager:evaluteForListFooter:01");
	   	if(evaluteForListFooter==null){
	   		evaluteForListFooter=false;
	    	String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
		             .getRequestParameterMap()
		             .get("remoteAudit");
		   log.info("aFuncManager:evaluteForListFooter:remoteAudit:"+remoteAudit);
	     
	    	if(getEvaluteForList()&&
	    	   //new-1-	
	    	   !remoteAudit.equals("protBeanWord")&&	
	    	   //new-2-	
	   	       !remoteAudit.equals("selRecAllFact")&&
	   	       !remoteAudit.equals("clRecAllFact")&&
	   	      // !remoteAudit.equals("clSelOneFact")&&
	   	       !remoteAudit.equals("onSelColSaveFact")){
	    		  log.info("aFuncManager:evaluteForListFooter!!!");
	   		      evaluteForListFooter=true;
	    	}
	   	 }
	       return evaluteForListFooter;
	   }
   
   public Boolean getEvaluteForBean() {
		
		  // 	log.info("reposManager:evaluteForListFooter:01");
		   	if(evaluteForBean==null){
		   		evaluteForBean=false;
		    	String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
			             .getRequestParameterMap()
			             .get("remoteAudit");
			    log.info("aFuncManager:evaluteForBean:remoteAudit:"+remoteAudit);
				String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			             .getRequestParameterMap()
			             .get("sessionId");
			    log.info("aFuncManager:evaluteForBean:sessionId:"+sessionId);
		    	if(sessionId!=null && remoteAudit!=null &&
		    	   (remoteAudit.equals("rowSelectFact")||	
		    	    remoteAudit.equals("UpdFact"))){
		    	      log.info("aFuncManager:evaluteForBean!!!");
		   		      evaluteForBean=true;
		    	}
		   	 }
		     return evaluteForBean;
		   }

}
/*
Department dept = em.getReference(Department.class, 30);
Employee emp = new Employee();
emp.setId(53);
emp.setName("Peter");
emp.setDepartment(dept);
dept.getEmployees().add(emp);
em.persist(emp);
*/
