package iac.grn.infosweb.context.mc.rol;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.annotations.Out;
import java.util.*;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import iac.cud.infosweb.entity.*;

/**
 * ����������� ���, �������������� ��������� �������� ��������, ���������,
 * �������� � ������������� ��� ���������� ���� 
 * @author bubnov
 *
 */
//@Name("rolManager")
public class _RolManager {
//  private static final long serialVersionUID = 991300443278089016L;

	/**
     * ������ ��������� ��������� ���� ��� �����������
     */
	   private List<AcRole> listRol = null;
	   
	   /**
        * ������������� � �������������� �������� ���� 
        * ��� ����������� � ��������������
        */
	   @In(required = false)
	   @Out(required = false)
	   private AcRole acRolBean;
	   
	   /**
        * ������������� � �������������� �������� ���� 
        * ��� ��������
        */
	   @In(required = false)
	   @Out(required = false)
	   private AcRole acRolBeanCrt;
	   //private Long arm;
	   
	   private List<AcApplication> listRolArm = null;
	   
	   /**
	     * ������ ��������� ��������� ���� ��� ��������
	     */
	   private List<AcAppPage> listRolRes = null;
	   /**
	     * ������ ��������� ��������� ���� ��� ��������
	     */
	   private List<AcAppPage> listRolResEdit = null;
	   
	   /**
	     * ������ ��������� ��������� ���������� ��� ��������
	     */
	   private List<AcPermissionsList> listRolPerm = null;
	   
	   /**
	     * ������ ��������� ������������� ����������
	     */
	   private List<Long> checkboxPerm = null;
	   
	   /**
        * ������������� ��������� �������� ����
        */
	   private String pidRol=null; 
	   /**
        * �������� ���������, �������������� �������������� � ��
        */
	   @In 
	   EntityManager entityManager;
   
	   public List<AcRole> getListRol() throws Exception{
		    System.out.println("RolManager:getListRol:01");
		    try {
		    	if(listRol==null){
		    	//	System.out.println("getListArm_02");
		    		listRol=entityManager.createQuery("select o from AcRole o").getResultList();
		    	}
		     } catch (Exception e) {
		    	 System.out.println("RolManager:getListRol:ERROR:"+e);
		         throw e;
		     }
		    return listRol;
	   }
	   public List<AcApplication> getListRolArm() throws Exception{
		    System.out.println("RolManager:getListRolArm:01");
		    try {
		    	if(listRolArm==null){
		     	//	listRolArm=em.createQuery("select _o from AcApplication _o where _o IN (select o from AcApplication o JOIN o.acAppPages o1 JOIN o1.acLinkRoleAppPagePrmssns o2)").getResultList();
		     		listRolArm=entityManager.createQuery("select o from AcApplication o where o.acRoles IS NOT EMPTY").getResultList();
						
		    	}
		     } catch (Exception e) {
		    	 System.out.println("RolManager:getListRolArm:ERROR:"+e);
		         throw e;
		     }
		    return listRolArm;
	   }
	   public List<AcAppPage> getListRolRes() throws Exception{
		   // System.out.println("RolManager:getListRolRes:arm:"+this.arm);
		    try {
		    	System.out.println("!!!RolManager:getListRolRes:01:acRolBeanCrt==null:"+(acRolBeanCrt==null));
		    
		    	//��� ������ ������� - ������ ������ ���� ����,
		    	//�.�. ��� ��� �� ������
		    	
		    	//� h:selectOneRadio immediate="true", ����� -
		    	//acRolBeanCrt ����� null
		    	String pidArm=null;
		    	if(acRolBeanCrt!=null){
		    	
		    	if(acRolBeanCrt.getAcApplication()==null){
		    		 pidArm= FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("CBformCrt:appl"); //����� ��� ajax ����� ����
		    		// .get("CBformCrt:alCrt");
		    		 System.out.println("RolManager:getListRolRes:pidArm:"+pidArm);
		    	 }
		    		    	
		    	if(listRolRes==null&&(acRolBeanCrt.getAcApplication()!=null||pidArm!=null)){
		    		System.out.println("RolManager:getListRolRes:02");
		    		//��� ����:
		    		//1)��� ajax ����� ���(pidArm!=null)
		    		//2)��� ������� ��������� (pidArm!=null)
		    		listRolRes=entityManager.createQuery("select o from AcAppPage o where " +
		    				"o.idResCollection is empty and "+
		    				"o.visible=1 and "+
		    		    	"o.acApplication = :idArm and " +
		    				"o.idParent2 !=1 and o.pageCode is not null ")
		    				.setParameter("idArm", (pidArm!=null ? new Long(pidArm) : acRolBeanCrt.getAcApplication()))
		    				.getResultList();
		   		   for(AcAppPage aap:listRolRes){
		   			  System.out.println("RolManager:getListRolRes:Cicle:1");
		   			  String st=aap.getPageName();
		   			  AcAppPage aapin=aap.getIdParent();
		   			   while(!aapin.getIdParent2().equals(new Long(1))){
		   				System.out.println("RolManager:getListRolRes:Cicle:2");
		   				   st=aapin.getPageName()+"/"+st;
		   				   aapin=aapin.getIdParent();
		   			   }
		   			   aap.setFullPageName(st);
		   		   }
		    		
		    	}
		       }
		    	System.out.println("RolManager:getListRolRes:03");
		     } catch (Exception e) {
		    	 System.out.println("RolManager:getListRolRes:ERROR:"+e);
		         throw e;
		     }
		    return listRolRes;
	   }
	 /*   public List<AcAppPage> getListRolRes() throws Exception{
		   // System.out.println("RolManager:getListRolRes:arm:"+this.arm);
		    try {
		    	
		    	//��� ������ �� ������ ������ ������ ���������� ������ ����
		    	//�.�. listRolRes==null. ����� ���������� ����� newRol.getAcApplication()==null
		    			    	
		    	String pidArm=null;
		    	if(acRolBeanCrt.getAcApplication()==null){
		    		 pidArm= FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("idRollAdd:idArm"); //����� ��� ������� ������ ���������
		    		 System.out.println("RolManager:getListRolRes:pidArm:"+pidArm);
		    	 }
		    	// newRol.getAcApplication()- ����� ��� ajax ����� ����
		    	
		    	if(listRolRes==null&&(acRolBeanCrt.getAcApplication()!=null||pidArm!=null)){
		    	//��� ����:
		    		//1)��� ������ �� ������ (addRol.getAcApplication()) 	
		    		//2)��� ajax ����� ���(pidArm!=null)
		    		//3)��� ������� ��������� (pidArm!=null)
		    		listRolRes=entityManager.createQuery("select o from AcAppPage o where o.acApplication = :idArm")
		    				.setParameter("idArm", (pidArm!=null ? new Long(pidArm) : acRolBeanCrt.getAcApplication()))
		    				.getResultList();
		   		   
		    	}
		     } catch (Exception e) {
		    	 System.out.println("RolManager:getListRolRes:ERROR:"+e);
		         throw e;
		     }
		    return listRolRes;
	   }*/
	   public void setListRolRes( List<AcAppPage> listRolRes ){
		   System.out.println("RolManager:setListRolRes");
		   this.listRolRes=listRolRes;
	   }
	   public List<AcAppPage> getListRolResEdit() throws Exception{
		   // System.out.println("RolManager:getListRolResEdit:arm:"+this.arm);
		  /*  String pscipAllFlag= FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("scipAllFlag");
		    	
		   if(pscipAllFlag==null){ */
		    try {
		    	
		    	// ��� ������ �� ������ - 1)����� = editRol.getAcApplication(),
		    	// 2)������ = pidRol (��� �� � editRol.idRol)
		        // ��� ajax ����� ��� - 1)����� = pidArm,
		    	// 2)������ = idForAjax
		    	// ��� ������� ��������� - 1)����� = pidArm,
		    	// 2)������ = idForAjax -�� ����������, ��� ��� ������������� saveEditFlag=1
		    	
		    	
		    	//pidRol -global ����������!!!
		    	String pidArm=null, saveEditFlag,idForAjax;
		    	
		    	 System.out.println("RolManager:getListRolResEdit:editRol.getAcApplication:"+acRolBean.getAcApplication());
		    	if(acRolBean.getAcApplication()==null){ //��� ������ �� ������ ������� = ���
		    		 pidArm= FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("CBformUpd:appl"); //����� ��� ������� ������ ���������
		    		// .get("CBformUpd:alUpd")
		    		 System.out.println("RolManager:getListRolResEdit:pidArm:"+pidArm);
		       	}
		    	if(listRolResEdit==null&&(acRolBean.getAcApplication()!=null||pidArm!=null)){
		    		//��� ����:
			    	//1)��� ������ �� ������ (editRol.getAcApplication()) 	
		    		//2)��� ajax ����� ��� (pidArm!=null)
		    		//3)��� ������� ��������� (pidArm!=null)
			    	
		    		listRolResEdit=entityManager.createQuery("select o from AcAppPage o where " +
		    				"o.idResCollection is empty and " +
		    				"o.visible=1 and "+
		    				"o.acApplication = :idArm and " +
		    				"o.idParent2 !=1 and o.pageCode is not null ")
		    				.setParameter("idArm",(pidArm!=null ? new Long(pidArm) : acRolBean.getAcApplication()))
		    				.getResultList();
		    		saveEditFlag= FacesContext.getCurrentInstance().getExternalContext()
		 			        .getRequestParameterMap()
		 			        .get("saveEditFlag");
		    		 System.out.println("RolManager:getListRolResEdit:saveEditFlag:"+saveEditFlag);
		    		 
		    		 idForAjax= FacesContext.getCurrentInstance().getExternalContext()
		 			        .getRequestParameterMap()
		 			      //  .get("CBformUpd:idForAjax");
		    		         .get("idForAjax");
		    		 System.out.println("RolManager:getListRolResEdit:idForAjax!!!:"+idForAjax);
		    		
		    	 	 
		    	  if(saveEditFlag==null){
		    		  System.out.println("RolManager:getListRolResEdit:01");
		    		  //����� ������, ����� ������� ������ ��������� - ��� ��� ������� �� �����, 
		    		  //� ��� ������ ����� �������� � �����
		           for(AcAppPage aca :listRolResEdit){
		        	   System.out.println("RolManager:getListRolResEdit:02:1");
		        	   
		        		  String st=aca.getPageName();
			   			  AcAppPage aapin=aca.getIdParent();
			   			   while(!aapin.getIdParent2().equals(new Long(1))){
			   				   st=aapin.getPageName()+"/"+st;
			   				   aapin=aapin.getIdParent();
			   			   }
			   			   aca.setFullPageName(st);
			   			 System.out.println("RolManager:getListRolResEdit:02:2");
			   			 
		        	if(aca.getAcLinkRoleAppPagePrmssns()!=null){ 
		        		 System.out.println("RolManager:getListRolResEdit:03");
		        	 Iterator it= aca.getAcLinkRoleAppPagePrmssns().iterator();
		        	 List<Long> ls = new ArrayList<Long>();
		        	 System.out.println("RolManager:getListRolResEdit:04");
		        	 while (it.hasNext()){
		        		 System.out.println("RolManager:getListRolResEdit:05");
		            AcLinkRoleAppPagePrmssn me = (AcLinkRoleAppPagePrmssn) it.next();
		            System.out.println("RolManager:getListRolResEdit:06");
			            if((me.getAcAppPage().getIdRes().equals(aca.getIdRes())) && 
			               (me.getAcRole().getIdRol().equals(new Long((pidRol!=null?pidRol:idForAjax)))) ){
			        	   System.out.println("RolManager:getListRolResEdit:IdPerm:"+me.getAcPermissionsList().getIdPerm());
			        	 ls.add(me.getAcPermissionsList().getIdPerm());
			         }
           	        } 
		        	 aca.setPermList(ls);
		           }
		           
		           }
		    	  }
		    	}
		     } catch (Exception e) {
		    	 System.out.println("getListRolResEdit:ERROR="+e);
		         throw e;
		     }
		  //  }
		    return listRolResEdit;
	   }
	   public void setListRolResEdit( List<AcAppPage> listRolResEdit ){
		   System.out.println("RolManager:setListRolResEdit");
		   this.listRolResEdit=listRolResEdit;
	   }
	   public List<AcPermissionsList> getListRolPerm() throws Exception{
		    System.out.println("getListRolPerm_01");
		    try {
		    	if(listRolPerm==null){
		     		listRolPerm=entityManager.createQuery("select o from AcPermissionsList o").getResultList();
		       	}
		     } catch (Exception e) {
		    	 System.out.println("getListRolPerm_ERROR="+e);
		         throw e;
		     }
		    return listRolPerm;
	   }
	   public List<Long> getCheckboxPerm() throws Exception{
		    System.out.println("getCheckboxPerm_01");
		    if (checkboxPerm==null){
		    	checkboxPerm = new ArrayList();
		    	checkboxPerm.add(new Long(1));
		    	checkboxPerm.add(new Long(3));
		    	checkboxPerm.add(new Long(5));
		       }
		    return checkboxPerm;
	   }
	   public void setCheckboxPerm(List<Long> checkboxPerm) throws Exception{
		    System.out.println("setCheckboxPerm_01");
		    this.checkboxPerm=checkboxPerm;
	   }
	   /**
		  * �������� �������� ����
		  * @return ���� ����������
		  * @throws Exception
		  */
	   public String addRol() throws Exception {
		      System.out.println("RolManager:addRol");
		      try {
		    	  acRolBeanCrt.setCreator(new Long(1));
		    	  acRolBeanCrt.setCreated(new Date());
		    	  
		    	  entityManager.persist(acRolBeanCrt);
		    	  
		    	  System.out.println("RolManager:addRol:IdRol:"+acRolBeanCrt.getIdRol());
		    	  
		    	  for(AcAppPage res:listRolRes){
		    		  System.out.println("RolManager:addRol:Res:"+res.getPageName());
		    		  for(Object l:res.getPermList()){
		    			  System.out.println("RolManager:addRol:perm:"+l.toString());
		    			  AcLinkRoleAppPagePrmssn ap = new AcLinkRoleAppPagePrmssn(res.getIdRes(), new Long(l.toString()), acRolBeanCrt.getIdRol());
		    			  ap.setCreated(new Date());
		    			  ap.setCreator(new Long(1));
		    			  entityManager.persist(ap);
		    		  }
		    	  }
		    	  entityManager.flush();
		    	  entityManager.clear();
		    	  
			     } catch (Exception e) {
		        System.out.println("RolManager:addRol:ERROR:"+e);
		        throw e;
		      }
		      return "addRol";
		   } 
	   /**
		  * ��������� �������� ����
		  * @return ���� ����������
		  * @throws Exception
		  */
	   public String editRol() throws Exception {
		      System.out.println("RolManager:editRol");
		      try {
		    	   entityManager.createQuery("UPDATE AcRole r SET " +
	    	      		         "r.roleTitle = :roleTitle, " +
	    	      		         "r.roleDescription = :roleDescription, " +
	    	      		         "r.acApplication = :acApplication, " +
	    	      		         "r.modificator = :modificator, " +
	    	      		         "r.modified = :modified " +
	    	      		         "WHERE r.idRol = :idRol")
	    	      		         .setParameter("roleTitle", acRolBean.getRoleTitle())
	    	      		         .setParameter("roleDescription", acRolBean.getRoleDescription())
	    	      		         .setParameter("acApplication", acRolBean.getAcApplication())
	    	      		         .setParameter("modificator", new Long(1))
	    	      		         .setParameter("modified", new Date())
	    	      		         .setParameter("idRol", acRolBean.getIdRol())
	    	      		         .executeUpdate();
		    	  entityManager.createQuery("DELETE FROM AcLinkRoleAppPagePrmssn r " +
	      		         "WHERE r.pk.acRole= :acRole ")
	      		         .setParameter("acRole", acRolBean.getIdRol())
	      		         .executeUpdate();
		    	/*  entityManager.createQuery("DELETE FROM AcLinkUserToRoleToRaion r " +
		      		         "WHERE r.pk.acRole= :acRole ")
		      		         .setParameter("acRole", acRolBean.getIdRol())
		      		         .executeUpdate();*/
		    	  
		    	  entityManager.clear();
		     
		         for(AcAppPage res:listRolResEdit){
		    		  System.out.println("RolManager:editRol:Res:"+res.getPageName());
		    		  for(Object l:res.getPermList()){
		    			  System.out.println("RolManager:editRol:perm:"+l.toString());
		    			  AcLinkRoleAppPagePrmssn ap = new AcLinkRoleAppPagePrmssn(res.getIdRes(), new Long(l.toString()), acRolBean.getIdRol());
		    			  ap.setCreated(new Date());
		    			  ap.setCreator(new Long(1));
		    			  entityManager.persist(ap);
		    		  }
		    	  }
		         acRolBean = entityManager.find(AcRole.class, acRolBean.getIdRol());
	          } catch (Exception e) {
	            System.out.println("RolManager:editRol:ERROR="+e);
	            throw e;
	         }
		      return "editRol";
		   }
	   /**
		  * �������� �������� ����
		  * @return ���� ����������
		  * @throws Exception
		  */
	   public String delRol() throws Exception {
		      System.out.println("RolManager:delRol");
		      try {
		    	  AcRole ar= entityManager.find(AcRole.class,acRolBean.getIdRol());
		    	  entityManager.remove(ar);
		    	/*  entityManager.createQuery("DELETE FROM AcLinkRoleAppPagePrmssn r " +
		      		         "WHERE r.pk.acRole= :acRole ")
		      		         .setParameter("acRole", acRolBean.getIdRol())
		      		         .executeUpdate(); 
		    	  entityManager.createQuery("DELETE FROM AcRole r " +
 	      		                 "WHERE r.idRol= :idRol")
 	      		         .setParameter("idRol", acRolBean.getIdRol())
 	      		         .executeUpdate();*/
	          } catch (Exception e) {
	           System.out.println("RolManager:delRol:ERROR:"+e);
	           throw e;
	          }
 		      return "delRol";
		   }
	   /**
		  * ���������� �������� ���� 
		  * ��� ����������� �������� ���������, ��������, ���������
		  */
	   public void forView() {
		   pidRol = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("idRol");
				 System.out.println("forView_pidRol="+pidRol);
				 if(pidRol!=null){
					 acRolBean = entityManager.find(AcRole.class, new Long(pidRol));
		}
	 }
	/*   public AcRole getNewRol() {  //����������� ��� ������ �� ������
	      return newRol;
	   }
	   public AcRole getEditRol() {//����������� ��� ������ �� ������
		pidRol= FacesContext.getCurrentInstance().getExternalContext()
	        .getRequestParameterMap()
	        .get("idRol");
		 System.out.println("RolManager:getEditRol:idRol="+pidRol);
		 if(pidRol!=null){
		  editRol = em.find(AcRole.class, new Long(pidRol));
		//  setArm(editRol.getArm());
		}
	      return editRol;
	   }*/
	/*   public Long getArm() {
		      return this.arm;
	   }
	   public void setArm(Long arm) {
	       this.arm=arm;
	   }*/
}
