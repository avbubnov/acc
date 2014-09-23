package iac.grn.infosweb.context.mc.ugroup;

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


@Name("ugroupStateHolder")
@Scope(ScopeType.SESSION)
@AutoCreate
public class UgroupStateHolder {
	
	   @Logger private Log log;
	
       private HashMap<String, String> sortOrders = new HashMap<String, String>();
       private HashMap<String, String> columnFilterValues = new HashMap<String, String>();
      
       private List <String> auditFieldListSelect = new ArrayList<String>();
       
       private int pageNumber;
       
       @Create
       public void create() {
    	   log.info("datatableStateHolder:create");
    	
    	 /*  auditFieldListSelect.add("extendedTimestamp");
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
       public HashMap<String, String> getColumnFilterValues() {
    	//   log.info("getColumnFilterValues:01");
           return columnFilterValues;
       }
       public void setColumnFilterValues(HashMap<String, String> columnFilterValues) {
    	//   log.info("setColumnFilterValues:02"); 
    	   this.columnFilterValues = columnFilterValues;
       }
       public HashMap<String, String> getSortOrders() {
               return sortOrders;
       }
       public void setSortOrders(HashMap<String, String> sortOrders) {
                this.sortOrders = sortOrders;
       }
       public void clearFilters(){
    	   log.info("clearFilters:01");
    	   if(columnFilterValues!=null){
          	//	log.info("clearFilters:02");
          		for(Iterator<Map.Entry<String, String>> it = columnFilterValues.entrySet().iterator(); it.hasNext();)
    			{
    			      Map.Entry<String, String> me = it.next();
    			    //  System.out.print("me.getKey:"+me.getKey());
    	     		//  log.info("; me.getValue:"+me.getValue());
    	     		  if(me.getValue()==null||me.getValue().isEmpty()){
    	     			  log.info("Ahtung!!!");
    	     			  it.remove();
    	     		   }
    			}
          }
       }
       public void sort(String itemField){
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
       }
       
       public void sort(){
    	   String itemField = FacesContext.getCurrentInstance().getExternalContext()
  	             .getRequestParameterMap()
  	             .get("itemField");
  	 
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
       }
       
       public int getPageNumber() {  
      		return this.pageNumber; 
         }       
         public void setPageNumber(int pageNumber) { 
      		this.pageNumber= pageNumber; 
         } 

         public void resetPageNumber() { 
      		this.pageNumber=1; 
         } 
         
    }