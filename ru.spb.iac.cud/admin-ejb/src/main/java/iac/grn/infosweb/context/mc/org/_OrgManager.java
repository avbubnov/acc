package iac.grn.infosweb.context.mc.org;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import java.util.*;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import iac.cud.infosweb.entity.*;

/**
 * Управляющий Бин, обеспечивающий выполение операций создания, изменения,
 * удаления и представления над сущностями Организации 
 * @author bubnov
 *
 */
//@Name("orgManager")
public class _OrgManager {
//  private static final long serialVersionUID = 991300443278089016L;

	   private String number;
	   /**
        *Сообщение выдаваемое при удалении сущности Консоль 
        *при обнаружении порождённых записей
        */
	   private String dellMessage;
	   
	   /**
        * Список доступных сущностей Организация для отображения
        */
	   private List<AcOrganization> listOrg = null;
	   private List<AcLegalEntityType> listOrgLET= null;
	  // private AcOrganization newOrg = new AcOrganization();
	   
	   /**
        * Импортируемая и экспортируемая сущности Организация 
        * для отображения и редактирования
        */
	   @In(required = false)
	   @Out(required = false)
	   private AcOrganization acOrgBean;
	   
	   /**
        * Импортируемая и экспортируемая сущности Организация 
        * для создания
        */
	   @In(required = false)
	   @Out(required = false)
	   private AcOrganization acOrgBeanCrt;
	   
	   /**
        * Список доступных сущностей Тип Организации для создания
        */
	   private List<AcLegalEntityType> listLET = null;
	   
	   
	   private Long rowsCount=new Long(5);
	   
	  // @Inject @HttpParam
	   //private String idOrg;
	   /**
        * Менеджер сущностей, обеспечивающий взаимодействие с БД
        */
	   @In 
	   EntityManager entityManager;
   
	   public String getNumber() {
	      return number;
	   }
	   public String getDellMessage() {
	      return dellMessage;
	   }
	   public void setNumber(String number) {
	      this.number = number;
	   }
	   public void setDellMessage(String dellMessage) {
		      this.dellMessage = dellMessage;
	   }
	   public Long getRowsCount() {
		/*   if(rowsCount==null){
		   String rowsCountSt = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("rowsCount");
		   System.out.println("rowsCountSt:"+rowsCountSt);
		   if(rowsCountSt!=null){
			   rowsCount=new Long(rowsCountSt);
		   }
		   }*/
		      return rowsCount;
		   }
	  public void setRowsCount(Long rowsCount) {
		      this.rowsCount = rowsCount;
		   }
	 /*  @SuppressWarnings("unchecked")
	   @Produces
	   @Named("listOrg")
	   @RequestScoped*/
	   public List<AcOrganization> getListOrg() throws Exception{
		    System.out.println("getListOrgEJB3_01");
		    try {
		    	if(listOrg==null){
		    		System.out.println("getListOrgEJB3_02");
		    		listOrg=entityManager.createQuery("select o from AcOrganization o").getResultList();
		    	}
		     } catch (Exception e) {
		    	 System.out.println("getListOrgEJB3_ERROR="+e);
		         throw e;
		     }
		    return listOrg;
	   }
	   public List<AcLegalEntityType> getListOrgLET() throws Exception{
		    System.out.println("getListOrgLETEJB3_01");
		    try {
		    	if(listOrgLET==null){
		    	//	System.out.println("getListOrgEJB3_02");
		    		listOrgLET=entityManager.createQuery("SELECT o FROM AcLegalEntityType o WHERE o.acOrganizations IS NOT EMPTY").getResultList();
		    	}
		     } catch (Exception e) {
		    	 System.out.println("getListOrgLETEJB3_ERROR="+e);
		         throw e;
		     }
		    return listOrgLET;
	   }
	   /**
		  * Создание сущности Оргпнизация
		  * @return флаг успешности
		  * @throws Exception
		  */
	   public String addOrg() throws Exception {
		      System.out.println("addOrg");
		      try {
		    	      acOrgBeanCrt.setCreator(new Long(1));
		    	      acOrgBeanCrt.setCreated(new Date());
		    	      entityManager.persist(acOrgBeanCrt);
		          } catch (Exception e) {
		        System.out.println("addOrg_ERROR="+e);
		        throw e;
		      }
		      return "addOrg";
		   }
	   /**
		  * Изменение сущности Оргпнизация
		  * @return флаг успешности
		  * @throws Exception
		  */
	   public String editOrg() throws Exception {
		      System.out.println("editOrgEJB3");
		      try {
		    	  System.out.println("editOrg_FullName="+acOrgBean.getFullName());
		    	  System.out.println("editOrg_IdOrg="+acOrgBean.getIdOrg());
		         // editOrg.setModificator(new Long(1));
		         // editOrg.setModified(new Date());
		    	  // entityManager.merge(acOrgBean);
		    	
		    	  entityManager.createQuery("UPDATE AcOrganization r SET " +
	    	      		         "r.fullName= :fullName, " +
	    	      		         "r.shortName= :shortName, " +
	    	      		         "r.contactEmployeeFio= :contactEmployeeFio, " +
	    	      		         "r.contactEmployeePosition= :contactEmployeePosition, " +
	    	      		         "r.contactEmployeePhone= :contactEmployeePhone, " +
	    	      		         "r.acLegalEntityType= :acLegalEntityType, " +
	    	      		         "r.modificator= :modificator, " +
	    	      		         "r.modified= :modified " +
	    	      		         "WHERE r.idOrg= :idOrg")
	    	      		         .setParameter("fullName", acOrgBean.getFullName())
	    	      		         .setParameter("shortName", acOrgBean.getShortName())
	    	      		         .setParameter("contactEmployeeFio", acOrgBean.getContactEmployeeFio())
	    	      		         .setParameter("contactEmployeePosition", acOrgBean.getContactEmployeePosition())
	    	      		         .setParameter("contactEmployeePhone", acOrgBean.getContactEmployeePhone())
	    	      		         .setParameter("acLegalEntityType", acOrgBean.getAcLegalEntityType())
	    	      		         .setParameter("modificator", acOrgBean.getModificator())
	    	      		         .setParameter("modified", acOrgBean.getModified())
	    	      		         .setParameter("idOrg", acOrgBean.getIdOrg())
	    	      		         .executeUpdate();
		    	 // entityManager.clear();
		       	
		    	  AcOrganization ao= entityManager.find(AcOrganization.class, acOrgBean.getIdOrg());
		    	  entityManager.refresh(ao);
		    	  
	          } catch (Exception e) {
	            System.out.println("editOrgEJB3_ERROR="+e);
	            throw e;
	         }
		      return "editOrg";
		   }
	   /**
		  * Удаление сущности Оргпнизация
		  * @return флаг успешности
		  * @throws Exception
		  */
	   public String delOrg() throws Exception {
		      System.out.println("delOrgEJB3");
		      try {
		    	  AcOrganization ao=  entityManager.find(AcOrganization.class,acOrgBean.getIdOrg());
		    	  entityManager.remove(ao);
		    	 /*  entityManager.createQuery("DELETE FROM AcOrganization r " +
 	      		                 "WHERE r.idOrg= :idOrg")
 	      		         .setParameter("idOrg", acOrgBean.getIdOrg())
 	      		         .executeUpdate();*/
		    	  
	          } catch (Exception e) {
	           System.out.println("delOrgEJB3_ERROR="+e);
	           throw e;
	          }
 		      return "delOrg";
		   }
	   public List<AcLegalEntityType> getListLET() throws Exception{
		   System.out.println("getLETEJB3");
		    try {
		    	if(listLET==null){
		    	  listLET = entityManager.createQuery("select r from AcLegalEntityType r").getResultList();
		    	 }
		    	} catch (Exception e) {
		    	 System.out.println("getLETEJB3_ERROR="+e);
		         throw e;
	           }
		    return listLET;
      }
	/*	   
	   @Out(required = false,scope=ScopeType.EVENT)
       public AcOrganization getNewOrg() {
	      return newOrg;
	   }
	   */
	   
	   /*   
	   public AcOrganization getEditOrg() {

	      return editOrg;
	   }
	  public void setEditOrg(AcOrganization  editOrg) {
		   this.editOrg=editOrg;
	   }*/
	   /**
		  * Подготовка сущности Организация 
		  * для последующих операций просмотра, создания, изменения
		  */
	   public void forView() {
		   String  pidOrg = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("idOrg");
				 System.out.println("forView_pidOrg="+pidOrg);
				 if(pidOrg!=null){
					 acOrgBean = entityManager.find(AcOrganization.class, new Long(pidOrg));
		}
	  }
	   /**
		  * Подготовка сущности Организация 
		  * для последующих операций удаления
		  */
		 public void forViewDel() {
			 String  pidOrg = FacesContext.getCurrentInstance().getExternalContext()
					.getRequestParameterMap()
					.get("idOrg");
				 System.out.println("forViewDel:pidOrg="+pidOrg);
				 if(pidOrg!=null){
						acOrgBean = entityManager.find(AcOrganization.class, new Long(pidOrg));
				// if(acOrgBean.getAcUsers()!=null&&!acOrgBean.getAcUsers().isEmpty()){
						dellMessage="У организации есть порождённые записи!\n При удалении они будут удалены!";
						System.out.println("forViewDel:dellMessage="+dellMessage);
				//	}
				 }	
	}
	/*   public AcOrganization getAcOrgBeanCrt() {
		      return acOrgBeanCrt;
		   }
	  public void setAcOrgBeanCrt(AcOrganization acOrgBeanCrt) {
		      this.acOrgBeanCrt = acOrgBeanCrt;
		   }*/
}
