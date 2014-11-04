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
 * ����������� ���, �������������� ����������, ������������� 
 * � �������������� �������������� ������ 
 * @author bubnov
 *
 */
@Name("treeManager")
public class TreeManager {

	 @Logger private Log log;
	
	 @In 
	 EntityManager entityManager;
	
	   /**
	     * �������� ���������, �������������� �������������� � ��
	     */
	 private Map<Long, AcAppPage> childrenMap = 
				new LinkedHashMap<Long, AcAppPage>();

	 /**
	  * ������ �������� ���� ������ ���������
	  * @return ����� �����
	  */
	 public Map<Long, AcAppPage> getChildrenMap(){
		
		 log.info("TreeManager:getChildrenMap:01");
		 if(childrenMap.isEmpty()){
			 log.info("TreeManager:getChildrenMap:02");
		
			
			AcAppPage nav=(AcAppPage)entityManager 
					    .createQuery("select aap from AcAppPage aap where aap.idRes =:idRes ")
					      .setParameter("idRes",new Long(1))
				 	      .getSingleResult();
			for(AcAppPage nl: nav.getIdResCollection()){
				childrenMap.put(nl.getIdRes(), nl);
			 }

		
		 
		   }
			return childrenMap;
		 
	 }
	 /**
	  * 
	  * @param �������� ���� ������ ���������
	  * @return �������� ����
	  */
	 public Map<Long, AcAppPage> getChild(AcAppPage pn){
		 
		  
		  Map<Long, AcAppPage> nnn=new LinkedHashMap<Long, AcAppPage>();
		  
		   if(pn!=null){
		  
		      AcAppPage pnch =entityManager.find(AcAppPage.class, pn.getIdRes());
		  
		      if(pnch.getIdResCollection()!=null){
		        for (AcAppPage nl: pnch.getIdResCollection()) {
		       	
		    	  nnn.put(nl.getIdRes(), nl);
	            }
		      }
	       }
		  return nnn;
	  }
 
}
