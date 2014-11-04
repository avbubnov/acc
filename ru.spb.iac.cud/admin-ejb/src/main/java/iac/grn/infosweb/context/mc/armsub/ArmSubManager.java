package iac.grn.infosweb.context.mc.armsub;

import org.apache.xml.security.utils.Base64;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.transaction.Transaction;
 



import iac.cud.infosweb.dataitems.BaseItem;
import iac.cud.infosweb.dataitems.SystemCertItem;
import iac.cud.infosweb.entity.AcAppPage;
import iac.cud.infosweb.entity.AcSubsystemCertBssT;
import iac.cud.infosweb.entity.AcUser;
import iac.grn.infosweb.session.audit.actions.ActionsMap;
import iac.grn.infosweb.session.audit.actions.ResourcesMap;
import iac.grn.infosweb.session.audit.export.AuditExportData;
import iac.grn.infosweb.session.cache.CacheManager;
import iac.grn.infosweb.session.navig.LinksMap;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
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

import iac.grn.serviceitems.BaseTableItem;

import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletResponse;

/**
 * ����������� ���
 * @author bubnov
 *
 */
@Name("armSubManager")
public class ArmSubManager {
	
	 @Logger private Log log;
	
	 @In 
	 EntityManager entityManager;
	 
	/**
     * �������������� �������� 
     * ��� �����������
     */
	
	private String dellMessage=null;
	private int delNot=0;
	
	private List<BaseItem> auditList; 
	
	private Long auditCount;
	
	private List <BaseTableItem> auditItemsListSelect;
	
	private List <BaseTableItem> auditItemsListContext;
	
	private int connectError=0;
	private Boolean evaluteForList;
	private Boolean evaluteForListFooter;  
	private Boolean evaluteForBean;
	
	private List<AcSubsystemCertBssT> listArmSub = null;
	
	private boolean armSubCodeExist=false;
	
	public List<BaseItem> getAuditList(int firstRow, int numberOfRows){
	  String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
	             .getRequestParameterMap()
	             .get("remoteAudit");
	   log.info("auditManager:getAuditList:remoteAudit:"+remoteAudit);
	  
	  
	  log.info("getAuditList:firstRow:"+firstRow);
	  log.info("getAuditList:numberOfRows:"+numberOfRows);
	  
	  List<BaseItem> armSubListCached = (List<BaseItem>)
			  Component.getInstance("armSubListCached",ScopeType.SESSION);
	  if(auditList==null){
		  log.info("getAuditList:01");
		 	if(("rowSelectFact".equals(remoteAudit)||
			    "selRecAllFact".equals(remoteAudit)||
			    "clRecAllFact".equals(remoteAudit)||
			    "clSelOneFact".equals(remoteAudit)||
			    "onSelColSaveFact".equals(remoteAudit))&&
			    armSubListCached!=null){
		 	    	this.auditList=armSubListCached;
			}else{
				log.info("getAuditList:03");
		    	invokeLocal("list", firstRow, numberOfRows, null);
			    Contexts.getSessionContext().set("armSubListCached", this.auditList);
			    log.info("getAuditList:03:"+this.auditList.size());
			}
		 	
		 	List<String>  selRecArmSub = (ArrayList<String>)
					  Component.getInstance("selRecArmSub",ScopeType.SESSION);
		 	if(this.auditList!=null && selRecArmSub!=null) {
		 		 for(BaseItem it:this.auditList){
				   if(selRecArmSub.contains(it.getBaseId().toString())){
					 
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
	public void invokeLocal(String type, int firstRow, int numberOfRows,
	           String sessionId) {
		try{
			 String orderQuery=null;
			 log.info("ArmSubManager:invokeLocal");
			 
			 ArmSubStateHolder orgStateHolder = (ArmSubStateHolder)
					  Component.getInstance("armSubStateHolder",ScopeType.SESSION);
			 
			 Map<String, String> filterMap = orgStateHolder.getColumnFilterValues();
			 String st=null;
			 
			 if("list".equals(type)){
				 log.info("ArmSub:invokeLocal:list:01");
				 
				
				 Set<Map.Entry<String, String>> set = orgStateHolder.getSortOrders().entrySet();
                 for (Map.Entry<String, String> me : set) {
      		       
      		       if(orderQuery==null){
      		    	 orderQuery="order by "+me.getKey()+" "+me.getValue();
      		       }else{
      		    	 orderQuery=orderQuery+", "+me.getKey()+" "+me.getValue();  
      		       }
      		     }
                 log.info("ArmSub:invokeLocal:list:orderQuery:"+orderQuery);
                
                 if(filterMap!=null){
    	    		 Set<Map.Entry<String, String>> setFilterArmSub = filterMap.entrySet();
    	              for (Map.Entry<String, String> me : setFilterArmSub) {
    	             
    	                if(me.getKey().equals("acIsBssTLong")){  
    	   		    	  st=(st!=null?st+" and " :" ")+me.getKey()+"='"+me.getValue()+"' ";
    	    	       }else{
    	        		//������ ������ �� ������
    	            	  st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('"+me.getValue()+"%') ";
    	                  }
    	              }
    	    	   }
                 log.info("invokeLocal:list:filterQuery:"+st);
                 
				 auditList = entityManager.createQuery("select o from AcSubsystemCertBssT o "+
						 (st!=null ? " where "+st :"")+
						 (orderQuery!=null ? orderQuery+", o.idSrv " : " order by o.idSrv "))
                       .setFirstResult(firstRow)
                       .setMaxResults(numberOfRows)
                       .getResultList();
             log.info("ArmSub:invokeLocal:list:02");
  
			 } else if("count".equals(type)){
				 log.info("ArmSubList:count:01");
				 
				 if(filterMap!=null){
    	    		 Set<Map.Entry<String, String>> setFilterArmSub = filterMap.entrySet();
    	              for (Map.Entry<String, String> me : setFilterArmSub) {
    	             
    	                 if(me.getKey().equals("acIsBssTLong")){  
    	   		    	  st=(st!=null?st+" and " :" ")+me.getKey()+"='"+me.getValue()+"' ";
    	    	       }else{
    	        		//������ ������ �� ������
    	            	  st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('"+me.getValue()+"%') ";
    	               }
    	              }
    	    	   }
                 log.info("ArmSub:invokeLocal:count:filterQuery:"+st);
				 
				 
				 auditCount = (Long)entityManager.createQuery(
						 "select count(o) " +  
				         "from AcSubsystemCertBssT o "+
				         (st!=null ? " where "+st :""))
		                .getSingleResult();
				 
               log.info("ArmSub:invokeLocal:count:02:"+auditCount);
           	 } 
		}catch(Exception e){
			  log.error("ArmSub:invokeLocal:error:"+e);
			  evaluteForList=false;
			  FacesMessages.instance().add("������!");
		}
	}


	
   public void forView(String modelType) {
	   String  armSubId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	  log.info("forView:armSubId:"+armSubId);
	  log.info("forView:modelType:"+modelType);
	  if(armSubId!=null ){
		  
		   
			if(modelType==null){
		    	return ;
		    }
			if(modelType.equals("armSubDataModel")){
				
			}  
		
		 AcSubsystemCertBssT ar = searchBean(armSubId);
		 
		 if(!isAllowedSys(new Long(armSubId))){
			 log.info("armSubManager:forView:02");
			 ar.setIsAllowedSys(false);
		 }		 
		 
		 Contexts.getEventContext().set("armSubBean", ar);
		 
		 forViewCert();
	  }
   }
   
   public boolean isAllowedSys(Long idSub){
	   log.info("armSubManager:isAllowedSys:01:"+idSub);
	 
	   boolean result = false;
	   
	   try {
	   
		  AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
  		 
   		  if(au.getAllowedSys()!=null){
   		   
   			//������ �� � ������, ������� ��� � �������� � ������������  
		    List<Object> list= entityManager.createNativeQuery(
		    		
			    		"SELECT SUB.UP_IS " + 
			    		"    FROM AC_SUBSYSTEM_CERT_BSS_T sub " + 
			    		"   WHERE   sub.ID_SRV = :idSub " + 
			    		"         AND  SUB.UP_IS NOT IN (:idsArm) " + 
			    		"GROUP BY SUB.UP_IS")
                      .setParameter("idSub", idSub)
                      .setParameter("idsArm", au.getAllowedSys())
                      .getResultList();
		    
		    //���� ��� �����, ������� �� ��������� � ������������,
		    //����� � ������������ ���� ������ � ���� ������
		    //� �. �. ������ ������
		    if(list.isEmpty()){
		    	result = true;
		    }
		    
	    }else{
	    	result = true;
	    }
	   
	   }catch (Exception e) {
	       log.error("armSubManager:isAllowedSys:ERROR:"+e);
	    }
	   
	   log.info("armSubManager:isAllowedSys:02:"+result);
	   
	   return result;
  }
   
   private AcSubsystemCertBssT searchBean(String sessionId){
    	
      if(sessionId!=null){
    	 List<AcSubsystemCertBssT> armSubListCached = (List<AcSubsystemCertBssT>)
				  Component.getInstance("armSubListCached",ScopeType.SESSION);
		if(armSubListCached!=null){
			for(AcSubsystemCertBssT it : armSubListCached){
				 
			 
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
   }
   
   public void addArmSub(){
	   log.info("armSubManager:addOrg:01");
	   
	   AcSubsystemCertBssT armSubBeanCrt = (AcSubsystemCertBssT)
				  Component.getInstance("armSubBeanCrt",ScopeType.CONVERSATION);
	   
	   if(armSubBeanCrt==null){
		   return;
	   }
	 
	   try {
		  
		 if(!armSubCodeExistCrt(armSubBeanCrt.getAcIsBssTLong(), armSubBeanCrt.getSubsystemCode().trim())){
		   
		  AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
		  
		  armSubBeanCrt.setSubsystemName(armSubBeanCrt.getSubsystemName().trim());
		  armSubBeanCrt.setSubsystemCode(armSubBeanCrt.getSubsystemCode().trim());
			  
		  armSubBeanCrt.setCreator(au.getIdUser());
		  armSubBeanCrt.setCreated(new Date());
	      entityManager.persist(armSubBeanCrt);
	    	
	    
	     // aud/it(Resour/cesMap.IS, ActionsM/ap.CREATE); 
	      
	    }
	      
	    }catch (Exception e) {
	       log.error("armSubManager:addArmSub:ERROR:"+e);
	    }
	   
   }
   
   public void updArmSub(){
	   
	   log.info("armSubManager:updArmSub:01");
	   
	   AcSubsystemCertBssT armSubBean = ( AcSubsystemCertBssT)
				  Component.getInstance("armSubBean",ScopeType.CONVERSATION);
	   
	   String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	   log.info("armSubManager:updArmSub:sessionId:"+sessionId);
	
	   if(armSubBean==null || sessionId==null){
		   return;
	   }
	
	   try {
		   
		 if(!armSubCodeExistUpd(armSubBean.getAcIsBssTLong(), armSubBean.getSubsystemCode().trim(), new Long(sessionId))){  
		
		 AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
		   
		  AcSubsystemCertBssT aam = entityManager.find(AcSubsystemCertBssT.class, new Long(sessionId));
		  
		  aam.setSubsystemName(armSubBean.getSubsystemName().trim());
		  aam.setSubsystemCode(armSubBean.getSubsystemCode().trim());
		  
		  aam.setAcIsBssTLong(armSubBean.getAcIsBssTLong());
			  
		  aam.setModificator(au.getIdUser());
		  aam.setModified(new Date());
		  
		  entityManager.flush();
	      entityManager.refresh(aam);
	    	  
	      Contexts.getEventContext().set("armSubBean", aam);
		
	    //  au/dit(ResourcesMap.IS, Acti/onsMap.UPDATE); 
	      
		 }
		 
	     }catch (Exception e) {
           log.error("armSubManager:updSrm:ERROR:"+e);
         }
   }
   
    public void saveArmSubCertificate(byte[] file_byte, Long id_sys){
	   
	   log.info("armSubManager:saveArmSubCertificate:01:"+(file_byte!=null));
	   log.info("armSubManager:saveArmSubCertificate:02:"+id_sys);
	   
	   try {
		   
		  
		   
		   CertificateFactory userCf = CertificateFactory.getInstance("X.509");
           X509Certificate userCertX = (X509Certificate)
        		   userCf.generateCertificate(new  ByteArrayInputStream(file_byte));
            String x509Cert = Base64.encode(userCertX.getEncoded());
           
           log.info("armSubManager:saveArmSubCertificate:03:"+x509Cert);
           
           Transaction.instance().begin();
		   
		   Transaction.instance().enlist(entityManager);
		   
		   entityManager.createNativeQuery("update AC_SUBSYSTEM_CERT_BSS_T t1 " + 
		   		                           "set T1.CERT_DATE=? " + 
		   		                           "where t1.ID_SRV=? ")
		   .setParameter(1, x509Cert)
		   .setParameter(2, id_sys)
		   .executeUpdate();  
			 
		   Transaction.instance().commit();
		   
	     }catch (Exception e) {
           log.error("armSubManager:saveArmSubCertificate:ERROR:"+e);
           try{
             Transaction.instance().rollback();
           }catch(Exception et){}
         }
   }

    public void removeCert(){
 	   
 	   try {
 		   
 		   String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
 			        .getRequestParameterMap()
 			        .get("sessionId");
 		   log.info("armSubManager:removeCert:sessionId:"+sessionId);
 		
 		   if(sessionId==null||sessionId.trim().equals("")){
 			   return;
 		   }
 			   
 		   entityManager.createNativeQuery("update AC_SUBSYSTEM_CERT_BSS_T t1 " + 
 		   		                           "set T1.CERT_DATE=null " + 
 		   		                           "where t1.ID_SRV=? ")
 		   .setParameter(1, new Long(sessionId))
 		   .executeUpdate();  
 			 
 			   
 	     }catch (Exception e) {
            log.error("armSubManager:removeCert:ERROR:"+e);
          }
   }
    
   public void delArmSub(){
	 try{
		log.info("armSubManager:delArmSub:01");  
		
		AcSubsystemCertBssT armSubBean = (AcSubsystemCertBssT)
				  Component.getInstance("armSubBean",ScopeType.CONVERSATION);
		// <h:inputHidden value="#{armSubBean.idArmSub}"/>
		
		if(armSubBean==null){
			return;
		}
		 
		log.info("armSubManager:delArmSub:IdArmSub:"+armSubBean.getBaseId());
		
		AcSubsystemCertBssT aom = entityManager.find(AcSubsystemCertBssT.class, armSubBean.getBaseId());
		  
		entityManager.remove(aom);
		
		audit(ResourcesMap.IS, ActionsMap.DELETE); 
		
	 }catch(Exception e){
		 log.error("armSubManager:delArmSub:error:"+e); 
	 }
    }
 
    public void forViewUpdDel() {
	   try{
	     String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("sessionId");
	     log.info("forViewUpdDel:sessionId:"+sessionId);
	     if(sessionId!=null){
	    	 AcSubsystemCertBssT ao = entityManager.find(AcSubsystemCertBssT.class, new Long(sessionId));
	    	 Contexts.getEventContext().set("armSubBean", ao);
	   	 }
	   }catch(Exception e){
		   log.error("forViewUpdDel:Error:"+e);
	   }
    } 
   
    public void forViewDelMessage() {
		  String sessionId = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap()
				.get("sessionId");
		  log.info("forViewDelMessage:sessionId:"+sessionId);
		  if(sessionId!=null){
		
			  
			  AcSubsystemCertBssT aa = entityManager.find(AcSubsystemCertBssT.class, new Long(sessionId));
			
			  
			 
			 Contexts.getEventContext().set("armSubBean", aa);
			 
			 
			 
		 }	
    }
  
    public void forViewCert() {
 	   try{
 	     String sessionId = FacesContext.getCurrentInstance().getExternalContext()
 			        .getRequestParameterMap()
 			        .get("sessionId");
 	     log.info("forViewCert:sessionId:"+sessionId);
 	     
 	     DateFormat df = new SimpleDateFormat ("dd.MM.yy");
 	    
 	     if(sessionId!=null){
 	    	 
 	    	String certDataX = (String) entityManager.createNativeQuery(
 	    			 "select to_char(T1.CERT_DATE) " + 
 	    	 		 "from AC_SUBSYSTEM_CERT_BSS_T t1 " + 
 	    	 		 "where T1.ID_SRV=? ")
                 .setParameter(1, new Long(sessionId))
                 .getSingleResult();
 	    	
 	    	 log.info("forViewCert:cert_data:"+certDataX); 
 	    	
 	     if(certDataX!=null&&!certDataX.trim().equals("")){
 	    	 byte[] certByteX = Base64.decode(certDataX);
 	    	 
 	    	 CertificateFactory userCf = CertificateFactory.getInstance("X.509");
 	           X509Certificate userCertX = (X509Certificate)
 	        		   userCf.generateCertificate(new  ByteArrayInputStream(certByteX ));
 	          
 	           
 	           log.info("armSubManager:saveArmSubCertificate:02:"+userCertX);
 	         
 	          SystemCertItem sci = new SystemCertItem();
 	    	 
 	          sci.setName(userCertX.getSubjectDN().getName());
 	          sci.setIssuer(userCertX.getIssuerDN().getName());
 	          sci.setSerial(dec_to_hex(userCertX.getSerialNumber()));
 	          sci.setDate1(df.format(userCertX.getNotBefore()));
 	          sci.setDate2(df.format(userCertX.getNotAfter()));
 	          
 	    	 Contexts.getEventContext().set("systemCertBean", sci);
 	    	 
 	       }
 	   	 }
 	   }catch(Exception e){
 		   log.error("forViewCert:Error:"+e);
 	   }
     } 
    
   public int getConnectError(){
	   return connectError;
   }
   
   public List<AcSubsystemCertBssT> getListArmSub() throws Exception{
	    log.info("armSubManager::getListArmSub:01");
	    try {
	    	if(listArmSub==null){
	       		listArmSub=entityManager.createQuery("select o from AcSubsystemCertBssT o").getResultList();
	    	}
	     } catch (Exception e) {
	    	 log.error("armSubManager::getListArmSub:ERROR:"+e);
	         throw e;
	     }
	    return listArmSub;
  }
   
   public List <BaseTableItem> getAuditItemsListSelect() {
		 log.info("getAuditItemsListSelect:01");
	
	    ArmSubContext ac= new ArmSubContext();
		   if( auditItemsListSelect==null){
			   log.info("getAuditItemsListSelect:02");
			   auditItemsListSelect = new ArrayList<BaseTableItem>();
			   
			
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("subsystemName"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("subsystemCode"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("armName"));
		   }
	       return this.auditItemsListSelect;
   }
   
   public void setAuditItemsListSelect(List <BaseTableItem> auditItemsListSelect) {
		    this.auditItemsListSelect=auditItemsListSelect;
   }
   
   public List <BaseTableItem> getAuditItemsListContext() {
	   log.info("orgManager:getAuditItemsListContext");
	   if(auditItemsListContext==null){
		   ArmSubContext ac= new ArmSubContext();
		   auditItemsListContext = new ArrayList<BaseTableItem>();
			   auditItemsListContext=ac.getAuditItemsCollection();
	   }
	   return this.auditItemsListContext;
   }
      
    
   public void selectRecord(){
	    String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	    log.info("selectRecord:sessionId="+sessionId);
	    
	   //  for/View(/); //!!!
	    List<String>  selRecArmSub = (ArrayList<String>)
				  Component.getInstance("selRecArmSub",ScopeType.SESSION);
	    
	    if(selRecArmSub==null){
	       selRecArmSub = new ArrayList<String>();
	       log.info("selectRecord:01");
	    }
	    
	    AcSubsystemCertBssT aa = new AcSubsystemCertBssT();
  	    
	    if(aa!=null){
	     if(selRecArmSub.contains(sessionId)){
	    	selRecArmSub.remove(sessionId);
	    	aa.setSelected(false);
	    	log.info("selectRecord:02");
	     }else{
	    	selRecArmSub.add(sessionId);
	    	aa.setSelected(true);
	    	log.info("selectRecord:03");
	     }
	    Contexts.getSessionContext().set("selRecArmSub", selRecArmSub);	
	    
	    Contexts.getEventContext().set("armSubBean", aa);
	   }
	}
   
   public String getDellMessage() {
	   return dellMessage;
   }
   public void setDellMessage(String dellMessage) {
	   this.dellMessage = dellMessage;
   } 
   
   public int getDelNot() {
	   return delNot;
   }
   public boolean getArmSubCodeExist() {
	    return armSubCodeExist;
   }
   private boolean armSubCodeExistCrt(Long idArm, String armSubCode) throws Exception {
		log.info("ArmSubManager:ArmSubcodeExistCrt:armSubCode="+armSubCode);
		if(armSubCode!=null){
		  try{
			  //� �����������
			  List<Object> lo=entityManager.createNativeQuery(
					  "select 1 "+
                      "from AC_SUBSYSTEM_CERT_BSS_T ass "+
                      "where ASS.UP_IS = ? "+
                      "and ASS.SUBSYSTEM_CODE = ? ")
	  				.setParameter(1, idArm)
	  				.setParameter(2, armSubCode)
	  				.getResultList();
	  
	          if(!lo.isEmpty()){
		        armSubCodeExist=true;
		        return this.armSubCodeExist;
	          }
	          
	          //� ��������
	          lo=entityManager.createNativeQuery(
					  "select 1 "+
                      "from AC_IS_BSS_T ass "+
                      "where ASS.SIGN_OBJECT = ? ")
	  				.setParameter(1, armSubCode)
	  				.getResultList();
	  
	          if(!lo.isEmpty()){
		        armSubCodeExist=true;
	          }
	          
			  log.info("ArmSubManager:ArmSubCodeExistCrt:addLoginExist!");	
			  
		  }catch(Exception e){
	           log.error("ArmSubManager:ArmSubCodeExistCrt:Error:"+e);
	           throw e;
       }
		}
		return this.armSubCodeExist;
 }
  
  private boolean armSubCodeExistUpd(Long idArm, String armSubCode, Long idArmSub) throws Exception {
	   
		log.info("ArmSubManager:ArmSubcodeExistUpd:armSubCode:"+armSubCode);
		log.info("ArmSubManager:ArmSubcodeExistUpd:idArmSub:"+idArmSub);
			
		if(armSubCode!=null){
		  try{
			//� �����������
			  List<Object> lo=entityManager.createNativeQuery(
					          "select 1 "+
		                      "from AC_SUBSYSTEM_CERT_BSS_T ass "+
		                      "where ASS.UP_IS = ? "+
		                      "and ASS.SUBSYSTEM_CODE = ? " +
		                      "and ASS.ID_SRV!= ? ")
			  				.setParameter(1, idArm)
			  				.setParameter(2, armSubCode)
			  				.setParameter(3, idArmSub)
			  				.getResultList();
			  
			  if(!lo.isEmpty()){
				  armSubCodeExist=true;
				  return this.armSubCodeExist;
			  }
			  
			  //� ��������
	          lo=entityManager.createNativeQuery(
					  "select 1 "+
                      "from AC_IS_BSS_T ass "+
                      "where ASS.SIGN_OBJECT = ? ")
	  				.setParameter(1, armSubCode)
	  				.getResultList();
	  
	          if(!lo.isEmpty()){
		        armSubCodeExist=true;
	          }
			  
			  log.info("ArmSubManager:armSubCodeExistUpd:addLoginExist!");		     
		    }catch(Exception e){
	           log.error("ArmSubManager:armSubCodeExistUpd:Error:"+e);
	           throw e;
         }
		}
		return this.armSubCodeExist;
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
	
   	log.info("armSubManager:evaluteForList:01");
   	if(evaluteForList==null){
   		evaluteForList=false;
    	String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
	             .getRequestParameterMap()
	             .get("remoteAudit");
	   log.info("armSubManager:evaluteForList:remoteAudit:"+remoteAudit);
     	
    	if(remoteAudit!=null&&
    	 
    	   !"OpenCrtFact".equals(remoteAudit)&&	
    	   !"OpenUpdFact".equals(remoteAudit)&&
    	   !"OpenDelFact".equals(remoteAudit)&&
   	       !"onSelColFact".equals(remoteAudit)&&
   	       !"refreshPdFact".equals(remoteAudit)){
    		log.info("reposManager:evaluteForList!!!");
   		    evaluteForList=true;
    	}
   	 }
       return evaluteForList;
   }
   public Boolean getEvaluteForListFooter() {
		
	    	if(evaluteForListFooter==null){
	   		evaluteForListFooter=false;
	    	String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
		             .getRequestParameterMap()
		             .get("remoteAudit");
		   log.info("armSubManager:evaluteForListFooter:remoteAudit:"+remoteAudit);
	     
	    	if(getEvaluteForList()&&
	    	   //new-1-	
	    	   !"protBeanWord".equals(remoteAudit)&&	
	    	   //new-2-	
	   	       !"selRecAllFact".equals(remoteAudit)&&
	   	       !"clRecAllFact".equals(remoteAudit)&&
	   	      // !remoteAudit equals "clSelOneFact"
	   	       !"onSelColSaveFact".equals(remoteAudit)){
	    		  log.info("armSubManager:evaluteForListFooter!!!");
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
			    log.info("armSubManager:evaluteForBean:remoteAudit:"+remoteAudit);
				String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			             .getRequestParameterMap()
			             .get("sessionId");
			    log.info("armSubManager:evaluteForBean:sessionId:"+sessionId);
		    	if(sessionId!=null && remoteAudit!=null &&
		    	   ("rowSelectFact".equals(remoteAudit)||	
		    	    "UpdFact".equals(remoteAudit))){
		    	      log.info("armSubManager:evaluteForBean!!!");
		   		      evaluteForBean=true;
		    	}
		   	 }
		     return evaluteForBean;
		   }

   private String dec_to_hex(BigInteger bi) {
		
		String result = null;
		
		try
		{
		 result = bi.toString(16);
		  
		}
		catch (NumberFormatException e)
		{
			log.error("Error! tried to parse an invalid number format");
		}
		 return result;
	}
   
}

