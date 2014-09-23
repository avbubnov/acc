package ru.spb.iac.cud.uarm.ejb.context.reg;

import java.util.Date;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import ru.spb.iac.cud.uarm.ejb.entity.AcUsersKnlT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppAccessBssT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppAccessGroupsBssT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppUserBssT;

/**
 * Session Bean implementation class HomeBean
 */
@Stateless(mappedName = "userAccessEJB")
@LocalBean
public class UserAccessEJB {

   
	@PersistenceContext(unitName = "CUDUserConsolePU")
    private EntityManager entityManager;
	
    public UserAccessEJB() {
        // TODO Auto-generated constructor stub
    }

    public void save(JournAppAccessBssT user) {

       System.out.println("userAccessEJB:save:01");
       System.out.println("userAccessEJB:save:02:"+user.getCodeSystem());
       try{
    	  /*List<JournAppUserBssT>  app_user_list = entityManager
    			  .createQuery("select t1 from JournAppUserBssT t1 ")
    			  .getResultList();
    	  
    	  System.out.println("UserRegEJB:save:03:"+app_user_list.size());
    	  */
    	   
    	   user.setCreated(new Date());
    	   entityManager.persist(user);
    	   
    	   
       }catch(Exception e){
    	   System.out.println("userAccessEJB:save:error:"+e);
       }
     }
    
    public void saveGroup(JournAppAccessGroupsBssT user) {

        System.out.println("userAccessEJB:saveGroup:01");
      
        try{
     	  /*List<JournAppUserBssT>  app_user_list = entityManager
     			  .createQuery("select t1 from JournAppUserBssT t1 ")
     			  .getResultList();
     	  
     	  System.out.println("UserRegEJB:save:03:"+app_user_list.size());
     	  */
     	   
     	   user.setCreated(new Date());
     	   entityManager.persist(user);
     	   
     	   
        }catch(Exception e){
     	   System.out.println("userAccessEJB:saveGroup:error:"+e);
        }
      }
}
