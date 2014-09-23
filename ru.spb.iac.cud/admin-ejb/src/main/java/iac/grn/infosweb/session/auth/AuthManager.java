package iac.grn.infosweb.session.auth;

import iac.cud.infosweb.entity.AcUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import java.util.ArrayList;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;

/**
 * Session Bean implementation class AuthManager
 */
//@Name("authManager")
//@Stateless
//@RemoteBinding(jndiBinding="auth.AuthManager.remote")
//@LocalBinding(jndiBinding="auth.AuthManager.local")
public class AuthManager implements AuthManagerLocal, AuthManagerRemote {

	@In EntityManager entityManager;
	
    public AuthManager() {
    }

    public Map<String, ArrayList<String>[]> authComplete(Long appCode, String login, String password) throws Exception{
    	
    	Map<String, ArrayList<String>[]> result = new HashMap<String, ArrayList<String>[]>();
    	
      	try{
    			
    		System.out.println("auth_01");
    		Long idUser = createAuth(login,password);
       
    		if(idUser==null){
    			return null;
    		}
    		System.out.println("AuthManager:getAuthConstraint:01:appCode:"+appCode);
    		System.out.println("AuthManager:getAuthConstraint:01:idUser:"+idUser);
    	
    		result = createResourceTree(appCode, null, idUser);
    	 }catch(Exception e){
    		 System.out.println("AuthManager:getAuthConstraint:Error:"+e);
    		 throw e;
    	}
      	return result;
  }
    public Long authenticate(String login, String password) throws Exception{
      return createAuth(login,password);
    }
    public ArrayList<String>[] access(Long appCode,String pageCode, Long idUser) throws Exception{
     	return createResourceTree(appCode, pageCode, idUser).get(pageCode);
    }

    private Long createAuth(String login, String password) throws Exception{
    	Long idUser=null;
    	try{
    		System.out.println("AuthManager:authenticate:01");
       	    idUser = ((java.math.BigDecimal) entityManager.createNativeQuery(
                 "select AU.ID_USER "+
                 "from "+
                 "AC_USERS au "+
                 "where AU.LOGIN=? "+
                 "and AU.PASSWORD=? "+
                 "and (AU.START_ is null or au.START_ <= sysdate) "+ 
                 "and (AU.FINISH_ is null or au.FINISH_ > sysdate) ")
                 .setParameter(1, login)
                 .setParameter(2, password)
                 .getSingleResult()).longValue();
    		
       	 System.out.println("AuthManager:authenticate:idUser:"+idUser);
       	 
     }catch (NoResultException ex){
         System.out.println("AuthManager:authenticate:NoResultException");
     }catch(Exception e){
		 System.out.println("AuthManager:getAuthConstraint:Error:"+e);
		 throw e;
	}
     return idUser;
  }
     private Map<String, ArrayList<String>[]> createResourceTree(Long appCode, String pageCode, Long idUser) throws Exception{
    	
        Map<String, ArrayList<String>[]> result = new HashMap<String, ArrayList<String>[]>();
    	
    	ArrayList<String> raiList=new ArrayList<String>();
    	ArrayList<String> permList=new ArrayList<String>();
    	String pageCode_prev="", pageCode_curr="";
    	String idRai_prev="", idRai_curr="";
    	int raiChange=0;
    
    	try{
    	List<Object[]> lo=
        		entityManager.createNativeQuery(
        				      "select AAD.PAGE_CODE, ARR.ID_RAION, ADP.ID_PERM "+
                              "from AC_APP_DOMAINS aad, "+
                              "AC_LINK_ROLE_APP_DOMEN_PRMSSNS adp, "+
                              "AC_LINK_USER_TO_ROLE_TO_RAIONS arr "+
                              "where AAD.APP_CODE=? and "+ 
                              "adp.ID_DOMEN=AAD.ID_DOMEN "+
                              "and adp.ID_ROLE=arr.ID_ROLE "+
                              "and arr.ID_USER=? "+
                              (pageCode!=null ? "and AAD.PAGE_CODE=? " : "and 1=? ")+
                              "group by AAD.PAGE_CODE, ARR.ID_RAION, ADP.ID_PERM "+
                              "order by  AAD.PAGE_CODE, ARR.ID_RAION, ADP.ID_PERM ")
                .setParameter(1, appCode)
       	        .setParameter(2, idUser)
       	        .setParameter(3, (pageCode!=null?pageCode:1))
       			.getResultList();
        		
        		System.out.println("AuthManager:getAuthConstraint:02");
        		 
        		for(Object[] objectArray :lo){
    	    		//	System.out.println("Array:"+
    	    		//   (objectArray[0].toString()+", "+objectArray[1].toString()+", "+objectArray[2].toString()));
    	    			pageCode_curr=objectArray[0].toString();
    	    			idRai_curr=objectArray[1].toString();
    	    			
    	    			if(pageCode_curr.equals(pageCode_prev)||pageCode_prev.equals("")){
    	    				
    	    				if((idRai_curr.equals(idRai_prev)||idRai_prev.equals(""))){
    	    					if(raiChange==0){
    	    					  permList.add(objectArray[2].toString());
    	    					//  System.out.println("permList.add:"+objectArray[2].toString());
    	    					} 
    	    					if(idRai_prev.equals("")&&!idRai_curr.equals("-1")){
    	    					//	System.out.println("raiList.add:1:"+idRai_curr);
    	    					  raiList.add(idRai_curr);
    	    				   }
    	    				}else{
    	    					
    	    					if(!idRai_curr.equals("-1")){
    	    					  raiList.add(idRai_curr);
    	    					}
    	    				//	  System.out.println("raiList.add:2:"+objectArray[1].toString());
    	    					raiChange=1;
    	    				}
    	    				
    	    				//	result.put(pageCode_curr, value)
    	    			}else{
    	    			//	ArrayList[] al={;
    	    				result.put(pageCode_prev,new ArrayList[]{raiList,permList});
    	    				 System.out.println("result.put:"+pageCode_prev);
    	    				raiChange=0;
    	    				raiList.clear();
    	    				permList.clear();
    	    				if(!idRai_curr.equals("-1")){
    	    				  raiList.add(objectArray[1].toString());
    	    				}
    	    				permList.add(objectArray[2].toString());
    	    				
    	    			}
    	    			
    	    			idRai_prev=idRai_curr;
    	    			pageCode_prev=pageCode_curr;
    	    		//	 System.out.println("_idRai_prev:"+idRai_prev);
    	    		//	 System.out.println("_pageCode_prev:"+pageCode_prev);
    	     		}
    			if(!idRai_curr.equals("")){
    			 result.put(pageCode_prev,new ArrayList[]{raiList,permList});
    			}
        		for(Iterator<Map.Entry<String, ArrayList<String>[]>> it = result.entrySet().iterator(); it.hasNext();)
    			{
    			      Map.Entry<String, ArrayList<String>[]> me = it.next();
    			      System.out.println("me.getKey:"+me.getKey());
    	     		 
    	     		  for(String s:me.getValue()[0]){
    	     			  System.out.println("me.getValue[0]:"+s);
    	     		  }
    	     		  for(String s:me.getValue()[1]){
    	     			  System.out.println("me.getValue[1]:"+s);
    	     		  }
    	       }
    }catch(Exception e){
		 System.out.println("AuthManager:getAuthConstraint:Error:"+e);
		 throw e;
	}
    	return result;
    }
}