package iac.grn.infosweb.session.navig;

import iac.cud.authmodule.dataitem.AuthItem;
import iac.cud.infosweb.dataitems.NavigItem;
import iac.cud.infosweb.entity.AcAppPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import iac.grn.infosweb.session.navig.LinksMap;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;


/**
 * Управляющий бин, осуществляющий реализацию навигации 
 * по используемым ресурсам приложения
 * @author bubnov
 *
 */
@Name("navigManager")
public class NavigManager {

	 @Logger private Log log;
	 
	// static Logger logger = Logger.getLogger(GRuNProFile.class);
	 
	/**
      * Менеджер сущностей, обеспечивающий взаимодействие с БД
      */
	 @In 
	 EntityManager entityManager;
	
	 /**
	   * Импортируемая сущности Используемых Ресурсов 
	   * для навигации
	   */
	 @In
	 LinksMap linksMap;
	
	 /**
	   * Текущий активный раздел
	   */
	 private String activeSection;
	 
	 /**
	   * Список элементов меню
	   */
	 private List<AcAppPage> listMenu = null;
	 
	 private List<NavigItem> listNavigMenu = null;
	 
	 public List<AcAppPage> getListMenu() throws Exception{
		 log.info("navigManager:getListMenu:01");
		 
		    try {
		    	if(listMenu==null){
		    	//	log.info("getListMenu:02");
		    		/*listMenu=entityManager.createQuery("select aap.idResCollection from AcAppPage aap " +
		    				"where aap.idParent2 = 1 and aap.acApplication = :acApplication " +
		    				"and aap.visible = 1")
		    				.setParameter("acApplication",linksMap.getAppCode())
		    				.getResultList();*/
		    		listMenu=entityManager.createQuery("select aap from AcAppPage aap " +
		    				"where aap.idParent.idParent2 = 1 and aap.idParent.acApplication = :acApplication " +
		    				"and aap.visible = 1 order by orderNum ")
		    				.setParameter("acApplication",linksMap.getAppCode())
		    				.getResultList();
		    	}
		     } catch (Exception e) {
		    	 log.error("getListMenu:ERROR:"+e);
		         throw e;
		     }
		    return listMenu;
	 }
	 
	 public List<NavigItem> getListNavigMenu() throws Exception{
		 
		 log.info("navigManager:getListNavigMenu:01");
		 
		 int level_curr=-1, level_prev=-1;
		 NavigItem ni_prev=null;
		 NavigItem ni_parent=null;
		 try {
		    	
		    	if(listNavigMenu==null){  //listNavigMenu=new ArrayList<NavigItem>();
		    		
		    		List <NavigItem> navigList=(List <NavigItem>)Component.getInstance("navigList", ScopeType.SESSION);
			    	
		    		if(navigList==null) {
		    		
		    		  navigList=new ArrayList<NavigItem>();
		    			
		    		  log.info("navigManager:getListNavigMenu:02");
		    			
		    		  List<Object[]> lo=
		    				entityManager.createNativeQuery(
		    						    // "SELECT N.ID_SRV, N.UP, N.PAGE_CODE, LPAD(' ', (LEVEL - 1) * 2) || N.PAGE_NAME PAGE_NAME , LEVEL 
                                          "SELECT N.ID_SRV, N.UP, N.PAGE_CODE, N.PAGE_NAME, LEVEL "+
                                          "FROM AC_APP_DOMAINS_BSS_T n "+ 
                                          "WHERE N.IS_VISIBLE=1 "+
                                          "CONNECT BY  PRIOR N.ID_SRV = N.UP "+
                                          "START WITH N.UP= "+
                                          "(SELECT N2.ID_SRV FROM AC_APP_DOMAINS_BSS_T n2 WHERE N2.UP_IS=? and N2.UP=1 )"+
                                          "ORDER SIBLINGS BY  N.ORDER_NUM, N.PAGE_NAME ")
			                .setParameter(1, linksMap.getAppCode())
			       	      	.getResultList();
			        		
			          		 
			        		for(Object[] objectArray :lo){
			    	     		
			        			level_curr=Integer.parseInt(objectArray[4].toString());
			        			
			        			NavigItem navi= new NavigItem(
			    	    				(objectArray[0]!=null?objectArray[0].toString():""),
			    	    				(objectArray[2]!=null?objectArray[2].toString():""),
			    	    				(objectArray[3]!=null?objectArray[3].toString():""));

			        			if(level_curr==1){
			        				
			        				navigList.add(navi);
			        				
			        			}else{
			        			
			                      if(level_curr>level_prev){ //пошли дочерние зописи
			        				
			        				ni_parent=ni_prev;
			        				  
			        				if(ni_parent.getChildren()==null){
			        					ni_parent.setChildren(new ArrayList<NavigItem>());
			        				}
			        				
			        				navi.setParent(ni_parent);
			        				ni_parent.getChildren().add(navi);
			        				
			        			  }else if(level_curr==level_prev){
			        				
			        				 navi.setParent(ni_parent);
			        				 ni_parent.getChildren().add(navi);
			        				
			        			  }else if(level_curr<level_prev){ //прошли дочерние зописи
			        				  ni_parent.getParent().getChildren().add(navi);
			        			  }
			        			}
			        			level_prev=level_curr;
			        			ni_prev=navi;
			    	     	}
			        		
			        	 Contexts.getSessionContext().set("navigList", navigList);	
			  	   }
		    	   listNavigMenu=navigList;
		    	}
		     } catch (Exception e) {
		    	 log.error("getListNavigMenu:ERROR:"+e);
		         throw e;
		     }
		    return listNavigMenu;
	  }
	 
	 
	/* 
	 public List<NavigItem> getListNavigMenu() throws Exception{
		 log.info("navigManager:getListNavigMenu:01");
		 
		    try {
		    	if(listNavigMenu==null){
		    		listNavigMenu=new ArrayList<NavigItem>();
		    	
		    		List<Object[]> lo=
		    				entityManager.createNativeQuery(
			        				      "select AAP1.ID_SRV, AAP1.PAGE_CODE, AAP1.PAGE_NAME from AC_APP_DOMAINS_BSS_T aap1, "+
                                          "AC_APP_DOMAINS_BSS_T aap2 "+  
                                          "where aap1.UP = aap2.ID_SRV and  aap2.UP = 1 and  aap2.UP_IS=? "+
                                          "and aap1.IS_VISIBLE = 1 order by AAP1.ORDER_NUM ")
			                .setParameter(1, linksMap.getAppCode())
			       	      	.getResultList();
			        		
			          		 
			        		for(Object[] objectArray :lo){
			    	     			
			    	    		NavigItem navi= new NavigItem(
			    	    				(objectArray[0]!=null?objectArray[0].toString():""),
			    	    				(objectArray[1]!=null?objectArray[1].toString():""),
			    	    				(objectArray[2]!=null?objectArray[2].toString():""));
			    	    			
			    	    		listNavigMenu.add(navi);
			    	     	}
		    	}
		     } catch (Exception e) {
		    	 log.error("getListNavigMenu:ERROR:"+e);
		         throw e;
		     }
		    return listNavigMenu;
	  }
	 
	 public List<NavigItem> getListNavigInnerMenu(String idRes) throws Exception{
		 log.info("navigManager:getListNavigInnerMenu:idRes:"+idRes);
		 
		  List<NavigItem> result=new ArrayList<NavigItem>();
		  
		  if(idRes==null||idRes.equals("")){
			  return result;
		  }
		  try {
		    	List<Object[]> lo=
		    				entityManager.createNativeQuery(
		    						 "select AAP.ID_SRV, AAP.PAGE_CODE, AAP.PAGE_NAME "+
	                                 "from AC_APP_DOMAINS_BSS_T aap "+
	                                 "where aap.UP = ? and aap.IS_VISIBLE = 1 "+
	                                 "order by AAP.ORDER_NUM ")
			                .setParameter(1, new Long(idRes))
			       	      	.getResultList();
			         		 
			        		for(Object[] objectArray :lo){
			    	    			
			    	    		NavigItem navi= new NavigItem(
			    	    				(objectArray[0]!=null?objectArray[0].toString():""),
			    	    				(objectArray[1]!=null?objectArray[1].toString():""),
			    	    				(objectArray[2]!=null?objectArray[2].toString():""));
			    	    			
			    	    		result.add(navi);
			    	     	}
		      } catch (Exception e) {
		    	 log.error("getListNavigInnerMenu:ERROR:"+e);
		         throw e;
		     }
		    return result;
	   }*/
	 
	 public String getActiveSection(){
		 if(activeSection==null){
		  String actSect = FacesContext.getCurrentInstance().getExternalContext()
	         .getRequestParameterMap()
	         .get("actSect");
		  log.info("getActiveSection:actSect:"+actSect);
		/*  if(actSect==null){
			 activeSection=new Long(0);
		  }else{
			 activeSection=new Long(actSect);
		  }*/
		  if(actSect==null){
			 activeSection="#topnav_0";
		  }else{
			 activeSection="#topnav_"+actSect;
		  }
		 }
		 return activeSection;
	 }
	 
	 public void setActiveSection(String activeSection){
		this.activeSection=activeSection;
	 }

 /*
 for(NavigItem navi:list){
	 System.out.println("testMenu:name:"+navi.getPageName()); 
	 System.out.println("testMenu:code:"+navi.getPageCode()); 
	 recursion(navi);
 }
 private void recursion(NavigItem navi){
	 
	if(navi.getChildren()!=null){ 
	 System.out.println("recursion:list:"+navi.getChildren().size());
	 
	 for(NavigItem ni:navi.getChildren()){
		 System.out.println("recursion:name:"+ni.getPageName()); 
		 System.out.println("recursion:code:"+ni.getPageCode()); 
		recursion(ni);
		 
	 }
	}
 }*/
}
/*
 SELECT N.ID_SRV, N.UP, N.PAGE_CODE, LPAD(' ', (LEVEL - 1) * 2) || N.PAGE_NAME PAGE_NAME , LEVEL 
FROM AC_APP_DOMAINS_BSS_T n  
WHERE N.IS_VISIBLE=1 
CONNECT BY  PRIOR N.ID_SRV = N.UP
START WITH N.UP=
(SELECT N2.ID_SRV FROM AC_APP_DOMAINS_BSS_T n2 WHERE N2.UP_IS=1 and N2.UP=1  )
ORDER SIBLINGS BY  N.ORDER_NUM, N.PAGE_NAME
*/
