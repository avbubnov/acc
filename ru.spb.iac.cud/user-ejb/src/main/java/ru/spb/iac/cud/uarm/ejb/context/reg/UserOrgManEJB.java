package ru.spb.iac.cud.uarm.ejb.context.reg;

import java.util.Date;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import ru.spb.iac.cud.uarm.ejb.entity.AcUsersKnlT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppAccessBssT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppAdminUserSysBssT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppOrgManagerBssT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppUserBssT;

/**
 * Session Bean implementation class HomeBean
 */
@Stateless(mappedName = "userOrgManEJB")
@LocalBean
public class UserOrgManEJB {

   
	@PersistenceContext(unitName = "CUDUserConsolePU")
    private EntityManager entityManager;
	
    public UserOrgManEJB() {
        // TODO Auto-generated constructor stub
    }

    public void save(JournAppOrgManagerBssT user) {

       System.out.println("userOrgManEJB:save:01");
      
       try{
    	  /*List<JournAppUserBssT>  app_user_list = entityManager
    			  .createQuery("select t1 from JournAppUserBssT t1 ")
    			  .getResultList();
    	  
    	  System.out.println("UserRegEJB:save:03:"+app_user_list.size());
    	  */
    	   
    	   user.setCreated(new Date());
    	   entityManager.persist(user);
    	   
    	   
       }catch(Exception e){
    	   System.out.println("userOrgManEJB:save:error:"+e);
       }
       
       
     }
}
