package iac.grn.infosweb.session.tree;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import iac.cud.infosweb.entity.*;

import java.util.*;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

/**
 * ”правл€ющий бин, осуществл€ющий построение, представление 
 * и преобразование навигационного дерева 
 * @author bubnov
 *
 */
@Name("treeManager")
public class TreeManager {

	 @Logger private Log log;
	
	 @In 
	 EntityManager entityManager;
	
	   /**
	     * ћенеджер сущностей, обеспечивающий взаимодействие с Ѕƒ
	     */
	 private Map<Long, AcAppPage> childrenMap = 
				new LinkedHashMap<Long, AcAppPage>();

	// private Long rootNode;
	 /**
	  * ѕервые дочерние узлы дерева навигации
	  * @return спиок узлов
	  */
	 public Map<Long, AcAppPage> getChildrenMap(){
	//	 log.info("---getCurrentNavigator-01_currentItem="+currentItem);
		
		 log.info("TreeManager:getChildrenMap:01");
		 if(childrenMap.isEmpty()){
			 log.info("TreeManager:getChildrenMap:02");
			 /*
			String gl_nid = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("gl_nid");	  
			String gl_tid = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("gl_tid");	  
			Long idTree=null;
			
			if(currentItem!=null){
				idTree=new Long(currentItem);
			}else{
			    idTree=(gl_tid!=null?new Long(gl_tid):new Long(2));
			}
			
			log.info("-+-getCurrentNavigator-gl_nid="+gl_nid);
			log.info("---getCurrentNavigator-gl_nid2="+gl_tid);
			
			*/ 
			
			AcAppPage nav=(AcAppPage)entityManager /*((EntityManager)
					      Component.getInstance("entityManager"))*/
					    .createQuery("select aap from AcAppPage aap where aap.idRes =:idRes ")
					     // .setParameter("idNav",idTree)
					      .setParameter("idRes",new Long(1))
				 	      .getSingleResult();
		//	log.info("---getCurrentNavigator-02");
			for(AcAppPage nl: nav.getIdResCollection()){
				childrenMap.put(nl.getIdRes(), nl);
			 }

			//listNavigator=nav.getNavigatorCollection();
		/*	if(currentNodeObject==null){
				log.info("---getCurrentNavigator-03");
				loadCurrentTree("2");
			}*/
		 
		   }
			return childrenMap;
		 
	 }
	 /**
	  * 
	  * @param дочерний узел дерева навигации
	  * @return дочерний узел
	  */
	 public Map<Long, AcAppPage> getChild(AcAppPage pn){
		//  log.info("treeManager:getChild:pn==null:"+(pn==null));
		  
		  Map<Long, AcAppPage> nnn=new LinkedHashMap<Long, AcAppPage>();
		  
		   if(pn!=null){
		//	  log.info("treeManager:getChild:IdRes:1:"+pn.getIdRes());
		  
		      AcAppPage pnch =entityManager.find(AcAppPage.class, pn.getIdRes());
		  
		      if(pnch.getIdResCollection()!=null){
		        for (AcAppPage nl: pnch.getIdResCollection()) {
		      //    log.info("treeManager:getChild:IdRes:2:"+nl.getIdRes());	
		    	  nnn.put(nl.getIdRes(), nl);
	            }
		      }
	       }
		  return nnn;
	  }
  /*   public Long getRootNode(){
    	 return rootNode;
     }
     public void setRootNode(Long rootNode){
    	 this.rootNode=rootNode;
     }*/
}
