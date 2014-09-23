package iac.grn.infosweb.session.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.faces.context.FacesContext;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.log.Log;
import org.richfaces.model.SortOrder;


@Name("supportStateHolder")
@Scope(ScopeType.SESSION)
@AutoCreate
public class SupportStateHolder {
	
       @Logger private Log log;
	
       private HashMap<String, HashMap<String, String>> sortOrders = new HashMap<String, HashMap<String, String>>();
       private HashMap<String, HashMap<String, String>> columnFilterValues = new HashMap<String, HashMap<String, String>>();
      
       private List <String> auditFieldListSelect = new ArrayList<String>();
       
     //  private HashMap<String, JournSeanceContextItem> seanceContext = new HashMap<String, JournSeanceContextItem>();
       
       @Create
       public void create() {
    	   log.info("datatableStateHolder:create");
    	
    	/* auditFieldListSelect.add("extendedTimestamp");
   		   auditFieldListSelect.add("objectName");
		   auditFieldListSelect.add("osUser");
		   auditFieldListSelect.add("osHost");
		   auditFieldListSelect.add("sessionId");*/
   		   
        }
       
       public List <String> getAuditFieldListSelect() {
    	   return this.auditFieldListSelect;
       }
       public void setAuditFieldListSelect(List <String> auditFieldListSelect) {
    	   this.auditFieldListSelect=auditFieldListSelect;
       }
       
       public HashMap<String, HashMap<String, String>> getColumnFilterValues() {
           return columnFilterValues;
       }
       public HashMap<String, String> getColumnFilterValues(String modelType) {
    	   log.info("datatableStateHolder:getColumnFilterValues:01");
    	   if(!columnFilterValues.containsKey(modelType)){
    		   log.info("datatableStateHolder:getColumnFilterValues:02");
    		   columnFilterValues.put(modelType, new HashMap<String, String>());
    	   }
           return columnFilterValues.get(modelType);
       }
       public void setColumnFilterValues(HashMap<String, HashMap<String, String>> columnFilterValues) {
    	   log.info("datatableStateHolder:setColumnFilterValues");
    	   this.columnFilterValues = columnFilterValues;
       }
       
       public HashMap<String, HashMap<String, String>> getSortOrders() {
               return sortOrders;
       }
       public void setSortOrders(HashMap<String, HashMap<String, String>> sortOrders) {
                this.sortOrders = sortOrders;
       }
       
       public void clearFilters(String modelType){
    	   log.info("clearFilters:01");
    	   if(columnFilterValues!=null){
          	
    		   if(columnFilterValues.containsKey(modelType)){
    			  HashMap<String, String> hm = columnFilterValues.get(modelType);
          		  for(Iterator<Map.Entry<String, String>> it = hm.entrySet().iterator(); it.hasNext();){
    			      Map.Entry<String, String> me = it.next();
    			 //   System.out.print("me.getKey:"+me.getKey());
    	     	//	  log.info("; me.getValue:"+me.getValue());
    	     		  if(me.getValue()==null||me.getValue().isEmpty()||me.getValue().equals("#-1#")){
    	     			  log.info("Ahtung!!!");
    	     			  it.remove();
    	     		   }
    			}
    	     }
          }
       }
       
       public void clearAll(String modelType){
    	   log.info("clearAllFilters:01:modelType:"+modelType);
    	   if(sortOrders!=null){
      		   if(sortOrders.containsKey(modelType)){
    			//   log.info("clearAllFilters:02");
    			   
    			  HashMap<String, String> hm = sortOrders.get(modelType);
          		  for(Iterator<Map.Entry<String, String>> it = hm.entrySet().iterator(); it.hasNext();){
          			Map.Entry<String, String> me = it.next();
  			        it.remove();
    			}
    	     }
          }
    	  if(columnFilterValues!=null){
     		   if(columnFilterValues.containsKey(modelType)){
    			  HashMap<String, String> hm = columnFilterValues.get(modelType);
          		  for(Iterator<Map.Entry<String, String>> it = hm.entrySet().iterator(); it.hasNext();){
    			      Map.Entry<String, String> me = it.next();
    		  	      it.remove();
    			}
    	     }
          }
       }
       
   /*    public void sort(String itemField){
    	   log.info("sort:itemField:"+itemField);
    	   
    	   String value=null;
    	   
    	   if(this.sortOrders.containsKey(itemField)){
    		   value=this.sortOrders.get(itemField);
    		   if(value!=null && value.equals("asc")){
    			   this.sortOrders.put(itemField, "desc");
    		   }else{
    			   this.sortOrders.put(itemField, "asc");
    		   }
    	   }else{
    		   this.sortOrders.clear();
    		   this.sortOrders.put(itemField, "asc");  
    	   }
    	   log.info("sort:02");
       }*/
    
       public void filterSelect(){
    	   String modelType = FacesContext.getCurrentInstance().getExternalContext()
      	         .getRequestParameterMap()
      	         .get("modelType");
    	   String fieldName = FacesContext.getCurrentInstance().getExternalContext()
  	             .getRequestParameterMap()
  	             .get("fieldName");
    	   String fieldValue = FacesContext.getCurrentInstance().getExternalContext()
    	         .getRequestParameterMap()
    	        .get("fieldValue");
    	   log.info("filterSelect:modelType:"+modelType);
    	   log.info("filterSelect:fieldName:"+fieldName);
  	       log.info("filterSelect:fieldValue:"+fieldValue);
  	       
  	  	   String value=null;
    	   
  	  	   HashMap<String, String> fv = getColumnFilterValues(modelType);
  	  	
  	  	   fv.put(fieldName, fieldValue);
  	  	   
    	   log.info("filterSelect:02");
       }
       
       public void sort(){
    	   String itemField = FacesContext.getCurrentInstance().getExternalContext()
  	             .getRequestParameterMap()
  	             .get("itemField");
  	       log.info("sort:itemField:"+itemField);
  	       
  	       String modelType = FacesContext.getCurrentInstance().getExternalContext()
  	             .getRequestParameterMap()
  	             .get("modelType");
  	 
    	   log.info("sort:modelType:"+modelType);
    	   String value=null;
    	   
    	   if(this.sortOrders.containsKey(modelType)){
    		   
    		   if(this.sortOrders.get(modelType).containsKey(itemField)){
        		   value=this.sortOrders.get(modelType).get(itemField);
        		   if(value!=null && value.equals("asc")){
        			   this.sortOrders.get(modelType).put(itemField, "desc");
        		   }else{
        			   this.sortOrders.get(modelType).put(itemField, "asc");
        		   }
        	   }else{
        		   this.sortOrders.get(modelType).clear();
        		   this.sortOrders.get(modelType).put(itemField, "asc");  
        	   }
    	   }else{
    		   HashMap<String, String> hm = new HashMap<String, String>();
    		   hm.put(itemField, "asc");
    		   this.sortOrders.put(modelType, hm);
    	   }
    	   
       	   log.info("sort:02");
       }
       public String getSortImg(String modelType, String itemField) {
           if(this.sortOrders.containsKey(modelType)){
    		   
    		   if(this.sortOrders.get(modelType).containsKey(itemField)){
        		   return this.sortOrders.get(modelType).get(itemField);
        	   }else{
        		   return "";
        	   }
    	   }else{
    		   return "";
    	   }
           
       }
       
   /*    public HashMap<String, JournSeanceContextItem> getSeanceContext() {
           return seanceContext;
       }
       public void setSeanceContext(HashMap<String, JournSeanceContextItem> seanceContext) {
            this.seanceContext = seanceContext;
       }*/
    }