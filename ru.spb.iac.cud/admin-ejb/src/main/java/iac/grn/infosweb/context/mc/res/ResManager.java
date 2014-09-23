package iac.grn.infosweb.context.mc.res;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.log.Log;

import java.util.*;

import iac.cud.infosweb.entity.*;
import iac.grn.infosweb.session.navig.LinksMap;
import javax.faces.context.FacesContext;
import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import iac.grn.infosweb.session.audit.export.ActionsMap;
import iac.grn.infosweb.session.audit.export.AuditExportData;
import iac.grn.infosweb.session.audit.export.ResourcesMap;
import iac.grn.infosweb.session.cache.CacheManager;


/**
 * ����������� ���, �������������� ��������� �������� ��������, ���������,
 * �������� � ������������� ��� ���������� ������� 
 * @author bubnov
 *
 */
@Name("resManager")
public class ResManager {//implements OrgManagerInterface{
	
	 @Logger private Log log;
	
	//  private static final long serialVersionUID = 991300443278089016L;

	/**
     * ������ ��������� ��������� ������� ��� �����������
     */
	   private List<AcAppPage> listRes = null;
	   
	   /**
        *��������� ���������� ��� �������� �������� ������� 
        *��� ����������� ���������� �������
        */
	   private String dellMessage;
	   
	   /**
        * ������������� � �������������� �������� �������
        * ��� ����������� � ��������������
        */
	   @In(required = false)
	   @Out(required = false)
	   private AcAppPage acResBean;
	   
	   /**
        * ������������� � �������������� �������� ������� 
        * ��� ��������
        */
	   @In(required = false)
	   @Out(required = false)
	   private AcAppPage acResBeanCrt;
	   
	   /**
	     * ������ ��������� ��������� ������� ��� ��������
	     */
	   private List<AcApplication> listResArm = null;
	   
	 //  private List<AcApplication> listArm = null;
	   /**
        * �������� ���������, �������������� �������������� � ��
        */
	   @In 
	   EntityManager entityManager;
   
	   public String getDellMessage() {
		      return dellMessage;
	   }
	   public void setDellMessage(String dellMessage) {
		      this.dellMessage = dellMessage;
	   }
	   
	   public List<AcAppPage> getListRes() throws Exception{
		    log.info("getListRes_01");
		    try {
		    	if(listRes==null){
		    	//	log.info("getListArm_02");
		    		listRes=entityManager.createQuery("select o from AcAppPage o").getResultList();
		    	}
		     } catch (Exception e) {
		    	 log.error("getListRes_ERROR="+e);
		         throw e;
		     }
		    return listRes;
	   }
	   public List<AcApplication> getListResArm() throws Exception{
		    log.info("getListResArm_01");
		    try {
		    	if(listResArm==null){
		    	//	log.info("getListArm_02");
		    		listResArm=entityManager.createQuery("select o from AcApplication o WHERE o.acAppPages IS NOT EMPTY").getResultList();
		    	}
		     } catch (Exception e) {
		    	 log.error("getListResArm_ERROR="+e);
		         throw e;
		     }
		    return listResArm;
	   }
	   /**
		  * �������� �������� �������
		  * @return ���� ����������
		  * @throws Exception
		  */
	   public String addRes() throws Exception {
		      log.info("addRes");
		      try {
			 	  
		    	  String  pidRootNode = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("idRootNode");
		    	  log.info("addRes:pidRootNode:"+pidRootNode);
		    	  
		    	  String pappCode = FacesContext.getCurrentInstance().getExternalContext()
					        .getRequestParameterMap()
					        .get("appCode");
				  log.info("addRes:pappCode:"+pappCode);
				    	
				    	  
		    	  if(pidRootNode!=null&&pappCode!=null){ 
		    	   acResBeanCrt.setIdParent2(new Long(pidRootNode));
		    	   acResBeanCrt.setAcApplication(new Long(pappCode));
		    	   
		    	  if(acResBeanCrt.getVisibleBoolean()){
		    		  acResBeanCrt.setVisible(new Long(1));
		    	  }else{
		    		  acResBeanCrt.setVisible(new Long(0));
		    	  }
		    	  
		     //  [������� -1-]
		     // <a4j:region>
		    	 entityManager.persist(acResBeanCrt);
		    	 entityManager.flush();
		      	 entityManager.refresh(acResBeanCrt);
		    
		     //  [������� -2-]
		     //	 entityManager.persist(acResBeanCrt);
		     //	 entityManager.flush();
		     //	 AcAppPage parent = entityManager.find(AcAppPage.class, new Long(pidRootNode));
		     //	 parent.getIdResCollection().add(acResBeanCrt);
		    	  
		     //  [������� -3-]
             //  entityManager.persist(acResBeanCrt);
		     //  entityManager.flush();
		     //  entityManager.refresh(acResBeanCrt);
		     //  entityManager.refresh(acResBeanCrt.getIdParent());
		    
		      //  [������� -4-]
		      //  entityManager.persist(acResBeanCrt);
			  //  entityManager.flush();
			  //  AcAppPage parent = entityManager.find(AcAppPage.class, new Long(pidRootNode));
			  //  entityManager.refresh(parent);
		      	String ac = ((LinksMap)Component.getInstance("linksMap",ScopeType.APPLICATION)).getAppCode().toString();
				if(pappCode.equals(ac)){
				  cache();
				}
				audit(ResourcesMap.RES, ActionsMap.CREATE); 
				
		   	    }
		      } catch (Exception e) {
		        log.error("addRes_ERROR="+e);
		        throw e;
		      }
		      return "addRes";
		   }
	   /**
		  * ��������� �������� �������
		  * @return ���� ����������
		  * @throws Exception
		  */
	   public String editRes() throws Exception {
		      log.info("ResManager:editRes:IdRes:"+acResBean.getIdRes());
		    //  log.info("ResManager:editRes:AcApplication="+acResBean.getAcApplication());
		      
		      String  pidRootNode = FacesContext.getCurrentInstance().getExternalContext()
				        .getRequestParameterMap()
				        .get("idRootNode");
			  log.info("editRes:pidRootNode:"+pidRootNode);
			   
			  String  pappCode = FacesContext.getCurrentInstance().getExternalContext()
				        .getRequestParameterMap()
				        .get("appCode");
			  log.info("addRes:pappCode:"+pappCode);
		
			  String  idRes = FacesContext.getCurrentInstance().getExternalContext()
				        .getRequestParameterMap()
				        .get("idRes");
			  log.info("addRes:idRes:"+idRes);
		
		      try {
		    	  if(pidRootNode!=null && pappCode!=null && idRes!=null){
		    	/*  entityManager.createQuery("UPDATE AcAppPage r SET " +
	    	      		         "r.pageName= :pageName, " +
	    	      		         "r.acApplication= :acApplication, " +
	    	      		         "r.isRaions= :isRaions, " +
	    	      		         "r.visible= :visible, " +
	    	      		         //"r.modificator= :modificator, " +
	    	      		         //"r.modified= :modified " +
	    	      		         "r.idParent2= :idParent2, " +
	    	      		         "r.pageCode= :pageCode " +
	    	      		         "WHERE r.idRes= :idRes")
	    	      		      //   .setParameter("modificator", editOrg.getModificator())
	    	      		      //   .setParameter("modified", editOrg.getModified())
	    	      		         .setParameter("pageName", acResBean.getPageName())
	    	      		         .setParameter("idParent2", new Long(pidRootNode))
	    	      		         .setParameter("pageCode", acResBean.getPageCode())
	    	      		         .setParameter("acApplication", new Long(pappCode))
	    	      		         .setParameter("isRaions", (acResBean.getIsRaionsBoolean()? new Long(1): new Long(0)))
	    	      		         .setParameter("visible", (acResBean.getVisibleBoolean()? new Long(1): new Long(0)))
	    	      		         .setParameter("idRes", acResBean.getIdRes())
	    	      		         .executeUpdate();
		    	   entityManager.clear();
				   acResBean=entityManager.find(AcAppPage.class, acResBean.getIdRes());
				   */ 
		    		  
		    	  AcAppPage apm = entityManager.find(AcAppPage.class, new Long(idRes)/*acResBean.getIdRes()*/);
		    	  
		    	  apm.setPageName(acResBean.getPageName());
		    	  apm.setIdParent2(new Long(pidRootNode));
		    	  apm.setPageCode(acResBean.getPageCode());
		    	  apm.setAcApplication(new Long(pappCode));
		    	  apm.setVisible((acResBean.getVisibleBoolean()? new Long(1): new Long(0)));
		    	  apm.setOrderNum(acResBean.getOrderNum());
		    	  
		   	 //  <a4j:region>
		     //	 (relationship cascade REFRESH)
		    	  
		    	  entityManager.flush();
		    	  entityManager.refresh(apm);
		    	  
		    	  acResBean=apm;
		    	  
		    	  String ac = ((LinksMap)Component.getInstance("linksMap",ScopeType.APPLICATION)).getAppCode().toString();
				  if(pappCode.equals(ac)){
					  cache();
				  }
				  
				  audit(ResourcesMap.RES, ActionsMap.UPDATE); 
				  
		   	   }
	          } catch (Exception e) {
	            log.error("editRes_ERROR="+e);
	            throw e;
	         }
		      return "editRes";
		   }
	   /**
		  * �������� �������� �������
		  * @return ���� ����������
		  * @throws Exception
		  */
	   public String delRes() throws Exception {
		      log.info("delRes:01");
		      try {
		    	  AcAppPage ap= entityManager.find(AcAppPage.class, acResBean.getIdRes()) ;
		    	  
		    	  entityManager.remove(ap);
		    	  
		    	 // log.info("delRes:IdParent:"+ap.getIdParent2()); 
		    	
		    	  entityManager.flush();
		     	
		    	 // [������� -1-]
		    	 // ������������, ��� refresh - �� ��������� :
		    	 // @OneToMany(mappedBy="idParent", cascade={CascadeType.REMOVE, /*CascadeType.REFRESH*/})
		    	 // private Set<AcAppPage> idResCollection;
		    	 // ����� ��� refresh ��������, ����� refresh ��� ������� �� ������ idResCollection, 
		    	 // � ���� ������ ��� �������� ��������� �������, � ��� ������� ��� ������������� 
		    	 // ����� ������, ��� ��� ����� flush() � �� ��������� ������� ��� ���
		    	 
		    	  entityManager.refresh(ap.getIdParent());
		    	  
		    	 // [������� -2-]
		    	 /* if(parent!=null && parent.getIdResCollection()!=null && parent.getIdResCollection().contains(ap)){
		    		  log.info("delRes:04");
		    		  parent.getIdResCollection().remove(ap);
		    	  }*/
		    	  String ac = ((LinksMap)Component.getInstance("linksMap",ScopeType.APPLICATION)).getAppCode().toString();
				  if(ap.getAcApplication().toString().equals(ac)){
					cache();
				  }
				  
				  audit(ResourcesMap.RES, ActionsMap.DELETE); 
				  
		         } catch (Exception e) {
	           log.error("delRes_ERROR="+e);
	           throw e;
	          }
 		      return "delRes";
		   }
	   /**
		  * ���������� �������� ������� 
		  * ��� ����������� �������� ���������, ��������, ���������
		  */
	   public void forView() {
		   String  pidRes = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("idRes");
			log.info("forView_pidRes="+pidRes);
			if(pidRes!=null){
				 acResBean = entityManager.find(AcAppPage.class, new Long(pidRes));
			}
		}
	   /**
		  * ���������� �������� ������� 
		  * ��� ����������� �������� ��������
		  */
	   public void forViewDel() {
			String  pidRes = FacesContext.getCurrentInstance().getExternalContext()
					.getRequestParameterMap()
					.get("idRes");
			log.info("forViewDel_pidRes="+pidRes);
			if(pidRes!=null){
				 acResBean = entityManager.find(AcAppPage.class, new Long(pidRes));
				 if(acResBean.getIdResCollection()!=null&&!acResBean.getIdResCollection().isEmpty()){
					 dellMessage="� ������� ���� ���������� ������!\n ��� �������� ��� ����� �������!";
					 log.info("forViewDel:dellMessage="+dellMessage);
			    }
			}
	 }
	 public void cache(){
		try{
		/* log.info("armManager:cache:01");
		 CacheManager cm = (CacheManager) Component.getInstance("cacheManager",ScopeType.EVENT);
		 cm.removeCache("leftUnitFragment", "treeNavigation");
		 log.info("armManager:cache:02");*/
		}catch(Exception e){
		 log.error("armManager:cache:Error:"+e);
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
	 
	/*
	   public AcAppPage getNewRes() {
	      return newRes;
	   }
	   public AcAppPage getEditRes() {
		 String  pidRes = FacesContext.getCurrentInstance().getExternalContext()
	        .getRequestParameterMap()
	        .get("idRes");
		 log.info("getEditRes_idRes="+pidRes);
		 if(pidRes!=null){
		  editRes = em.find(AcAppPage.class, new Long(pidRes));
		}
	      return editRes;
	   }*/
}
/*select last_number from user_sequences
where sequence_name='SQNC_AC_ROLES';*/